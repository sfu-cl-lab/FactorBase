

DROP SCHEMA IF EXISTS MovieLens_Small_Training1_target_setup; 
create schema MovieLens_Small_Training1_target_setup;

USE MovieLens_Small_Training1_target_setup;
SET storage_engine=INNODB;


create table FNodes as select * from MovieLens_Small_Training1_BN.FNodes;
create table FNodes_pvars as select * from MovieLens_Small_Training1_BN.FNodes_pvars;
create table PVariables as select * from MovieLens_Small_Training1_BN.PVariables;
create table EntityTables as select * from MovieLens_Small_Training1_BN.EntityTables;
create table AttributeColumns as select * from MovieLens_Small_Training1_BN.AttributeColumns;
create table TernaryRelations as select * from MovieLens_Small_Training1_BN.TernaryRelations;
create table RelationTables as select * from MovieLens_Small_Training1_BN.RelationTables;
create table NoPKeys as select * from  MovieLens_Small_Training1_BN.NoPKeys;
create table ForeignKeyColumns as select * from  MovieLens_Small_Training1_BN.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  MovieLens_Small_Training1_BN.ForeignKeys_pvars;
create table InputColumns as select * from  MovieLens_Small_Training1_BN.InputColumns;
create table Attribute_Value as select * from  MovieLens_Small_Training1_BN.Attribute_Value;
create table RNodes_pvars as select * from MovieLens_Small_Training1_BN.RNodes_pvars;



create table FNodes_pvars_UNION_RNodes_pvars  as select * from MovieLens_Small_Training1_BN.FNodes_pvars_UNION_RNodes_pvars ; 


DROP SCHEMA IF EXISTS MovieLens_Small_Training1_target; 
create schema MovieLens_Small_Training1_target;

