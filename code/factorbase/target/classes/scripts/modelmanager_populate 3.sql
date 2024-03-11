/**
 * Find columns associated with Rnodes. These should be the same as the ones used in GROUP BY clauses except that you
 * also use the main auxilliary information.
 *
 * TODO: should reconcile Rnodes_BN_NOdes and Path_BN_Nodes with metaqueries at some point.
 */

TRUNCATE RNodes_BN_Nodes;
INSERT INTO RNodes_BN_Nodes
    SELECT DISTINCT
        rnid,
        1nid AS Fid,
        N.main
    FROM
        @database@_setup.RNodes_pvars R,
        @database@_setup.PVariables P,
        @database@_setup.1Nodes N
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
        @database@_setup.RNodes_2Nodes

    UNION DISTINCT

    SELECT DISTINCT
        rnid,
        rnid AS Fid,
        main
    FROM
        @database@_setup.RNodes;


/**
 * Propagate BNnodes to rchains.
 */
TRUNCATE Path_BN_nodes;
INSERT INTO Path_BN_nodes
    SELECT DISTINCT
        LM.name AS Rchain,
        Fid AS node
    FROM
        @database@_setup.lattice_membership LM,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = LM.member
    ORDER BY
        LM.name;



TRUNCATE Path_Aux_Edges;
INSERT INTO Path_Aux_Edges
    SELECT
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        @database@_setup.FNodes F
    WHERE
        BN_nodes1.Rchain = BN_nodes2.Rchain
    AND
        F.Fid = BN_nodes1.node
    AND
        F.main = 0;


TRUNCATE SchemaEdges;
INSERT INTO SchemaEdges
    SELECT DISTINCT
        LM.name AS Rchain,
        2N.2nid AS child,
        R.rnid AS parent
    FROM
        @database@_setup.RNodes R,
        @database@_setup.2Nodes 2N,
        @database@_setup.lattice_membership LM
    WHERE
        LM.member = R.rnid
    AND
        R.pvid1 = 2N.pvid1
    AND
        R.pvid2 = 2N.pvid2
    AND
        R.TABLE_NAME = 2N.TABLE_NAME
    AND
        LM.name IN (
            SELECT
                LS.name
            FROM
                @database@_setup.lattice_set LS
            WHERE
                length = (
                    SELECT
                        MAX(length)
                    FROM
                        @database@_setup.lattice_set
                )
        );



TRUNCATE Path_Required_Edges;
INSERT INTO Path_Required_Edges
    SELECT DISTINCT
        *
    FROM
        Knowledge_Required_Edges;


INSERT INTO Path_Required_Edges
    SELECT  DISTINCT
        *
    FROM
        SchemaEdges;


INSERT IGNORE INTO Path_Required_Edges
    SELECT DISTINCT
        RP.rnid AS Rchain,
        Entity_BayesNets.child AS child,
        Entity_BayesNets.parent AS parent
    FROM
        @database@_setup.RNodes_pvars RP,
        Entity_BayesNets
    WHERE
        RP.pvid = Entity_BayesNets.pvid
    AND
        Entity_BayesNets.parent <> '';


TRUNCATE Path_Forbidden_Edges;
INSERT INTO Path_Forbidden_Edges
    SELECT DISTINCT
        *
    FROM
        Knowledge_Forbidden_Edges;


INSERT INTO Path_Forbidden_Edges
    SELECT DISTINCT
        *
    FROM
        Path_Aux_Edges;


/**
 * Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net.
 * Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around.
 */
TRUNCATE Entity_Complement_Edges;
INSERT INTO Entity_Complement_Edges
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


INSERT IGNORE INTO Path_Forbidden_Edges
    SELECT DISTINCT
        RP.rnid AS Rchain,
        Entity_Complement_Edges.child AS child,
        Entity_Complement_Edges.parent AS parent
    FROM
        @database@_setup.RNodes_pvars RP,
        Entity_Complement_Edges
    WHERE
        RP.pvid = Entity_Complement_Edges.pvid;


/**
 * For each singleton relationship, find the associated functor ids.  Union these for each lattice point that contains
 * the singleton.  Including the singleton itself.
 *
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