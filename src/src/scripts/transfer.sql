DROP SCHEMA IF EXISTS @database@_BN; 
create schema @database@_BN;

DROP SCHEMA IF EXISTS @database@_CT; 
create schema @database@_CT;

USE @database@_BN;
SET storage_engine=INNODB;


/*copy tables from setup metadata database into learning database
/*copy subset as specified in setup  rather than full functor set */

create table Fnodes as select * from @database@_setup.FunctorSet; /*create subset of functor nodes */
create table 1Nodes as select * from @database@_setup.1Nodes N, Fnodes F where N.1nid = F.fid;
create table 2Nodes as select * from @database@_setup.2Nodes N, Fnodes F where N.2nid = F.Fid;
create table RNodes as select * from @database@_setup.RNodes N, Fnodes F where N.rnid = F.Fid;
create table FNodes_pvars select * from @database@_setup.FNodes_pvars FP where FP.fid in (select fid from Fnodes);
create table PVariables as select * from @database@_setup.PVariables P where P.pvid in (select pvid from FNodes_pvars);
/*copy only Pvariables that appear in functor subset */
create table Groundings like @database@_setup.Groundings; 
insert into Groundings select * from @database@_setup.Groundings;
/* the like construct should preserve foreign key pointers to Pvariables from the setup database. 
/* Want to force groundings to target Pvariables that appear in the functor subset.*/

/*the remaining tables are copied as they are */
/*they contain information about all functors and population variables, but it should work just as look ups for the restricted subsets.
/*otherwise need to make restricted versions of these as well */

create table EntityTables as select * from @database@_setup.EntityTables;
create table AttributeColumns as select * from @database@_setup.AttributeColumns;
create table TernaryRelations as select * from @database@_setup.TernaryRelations;
create table RelationTables as select * from @database@_setup.RelationTables;
create table NoPKeys as select * from  @database@_setup.NoPKeys;
create table ForeignKeyColumns as select * from  @database@_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  @database@_setup.ForeignKeys_pvars;
create table InputColumns as select * from  @database@_setup.InputColumns;
create table Attribute_Value as select * from  @database@_setup.Attribute_Value;


/* these tables are actually created later in metadata_3 */
/*
create table Path_BN_nodes as select * from  @database@_setup.Path_BN_nodes;
create table lattice_membership as select * from  @database@_setup.lattice_membership;
create table lattice_set as select * from  @database@_setup.lattice_set;
create table Path_Aux_Edges as select * from  @database@_setup.Path_Aux_Edges;
create table SchemaEdges as select * from  @database@_setup.SchemaEdges;
*/


