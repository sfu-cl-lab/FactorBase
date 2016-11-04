/*adding covering index to speed up the query, however hash index does not support this technique, so replace it with default index, ie. B_Tree @ zqian May 22nd*/
USE @database@_BN;
SET storage_engine=INNODB;

/* Set up a table that contains all functor nodes of any arity, useful for Bayes net learning later. */
CREATE TABLE FNodes (   /*May 10th */
  `Fid` varchar(199) ,
  `FunctorName` varchar(64) ,
  `Type` varchar(5) ,
  `main` int(11) ,
  PRIMARY KEY  (`Fid`)
);

insert into FNodes
SELECT 
    1nid AS Fid,
    COLUMN_NAME as FunctorName,
    '1Node' as Type,
    main
FROM
    1Nodes 
UNION SELECT 
    2nid AS Fid,
    COLUMN_NAME as FunctorName,
    '2Node' as Type,
    main
FROM
    2Nodes 
union select 
    rnid as FID,
    TABLE_NAME as FunctorName,
    'Rnode' as Type,
    main
from
    RNodes;


create table FNodes_pvars as 
SELECT FNodes.Fid, PVariables.pvid FROM
    FNodes,
    2Nodes,
    PVariables
where
    FNodes.Type = '2Node'
    and FNodes.Fid = 2Nodes.2nid
    and PVariables.pvid = 2Nodes.pvid1 
union 
SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    2Nodes,
    PVariables
where
    FNodes.Type = '2Node'
    and FNodes.Fid = 2Nodes.2nid
    and PVariables.pvid = 2Nodes.pvid2 
union 
SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    1Nodes,
    PVariables
where
    FNodes.Type = '1Node'
    and FNodes.Fid = 1Nodes.1nid
    and PVariables.pvid = 1Nodes.pvid;

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

create table 1Nodes_From_List select 1nid,
    concat(PVariables.TABLE_NAME,
            ' AS ',
            PVariables.pvid) as Entries from
    1Nodes,
    PVariables
where
    1Nodes.pvid = PVariables.pvid;

