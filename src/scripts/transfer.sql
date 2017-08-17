/* copies key metadata tables from @database@_setup to @database@_BN */
/* should just be able to change Fnodes being copied to restrict to subset */

DROP SCHEMA IF EXISTS @database@_BN; 
create schema @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT; 
create schema @database@_CT;

USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup database. restricted to functor set */
/* redundant: August 17, 2017, OS */

 /*CREATE TABLE FunctorSet AS SELECT * FROM
    @database@_setup.FunctorSet;
    */

CREATE TABLE 1Nodes AS SELECT N.1nid, N.COLUMN_NAME, N.pvid, N.main FROM
    @database@_setup.1Nodes N,
    @database@_setup.FunctorSet F
WHERE
    N.1nid = F.fid;

CREATE TABLE 2Nodes AS SELECT N.2nid,
    N.COLUMN_NAME,
    N.pvid1,
    N.pvid2,
    N.TABLE_NAME,
    N.main FROM
    @database@_setup.2Nodes N,
    @database@_setup.FunctorSet F
WHERE
    N.2nid = F.Fid;
    
     /*map the 2nodes to rnodes for the given 2Nodes in the functor set*/
    
CREATE TABLE RNodes_2Nodes AS 
select N.rnid, N.`2nid` from @database@_setup.RNodes_2Nodes N, 2Nodes F where N.2nid = F.2nid;

     /*copy the rnodes for the functor set*/
    
CREATE TABLE RNodes AS SELECT 
    N.rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.main FROM
    @database@_setup.RNodes N,
    @database@_setup.FunctorSet F
WHERE
    N.rnid = F.Fid
/*for each 2node that's included in the functor set, copy its rnode as well in case the user missed it */
    union DISTINCT
    select N.rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.main FROM
    @database@_setup.RNodes N, RNodes_2Nodes F where N.rnid = F.rnid;
    
    
/* Set up a table that contains all functor nodes of any arity. summarizes all the work we've done. */

 CREATE TABLE FNodes (   
  `Fid` varchar(199) ,
  `FunctorName` varchar(64) ,
  `Type` varchar(5) ,
  `main` int(11) ,
  PRIMARY KEY  (`Fid`)
);


/******* make comprehensive table for all functor nodes but restricted to functor set *****/

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
    rnid as Fid,
    TABLE_NAME as FunctorName,
    'Rnode' as Type,
    main
from
    RNodes;

/**********
/* transfer links to pvariables. restrict only to functor nodes in functor set, now known as FNodes */
    
create table FNodes_pvars AS SELECT N.Fid, N.pvid
FROM @database@_setup.FNodes_pvars N, FNodes F where N.Fid = F.Fid;

create table RNodes_pvars as SELECT N.rnid, N.pvid, N.TABLE_NAME, N.COLUMN_NAME, N.REFERENCED_COLUMN_NAME
FROM @database@_setup.RNodes_pvars N, RNodes F where N.rnid = F.rnid;

/* transfer pvariables. Only those that occur in functor set */

create table PVariables as SELECT DISTINCT N.pvid, N.TABLE_NAME, N.index_number
FROM @database@_setup.PVariables N, FNodes_pvars F where F.pvid = N.pvid;
/* next clause should no longer be necessary given that FNodes_Pvars now includes RNodes as well */
/*union distinct 
select N.pvid, N.TABLE_NAME, N.index_number
FROM @database@_setup.PVariables N, RNodes_pvars F where F.pvid = N.pvid;
*/



/*********
/* transfer the rest. Not sure if I need all of these actually
*/

create table EntityTables as select * from @database@_setup.EntityTables;
create table AttributeColumns as select * from @database@_setup.AttributeColumns;
/* create table TernaryRelations as select * from @database@_setup.TernaryRelations; */
create table RelationTables as select * from @database@_setup.RelationTables;
/* create table NoPKeys as select * from  @database@_setup.NoPKeys; */
create table ForeignKeyColumns as select * from  @database@_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  @database@_setup.ForeignKeys_pvars;
create table InputColumns as select * from  @database@_setup.InputColumns;
create table Expansions as select * from @database@_setup.Expansions;
create table Groundings as select * from @database@_setup.Groundings;
create table TargetNode as select * from @database@_setup.TargetNode;


/*
create table Path_BN_nodes as select * from  @database@_setup.Path_BN_nodes;
create table lattice_membership as select * from  @database@_setup.lattice_membership;
create table lattice_set as select * from  @database@_setup.lattice_set;
create table Path_Aux_Edges as select * from  @database@_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  @database@_setup.SchemaEdges;
*/


