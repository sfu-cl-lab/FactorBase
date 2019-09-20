SET storage_engine=INNODB;

/**
 * Find columns associated with Rnodes. These should be the same as the ones used in GROUP BY clauses except that you
 * also use the main auxilliary information.
 *
 * TODO: should reconcile Rnodes_BN_NOdes and Path_BN_Nodes with metaqueries at some point.
 */

CREATE TABLE RNodes_BN_Nodes AS
    SELECT DISTINCT
        rnid,
        1nid AS Fid,
        N.main
    FROM
        RNodes_pvars R,
        PVariables P,
        1Nodes N
    WHERE
        R.pvid = P.pvid
    AND
        R.pvid = N.pvid

    UNION DISTINCT

    SELECT DISTINCT
        rnid,
        2nid AS Fid,
        main
    FROM
        RNodes_2Nodes

    UNION DISTINCT

    SELECT DISTINCT
        rnid,
        rnid AS Fid,
        main
    FROM
        RNodes;


CREATE TABLE  Entity_BayesNets (
    pvid VARCHAR(65) NOT NULL,
    child VARCHAR(131) NOT NULL,
    parent VARCHAR(131) NOT NULL,
    PRIMARY KEY (pvid, child, parent)
);


/**
 * Create tables to store Bayes Nets in lattice chains.
 *
 * zqian, max key length limitation: "The maximum column size is 767 bytes".
 * Enable "innodb_large_prefix" to allow index key prefixes longer than 767 bytes (up to 3072 bytes).
 * Oct 17, 2013
 */

CREATE TABLE Path_BayesNets (
    Rchain VARCHAR(255) NOT NULL,
    child VARCHAR(197) NOT NULL,
    parent VARCHAR(197) NOT NULL,
    PRIMARY KEY (Rchain, child, parent)
);

CREATE OR REPLACE VIEW Final_Path_BayesNets_view AS
    SELECT
        *
    FROM
        Path_BayesNets
    WHERE
        character_length(Rchain) = (
            SELECT
                MAX(character_length(Rchain))
            FROM
                Path_BayesNets
        );


/**
 * Parepare output view with longest rchain only.
 */
CREATE TABLE NewLearnedEdges LIKE Path_BayesNets;


/**
 * Propagate BNnodes to rchains.
 */
ALTER TABLE `RNodes_BN_Nodes` ADD INDEX `Index_rnid` (rnid ASC);


CREATE TABLE Path_BN_nodes AS
    SELECT DISTINCT
        lattice_membership.name AS Rchain,
        Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.member
    ORDER BY
        lattice_membership.name;


ALTER TABLE Path_BN_nodes ADD INDEX HashIndex (Rchain, node);


/**
 * Create tables that allow us to represent background knowledge.
 */

CREATE TABLE IF NOT EXISTS Knowledge_Forbidden_Edges LIKE Path_BayesNets;
CREATE TABLE IF NOT EXISTS Knowledge_Required_Edges LIKE Path_BayesNets;

CREATE TABLE Path_Aux_Edges AS
    SELECT
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        FNodes
    WHERE
        BN_nodes1.Rchain = BN_nodes2.Rchain
    AND
        FNodes.Fid = BN_nodes1.node
    AND
        FNodes.main = 0;


/**
 * The next primary key makes an index that's too long.  innodb_enable_large_prefix would fix that. But will be
 * deprecated.  For now we comment out, may slow down learning.
 */
-- ALTER TABLE Path_Aux_Edges99 ADD PRIMARY KEY (Rchain, child, parent);


CREATE TABLE SchemaEdges AS
    SELECT DISTINCT
        lattice_membership.name AS Rchain,
        2Nodes.2nid AS child,
        RNodes.rnid AS parent
    FROM
        RNodes,
        2Nodes,
        lattice_membership
    WHERE
        lattice_membership.member = RNodes.rnid
    AND
        RNodes.pvid1 = 2Nodes.pvid1
    AND
        RNodes.pvid2 = 2Nodes.pvid2
    AND
        RNodes.TABLE_NAME = 2Nodes.TABLE_NAME
    AND
        lattice_membership.name IN (
            SELECT
                lattice_set.name
            FROM
                lattice_set
            WHERE
                length = (
                    SELECT
                        MAX(length)
                    FROM
                        lattice_set
                )
        );


ALTER TABLE SchemaEdges ADD INDEX HashIn (Rchain, child, parent);


CREATE TABLE Path_Required_Edges LIKE Path_BayesNets;


INSERT IGNORE INTO Path_Required_Edges
    SELECT DISTINCT
        *
    FROM
        Knowledge_Required_Edges;


INSERT IGNORE INTO Path_Required_Edges
    SELECT  DISTINCT
        *
    FROM
        SchemaEdges;


CREATE TABLE Path_Forbidden_Edges LIKE Path_BayesNets;


INSERT IGNORE INTO Path_Forbidden_Edges
    SELECT DISTINCT
        *
    FROM
        Knowledge_Forbidden_Edges;


INSERT IGNORE INTO Path_Forbidden_Edges
    SELECT DISTINCT
        *
    FROM
        Path_Aux_Edges;


CREATE TABLE LearnedEdges LIKE Path_BayesNets;
CREATE TABLE ContextEdges LIKE Path_BayesNets;
CREATE TABLE InheritedEdges LIKE Path_BayesNets;


/**
 * Extendibility Note: Could allow a user or program to create a table with its required forbidden edges, simply union
 * those into this construct.
 */
CREATE OR REPLACE VIEW Entity_BN_nodes AS
    SELECT
        Entity_BayesNets.pvid AS pvid,
        Entity_BayesNets.child AS node
    FROM
        Entity_BayesNets ORDER BY pvid;


/**
 * Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net.
 * Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around.
 */
CREATE TABLE Entity_Complement_Edges AS
    SELECT DISTINCT
        BN_nodes1.pvid AS pvid,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Entity_BN_nodes AS BN_nodes1,
        Entity_BN_nodes AS BN_nodes2
    WHERE
        BN_nodes1.pvid = BN_nodes2.pvid
    AND (
        NOT (EXISTS (
            SELECT
                *
            FROM
                Entity_BayesNets
            WHERE
                Entity_BayesNets.pvid = BN_nodes1.pvid
            AND
                Entity_BayesNets.child = BN_nodes1.node
            AND
                Entity_BayesNets.parent = BN_nodes2.node
        ))
    );


ALTER TABLE `Entity_Complement_Edges` ADD PRIMARY KEY (pvid, child, parent);


/**
 * For each singleton relationship, find the associated functor ids.  Union these for each lattice point that contains
 * the singleton.  Including the singleton itself.
 */

CREATE TABLE Path_Complement_Edges LIKE Path_BayesNets;

/**
 * Dealing with the Main Functor Constraint. The auxilliary functor nodes are those that are not main functor nodes. The
 * aux edges are the ones that point to an auxilliary node.  Our approach is this: for each node in the relationship
 * lattice, find the nodes in its associated Bayes net.  If such a node is an auxilliary node, any edge pointing into
 * this node is forbidden.  This builds a fairly large view.  We could be more efficient in terms of storage by
 * computing this dynamically as we learn the Bayes nets going up the lattice.
 *
 * Finally, the forbidden edges for the Bayes nets are:
 * 1) inherited from the entity tables for singleton relationship sets
 * 2) inherited from the subsets for relationships of size > 1
 * 3) the edges pointing into the main edges.
 *
 * Assumes that lattice_rel gives the subsets that are one size smaller.
 */