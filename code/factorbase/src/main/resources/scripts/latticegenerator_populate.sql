/**
 * Populate the local relationship lattice tables by extracting information from the global relationship lattice
 * tables.
 */
CREATE PROCEDURE populateLattice()
BEGIN

TRUNCATE lattice_membership;
INSERT INTO lattice_membership
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
INSERT INTO lattice_set
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
END//