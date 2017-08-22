

DROP SCHEMA IF EXISTS unielwin_setup; 
create schema unielwin_setup;




USE unielwin_setup;
SET storage_engine=INNODB;



CREATE TABLE Schema_Key_Info AS SELECT TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    CONSTRAINT_NAME FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    (KEY_COLUMN_USAGE.TABLE_SCHEMA = 'unielwin')
ORDER BY TABLE_NAME;




CREATE TABLE Schema_Position_Info AS SELECT COLUMNS.TABLE_NAME,
    COLUMNS.COLUMN_NAME,
    COLUMNS.ORDINAL_POSITION FROM
    INFORMATION_SCHEMA.COLUMNS,
    INFORMATION_SCHEMA.TABLES
WHERE
    (COLUMNS.TABLE_SCHEMA = 'unielwin'
        AND TABLES.TABLE_SCHEMA = 'unielwin'
        AND TABLES.TABLE_NAME = COLUMNS.TABLE_NAME
        AND TABLES.TABLE_TYPE = 'BASE TABLE')
ORDER BY TABLE_NAME;



CREATE TABLE NoPKeys AS SELECT TABLE_NAME FROM
    Schema_Key_Info
WHERE
    TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            Schema_Key_Info
        WHERE
            CONSTRAINT_NAME LIKE 'PRIMARY');




CREATE table NumEntityColumns AS
    SELECT 
        TABLE_NAME, COUNT(DISTINCT COLUMN_NAME) num
    FROM
        Schema_Key_Info
    WHERE
        CONSTRAINT_NAME LIKE 'PRIMARY'
            OR REFERENCED_COLUMN_NAME IS NOT NULL
    GROUP BY TABLE_NAME;

CREATE TABLE TernaryRelations as SELECT TABLE_NAME FROM
    NumEntityColumns
WHERE
    num > 2;



CREATE TABLE KeyColumns AS SELECT * FROM
    (Schema_Key_Info
    NATURAL JOIN Schema_Position_Info)
WHERE
    TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            NoPKeys)
        AND TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            TernaryRelations);







CREATE TABLE AttributeColumns AS SELECT TABLE_NAME, COLUMN_NAME FROM
    Schema_Position_Info
WHERE
    (TABLE_NAME , COLUMN_NAME) NOT IN (SELECT 
            TABLE_NAME, COLUMN_NAME
        FROM
            KeyColumns)
        and TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            NoPKeys)
        and TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            TernaryRelations);

ALTER TABLE AttributeColumns ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME);




CREATE TABLE InputColumns AS SELECT * FROM
    KeyColumns
WHERE
    CONSTRAINT_NAME = 'PRIMARY'
ORDER BY TABLE_NAME;




CREATE TABLE ForeignKeyColumns AS SELECT * FROM
    KeyColumns
WHERE
    REFERENCED_COLUMN_NAME IS NOT NULL
ORDER BY TABLE_NAME;

ALTER TABLE ForeignKeyColumns ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME);









CREATE TABLE EntityTables AS SELECT distinct TABLE_NAME, COLUMN_NAME FROM
    KeyColumns T
WHERE
    1 = (SELECT 
            COUNT(COLUMN_NAME)
        FROM
            KeyColumns T2
        WHERE
            T.TABLE_NAME = T2.TABLE_NAME
                AND CONSTRAINT_NAME = 'PRIMARY');

ALTER TABLE EntityTables ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME);




CREATE TABLE SelfRelationships AS SELECT DISTINCT RTables1.TABLE_NAME AS TABLE_NAME,
    RTables1.REFERENCED_TABLE_NAME AS REFERENCED_TABLE_NAME,
    RTables1.REFERENCED_COLUMN_NAME AS REFERENCED_COLUMN_NAME FROM
    KeyColumns AS RTables1,
    KeyColumns AS RTables2
