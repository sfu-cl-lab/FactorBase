USE unielwin_BN;
SET storage_engine=INNODB;





DROP TABLE IF EXISTS MetaQueries;

CREATE TABLE MetaQueries (   
  Lattice_Point varchar(199) , 
  TableType varchar(100) , 
  ClauseType varchar(10) , 
  EntryType varchar(100), 
  Entries varchar(150)
);




--- map Pvariables to entity tables ---

INSERT into MetaQueries
SELECT distinct pvid as Lattice_Point, 'Counts' as TableType, 'FROM' as ClauseType, 'table' as EntryType, CONCAT('unielwin.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;
    
   

    


  
    

    
INSERT into MetaQueries
SELECT DISTINCT
    pvid as Lattice_Point, 'Counts' as TableType, 'SELECT' as ClauseType, 'aggregate' as EntryType, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables;
 
  

    
INSERT into MetaQueries
SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType,  'SELECT' as ClauseType, '1node' as EntryType,
    CONCAT(P.pvid, '.', N.COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;


 
 
 INSERT into MetaQueries
 SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType,  'SELECT' as ClauseType, 'id' as EntryType, CONCAT(E.pvid,'.',P.ID_COLUMN_NAME, ' AS `ID(', E.pvid, ')`') AS Entries FROM
 PVariables P, Expansions E where E.pvid = P.pvid;
 
 
 
 
  
 INSERT into MetaQueries
 SELECT DISTINCT P.pvid as Lattice_Point, 'Counts' as TableType,  'GROUPBY' as ClauseType, '1node' as EntryType,
    1nid  AS Entries FROM
    1Nodes N, PVariables P where N.pvid = P.pvid;
    

    
INSERT into MetaQueries
SELECT distinct E.pvid AS Lattice_Point, 'Counts' as TableType, 'GROUPBY' as ClauseType, 'id' as EntryType, CONCAT('`ID(', E.pvid, ')`') AS Entries FROM
PVariables P, Expansions E where E.pvid = P.pvid;
 
 


INSERT into MetaQueries
SELECT distinct G.pvid AS Lattice_Point, 'Counts' as TableType,  'WHERE' as ClauseType, 'id' as EntryType, CONCAT('`ID(', G.pvid, ')` = ', G.id) AS Entries FROM
Groundings G;
 








INSERT into MetaQueries
SELECT DISTINCT
    short_rnid as Lattice_Point,'Counts' as TableType, 'FROM' as ClauseType, 'rtable' as EntryType, CONCAT('unielwin.',R.TABLE_NAME, ' AS ', rnid) AS Entries
FROM
    RNodes R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid;



INSERT into MetaQueries
SELECT DISTINCT
    short_rnid as Lattice_Point,'Counts' as TableType, 'FROM' as ClauseType, 'rtable' as EntryType,
    concat('(select "T" as ',
            rnid,
            ') as ',
            concat('`temp_', replace(rnid, '`', ''), '`')) as Entries
from
    RNodes R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid; 



INSERT into MetaQueries
SELECT DISTINCT short_rnid as Lattice_Point,'Counts' as TableType, 'WHERE' as ClauseType, 'rtable' as EntryType,
    CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME) AS Entries 
FROM
    RNodes_pvars R, LatticeRNodes L 
WHERE R.rnid = L.orig_rnid;





INSERT into MetaQueries
select 
    short_rnid as Lattice_Point, 'Counts' as TableType, 'SELECT' as ClauseType, 'rnid' as EntryType, orig_rnid AS Entries
from
    LatticeRNodes;

INSERT into MetaQueries
SELECT DISTINCT
short_rnid as Lattice_Point, 'Counts' as TableType, 'SELECT' as ClauseType, '2nid' as EntryType, CONCAT(L.orig_rnid, '.', COLUMN_NAME, ' AS ', N.2nid) AS Entries
FROM
    LatticeRNodes L, RNodes_2Nodes RN, 2Nodes N
where RN.rnid = L.orig_rnid and N.2nid = RN.2nid;




INSERT into MetaQueries
select 
    short_rnid as Lattice_Point, 'Counts' as TableType, 'GROUPBY' as ClauseType, 'rnid' as EntryType, orig_rnid AS Entries
from
    LatticeRNodes;
    
INSERT into MetaQueries
SELECT DISTINCT
short_rnid as Lattice_Point, 'Counts' as TableType, 'GROUPBY' as ClauseType, '2nid' as EntryType, N.2nid AS Entries
FROM
    LatticeRNodes L, RNodes_2Nodes RN, 2Nodes N
where RN.rnid = L.orig_rnid and N.2nid = RN.2nid order by RN.rnid,COLUMN_NAME;
    
 


INSERT into MetaQueries
SELECT distinct short_rnid as Lattice_Point, TableType, ClauseType, EntryType, Entries FROM
    LatticeRNodes L, RNodes_pvars R, MetaQueries M
WHERE
    L.orig_rnid = R.rnid and M.Lattice_Point = R.pvid;
    



 INSERT into MetaQueries
 select L.name AS Lattice_Point, 'Counts' as TableType, M.ClauseType, M.EntryType, M.Entries
 from lattice_membership L, MetaQueries M
 where L.name <> L.`member` and Lattice_Point = L.`member` and M.TableType = 'COUNTS';
 

