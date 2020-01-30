/****************************************************
Analyze schema information to prepare for statistical analysis.
@database@ stands for a generic database. This is replaced with the name of the actual target database schema by the program that calls this sql script.
*/

SET storage_engine=INNODB; -- Allows adding foreign key constraints.


CREATE TABLE Schema_Key_Info AS SELECT TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    CONSTRAINT_NAME FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    (KEY_COLUMN_USAGE.TABLE_SCHEMA = '@database@')
ORDER BY TABLE_NAME;


/* find information about the argument ordinal position, so we know how to order the arguments.  */

CREATE TABLE Schema_Position_Info AS SELECT COLUMNS.TABLE_NAME,
    COLUMNS.COLUMN_NAME,
    COLUMNS.ORDINAL_POSITION FROM
    INFORMATION_SCHEMA.COLUMNS,
    INFORMATION_SCHEMA.TABLES
WHERE
    (COLUMNS.TABLE_SCHEMA = '@database@'
        AND TABLES.TABLE_SCHEMA = '@database@'
        AND TABLES.TABLE_NAME = COLUMNS.TABLE_NAME
        AND TABLES.TABLE_TYPE = 'BASE TABLE')
ORDER BY TABLE_NAME;

/* end of reading information from the database schema */

CREATE TABLE NoPKeys AS SELECT TABLE_NAME FROM
    Schema_Key_Info
WHERE
    TABLE_NAME NOT IN (SELECT 
            TABLE_NAME
        FROM
            Schema_Key_Info
        WHERE
            CONSTRAINT_NAME LIKE 'PRIMARY');

/* WISHLIST: write trigger that warns when something is inserted here.
Row-based as proposed by Tim */

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

/* WISHLIST: write trigger that warns when something is inserted here.
Row-based as proposed by Tim */

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


/*ALTER TABLE KeyColumns ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME);  May 1, should be commented before running the new BayesBayes.java*/
/* Ali create a warning for this problem on May 3rd */

/* natural join adds information about ordinal position so we can tell which argument (column) comes first */

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


/* WISHLIST: add triggers to inform the user about omissions like this.
change to row-based.

create trigger
after insert on KeyColumns
when exists (select * from TablesWithoutPKeys)
issue message "some tables have been ommitted from analysis because they lack primary keys. Please see view TablesWithoutPKeys.")

create trigger
after insert on KeyColumns
when exists (SELECT 
            TABLE_NAME
        FROM
            NumEntityColumns
        WHERE
            num > 2)
issue message "some tables have been ommitted from analysis because they represent ternary relationships. Please see view NumEntityColumns.")
*/

CREATE TABLE InputColumns AS SELECT * FROM
    KeyColumns
WHERE
    CONSTRAINT_NAME = 'PRIMARY'
ORDER BY TABLE_NAME;

/*ALTER TABLE InputColumns ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME); May 1, should be commented before running the new BayesBayes.java*/
/* Ali create a warning for this problem on May 3rd */

CREATE TABLE ForeignKeyColumns AS SELECT * FROM
    KeyColumns
WHERE
    REFERENCED_COLUMN_NAME IS NOT NULL
ORDER BY TABLE_NAME;

ALTER TABLE ForeignKeyColumns ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME);




/*********************************************************************************
Find the possible values of attributes that appear in the data.
This works but seems to slow down the script a lot. Why? Could comment out if too slow.
ALIBZ: CREATE AND CALL SP FROM JAVA
*/


/*****************************************************
Now start setting up the elements of Bayes nets. 
We begin with the populations or entities. These have only one primary key.
******/

CREATE TABLE EntityTables AS SELECT distinct TABLE_NAME, COLUMN_NAME FROM
    KeyColumns T
WHERE
    CONSTRAINT_NAME = 'PRIMARY'
AND
    1 = (SELECT 
            COUNT(COLUMN_NAME)
        FROM
            KeyColumns T2
        WHERE
            T.TABLE_NAME = T2.TABLE_NAME
                AND CONSTRAINT_NAME = 'PRIMARY');

ALTER TABLE EntityTables ADD PRIMARY KEY (TABLE_NAME,COLUMN_NAME);


/* next, look for SelfRelationships, where two different foreign key columns refer to the same table. */

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

/* Second, find Many-one relationships, where we have a foreign key that is not a primary key (i.e., the foreign key entry is a function of other columns) */

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




/*********************************************************************
Now set up tables recording information about populations (entities) and link types (relations).
First, set up table that represent entity types (sets of individuals)/
We extract table name and the name of the primary key column. 
If an entity type is involved in a self relationship, then we make three "copies" of the population variable,
so that we can represent transitivity.
*/

