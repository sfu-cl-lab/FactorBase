DROP SCHEMA IF EXISTS @database@_BN; 
create schema @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT; 
create schema @database@_CT;

USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup database */


create table 1Nodes as select * from @database@_setup.1Nodes;
create table 2Nodes as select * from @database@_setup.2Nodes;
create table RNodes as select * from @database@_setup.RNodes;
create table PVariables as select * from @database@_setup.PVariables;
create table EntityTables as select * from @database@_setup.EntityTables;
create table AttributeColumns as select * from @database@_setup.AttributeColumns;
create table TernaryRelations as select * from @database@_setup.TernaryRelations;
create table RelationTables as select * from @database@_setup.RelationTables;
create table NoPKeys as select * from  @database@_setup.NoPKeys;
create table ForeignKeyColumns as select * from  @database@_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  @database@_setup.ForeignKeys_pvars;
create table InputColumns as select * from  @database@_setup.InputColumns;
create table Attribute_Value as select * from  @database@_setup.Attribute_Value;
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


