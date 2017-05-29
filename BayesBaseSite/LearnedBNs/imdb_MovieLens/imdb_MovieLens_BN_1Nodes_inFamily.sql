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
-- Table structure for table `1Nodes_inFamily`
--

DROP TABLE IF EXISTS `1Nodes_inFamily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_inFamily` (
  `ChildNode` varchar(197) NOT NULL,
  `1node` varchar(197) NOT NULL,
  `NumAtts` bigint(21) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_inFamily`
--

LOCK TABLES `1Nodes_inFamily` WRITE;
/*!40000 ALTER TABLE `1Nodes_inFamily` DISABLE KEYS */;
INSERT INTO `1Nodes_inFamily` VALUES ('`occupation(users0)`','`age(users0)`',7),('`runningtime(movies0)`','`age(users0)`',7),('`age(users0)`','`avg_revenue(directors0)`',5),('`cast_num(movies0,actors0)`','`avg_revenue(directors0)`',5),('`genre(movies0,directors0)`','`avg_revenue(directors0)`',5),('`isEnglish(movies0)`','`avg_revenue(directors0)`',5),('`rating(users0,movies0)`','`avg_revenue(directors0)`',5),('`a_quality(actors0)`','`a_gender(actors0)`',2),('`rating(users0,movies0)`','`a_gender(actors0)`',2),('`u_gender(users0)`','`a_gender(actors0)`',2),('`year(movies0)`','`a_gender(actors0)`',2),('`age(users0)`','`a_quality(actors0)`',6),('`cast_num(movies0,actors0)`','`a_quality(actors0)`',6),('`country(movies0)`','`a_quality(actors0)`',6),('`d_quality(directors0)`','`a_quality(actors0)`',6),('`genre(movies0,directors0)`','`a_quality(actors0)`',6),('`occupation(users0)`','`a_quality(actors0)`',6),('`rating(users0,movies0)`','`a_quality(actors0)`',6),('`runningtime(movies0)`','`a_quality(actors0)`',6),('`u_gender(users0)`','`a_quality(actors0)`',6),('`age(users0)`','`country(movies0)`',4),('`rating(users0,movies0)`','`country(movies0)`',4),('`a_quality(actors0)`','`isEnglish(movies0)`',2),('`cast_num(movies0,actors0)`','`isEnglish(movies0)`',2),('`country(movies0)`','`isEnglish(movies0)`',2),('`rating(users0,movies0)`','`isEnglish(movies0)`',2),('`u_gender(users0)`','`occupation(users0)`',5),('`d_quality(directors0)`','`runningtime(movies0)`',4),('`genre(movies0,directors0)`','`runningtime(movies0)`',4),('`rating(users0,movies0)`','`runningtime(movies0)`',4),('`genre(movies0,directors0)`','`u_gender(users0)`',2),('`age(users0)`','`year(movies0)`',4),('`avg_revenue(directors0)`','`year(movies0)`',4),('`a_quality(actors0)`','`year(movies0)`',4),('`cast_num(movies0,actors0)`','`year(movies0)`',4),('`country(movies0)`','`year(movies0)`',4),('`d_quality(directors0)`','`year(movies0)`',4),('`rating(users0,movies0)`','`year(movies0)`',4),('`runningtime(movies0)`','`year(movies0)`',4),('`u_gender(users0)`','`year(movies0)`',4);
/*!40000 ALTER TABLE `1Nodes_inFamily` ENABLE KEYS */;
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