CREATE TABLE PVariables (
    pvid VARCHAR(100),
    TABLE_NAME VARCHAR(100),
    ID_COLUMN_NAME VARCHAR(100),
    index_number CHAR(1),
    PRIMARY KEY (pvid)
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
            
/*zqian,Oct-02-13, reduce copies from 3 to 2*/
/*
UNION  
SELECT 
    CONCAT(EntityTables.TABLE_NAME, '2') AS pvid,
    EntityTables.TABLE_NAME,
    2 AS index_number
FROM
    EntityTables,
    SelfRelationships
WHERE
    EntityTables.TABLE_NAME = SelfRelationships.REFERENCED_TABLE_NAME
        AND EntityTables.COLUMN_NAME = SelfRelationships.REFERENCED_COLUMN_NAME;
  */    




/* Next, we work on relationship tables. There are two kinds: link tables that come from binary relationships, 
and tables that come from self-relationships. */



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

/************************
Now we start working on defining functor random variables, which eventually become the nodes in the Bayes nets.
First, unary functors apply to entities defined by Population variables.
*/

CREATE TABLE 1Nodes AS
    SELECT
        CONCAT(
            COLUMN_NAME,
            '(',
            pvid,
            ')'
        ) AS 1nid,
        COLUMN_NAME,
        pvid,
        index_number = 0 AS main
    FROM
        PVariables

    NATURAL JOIN

    AttributeColumns;
/* natural join on table name */

ALTER TABLE 1Nodes ADD PRIMARY KEY (1nid);
ALTER TABLE 1Nodes ADD UNIQUE(pvid,COLUMN_NAME);
/* for each population variable, there ought to be at most one function returning a given attribute for the population */

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

/* The table lists which relationship tables can have which population variables in which argument place. 
this view is just a temporary table for creating the relationship nodes. We could drop it after use. 
Materializing it speeds up processing. */

CREATE table RNodes_MM_NotSelf AS
    SELECT 
        CONCAT(
            ForeignKeys_pvars1.TABLE_NAME,
            '(',
            ForeignKeys_pvars1.pvid,
            ',',
            ForeignKeys_pvars2.pvid,
            ')'
        ) AS rnid,
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

/* second case: many-many relationship, and self-relationship. One difference is that we have a main variable if and only if the first argument is a main population variable, and the second is the second variable. Also, we consider only
functor nodes whose first argument has a lower index than the second. */

CREATE table RNodes_MM_Self AS
    SELECT 
        CONCAT(
            ForeignKeys_pvars1.TABLE_NAME,
            '(',
            ForeignKeys_pvars1.pvid,
            ',',
            ForeignKeys_pvars2.pvid,
            ')'
        ) AS rnid,
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

/* third case: many-one, not a self-relationship. Now we need to include the primary key as an argument. 
Also, we switch to functional notation for legibility. */
/* OS August 15. Why does this refer to Referenced_Table_Name, n ot Table_Name? */
CREATE table RNodes_MO_NotSelf AS
    SELECT 
        CONCAT(
            ForeignKeys_pvars.REFERENCED_TABLE_NAME,
            '(',
            PVariables.pvid,
            ') = ',
            ForeignKeys_pvars.pvid
        ) AS rnid,
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
            AND RelationTables.Many_OneRelationship = 1
            AND KeyColumns.CONSTRAINT_NAME = 'PRIMARY';

/*fourth case: many-one, self-relationship */

CREATE table RNodes_MO_Self AS
    SELECT 
        CONCAT(
            ForeignKeys_pvars.REFERENCED_TABLE_NAME,
            '(',
            PVariables.pvid,
            ') = ',
            ForeignKeys_pvars.pvid
        ) AS rnid,
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


CREATE TABLE RNodes AS SELECT * FROM  /*@ zqian May 22nd*/
    RNodes_MM_NotSelf    /*when we generate the RNodes, we replace rnid with orig_rnid, and replace short_rnid with rnid,*/
UNION SELECT            /* and we do not need to modify the latter code when it refering the RNodes/rnid */
    *                   /* so that we minimize the modification of java code and scripts*/
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


/* ALTER TABLE RNodes ADD PRIMARY KEY (orig_rnid);
 * OS August 15 can exceed byte limit. Maybe better use table name, pvid, pvid as primary key
 * //zqian, max key length limitation "The maximum column size is 767 bytes", 
enable "innodb_large_prefix" to allow index key prefixes longer than 767 bytes (up to 3072 bytes).
 Oct 17, 2013 
 */
 /* ALTER TABLE `RNodes` ADD INDEX `Index`  (`pvid1` ASC, `pvid2` ASC, `TABLE_NAME` ASC) ;/*July 17 vidhij--moved from metadata_2*/

 
ALTER TABLE RNodes ADD PRIMARY KEY (TABLE_NAME, pvid1, pvid2);

/* this key exceeds maximum byte length. OS august 17, 2017. Too bad, we could definitely use an index */
/*ALTER TABLE `RNodes` ADD UNIQUE INDEX `rnid_UNIQUE` (`rnid` ASC) ; */

/*OS August 16, 2017. Get rid of that virtual rnid business */
/*ALTER TABLE `RNodes` ADD COLUMN `rnid` VARCHAR(10) NULL , ADD UNIQUE INDEX `rnid_UNIQUE` (`rnid` ASC) ; */
/*May 16th, for shorter name of Rchain*/


/* Make tables for binary functor nodes that record attributes of links. 
By default, all ***nonkey columns*** of a relation table are possible attribute functors, with the appropriate population ids.
*/

SET @count = 96;
CREATE TABLE LatticeRNodes AS
    SELECT
        rnid AS orig_rnid,
        CHAR(@count := @count + 1) AS short_rnid
    FROM
        RNodes;


CREATE TABLE 2Nodes AS
    SELECT
        CONCAT(
            COLUMN_NAME,
            '(',
            pvid1,
            ',',
            pvid2,
            ')'
        ) AS 2nid,
        COLUMN_NAME,
        pvid1,
        pvid2,
        TABLE_NAME,
        main
    FROM
        RNodes

    NATURAL JOIN

    AttributeColumns;

/* ALTER TABLE 2Nodes ADD PRIMARY KEY (2nid); */
/* violates key length restriction. OS August 15 */
ALTER TABLE 2Nodes ADD PRIMARY KEY (COLUMN_NAME,pvid1,pvid2); 

ALTER TABLE 2Nodes ADD INDEX `index` (pvid1 ASC, pvid2 ASC, TABLE_NAME ASC); /* July 17 vidhij -- moved from metadata_2 */

/* Set up a table that contains all functor nodes of any arity. summarizes all the work we've done. CAn also be used for foreign key constraints */

CREATE TABLE FNodes (
    Fid VARCHAR(199),
    FunctorName VARCHAR(64),
    Type VARCHAR(5),
    main INT(11),
    PRIMARY KEY (Fid)
);


/******* make comprehensive table for all functor nodes *****/

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

/*

CREATE TABLE IF NOT EXISTS lattice_membership (
    name VARCHAR(256),
    member VARCHAR(256),
    PRIMARY KEY (name , member)
);
*/
/* Lists for each relationship chain in the lattice, the singleton relationship functors contained in the chain.
TODO: check with Ali if this description is correct. */
/*
CREATE TABLE IF NOT EXISTS lattice_rel (
    parent VARCHAR(256),
    child VARCHAR(256),
    PRIMARY KEY (parent , child)
);*/
/* Lists for each relationship chain in the lattice, the singleton relationship functors contained in the chain, its immediate predecessor (parent). */
/*
CREATE TABLE IF NOT EXISTS lattice_set (
    name VARCHAR(256),
    length INT(11),
    PRIMARY KEY (name , length)
);*/
/* Lists each relationship chain in the lattice and its length. */

/*************************************************************************/
/* Now add tables that support user input: counting for contingency tables, classification, outlier detection */
/**********************************************************************/

CREATE TABLE Expansions (
    pvid VARCHAR(40),
    PRIMARY KEY (pvid),
    FOREIGN KEY (pvid) REFERENCES PVariables(pvid)
);

CREATE TABLE Groundings (pvid varchar(40), id varchar(256), primary key (pvid, id), FOREIGN KEY (pvid) REFERENCES PVariables(pvid));


CREATE TABLE FunctorSet (
    Fid VARCHAR(199),
    PRIMARY KEY (Fid), FOREIGN KEY (Fid) REFERENCES FNodes(Fid)
) ENGINE = MEMORY;

/* By default, FunctorSet contains all Fnodes */
INSERT  INTO FunctorSet 
SELECT DISTINCT Fid from FNodes;

CREATE TABLE TargetNode (
    Fid VARCHAR(199),
    PRIMARY KEY (Fid), FOREIGN KEY (Fid) REFERENCES FNodes(Fid)
);

/**************************************************************
/* Adding more views for more metadata. useful for learning later */
/*********************************************************************/

/** now link each rnode 2node, i.e. each attribute of the relationship to the associated 2nodes **/
CREATE TABLE RNodes_2Nodes AS
    SELECT
        RNodes.rnid,
        2Nodes.2nid,
        2Nodes.main
    FROM
        2Nodes,
        RNodes
    WHERE
        2Nodes.TABLE_NAME = RNodes.TABLE_NAME;


/*** for each functor node, record which population variables appear in it ***/

CREATE TABLE FNodes_pvars AS
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
    


ALTER TABLE FNodes_pvars
    ADD INDEX (Fid);


 /*** for each relationship node, record which population variables appear in it. 
Plus metadata about those variable, e.g. the name of the id column associated with them.  (August 17, 2017) This seems inelegant.
*/

CREATE TABLE RNodes_pvars AS
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

