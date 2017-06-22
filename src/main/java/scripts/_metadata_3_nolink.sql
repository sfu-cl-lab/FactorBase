
USE imdb_Gender_target_BN;
SET storage_engine=INNODB;


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
    rnid as FID,
    TABLE_NAME as FunctorName,
    'Rnode' as Type,
    main
from
    RNodes;


create table FNodes_pvars as 
SELECT FNodes.Fid, PVariables.pvid FROM
    FNodes,
    2Nodes,
    PVariables
where
    FNodes.Type = '2Node'
    and FNodes.Fid = 2Nodes.2nid
    and PVariables.pvid = 2Nodes.pvid1 
union 
SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    2Nodes,
    PVariables
where
    FNodes.Type = '2Node'
    and FNodes.Fid = 2Nodes.2nid
    and PVariables.pvid = 2Nodes.pvid2 
union 
SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    1Nodes,
    PVariables
where
    FNodes.Type = '1Node'
    and FNodes.Fid = 1Nodes.1nid
    and PVariables.pvid = 1Nodes.pvid;

create table 1Nodes_Select_List as select 1nid,
    concat(1Nodes.pvid,
            '.',
            1Nodes.COLUMN_NAME,
            ' AS ',
            1nid) as Entries from
    1Nodes,
    PVariables
where
    1Nodes.pvid = PVariables.pvid;

create table 1Nodes_From_List select 1nid,
    concat(PVariables.TABLE_NAME,
            ' AS ',
            PVariables.pvid) as Entries from
    1Nodes,
    PVariables
where
    1Nodes.pvid = PVariables.pvid;

