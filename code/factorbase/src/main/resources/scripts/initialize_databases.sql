-- Initialize the databases required by FactorBase.

DROP SCHEMA IF EXISTS @database@_setup;
CREATE SCHEMA @database@_setup;

DROP SCHEMA IF EXISTS @database@_BN;
CREATE SCHEMA @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT;
CREATE SCHEMA @database@_CT;

DROP SCHEMA IF EXISTS @database@_global_counts;
CREATE SCHEMA @database@_global_counts;