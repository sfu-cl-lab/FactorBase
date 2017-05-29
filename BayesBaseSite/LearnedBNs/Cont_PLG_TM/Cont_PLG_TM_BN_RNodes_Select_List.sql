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
-- Table structure for table `RNodes_Select_List`
--

DROP TABLE IF EXISTS `RNodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(278) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_Select_List`
--

LOCK TABLES `RNodes_Select_List` WRITE;
/*!40000 ALTER TABLE `RNodes_Select_List` DISABLE KEYS */;
INSERT INTO `RNodes_Select_List` VALUES ('`a`','1 as \"MULT\"'),('`b`','1 as \"MULT\"'),('`c`','1 as \"MULT\"'),('`d`','1 as \"MULT\"'),('`e`','1 as \"MULT\"'),('`f`','1 as \"MULT\"'),('`a`','MatchComp0.Result AS `Result(MatchComp0)`'),('`b`','MatchComp0.Result AS `Result(MatchComp0)`'),('`c`','MatchComp0.Result AS `Result(MatchComp0)`'),('`d`','MatchComp0.Result AS `Result(MatchComp0)`'),('`a`','`a`.dribble_eff_PA AS `dribble_eff_PA(Players0,MatchComp0)`'),('`a`','`a`.passes_eff_PA AS `passes_eff_PA(Players0,MatchComp0)`'),('`a`','`a`.SavesMade_PA AS `SavesMade_PA(Players0,MatchComp0)`'),('`a`','`a`.shot_eff_PA AS `shot_eff_PA(Players0,MatchComp0)`'),('`a`','`a`.tackle_eff_PA AS `tackle_eff_PA(Players0,MatchComp0)`'),('`a`','`a`.TimePlayed_PA AS `TimePlayed_PA(Players0,MatchComp0)`'),('`b`','`b`.dribble_eff_PH AS `dribble_eff_PH(Players1,MatchComp0)`'),('`b`','`b`.passes_eff_PH AS `passes_eff_PH(Players1,MatchComp0)`'),('`b`','`b`.SavesMade_PH AS `SavesMade_PH(Players1,MatchComp0)`'),('`b`','`b`.shot_eff_PH AS `shot_eff_PH(Players1,MatchComp0)`'),('`b`','`b`.tackle_eff_PH AS `tackle_eff_PH(Players1,MatchComp0)`'),('`b`','`b`.TimePlayed_PH AS `TimePlayed_PH(Players1,MatchComp0)`'),('`c`','`c`.shot_eff_AT AS `shot_eff_AT(Teams0,MatchComp0)`'),('`d`','`d`.shot_eff_HT AS `shot_eff_HT(Teams1,MatchComp0)`');
/*!40000 ALTER TABLE `RNodes_Select_List` ENABLE KEYS */;
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
