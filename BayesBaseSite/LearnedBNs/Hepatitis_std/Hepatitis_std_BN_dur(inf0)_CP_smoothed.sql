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
-- Table structure for table `dur(inf0)_CP_smoothed`
--

DROP TABLE IF EXISTS `dur(inf0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dur(inf0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL,
  `ttt(indis0)` varchar(45) DEFAULT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dur(inf0)_CP_smoothed`
--

LOCK TABLES `dur(inf0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `dur(inf0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `dur(inf0)_CP_smoothed` VALUES ('242','0','0',414,0.584541,NULL),('197','0','1',372,0.529569,NULL),('245','0','2',588,0.416667,NULL),('274','0','3',881,0.311010,NULL),('137','0','4',900,0.152222,NULL),('495','0','5',2832,0.174789,NULL),('56','1','0',414,0.135266,NULL),('112','1','1',372,0.301075,NULL),('171','1','2',588,0.290816,NULL),('122','1','3',881,0.138479,NULL),('125','1','4',900,0.138889,NULL),('703','1','5',2832,0.248234,NULL),('51','2','0',414,0.123188,NULL),('32','2','1',372,0.086022,NULL),('117','2','2',588,0.198980,NULL),('209','2','3',881,0.237230,NULL),('296','2','4',900,0.328889,NULL),('485','2','5',2832,0.171257,NULL),('42','3','0',414,0.101449,NULL),('21','3','1',372,0.056452,NULL),('31','3','2',588,0.052721,NULL),('92','3','3',881,0.104427,NULL),('107','3','4',900,0.118889,NULL),('431','3','5',2832,0.152189,NULL),('23','4','0',414,0.055556,NULL),('10','4','1',372,0.026882,NULL),('24','4','2',588,0.040816,NULL),('184','4','3',881,0.208854,NULL),('235','4','4',900,0.261111,NULL),('718','4','5',2832,0.253531,NULL);
/*!40000 ALTER TABLE `dur(inf0)_CP_smoothed` ENABLE KEYS */;
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
