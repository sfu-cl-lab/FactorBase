/**
 * Populate the local relationship lattice tables by extracting information from the global relationship lattice
 * tables.
 */
CREATE PROCEDURE populateLattice()
BEGIN

DROP TABLE IF EXISTS lattice_membership;
CREATE TABLE lattice_membership AS
    -- Get the "name"s and "member"s where the "name"s are unique to the local lattice.
    SELECT
        name,
        member
    FROM
        @database@_setup.lattice_membership LMEM,
        LatticeRNodes LR
    WHERE
        LMEM.member = LR.orig_rnid
    AND
        name
    NOT IN (
        -- Get the "name"s that belong to RNodes that are not in the local lattice.
        SELECT
            name
        FROM
            @database@_setup.lattice_membership
        WHERE
            member
        NOT IN (
            -- Get the RNodes for the local lattice.
            SELECT
                orig_rnid
            FROM
                LatticeRNodes
        )
    );


DROP TABLE IF EXISTS lattice_rel;
CREATE TABLE lattice_rel AS
    SELECT
        parent,
        child,
        removed
    FROM
        @database@_setup.lattice_rel LREL,
        LatticeRNodes LR
    WHERE
        LREL.removed = LR.orig_rnid;


DROP TABLE IF EXISTS lattice_set;
CREATE TABLE lattice_set AS
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
        LS.name = LMEM.name
    GROUP BY
        LS.name;


DROP TABLE IF EXISTS lattice_mapping;
CREATE TABLE lattice_mapping AS
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
        LMAP.orig_rnid = LMEM.name
    GROUP BY
       LMAP.orig_rnid;

END//