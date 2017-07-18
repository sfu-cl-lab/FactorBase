
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
    RNodes
    
    union
    select distinct rnid, rnid as Fid, main from RNodes;


CREATE TABLE  Entity_BayesNets (
    pvid VARCHAR(65) NOT NULL,
    child VARCHAR(131) NOT NULL,
    parent VARCHAR(131) NOT NULL,
    PRIMARY KEY (pvid , child , parent)
);





CREATE TABLE Path_BayesNets (
    Rchain VARCHAR(255) NOT NULL,     child VARCHAR(197) NOT NULL,
    parent VARCHAR(197) NOT NULL,
    PRIMARY KEY (Rchain , child , parent)
);

CREATE TABLE NewLearnedEdges LIKE Path_BayesNets;




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

 





















