/**
 * Populate the local relationship lattice tables by extracting information from the global relationship lattice
 * tables.
 */
TRUNCATE lattice_membership;
INSERT INTO lattice_membership
    SELECT
        name,
        member
    FROM
        @database@_setup.lattice_membership LMEM,
        LatticeRNodes LR
    WHERE
        LMEM.member = LR.orig_rnid;


TRUNCATE lattice_rel;
INSERT INTO lattice_rel
    SELECT
        parent,
        child,
        removed
    FROM
        @database@_setup.lattice_rel LREL,
        LatticeRNodes LR
    WHERE
        LREL.removed = LR.orig_rnid;


TRUNCATE lattice_set;
INSERT IGNORE INTO lattice_set
    SELECT
        LS.name,
        length
    FROM
        @database@_setup.lattice_set LS,
        lattice_membership LMEM,
        LatticeRNodes LR
    WHERE
        LMEM.member = LR.orig_rnid
    AND
        LS.name = LMEM.name;


TRUNCATE lattice_mapping;
INSERT IGNORE INTO lattice_mapping
    SELECT
        LMAP.orig_rnid,
        LMAP.short_rnid
    FROM
        @database@_setup.lattice_mapping LMAP,
        lattice_membership LMEM,
        LatticeRNodes LR
    WHERE
        LMEM.member = LR.orig_rnid
    AND
        LMAP.orig_rnid = LMEM.name;