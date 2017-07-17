USE unielwin_BN;
SET storage_engine=INNODB;

CREATE TABLE `Expansions` (
  `pvid` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`pvid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

