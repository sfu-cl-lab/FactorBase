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
-- Table structure for table `gpt(indis0)_CP_smoothed`
--

DROP TABLE IF EXISTS `gpt(indis0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gpt(indis0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(10) DEFAULT NULL,
  `got(indis0)` varchar(10) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gpt(indis0)_CP_smoothed`
--

LOCK TABLES `gpt(indis0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `gpt(indis0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `gpt(indis0)_CP_smoothed` VALUES ('502','0','0','1',717,0.700140,NULL),('124','0','1','1',1538,0.080624,NULL),('5','0','2','1',1711,0.002923,NULL),('212','1','0','1',717,0.295676,NULL),('1341','1','1','1',1538,0.871912,NULL),('689','1','2','1',1711,0.402688,NULL),('57','1','3','1',2002,0.028472,NULL),('2','2','0','1',717,0.002789,NULL),('72','2','1','1',1538,0.046814,NULL),('1016','2','2','1',1711,0.593805,NULL),('1871','2','3','1',2002,0.934565,NULL),('73','3','3','1',2002,0.036464,NULL),('6','3','4','1',9,0.666667,NULL),('1','0','3','1',2002,0.000499,NULL),('1','0','4','1',9,0.111111,NULL),('1','1','4','1',9,0.111111,NULL),('1','2','4','1',9,0.111111,NULL),('1','3','0','1',717,0.001395,NULL),('1','3','1','1',1538,0.000650,NULL),('1','3','2','1',1711,0.000584,NULL);
/*!40000 ALTER TABLE `gpt(indis0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:22
