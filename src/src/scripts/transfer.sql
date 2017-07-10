DROP SCHEMA IF EXISTS @database@_BN; 
CREATE SCHEMA @database@_BN;
DROP SCHEMA IF EXISTS @database@_CT; 
CREATE SCHEMA @database@_CT;
USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup metadata database into learning database
/*copy subset as specified in setup  rather than full functor set */

CREATE OR REPLACE VIEW FunctorSet AS SELECT * FROM
    unielwin_setup.FunctorSet;

CREATE OR REPLACE VIEW 1Nodes AS SELECT N.1nid, N.COLUMN_NAME, N.pvid, N.main FROM
    unielwin_setup.1Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.1nid = F.fid;

CREATE OR REPLACE VIEW 2Nodes AS SELECT N.2nid,
    N.COLUMN_NAME,
    N.pvid1,
    N.pvid2,
    N.TABLE_NAME,
    N.main FROM
    unielwin_setup.2Nodes N,
    unielwin_setup.FunctorSet F
WHERE
    N.2nid = F.Fid;
CREATE OR REPLACE VIEW RNodes AS SELECT N.orig_rnid,
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
CREATE TABLE PVariables AS SELECT * FROM
    unielwin_setup.PVariables;

/*copy only Pvariables that appear in functor subset */

/* the like construct should preserve foreign key pointers to Pvariables from the setup database. 
/* Want to force groundings to target Pvariables that appear in the functor subset.*/

CREATE OR REPLACE VIEW EntityTables AS SELECT * FROM
    unielwin_setup.EntityTables;
CREATE OR REPLACE VIEW AttributeColumns AS SELECT * FROM
    unielwin_setup.AttributeColumns;
CREATE OR REPLACE VIEW TernaryRelations AS SELECT * FROM
    unielwin_setup.TernaryRelations;
CREATE OR REPLACE VIEW RelationTables AS SELECT * FROM
    unielwin_setup.RelationTables;
CREATE OR REPLACE VIEW NoPKeys AS SELECT * FROM
    unielwin_setup.NoPKeys;
CREATE OR REPLACE VIEW ForeignKeyColumns AS SELECT * FROM
    unielwin_setup.ForeignKeyColumns;
CREATE OR REPLACE VIEW ForeignKeys_pvars AS SELECT * FROM
    unielwin_setup.ForeignKeys_pvars;
CREATE OR REPLACE VIEW InputColumns AS SELECT * FROM
    unielwin_setup.InputColumns;
CREATE OR REPLACE VIEW Attribute_Value AS SELECT * FROM
    unielwin_setup.Attribute_Value;



/* these tables are actually created later in metadata_3 */
/*
create table Path_BN_nodes as select * from  unielwin_setup.Path_BN_nodes;
create table lattice_membership as select * from  unielwin_setup.lattice_membership;
create table lattice_set as select * from  unielwin_setup.lattice_set;
create table Path_Aux_Edges as select * from  unielwin_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  unielwin_setup.SchemaEdges;
*/