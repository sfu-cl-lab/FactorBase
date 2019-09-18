/* Cascade the information (based on the FunctorSet table) into the tables created by the transfer_initialize.sql script. */

TRUNCATE 1Nodes;
INSERT INTO 1Nodes
    SELECT
        N.1nid,
        N.COLUMN_NAME,
        N.pvid,
        N.main
    FROM
        @database@_setup.1Nodes N,
        @database@_setup.FunctorSet F
    WHERE
        N.1nid = F.fid;


TRUNCATE 2Nodes;
INSERT INTO 2Nodes
    SELECT
        N.2nid,
        N.COLUMN_NAME,
        N.pvid1,
        N.pvid2,
        N.TABLE_NAME,
        N.main
    FROM
        @database@_setup.2Nodes N,
        @database@_setup.FunctorSet F
    WHERE
        N.2nid = F.Fid;


/* Map the 2nodes to rnodes for the given 2Nodes in the functor set. */
TRUNCATE RNodes_2Nodes;
INSERT INTO RNodes_2Nodes
    SELECT
        N.rnid,
        N.2nid,
        N.main
    FROM
        @database@_setup.RNodes_2Nodes N,
        2Nodes F
    WHERE
        N.2nid = F.2nid;


/* Copy the rnodes for the functor set. */
TRUNCATE RNodes;
INSERT INTO RNodes
    SELECT
        N.rnid,
        N.TABLE_NAME,
        N.pvid1,
        N.pvid2,
        N.COLUMN_NAME1,
        N.COLUMN_NAME2,
        N.main
    FROM
        @database@_setup.RNodes N,
        @database@_setup.FunctorSet F
    WHERE
        N.rnid = F.Fid

    /* For each 2node that's included in the FunctorSet, copy its rnode as well in case the user missed it. */
    UNION DISTINCT

    SELECT
        N.rnid,
        N.TABLE_NAME,
        N.pvid1,
        N.pvid2,
        N.COLUMN_NAME1,
        N.COLUMN_NAME2,
        N.main
    FROM
        @database@_setup.RNodes N,
        RNodes_2Nodes F
    WHERE
        N.rnid = F.rnid;


/* Make comprehensive table for all functor nodes but restricted to the functor set. */
TRUNCATE FNodes;
INSERT INTO FNodes
    SELECT
        1nid AS Fid,
        COLUMN_NAME AS FunctorName,
        '1Node' AS Type,
        main
    FROM
        1Nodes

    UNION DISTINCT

    SELECT
        2nid AS Fid,
        COLUMN_NAME AS FunctorName,
        '2Node' AS Type,
        main
    FROM
        2Nodes

    UNION DISTINCT

    SELECT
        rnid AS Fid,
        TABLE_NAME AS FunctorName,
        'Rnode' AS Type,
        main
    FROM
        RNodes;


/* Transfer links to pvariables.  Restrict only to functor nodes in the functor set, now known as FNodes. */
TRUNCATE FNodes_pvars;
INSERT INTO FNodes_pvars
    SELECT
        N.Fid,
        N.pvid
    FROM
        @database@_setup.FNodes_pvars N,
        FNodes F
    WHERE
        N.Fid = F.Fid;


TRUNCATE RNodes_pvars;
INSERT INTO RNodes_pvars
    SELECT
        N.rnid,
        N.pvid,
        N.TABLE_NAME,
        N.COLUMN_NAME,
        N.REFERENCED_COLUMN_NAME
    FROM
        @database@_setup.RNodes_pvars N,
        RNodes F
    WHERE
        N.rnid = F.rnid;


/* Transfer pvariables.  Only those that occur in the functor set. */
TRUNCATE PVariables;
INSERT INTO PVariables
    SELECT DISTINCT
        N.pvid,
        N.ID_COLUMN_NAME,
        N.TABLE_NAME,
        N.index_number
    FROM
        @database@_setup.PVariables N,
        FNodes_pvars F
    WHERE
        F.pvid = N.pvid;


/**
 * Transfer the rest.
 */
TRUNCATE EntityTables;
INSERT INTO EntityTables
    SELECT
        *
    FROM
        @database@_setup.EntityTables;


TRUNCATE Attribute_Value;
INSERT INTO Attribute_Value
    SELECT
        *
    FROM
        @database@_setup.Attribute_Value;


TRUNCATE Expansions;
INSERT INTO Expansions
    SELECT
        *
    FROM
        @database@_setup.Expansions;


TRUNCATE Groundings;
INSERT INTO Groundings
    SELECT
        *
    FROM
        @database@_setup.Groundings;


/**
 * Prepare lattice generator by copying information from Rnodes to a new temporary table.
 * The temporary table will have the original rnids and short rnids.
 */
TRUNCATE LatticeRNodes;
INSERT INTO LatticeRNodes
    SELECT
        rnid AS orig_rnid,
        TABLE_NAME,
        pvid1,
        pvid2,
        COLUMN_NAME1,
        COLUMN_NAME2,
        main,
        NULL
    FROM
        RNodes;