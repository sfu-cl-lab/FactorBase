/* Create the table and view necessary for storing metaquery information. */

/*SET storage_engine=MEMORY;*/

CREATE TABLE MetaQueries (
    Lattice_Point varchar(398), /* e.g. pvid, rchain, prof0, a */
    TableType varchar(100), /* e.g. star, flat, counts */
    ClauseType varchar(10), /* FROM, WHERE, SELECT, GROUPBY */
    EntryType varchar(100), /* e.g. 1node, aggregate like count */
    Entries varchar(150)
);


CREATE VIEW RChain_pvars AS
    SELECT DISTINCT
        M.name AS rchain,
        pvid
    FROM
        lattice_membership M,
        RNodes_pvars R
    WHERE
        R.rnid = M.member;