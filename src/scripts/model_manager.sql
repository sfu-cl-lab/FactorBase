USE unielwin_BN;
SET storage_engine=INNODB;
/******************************************************
find columns associated with Rnodes. These should be the same as used in group by clauses except that you also use the main auxilliary information.
TODO: should reconcile Rnodes_BN_NOdes and Path_BN_Nodes with metaqueries at some point
****/
/*CREATE TABLE RNodes_BN_Nodes AS 
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
    /*OS: next add the rnode as a functor node for itself Oct 13, 2016; */
  /*  union
    select distinct rnid, rnid as Fid, main from RNodes;
    */

CREATE TABLE RNodes_BN_Nodes AS select distinct rnid, 1nid as Fid, N.main from RNodes_pvars R, PVariables P, `1Nodes` N where R.pvid = P.pvid and R.pvid = N.pvid
UNION DISTINCT
select distinct rnid, 2nid as Fid, main from RNodes_2Nodes
UNION DISTINCT
select distinct rnid, rnid as Fid, main from RNodes;


CREATE TABLE  Entity_BayesNets (
    pvid VARCHAR(65) NOT NULL,
    child VARCHAR(131) NOT NULL,
    parent VARCHAR(131) NOT NULL,
    PRIMARY KEY (pvid , child , parent)
);

/******* create Tables to store Bayes nets in Lattice Chains  ****/

/* //zqian, max key length limitation "The maximum column size is 767 bytes", 
enable "innodb_large_prefix" to allow index key prefixes longer than 767 bytes (up to 3072 bytes).
 Oct 17, 2013    */

CREATE TABLE Path_BayesNets (
    Rchain VARCHAR(255) NOT NULL,     child VARCHAR(197) NOT NULL,
    parent VARCHAR(197) NOT NULL,
    PRIMARY KEY (Rchain , child , parent)
);

create or replace view Final_Path_BayesNets as select * from Path_BayesNets where character_length(Rchain) = (select max(character_length(Rchain)) from Path_BayesNets);

/* parepare output view with longest rchain only */

CREATE TABLE NewLearnedEdges LIKE Path_BayesNets;

/****************
Propagate BNnodes to rchains
*****************/

/*CREATE OR REPLACE VIEW Path_BN_nodes AS*/
ALTER TABLE `RNodes_BN_Nodes` ADD INDEX `Index_rnid`  (`rnid` ASC) ;/* May 10th*/

CREATE TABLE Path_BN_nodes AS 
SELECT DISTINCT lattice_membership.name AS Rchain, Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.orig_rnid
    ORDER BY lattice_membership.name;

ALTER TABLE Path_BN_nodes ADD INDEX `HashIndex`  (`Rchain`,`node`); /* May 7*/

/******************
 * Create tables that allow us to represent background knowledge. 
 */

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
          /* OS: we don't remember why we had this union! Nov 4 
union distinct             
            
   SELECT 
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2,
        FNodes
    WHERE
        BN_nodes1.Rchain = BN_nodes2.Rchain
            AND FNodes.Fid = BN_nodes2.node
            AND FNodes.main = 0;
/*zqian Oct 20, 2016*/

/* the next primary key makes an index that's too long. innodb_enable_large_prefix would fix that. But will be deprecated. 
For now we comment out, may slow down learning. */

/*ALTER TABLE Path_Aux_Edges99 ADD PRIMARY KEY (`Rchain`, `child`, `parent`);*/ /* May 10th*/ 



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
    lattice_membership.orig_rnid = RNodes.rnid
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

CREATE TABLE  Path_Required_Edges like Path_BayesNets;/*May 10th*/
insert ignore into Path_Required_Edges select distinct *    from        Knowledge_Required_Edges;
/*union distinct  */
insert ignore into Path_Required_Edges select  distinct *    from        SchemaEdges;

   
CREATE TABLE Path_Forbidden_Edges like Path_BayesNets;/*May 10th*/
insert ignore into Path_Forbidden_Edges select     distinct *    from        Knowledge_Forbidden_Edges ;
/*UNION  distinct */
insert ignore into Path_Forbidden_Edges   SELECT    distinct     *    FROM        Path_Aux_Edges ;


CREATE TABLE  LearnedEdges like Path_BayesNets;
CREATE TABLE  ContextEdges like Path_BayesNets;
CREATE TABLE  InheritedEdges like Path_BayesNets;



/******************************************
Create Views to enforce constraints as knowledge, in the sense of Tetrad: edges that are required and forbidden.action


/******************
Required Edges: The required edges for the Bayes nets are 1) inherited from the entity tables for singleton relationship sets, and 2) inherited from the subsets for relationships of size > 1. ???
This view is updated every time Bayes net learning is performed.
*/

/*create or replace view InheritedEdges as*/
/*create table InheritedEdges as
    select distinct
        lattice_rel.child AS Rchain,
        Path_BayesNets.child AS child,
        Path_BayesNets.parent AS parent
    FROM
        Path_BayesNets,
        lattice_rel
    WHERE
        lattice_rel.parent = Path_BayesNets.Rchain
            AND Path_BayesNets.parent <> ''
    ORDER BY Rchain;

*/


/*CREATE OR REPLACE VIEW Path_Required_Edges AS*/
/*CREATE table Path_Required_Edges AS
    SELECT DISTINCT
        RNodes_pvars.rnid AS Rchain,
        Entity_BayesNets.child AS child,
        Entity_BayesNets.parent AS parent
    FROM
        (RNodes_pvars, Entity_BayesNets)
    WHERE
        (RNodes_pvars.pvid = Entity_BayesNets.pvid
            AND Entity_BayesNets.parent <> '') 
    UNION SELECT 
        Rchain, child, parent
    from
        InheritedEdges
    where
        (Rchain , parent, child) NOT IN (select 
                *
            from
                InheritedEdges) 
    union select 
        *
    from
        Knowledge_Required_Edges
    union distinct 
    select * from SchemaEdges;
*/
/* Extendibility Note: Could allow a user or program to create a table with its required forbidden edges, simply union those into this construct */

