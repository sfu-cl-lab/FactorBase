


DROP SCHEMA IF EXISTS unielwin_BN; 
create schema unielwin_BN;

DROP SCHEMA IF EXISTS unielwin_CT; 
create schema unielwin_CT;

USE unielwin_BN;
SET storage_engine=INNODB;





 

CREATE TABLE 1Nodes AS SELECT N.1nid, N.COLUMN_NAME, N.pvid, N.main FROM
    unielwin_setup.1Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.1nid = F.fid;

CREATE TABLE 2Nodes AS SELECT N.2nid,
    N.COLUMN_NAME,
    N.pvid1,
    N.pvid2,
    N.TABLE_NAME,
    N.main FROM
    unielwin_setup.2Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.2nid = F.Fid;
    
     
    
CREATE TABLE RNodes_2Nodes AS 
select N.rnid, N.`2nid`, N.main from unielwin_setup.RNodes_2Nodes N, 2Nodes F where N.2nid = F.2nid;

     
    
CREATE TABLE RNodes AS SELECT 
    N.rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.main FROM
    unielwin_setup.RNodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.rnid = F.Fid

    union DISTINCT
    select N.rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.main FROM
    unielwin_setup.RNodes N, RNodes_2Nodes F where N.rnid = F.rnid;
    
    


 CREATE TABLE FNodes (   
  `Fid` varchar(199) ,
  `FunctorName` varchar(64) ,
  `Type` varchar(5) ,
  `main` int(11) ,
  PRIMARY KEY  (`Fid`)
);




insert into FNodes
SELECT 
    1nid AS Fid,
    COLUMN_NAME as FunctorName,
    '1Node' as Type,
    main
FROM
    1Nodes 
UNION SELECT 
    2nid AS Fid,
    COLUMN_NAME as FunctorName,
    '2Node' as Type,
    main
FROM
    2Nodes 
union select 
    rnid as Fid,
    TABLE_NAME as FunctorName,
    'Rnode' as Type,
    main
from
    RNodes;


    
create table FNodes_pvars AS SELECT N.Fid, N.pvid
FROM unielwin_setup.FNodes_pvars N, FNodes F where N.Fid = F.Fid;

create table RNodes_pvars as SELECT N.rnid, N.pvid, N.TABLE_NAME, N.COLUMN_NAME, N.REFERENCED_COLUMN_NAME
FROM unielwin_setup.RNodes_pvars N, RNodes F where N.rnid = F.rnid;



create table PVariables as SELECT DISTINCT N.pvid, N.ID_COLUMN_NAME ,N.TABLE_NAME, N.index_number
FROM unielwin_setup.PVariables N, FNodes_pvars F where F.pvid = N.pvid;









create table EntityTables as select * from unielwin_setup.EntityTables;
create table AttributeColumns as select * from unielwin_setup.AttributeColumns;
    create table Attribute_Value as select * from unielwin_setup.Attribute_Value;

create table RelationTables as select * from unielwin_setup.RelationTables;

create table ForeignKeyColumns as select * from  unielwin_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  unielwin_setup.ForeignKeys_pvars;
create table InputColumns as select * from  unielwin_setup.InputColumns;
create table Expansions as select * from unielwin_setup.Expansions;
create table Groundings as select * from unielwin_setup.Groundings;
create table TargetNode as select * from unielwin_setup.TargetNode;


create table LatticeRNodes as SELECT rnid as orig_rnid, TABLE_NAME, pvid1, pvid2, COLUMN_NAME1, COLUMN_NAME2, main
FROM RNodes;

ALTER TABLE LatticeRNodes ADD COLUMN `short_rnid` VARCHAR(10) NULL , ADD UNIQUE INDEX `rnid_UNIQUE` (`short_rnid` ASC) ;




