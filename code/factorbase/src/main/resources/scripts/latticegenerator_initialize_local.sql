/* Create the tables necessary for generating the lattice. */
SET storage_engine=MEMORY;

CREATE TABLE lattice_membership (
    name VARCHAR(399),
    member VARCHAR(399),
    PRIMARY KEY(name, member)
)CHARACTER SET latin1 COLLATE latin1_swedish_ci;



CREATE TABLE lattice_rel (
    parent VARCHAR(399),
    child VARCHAR(399),
    removed VARCHAR(199),
    PRIMARY KEY(parent, child)
)CHARACTER SET latin1 COLLATE latin1_swedish_ci;



CREATE TABLE lattice_set (
    name VARCHAR(399),
    length INT(11),
    PRIMARY KEY(name, length)
)CHARACTER SET latin1 COLLATE latin1_swedish_ci;



CREATE VIEW lattice_mapping AS
    SELECT
        orig_rnid,
        short_rnid
    FROM
        @database@_setup.lattice_mapping;