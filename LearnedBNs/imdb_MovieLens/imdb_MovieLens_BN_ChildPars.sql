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
-- Table structure for table `ChildPars`
--

DROP TABLE IF EXISTS `ChildPars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ChildPars` (
  `NumPars` bigint(22) NOT NULL DEFAULT '0',
  `ChildNode` varchar(199) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ChildPars`
--

LOCK TABLES `ChildPars` WRITE;
/*!40000 ALTER TABLE `ChildPars` DISABLE KEYS */;
INSERT INTO `ChildPars` VALUES (6,'`age(users0)`'),(4,'`avg_revenue(directors0)`'),(1,'`a_gender(actors0)`'),(5,'`a_quality(actors0)`'),(3,'`cast_num(movies0,actors0)`'),(3,'`country(movies0)`'),(5,'`d_quality(directors0)`'),(8,'`genre(movies0,directors0)`'),(1,'`isEnglish(movies0)`'),(4,'`occupation(users0)`'),(4,'`rating(users0,movies0)`'),(3,'`runningtime(movies0)`'),(1,'`u_gender(users0)`'),(3,'`year(movies0)`'),(1,'`a`'),(1,'`b`'),(1,'`c`');
/*!40000 ALTER TABLE `ChildPars` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:38:56
