-- FunctorNode.sql
USE @database@_BN;
SET storage_engine=INNODB;

DROP TABLE IF EXISTS FNodes;
CREATE TABLE FNodes (
    `Fid` VARCHAR(199),
    `FunctorName` VARCHAR(64),
    `Type` VARCHAR(5),
    `main` INT(11),
    PRIMARY KEY (`Fid`)
);
INSERT INTO FNodes
SELECT 
    1nid AS Fid,
    COLUMN_NAME AS FunctorName,
    '1Node' AS Type,
    main
FROM
    1Nodes 
UNION SELECT 
    2nid AS Fid,
    COLUMN_NAME AS FunctorName,
    '2Node' AS Type,
    main
FROM
    2Nodes 
UNION SELECT 
    rnid AS FID,
    TABLE_NAME AS FunctorName,
    'Rnode' AS Type,
    main
FROM
    RNodes;
DROP TABLE IF EXISTS FNodes_pvars;
CREATE OR REPLACE VIEW FNodes_pvars AS SELECT FNodes.Fid, PVariables.pvid FROM
    FNodes,
    2Nodes,
    PVariables
WHERE
    FNodes.Type = '2Node'
        AND FNodes.Fid = 2Nodes.2nid
        AND PVariables.pvid = 2Nodes.pvid1 
UNION SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    2Nodes,
    PVariables
WHERE
    FNodes.Type = '2Node'
        AND FNodes.Fid = 2Nodes.2nid
        AND PVariables.pvid = 2Nodes.pvid2 
UNION SELECT 
    FNodes.Fid, PVariables.pvid
FROM
    FNodes,
    1Nodes,
    PVariables
WHERE
    FNodes.Type = '1Node'
        AND FNodes.Fid = 1Nodes.1nid
        AND PVariables.pvid = 1Nodes.pvid;