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
-- Table structure for table `activity(Bio0)_CP_smoothed`
--

DROP TABLE IF EXISTS `activity(Bio0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity(Bio0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) NOT NULL,
  `tbil(indis0)` varchar(45) DEFAULT NULL,
  `tcho(indis0)` varchar(45) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity(Bio0)_CP_smoothed`
--

LOCK TABLES `activity(Bio0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `activity(Bio0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `activity(Bio0)_CP_smoothed` VALUES ('4','0','0','0','1',359,0.011142,NULL),('27','0','0','1','1',600,0.045000,NULL),('3','0','0','2','1',161,0.018633,NULL),('17','0','1','0','1',615,0.027642,NULL),('57','0','1','1','1',2650,0.021509,NULL),('58','0','1','2','1',1233,0.047039,NULL),('13','0','1','3','1',352,0.036931,NULL),('82','1','0','0','1',359,0.228412,NULL),('216','1','0','1','1',600,0.360000,NULL),('45','1','0','2','1',161,0.279503,NULL),('3','1','0','3','1',27,0.111111,NULL),('216','1','1','0','1',615,0.351220,NULL),('1096','1','1','1','1',2650,0.413585,NULL),('462','1','1','2','1',1233,0.374696,NULL),('125','1','1','3','1',352,0.355114,NULL),('194','2','0','0','1',359,0.540390,NULL),('224','2','0','1','1',600,0.373333,NULL),('77','2','0','2','1',161,0.478261,NULL),('15','2','0','3','1',27,0.555556,NULL),('205','2','1','0','1',615,0.333333,NULL),('912','2','1','1','1',2650,0.344151,NULL),('403','2','1','2','1',1233,0.326845,NULL),('165','2','1','3','1',352,0.468750,NULL),('66','3','0','0','1',359,0.183844,NULL),('28','3','0','1','1',600,0.046667,NULL),('15','3','0','2','1',161,0.093168,NULL),('6','3','0','3','1',27,0.222222,NULL),('51','3','1','0','1',615,0.082927,NULL),('197','3','1','1','1',2650,0.074340,NULL),('86','3','1','2','1',1233,0.069749,NULL),('13','3','1','3','1',352,0.036932,NULL),('13','4','0','0','1',359,0.036212,NULL),('105','4','0','1','1',600,0.175000,NULL),('21','4','0','2','1',161,0.130435,NULL),('2','4','0','3','1',27,0.074074,NULL),('126','4','1','0','1',615,0.204878,NULL),('388','4','1','1','1',2650,0.146415,NULL),('224','4','1','2','1',1233,0.181671,NULL),('36','4','1','3','1',352,0.102273,NULL),('1','0','0','3','1',27,0.037037,NULL);
/*!40000 ALTER TABLE `activity(Bio0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:18
