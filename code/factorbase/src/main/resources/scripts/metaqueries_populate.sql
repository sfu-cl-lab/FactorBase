/**
 * Script for creating metaqueries that are executed to compute contingency tables
 * can only be understood by reading our DSAA paper or at least the github documentation.
 * See also https://github.com/sfu-cl-lab/FactorBase/issues/46
 *
 * Assumes the presence of the following tables:
 * RNodes_2Nodes
 * RNodes_pvars
 */
CREATE PROCEDURE populateMQ()
BEGIN

DROP TABLE IF EXISTS MetaQueries;
CREATE TABLE MetaQueries (
    Lattice_Point varchar(199), /* e.g. pvid, rchain, prof0, a */
    TableType varchar(100), /* e.g. star, flat, counts */
    ClauseType varchar(10), /* FROM, WHERE, SELECT, GROUPBY */
    EntryType varchar(100), /* e.g. 1node, aggregate like count */
    Entries varchar(150)
) ENGINE = INNODB;

/* metaqueries for population variables */

--- map Pvariables to entity tables ---

INSERT INTO MetaQueries
    SELECT
        pvid AS Lattice_Point,
        'Counts' AS TableType,
        'FROM' AS ClauseType,
        'table' AS EntryType,
        CONCAT('@database@.', TABLE_NAME, ' AS ', pvid) AS Entries
    FROM
        PVariables;


/* August 18, 2017 OS. Could consider optimizing by using only main pvids */
/*
    WHERE
        index_number = 0;
*/

/* Pvariable SELECT list */
/* Contains 1nodes with renaming, and mult for aggregating, and id columns in case these are selected in the Expansions table. */
INSERT INTO MetaQueries
    SELECT
        pvid AS Lattice_Point,
        'Counts' AS TableType,
        'SELECT' AS ClauseType,
        'aggregate' AS EntryType,
        'COUNT(*) AS "MULT"' AS Entries
    FROM
        PVariables;


/* For each pvariable, find the SELECT list for the associated attributes = 1Nodes. */
INSERT INTO MetaQueries
    SELECT
        N.pvid AS Lattice_Point,
        'Counts' AS TableType,
        'SELECT' AS ClauseType,
        '1node' AS EntryType,
        CONCAT(
            N.pvid,
            '.',
            N.COLUMN_NAME,
            ' AS `',
            1nid,
            '`'
        ) AS Entries
    FROM
        1Nodes N;


/* For each pvariable in expansion, find the primary column and add it to the SELECT list. */
/* Don't use this for continuous, but do use it for the no_link case. */
INSERT INTO MetaQueries
    SELECT DISTINCT
        E.pvid AS Lattice_Point,
        'Counts' AS TableType,
        'SELECT' AS ClauseType,
        'id' AS EntryType,
        CONCAT(E.pvid, '.', P.ID_COLUMN_NAME, ' AS `ID(', E.pvid, ')`') AS Entries
    FROM
        PVariables P,
        @database@_setup.Expansions E
    WHERE
        E.pvid = P.pvid;


/* Pvariable GROUPBY list. */
/* Contains 1nodes without renaming. */
INSERT INTO MetaQueries
   SELECT
       N.pvid AS Lattice_Point,
       'Counts' AS TableType,
       'GROUPBY' AS ClauseType,
       '1node' AS EntryType,
        CONCAT(
            '`',
            1nid,
            '`'
        ) AS Entries
    FROM
        1Nodes N;


/* Add id columns for expansions without renaming. */
INSERT INTO MetaQueries
    SELECT DISTINCT
        E.pvid AS Lattice_Point,
        'Counts' AS TableType,
        'GROUPBY' AS ClauseType,
        'id' AS EntryType,
        CONCAT('`ID(', E.pvid, ')`') AS Entries
    FROM
        PVariables P,
        @database@_setup.Expansions E
    WHERE
        E.pvid = P.pvid;


/* Pvariable WHERE list. */
/* The only thing to add is any grounding constraints. */
INSERT INTO MetaQueries
    SELECT DISTINCT
        G.pvid AS Lattice_Point,
        'Counts' AS TableType,
        'WHERE' AS ClauseType,
        'id' AS EntryType,
        CONCAT('`ID(', G.pvid, ')` = ', G.id) AS Entries
    FROM
        @database@_setup.Groundings G;


