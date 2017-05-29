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
-- Table structure for table `Age(User0)_CP_smoothed`
--

DROP TABLE IF EXISTS `Age(User0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Age(User0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `action(item20)` varchar(4) default NULL,
  `rating(User0,item20)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Age(User0)_CP_smoothed`
--

LOCK TABLES `Age(User0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `Age(User0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `Age(User0)_CP_smoothed` VALUES ('1817','0','0','1',3504,0.518550,NULL),('3017','0','0','2',6529,0.462092,NULL),('6860','0','0','3',16047,0.427494,NULL),('8717','0','0','4',20579,0.423587,NULL),('5330','0','0','5',12616,0.422479,NULL),('512321','0','0','N/A',1287314,0.397976,NULL),('709','0','1','1',1205,0.588382,NULL),('1338','0','1','2',2629,0.508939,NULL),('2893','0','1','3',5883,0.491756,NULL),('3284','0','1','4',6759,0.485870,NULL),('2034','0','1','5',4058,0.501232,NULL),('84124','0','1','N/A',215675,0.390050,NULL),('1492','1','0','1',3504,0.425799,NULL),('2952','1','0','2',6529,0.452137,NULL),('7724','1','0','3',16047,0.481336,NULL),('9545','1','0','4',20579,0.463822,NULL),('5917','1','0','5',12616,0.469008,NULL),('632067','1','0','N/A',1287314,0.490997,NULL),('447','1','1','1',1205,0.370954,NULL),('1125','1','1','2',2629,0.427919,NULL),('2574','1','1','3',5883,0.437532,NULL),('2939','1','1','4',6759,0.434828,NULL),('1728','1','1','5',4058,0.425826,NULL),('106904','1','1','N/A',215675,0.495672,NULL),('195','2','0','1',3504,0.055651,NULL),('560','2','0','2',6529,0.085771,NULL),('1463','2','0','3',16047,0.091170,NULL),('2317','2','0','4',20579,0.112591,NULL),('1369','2','0','5',12616,0.108513,NULL),('142926','2','0','N/A',1287314,0.111027,NULL),('49','2','1','1',1205,0.040664,NULL),('166','2','1','2',2629,0.063142,NULL),('416','2','1','3',5883,0.070712,NULL),('536','2','1','4',6759,0.079302,NULL),('296','2','1','5',4058,0.072942,NULL),('24647','2','1','N/A',215675,0.114278,NULL);
/*!40000 ALTER TABLE `Age(User0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:19
