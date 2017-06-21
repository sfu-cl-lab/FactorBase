DROP SCHEMA IF EXISTS MovieLens_Small_Training1_target_BN; 
create schema MovieLens_Small_Training1_target_BN;

DROP SCHEMA IF EXISTS MovieLens_Small_Training1_target_CT; 
create schema MovieLens_Small_Training1_target_CT;

USE MovieLens_Small_Training1_target_BN;
SET storage_engine=INNODB;





create table 1Nodes as select * from MovieLens_Small_Training1_target_setup.1Nodes;
create table 2Nodes as select * from MovieLens_Small_Training1_target_setup.2Nodes;
create table RNodes as select * from MovieLens_Small_Training1_target_setup.RNodes;
create table PVariables as select * from MovieLens_Small_Training1_target_setup.PVariables;
create table EntityTables as select * from MovieLens_Small_Training1_target_setup.EntityTables;
create table AttributeColumns as select * from MovieLens_Small_Training1_target_setup.AttributeColumns;
create table TernaryRelations as select * from MovieLens_Small_Training1_target_setup.TernaryRelations;
create table RelationTables as select * from MovieLens_Small_Training1_target_setup.RelationTables;
create table NoPKeys as select * from  MovieLens_Small_Training1_target_setup.NoPKeys;
create table ForeignKeyColumns as select * from  MovieLens_Small_Training1_target_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  MovieLens_Small_Training1_target_setup.ForeignKeys_pvars;
create table InputColumns as select * from  MovieLens_Small_Training1_target_setup.InputColumns;
create table Attribute_Value as select * from  MovieLens_Small_Training1_target_setup.Attribute_Value;

create table FNodes_pvars_UNION_RNodes_pvars  as select * from MovieLens_Small_Training1_target_setup.FNodes_pvars_UNION_RNodes_pvars ;  
create table Test_Node like MovieLens_Small_Training1_target_setup.Test_Node; 
insert into Test_Node select * from MovieLens_Small_Training1_target_setup.Test_Node; 

insert into 1Nodes 
SELECT 
    CONCAT('`', COLUMN_NAME, '(', pvid, ')', '`') AS 1nid, 
COLUMN_NAME,
pvid,
index_number = 0 AS main
FROM
    EntityTables
        natural join
    PVariables
natural join 
FNodes_pvars_UNION_RNodes_pvars
natural join 
Test_Node;


Drop table if exists Test_1nid ;
create table Test_1nid as
SELECT 
    CONCAT('`', COLUMN_NAME, '(', pvid, ')', '`') AS 1nid, 
COLUMN_NAME,
pvid,
index_number = 0 AS main
FROM
    EntityTables
        natural join
    PVariables
natural join 
FNodes_pvars_UNION_RNodes_pvars
natural join 
Test_Node;









