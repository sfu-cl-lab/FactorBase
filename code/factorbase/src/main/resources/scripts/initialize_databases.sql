-- Initialize the databases required by FactorBase.

/*M!100316 SET collation_server = 'latin1_swedish_ci';*/
SET collation_database = 'latin1_swedish_ci';

DROP SCHEMA IF EXISTS @database@_setup;
CREATE SCHEMA @database@_setup;

DROP SCHEMA IF EXISTS @database@_BN;
CREATE SCHEMA @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT;
CREATE SCHEMA @database@_CT;

DROP SCHEMA IF EXISTS @database@_global_counts;
CREATE SCHEMA @database@_global_counts;

DROP SCHEMA IF EXISTS @database@_CT_cache;
CREATE SCHEMA @database@_CT_cache;

ALTER DATABASE @database@_setup CHARACTER SET latin1 COLLATE  latin1_swedish_ci;
ALTER DATABASE @database@_BN CHARACTER SET latin1 COLLATE  latin1_swedish_ci;
ALTER DATABASE @database@_CT CHARACTER SET latin1 COLLATE  latin1_swedish_ci;
ALTER DATABASE @database@_global_counts CHARACTER SET latin1 COLLATE  latin1_swedish_ci;
ALTER DATABASE @database@_CT_cache CHARACTER SET latin1 COLLATE  latin1_swedish_ci;


