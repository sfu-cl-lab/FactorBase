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
-- Table structure for table `ForeignKeys_pvars`
--

DROP TABLE IF EXISTS `ForeignKeys_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ForeignKeys_pvars` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `index_number` bigint(20) NOT NULL DEFAULT '0',
  `ARGUMENT_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ForeignKeys_pvars`
--

LOCK TABLES `ForeignKeys_pvars` WRITE;
/*!40000 ALTER TABLE `ForeignKeys_pvars` DISABLE KEYS */;
INSERT INTO `ForeignKeys_pvars` VALUES ('AppearsPlayerAway','MatchComp','MatchID','MatchComp0',0,2),('AppearsPlayerAway','Players','PlayerID','Players0',0,1),('AppearsPlayerAway','Players','PlayerID','Players1',0,1),('AppearsPlayerHome','MatchComp','MatchID','MatchComp0',0,2),('AppearsPlayerHome','Players','PlayerID','Players0',0,1),('AppearsPlayerHome','Players','PlayerID','Players1',0,1),('AppearsTeamAway','MatchComp','MatchID','MatchComp0',0,2),('AppearsTeamAway','Teams','TeamID','Teams0',0,1),('AppearsTeamAway','Teams','TeamID','Teams1',0,1),('AppearsTeamHome','MatchComp','MatchID','MatchComp0',0,2),('AppearsTeamHome','Teams','TeamID','Teams0',0,1),('AppearsTeamHome','Teams','TeamID','Teams1',0,1),('TeamPlayer','Players','PlayerID','Players0',0,2),('TeamPlayer','Players','PlayerID','Players1',0,2),('TeamPlayer','Teams','TeamID','Teams0',0,1),('TeamPlayer','Teams','TeamID','Teams1',0,1);
/*!40000 ALTER TABLE `ForeignKeys_pvars` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:52
