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
-- Table structure for table `1Nodes_From_List`
--

DROP TABLE IF EXISTS `1Nodes_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_From_List` (
  `1nid` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(133) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_From_List`
--

LOCK TABLES `1Nodes_From_List` WRITE;
/*!40000 ALTER TABLE `1Nodes_From_List` DISABLE KEYS */;
INSERT INTO `1Nodes_From_List` VALUES ('`activity(Bio0)`','Bio AS Bio0'),('`age(dispat0)`','dispat AS dispat0'),('`alb(indis0)`','indis AS indis0'),('`che(indis0)`','indis AS indis0'),('`dbil(indis0)`','indis AS indis0'),('`dur(inf0)`','inf AS inf0'),('`fibros(Bio0)`','Bio AS Bio0'),('`got(indis0)`','indis AS indis0'),('`gpt(indis0)`','indis AS indis0'),('`sex(dispat0)`','dispat AS dispat0'),('`tbil(indis0)`','indis AS indis0'),('`tcho(indis0)`','indis AS indis0'),('`tp(indis0)`','indis AS indis0'),('`ttt(indis0)`','indis AS indis0'),('`Type(dispat0)`','dispat AS dispat0'),('`ztt(indis0)`','indis AS indis0');
/*!40000 ALTER TABLE `1Nodes_From_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:19
