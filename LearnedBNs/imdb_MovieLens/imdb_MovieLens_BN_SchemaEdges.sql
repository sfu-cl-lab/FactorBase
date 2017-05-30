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
-- Table structure for table `SchemaEdges`
--

DROP TABLE IF EXISTS `SchemaEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SchemaEdges` (
  `Rchain` varchar(256) NOT NULL DEFAULT '',
  `child` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `parent` varchar(10) DEFAULT NULL,
  KEY `HashIn` (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SchemaEdges`
--

LOCK TABLES `SchemaEdges` WRITE;
/*!40000 ALTER TABLE `SchemaEdges` DISABLE KEYS */;
INSERT INTO `SchemaEdges` VALUES ('`a,b,c`','`cast_num(movies0,actors0)`','`a`'),('`a,b,c`','`genre(movies0,directors0)`','`b`'),('`a,b,c`','`rating(users0,movies0)`','`c`'),('`a,b`','`cast_num(movies0,actors0)`','`a`'),('`a,b`','`genre(movies0,directors0)`','`b`'),('`a,c`','`cast_num(movies0,actors0)`','`a`'),('`a,c`','`rating(users0,movies0)`','`c`'),('`a`','`cast_num(movies0,actors0)`','`a`'),('`b,c`','`genre(movies0,directors0)`','`b`'),('`b,c`','`rating(users0,movies0)`','`c`'),('`b`','`genre(movies0,directors0)`','`b`'),('`c`','`rating(users0,movies0)`','`c`');
/*!40000 ALTER TABLE `SchemaEdges` ENABLE KEYS */;
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
