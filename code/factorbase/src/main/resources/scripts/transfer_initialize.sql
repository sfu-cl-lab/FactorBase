/* Create the tables necessary for cascading information based on the FunctorSet table. */
SET storage_engine=MEMORY;

CREATE TABLE 1Nodes AS
    SELECT
        N.1nid,
        N.COLUMN_NAME,
        N.pvid,
        N.main
    FROM
        @database@_setup.1Nodes N
    LIMIT
        0;


CREATE TABLE 2Nodes AS
    SELECT
        N.2nid,
        N.COLUMN_NAME,
        N.pvid1,
        N.pvid2,
        N.TABLE_NAME,
        N.main
    FROM
        @database@_setup.2Nodes N
    LIMIT
        0;


CREATE TABLE RNodes_2Nodes AS
    SELECT
        N.rnid,
        N.2nid,
        N.main
    FROM
        @database@_setup.RNodes_2Nodes N
    LIMIT
        0;


CREATE TABLE RNodes AS
    SELECT
        N.rnid,
        N.TABLE_NAME,
        N.pvid1,
        N.pvid2,
        N.COLUMN_NAME1,
        N.COLUMN_NAME2,
        N.main
    FROM
        @database@_setup.RNodes N
    LIMIT
        0;


/* Set up a table that contains all functor nodes of any arity.  Summarizes all the work we've done. */
CREATE TABLE FNodes (
    Fid varchar(199),
    FunctorName varchar(64),
    Type varchar(5),
    main int(11),
    PRIMARY KEY (Fid)
);


CREATE TABLE RNodes_pvars AS
    SELECT
        N.rnid,
        N.pvid,
        N.TABLE_NAME,
        N.COLUMN_NAME,
        N.REFERENCED_COLUMN_NAME
    FROM
        @database@_setup.RNodes_pvars N
    LIMIT
        0;


CREATE TABLE PVariables AS
    SELECT
        N.pvid,
        N.ID_COLUMN_NAME,
        N.TABLE_NAME,
        N.index_number
    FROM
        @database@_setup.PVariables N
    LIMIT
        0;


CREATE TABLE LatticeRNodes AS
    SELECT
        orig_rnid,
        short_rnid
    FROM
        @database@_setup.LatticeRNodes
    LIMIT
        0;


ALTER TABLE LatticeRNodes
    ADD UNIQUE INDEX rnid_UNIQUE (short_rnid ASC);