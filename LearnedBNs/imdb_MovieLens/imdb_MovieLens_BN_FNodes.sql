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
-- Table structure for table `FNodes`
--

DROP TABLE IF EXISTS `FNodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FNodes` (
  `Fid` varchar(199) NOT NULL DEFAULT '',
  `FunctorName` varchar(64) DEFAULT NULL,
  `Type` varchar(5) DEFAULT NULL,
  `main` int(11) DEFAULT NULL,
  PRIMARY KEY (`Fid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FNodes`
--

LOCK TABLES `FNodes` WRITE;
/*!40000 ALTER TABLE `FNodes` DISABLE KEYS */;
INSERT INTO `FNodes` VALUES ('`age(users0)`','age','1Node',1),('`avg_revenue(directors0)`','avg_revenue','1Node',1),('`a_gender(actors0)`','a_gender','1Node',1),('`a_quality(actors0)`','a_quality','1Node',1),('`a`','`a`','Rnode',1),('`b`','`b`','Rnode',1),('`cast_num(movies0,actors0)`','cast_num','2Node',1),('`country(movies0)`','country','1Node',1),('`c`','`c`','Rnode',1),('`d_quality(directors0)`','d_quality','1Node',1),('`genre(movies0,directors0)`','genre','2Node',1),('`isEnglish(movies0)`','isEnglish','1Node',1),('`occupation(users0)`','occupation','1Node',1),('`rating(users0,movies0)`','rating','2Node',1),('`runningtime(movies0)`','runningtime','1Node',1),('`u_gender(users0)`','u_gender','1Node',1),('`year(movies0)`','year','1Node',1);
/*!40000 ALTER TABLE `FNodes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:38:58
