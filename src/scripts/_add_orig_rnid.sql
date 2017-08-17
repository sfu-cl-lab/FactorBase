USE unielwin_BN;
SET storage_engine=INNODB;

UPDATE `lattice_membership`
        JOIN
    `LatticeRNodes` ON `lattice_membership`.member = `LatticeRNodes`.short_rnid 
SET 
    `lattice_membership`.orig_rnid = `LatticeRNodes`.orig_rnid
WHERE
    `lattice_membership`.member = `LatticeRNodes`.short_rnid;

UPDATE `lattice_rel`
        JOIN
    `LatticeRNodes` ON `lattice_rel`.removed = `LatticeRNodes`.short_rnid 
SET 
    `lattice_rel`.orig_rnid = `LatticeRNodes`.orig_rnid
WHERE
    `lattice_rel`.removed = `LatticeRNodes`.short_rnid;
