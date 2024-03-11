/* Create the tables necessary for generating the lattice. */
SET storage_engine=INNODB;

CREATE TABLE lattice_membership (
    name VARCHAR(300),
    member VARCHAR(300),
    PRIMARY KEY(name, member)
);


CREATE TABLE lattice_rel (
    parent VARCHAR(300),
    child VARCHAR(300),
    removed VARCHAR(300),
    PRIMARY KEY(parent, child)
);


CREATE TABLE lattice_set (
    name VARCHAR(300),
    length INT(11),
    PRIMARY KEY(name, length)
);


CREATE TABLE lattice_mapping (
    orig_rnid VARCHAR(300),
    short_rnid VARCHAR(20),
    PRIMARY KEY(orig_rnid, short_rnid)
);