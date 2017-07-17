create table 1Nodes_Select_List as select 1nid,
    concat(1Nodes.pvid,
            '.',
            1Nodes.COLUMN_NAME,
            ' AS ',
            1nid) as Entries from
    1Nodes,
    PVariables
where
    1Nodes.pvid = PVariables.pvid;
    
---map Pvariables in 1Node to entity table name---

create table 1Nodes_From_List select 1nid,
    concat(PVariables.TABLE_NAME,
            ' AS ',
            PVariables.pvid) as Entries from
    1Nodes,
    PVariables
where
    1Nodes.pvid = PVariables.pvid;

----map 2Nodes to column names in relationship tables---

create table 2Nodes_Select_List as select 2nid,
    concat(RNodes.rnid,
            '.',
            2Nodes.COLUMN_NAME,
            ' AS ',
            2nid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;
    
----find the Relationship table for the 2nid----
create table 2Nodes_From_List as select 2nid,
    concat(2Nodes.TABLE_NAME, ' AS ', RNodes.rnid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;



---map Pvariables to entity tables---

CREATE TABLE PVariables_From_List AS SELECT pvid, CONCAT(TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables
WHERE
    index_number = 0;
/* use entity tables for main variables only (index = 0). 
Other entity tables have empty Bayes nets by the main functor constraint. */

/* the pvariable select list contains the union of select list for each 1node associated with the pvariable */

CREATE TABLE PVariables_Select_List AS 
SELECT 
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables
UNION
/*for each pvariable, find the select list for the associated attributes = 1Nodes*/
SELECT pvid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables
WHERE
    PVariables.index_number = 0
    UNION
 /*for each pvariable in expansion, find the primary column and add it to the select list */
 /* don't use this for continuous, but do use it for the no_link case */
 SELECT E.pvid, CONCAT(E.pvid,'.',REFERENCED_COLUMN_NAME) AS Entries FROM
 RNodes_pvars RP, Expansions E where E.pvid = RP.pvid;
 
 /* add a where clause to eliminate states with 0 count, trying to make the contigency table smaller */
create table PVariables_GroupBy_List as
SELECT pvid,
    1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables
     UNION
 /*for each pvariable in expansion, find the primary column and add it to the group by list */
 /* don't use this for continuous, but do use it for the no_link case */
 SELECT E.pvid, CONCAT(E.pvid,'.',REFERENCED_COLUMN_NAME) AS Entries FROM
 RNodes_pvars RP, Expansions E where E.pvid = RP.pvid;

 

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

Create TABLE RNodes_2Nodes as 
select RNodes.rnid, 2Nodes.2nid from 2Nodes, RNodes where 2Nodes.TABLE_NAME = RNodes.TABLE_NAME; 


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
FROM unielwin_BN.RNodes_pvars RP, PVariables_Select_List PV where RP.pvid = PV.pvid;
;

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
FROM unielwin_BN.RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;
