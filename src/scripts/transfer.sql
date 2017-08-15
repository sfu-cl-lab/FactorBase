/* copies key metadata tables from @database@_setup to @database@_BN */
/* should just be able to change Fnodes being copied to restrict to subset */

DROP SCHEMA IF EXISTS @database@_BN; 
create schema @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT; 
create schema @database@_CT;

USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup database. restricted to functor set */
CREATE TABLE FunctorSet AS SELECT * FROM
    unielwin_setup.FunctorSet;

CREATE TABLE 1Nodes AS SELECT N.1nid, N.COLUMN_NAME, N.pvid, N.main FROM
    unielwin_setup.1Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.1nid = F.fid;

CREATE TABLE 2Nodes AS SELECT N.2nid,
    N.COLUMN_NAME,
    N.pvid1,
    N.pvid2,
    N.TABLE_NAME,
    N.main FROM
    unielwin_setup.2Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.2nid = F.Fid;
CREATE TABLE RNodes AS SELECT N.orig_rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.rnid,
    N.main FROM
    unielwin_setup.RNodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.orig_rnid = F.Fid;


create table PVariables as select * from @database@_setup.PVariables;
create table EntityTables as select * from @database@_setup.EntityTables;
create table AttributeColumns as select * from @database@_setup.AttributeColumns;
/* create table TernaryRelations as select * from @database@_setup.TernaryRelations; */
create table RelationTables as select * from @database@_setup.RelationTables;
/* create table NoPKeys as select * from  @database@_setup.NoPKeys; */
create table ForeignKeyColumns as select * from  @database@_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  @database@_setup.ForeignKeys_pvars;
create table InputColumns as select * from  @database@_setup.InputColumns;
create table Expansions as select * from @database@_setup.Expansions;
/*
create table Groundings like @database@_setup.Groundings; 
insert into Groundings select * from @database@_setup.Groundings;
*/

/*
create table Path_BN_nodes as select * from  @database@_setup.Path_BN_nodes;
create table lattice_membership as select * from  @database@_setup.lattice_membership;
create table lattice_set as select * from  @database@_setup.lattice_set;
create table Path_Aux_Edges as select * from  @database@_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  @database@_setup.SchemaEdges;
*/


