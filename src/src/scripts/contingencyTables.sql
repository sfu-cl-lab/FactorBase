-- contingencyTables.sql
USE @database@_BN;
SET storage_engine=INNODB;

-- The goal is to create join data tables that can be given as input to a Bayes net learner. 


CREATE TABLE 1Nodes_Select_List AS
    SELECT 
        1nid,
        CONCAT(1Nodes.pvid,
                '.',
                1Nodes.COLUMN_NAME,
                ' AS ',
                1nid) AS Entries
    FROM
        1Nodes,
        PVariables
    WHERE
        1Nodes.pvid = PVariables.pvid;

CREATE TABLE 1Nodes_From_List AS
    SELECT 
        1nid,
        CONCAT(PVariables.TABLE_NAME,
                ' AS ',
                PVariables.pvid) AS Entries
    FROM
        1Nodes,
        PVariables
    WHERE
        1Nodes.pvid = PVariables.pvid;

-- 2Nodes
CREATE TABLE 2Nodes_Select_List AS
    SELECT 
        2nid,
        CONCAT(RNodes.rnid,
                '.',
                2Nodes.COLUMN_NAME,
                ' AS ',
                2nid) AS Entries
    FROM
        2Nodes
            NATURAL JOIN
        RNodes;

CREATE TABLE 2Nodes_From_List AS
    SELECT 
        2nid,
        CONCAT(2Nodes.TABLE_NAME, ' AS ', RNodes.rnid) AS Entries
    FROM
        2Nodes
            NATURAL JOIN
        RNodes;

-- PVariables
-- use entity tables for main variables only (index = 0). 
-- Other entity tables have empty Bayes nets by the main functor constraint. 

CREATE TABLE PVariables_Select_List AS
    SELECT 
        pvid, CONCAT('count(*)', ' as "MULT"') AS Entries
    FROM
        PVariables 
    UNION SELECT 
        pvid,
        CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries
    FROM
        1Nodes
            NATURAL JOIN
        PVariables
    WHERE
        PVariables.index_number = 0;
        
CREATE OR REPLACE VIEW PVariables_From_List AS
    SELECT 
        pvid, CONCAT(TABLE_NAME, ' AS ', pvid) AS Entries
    FROM
        PVariables
    WHERE
        index_number = 0;




-- CREATING data join tables for each relationship functor node.
CREATE TABLE RNodes_pvars AS
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
        pvid1 = pvid
            AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
            AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME1
            AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME 
    UNION SELECT DISTINCT
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
            
CREATE OR REPLACE VIEW RNodes_1Nodes AS
    SELECT 
        rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid1 AS pvid
    FROM
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

CREATE OR REPLACE VIEW RNodes_2Nodes AS
    SELECT 
        RNodes.rnid, 2Nodes.2nid
    FROM
        2Nodes,
        RNodes
    WHERE
        2Nodes.TABLE_NAME = RNodes.TABLE_NAME;

CREATE TABLE RNodes_Select_List AS
    SELECT 
        rnid, CONCAT('count(*)', ' as "MULT"') AS Entries
    FROM
        RNodes 
    UNION SELECT DISTINCT
        rnid,
        CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries
    FROM
        RNodes_1Nodes 
    UNION DISTINCT SELECT 
        temp.rnid, temp.Entries
    FROM
        (SELECT DISTINCT
            rnid,
                CONCAT(rnid, '.', COLUMN_NAME, ' AS ', 2nid) AS Entries
        FROM
            2Nodes
        NATURAL JOIN RNodes
        ORDER BY RNodes.rnid , COLUMN_NAME) AS temp 
    UNION SELECT DISTINCT
        rnid, rnid AS Entries
    FROM
        RNodes;

CREATE TABLE RNodes_From_List AS
    SELECT DISTINCT
        rnid,
        CONCAT('@database@.', TABLE_NAME, ' AS ', pvid) AS Entries
    FROM
        RNodes_pvars 
    UNION DISTINCT SELECT DISTINCT
        rnid,
        CONCAT('@database@.', TABLE_NAME, ' AS ', rnid) AS Entries
    FROM
        RNodes 
    UNION DISTINCT SELECT DISTINCT
        rnid,
        CONCAT('(select "T" as ',
                rnid,
                ') as ',
                CONCAT('`temp_', REPLACE(rnid, '`', ''), '`')) AS Entries
    FROM
        RNodes;
/** we add a table that has a single column and single row contain "T" for "true", whose header is the rnid. This simulates the case where all the relationships are true.
We need to replace the apostrophes in rnid to make the rnid a valid name for the temporary table 
**/

CREATE TABLE RNodes_Where_List AS
    SELECT 
        rnid,
        CONCAT(rnid,
                '.',
                COLUMN_NAME,
                ' = ',
                pvid,
                '.',
                REFERENCED_COLUMN_NAME) AS Entries
    FROM
        RNodes_pvars;
/*    union
    select rnid, CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
           Groundings.id) AS Entries 
FROM
    RNodes_pvars natural join Groundings;
    */
    


CREATE TABLE RNodes_GroupBy_List AS
    SELECT DISTINCT
        rnid, 1nid AS Entries
    FROM
        RNodes_1Nodes 
    UNION DISTINCT SELECT DISTINCT
        rnid, 2nid AS Entries
    FROM
        2Nodes
            NATURAL JOIN
        RNodes 
    UNION DISTINCT SELECT 
        rnid, rnid
    FROM
        RNodes;
