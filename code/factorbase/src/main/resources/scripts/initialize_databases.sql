-- Initialize the databases required by FactorBase.

DROP SCHEMA IF EXISTS @database@_setup;
CREATE SCHEMA @database@_setup;

DROP SCHEMA IF EXISTS @database@_BN;
CREATE SCHEMA @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT;
CREATE SCHEMA @database@_CT;