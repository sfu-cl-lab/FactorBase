USE unielwin_BN;

CREATE TABLE ADT_PVariables_Select_List AS 
SELECT 
    pvid,CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries 
FROM
    1Nodes
        NATURAL JOIN
    PVariables
    UNION
 
 
 
 SELECT E.pvid, CONCAT(E.pvid,'.',REFERENCED_COLUMN_NAME, ' AS `', REFERENCED_COLUMN_NAME, '`') AS Entries FROM
 RNodes_pvars RP, Expansions E where E.pvid = RP.pvid
 union distinct
 SELECT distinct
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
    from PVariables;


create table ADT_PVariables_GroupBy_List as
SELECT 
    pvid,1nid AS Entries 
FROM
    1Nodes
        NATURAL JOIN
    PVariables
    UNION
 
 
 
SELECT E.pvid, CONCAT('`', REFERENCED_COLUMN_NAME, '`') AS Entries FROM
 RNodes_pvars RP, Expansions E where E.pvid = RP.pvid;










CREATE TABLE ADT_PVariables_From_List AS SELECT pvid, CONCAT('unielwin.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;










CREATE TABLE ADT_RNodes_1Nodes_Select_List AS 
select 
    rnid, concat('sum(`',replace(rnid, '`', ''),'_counts`.`MULT`)',' as "MULT"') AS Entries
from
    RNodes
union
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes 
;

CREATE TABLE ADT_RNodes_1Nodes_FROM_List AS 
select 
    rnid, concat('`',replace(rnid, '`', ''),'_counts`') AS Entries
from RNodes
;

CREATE TABLE ADT_RNodes_1Nodes_GroupBY_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes 
;




CREATE TABLE ADT_RNodes_Star_Select_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes
    UNION DISTINCT
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;



CREATE TABLE ADT_RNodes_Star_From_List AS 
SELECT DISTINCT rnid, concat('`',replace(pvid, '`', ''),'_counts`')
    AS Entries FROM
    RNodes_pvars;




create table ADT_RNodes_False_Select_List as
SELECT DISTINCT rnid, concat('(`',replace(rnid, '`', ''),'_star`.MULT','-','`',replace(rnid, '`', ''),'_flat`.MULT)',' AS "MULT"') as Entries
from RNodes
union
SELECT DISTINCT rnid,
    concat('`',replace(rnid, '`', ''),'_star`.',1nid) AS Entries FROM
    RNodes_1Nodes
    UNION DISTINCT
    
 
    SELECT distinct rnid, concat('`',replace(rnid, '`', ''),'_star`.',PV.Entries) AS Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;

create table ADT_RNodes_False_FROM_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`') as Entries from RNodes
union 
select distinct rnid, concat('`',replace(rnid, '`', ''),'_flat`') as Entries from RNodes;

create table ADT_RNodes_False_WHERE_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`.',1nid,'=','`',replace(rnid, '`', ''),'_flat`.',1nid) as Entries from RNodes_1Nodes; 


create table Rnodes_join_columnname_list as 
select distinct rnid,concat(2nid, ' varchar(5)  default ',' "N/A" ') as Entries from 2Nodes natural join RNodes;







CREATE TABLE RChain_pvars AS
select  distinct 
    lattice_membership.name as rchain, 
    pvid 
from 
    lattice_membership, RNodes_pvars 
where 
    RNodes_pvars.rnid = lattice_membership.member;
 

CREATE TABLE ADT_RChain_Star_From_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    concat('`',replace(lattice_rel.parent,'`',''),'_CT`')  AS Entries 
FROM
    lattice_rel
where 
    lattice_rel.parent <>'EmptySet'
union
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
concat('`',replace(RNodes_pvars.pvid, '`', ''),'_counts`')    AS Entries 
FROM
    lattice_rel,RNodes_pvars
where lattice_rel.parent <>'EmptySet'
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain =     lattice_rel.parent)
;


CREATE TABLE ADT_RChain_Star_Where_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    concat(lattice_membership.member,' = "T"')  AS Entries 
FROM
    lattice_rel,    lattice_membership
where 
    lattice_rel.child = lattice_membership.name
    and  lattice_membership.member > lattice_rel.removed
    and lattice_rel.parent <>'EmptySet';



CREATE TABLE ADT_RChain_Star_Select_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    RNodes_GroupBy_List.Entries 
FROM
    lattice_rel,lattice_membership,RNodes_GroupBy_List
where 
    lattice_rel.parent <>'EmptySet'  and lattice_membership.name = lattice_rel.parent
and RNodes_GroupBy_List.rnid = lattice_membership.member
union
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.removed as rnid, 
    1Nodes.1nid    AS Entries 
FROM
    lattice_rel,RNodes_pvars,1Nodes
where lattice_rel.parent <>'EmptySet' 
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid = 1Nodes.pvid and  1Nodes.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain =  lattice_rel.parent)
union
SELECT DISTINCT  
    lattice_rel.removed as rchain, 
    lattice_rel.removed as rnid, 
    1Nodes.1nid    AS Entries 
FROM
    lattice_rel,RNodes_pvars,1Nodes
where lattice_rel.parent ='EmptySet' 
and RNodes_pvars.rnid = lattice_rel.removed and
RNodes_pvars.pvid = 1Nodes.pvid 
UNION DISTINCT
SELECT DISTINCT  
    lattice_rel.removed as rchain, 
    lattice_rel.removed as rnid, 
    PV.Entries   
    from lattice_rel,RNodes_pvars RP,PVariables_GroupBy_List PV
    where lattice_rel.parent ='EmptySet' 
and RP.rnid = lattice_rel.removed and
RP.pvid = PV.pvid;


