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
-- Table structure for table `1Nodes_inFamily`
--

DROP TABLE IF EXISTS `1Nodes_inFamily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_inFamily` (
  `ChildNode` varchar(197) NOT NULL,
  `1node` varchar(197) NOT NULL,
  `NumAtts` bigint(21) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_inFamily`
--

LOCK TABLES `1Nodes_inFamily` WRITE;
/*!40000 ALTER TABLE `1Nodes_inFamily` DISABLE KEYS */;
INSERT INTO `1Nodes_inFamily` VALUES ('`Type(dispat0)`','`age(dispat0)`',7),('`che(indis0)`','`alb(indis0)`',2),('`sex(dispat0)`','`alb(indis0)`',2),('`dbil(indis0)`','`che(indis0)`',10),('`tbil(indis0)`','`dbil(indis0)`',2),('`tcho(indis0)`','`dur(inf0)`',5),('`che(indis0)`','`got(indis0)`',5),('`gpt(indis0)`','`got(indis0)`',5),('`tbil(indis0)`','`got(indis0)`',5),('`dbil(indis0)`','`gpt(indis0)`',4),('`tp(indis0)`','`gpt(indis0)`',4),('`activity(Bio0)`','`tbil(indis0)`',2),('`fibros(Bio0)`','`tbil(indis0)`',2),('`sex(dispat0)`','`tbil(indis0)`',2),('`activity(Bio0)`','`tcho(indis0)`',4),('`fibros(Bio0)`','`tcho(indis0)`',4),('`got(indis0)`','`tcho(indis0)`',4),('`sex(dispat0)`','`tcho(indis0)`',4),('`tbil(indis0)`','`tcho(indis0)`',4),('`alb(indis0)`','`tp(indis0)`',4),('`dur(inf0)`','`ttt(indis0)`',6),('`got(indis0)`','`ttt(indis0)`',6),('`ztt(indis0)`','`ttt(indis0)`',6),('`activity(Bio0)`','`Type(dispat0)`',2),('`che(indis0)`','`Type(dispat0)`',2),('`fibros(Bio0)`','`Type(dispat0)`',2),('`gpt(indis0)`','`Type(dispat0)`',2),('`sex(dispat0)`','`Type(dispat0)`',2),('`tcho(indis0)`','`Type(dispat0)`',2),('`age(dispat0)`','`ztt(indis0)`',6),('`alb(indis0)`','`ztt(indis0)`',6),('`tcho(indis0)`','`ztt(indis0)`',6),('`tp(indis0)`','`ztt(indis0)`',6),('`Type(dispat0)`','`ztt(indis0)`',6);
/*!40000 ALTER TABLE `1Nodes_inFamily` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:20
