USE @database@_BN;
SET storage_engine=INNODB;

/* May 16th, last step for _CT tables, preparing the colunmname_list */

INSERT into MetaQueries
select distinct short_rnid as Lattice_Point, 'Join' as TableType, 'COLUMN' as ClauseType, 'column' as EntryType,concat(2nid,
' varchar(5)  default ',' "N/A" ') as Entries from RNodes_2Nodes N, LatticeRNodes L where N.rnid = L.orig_rnid;


CREATE TABLE ADT_RNodes_1Nodes_Select_List AS 
select 
    rnid, concat('sum(`',replace(rnid, '`', ''),'_counts`.`MULT`)',' as "MULT"') AS Entries
from
    RNodes
union
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes
    UNION DISTINCT
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;

CREATE TABLE ADT_RNodes_1Nodes_FROM_List AS 
select 
    rnid, concat('`',replace(rnid, '`', ''),'_counts`') AS Entries
from RNodes
;

CREATE TABLE ADT_RNodes_1Nodes_GroupBY_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes 
    UNION DISTINCT
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;




CREATE TABLE ADT_RNodes_Star_Select_List AS 
SELECT DISTINCT rnid,
    1nid AS Entries FROM
    RNodes_1Nodes
    UNION DISTINCT
    SELECT distinct rnid, PV.Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;

/** TODO: Also need to multiply the mult columns from the different tables, e.g. 
select (t1.mult * t2.mult * t3.mult) as "MULT"
**/

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
    /*for each associated to rnode pvariable in expansion , find the primary column and add it to the group by list */
 /* don't use this for continuous, but do use it for the no_link case */
    SELECT distinct rnid, concat('`',replace(rnid, '`', ''),'_star`.',PV.Entries) AS Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;

create table ADT_RNodes_False_FROM_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`') as Entries from RNodes
union 
select distinct rnid, concat('`',replace(rnid, '`', ''),'_flat`') as Entries from RNodes;

create table ADT_RNodes_False_WHERE_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`.',1nid,'=','`',replace(rnid, '`', ''),'_flat`.',1nid) as Entries from RNodes_1Nodes; 


/*May 17th*/


/** TODO: Also need to multiply the mult columns from the different tables, e.g. 
select (t1.mult * t2.mult * t3.mult) as "MULT"
**/
CREATE TABLE RChain_pvars AS
select  distinct 
    lattice_membership.name as rchain, 
    pvid 
from 
    lattice_membership, RNodes_pvars 
where 
    RNodes_pvars.rnid = lattice_membership.orig_rnid;
 
/***** I hope this follows our paper from http://www.cs.sfu.ca/~oschulte/files/pubs/Qian2014.pdf */
/* Star table to be used in Pivot operation */

CREATE TABLE ADT_RChain_Star_From_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.orig_rnid as rnid, 
    /** rnid = lattice_rel.removed should now point to the R_i of our paper **/
    concat('`',replace(lattice_rel.parent,'`',''),'_CT`')  AS Entries 
    /* current CT should be like conditioning on all other relationships being true */
FROM
    lattice_rel
where 
    lattice_rel.parent <>'EmptySet'
union
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.orig_rnid as rnid, 
concat('`',replace(RNodes_pvars.pvid, '`', ''),'_counts`')    AS Entries 
/* should check that this includes expansion for pvid = course 0 */
FROM
    lattice_rel, RNodes_pvars
where lattice_rel.parent <>'EmptySet'
and RNodes_pvars.rnid = lattice_rel.orig_rnid and
RNodes_pvars.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain =     lattice_rel.parent)
/* this seems to implement the "differing first-order variable rule" from the paper */
;


CREATE TABLE ADT_RChain_Star_Where_List AS 
SELECT DISTINCT 
    lattice_rel.child as rchain, 
    lattice_rel.orig_rnid as rnid, 
    concat(lattice_membership.orig_rnid,' = "T"')  AS Entries 
FROM
    lattice_rel,    lattice_membership
where 
    lattice_rel.child = lattice_membership.name
    and  lattice_membership.orig_rnid > lattice_rel.orig_rnid
    /* going through rnids in order, find the rows in current CT-table where the remaining rnids are true */
    and lattice_rel.parent <>'EmptySet';



CREATE TABLE ADT_RChain_Star_Select_List AS 
SELECT DISTINCT 
    lattice_rel.child AS rchain,
    lattice_rel.orig_rnid AS rnid,
    RNodes_GroupBy_List.Entries 
FROM
    lattice_rel,
    lattice_membership,
    RNodes_GroupBy_List
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND lattice_membership.name = lattice_rel.parent
        AND RNodes_GroupBy_List.rnid = lattice_membership.orig_rnid 
/* find all elements in the groupBy List for the big parent rchain */
/* should this be just the ones for groupby except for the removed rnid? */
UNION SELECT DISTINCT
    lattice_rel.child AS rchain,
    lattice_rel.orig_rnid AS rnid,
    1Nodes.1nid AS Entries
FROM
    lattice_rel,
    RNodes_pvars,
    1Nodes
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.orig_rnid
        AND RNodes_pvars.pvid = 1Nodes.pvid
        AND 1Nodes.pvid NOT IN (SELECT 
            pvid
        FROM
            RChain_pvars
        WHERE
            RChain_pvars.rchain = lattice_rel.parent) 
/* July 19, 2017 O.S. If we are going to add the 1nodes for the pvariable we also need to add the ID column if any*/
/* can this just be the PVariables GroupBY list? Like below for the empty parent case? */
UNION SELECT DISTINCT
    lattice_rel.child AS rchain,
    lattice_rel.orig_rnid AS rnid,
    CONCAT('`ID(', E.pvid, ')`') AS Entries
FROM
    lattice_rel,
    RNodes_pvars,
    Expansions E
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.orig_rnid
        AND RNodes_pvars.pvid = E.pvid
        AND E.pvid NOT IN (SELECT 
            pvid
        FROM
            RChain_pvars
        WHERE
            RChain_pvars.rchain = lattice_rel.parent) 
/* The case where the parent is empty.*/
UNION SELECT DISTINCT
    lattice_rel.orig_rnid AS rchain,
    lattice_rel.orig_rnid AS rnid,
    1Nodes.1nid AS Entries
FROM
    lattice_rel,
    RNodes_pvars,
    1Nodes
WHERE
    lattice_rel.parent = 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.orig_rnid
        AND RNodes_pvars.pvid = 1Nodes.pvid 
UNION DISTINCT SELECT DISTINCT
    lattice_rel.orig_rnid AS rchain,
    lattice_rel.orig_rnid AS rnid,
    PV.Entries
FROM
    lattice_rel,
    RNodes_pvars RP,
    PVariables_GroupBy_List PV
    /* check that PVariablesGroupByList includes the ID column */
WHERE
    lattice_rel.parent = 'EmptySet'
        AND RP.rnid = lattice_rel.orig_rnid
        AND RP.pvid = PV.pvid;
