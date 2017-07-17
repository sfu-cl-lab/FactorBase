/*adding covering index to speed up the query, however hash index does not support this technique, so replace it with default index, ie. B_Tree @ zqian May 22nd*/
USE @database@_BN;
SET storage_engine=INNODB;

/* Set up a table that contains all functor nodes of any arity, useful for Bayes net learning later. */
CREATE TABLE FNodes (   /*May 10th */
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


create table FNodes_pvars as 
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
    and PVariables.pvid = 1Nodes.pvid;