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
-- Table structure for table `a_quality(actors0)_CP_pairs`
--

DROP TABLE IF EXISTS `a_quality(actors0)_CP_pairs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `a_quality(actors0)_CP_pairs` (
  `ChildValue` varchar(200) DEFAULT NULL,
  `a_gender(actors0)` varchar(200) DEFAULT NULL,
  `isEnglish(movies0)` varchar(200) DEFAULT NULL,
  `year(movies0)` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `a_quality(actors0)_CP_pairs`
--

LOCK TABLES `a_quality(actors0)_CP_pairs` WRITE;
/*!40000 ALTER TABLE `a_quality(actors0)_CP_pairs` DISABLE KEYS */;
INSERT INTO `a_quality(actors0)_CP_pairs` VALUES ('0','M','T','1'),('0','M','T','2'),('0','M','T','4'),('0','M','T','3'),('0','M','F','1'),('0','M','F','2'),('0','M','F','4'),('0','M','F','3'),('0','F','T','1'),('0','F','T','2'),('0','F','T','4'),('0','F','T','3'),('0','F','F','1'),('0','F','F','2'),('0','F','F','4'),('0','F','F','3'),('1','M','T','1'),('1','M','T','2'),('1','M','T','4'),('1','M','T','3'),('1','M','F','1'),('1','M','F','2'),('1','M','F','4'),('1','M','F','3'),('1','F','T','1'),('1','F','T','2'),('1','F','T','4'),('1','F','T','3'),('1','F','F','1'),('1','F','F','2'),('1','F','F','4'),('1','F','F','3'),('2','M','T','1'),('2','M','T','2'),('2','M','T','4'),('2','M','T','3'),('2','M','F','1'),('2','M','F','2'),('2','M','F','4'),('2','M','F','3'),('2','F','T','1'),('2','F','T','2'),('2','F','T','4'),('2','F','T','3'),('2','F','F','1'),('2','F','F','2'),('2','F','F','4'),('2','F','F','3'),('3','M','T','1'),('3','M','T','2'),('3','M','T','4'),('3','M','T','3'),('3','M','F','1'),('3','M','F','2'),('3','M','F','4'),('3','M','F','3'),('3','F','T','1'),('3','F','T','2'),('3','F','T','4'),('3','F','T','3'),('3','F','F','1'),('3','F','F','2'),('3','F','F','4'),('3','F','F','3'),('4','M','T','1'),('4','M','T','2'),('4','M','T','4'),('4','M','T','3'),('4','M','F','1'),('4','M','F','2'),('4','M','F','4'),('4','M','F','3'),('4','F','T','1'),('4','F','T','2'),('4','F','T','4'),('4','F','T','3'),('4','F','F','1'),('4','F','F','2'),('4','F','F','4'),('4','F','F','3'),('5','M','T','1'),('5','M','T','2'),('5','M','T','4'),('5','M','T','3'),('5','M','F','1'),('5','M','F','2'),('5','M','F','4'),('5','M','F','3'),('5','F','T','1'),('5','F','T','2'),('5','F','T','4'),('5','F','T','3'),('5','F','F','1'),('5','F','F','2'),('5','F','F','4'),('5','F','F','3');
/*!40000 ALTER TABLE `a_quality(actors0)_CP_pairs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:39:04
