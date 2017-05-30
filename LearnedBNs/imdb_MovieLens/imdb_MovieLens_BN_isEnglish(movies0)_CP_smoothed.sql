CREATE DATABASE  IF NOT EXISTS `imdb_MovieLens_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `imdb_MovieLens_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: imdb_MovieLens_BN
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
-- Table structure for table `isEnglish(movies0)_CP_smoothed`
--

DROP TABLE IF EXISTS `isEnglish(movies0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `isEnglish(movies0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` enum('T','F') NOT NULL,
  `avg_revenue(directors0)` int(11) NOT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `isEnglish(movies0)_CP_smoothed`
--

LOCK TABLES `isEnglish(movies0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `isEnglish(movies0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `isEnglish(movies0)_CP_smoothed` VALUES ('632022','T',0,673070,0.939014,NULL),('600026','T',1,829857,0.723047,NULL),('2646442','T',2,3058017,0.865411,NULL),('9545650','T',3,9676813,0.986446,NULL),('33122111','T',4,33441563,0.990447,NULL),('41048','F',0,673070,0.060986,NULL),('229831','F',1,829857,0.276953,NULL),('411575','F',2,3058017,0.134589,NULL),('131163','F',3,9676813,0.013554,NULL),('319452','F',4,33441563,0.009553,NULL);
/*!40000 ALTER TABLE `isEnglish(movies0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:39:00
