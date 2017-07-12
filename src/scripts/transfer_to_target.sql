/*
 * transfer_to_target.sql
 * 		- Creates @database@_target_setup schema
 * 		- Gets Markov Blanket for Functors
 * Author: Kurt Routley
 * Date: September 23, 2013
 */

DROP SCHEMA IF EXISTS @database@_target_setup; 
create schema @database@_target_setup;

USE @database@_target_setup;
SET storage_engine=INNODB;

/*copy tables from setup database */
create table FNodes as select * from @database@_BN.FNodes;
create table FNodes_pvars as select * from @database@_BN.FNodes_pvars;
create table PVariables as select * from @database@_BN.PVariables;
create table EntityTables as select * from @database@_BN.EntityTables;
create table AttributeColumns as select * from @database@_BN.AttributeColumns;
create table TernaryRelations as select * from @database@_BN.TernaryRelations;
create table RelationTables as select * from @database@_BN.RelationTables;
create table NoPKeys as select * from  @database@_BN.NoPKeys;
create table ForeignKeyColumns as select * from  @database@_BN.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  @database@_BN.ForeignKeys_pvars;
create table InputColumns as select * from  @database@_BN.InputColumns;
create table Attribute_Value as select * from  @database@_BN.Attribute_Value;
create table RNodes_pvars as select * from @database@_BN.RNodes_pvars;


/*June 19, 2014, for testing database*/
create table FNodes_pvars_UNION_RNodes_pvars  as select * from @database@_BN.FNodes_pvars_UNION_RNodes_pvars ; 


DROP SCHEMA IF EXISTS @database@_target; 
create schema @database@_target;

