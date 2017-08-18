USE test_BN;
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

DROP TABLE IF EXISTS MetaQueries;

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
SELECT distinct pvid as Lattice_Point, 'Counts' as TableType, 'FROM' as ClauseType, 'table' as EntryType, CONCAT(TABLE_NAME, ' AS ', pvid) AS Entries FROM
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
SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType, '1node' as EntryType, 'SELECT' as ClauseType,
    CONCAT(P.pvid, '.', N.COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;


 /*for each pvariable in expansion, find the primary column and add it to the select list */
 /* don't use this for continuous, but do use it for the no_link case */
 INSERT into MetaQueries
 SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType, 'id' as EntryType, 'SELECT' as ClauseType, CONCAT(E.pvid,'.',P.COLUMN_NAME, ' AS `ID(', E.pvid, ')`') AS Entries FROM
 PVariables P, Expansions E where E.pvid = P.pvid;
 
 
 /* Pvariable GroupBY  list */
 
/* contains 1nodes without renaming */  
 INSERT into MetaQueries
 SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType, '1node' as EntryType, 'GROUPBY' as ClauseType,
    1nid  AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;
    
/* add id columns for expansions without renaming */
    
INSERT into MetaQueries
SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType, 'id' as EntryType, 'GROUPBY' as ClauseType, CONCAT('`ID(', E.pvid, ')`') AS Entries FROM
PVariables P, Expansions E where E.pvid = P.pvid;
 
 /* Pvariable where  list */
/* the only thing to add is any grounding constraints */

INSERT into MetaQueries
SELECT distinct G.pvid AS Lattice_Point, 'Counts' as TableType, 'id' as EntryType, 'WHERE' as ClauseType, CONCAT('`ID(', G.pvid, ')` = ', G.id) AS Entries FROM
Groundings G;
 
/*I'm a bit worried about the type of g.id, integer or string? */

/**********
Now we make data join tables for each relationship functor node.
***********/


/* the from list for the relationship functor contains: 
1) the relationship table 
2) the union of the from lists of the population variables associated with the relationship node
/* for each relationship functor, for each population variable, 
find the column name that is a foreign key pointer to the Entity table for the population, 
and find the name the referenced column in the Entity table. */



CREATE TABLE RNodes_From_List AS SELECT DISTINCT rnid, CONCAT('@database@.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    RNodes_pvars 
UNION DISTINCT 
SELECT DISTINCT
    rnid, CONCAT('@database@.',TABLE_NAME, ' AS ', rnid) AS Entries
FROM
    RNodes 
union distinct 
select distinct
    rnid,
    concat('(select "T" as ',
            rnid,
            ') as ',
            concat('`temp_', replace(rnid, '`', ''), '`')) as Entries
from
    RNodes
;
/** we add a table that has a single column and single row contain "T" for "true", whose header is the rnid. This simulates the case where all the relationships are true.
We need to replace the apostrophes in rnid to make the rnid a valid name for the temporary table 
**/

/** The Where List for each rnode contains the join condition that links the foreign key pointers in the relationship table to
the ids in the population entity tables. **/

CREATE TABLE RNodes_Where_List AS SELECT rnid,
    CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME) AS Entries 
FROM
    RNodes_pvars;
/*    union
    select rnid, CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
           Groundings.id) AS Entries 
FROM
    RNodes_pvars natural join Groundings;
    */
    
    /* the table RNodes_pvars is such useful | added by zqian*/

/** now link each rnode id to the associated population variables **/

CREATE TABLE RNodes_1Nodes AS SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid1 AS pvid FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid1 
UNION SELECT 
    rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid2 AS pvid
FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid2;

/** now link each rnode 2node, i.e. each attribute of the relationship to the associated 2nodes **/
    /* this should be superseded by transfer script August 16, 2017 */

/* Create TABLE RNodes_2Nodes as 
select RNodes.rnid, 2Nodes.2nid from 2Nodes, RNodes where 2Nodes.TABLE_NAME = RNodes.TABLE_NAME; 
*/
/* should be created by Transfer */

/** The select list for an rnode contains
1) the rnid itself
2) the union of the selet lists for each population variable (currently implemented as 1nid)
3) the 2nodes of the relationship variable 
4) 

***/

CREATE TABLE RNodes_Select_List AS 
select 
    rnid, concat('count(*)',' as "MULT"') AS Entries
from
    RNodes
union
SELECT DISTINCT rnid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries 
FROM
    RNodes_1Nodes 
UNION DISTINCT 
select temp.rnid,temp.Entries from (
SELECT DISTINCT
    rnid,
    CONCAT(rnid, '.', COLUMN_NAME, ' AS ', 2nid) AS Entries
FROM
    2Nodes
        NATURAL JOIN
    RNodes order by RNodes.rnid,COLUMN_NAME
) as temp
UNION distinct 
select 
    rnid, rnid AS Entries
from
    RNodes 
    UNION DISTINCT
    /*for each associated to rnode pvariable in expansion , find the primary column and add it to the group by list */
 /* don't use this for continuous, but do use it for the no_link case */
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_Select_List PV where RP.pvid = PV.pvid;

CREATE TABLE RNodes_GroupBy_List AS SELECT DISTINCT rnid, 1nid AS Entries FROM
    RNodes_1Nodes 
UNION DISTINCT 
SELECT DISTINCT
    rnid, 2nid AS Entries
FROM
    2Nodes
        NATURAL JOIN
    RNodes 
UNION distinct 
select 
    rnid, rnid
from
    RNodes
    UNION DISTINCT
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;
