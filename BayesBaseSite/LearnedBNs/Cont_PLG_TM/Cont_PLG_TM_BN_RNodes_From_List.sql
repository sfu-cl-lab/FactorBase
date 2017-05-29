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
-- Table structure for table `RNodes_From_List`
--

DROP TABLE IF EXISTS `RNodes_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_From_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(145) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_From_List`
--

LOCK TABLES `RNodes_From_List` WRITE;
/*!40000 ALTER TABLE `RNodes_From_List` DISABLE KEYS */;
INSERT INTO `RNodes_From_List` VALUES ('`a`','Cont_PLG_TM.Players AS Players0'),('`b`','Cont_PLG_TM.Players AS Players1'),('`c`','Cont_PLG_TM.Teams AS Teams0'),('`d`','Cont_PLG_TM.Teams AS Teams1'),('`e`','Cont_PLG_TM.Teams AS Teams0'),('`f`','Cont_PLG_TM.Teams AS Teams1'),('`a`','Cont_PLG_TM.MatchComp AS MatchComp0'),('`b`','Cont_PLG_TM.MatchComp AS MatchComp0'),('`c`','Cont_PLG_TM.MatchComp AS MatchComp0'),('`d`','Cont_PLG_TM.MatchComp AS MatchComp0'),('`e`','Cont_PLG_TM.Players AS Players0'),('`f`','Cont_PLG_TM.Players AS Players1'),('`a`','Cont_PLG_TM.AppearsPlayerAway AS `a`'),('`b`','Cont_PLG_TM.AppearsPlayerHome AS `b`'),('`c`','Cont_PLG_TM.AppearsTeamAway AS `c`'),('`d`','Cont_PLG_TM.AppearsTeamHome AS `d`'),('`e`','Cont_PLG_TM.TeamPlayer AS `e`'),('`f`','Cont_PLG_TM.TeamPlayer AS `f`');
/*!40000 ALTER TABLE `RNodes_From_List` ENABLE KEYS */;
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
