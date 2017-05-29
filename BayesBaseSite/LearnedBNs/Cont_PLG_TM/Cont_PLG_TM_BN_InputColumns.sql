CREATE DATABASE  IF NOT EXISTS `Cont_PLG_TM_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Cont_PLG_TM_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Cont_PLG_TM_BN
-- ------------------------------------------------------
-- Server version	5.5.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `InputColumns`
--

DROP TABLE IF EXISTS `InputColumns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `InputColumns` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_TABLE_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `REFERENCED_COLUMN_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `CONSTRAINT_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `ORDINAL_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `InputColumns`
--

LOCK TABLES `InputColumns` WRITE;
/*!40000 ALTER TABLE `InputColumns` DISABLE KEYS */;
INSERT INTO `InputColumns` VALUES ('AppearsPlayerAway','PlayerID',NULL,NULL,'PRIMARY',1),('AppearsPlayerAway','MatchID',NULL,NULL,'PRIMARY',2),('AppearsPlayerHome','MatchID',NULL,NULL,'PRIMARY',2),('AppearsPlayerHome','PlayerID',NULL,NULL,'PRIMARY',1),('AppearsTeamAway','TeamID',NULL,NULL,'PRIMARY',1),('AppearsTeamAway','MatchID',NULL,NULL,'PRIMARY',2),('AppearsTeamHome','MatchID',NULL,NULL,'PRIMARY',2),('AppearsTeamHome','TeamID',NULL,NULL,'PRIMARY',1),('MatchComp','MatchID',NULL,NULL,'PRIMARY',1),('Players','PlayerID',NULL,NULL,'PRIMARY',1),('TeamPlayer','TeamID',NULL,NULL,'PRIMARY',1),('TeamPlayer','PlayerID',NULL,NULL,'PRIMARY',2),('Teams','TeamID',NULL,NULL,'PRIMARY',1);
/*!40000 ALTER TABLE `InputColumns` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:55
