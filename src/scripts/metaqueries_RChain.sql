USE @database@_BN;
SET storage_engine=INNODB;

/* May 16th, last step for _CT tables, preparing the colunmname_list */
/* set up the join tables that represent the case where a relationship is false and its attributes are undefined */

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid as Lattice_Point,
    'Join' as TableType,
    'COLUMN' as ClauseType,
    '2nid' as EntryType,
    CONCAT(2nid, ' varchar(5)  default ', ' "N/A" ') AS Entries
FROM
    RNodes_2Nodes N, LatticeRNodes L
WHERE
    N.rnid = L.orig_rnid;

/**************
 * Generating flat, star, and join tables for Rnodes

/**********************************
 * Generating metqueries for the flat table.
 * For each rnode, the flat table drops the rnid and the 2nids from the rnodes_counts table. Then it sums up the remaining mults to get marginal sums.
 * That is, the counts are conditional on Rnode = T but we drop the rnid and 2nid ids.
 * so we have to sum over them
 */


/* the base table is the rnode counts
 * 
 */
INSERT into MetaQueries
select DISTINCT 
    orig_rnid AS Lattice_Point,
    'Flat' AS TableType,
    'FROM' AS ClauseType,
    'table' AS EntryType,
    concat('`',replace(short_rnid, '`', ''),'_counts`') AS Entries
from LatticeRNodes;

/********
 * copy group by columns from Rnodes_counts to Rnodes_flat
 * The groupby columns are renamed properly and do not contain the aggregate function
 * do NOT copy rnodes and associated 2nodes
 */

INSERT into MetaQueries
SELECT distinct Lattice_Point, 'Flat' as TableType, ClauseType, EntryType, Entries
FROM
    LatticeRNodes L, MetaQueries M
WHERE
    L.orig_rnid = M.Lattice_Point
    AND TableType = 'Counts'
    AND ClauseType = 'GROUPBY'
    AND EntryType <> 'rnid'
    AND EntryType <> '2nid';

/* use the same columns as in group by in select clause */

INSERT into MetaQueries
SELECT distinct Lattice_Point, 'Flat' as TableType, 'SELECT' AS ClauseType, EntryType, Entries
FROM
    LatticeRNodes L, MetaQueries M
WHERE
    L.orig_rnid = M.Lattice_Point
    AND TableType = 'Counts'
    AND ClauseType = 'GROUPBY'
    AND EntryType <> 'rnid'
    AND EntryType <> '2nid';

/* sum over all the mults in the counts table 
 * 
 */
INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Flat' AS TableType,
    'SELECT' AS ClauseType,
    'aggregate' AS EntryType,
    CONCAT('SUM(`',REPLACE(short_rnid, '`', ''),'_counts`.`MULT`)',' as "MULT"') AS Entries
from
    LatticeRNodes;

/****************
 * Now we work on the star tables. These are the ones were the Rnode value is unspecified. 
 * so we just need the union of the pvariable columns (except for the aggregate count)
 */

INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Star' AS TableType,
    'SELECT' AS ClauseType,
    '1nid' AS EntryType,
    Entries
FROM
    LatticeRNodes L, RNodes_pvars R, MetaQueries M
WHERE
    L.orig_rnid = R.rnid
    AND M.Lattice_Point = R.pvid
    AND ClauseType = 'GROUPBY';
    
/* Key condition: these fields must match the ones in the flat table. I.e. rnodes_counts - rnid, 1nid, multi = union of group by from pvariables
 * 
 */
/****group by list does not have the renaming that the select list does ****/

/**  Also need to multiply the mult columns from the different tables, e.g. 
select (t1.mult * t2.mult * t3.mult) as "MULT". Currently done in code
**/

/***
 * working on from list
 * insert all count tables from associated populations in from list for join
 */
INSERT into MetaQueries
SELECT DISTINCT
    orig_rnid AS Lattice_Point,
    'Star' AS TableType,
    'FROM' AS ClauseType,
    'table' AS EntryType,
concat('`',replace(pvid, '`', ''),'_counts`')
    AS Entries FROM
    LatticeRNodes L, RNodes_pvars R
    where L.orig_rnid = R.rnid;
    
   