/* Now we make data join tables for each relationship functor node. */
/* The FROM list for the relationship functor contains the relationship table. */
INSERT INTO MetaQueries
    SELECT
        rnid AS Lattice_Point,
        'Counts' AS TableType,
        'FROM' AS ClauseType,
        'rtable' AS EntryType,
        CONCAT(
            '@database@.',
            R.TABLE_NAME,
            ' AS `',
            rnid,
            '`'
        ) AS Entries
    FROM
        RNodes R;


/**
 * We add an expression to the SELECT list that returns "T" for "true" and whose header is
 * the rnid.  This simulates the case where all the relationships are true.
 */
INSERT INTO MetaQueries
    SELECT
        rnid AS Lattice_Point,
        'Counts' AS TableType,
        'SELECT' AS ClauseType,
        'rtable' AS EntryType,
        CONCAT(
            '"T" AS `',
            rnid,
            '`'
        ) AS Entries
    FROM
        RNodes R;


/**
 * The WHERE list for each rnode contains the join condition that links the foreign key pointers in the relationship
 * table to the ids in the population entity tables.
 */
INSERT INTO MetaQueries
    SELECT
        rnid AS Lattice_Point,
        'Counts' AS TableType,
        'WHERE' AS ClauseType,
        'rtable' AS EntryType,
        CONCAT(
            '`',
            rnid,
            '`.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME
        ) AS Entries
    FROM
        RNodes_pvars R;


/**
 * The SELECT list for an rnode contains:
 * 1) The rnid itself.  This is generated by the "T" expression above.
 * 2) The 2nodes of the relationship variable.
 */
INSERT INTO MetaQueries
    SELECT
        rnid AS Lattice_Point,
        'Counts' AS TableType,
        'SELECT' AS ClauseType,
        '2nid' AS EntryType,
        CONCAT(
            '`',
            rnid,
            '`.',
            COLUMN_NAME,
            ' AS `',
            N.2nid,
            '`'
        ) AS Entries
    FROM
        RNodes_2Nodes RN,
        2Nodes N
    WHERE
        N.2nid = RN.2nid;


/**
 * The GROUPBY List is like the SELECT list, but without renaming.
 */
INSERT INTO MetaQueries
    SELECT
        orig_rnid AS Lattice_Point,
        'Counts' AS TableType,
        'GROUPBY' AS ClauseType,
        'rnid' AS EntryType,
        CONCAT(
            '`',
            orig_rnid,
            '`'
        ) AS Entries
    FROM
        LatticeRNodes;


INSERT INTO MetaQueries
    SELECT
        rnid AS Lattice_Point,
        'Counts' AS TableType,
        'GROUPBY' AS ClauseType,
        '2nid' AS EntryType,
        CONCAT(
            '`',
            N.2nid,
            '`'
        ) AS Entries
    FROM
        RNodes_2Nodes RN,
        2Nodes N
    WHERE
        N.2nid = RN.2nid;


/**
 * Propagate to Rnode meta query all the metaquery components of the two associated population variables.
 * This includes the COUNT(*) aggregation.
 */
INSERT INTO MetaQueries
    SELECT DISTINCT
        rnid AS Lattice_Point,
        TableType,
        ClauseType,
        EntryType,
        Entries
    FROM
        RNodes_pvars R,
        MetaQueries M
    WHERE
        M.Lattice_Point = R.pvid;


/**
 * Build the whole Rchain.  Propagate Rnode meta queries to Rchains containing the rnode.  Make sure we do this for
 * proper rchains only.
 */
INSERT INTO MetaQueries
    SELECT
        L.name AS Lattice_Point,
        'Counts' AS TableType,
        M.ClauseType,
        M.EntryType,
        M.Entries
    FROM
        lattice_membership L,
        MetaQueries M
    WHERE
        L.name <> L.`member`
    AND
        Lattice_Point = L.`member`
    AND
        M.TableType = 'COUNTS';

END//