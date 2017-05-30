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
-- Table structure for table `sex(dispat0)_CP_smoothed`
--

DROP TABLE IF EXISTS `sex(dispat0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sex(dispat0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: COUNT',
  `alb(indis0)` varchar(45) DEFAULT NULL,
  `tbil(indis0)` varchar(45) DEFAULT NULL,
  `tcho(indis0)` varchar(45) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sex(dispat0)_CP_smoothed`
--

LOCK TABLES `sex(dispat0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `sex(dispat0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `sex(dispat0)_CP_smoothed` VALUES ('197','0','0','0','0','1',219,0.899543,NULL),('249','0','0','0','1','1',490,0.508163,NULL),('61','0','0','0','2','1',148,0.412162,NULL),('2','0','0','0','3','1',14,0.142857,NULL),('320','0','0','1','0','1',471,0.679406,NULL),('1148','0','0','1','1','1',1787,0.642417,NULL),('445','0','0','1','2','1',809,0.550062,NULL),('147','0','0','1','3','1',229,0.641921,NULL),('138','0','1','0','0','1',139,0.992806,NULL),('77','0','1','0','1','1',109,0.706422,NULL),('5','0','1','0','2','1',12,0.416667,NULL),('2','0','1','0','3','1',12,0.166667,NULL),('122','0','1','1','0','1',143,0.853147,NULL),('721','0','1','1','1','1',862,0.836427,NULL),('255','0','1','1','2','1',423,0.602837,NULL),('66','0','1','1','3','1',122,0.540984,NULL),('22','1','0','0','0','1',219,0.100457,NULL),('241','1','0','0','1','1',490,0.491837,NULL),('87','1','0','0','2','1',148,0.587838,NULL),('12','1','0','0','3','1',14,0.857143,NULL),('151','1','0','1','0','1',471,0.320594,NULL),('639','1','0','1','1','1',1787,0.357583,NULL),('364','1','0','1','2','1',809,0.449938,NULL),('82','1','0','1','3','1',229,0.358079,NULL),('32','1','1','0','1','1',109,0.293578,NULL),('7','1','1','0','2','1',12,0.583333,NULL),('10','1','1','0','3','1',12,0.833333,NULL),('21','1','1','1','0','1',143,0.146853,NULL),('141','1','1','1','1','1',862,0.163573,NULL),('168','1','1','1','2','1',423,0.397163,NULL),('56','1','1','1','3','1',122,0.459016,NULL),('1','1','1','0','0','1',139,0.007194,NULL);
/*!40000 ALTER TABLE `sex(dispat0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:24
