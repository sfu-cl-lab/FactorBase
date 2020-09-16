/* Cascade the information (based on the FunctorSet table) into the tables created by the transfer_initialize.sql script. */

CREATE PROCEDURE cascadeFS()
BEGIN

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
        P.pvid,
        P.ID_COLUMN_NAME,
        P.TABLE_NAME,
        P.index_number
    FROM
        @database@_setup.PVariables P,
        @database@_setup.FNodes_pvars FP,
        FNodes F
    WHERE
        P.pvid = FP.pvid
    AND
        FP.Fid = F.Fid;


/**
 * Prepare lattice generator by copying information from LatticeRNodes in the "_setup" database, restricted to the
 * FunctorSet.
 */
TRUNCATE LatticeRNodes;
INSERT INTO LatticeRNodes
    SELECT
        LR.orig_rnid,
        LR.short_rnid
    FROM
        @database@_setup.LatticeRNodes LR,
        RNodes R
    WHERE
        LR.orig_rnid = R.rnid;

END//