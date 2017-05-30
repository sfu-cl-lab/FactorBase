CREATE DATABASE  IF NOT EXISTS `unielwin_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `unielwin_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: unielwin_BN
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
-- Table structure for table `ranking(student0)_CP_smoothed`
--

DROP TABLE IF EXISTS `ranking(student0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ranking(student0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `a` varchar(5) default NULL,
  `intelligence(student0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ranking(student0)_CP_smoothed`
--

LOCK TABLES `ranking(student0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `ranking(student0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `ranking(student0)_CP_smoothed` VALUES ('421','1','F','3',585,0.719659,NULL),('61','1','T','3',85,0.717646,NULL),('251','2','F','2',655,0.383206,NULL),('161','2','F','3',585,0.275214,NULL),('51','2','T','2',135,0.377778,NULL),('21','2','T','3',85,0.247059,NULL),('351','3','F','2',655,0.535878,NULL),('71','3','T','2',135,0.525926,NULL),('321','4','F','1',805,0.398758,NULL),('51','4','F','2',655,0.077863,NULL),('41','4','T','1',45,0.911111,NULL),('11','4','T','2',135,0.081481,NULL),('481','5','F','1',805,0.597516,NULL),('1','1','F','2',655,0.001526,NULL),('1','1','F','1',805,0.001242,NULL),('1','1','T','2',135,0.007408,NULL),('1','1','T','1',45,0.022223,NULL),('1','2','F','1',805,0.001242,NULL),('1','2','T','1',45,0.022222,NULL),('1','3','F','3',585,0.001709,NULL),('1','3','F','1',805,0.001242,NULL),('1','3','T','3',85,0.011765,NULL),('1','3','T','1',45,0.022222,NULL),('1','4','F','3',585,0.001709,NULL),('1','4','T','3',85,0.011765,NULL),('1','5','F','3',585,0.001709,NULL),('1','5','F','2',655,0.001527,NULL),('1','5','T','3',85,0.011765,NULL),('1','5','T','2',135,0.007407,NULL),('1','5','T','1',45,0.022222,NULL);
/*!40000 ALTER TABLE `ranking(student0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:10
