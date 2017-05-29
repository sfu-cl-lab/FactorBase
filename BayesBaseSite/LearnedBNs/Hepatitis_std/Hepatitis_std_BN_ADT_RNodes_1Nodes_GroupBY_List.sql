CREATE DATABASE  IF NOT EXISTS `Hepatitis_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Hepatitis_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Hepatitis_std_BN
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
-- Table structure for table `ADT_RNodes_1Nodes_GroupBY_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_1Nodes_GroupBY_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_1Nodes_GroupBY_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_1Nodes_GroupBY_List`
--

LOCK TABLES `ADT_RNodes_1Nodes_GroupBY_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_GroupBY_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_1Nodes_GroupBY_List` VALUES ('`a`','`activity(Bio0)`'),('`b`','`alb(indis0)`'),('`b`','`che(indis0)`'),('`b`','`dbil(indis0)`'),('`c`','`dur(inf0)`'),('`a`','`fibros(Bio0)`'),('`b`','`got(indis0)`'),('`b`','`gpt(indis0)`'),('`b`','`tbil(indis0)`'),('`b`','`tcho(indis0)`'),('`b`','`tp(indis0)`'),('`b`','`ttt(indis0)`'),('`b`','`ztt(indis0)`'),('`a`','`age(dispat0)`'),('`b`','`age(dispat0)`'),('`c`','`age(dispat0)`'),('`a`','`sex(dispat0)`'),('`b`','`sex(dispat0)`'),('`c`','`sex(dispat0)`'),('`a`','`Type(dispat0)`'),('`b`','`Type(dispat0)`'),('`c`','`Type(dispat0)`');
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_GroupBY_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:23
