DROP SCHEMA IF EXISTS unielwin_BN; 
CREATE SCHEMA unielwin_BN;
DROP SCHEMA IF EXISTS unielwin_CT; 
CREATE SCHEMA unielwin_CT;
USE unielwin_BN;
SET storage_engine=INNODB;




CREATE OR REPLACE VIEW FunctorSet AS SELECT * FROM
    unielwin_setup.FunctorSet;

CREATE OR REPLACE VIEW 1Nodes AS SELECT N.1nid, N.COLUMN_NAME, N.pvid, N.main FROM
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

CREATE TABLE PVariables AS SELECT * FROM
    unielwin_setup.PVariables;





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





