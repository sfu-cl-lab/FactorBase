DROP SCHEMA IF EXISTS New_Financial_std_BN; 
create schema New_Financial_std_BN;

DROP SCHEMA IF EXISTS New_Financial_std_CT; 
create schema New_Financial_std_CT;

USE New_Financial_std_BN;
SET storage_engine=INNODB;





create table 1Nodes as select * from New_Financial_std_setup.1Nodes;
create table 2Nodes as select * from New_Financial_std_setup.2Nodes;
create table RNodes as select * from New_Financial_std_setup.RNodes;
create table PVariables as select * from New_Financial_std_setup.PVariables;
create table EntityTables as select * from New_Financial_std_setup.EntityTables;
create table AttributeColumns as select * from New_Financial_std_setup.AttributeColumns;
create table TernaryRelations as select * from New_Financial_std_setup.TernaryRelations;
create table RelationTables as select * from New_Financial_std_setup.RelationTables;
create table NoPKeys as select * from  New_Financial_std_setup.NoPKeys;
create table ForeignKeyColumns as select * from  New_Financial_std_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  New_Financial_std_setup.ForeignKeys_pvars;
create table InputColumns as select * from  New_Financial_std_setup.InputColumns;
create table Attribute_Value as select * from  New_Financial_std_setup.Attribute_Value;