create table 2Nodes_Select_List as select 2nid,
    concat(RNodes.rnid,
            '.',
            2Nodes.COLUMN_NAME,
            ' AS ',
            2nid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;

create table 2Nodes_From_List as select 2nid,
    concat(2Nodes.TABLE_NAME, ' AS ', RNodes.rnid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;

/******************************************************
Reformat table information, to support easy formulation of data queries for sufficient statistics.
The goal is to create join data tables that can be given as input to a Bayes net learner. 
We create queries that represent the join data tables.
The join queries themselves are encoded in tables, where one table lists the entries in the select clause, one lists those in the from clause, one lists those in the where clause.
*/


CREATE TABLE PVariables_From_List AS SELECT pvid, CONCAT(TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables
WHERE
    index_number = 0;
/* use entity tables for main variables only (index = 0). 
Other entity tables have empty Bayes nets by the main functor constraint. */

CREATE TABLE PVariables_Select_List AS 
SELECT 
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables
UNION
SELECT pvid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables
WHERE
    PVariables.index_number = 0;

/**********
Now we make data join tables for each relationship functor node.
***********/

CREATE TABLE RNodes_pvars AS SELECT DISTINCT rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME 
FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid1 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME1
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME 
UNION 
SELECT DISTINCT
    rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME
FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid2 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME2
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;
/* for each relationship functor, for each population variable, find the column name that is a foreign key pointer to the Entity table for the population, and find the name the referenced column in the Entity table. */



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

Create TABLE RNodes_2Nodes as 
select RNodes.rnid, 2Nodes.2nid from 2Nodes, RNodes where 2Nodes.TABLE_NAME = RNodes.TABLE_NAME; 


/** Each unary functor, and each binary functor, becomes an attribute to be retrieved in the select list.
Make a table to record this. **/
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
    RNodes;


/******************************************************
Also, these attributes become nodes in the Bayes net for later analysis.
****/
CREATE TABLE RNodes_BN_Nodes AS 
SELECT DISTINCT 
    rnid, 1nid AS Fid, FNodes.main 
FROM
    RNodes_1Nodes,
    FNodes
WHERE
    FNodes.Fid = 1nid 
UNION DISTINCT 
SELECT DISTINCT
    rnid, 2nid AS Fid, main
FROM
    2Nodes
        NATURAL JOIN
    RNodes
    /*OS: next add the rnode as a functor node for itself Oct 13, 2016; */
    union
    select distinct rnid, rnid as Fid, main from RNodes;


CREATE TABLE  Entity_BayesNets (
    pvid VARCHAR(65) NOT NULL,
    child VARCHAR(131) NOT NULL,
    parent VARCHAR(131) NOT NULL,
    PRIMARY KEY (pvid , child , parent)
);

/******* create Tables to store Bayes nets in Lattice Chains  ****/

/* //zqian, max key length limitation "The maximum column size is 767 bytes", 
enable "innodb_large_prefix" to allow index key prefixes longer than 767 bytes (up to 3072 bytes).
 Oct 17, 2013    */

CREATE TABLE Path_BayesNets (
    Rchain VARCHAR(255) NOT NULL,     child VARCHAR(197) NOT NULL,
    parent VARCHAR(197) NOT NULL,
    PRIMARY KEY (Rchain , child , parent)
);

CREATE TABLE NewLearnedEdges LIKE Path_BayesNets;

/****************
Create tables that allow us to represent background knowledge
*****************/

/*CREATE OR REPLACE VIEW Path_BN_nodes AS*/
ALTER TABLE `RNodes_BN_Nodes` ADD INDEX `Index_rnid`  (`rnid` ASC) ;/* May 10th*/
CREATE TABLE Path_BN_nodes AS 
SELECT DISTINCT lattice_membership.name AS Rchain, Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.member
    ORDER BY lattice_membership.name;

ALTER TABLE Path_BN_nodes ADD INDEX `HashIndex`  (`Rchain`,`node`); /* May 7*/


CREATE TABLE IF NOT EXISTS Knowledge_Forbidden_Edges like Path_BayesNets;
CREATE TABLE IF NOT EXISTS Knowledge_Required_Edges like Path_BayesNets;

CREATE table Path_Aux_Edges as SELECT 
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        FNodes
    WHERE
        BN_nodes1.Rchain = BN_nodes2.Rchain
            AND FNodes.Fid = BN_nodes1.node
            AND FNodes.main = 0;
          /* OS: we don't remember why we had this union! Nov 4 
union distinct             
            
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
            AND FNodes.Fid = BN_nodes2.node
            AND FNodes.main = 0;
/*zqian Oct 20, 2016*/
*/

ALTER TABLE Path_Aux_Edges ADD PRIMARY KEY (`Rchain`, `child`, `parent`); /* May 10th*/ 


ALTER TABLE `2Nodes` ADD INDEX `index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ; /* May 10th*/
ALTER TABLE `RNodes` ADD INDEX `Index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ;

create table SchemaEdges as 
select distinct 
    lattice_membership.name AS Rchain,
    2Nodes.2nid As child,
    RNodes.rnid AS parent 
from
    RNodes,
    2Nodes,
    lattice_membership
where
    lattice_membership.member = RNodes.rnid
        and RNodes.pvid1 = 2Nodes.pvid1
        and RNodes.pvid2 = 2Nodes.pvid2
        and RNodes.TABLE_NAME = 2Nodes.TABLE_NAME
        AND lattice_membership.name in (select 
            lattice_set.name
        from
            lattice_set
        where
            length = (select 
                    max(length)
                from
                    lattice_set));
ALTER TABLE SchemaEdges ADD INDEX `HashIn`  (`Rchain`,`child`,`parent`);

CREATE TABLE  Path_Required_Edges like Path_BayesNets;/*May 10th*/
insert ignore into Path_Required_Edges select distinct *    from        Knowledge_Required_Edges;
/*union distinct  */
insert ignore into Path_Required_Edges select  distinct *    from        SchemaEdges;

   
CREATE TABLE Path_Forbidden_Edges like Path_BayesNets;/*May 10th*/
insert ignore into Path_Forbidden_Edges select     distinct *    from        Knowledge_Forbidden_Edges ;
/*UNION  distinct */
insert ignore into Path_Forbidden_Edges   SELECT    distinct     *    FROM        Path_Aux_Edges ;


CREATE TABLE  LearnedEdges like Path_BayesNets;
CREATE TABLE  ContextEdges like Path_BayesNets;
CREATE TABLE  InheritedEdges like Path_BayesNets;



/******************************************
Create Views to enforce constraints as knowledge, in the sense of Tetrad: edges that are required and forbidden.action


/******************
Required Edges: The required edges for the Bayes nets are 1) inherited from the entity tables for singleton relationship sets, and 2) inherited from the subsets for relationships of size > 1. ???
This view is updated every time Bayes net learning is performed.
*/

/*create or replace view InheritedEdges as*/
/*create table InheritedEdges as
    select distinct
        lattice_rel.child AS Rchain,
        Path_BayesNets.child AS child,
        Path_BayesNets.parent AS parent
    FROM
        Path_BayesNets,
        lattice_rel
    WHERE
        lattice_rel.parent = Path_BayesNets.Rchain
            AND Path_BayesNets.parent <> ''
    ORDER BY Rchain;

*/


/*CREATE OR REPLACE VIEW Path_Required_Edges AS*/
/*CREATE table Path_Required_Edges AS
    SELECT DISTINCT
        RNodes_pvars.rnid AS Rchain,
        Entity_BayesNets.child AS child,
        Entity_BayesNets.parent AS parent
    FROM
        (RNodes_pvars, Entity_BayesNets)
    WHERE
        (RNodes_pvars.pvid = Entity_BayesNets.pvid
            AND Entity_BayesNets.parent <> '') 
    UNION SELECT 
        Rchain, child, parent
    from
        InheritedEdges
    where
        (Rchain , parent, child) NOT IN (select 
                *
            from
                InheritedEdges) 
    union select 
        *
    from
        Knowledge_Required_Edges
    union distinct 
    select * from SchemaEdges;
*/
/* Extendibility Note: Could allow a user or program to create a table with its required forbidden edges, simply union those into this construct */

CREATE OR REPLACE VIEW Entity_BN_nodes /*CREATE table Entity_BN_nodes */
       AS SELECT 
        Entity_BayesNets.pvid AS pvid,
        Entity_BayesNets.child AS node
    FROM
        Entity_BayesNets ORDER BY pvid;

/* Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net. Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around */

/*CREATE OR REPLACE VIEW Entity_Complement_Edges AS*/
CREATE table Entity_Complement_Edges as 
    SELECT distinct
        BN_nodes1.pvid AS pvid,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Entity_BN_nodes AS BN_nodes1,
        Entity_BN_nodes AS BN_nodes2
    WHERE
        BN_nodes1.pvid = BN_nodes2.pvid
            AND (NOT (EXISTS( SELECT 
                *
            FROM
                Entity_BayesNets
            WHERE
                (Entity_BayesNets.pvid = BN_nodes1.pvid)
                    AND (Entity_BayesNets.child = BN_nodes1.node)
                    AND (Entity_BayesNets.parent = BN_nodes2.node))));

ALTER TABLE `Entity_Complement_Edges` ADD PRIMARY KEY (`pvid`, `child`, `parent`) ;/*May 10th*/
/*ALTER TABLE Entity_Complement_Edges ADD INDEX `HashIn`  (`pvid`,`child`,`parent`);*/
/* Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net. Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around */




/*CREATE OR REPLACE VIEW Path_BN_nodes AS*/
/*CREATE table Path_BN_nodes AS
    SELECT 
        lattice_membership.name AS Rchain, Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.member
    ORDER BY lattice_membership.name;
    */
/* for each singleton relationship, find the associated functor ids. Union these for each lattice point that contains the singleton. Including the singleton itself. */

/*CREATE OR REPLACE VIEW Path_Complement_Edges AS*/
CREATE table Path_Complement_Edges like Path_BayesNets; /*May 10th*/
/*SELECT 
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2
    WHERE
        ((BN_nodes1.Rchain = BN_nodes2.Rchain)
            AND (NOT (EXISTS( SELECT 
                *
            FROM
                Path_BayesNets
            WHERE
                ((Path_BayesNets.Rchain = BN_nodes1.Rchain)
                    AND (Path_BayesNets.child = BN_nodes1.node)
                    AND (Path_BayesNets.parent = BN_nodes2.node))))));

*/
/*ALTER TABLE Path_Complement_Edges ADD INDEX `HashIndex`  (`Rchain`,`child`,`parent`);*/ /* May 7*/
/*ALTER TABLE Path_Complement_Edges ADD INDEX `HashIn`  (`child`,`parent`); *//* May 9*/

/********************************************
Dealing with the Main Functor Constraint. The auxilliary functor nodes are those that are not main functor nodes. The aux edges are the ones that point to an auxilliary node. 
Our approach is this: for each node in the relationship lattice, find the nodes in its associated Bayes net. If such a node is an auxilliary node, any edge pointing into this node is forbidden.
This builds a fairly large view. We could be more efficient in terms of storage by computing this dynamically as we learn the Bayes nets going up the lattice.
*********************************************/





/****************************
Finally, the forbidden edges for the Bayes nets are 1) inherited from the entity tables for singleton relationship sets, and 2) inherited from the subsets for relationships of size > 1
, and 3) the edges pointing into the main edges. 
Assumes that lattice_rel gives the subsets that are one size smaller.
*/

/*CREATE OR REPLACE VIEW Path_Forbidden_Edges AS*/
/*CREATE table Path_Forbidden_Edges AS
    SELECT DISTINCT
        RNodes_pvars.rnid AS Rchain,
        Entity_Complement_Edges.child AS child,
        Entity_Complement_Edges.parent AS parent
    FROM
        (RNodes_pvars, Entity_Complement_Edges)
    WHERE
        (RNodes_pvars.pvid = Entity_Complement_Edges.pvid) 
    UNION SELECT DISTINCT
        lattice_rel.child AS Rchain,
        Path_Complement_Edges.child AS child,
        Path_Complement_Edges.parent AS parent
    FROM
        Path_Complement_Edges,
        lattice_rel
    WHERE
        lattice_rel.parent = Path_Complement_Edges.Rchain
            AND Path_Complement_Edges.parent <> ''
            and (lattice_rel.child , Path_Complement_Edges.child,
            Path_Complement_Edges.parent) not in (select 
                *
            from
                Path_Required_Edges) 
    UNION SELECT 
        *
    FROM
        Path_Aux_Edges 
    UNION SELECT 
        *
    FROM
        Knowledge_Forbidden_Edges
;
*/

/* Extendibility Note: Could allow a user or program to create a table with 
its forbidden edges, simply union those into this construct */


/*create or replace view LearnedEdges as*/
/*create table LearnedEdges as
    select 
        Path_BayesNets.Rchain,
        Path_BayesNets.child,
        Path_BayesNets.parent
    from
        Path_BayesNets
    where
        Path_BayesNets.parent <> ''
            and (Path_BayesNets.Rchain , Path_BayesNets.child,
            Path_BayesNets.parent) not in (select 
                *
            from
                Path_Required_Edges);
*/
/*create or replace view ContextEdges as*/
/*create table ContextEdges as
    select distinct
        LearnedEdges.Rchain as Rchain,
        LearnedEdges.child as child,
        lattice_membership.member as parent
    from
        LearnedEdges,
        lattice_membership
    where
        LearnedEdges.Rchain = lattice_membership.name;
*/


/*Metadata for building CT tables*/
USE @database@_BN;


CREATE TABLE ADT_PVariables_Select_List AS 
SELECT 
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables
UNION
SELECT 
    pvid,CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries 
FROM
    1Nodes
        NATURAL JOIN
    PVariables;
/*WHERE
    PVariables.index_number = 0;
*/

/* For The Testing databses, need the primary key, June 19, 2014 */
/* We want to add the primary key for the target instances to keep a table of all groundings at the same time. */

/*CREATE TABLE Test_PVariables_Select_List AS
SELECT pvid, column_name as Entries 
FROM `PVariables`  natural join  `EntityTables`;

create table pvid_rnode_Select_List as
SELECT distinct pvid, CONCAT(rnid,'.',COLUMN_NAME)  as Entries  FROM FNodes_pvars_UNION_RNodes_pvars
natural join RNodes_pvars;

CREATE TABLE Test_RNodes_Select_List as 
select Fid,rnid,Entries from FNodes_pvars_UNION_RNodes_pvars 
natural join  pvid_rnode_Select_List;
*/

CREATE TABLE ADT_PVariables_From_List AS SELECT pvid, CONCAT('@database@.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;
/*WHERE
    index_number = 0;*/
/* use entity tables for main variables only (index = 0). 
Other entity tables have empty Bayes nets by the main functor constraint. */


/*
CREATE TABLE ADT_PVariables_WHERE_List AS SELECT pvid, '"MULT" > 0' AS Entries FROM
    PVariables
WHERE
    index_number = 0;
*. May 13rd*/

/* add a where clause to eliminate states with 0 count, trying to make the contigency table smaller */
create table PVariables_GroupBy_List as
SELECT pvid,
    1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables;

create table ADT_PVariables_GroupBy_List as
SELECT pvid,
    1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables;
/*WHERE
    PVariables.index_number = 0;
* May 13rd/
/** now to build tables for the relationship nodes **/

CREATE TABLE ADT_RNodes_1Nodes_Select_List AS 
select 
    rnid, concat('sum(`',replace(rnid, '`', ''),'_counts`.`MULT`)',' as "MULT"') AS Entries
from
    RNodes
union
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes 
;

CREATE TABLE ADT_RNodes_1Nodes_FROM_List AS 
select 
    rnid, concat('`',replace(rnid, '`', ''),'_counts`') AS Entries
from RNodes
;

CREATE TABLE ADT_RNodes_1Nodes_GroupBY_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes 
;




CREATE TABLE ADT_RNodes_Star_Select_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes;

/** TODO: Also need to multiply the mult columns from the different tables, e.g. 
select (t1.mult * t2.mult * t3.mult) as "MULT"
**/

CREATE TABLE ADT_RNodes_Star_From_List AS 
SELECT DISTINCT rnid, concat('`',replace(pvid, '`', ''),'_counts`')
    AS Entries FROM
    RNodes_pvars;




create table ADT_RNodes_False_Select_List as
SELECT DISTINCT rnid, concat('(`',replace(rnid, '`', ''),'_star`.MULT','-','`',replace(rnid, '`', ''),'_flat`.MULT)',' AS "MULT"') as Entries
from RNodes
union
SELECT DISTINCT rnid,
    concat('`',replace(rnid, '`', ''),'_star`.',1nid) AS Entries FROM
    RNodes_1Nodes;

create table ADT_RNodes_False_FROM_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`') as Entries from RNodes
union 
select distinct rnid, concat('`',replace(rnid, '`', ''),'_flat`') as Entries from RNodes;

create table ADT_RNodes_False_WHERE_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`.',1nid,'=','`',replace(rnid, '`', ''),'_flat`.',1nid) as Entries from RNodes_1Nodes; 

/* May 16th, last step for _CT tables, preparing the colunmname_list */
create table Rnodes_join_columnname_list as 
select distinct rnid,concat(2nid, ' varchar(5)  default ',' "N/A" ') as Entries from 2Nodes natural join RNodes;



/*May 17th*/


/** TODO: Also need to multiply the mult columns from the different tables, e.g. 
select (t1.mult * t2.mult * t3.mult) as "MULT"
**/
CREATE TABLE RChain_pvars AS
select  distinct 
    lattice_membership.name as rchain, 
    pvid 
from 
    lattice_membership, RNodes_pvars 
where 
    RNodes_pvars.rnid = lattice_membership.member;
 

CREATE TABLE ADT_RChain_Star_From_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    concat('`',replace(lattice_rel.parent,'`',''),'_CT`')  AS Entries 
FROM
    lattice_rel
where 
    lattice_rel.parent <>'EmptySet'
union
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
concat('`',replace(RNodes_pvars.pvid, '`', ''),'_counts`')    AS Entries 
FROM
    lattice_rel,RNodes_pvars
where lattice_rel.parent <>'EmptySet'
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain =     lattice_rel.parent)
;


CREATE TABLE ADT_RChain_Star_Where_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    concat(lattice_membership.member,' = "T"')  AS Entries 
FROM
    lattice_rel,    lattice_membership
where 
    lattice_rel.child = lattice_membership.name
    and  lattice_membership.member > lattice_rel.removed
    and lattice_rel.parent <>'EmptySet';



CREATE TABLE ADT_RChain_Star_Select_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    RNodes_GroupBy_List.Entries 
FROM
    lattice_rel,lattice_membership,RNodes_GroupBy_List
where 
    lattice_rel.parent <>'EmptySet'  and lattice_membership.name = lattice_rel.parent
and RNodes_GroupBy_List.rnid = lattice_membership.member
union
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    1Nodes.1nid    AS Entries 
FROM
    lattice_rel,RNodes_pvars,1Nodes

where lattice_rel.parent <>'EmptySet' 
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid = 1Nodes.pvid and  1Nodes.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain =  lattice_rel.parent)
union
SELECT DISTINCT  /*May 21*/
    lattice_rel.removed as rchain, 
    lattice_rel.removed as rnid, 
    1Nodes.1nid    AS Entries 
FROM
    lattice_rel,RNodes_pvars,1Nodes

where lattice_rel.parent ='EmptySet' 
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid = 1Nodes.pvid 
;


