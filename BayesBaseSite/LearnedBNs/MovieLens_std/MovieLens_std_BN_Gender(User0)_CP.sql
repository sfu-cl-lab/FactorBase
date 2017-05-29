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
-- Table structure for table `Gender(User0)_CP`
--

DROP TABLE IF EXISTS `Gender(User0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Gender(User0)_CP` (
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
-- Dumping data for table `Gender(User0)_CP`
--

LOCK TABLES `Gender(User0)_CP` WRITE;
/*!40000 ALTER TABLE `Gender(User0)_CP` DISABLE KEYS */;
INSERT INTO `Gender(User0)_CP` VALUES ('1162','F','0','1',3501,0.331905,-1.10291),('1761','F','0','2',6526,0.269844,-1.30991),('4416','F','0','3',16044,0.275243,-1.2901),('5362','F','0','4',20576,0.260595,-1.34479),('3733','F','0','5',12613,0.295964,-1.21752),('372798','F','0','N/A',1287311,0.289594,-1.23928),('313','F','1','1',1202,0.260399,-1.34554),('529','F','1','2',2626,0.201447,-1.60223),('1207','F','1','3',5880,0.205272,-1.58342),('1387','F','1','4',6756,0.205299,-1.58329),('981','F','1','5',4055,0.241924,-1.41913),('63855','F','1','N/A',215672,0.296075,-1.21714),('2339','M','0','1',3501,0.668095,-0.403325),('4765','M','0','2',6526,0.730156,-0.314497),('11628','M','0','3',16044,0.724757,-0.321919),('15214','M','0','4',20576,0.739405,-0.30191),('8880','M','0','5',12613,0.704036,-0.350926),('914513','M','0','N/A',1287311,0.710406,-0.341919),('889','M','1','1',1202,0.739601,-0.301644),('2097','M','1','2',2626,0.798553,-0.224954),('4673','M','1','3',5880,0.794728,-0.229755),('5369','M','1','4',6756,0.794701,-0.229789),('3074','M','1','5',4055,0.758076,-0.276972),('151817','M','1','N/A',215672,0.703925,-0.351083);
/*!40000 ALTER TABLE `Gender(User0)_CP` ENABLE KEYS */;
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
