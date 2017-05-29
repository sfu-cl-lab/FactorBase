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
-- Table structure for table `2Nodes`
--

DROP TABLE IF EXISTS `2Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `2Nodes` (
  `2nid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid1` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid2` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `main` int(11) NOT NULL DEFAULT '0',
  KEY `index` (`pvid1`,`pvid2`,`TABLE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `2Nodes`
--

LOCK TABLES `2Nodes` WRITE;
/*!40000 ALTER TABLE `2Nodes` DISABLE KEYS */;
INSERT INTO `2Nodes` VALUES ('`dribble_eff_PA(Players0,MatchComp0)`','dribble_eff_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`dribble_eff_PH(Players1,MatchComp0)`','dribble_eff_PH','Players1','MatchComp0','AppearsPlayerHome',1),('`passes_eff_PA(Players0,MatchComp0)`','passes_eff_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`passes_eff_PH(Players1,MatchComp0)`','passes_eff_PH','Players1','MatchComp0','AppearsPlayerHome',1),('`SavesMade_PA(Players0,MatchComp0)`','SavesMade_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`SavesMade_PH(Players1,MatchComp0)`','SavesMade_PH','Players1','MatchComp0','AppearsPlayerHome',1),('`shot_eff_AT(Teams0,MatchComp0)`','shot_eff_AT','Teams0','MatchComp0','AppearsTeamAway',1),('`shot_eff_HT(Teams1,MatchComp0)`','shot_eff_HT','Teams1','MatchComp0','AppearsTeamHome',1),('`shot_eff_PA(Players0,MatchComp0)`','shot_eff_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`shot_eff_PH(Players1,MatchComp0)`','shot_eff_PH','Players1','MatchComp0','AppearsPlayerHome',1),('`tackle_eff_PA(Players0,MatchComp0)`','tackle_eff_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`tackle_eff_PH(Players1,MatchComp0)`','tackle_eff_PH','Players1','MatchComp0','AppearsPlayerHome',1),('`TimePlayed_PA(Players0,MatchComp0)`','TimePlayed_PA','Players0','MatchComp0','AppearsPlayerAway',1),('`TimePlayed_PH(Players1,MatchComp0)`','TimePlayed_PH','Players1','MatchComp0','AppearsPlayerHome',1);
/*!40000 ALTER TABLE `2Nodes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:53