WHERE
    (RTables1.TABLE_NAME = RTables2.TABLE_NAME)
        AND (RTables1.REFERENCED_TABLE_NAME = RTables2.REFERENCED_TABLE_NAME)
        AND (RTables1.REFERENCED_COLUMN_NAME = RTables2.REFERENCED_COLUMN_NAME)
        AND (RTables1.ORDINAL_POSITION < RTables2.ORDINAL_POSITION);

ALTER TABLE SelfRelationships ADD PRIMARY KEY (TABLE_NAME);



CREATE TABLE Many_OneRelationships AS SELECT KeyColumns1.TABLE_NAME FROM
    KeyColumns AS KeyColumns1,
    KeyColumns AS KeyColumns2
WHERE
    (KeyColumns1.TABLE_NAME , KeyColumns1.COLUMN_NAME) IN (SELECT 
            TABLE_NAME, COLUMN_NAME
        FROM
            InputColumns)
        AND (KeyColumns2.TABLE_NAME , KeyColumns2.COLUMN_NAME) IN (SELECT 
            TABLE_NAME, COLUMN_NAME
        FROM
            ForeignKeyColumns)
        AND (KeyColumns2.TABLE_NAME , KeyColumns2.COLUMN_NAME) NOT IN (SELECT 
            TABLE_NAME, COLUMN_NAME
        FROM
            InputColumns);






CREATE TABLE PVariables (
  `pvid` varchar(100),
  `TABLE_NAME` varchar(100),
  `ID_COLUMN_NAME` varchar(100),
   `index_number` varchar(1), 
  PRIMARY KEY (`pvid`)
);

INSERT  INTO PVariables 
SELECT CONCAT(EntityTables.TABLE_NAME, '0') AS pvid,
    EntityTables.TABLE_NAME, EntityTables.COLUMN_NAME as ID_COLUMN_NAME,
    0 AS index_number FROM
    EntityTables 
UNION 
SELECT 
    CONCAT(EntityTables.TABLE_NAME, '1') AS pvid,
    EntityTables.TABLE_NAME, EntityTables.COLUMN_NAME as ID_COLUMN_NAME,
    1 AS index_number
FROM
    EntityTables,
    SelfRelationships
WHERE
    EntityTables.TABLE_NAME = SelfRelationships.REFERENCED_TABLE_NAME
AND EntityTables.COLUMN_NAME = SelfRelationships.REFERENCED_COLUMN_NAME ;
            

    








CREATE TABLE RelationTables AS SELECT DISTINCT ForeignKeyColumns.TABLE_NAME,
    ForeignKeyColumns.TABLE_NAME IN (SELECT 
            TABLE_NAME
        FROM
            SelfRelationships) AS SelfRelationship,
    ForeignKeyColumns.TABLE_NAME IN (SELECT 
            TABLE_NAME
        FROM
            Many_OneRelationships) AS Many_OneRelationship FROM
    ForeignKeyColumns;

ALTER TABLE RelationTables ADD PRIMARY KEY (TABLE_NAME);



CREATE TABLE 1Nodes AS SELECT CONCAT('`', COLUMN_NAME, '(', pvid, ')', '`') AS 1nid,
    COLUMN_NAME,
    pvid,
    index_number = 0 AS main FROM
    PVariables
        NATURAL JOIN
    AttributeColumns;



ALTER TABLE 1Nodes ADD PRIMARY KEY (1nid);
ALTER TABLE 1Nodes ADD UNIQUE(pvid,COLUMN_NAME);


CREATE TABLE ForeignKeys_pvars AS SELECT ForeignKeyColumns.TABLE_NAME,
    ForeignKeyColumns.REFERENCED_TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    pvid,
    index_number,
    ORDINAL_POSITION AS ARGUMENT_POSITION FROM
    ForeignKeyColumns,
    PVariables
WHERE
    PVariables.TABLE_NAME = REFERENCED_TABLE_NAME;

ALTER TABLE ForeignKeys_pvars ADD PRIMARY KEY (TABLE_NAME,pvid,ARGUMENT_POSITION);