create table 2Nodes_Select_List as select 2nid,
    concat(RNodes.rnid,
            '.',
            2Nodes.COLUMN_NAME,
            ' AS ',
            2nid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;

create table 2Nodes_From_List as select 2nid,
    concat(2Nodes.TABLE_NAME, ' AS ', RNodes.rnid) as Entries from
    2Nodes
        NATURAL JOIN
    RNodes;




CREATE TABLE PVariables_From_List AS SELECT pvid, CONCAT(TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables
WHERE
    index_number = 0;


CREATE TABLE PVariables_Select_List AS 
SELECT 
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables
UNION
SELECT pvid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables
WHERE
    PVariables.index_number = 0;



CREATE TABLE RNodes_pvars AS SELECT DISTINCT rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME 
FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid1 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME1
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME 
UNION 
SELECT DISTINCT
    rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME
FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid2 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME2
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;




CREATE TABLE RNodes_From_List AS SELECT DISTINCT rnid, CONCAT('imdb_Gender_target.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    RNodes_pvars 
UNION DISTINCT 
SELECT DISTINCT
    rnid, CONCAT('imdb_Gender_target.',TABLE_NAME, ' AS ', rnid) AS Entries
FROM
    RNodes 

union distinct 
select distinct
    rnid,
    concat('(select "T" as ',
            rnid,
            ') as ',
            concat('`temp_', replace(rnid, '`', ''), '`')) as Entries
from
    RNodes
  
;


CREATE TABLE RNodes_Where_List AS SELECT rnid,
    CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME) AS Entries 
FROM
    RNodes_pvars
 
    
    ;

CREATE TABLE RNodes_1Nodes AS SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid1 AS pvid FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid1 
UNION SELECT 
    rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid2 AS pvid
FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid2;

Create TABLE RNodes_2Nodes as 
select RNodes.rnid, 2Nodes.2nid from 2Nodes, RNodes where 2Nodes.TABLE_NAME = RNodes.TABLE_NAME; 



CREATE TABLE RNodes_Select_List AS 
select 
    rnid, concat('count(*)',' as "MULT"') AS Entries
from
    RNodes
union
SELECT DISTINCT rnid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries 
FROM
    RNodes_1Nodes 
UNION DISTINCT 
select temp.rnid,temp.Entries from (
SELECT DISTINCT
    rnid,
    CONCAT(rnid, '.', COLUMN_NAME, ' AS ', 2nid) AS Entries
FROM
    2Nodes
        NATURAL JOIN
    RNodes order by RNodes.rnid,COLUMN_NAME
) as temp

UNION distinct 
select 
    rnid, rnid AS Entries
from
    RNodes 
    
;

CREATE TABLE RNodes_GroupBy_List AS SELECT DISTINCT rnid, 1nid AS Entries FROM
    RNodes_1Nodes 
UNION DISTINCT 
SELECT DISTINCT
    rnid, 2nid AS Entries
FROM
    2Nodes
        NATURAL JOIN
    RNodes 

    ;



CREATE TABLE RNodes_BN_Nodes AS 
SELECT DISTINCT 
	rnid, 1nid AS Fid, FNodes.main 
FROM
    RNodes_1Nodes,
    FNodes
WHERE
    FNodes.Fid = 1nid 
UNION DISTINCT 
SELECT DISTINCT
    rnid, 2nid AS Fid, main
FROM
    2Nodes
        NATURAL JOIN
    RNodes;


CREATE TABLE  Entity_BayesNets (
    pvid VARCHAR(65) NOT NULL,
    child VARCHAR(131) NOT NULL,
    parent VARCHAR(131) NOT NULL,
    PRIMARY KEY (pvid , child , parent)
);



CREATE TABLE Path_BayesNets (
    Rchain VARCHAR(256) NOT NULL,
    child VARCHAR(197) NOT NULL,
    parent VARCHAR(197) NOT NULL,
    PRIMARY KEY (Rchain , child , parent)
);




ALTER TABLE `RNodes_BN_Nodes` ADD INDEX `Index_rnid`  (`rnid` ASC) ;
CREATE TABLE Path_BN_nodes AS 
SELECT DISTINCT lattice_membership.name AS Rchain, Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.member
    ORDER BY lattice_membership.name;

ALTER TABLE Path_BN_nodes ADD INDEX `HashIndex`  (`Rchain`,`node`); 


CREATE TABLE IF NOT EXISTS Knowledge_Forbidden_Edges like Path_BayesNets;
CREATE TABLE IF NOT EXISTS Knowledge_Required_Edges like Path_BayesNets;

CREATE table Path_Aux_Edges as SELECT 
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        FNodes
    WHERE
        BN_nodes1.Rchain = BN_nodes2.Rchain
            AND FNodes.Fid = BN_nodes1.node
            AND FNodes.main = 0;
ALTER TABLE Path_Aux_Edges ADD PRIMARY KEY (`Rchain`, `child`, `parent`);  


ALTER TABLE `2Nodes` ADD INDEX `index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ; 
ALTER TABLE `RNodes` ADD INDEX `Index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ;

create table SchemaEdges as 
select distinct 
    lattice_membership.name AS Rchain,
    2Nodes.2nid As child,
    RNodes.rnid AS parent 
from
    RNodes,
    2Nodes,
    lattice_membership
where
    lattice_membership.member = RNodes.rnid
        and RNodes.pvid1 = 2Nodes.pvid1
        and RNodes.pvid2 = 2Nodes.pvid2
        and RNodes.TABLE_NAME = 2Nodes.TABLE_NAME
        AND lattice_membership.name in (select 
            lattice_set.name
        from
            lattice_set
        where
            length = (select 
                    max(length)
                from
                    lattice_set));
ALTER TABLE SchemaEdges ADD INDEX `HashIn`  (`Rchain`,`child`,`parent`);

CREATE TABLE  Path_Required_Edges like Path_BayesNets;
insert ignore into Path_Required_Edges select distinct *    from        Knowledge_Required_Edges;

insert ignore into Path_Required_Edges select  distinct *    from        SchemaEdges;

   
CREATE TABLE Path_Forbidden_Edges like Path_BayesNets;
insert ignore into Path_Forbidden_Edges select     distinct *    from        Knowledge_Forbidden_Edges ;

insert ignore into Path_Forbidden_Edges   SELECT    distinct     *    FROM        Path_Aux_Edges ;


CREATE TABLE  LearnedEdges like Path_BayesNets;
CREATE TABLE  ContextEdges like Path_BayesNets;
CREATE TABLE  InheritedEdges like Path_BayesNets;













CREATE OR REPLACE VIEW Entity_BN_nodes 
       AS SELECT 
        Entity_BayesNets.pvid AS pvid,
        Entity_BayesNets.child AS node
    FROM
        Entity_BayesNets ORDER BY pvid;




CREATE table Entity_Complement_Edges as 
    SELECT distinct
        BN_nodes1.pvid AS pvid,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Entity_BN_nodes AS BN_nodes1,
        Entity_BN_nodes AS BN_nodes2
    WHERE
        BN_nodes1.pvid = BN_nodes2.pvid
            AND (NOT (EXISTS( SELECT 
                *
            FROM
                Entity_BayesNets
            WHERE
                (Entity_BayesNets.pvid = BN_nodes1.pvid)
                    AND (Entity_BayesNets.child = BN_nodes1.node)
                    AND (Entity_BayesNets.parent = BN_nodes2.node))));

ALTER TABLE `Entity_Complement_Edges` ADD PRIMARY KEY (`pvid`, `child`, `parent`) ;











CREATE table Path_Complement_Edges like Path_BayesNets; 

 























USE imdb_Gender_target_BN;


CREATE TABLE ADT_PVariables_Select_List AS 
SELECT 
    pvid, CONCAT('count(*)',' as "MULT"') AS Entries
FROM
    PVariables
UNION
SELECT 
    pvid,CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries 
FROM
    1Nodes
        NATURAL JOIN
    PVariables;

CREATE TABLE ADT_PVariables_From_List AS SELECT pvid, CONCAT('imdb_Gender_target.',TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;







create table PVariables_GroupBy_List as
SELECT pvid,
    1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables;

create table ADT_PVariables_GroupBy_List as
SELECT pvid,
    1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
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
    RNodes_1Nodes;



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
    RNodes_1Nodes;

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
RNodes_pvars.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = 	lattice_rel.parent)
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
RNodes_pvars.pvid = 1Nodes.pvid and  1Nodes.pvid not in (select pvid from RChain_pvars where RChain_pvars.rchain = 	lattice_rel.parent)
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
;


