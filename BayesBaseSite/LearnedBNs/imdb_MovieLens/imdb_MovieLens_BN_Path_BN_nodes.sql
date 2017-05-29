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
-- Table structure for table `Path_BN_nodes`
--

DROP TABLE IF EXISTS `Path_BN_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_BN_nodes` (
  `Rchain` varchar(256) NOT NULL DEFAULT '',
  `node` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  KEY `HashIndex` (`Rchain`,`node`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BN_nodes`
--

LOCK TABLES `Path_BN_nodes` WRITE;
/*!40000 ALTER TABLE `Path_BN_nodes` DISABLE KEYS */;
INSERT INTO `Path_BN_nodes` VALUES ('`a,b,c`','`age(users0)`'),('`a,b,c`','`avg_revenue(directors0)`'),('`a,b,c`','`a_gender(actors0)`'),('`a,b,c`','`a_quality(actors0)`'),('`a,b,c`','`cast_num(movies0,actors0)`'),('`a,b,c`','`country(movies0)`'),('`a,b,c`','`d_quality(directors0)`'),('`a,b,c`','`genre(movies0,directors0)`'),('`a,b,c`','`isEnglish(movies0)`'),('`a,b,c`','`occupation(users0)`'),('`a,b,c`','`rating(users0,movies0)`'),('`a,b,c`','`runningtime(movies0)`'),('`a,b,c`','`u_gender(users0)`'),('`a,b,c`','`year(movies0)`'),('`a,b`','`avg_revenue(directors0)`'),('`a,b`','`a_gender(actors0)`'),('`a,b`','`a_quality(actors0)`'),('`a,b`','`cast_num(movies0,actors0)`'),('`a,b`','`country(movies0)`'),('`a,b`','`d_quality(directors0)`'),('`a,b`','`genre(movies0,directors0)`'),('`a,b`','`isEnglish(movies0)`'),('`a,b`','`runningtime(movies0)`'),('`a,b`','`year(movies0)`'),('`a,c`','`age(users0)`'),('`a,c`','`a_gender(actors0)`'),('`a,c`','`a_quality(actors0)`'),('`a,c`','`cast_num(movies0,actors0)`'),('`a,c`','`country(movies0)`'),('`a,c`','`isEnglish(movies0)`'),('`a,c`','`occupation(users0)`'),('`a,c`','`rating(users0,movies0)`'),('`a,c`','`runningtime(movies0)`'),('`a,c`','`u_gender(users0)`'),('`a,c`','`year(movies0)`'),('`a`','`a_gender(actors0)`'),('`a`','`a_quality(actors0)`'),('`a`','`cast_num(movies0,actors0)`'),('`a`','`country(movies0)`'),('`a`','`isEnglish(movies0)`'),('`a`','`runningtime(movies0)`'),('`a`','`year(movies0)`'),('`b,c`','`age(users0)`'),('`b,c`','`avg_revenue(directors0)`'),('`b,c`','`country(movies0)`'),('`b,c`','`d_quality(directors0)`'),('`b,c`','`genre(movies0,directors0)`'),('`b,c`','`isEnglish(movies0)`'),('`b,c`','`occupation(users0)`'),('`b,c`','`rating(users0,movies0)`'),('`b,c`','`runningtime(movies0)`'),('`b,c`','`u_gender(users0)`'),('`b,c`','`year(movies0)`'),('`b`','`avg_revenue(directors0)`'),('`b`','`country(movies0)`'),('`b`','`d_quality(directors0)`'),('`b`','`genre(movies0,directors0)`'),('`b`','`isEnglish(movies0)`'),('`b`','`runningtime(movies0)`'),('`b`','`year(movies0)`'),('`c`','`age(users0)`'),('`c`','`country(movies0)`'),('`c`','`isEnglish(movies0)`'),('`c`','`occupation(users0)`'),('`c`','`rating(users0,movies0)`'),('`c`','`runningtime(movies0)`'),('`c`','`u_gender(users0)`'),('`c`','`year(movies0)`');
/*!40000 ALTER TABLE `Path_BN_nodes` ENABLE KEYS */;
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
