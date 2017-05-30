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
-- Table structure for table `fibros(Bio0)_CP`
--

DROP TABLE IF EXISTS `fibros(Bio0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fibros(Bio0)_CP` (
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
-- Dumping data for table `fibros(Bio0)_CP`
--

LOCK TABLES `fibros(Bio0)_CP` WRITE;
/*!40000 ALTER TABLE `fibros(Bio0)_CP` DISABLE KEYS */;
INSERT INTO `fibros(Bio0)_CP` VALUES ('8','0','0','0','1',354,0.022599,-3.78985),('69','0','0','1','1',595,0.115966,-2.15446),('9','0','0','2','1',156,0.057692,-2.85264),('1','0','0','3','1',22,0.045455,-3.09103),('34','0','1','0','1',610,0.055738,-2.88709),('229','0','1','1','1',2645,0.086578,-2.44671),('78','0','1','2','1',1228,0.063518,-2.75643),('6','0','1','3','1',347,0.017291,-4.05757),('85','1','0','0','1',354,0.240113,-1.42665),('337','1','0','1','1',595,0.566387,-0.568478),('56','1','0','2','1',156,0.358974,-1.02451),('6','1','0','3','1',22,0.272727,-1.29928),('190','1','1','0','1',610,0.311475,-1.16644),('1196','1','1','1','1',2645,0.452174,-0.793688),('565','1','1','2','1',1228,0.460098,-0.776316),('169','1','1','3','1',347,0.487032,-0.719425),('73','2','0','0','1',354,0.206215,-1.57884),('66','2','0','1','1',595,0.110924,-2.19891),('20','2','0','2','1',156,0.128205,-2.05412),('5','2','0','3','1',22,0.227273,-1.4816),('86','2','1','0','1',610,0.140984,-1.95911),('528','2','1','1','1',2645,0.199622,-1.61133),('269','2','1','2','1',1228,0.219055,-1.51843),('70','2','1','3','1',347,0.201729,-1.60083),('127','3','0','0','1',354,0.358757,-1.02511),('77','3','0','1','1',595,0.129412,-2.04475),('29','3','0','2','1',156,0.185897,-1.68256),('114','3','1','0','1',610,0.186885,-1.67726),('404','3','1','1','1',2645,0.152741,-1.87901),('143','3','1','2','1',1228,0.116450,-2.15029),('30','3','1','3','1',347,0.086455,-2.44813),('61','4','0','0','1',354,0.172316,-1.75843),('46','4','0','1','1',595,0.077311,-2.55992),('42','4','0','2','1',156,0.269231,-1.31219),('10','4','0','3','1',22,0.454545,-0.788458),('186','4','1','0','1',610,0.304918,-1.18771),('288','4','1','1','1',2645,0.108885,-2.21746),('173','4','1','2','1',1228,0.140879,-1.95985),('72','4','1','3','1',347,0.207493,-1.57266);
/*!40000 ALTER TABLE `fibros(Bio0)_CP` ENABLE KEYS */;
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
