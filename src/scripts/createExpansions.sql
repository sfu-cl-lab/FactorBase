-- createExpansions.sql

USE @database@_BN;
SET storage_engine=INNODB;
CREATE TABLE Expansions LIKE unielwin_setup.Expansions; 
INSERT INTO Expansions SELECT * FROM unielwin_setup.Expansions;