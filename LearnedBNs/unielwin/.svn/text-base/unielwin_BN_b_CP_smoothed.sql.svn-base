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
-- Table structure for table `b_CP_smoothed`
--

DROP TABLE IF EXISTS `b_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `b_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(5) default NULL,
  `a` varchar(5) default NULL,
  `salary(prof0,student0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `b_CP_smoothed`
--

LOCK TABLES `b_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `b_CP_smoothed` DISABLE KEYS */;
INSERT INTO `b_CP_smoothed` VALUES ('2031','F','F','N/A',2032,0.999508,NULL),('84','F','T','high',112,0.750000,NULL),('37','F','T','low',52,0.711538,NULL),('72','F','T','med',92,0.782609,NULL),('28','T','T','high',112,0.250000,NULL),('15','T','T','low',52,0.288462,NULL),('20','T','T','med',92,0.217391,NULL),('1','F','F','high',2,0.500000,NULL),('1','F','F','low',2,0.500000,NULL),('1','F','F','med',2,0.500000,NULL),('1','F','T','N/A',2,0.500000,NULL),('1','T','F','N/A',2032,0.000492,NULL),('1','T','F','high',2,0.500000,NULL),('1','T','F','low',2,0.500000,NULL),('1','T','F','med',2,0.500000,NULL),('1','T','T','N/A',2,0.500000,NULL);
/*!40000 ALTER TABLE `b_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:11
