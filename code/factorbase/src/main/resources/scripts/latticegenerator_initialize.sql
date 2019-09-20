/* Create the tables necessary for generating the lattice. */
SET storage_engine=INNODB;

CREATE TABLE lattice_membership (
    name VARCHAR(398),
    member VARCHAR(398),
    PRIMARY KEY(name, member)
);


CREATE TABLE lattice_rel (
    parent VARCHAR(398),
    child VARCHAR(398),
    removed VARCHAR(199),
    PRIMARY KEY(parent, child)
);


CREATE TABLE lattice_set (
    name VARCHAR(199),
    length INT(11),
    PRIMARY KEY(name, length)
);