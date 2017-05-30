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
-- Table structure for table `action(item20)_CP_smoothed`
--

DROP TABLE IF EXISTS `action(item20)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `action(item20)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(4) default NULL,
  `rating(User0,item20)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action(item20)_CP_smoothed`
--

LOCK TABLES `action(item20)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `action(item20)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `action(item20)_CP_smoothed` VALUES ('3502','0','1',4705,0.744315,NULL),('6527','0','2',9154,0.713022,NULL),('16045','0','3',21926,0.731780,NULL),('20577','0','4',27334,0.752799,NULL),('12614','0','5',16670,0.756689,NULL),('1287312','0','N/A',1502985,0.856504,NULL),('1203','1','1',4705,0.255685,NULL),('2627','1','2',9154,0.286978,NULL),('5881','1','3',21926,0.268220,NULL),('6757','1','4',27334,0.247201,NULL),('4056','1','5',16670,0.243311,NULL),('215673','1','N/A',1502985,0.143496,NULL);
/*!40000 ALTER TABLE `action(item20)_CP_smoothed` ENABLE KEYS */;
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
