

DROP SCHEMA IF EXISTS unielwin_target_setup; 
create schema unielwin_target_setup;

USE unielwin_target_setup;
SET storage_engine=INNODB;


create table FNodes as select * from unielwin_BN.FNodes;
create table FNodes_pvars as select * from unielwin_BN.FNodes_pvars;
create table PVariables as select * from unielwin_BN.PVariables;
create table EntityTables as select * from unielwin_BN.EntityTables;
create table AttributeColumns as select * from unielwin_BN.AttributeColumns;
create table TernaryRelations as select * from unielwin_BN.TernaryRelations;
create table RelationTables as select * from unielwin_BN.RelationTables;
create table NoPKeys as select * from  unielwin_BN.NoPKeys;
create table ForeignKeyColumns as select * from  unielwin_BN.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  unielwin_BN.ForeignKeys_pvars;
create table InputColumns as select * from  unielwin_BN.InputColumns;
create table Attribute_Value as select * from  unielwin_BN.Attribute_Value;
create table RNodes_pvars as select * from unielwin_BN.RNodes_pvars;



create table FNodes_pvars_UNION_RNodes_pvars  as select * from unielwin_BN.FNodes_pvars_UNION_RNodes_pvars ; 


DROP SCHEMA IF EXISTS unielwin_target; 
create schema unielwin_target;

