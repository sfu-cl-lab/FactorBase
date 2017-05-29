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
-- Table structure for table `sat(course0,student0)_CP_smoothed`
--

DROP TABLE IF EXISTS `sat(course0,student0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sat(course0,student0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `b` varchar(5) default NULL,
  `grade(course0,student0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sat(course0,student0)_CP_smoothed`
--

LOCK TABLES `sat(course0,student0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `sat(course0,student0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `sat(course0,student0)_CP_smoothed` VALUES ('19','1','T','1',25,0.760000,NULL),('12','1','T','2',31,0.387097,NULL),('4','2','T','1',25,0.160000,NULL),('15','2','T','2',31,0.483871,NULL),('7','2','T','3',15,0.466667,NULL),('3','3','T','2',31,0.096774,NULL),('6','3','T','3',15,0.400000,NULL),('2','3','T','4',5,0.400000,NULL),('2221','N/A','F','N/A',2224,0.998651,NULL),('1','1','T','3',15,0.066666,NULL),('1','1','T','4',5,0.200000,NULL),('1','1','T','N/A',4,0.250000,NULL),('1','1','F','1',4,0.250000,NULL),('1','1','F','2',4,0.250000,NULL),('1','1','F','3',4,0.250000,NULL),('1','1','F','4',4,0.250000,NULL),('1','1','F','N/A',2224,0.000449,NULL),('1','2','T','4',5,0.200000,NULL),('1','2','T','N/A',4,0.250000,NULL),('1','2','F','1',4,0.250000,NULL),('1','2','F','2',4,0.250000,NULL),('1','2','F','3',4,0.250000,NULL),('1','2','F','4',4,0.250000,NULL),('1','2','F','N/A',2224,0.000450,NULL),('1','3','T','1',25,0.040000,NULL),('1','3','T','N/A',4,0.250000,NULL),('1','3','F','1',4,0.250000,NULL),('1','3','F','2',4,0.250000,NULL),('1','3','F','3',4,0.250000,NULL),('1','3','F','4',4,0.250000,NULL),('1','3','F','N/A',2224,0.000450,NULL),('1','N/A','T','1',25,0.040000,NULL),('1','N/A','T','2',31,0.032258,NULL),('1','N/A','T','3',15,0.066667,NULL),('1','N/A','T','4',5,0.200000,NULL),('1','N/A','T','N/A',4,0.250000,NULL),('1','N/A','F','1',4,0.250000,NULL),('1','N/A','F','2',4,0.250000,NULL),('1','N/A','F','3',4,0.250000,NULL),('1','N/A','F','4',4,0.250000,NULL);
/*!40000 ALTER TABLE `sat(course0,student0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:09
