USE @database@_BN;
SET storage_engine=INNODB;

UPDATE `lattice_membership`
        JOIN
    `RNodes` ON `lattice_membership`.member = `RNodes`.rnid 
SET 
    `lattice_membership`.orig_rnid = `RNodes`.orig_rnid
WHERE
    `lattice_membership`.member = `RNodes`.rnid;

UPDATE `lattice_rel`
        JOIN
    `RNodes` ON `lattice_rel`.removed = `RNodes`.rnid 
SET 
    `lattice_rel`.orig_rnid = `RNodes`.orig_rnid
WHERE
    `lattice_rel`.removed = `RNodes`.rnid;