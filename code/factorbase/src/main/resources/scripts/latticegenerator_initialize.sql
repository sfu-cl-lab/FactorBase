/* Create the tables necessary for generating the lattice. */
SET storage_engine=INNODB;

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



CREATE TABLE lattice_mapping (
    orig_rnid VARCHAR(399),
    short_rnid VARCHAR(399),
    PRIMARY KEY(orig_rnid, short_rnid)
)CHARACTER SET latin1 COLLATE latin1_swedish_ci;
