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
-- Table structure for table `Path_Required_Edges`
--

DROP TABLE IF EXISTS `Path_Required_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_Required_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Required_Edges`
--

LOCK TABLES `Path_Required_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Required_Edges` DISABLE KEYS */;
INSERT INTO `Path_Required_Edges` VALUES ('`a,b,c`','`age(users0)`','`avg_revenue(directors0)`'),('`a,b,c`','`age(users0)`','`a_quality(actors0)`'),('`a,b,c`','`age(users0)`','`cast_num(movies0,actors0)`'),('`a,b,c`','`age(users0)`','`country(movies0)`'),('`a,b,c`','`age(users0)`','`year(movies0)`'),('`a,b,c`','`avg_revenue(directors0)`','`a_quality(actors0)`'),('`a,b,c`','`avg_revenue(directors0)`','`d_quality(directors0)`'),('`a,b,c`','`avg_revenue(directors0)`','`year(movies0)`'),('`a,b,c`','`a_quality(actors0)`','`a_gender(actors0)`'),('`a,b,c`','`a_quality(actors0)`','`isEnglish(movies0)`'),('`a,b,c`','`a_quality(actors0)`','`year(movies0)`'),('`a,b,c`','`cast_num(movies0,actors0)`','`avg_revenue(directors0)`'),('`a,b,c`','`cast_num(movies0,actors0)`','`a_quality(actors0)`'),('`a,b,c`','`cast_num(movies0,actors0)`','`a`'),('`a,b,c`','`cast_num(movies0,actors0)`','`isEnglish(movies0)`'),('`a,b,c`','`cast_num(movies0,actors0)`','`runningtime(movies0)`'),('`a,b,c`','`cast_num(movies0,actors0)`','`year(movies0)`'),('`a,b,c`','`country(movies0)`','`a_quality(actors0)`'),('`a,b,c`','`country(movies0)`','`cast_num(movies0,actors0)`'),('`a,b,c`','`country(movies0)`','`isEnglish(movies0)`'),('`a,b,c`','`country(movies0)`','`year(movies0)`'),('`a,b,c`','`d_quality(directors0)`','`a_quality(actors0)`'),('`a,b,c`','`d_quality(directors0)`','`rating(users0,movies0)`'),('`a,b,c`','`d_quality(directors0)`','`runningtime(movies0)`'),('`a,b,c`','`d_quality(directors0)`','`year(movies0)`'),('`a,b,c`','`genre(movies0,directors0)`','`avg_revenue(directors0)`'),('`a,b,c`','`genre(movies0,directors0)`','`a_quality(actors0)`'),('`a,b,c`','`genre(movies0,directors0)`','`b`'),('`a,b,c`','`genre(movies0,directors0)`','`cast_num(movies0,actors0)`'),('`a,b,c`','`genre(movies0,directors0)`','`rating(users0,movies0)`'),('`a,b,c`','`genre(movies0,directors0)`','`runningtime(movies0)`'),('`a,b,c`','`genre(movies0,directors0)`','`u_gender(users0)`'),('`a,b,c`','`isEnglish(movies0)`','`avg_revenue(directors0)`'),('`a,b,c`','`isEnglish(movies0)`','`d_quality(directors0)`'),('`a,b,c`','`occupation(users0)`','`age(users0)`'),('`a,b,c`','`occupation(users0)`','`a_quality(actors0)`'),('`a,b,c`','`occupation(users0)`','`rating(users0,movies0)`'),('`a,b,c`','`rating(users0,movies0)`','`avg_revenue(directors0)`'),('`a,b,c`','`rating(users0,movies0)`','`a_gender(actors0)`'),('`a,b,c`','`rating(users0,movies0)`','`a_quality(actors0)`'),('`a,b,c`','`rating(users0,movies0)`','`cast_num(movies0,actors0)`'),('`a,b,c`','`rating(users0,movies0)`','`country(movies0)`'),('`a,b,c`','`rating(users0,movies0)`','`c`'),('`a,b,c`','`rating(users0,movies0)`','`isEnglish(movies0)`'),('`a,b,c`','`rating(users0,movies0)`','`runningtime(movies0)`'),('`a,b,c`','`rating(users0,movies0)`','`year(movies0)`'),('`a,b,c`','`runningtime(movies0)`','`age(users0)`'),('`a,b,c`','`runningtime(movies0)`','`a_quality(actors0)`'),('`a,b,c`','`runningtime(movies0)`','`year(movies0)`'),('`a,b,c`','`u_gender(users0)`','`a_gender(actors0)`'),('`a,b,c`','`u_gender(users0)`','`a_quality(actors0)`'),('`a,b,c`','`u_gender(users0)`','`occupation(users0)`'),('`a,b,c`','`u_gender(users0)`','`rating(users0,movies0)`'),('`a,b,c`','`u_gender(users0)`','`year(movies0)`'),('`a,b,c`','`year(movies0)`','`a_gender(actors0)`'),('`a,b`','`avg_revenue(directors0)`','`d_quality(directors0)`'),('`a,b`','`avg_revenue(directors0)`','`year(movies0)`'),('`a,b`','`a_quality(actors0)`','`a_gender(actors0)`'),('`a,b`','`a_quality(actors0)`','`isEnglish(movies0)`'),('`a,b`','`a_quality(actors0)`','`year(movies0)`'),('`a,b`','`cast_num(movies0,actors0)`','`a_quality(actors0)`'),('`a,b`','`cast_num(movies0,actors0)`','`a`'),('`a,b`','`cast_num(movies0,actors0)`','`isEnglish(movies0)`'),('`a,b`','`cast_num(movies0,actors0)`','`runningtime(movies0)`'),('`a,b`','`cast_num(movies0,actors0)`','`year(movies0)`'),('`a,b`','`country(movies0)`','`a_quality(actors0)`'),('`a,b`','`country(movies0)`','`cast_num(movies0,actors0)`'),('`a,b`','`country(movies0)`','`isEnglish(movies0)`'),('`a,b`','`country(movies0)`','`year(movies0)`'),('`a,b`','`d_quality(directors0)`','`runningtime(movies0)`'),('`a,b`','`d_quality(directors0)`','`year(movies0)`'),('`a,b`','`genre(movies0,directors0)`','`avg_revenue(directors0)`'),('`a,b`','`genre(movies0,directors0)`','`b`'),('`a,b`','`genre(movies0,directors0)`','`runningtime(movies0)`'),('`a,b`','`isEnglish(movies0)`','`avg_revenue(directors0)`'),('`a,b`','`isEnglish(movies0)`','`d_quality(directors0)`'),('`a,b`','`runningtime(movies0)`','`a_quality(actors0)`'),('`a,b`','`runningtime(movies0)`','`year(movies0)`'),('`a,b`','`year(movies0)`','`a_gender(actors0)`'),('`a,c`','`age(users0)`','`country(movies0)`'),('`a,c`','`age(users0)`','`year(movies0)`'),('`a,c`','`a_quality(actors0)`','`a_gender(actors0)`'),('`a,c`','`a_quality(actors0)`','`isEnglish(movies0)`'),('`a,c`','`a_quality(actors0)`','`year(movies0)`'),('`a,c`','`cast_num(movies0,actors0)`','`a_quality(actors0)`'),('`a,c`','`cast_num(movies0,actors0)`','`a`'),('`a,c`','`cast_num(movies0,actors0)`','`isEnglish(movies0)`'),('`a,c`','`cast_num(movies0,actors0)`','`runningtime(movies0)`'),('`a,c`','`cast_num(movies0,actors0)`','`year(movies0)`'),('`a,c`','`country(movies0)`','`a_quality(actors0)`'),('`a,c`','`country(movies0)`','`cast_num(movies0,actors0)`'),('`a,c`','`country(movies0)`','`isEnglish(movies0)`'),('`a,c`','`country(movies0)`','`year(movies0)`'),('`a,c`','`occupation(users0)`','`age(users0)`'),('`a,c`','`occupation(users0)`','`rating(users0,movies0)`'),('`a,c`','`rating(users0,movies0)`','`country(movies0)`'),('`a,c`','`rating(users0,movies0)`','`c`'),('`a,c`','`rating(users0,movies0)`','`isEnglish(movies0)`'),('`a,c`','`rating(users0,movies0)`','`runningtime(movies0)`'),('`a,c`','`rating(users0,movies0)`','`year(movies0)`'),('`a,c`','`runningtime(movies0)`','`age(users0)`'),('`a,c`','`runningtime(movies0)`','`a_quality(actors0)`'),('`a,c`','`runningtime(movies0)`','`year(movies0)`'),('`a,c`','`u_gender(users0)`','`occupation(users0)`'),('`a,c`','`u_gender(users0)`','`rating(users0,movies0)`'),('`a,c`','`u_gender(users0)`','`year(movies0)`'),('`a,c`','`year(movies0)`','`a_gender(actors0)`'),('`a`','`a_quality(actors0)`','`a_gender(actors0)`'),('`a`','`cast_num(movies0,actors0)`','`a`'),('`a`','`country(movies0)`','`isEnglish(movies0)`'),('`a`','`country(movies0)`','`year(movies0)`'),('`a`','`runningtime(movies0)`','`year(movies0)`'),('`b,c`','`age(users0)`','`country(movies0)`'),('`b,c`','`age(users0)`','`year(movies0)`'),('`b,c`','`avg_revenue(directors0)`','`d_quality(directors0)`'),('`b,c`','`avg_revenue(directors0)`','`year(movies0)`'),('`b,c`','`country(movies0)`','`isEnglish(movies0)`'),('`b,c`','`country(movies0)`','`year(movies0)`'),('`b,c`','`d_quality(directors0)`','`runningtime(movies0)`'),('`b,c`','`d_quality(directors0)`','`year(movies0)`'),('`b,c`','`genre(movies0,directors0)`','`avg_revenue(directors0)`'),('`b,c`','`genre(movies0,directors0)`','`b`'),('`b,c`','`genre(movies0,directors0)`','`runningtime(movies0)`'),('`b,c`','`isEnglish(movies0)`','`avg_revenue(directors0)`'),('`b,c`','`isEnglish(movies0)`','`d_quality(directors0)`'),('`b,c`','`occupation(users0)`','`age(users0)`'),('`b,c`','`occupation(users0)`','`rating(users0,movies0)`'),('`b,c`','`rating(users0,movies0)`','`country(movies0)`'),('`b,c`','`rating(users0,movies0)`','`c`'),('`b,c`','`rating(users0,movies0)`','`isEnglish(movies0)`'),('`b,c`','`rating(users0,movies0)`','`runningtime(movies0)`'),('`b,c`','`rating(users0,movies0)`','`year(movies0)`'),('`b,c`','`runningtime(movies0)`','`age(users0)`'),('`b,c`','`runningtime(movies0)`','`year(movies0)`'),('`b,c`','`u_gender(users0)`','`occupation(users0)`'),('`b,c`','`u_gender(users0)`','`rating(users0,movies0)`'),('`b,c`','`u_gender(users0)`','`year(movies0)`'),('`b`','`avg_revenue(directors0)`','`d_quality(directors0)`'),('`b`','`country(movies0)`','`isEnglish(movies0)`'),('`b`','`country(movies0)`','`year(movies0)`'),('`b`','`genre(movies0,directors0)`','`b`'),('`b`','`runningtime(movies0)`','`year(movies0)`'),('`c`','`country(movies0)`','`isEnglish(movies0)`'),('`c`','`country(movies0)`','`year(movies0)`'),('`c`','`occupation(users0)`','`age(users0)`'),('`c`','`rating(users0,movies0)`','`c`'),('`c`','`runningtime(movies0)`','`year(movies0)`'),('`c`','`u_gender(users0)`','`occupation(users0)`');
/*!40000 ALTER TABLE `Path_Required_Edges` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:38:59
