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
-- Table structure for table `ADT_RChain_Star_Select_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_Select_List` (
  `rchain` varchar(256) DEFAULT NULL,
  `rnid` varchar(256) DEFAULT NULL,
  `Entries` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Select_List`
--

LOCK TABLES `ADT_RChain_Star_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b,c`','`b`','`age(users0)`'),('`a,b,c`','`a`','`age(users0)`'),('`a,c`','`a`','`age(users0)`'),('`b,c`','`b`','`age(users0)`'),('`a,b,c`','`c`','`country(movies0)`'),('`a,b,c`','`b`','`country(movies0)`'),('`a,b`','`b`','`country(movies0)`'),('`a,c`','`c`','`country(movies0)`'),('`a,b,c`','`a`','`country(movies0)`'),('`a,b`','`a`','`country(movies0)`'),('`b,c`','`c`','`country(movies0)`'),('`a,b,c`','`c`','`isEnglish(movies0)`'),('`a,b,c`','`b`','`isEnglish(movies0)`'),('`a,b`','`b`','`isEnglish(movies0)`'),('`a,c`','`c`','`isEnglish(movies0)`'),('`a,b,c`','`a`','`isEnglish(movies0)`'),('`a,b`','`a`','`isEnglish(movies0)`'),('`b,c`','`c`','`isEnglish(movies0)`'),('`a,b,c`','`b`','`occupation(users0)`'),('`a,b,c`','`a`','`occupation(users0)`'),('`a,c`','`a`','`occupation(users0)`'),('`b,c`','`b`','`occupation(users0)`'),('`a,b,c`','`c`','`runningtime(movies0)`'),('`a,b,c`','`b`','`runningtime(movies0)`'),('`a,b`','`b`','`runningtime(movies0)`'),('`a,c`','`c`','`runningtime(movies0)`'),('`a,b,c`','`a`','`runningtime(movies0)`'),('`a,b`','`a`','`runningtime(movies0)`'),('`b,c`','`c`','`runningtime(movies0)`'),('`a,b,c`','`b`','`u_gender(users0)`'),('`a,b,c`','`a`','`u_gender(users0)`'),('`a,c`','`a`','`u_gender(users0)`'),('`b,c`','`b`','`u_gender(users0)`'),('`a,b,c`','`c`','`year(movies0)`'),('`a,b,c`','`b`','`year(movies0)`'),('`a,b`','`b`','`year(movies0)`'),('`a,c`','`c`','`year(movies0)`'),('`a,b,c`','`a`','`year(movies0)`'),('`a,b`','`a`','`year(movies0)`'),('`b,c`','`c`','`year(movies0)`'),('`a,b,c`','`c`','`avg_revenue(directors0)`'),('`a,b,c`','`a`','`avg_revenue(directors0)`'),('`a,b`','`a`','`avg_revenue(directors0)`'),('`b,c`','`c`','`avg_revenue(directors0)`'),('`a,b,c`','`c`','`a_gender(actors0)`'),('`a,b,c`','`b`','`a_gender(actors0)`'),('`a,b`','`b`','`a_gender(actors0)`'),('`a,c`','`c`','`a_gender(actors0)`'),('`a,b,c`','`c`','`a_quality(actors0)`'),('`a,b,c`','`b`','`a_quality(actors0)`'),('`a,b`','`b`','`a_quality(actors0)`'),('`a,c`','`c`','`a_quality(actors0)`'),('`a,c`','`a`','`country(movies0)`'),('`b,c`','`b`','`country(movies0)`'),('`a,b,c`','`c`','`d_quality(directors0)`'),('`a,b,c`','`a`','`d_quality(directors0)`'),('`a,b`','`a`','`d_quality(directors0)`'),('`b,c`','`c`','`d_quality(directors0)`'),('`a,c`','`a`','`isEnglish(movies0)`'),('`b,c`','`b`','`isEnglish(movies0)`'),('`a,c`','`a`','`runningtime(movies0)`'),('`b,c`','`b`','`runningtime(movies0)`'),('`a,c`','`a`','`year(movies0)`'),('`b,c`','`b`','`year(movies0)`'),('`a,b,c`','`c`','`cast_num(movies0,actors0)`'),('`a,b,c`','`b`','`cast_num(movies0,actors0)`'),('`a,b`','`b`','`cast_num(movies0,actors0)`'),('`a,c`','`c`','`cast_num(movies0,actors0)`'),('`a,b,c`','`c`','`genre(movies0,directors0)`'),('`a,b,c`','`a`','`genre(movies0,directors0)`'),('`a,b`','`a`','`genre(movies0,directors0)`'),('`b,c`','`c`','`genre(movies0,directors0)`'),('`a,b,c`','`b`','`rating(users0,movies0)`'),('`a,b,c`','`a`','`rating(users0,movies0)`'),('`a,c`','`a`','`rating(users0,movies0)`'),('`b,c`','`b`','`rating(users0,movies0)`'),('`a,b,c`','`c`','`age(users0)`'),('`a,c`','`c`','`age(users0)`'),('`b,c`','`c`','`age(users0)`'),('`a,b,c`','`b`','`avg_revenue(directors0)`'),('`a,b`','`b`','`avg_revenue(directors0)`'),('`b,c`','`b`','`avg_revenue(directors0)`'),('`a,b,c`','`a`','`a_gender(actors0)`'),('`a,b`','`a`','`a_gender(actors0)`'),('`a,c`','`a`','`a_gender(actors0)`'),('`a,b,c`','`a`','`a_quality(actors0)`'),('`a,b`','`a`','`a_quality(actors0)`'),('`a,c`','`a`','`a_quality(actors0)`'),('`a,b,c`','`b`','`d_quality(directors0)`'),('`a,b`','`b`','`d_quality(directors0)`'),('`b,c`','`b`','`d_quality(directors0)`'),('`a,b,c`','`c`','`occupation(users0)`'),('`a,c`','`c`','`occupation(users0)`'),('`b,c`','`c`','`occupation(users0)`'),('`a,b,c`','`c`','`u_gender(users0)`'),('`a,c`','`c`','`u_gender(users0)`'),('`b,c`','`c`','`u_gender(users0)`'),('`c`','`c`','`age(users0)`'),('`b`','`b`','`avg_revenue(directors0)`'),('`a`','`a`','`a_gender(actors0)`'),('`a`','`a`','`a_quality(actors0)`'),('`a`','`a`','`country(movies0)`'),('`b`','`b`','`country(movies0)`'),('`c`','`c`','`country(movies0)`'),('`b`','`b`','`d_quality(directors0)`'),('`a`','`a`','`isEnglish(movies0)`'),('`b`','`b`','`isEnglish(movies0)`'),('`c`','`c`','`isEnglish(movies0)`'),('`c`','`c`','`occupation(users0)`'),('`a`','`a`','`runningtime(movies0)`'),('`b`','`b`','`runningtime(movies0)`'),('`c`','`c`','`runningtime(movies0)`'),('`c`','`c`','`u_gender(users0)`'),('`a`','`a`','`year(movies0)`'),('`b`','`b`','`year(movies0)`'),('`c`','`c`','`year(movies0)`');
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` ENABLE KEYS */;
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
