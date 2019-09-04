SET storage_engine=INNODB;

/***********************************/
/* Script for creating metaqueries that are executed to compute contingency tables
/* can only be understood by reading our DSAA paper or at least the github documentation.
 * See also https://github.com/sfu-cl-lab/FactorBase/issues/46
 */
/* Assumes the presence of the following tables, to be created by transfer.sql:
/* RNodes_2Nodes
/* RNodes_pvars
 */

CREATE TABLE MetaQueries (   
  Lattice_Point varchar(199) , /* e.g. pvid, rchain, prof0, a */
  TableType varchar(100) , /*e.g. star, flat, counts */
  ClauseType varchar(10) , /* from, where, select, group by */
  EntryType varchar(100), /* e.g. 1node, aggregate like count */
  Entries varchar(150)
);

/***********************************/
/* metaqueries for population variables */
/*************************************
 * 
 */
--- map Pvariables to entity tables ---

INSERT into MetaQueries
SELECT distinct pvid as Lattice_Point, 'Counts' as TableType, 'FROM' as ClauseType, 'table' as EntryType, CONCAT('@database@.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;
    
   
/* August 18, 2017 OS. Could consider optimizing by using only main pvids */
    /* WHERE
    index_number = 0;*/

/* Pvariable select list */
/* contains 1nodes with renaming, and mult for aggregating, and id columns in case these are selected in the expansions table */  
    

    
INSERT into MetaQueries
SELECT DISTINCT
    pvid as Lattice_Point, 'Counts' as TableType, 'SELECT' as ClauseType, 'aggregate' as EntryType, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables;
 
  
/*for each pvariable, find the select list for the associated attributes = 1Nodes*/
    
INSERT into MetaQueries
SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType,  'SELECT' as ClauseType, '1node' as EntryType,
    CONCAT(P.pvid, '.', N.COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;


 /*for each pvariable in expansion, find the primary column and add it to the select list */
 /* don't use this for continuous, but do use it for the no_link case */
 INSERT into MetaQueries
 SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType,  'SELECT' as ClauseType, 'id' as EntryType, CONCAT(E.pvid,'.',P.ID_COLUMN_NAME, ' AS `ID(', E.pvid, ')`') AS Entries FROM
 PVariables P, Expansions E where E.pvid = P.pvid;
 
 
 /* Pvariable GroupBY  list */
 
/* contains 1nodes without renaming */  
 INSERT into MetaQueries
 SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType,  'GROUPBY' as ClauseType, '1node' as EntryType,
    1nid  AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;
    
/* add id columns for expansions without renaming */
    
INSERT into MetaQueries
SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType, 'GROUPBY' as ClauseType, 'id' as EntryType, CONCAT('`ID(', E.pvid, ')`') AS Entries FROM
PVariables P, Expansions E where E.pvid = P.pvid;
 
 /* Pvariable where  list */
/* the only thing to add is any grounding constraints */

INSERT into MetaQueries
SELECT distinct G.pvid AS Lattice_Point, 'Counts' as TableType,  'WHERE' as ClauseType, 'id' as EntryType, CONCAT('`ID(', G.pvid, ')` = ', G.id) AS Entries FROM
Groundings G;
 
/*I'm a bit worried about the type of g.id, integer or string? */

/**********
Now we make data join tables for each relationship functor node.
***********/


/* the from list for the relationship functor contains the relationship table */


INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'FROM' AS ClauseType,
    'rtable' AS EntryType,
    CONCAT('@database@.', R.TABLE_NAME, ' AS ', rnid) AS Entries
FROM
    RNodes R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid;

/** we add a table to the from list that has a single column and single row contain "T" for "true", whose header is the rnid. This simulates the case where all the relationships are true.
We need to replace the apostrophes in rnid to make the rnid a valid name for the temporary table 
**/

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'FROM' AS ClauseType,
    'rtable' AS EntryType,
    concat('(select "T" as ',
            rnid,
            ') as ',
            concat('`temp_', replace(rnid, '`', ''), '`')) as Entries
from
    RNodes R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid; 

/** The Where List for each rnode contains the join condition that links the foreign key pointers in the relationship table to
the ids in the population entity tables. **/

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'WHERE' AS ClauseType,
    'rtable' AS EntryType,
    CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME) AS Entries 
FROM
    RNodes_pvars R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid;



/** The select list for an rnode contains
1) the rnid itself. This is the header in the temporary table created above
2) the 2nodes of the relationship variable 
***/

INSERT into MetaQueries
select 
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'SELECT' AS ClauseType,
    'rnid' AS EntryType,
    orig_rnid AS Entries
from
    LatticeRNodes;

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'SELECT' AS ClauseType,
    '2nid' AS EntryType,
    CONCAT(L.orig_rnid, '.', COLUMN_NAME, ' AS ', N.2nid) AS Entries
FROM
    LatticeRNodes L, RNodes_2Nodes RN, 2Nodes N
where RN.rnid = L.orig_rnid and N.2nid = RN.2nid;


/***************
 The GroupBy List is like the select list but without renaming
 */

INSERT into MetaQueries
select 
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'GROUPBY' AS ClauseType,
    'rnid' AS EntryType,
    orig_rnid AS Entries
from
    LatticeRNodes;
    
INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Counts' AS TableType,
    'GROUPBY' AS ClauseType,
    '2nid' AS EntryType,
    N.2nid AS Entries
FROM
    LatticeRNodes L, RNodes_2Nodes RN, 2Nodes N
where RN.rnid = L.orig_rnid and N.2nid = RN.2nid order by RN.rnid,COLUMN_NAME;
    
 
/*********************
Propagate to Rnode meta query all the metaquery components of the two associated population variables
This includes the count(*) aggregation
*************************************/

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    TableType,
    ClauseType,
    EntryType,
    Entries
FROM
    LatticeRNodes L, RNodes_pvars R, MetaQueries M
WHERE
    L.orig_rnid = R.rnid and M.Lattice_Point = R.pvid;
    
/***********
 * Build the whole Rchain
 * Propagate Rnode meta queries to Rchains containing the rnode. make sure we do this for proper rchains only
 */


 INSERT into MetaQueries
 select L.name AS Lattice_Point, 'Counts' as TableType, M.ClauseType, M.EntryType, M.Entries
 from lattice_membership L, MetaQueries M
 where L.name <> L.`member` and Lattice_Point = L.`member` and M.TableType = 'COUNTS';
 