CREATE table RNodes_MM_NotSelf AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars1.TABLE_NAME,
                '(',
                ForeignKeys_pvars1.pvid,
                ',',
                ForeignKeys_pvars2.pvid,
                ')',
                '`') AS rnid,
        ForeignKeys_pvars1.TABLE_NAME,
        ForeignKeys_pvars1.pvid AS pvid1,
        ForeignKeys_pvars2.pvid AS pvid2,
        ForeignKeys_pvars1.COLUMN_NAME AS COLUMN_NAME1,
        ForeignKeys_pvars2.COLUMN_NAME AS COLUMN_NAME2,
        (ForeignKeys_pvars1.index_number = 0
            AND ForeignKeys_pvars2.index_number = 0) AS main
    FROM
        ForeignKeys_pvars AS ForeignKeys_pvars1,
        ForeignKeys_pvars AS ForeignKeys_pvars2,
        RelationTables
    WHERE
        ForeignKeys_pvars1.TABLE_NAME = ForeignKeys_pvars2.TABLE_NAME
            AND RelationTables.TABLE_NAME = ForeignKeys_pvars1.TABLE_NAME
            AND ForeignKeys_pvars1.ARGUMENT_POSITION < ForeignKeys_pvars2.ARGUMENT_POSITION
            AND RelationTables.SelfRelationship = 0
            AND RelationTables.Many_OneRelationship = 0;




CREATE table RNodes_MM_Self AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars1.TABLE_NAME,
                '(',
                ForeignKeys_pvars1.pvid,
                ',',
                ForeignKeys_pvars2.pvid,
                ')',
                '`') AS rnid,
        ForeignKeys_pvars1.TABLE_NAME,
        ForeignKeys_pvars1.pvid AS pvid1,
        ForeignKeys_pvars2.pvid AS pvid2,
        ForeignKeys_pvars1.COLUMN_NAME AS COLUMN_NAME1,
        ForeignKeys_pvars2.COLUMN_NAME AS COLUMN_NAME2,
        (ForeignKeys_pvars1.index_number = 0
            AND ForeignKeys_pvars2.index_number = 1) AS main
    FROM
        ForeignKeys_pvars AS ForeignKeys_pvars1,
        ForeignKeys_pvars AS ForeignKeys_pvars2,
        RelationTables
    WHERE
        ForeignKeys_pvars1.TABLE_NAME = ForeignKeys_pvars2.TABLE_NAME
            AND RelationTables.TABLE_NAME = ForeignKeys_pvars1.TABLE_NAME
            AND ForeignKeys_pvars1.ARGUMENT_POSITION < ForeignKeys_pvars2.ARGUMENT_POSITION
            AND ForeignKeys_pvars1.index_number < ForeignKeys_pvars2.index_number
            AND RelationTables.SelfRelationship = 1
            AND RelationTables.Many_OneRelationship = 0;





CREATE table RNodes_MO_NotSelf AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars.REFERENCED_TABLE_NAME,
                '(',
                PVariables.pvid,
                ')=',
                ForeignKeys_pvars.pvid,
                '`') AS rnid,
        ForeignKeys_pvars.TABLE_NAME,
        PVariables.pvid AS pvid1,
        ForeignKeys_pvars.pvid AS pvid2,
        KeyColumns.COLUMN_NAME AS COLUMN_NAME1,
        ForeignKeys_pvars.COLUMN_NAME AS COLUMN_NAME2,
        (PVariables.index_number = 0
            AND ForeignKeys_pvars.index_number = 0) AS main
    FROM
        ForeignKeys_pvars,
        RelationTables,
        KeyColumns,
        PVariables
    WHERE
        RelationTables.TABLE_NAME = ForeignKeys_pvars.TABLE_NAME
            AND RelationTables.TABLE_NAME = PVariables.TABLE_NAME
            AND RelationTables.TABLE_NAME = KeyColumns.TABLE_NAME
            AND RelationTables.SelfRelationship = 0
            AND RelationTables.Many_OneRelationship = 1;




