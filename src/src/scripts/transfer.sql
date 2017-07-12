DROP SCHEMA IF EXISTS @database@_BN; 
CREATE SCHEMA @database@_BN;

USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup metadata database into learning database
/*copy subset as specified in setup  rather than full functor set */

CREATE TABLE FunctorSet AS SELECT * FROM
    unielwin_setup.FunctorSet;

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
CREATE TABLE RNodes AS SELECT N.orig_rnid,
    N.TABLE_NAME,
    N.pvid1,
    N.pvid2,
    N.COLUMN_NAME1,
    N.COLUMN_NAME2,
    N.rnid,
    N.main FROM
    unielwin_setup.RNodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.orig_rnid = F.Fid;
/* better:*/
CREATE OR REPLACE VIEW PVariables AS SELECT * FROM
    unielwin_setup.PVariables;

/*copy only Pvariables that appear in functor subset */

/* the like construct should preserve foreign key pointers to Pvariables from the setup database. 
/* Want to force groundings to target Pvariables that appear in the functor subset.*/

CREATE TABLE EntityTables AS SELECT * FROM
    unielwin_setup.EntityTables;
CREATE TABLE AttributeColumns AS SELECT * FROM
    unielwin_setup.AttributeColumns;
CREATE TABLE TernaryRelations AS SELECT * FROM
    unielwin_setup.TernaryRelations;
CREATE TABLE RelationTables AS SELECT * FROM
    unielwin_setup.RelationTables;
CREATE TABLE NoPKeys AS SELECT * FROM
    unielwin_setup.NoPKeys;
CREATE TABLE ForeignKeyColumns AS SELECT * FROM
    unielwin_setup.ForeignKeyColumns;
CREATE TABLE ForeignKeys_pvars AS SELECT * FROM
    unielwin_setup.ForeignKeys_pvars;
CREATE TABLE InputColumns AS SELECT * FROM
    unielwin_setup.InputColumns;
CREATE TABLE Attribute_Value AS SELECT * FROM
    unielwin_setup.Attribute_Value;



/* these tables are actually created later in metadata_3 */
/*
create table Path_BN_nodes as select * from  unielwin_setup.Path_BN_nodes;
create table lattice_membership as select * from  unielwin_setup.lattice_membership;
create table lattice_set as select * from  unielwin_setup.lattice_set;
create table Path_Aux_Edges as select * from  unielwin_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  unielwin_setup.SchemaEdges;
*/