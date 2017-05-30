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
-- Table structure for table `popularity(prof0)_CP`
--

DROP TABLE IF EXISTS `popularity(prof0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `popularity(prof0)_CP` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `a` varchar(5) default NULL,
  `b` varchar(5) default NULL,
  `teachingability(prof0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `popularity(prof0)_CP`
--

LOCK TABLES `popularity(prof0)_CP` WRITE;
/*!40000 ALTER TABLE `popularity(prof0)_CP` DISABLE KEYS */;
INSERT INTO `popularity(prof0)_CP` VALUES ('720','1','F','F','2',1040,0.692308,-0.367724),('32','1','T','F','2',78,0.410256,-0.890974),('8','1','T','T','2',22,0.363636,-1.0116),('320','2','F','F','2',1040,0.307692,-1.17866),('990','2','F','F','3',990,1.000000,0),('46','2','T','F','2',78,0.589744,-0.528067),('112','2','T','F','3',112,1.000000,0),('14','2','T','T','2',22,0.636364,-0.451985),('38','2','T','T','3',38,1.000000,0);
/*!40000 ALTER TABLE `popularity(prof0)_CP` ENABLE KEYS */;
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
