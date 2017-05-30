CREATE DATABASE  IF NOT EXISTS `MovieLens_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `MovieLens_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: MovieLens_std_BN
-- ------------------------------------------------------
-- Server version	5.0.95

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
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Table structure for table `rating(User0,item20)_CP_smoothed`
--

DROP TABLE IF EXISTS `rating(User0,item20)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rating(User0,item20)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `a` varchar(5) default NULL,
  `horror(item20)` varchar(4) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rating(User0,item20)_CP_smoothed`
--

LOCK TABLES `rating(User0,item20)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `rating(User0,item20)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `rating(User0,item20)_CP_smoothed` VALUES ('4334','1','T','0',75607,0.057323,NULL),('371','1','T','1',4184,0.088671,NULL),('8493','2','T','0',75607,0.112331,NULL),('661','2','T','1',4184,0.157983,NULL),('20729','3','T','0',75607,0.274168,NULL),('1197','3','T','1',4184,0.286090,NULL),('26053','4','T','0',75607,0.344584,NULL),('1281','4','T','1',4184,0.306166,NULL),('15997','5','T','0',75607,0.211581,NULL),('673','5','T','1',4184,0.160851,NULL),('1420590','N/A','F','0',1420595,0.999996,NULL),('82395','N/A','F','1',82400,0.999939,NULL),('1','1','F','0',1420595,0.000000,NULL),('1','1','F','1',82400,0.000013,NULL),('1','2','F','0',1420595,0.000001,NULL),('1','2','F','1',82400,0.000012,NULL),('1','3','F','0',1420595,0.000001,NULL),('1','3','F','1',82400,0.000012,NULL),('1','4','F','0',1420595,0.000001,NULL),('1','4','F','1',82400,0.000012,NULL),('1','5','F','0',1420595,0.000001,NULL),('1','5','F','1',82400,0.000012,NULL),('1','N/A','T','0',75607,0.000013,NULL),('1','N/A','T','1',4184,0.000239,NULL);
/*!40000 ALTER TABLE `rating(User0,item20)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:16
