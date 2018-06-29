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
/*June 19, 2014, for testing database*/
create table FNodes_pvars_UNION_RNodes_pvars  as select * from @database@_setup.FNodes_pvars_UNION_RNodes_pvars ;  
create table Test_Node like @database@_setup.Test_Node; 
insert into Test_Node select * from @database@_setup.Test_Node; 

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

/*June 24, 2014, prepare the primary key list for testing data*/
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




/*
create table lattice_mapping as select * from @databasebase@_BN.lattice_mapping;
create table lattice_membership as select * from @databasebase@_BN.lattice_membership;
create table lattice_rel as select * from @databasebase@_BN.lattice_rel;
create table lattice_set as select * from @databasebase@_BN.lattice_set;
*/

/*
create table Path_BN_nodes as select * from  @database@_setup.Path_BN_nodes;
create table lattice_membership as select * from  @database@_setup.lattice_membership;
create table lattice_set as select * from  @database@_setup.lattice_set;
create table Path_Aux_Edges as select * from  @database@_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  @database@_setup.SchemaEdges;
*/


