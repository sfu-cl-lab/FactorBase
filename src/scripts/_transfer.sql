


DROP SCHEMA IF EXISTS unielwin_BN; 
create schema unielwin_BN;

DROP SCHEMA IF EXISTS unielwin_CT; 
create schema unielwin_CT;

USE unielwin_BN;
SET storage_engine=INNODB;





create table 1Nodes as select * from unielwin_setup.1Nodes;
create table 2Nodes as select * from unielwin_setup.2Nodes;
create table RNodes as select * from unielwin_setup.RNodes;
create table PVariables as select * from unielwin_setup.PVariables;
create table EntityTables as select * from unielwin_setup.EntityTables;
create table AttributeColumns as select * from unielwin_setup.AttributeColumns;
create table TernaryRelations as select * from unielwin_setup.TernaryRelations;
create table RelationTables as select * from unielwin_setup.RelationTables;
create table NoPKeys as select * from  unielwin_setup.NoPKeys;
create table ForeignKeyColumns as select * from  unielwin_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  unielwin_setup.ForeignKeys_pvars;
create table InputColumns as select * from  unielwin_setup.InputColumns;
create table Attribute_Value as select * from  unielwin_setup.Attribute_Value;
create table Expansions as select * from unielwin_setup.Expansions;





