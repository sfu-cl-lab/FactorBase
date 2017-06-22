DROP SCHEMA IF EXISTS jeffery_prozon_three_BN; 
create schema jeffery_prozon_three_BN;

DROP SCHEMA IF EXISTS jeffery_prozon_three_CT; 
create schema jeffery_prozon_three_CT;

USE jeffery_prozon_three_BN;
SET storage_engine=INNODB;





create table 1Nodes as select * from jeffery_prozon_three_setup.1Nodes;
create table 2Nodes as select * from jeffery_prozon_three_setup.2Nodes;
create table RNodes as select * from jeffery_prozon_three_setup.RNodes;
create table PVariables as select * from jeffery_prozon_three_setup.PVariables;
create table EntityTables as select * from jeffery_prozon_three_setup.EntityTables;
create table AttributeColumns as select * from jeffery_prozon_three_setup.AttributeColumns;
create table TernaryRelations as select * from jeffery_prozon_three_setup.TernaryRelations;
create table RelationTables as select * from jeffery_prozon_three_setup.RelationTables;
create table NoPKeys as select * from  jeffery_prozon_three_setup.NoPKeys;
create table ForeignKeyColumns as select * from  jeffery_prozon_three_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  jeffery_prozon_three_setup.ForeignKeys_pvars;
create table InputColumns as select * from  jeffery_prozon_three_setup.InputColumns;
create table Attribute_Value as select * from  jeffery_prozon_three_setup.Attribute_Value;





