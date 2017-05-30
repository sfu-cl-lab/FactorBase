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
-- Table structure for table `RNodes_Select_List`
--

DROP TABLE IF EXISTS `RNodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(278) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_Select_List`
--

LOCK TABLES `RNodes_Select_List` WRITE;
/*!40000 ALTER TABLE `RNodes_Select_List` DISABLE KEYS */;
INSERT INTO `RNodes_Select_List` VALUES ('`a`','count(*) as \"MULT\"'),('`b`','count(*) as \"MULT\"'),('`c`','count(*) as \"MULT\"'),('`c`','users0.age AS `age(users0)`'),('`a`','movies0.country AS `country(movies0)`'),('`b`','movies0.country AS `country(movies0)`'),('`a`','movies0.isEnglish AS `isEnglish(movies0)`'),('`b`','movies0.isEnglish AS `isEnglish(movies0)`'),('`c`','users0.occupation AS `occupation(users0)`'),('`a`','movies0.runningtime AS `runningtime(movies0)`'),('`b`','movies0.runningtime AS `runningtime(movies0)`'),('`c`','users0.u_gender AS `u_gender(users0)`'),('`a`','movies0.year AS `year(movies0)`'),('`b`','movies0.year AS `year(movies0)`'),('`b`','directors0.avg_revenue AS `avg_revenue(directors0)`'),('`a`','actors0.a_gender AS `a_gender(actors0)`'),('`a`','actors0.a_quality AS `a_quality(actors0)`'),('`c`','movies0.country AS `country(movies0)`'),('`b`','directors0.d_quality AS `d_quality(directors0)`'),('`c`','movies0.isEnglish AS `isEnglish(movies0)`'),('`c`','movies0.runningtime AS `runningtime(movies0)`'),('`c`','movies0.year AS `year(movies0)`'),('`a`','`a`.cast_num AS `cast_num(movies0,actors0)`'),('`b`','`b`.genre AS `genre(movies0,directors0)`'),('`c`','`c`.rating AS `rating(users0,movies0)`');
/*!40000 ALTER TABLE `RNodes_Select_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:39:03