CREATE table RNodes_MO_Self AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars.REFERENCED_TABLE_NAME,
                '(',
                PVariables.pvid,
                ')=',
                ForeignKeys_pvars.pvid,
                '`') AS rnid,
        ForeignKeys_pvars.TABLE_NAME,
        PVariables.pvid AS pvid1,
        ForeignKeys_pvars.pvid AS pvid2,
        KeyColumns.COLUMN_NAME AS COLUMN_NAME1,
        ForeignKeys_pvars.COLUMN_NAME AS COLUMN_NAME2,
        (PVariables.index_number = 0
            AND ForeignKeys_pvars.index_number = 1) AS main
    FROM
        ForeignKeys_pvars,
        RelationTables,
        KeyColumns,
        PVariables
    WHERE
        RelationTables.TABLE_NAME = ForeignKeys_pvars.TABLE_NAME
            AND RelationTables.TABLE_NAME = PVariables.TABLE_NAME
            AND RelationTables.TABLE_NAME = KeyColumns.TABLE_NAME
            AND PVariables.index_number < ForeignKeys_pvars.index_number
            AND RelationTables.SelfRelationship = 1
            AND RelationTables.Many_OneRelationship = 1;


CREATE TABLE RNodes AS SELECT * FROM  
    RNodes_MM_NotSelf    
UNION SELECT            
    *                   
FROM
    RNodes_MM_Self 
UNION SELECT 
    *
FROM
    RNodes_MO_NotSelf 
UNION SELECT 
    *
FROM
    RNodes_MO_Self;



 

 
ALTER TABLE RNodes ADD PRIMARY KEY (TABLE_NAME, pvid1, pvid2);











CREATE TABLE 2Nodes AS SELECT CONCAT('`',
            COLUMN_NAME,
            '(',
            pvid1,
            ',',
            pvid2,
            ')',
            '`') AS 2nid,
    COLUMN_NAME,
    pvid1,
    pvid2,
    TABLE_NAME,
    main FROM
    RNodes
        NATURAL JOIN
    AttributeColumns;



    
ALTER TABLE 2Nodes ADD PRIMARY KEY (COLUMN_NAME,pvid1,pvid2); 

ALTER TABLE `2Nodes` ADD INDEX `index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ; 



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












CREATE TABLE `Expansions` (
  `pvid` varchar(40),
  PRIMARY KEY (`pvid`),
  FOREIGN KEY (pvid) REFERENCES PVariables(pvid)
);

CREATE TABLE Groundings (pvid varchar(40), id varchar(256), primary key (pvid, id), FOREIGN KEY (pvid) REFERENCES PVariables(pvid));


CREATE TABLE FunctorSet (   
  `Fid` varchar(199),
   PRIMARY KEY  (Fid), FOREIGN KEY (Fid) REFERENCES FNodes(Fid)
);


INSERT  INTO FunctorSet 
SELECT DISTINCT Fid from FNodes;

CREATE TABLE TargetNode (   
  `Fid` varchar(199),
   PRIMARY KEY  (Fid), FOREIGN KEY (Fid) REFERENCES FNodes(Fid)
);





create or replace view RNodes_2Nodes as select RNodes.rnid, 2Nodes.2nid from 2Nodes, RNodes where 2Nodes.TABLE_NAME = RNodes.TABLE_NAME; 



create or replace VIEW FNodes_pvars as 
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
    and PVariables.pvid = 1Nodes.pvid
UNION
SELECT DISTINCT rnid,
    pvid
FROM
    RNodes,
    PVariables
WHERE
    pvid1 = pvid
UNION 
SELECT DISTINCT
    rnid,
    pvid
FROM
    RNodes,
    PVariables
WHERE
    pvid2 = pvid;
    
    
    
    
 

CREATE or replace VIEW RNodes_pvars AS
SELECT DISTINCT rnid,
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


