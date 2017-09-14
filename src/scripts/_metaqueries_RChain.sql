USE unielwin_BN;
SET storage_engine=INNODB;




INSERT into MetaQueries
select distinct short_rnid as Lattice_Point, 'Join' as TableType, 'COLUMN' as ClauseType, '2nid' as EntryType, concat(2nid,
' varchar(5)  default ',' "N/A" ') as Entries from RNodes_2Nodes N, LatticeRNodes L where N.rnid = L.orig_rnid;





INSERT into MetaQueries
select DISTINCT 
    short_rnid as Lattice_Point, 'Flat' as TableType, 'FROM' as ClauseType , 'table' as EntryType, 
    concat('`',replace(short_rnid, '`', ''),'_counts`') AS Entries
from LatticeRNodes;



INSERT into MetaQueries
SELECT distinct Lattice_Point, 'Flat' as TableType, ClauseType, EntryType, Entries
FROM LatticeRNodes L, MetaQueries M where L.short_rnid = M.Lattice_Point and TableType = 'Counts' and ClauseType = 'GROUPBY'
and EntryType <> 'rnid' and EntryType <> '2nid';



INSERT into MetaQueries
SELECT distinct Lattice_Point, 'Flat' as TableType, 'SELECT' AS ClauseType, EntryType, Entries
FROM LatticeRNodes L, MetaQueries M where L.short_rnid = M.Lattice_Point and TableType = 'Counts' and ClauseType = 'GROUPBY'
and EntryType <> 'rnid' and EntryType <> '2nid';


INSERT into MetaQueries
SELECT distinct short_rnid as Lattice_Point, 'Flat' as TableType, 'SELECT' AS ClauseType, 'aggregate' as EntryType,
    concat('sum(`',replace(short_rnid, '`', ''),'_counts`.`MULT`)',' as "MULT"') AS Entries
from
    LatticeRNodes;



INSERT into MetaQueries
SELECT distinct short_rnid as Lattice_Point, 'Star' as TableType, 'SELECT' as ClauseType, '1nid' as EntryType, Entries FROM
    LatticeRNodes L, RNodes_pvars R, MetaQueries M
WHERE
    L.orig_rnid = R.rnid and M.Lattice_Point = R.pvid and ClauseType = 'GROUPBY';
    






INSERT into MetaQueries
SELECT DISTINCT short_rnid as Lattice_Point, 'Star' as TableType, 'FROM' as ClauseType, 'table' as EntryType, 
concat('`',replace(pvid, '`', ''),'_counts`')
    AS Entries FROM
    LatticeRNodes L, RNodes_pvars R
    where L.orig_rnid = R.rnid;
    
   

    
    
 
    




     


    
CREATE or REPLACE VIEW RChain_pvars AS
select  distinct 
    M.name as rchain, 
    pvid 
from 
    lattice_membership M, LatticeRNodes L, RNodes_pvars R
where 
    R.rnid = L.orig_rnid and L.short_rnid = M.member;
    
insert into MetaQueries
SELECT DISTINCT 
    lattice_rel.child as Lattice_Point, 
    'STAR' as TableType, 
    'FROM' as ClauseType,
    lattice_rel.removed as EntryType, 
    
    
    concat('`',replace(lattice_rel.parent,'`',''),'_CT`')  AS Entries 
    
    
FROM
    lattice_rel
where 
    lattice_rel.parent <>'EmptySet';
    


insert into MetaQueries
SELECT DISTINCT 
    LR.child as Lattice_Point, 
    'STAR' as TableType, 
    'FROM' as ClauseType,
    LR.removed as EntryType,
concat('`',replace(R.pvid, '`', ''),'_counts`')    AS Entries 

FROM
    lattice_rel LR, LatticeRNodes L, RNodes_pvars R
where LR.parent <>'EmptySet' and LR.removed = L.short_rnid and L.orig_rnid = R.rnid and
R.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = LR.parent);






insert into MetaQueries
SELECT DISTINCT 
     lattice_rel.child as Lattice_Point, 
    'STAR' as TableType, 
    'WHERE' as ClauseType,
    lattice_rel.removed as EntryType, 
    concat(L.orig_rnid,' = "T"')  AS Entries 
FROM
    lattice_rel,    lattice_membership, LatticeRNodes L
where 
    lattice_rel.child = lattice_membership.name
    and  lattice_membership.member > lattice_rel.removed
    
    and lattice_rel.parent <>'EmptySet'
    and L.short_rnid = lattice_membership.member;



insert into MetaQueries
SELECT DISTINCT 
    lattice_rel.child AS Lattice_Point, 
    'STAR' as TableType, 
    'SELECT' as ClauseType,
    lattice_rel.removed AS EntryType,
    M.Entries 
FROM
    lattice_rel,
    lattice_membership,
    MetaQueries M
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND lattice_membership.name = lattice_rel.parent
        AND M.Lattice_Point = lattice_membership.`member`
        AND M.ClauseType = 'GROUPBY'
        AND M.TableType = 'COUNTS';



insert into MetaQueries
SELECT DISTINCT 
    LR.child as Lattice_Point, 
    'STAR' as TableType, 
    'SELECT' as ClauseType,
    LR.removed as EntryType,
    M.Entries 
FROM
    lattice_rel LR, LatticeRNodes L, RNodes_pvars R,
    MetaQueries M
where LR.parent <>'EmptySet' and LR.removed = L.short_rnid and L.orig_rnid = R.rnid and
R.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = LR.parent)
AND M.Lattice_Point = R.pvid
AND M.ClauseType = 'GROUPBY'
AND M.TableType = 'COUNTS';


insert into MetaQueries
SELECT DISTINCT 
    lattice_rel.removed AS Lattice_Point, 
    'STAR' as TableType, 
    'SELECT' as ClauseType,
    lattice_rel.removed AS EntryType,
    M.Entries 
FROM lattice_rel, MetaQueries M
WHERE lattice_rel.parent = 'EmptySet' AND M.Lattice_Point = lattice_rel.removed AND M.TableType = 'STAR' and M.ClauseType = 'SELECT';