/********************
 * August 24, 2017. The false table seems to be computed exclusively in the Java code and no longer in the script. This is probably because using SQL to do the join is too slow.
 * May change with Maria DB or Spark
 * 
 * now compute the False table. This is the difference of the star and R_counts table (R = F = R=* - R=T)
 */
    
    /********************
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
    /*
    SELECT distinct rnid, concat('`',replace(rnid, '`', ''),'_star`.',PV.Entries) AS Entries
FROM RNodes_pvars RP, PVariables_GroupBy_List PV where RP.pvid = PV.pvid;

create table ADT_RNodes_False_FROM_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`') as Entries from RNodes
union 
select distinct rnid, concat('`',replace(rnid, '`', ''),'_flat`') as Entries from RNodes;

create table ADT_RNodes_False_WHERE_List as
SELECT DISTINCT rnid, concat('`',replace(rnid, '`', ''),'_star`.',1nid,'=','`',replace(rnid, '`', ''),'_flat`.',1nid) as Entries from RNodes_1Nodes; 


/******************
 * now computing tables for all lattice points 
 */


/***** I hope this follows our paper from http://www.cs.sfu.ca/~oschulte/files/pubs/Qian2014.pdf */
/* Star table to be used in Pivot operation */
    /* lattice_rel child = current rchain
     * lattice_rel removed = current rnodeid i
     * lattice_rel . parent = curent rchain - current rnodeid
     */ 
/* the code needs us to record both the long rchain and the current rnodeid
 * We do that by using EntryType for the current rnode is. A bit awkward
 */
/*****************
 * start with from list
 */
    
CREATE or REPLACE VIEW RChain_pvars AS
select  distinct 
    M.name as rchain, 
    pvid 
from 
    lattice_membership M, LatticeRNodes L, RNodes_pvars R
where 
    R.rnid = L.orig_rnid AND L.orig_rnid = M.member;
    
insert into MetaQueries
SELECT DISTINCT 
    lattice_rel.child as Lattice_Point, 
    'STAR' as TableType, 
    'FROM' as ClauseType,
    lattice_rel.removed as EntryType, 
    /** rnid = lattice_rel.removed should now point to the R_i of our paper **/
    /* a bit awkward to call it entry type */
    concat('`',replace(lattice_rel.parent,'`',''),'_CT`')  AS Entries 
    /* current CT should be like conditioning on all other relationships being true */
    /* the parent represents the shortened Rchain */
FROM
    lattice_rel
where 
    lattice_rel.parent <>'EmptySet';
    /* i.e. child = rchain has length > 1; */


insert into MetaQueries
SELECT DISTINCT 
    LR.child as Lattice_Point, 
    'STAR' as TableType, 
    'FROM' as ClauseType,
    LR.removed as EntryType,
concat('`',replace(R.pvid, '`', ''),'_counts`')    AS Entries 
/* should check that this includes expansion for pvid = course 0 */
FROM
    lattice_rel LR, LatticeRNodes L, RNodes_pvars R
WHERE
    LR.parent <> 'EmptySet'
    AND LR.removed = L.orig_rnid
    AND L.orig_rnid = R.rnid AND
R.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = LR.parent);
/* this seems to implement the "differing first-order variable rule" from the paper */
/* find pvids that are associated with the removed rnode but not with the shortened rchain = parent */

/***********
 * now doing the where list for the from list.
 * simply need to find the rows in the CT table for the shortened Rchain where all rnodes are set to T
 */


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
    /* going through rnids in order, find the rows in current CT-table where the remaining rnids are true */
    and lattice_rel.parent <>'EmptySet'
    AND L.orig_rnid = lattice_membership.member;

/****************
 * now the select clause. This finds the group by columns for all rnids in the shortened parent rchain
 Problem: for some reason the insertion fails. Inserting into the same table? But it worked for the rnid
 now need to change java code to call this view
 */

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


/* find all elements in the groupBy List for the shortened parent rchain */
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
WHERE
    LR.parent <> 'EmptySet'
    AND LR.removed = L.orig_rnid
    AND L.orig_rnid = R.rnid AND
R.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = LR.parent)
AND M.Lattice_Point = R.pvid
AND M.ClauseType = 'GROUPBY'
AND M.TableType = 'COUNTS';
/* The case where the parent is empty.*
 * In this case the rchain child contains just one rnid.
 * in this case we just insert the select entries from the star table for the rnide.
 * Not sure we need this - should try leaving it out. OS. August 25, 2017
/* again should be able to make it a call to itself but have to make it a view
 */

insert into MetaQueries
SELECT DISTINCT 
    lattice_rel.removed AS Lattice_Point, 
    'STAR' as TableType, 
    'SELECT' as ClauseType,
    lattice_rel.removed AS EntryType,
    M.Entries 
FROM lattice_rel, MetaQueries M
WHERE lattice_rel.parent = 'EmptySet' AND M.Lattice_Point = lattice_rel.removed AND M.TableType = 'STAR' and M.ClauseType = 'SELECT';