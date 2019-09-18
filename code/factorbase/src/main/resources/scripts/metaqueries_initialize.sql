/* Create the table necessary for storing metaquery information. */

SET storage_engine=INNODB;

CREATE TABLE MetaQueries (
    Lattice_Point varchar(199), /* e.g. pvid, rchain, prof0, a */
    TableType varchar(100), /* e.g. star, flat, counts */
    ClauseType varchar(10), /* FROM, WHERE, SELECT, GROUPBY */
    EntryType varchar(100), /* e.g. 1node, aggregate like count */
    Entries varchar(150)
);