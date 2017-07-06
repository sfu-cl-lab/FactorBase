DROP SCHEMA IF EXISTS unielwin_BN; 
create schema unielwin_BN;

DROP SCHEMA IF EXISTS unielwin_CT; 
create schema unielwin_CT;

USE unielwin_BN;
SET storage_engine=INNODB;




create table FunctorSet as select * from unielwin_setup.FunctorSet; 
create table 1Nodes as select 
	N.1nid,
	N.COLUMN_NAME,
	N.pvid, 
	N.main
	from unielwin_setup.1Nodes N, FunctorSet F where N.1nid = F.fid and N.main = F.main;
create table 2Nodes as select 
	N.2nid,
	N.COLUMN_NAME,
	N.pvid1, 
	N.pvid2,
	N.TABLE_NAME,
	N.main
	from unielwin_setup.2Nodes N, FunctorSet F where N.2nid = F.Fid and N.main = F.main;
create table RNodes as select 
	N.orig_rnid,
	N.TABLE_NAME,
	N.pvid1,
	N.pvid2, 
	N.COLUMN_NAME1,
	N.COLUMN_NAME2,
	N.rnid, 
	N.main
	from unielwin_setup.RNodes N, FunctorSet F where N.orig_rnid = F.Fid and N.main = F.main;

create table PVariables as select * from unielwin_setup.PVariables P where P.pvid in (select pvid from FNodes_pvars);


create table Expansions like unielwin_setup.Expansions; 
insert into Expansions select * from unielwin_setup.Expansions;





create table EntityTables as select * from unielwin_setup.EntityTables;
create table AttributeColumns as select * from unielwin_setup.AttributeColumns;
create table TernaryRelations as select * from unielwin_setup.TernaryRelations;
create table RelationTables as select * from unielwin_setup.RelationTables;
create table NoPKeys as select * from  unielwin_setup.NoPKeys;
create table ForeignKeyColumns as select * from  unielwin_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  unielwin_setup.ForeignKeys_pvars;
create table InputColumns as select * from  unielwin_setup.InputColumns;
create table Attribute_Value as select * from  unielwin_setup.Attribute_Value;