CREATE OR REPLACE VIEW Entity_BN_nodes /*CREATE table Entity_BN_nodes */
       AS SELECT 
        Entity_BayesNets.pvid AS pvid,
        Entity_BayesNets.child AS node
    FROM
        Entity_BayesNets ORDER BY pvid;

/* Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net. Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around */

/*CREATE OR REPLACE VIEW Entity_Complement_Edges AS*/
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

ALTER TABLE `Entity_Complement_Edges` ADD PRIMARY KEY (`pvid`, `child`, `parent`) ;/*May 10th*/
/*ALTER TABLE Entity_Complement_Edges ADD INDEX `HashIn`  (`pvid`,`child`,`parent`);*/
/* Now we can find the complement edges as the pairs of potential nodes that do not appear in the learned Bayes net. Unfortunately MySQL doesn't support set difference directly, so we use EXISTS to work around */




/*CREATE OR REPLACE VIEW Path_BN_nodes AS*/
/*CREATE table Path_BN_nodes AS
    SELECT 
        lattice_membership.name AS Rchain, Fid AS node
    FROM
        lattice_membership,
        RNodes_BN_Nodes
    WHERE
        RNodes_BN_Nodes.rnid = lattice_membership.member
    ORDER BY lattice_membership.name;
    */
/* for each singleton relationship, find the associated functor ids. Union these for each lattice point that contains the singleton. Including the singleton itself. */

/*CREATE OR REPLACE VIEW Path_Complement_Edges AS*/
CREATE table Path_Complement_Edges like Path_BayesNets; /*May 10th*/
/*SELECT 
        BN_nodes1.Rchain AS Rchain,
        BN_nodes1.node AS child,
        BN_nodes2.node AS parent
    FROM
        Path_BN_nodes AS BN_nodes1,
        Path_BN_nodes AS BN_nodes2
    WHERE
        ((BN_nodes1.Rchain = BN_nodes2.Rchain)
            AND (NOT (EXISTS( SELECT 
                *
            FROM
                Path_BayesNets
            WHERE
                ((Path_BayesNets.Rchain = BN_nodes1.Rchain)
                    AND (Path_BayesNets.child = BN_nodes1.node)
                    AND (Path_BayesNets.parent = BN_nodes2.node))))));

*/
/*ALTER TABLE Path_Complement_Edges ADD INDEX `HashIndex`  (`Rchain`,`child`,`parent`);*/ /* May 7*/
/*ALTER TABLE Path_Complement_Edges ADD INDEX `HashIn`  (`child`,`parent`); *//* May 9*/

/********************************************
Dealing with the Main Functor Constraint. The auxilliary functor nodes are those that are not main functor nodes. The aux edges are the ones that point to an auxilliary node. 
Our approach is this: for each node in the relationship lattice, find the nodes in its associated Bayes net. If such a node is an auxilliary node, any edge pointing into this node is forbidden.
This builds a fairly large view. We could be more efficient in terms of storage by computing this dynamically as we learn the Bayes nets going up the lattice.
*********************************************/





/****************************
Finally, the forbidden edges for the Bayes nets are 1) inherited from the entity tables for singleton relationship sets, and 2) inherited from the subsets for relationships of size > 1
, and 3) the edges pointing into the main edges. 
Assumes that lattice_rel gives the subsets that are one size smaller.
*/

/*CREATE OR REPLACE VIEW Path_Forbidden_Edges AS*/
/*CREATE table Path_Forbidden_Edges AS
    SELECT DISTINCT
        RNodes_pvars.rnid AS Rchain,
        Entity_Complement_Edges.child AS child,
        Entity_Complement_Edges.parent AS parent
    FROM
        (RNodes_pvars, Entity_Complement_Edges)
    WHERE
        (RNodes_pvars.pvid = Entity_Complement_Edges.pvid) 
    UNION SELECT DISTINCT
        lattice_rel.child AS Rchain,
        Path_Complement_Edges.child AS child,
        Path_Complement_Edges.parent AS parent
    FROM
        Path_Complement_Edges,
        lattice_rel
    WHERE
        lattice_rel.parent = Path_Complement_Edges.Rchain
            AND Path_Complement_Edges.parent <> ''
            and (lattice_rel.child , Path_Complement_Edges.child,
            Path_Complement_Edges.parent) not in (select 
                *
            from
                Path_Required_Edges) 
    UNION SELECT 
        *
    FROM
        Path_Aux_Edges 
    UNION SELECT 
        *
    FROM
        Knowledge_Forbidden_Edges
;
*/

/* Extendibility Note: Could allow a user or program to create a table with 
its forbidden edges, simply union those into this construct */


/*create or replace view LearnedEdges as*/
/*create table LearnedEdges as
    select 
        Path_BayesNets.Rchain,
        Path_BayesNets.child,
        Path_BayesNets.parent
    from
        Path_BayesNets
    where
        Path_BayesNets.parent <> ''
            and (Path_BayesNets.Rchain , Path_BayesNets.child,
            Path_BayesNets.parent) not in (select 
                *
            from
                Path_Required_Edges);
*/
/*create or replace view ContextEdges as*/
/*create table ContextEdges as
    select distinct
        LearnedEdges.Rchain as Rchain,
        LearnedEdges.child as child,
        lattice_membership.member as parent
    from
        LearnedEdges,
        lattice_membership
    where
        LearnedEdges.Rchain = lattice_membership.name;
*/

