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
-- Table structure for table `Gender(User0)_CP_smoothed`
--

DROP TABLE IF EXISTS `Gender(User0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Gender(User0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(5) default NULL,
  `action(item20)` varchar(4) default NULL,
  `rating(User0,item20)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Gender(User0)_CP_smoothed`
--

LOCK TABLES `Gender(User0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `Gender(User0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `Gender(User0)_CP_smoothed` VALUES ('1163','F','0','1',3503,0.332001,NULL),('1762','F','0','2',6528,0.269914,NULL),('4417','F','0','3',16046,0.275271,NULL),('5363','F','0','4',20578,0.260618,NULL),('3734','F','0','5',12615,0.295997,NULL),('372799','F','0','N/A',1287313,0.289595,NULL),('314','F','1','1',1204,0.260797,NULL),('530','F','1','2',2628,0.201674,NULL),('1208','F','1','3',5882,0.205372,NULL),('1388','F','1','4',6758,0.205386,NULL),('982','F','1','5',4057,0.242051,NULL),('63856','F','1','N/A',215674,0.296076,NULL),('2340','M','0','1',3503,0.667999,NULL),('4766','M','0','2',6528,0.730086,NULL),('11629','M','0','3',16046,0.724729,NULL),('15215','M','0','4',20578,0.739382,NULL),('8881','M','0','5',12615,0.704003,NULL),('914514','M','0','N/A',1287313,0.710405,NULL),('890','M','1','1',1204,0.739203,NULL),('2098','M','1','2',2628,0.798326,NULL),('4674','M','1','3',5882,0.794628,NULL),('5370','M','1','4',6758,0.794614,NULL),('3075','M','1','5',4057,0.757949,NULL),('151818','M','1','N/A',215674,0.703924,NULL);
/*!40000 ALTER TABLE `Gender(User0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:17
