SET storage_engine=INNODB;

/**
 * Create the required tables/views for model manager.
 */

CREATE TABLE RNodes_BN_Nodes AS
    SELECT DISTINCT
        rnid,
        2nid AS Fid,
        main
    FROM
        RNodes_2Nodes
    LIMIT
        0;

ALTER TABLE `RNodes_BN_Nodes` ADD INDEX `Index_rnid` (rnid ASC);


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

CREATE VIEW Final_Path_BayesNets_view AS
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
 * Prepare output view with longest rchain only.
 */
CREATE TABLE NewLearnedEdges LIKE Path_BayesNets;


CREATE TABLE Path_BN_nodes AS
    SELECT DISTINCT
        lattice_membership.name AS Rchain,
        Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    LIMIT
        0;

ALTER TABLE Path_BN_nodes ADD INDEX HashIndex (Rchain, node);


/**
 * Create tables that allow us to represent background knowledge.
 * Note: The Knowledge_*_Edges tables are filled in with user provided information.
 */
CREATE TABLE Knowledge_Forbidden_Edges LIKE Path_BayesNets;
CREATE TABLE Knowledge_Required_Edges LIKE Path_BayesNets;

CREATE TABLE Path_Aux_Edges AS
    SELECT
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        FNodes
    LIMIT
        0;

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
    LIMIT
        0;

ALTER TABLE SchemaEdges ADD INDEX HashIn (Rchain, child, parent);


CREATE TABLE Path_Required_Edges LIKE Path_BayesNets;
CREATE TABLE Path_Forbidden_Edges LIKE Path_BayesNets;
CREATE TABLE ContextEdges LIKE Path_BayesNets;
CREATE TABLE InheritedEdges LIKE Path_BayesNets;


/**
 * Extendibility Note: Could allow a user or program to create a table with its required forbidden edges, simply union
 * those into this construct.
 */
CREATE VIEW Entity_BN_nodes AS
    SELECT
        Entity_BayesNets.pvid AS pvid,
        Entity_BayesNets.child AS node
    FROM
        Entity_BayesNets ORDER BY pvid;


CREATE TABLE Entity_Complement_Edges AS
    SELECT DISTINCT
        BN_nodes1.pvid AS pvid,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Entity_BN_nodes AS BN_nodes1,
        Entity_BN_nodes AS BN_nodes2
    LIMIT
        0;

ALTER TABLE `Entity_Complement_Edges` ADD PRIMARY KEY (pvid, child, parent);


CREATE TABLE Path_Complement_Edges LIKE Path_BayesNets;