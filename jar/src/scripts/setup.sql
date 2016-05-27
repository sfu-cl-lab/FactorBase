/****************************************************
Analyze schema information to prepare for statistical analysis.
@database@ stands for a generic database. This is replaced with the name of the actual target database schema by the program that calls this sql script.
*/

DROP SCHEMA IF EXISTS @database@_setup; 
create schema @database@_setup;

create schema if not exists @database@_BN;
create schema if not exists @database@_CT;

USE @database@_setup;
SET storage_engine=INNODB;
/* allows adding foreign key constraints */


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

/*CREATE OR REPLACE VIEW NumEntityColumns AS */
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

CREATE TABLE PVariables AS SELECT CONCAT(EntityTables.TABLE_NAME, '0') AS pvid,
    EntityTables.TABLE_NAME,
    0 AS index_number FROM
    EntityTables 
UNION 
SELECT 
    CONCAT(EntityTables.TABLE_NAME, '1') AS pvid,
    EntityTables.TABLE_NAME,
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
ALTER TABLE PVariables ADD PRIMARY KEY (pvid);




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

CREATE TABLE 1Nodes AS SELECT CONCAT('`', COLUMN_NAME, '(', pvid, ')', '`') AS 1nid,
    COLUMN_NAME,
    pvid,
    index_number = 0 AS main FROM
    PVariables
        NATURAL JOIN
    AttributeColumns;
/* natural join on table name */
/* Unfortunately MYSQL doesn't seem to like having parentheses in field names, so we have to escape the functor Ids with single quotes */

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

/*CREATE OR REPLACE VIEW RNodes_MM_NotSelf AS*/
CREATE table RNodes_MM_NotSelf AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars1.TABLE_NAME,
                '(',
                ForeignKeys_pvars1.pvid,
                ',',
                ForeignKeys_pvars2.pvid,
                ')',
                '`') AS orig_rnid,
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

/*CREATE OR REPLACE VIEW RNodes_MM_Self AS*/
CREATE table RNodes_MM_Self AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars1.TABLE_NAME,
                '(',
                ForeignKeys_pvars1.pvid,
                ',',
                ForeignKeys_pvars2.pvid,
                ')',
                '`') AS orig_rnid,
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
/*CREATE OR REPLACE VIEW RNodes_MO_NotSelf AS*/
CREATE table RNodes_MO_NotSelf AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars.REFERENCED_TABLE_NAME,
                '(',
                PVariables.pvid,
                ')=',
                ForeignKeys_pvars.pvid,
                '`') AS orig_rnid,
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

/*fourth case: many-one, self-relationship */

/*CREATE OR REPLACE VIEW RNodes_MO_Self AS*/
CREATE table RNodes_MO_Self AS
    SELECT 
        CONCAT('`',
                ForeignKeys_pvars.REFERENCED_TABLE_NAME,
                '(',
                PVariables.pvid,
                ')=',
                ForeignKeys_pvars.pvid,
                '`') AS orig_rnid,
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

ALTER TABLE RNodes ADD PRIMARY KEY (orig_rnid);

ALTER TABLE `RNodes` ADD COLUMN `rnid` VARCHAR(10) NULL , ADD UNIQUE INDEX `rnid_UNIQUE` (`rnid` ASC) ; 
/*May 16th, for shorter name of Rchain*/


/* Make tables for binary functor nodes that record attributes of links. 
By default, all ***nonkey columns*** of a relation table are possible attribute functors, with the appropriate population ids.
*/

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

ALTER TABLE 2Nodes ADD PRIMARY KEY (2nid);


/*CREATE TABLE Groundings (pvid varchar(40) not null, id varchar(256) not null, primary key (pvid, id));*/


/*Added by zqian  Apr. 4th 2013
Column, index, and stored routine names are not case sensitive on any platform, nor are column aliases. Trigger names are case sensitive, which differs from standard SQL. */


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



