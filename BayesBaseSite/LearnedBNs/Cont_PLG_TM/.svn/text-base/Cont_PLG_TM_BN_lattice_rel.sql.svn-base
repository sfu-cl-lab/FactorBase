CREATE DATABASE  IF NOT EXISTS `Cont_PLG_TM_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Cont_PLG_TM_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Cont_PLG_TM_BN
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
-- Table structure for table `lattice_rel`
--

DROP TABLE IF EXISTS `lattice_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_rel` (
  `parent` varchar(256) NOT NULL DEFAULT '',
  `child` varchar(256) NOT NULL DEFAULT '',
  `removed` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`parent`,`child`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_rel`
--

LOCK TABLES `lattice_rel` WRITE;
/*!40000 ALTER TABLE `lattice_rel` DISABLE KEYS */;
INSERT INTO `lattice_rel` VALUES ('EmptySet','`a`','`a`'),('EmptySet','`b`','`b`'),('EmptySet','`c`','`c`'),('EmptySet','`d`','`d`'),('EmptySet','`e`','`e`'),('EmptySet','`f`','`f`'),('`a,b,c,d,e`','`a,b,c,d,e,f`','`f`'),('`a,b,c,d,f`','`a,b,c,d,e,f`','`e`'),('`a,b,c,d`','`a,b,c,d,e`','`e`'),('`a,b,c,d`','`a,b,c,d,f`','`f`'),('`a,b,c,e,f`','`a,b,c,d,e,f`','`d`'),('`a,b,c,e`','`a,b,c,d,e`','`d`'),('`a,b,c,e`','`a,b,c,e,f`','`f`'),('`a,b,c,f`','`a,b,c,d,f`','`d`'),('`a,b,c,f`','`a,b,c,e,f`','`e`'),('`a,b,c`','`a,b,c,d`','`d`'),('`a,b,c`','`a,b,c,e`','`e`'),('`a,b,c`','`a,b,c,f`','`f`'),('`a,b,d,e,f`','`a,b,c,d,e,f`','`c`'),('`a,b,d,e`','`a,b,c,d,e`','`c`'),('`a,b,d,e`','`a,b,d,e,f`','`f`'),('`a,b,d,f`','`a,b,c,d,f`','`c`'),('`a,b,d,f`','`a,b,d,e,f`','`e`'),('`a,b,d`','`a,b,c,d`','`c`'),('`a,b,d`','`a,b,d,e`','`e`'),('`a,b,d`','`a,b,d,f`','`f`'),('`a,b,e,f`','`a,b,c,e,f`','`c`'),('`a,b,e,f`','`a,b,d,e,f`','`d`'),('`a,b,e`','`a,b,c,e`','`c`'),('`a,b,e`','`a,b,d,e`','`d`'),('`a,b,e`','`a,b,e,f`','`f`'),('`a,b,f`','`a,b,c,f`','`c`'),('`a,b,f`','`a,b,d,f`','`d`'),('`a,b,f`','`a,b,e,f`','`e`'),('`a,b`','`a,b,c`','`c`'),('`a,b`','`a,b,d`','`d`'),('`a,b`','`a,b,e`','`e`'),('`a,b`','`a,b,f`','`f`'),('`a,c,d,e,f`','`a,b,c,d,e,f`','`b`'),('`a,c,d,e`','`a,b,c,d,e`','`b`'),('`a,c,d,e`','`a,c,d,e,f`','`f`'),('`a,c,d,f`','`a,b,c,d,f`','`b`'),('`a,c,d,f`','`a,c,d,e,f`','`e`'),('`a,c,d`','`a,b,c,d`','`b`'),('`a,c,d`','`a,c,d,e`','`e`'),('`a,c,d`','`a,c,d,f`','`f`'),('`a,c,e`','`a,b,c,e`','`b`'),('`a,c,e`','`a,c,d,e`','`d`'),('`a,c`','`a,b,c`','`b`'),('`a,c`','`a,c,d`','`d`'),('`a,c`','`a,c,e`','`e`'),('`a,d,e,f`','`a,b,d,e,f`','`b`'),('`a,d,e,f`','`a,c,d,e,f`','`c`'),('`a,d,e`','`a,b,d,e`','`b`'),('`a,d,e`','`a,c,d,e`','`c`'),('`a,d,e`','`a,d,e,f`','`f`'),('`a,d,f`','`a,b,d,f`','`b`'),('`a,d,f`','`a,c,d,f`','`c`'),('`a,d,f`','`a,d,e,f`','`e`'),('`a,d`','`a,b,d`','`b`'),('`a,d`','`a,c,d`','`c`'),('`a,d`','`a,d,e`','`e`'),('`a,d`','`a,d,f`','`f`'),('`a,e`','`a,b,e`','`b`'),('`a,e`','`a,c,e`','`c`'),('`a,e`','`a,d,e`','`d`'),('`a`','`a,b`','`b`'),('`a`','`a,c`','`c`'),('`a`','`a,d`','`d`'),('`a`','`a,e`','`e`'),('`b,c,d,e,f`','`a,b,c,d,e,f`','`a`'),('`b,c,d,e`','`a,b,c,d,e`','`a`'),('`b,c,d,e`','`b,c,d,e,f`','`f`'),('`b,c,d,f`','`a,b,c,d,f`','`a`'),('`b,c,d,f`','`b,c,d,e,f`','`e`'),('`b,c,d`','`a,b,c,d`','`a`'),('`b,c,d`','`b,c,d,e`','`e`'),('`b,c,d`','`b,c,d,f`','`f`'),('`b,c,e,f`','`a,b,c,e,f`','`a`'),('`b,c,e,f`','`b,c,d,e,f`','`d`'),('`b,c,e`','`a,b,c,e`','`a`'),('`b,c,e`','`b,c,d,e`','`d`'),('`b,c,e`','`b,c,e,f`','`f`'),('`b,c,f`','`a,b,c,f`','`a`'),('`b,c,f`','`b,c,d,f`','`d`'),('`b,c,f`','`b,c,e,f`','`e`'),('`b,c`','`a,b,c`','`a`'),('`b,c`','`b,c,d`','`d`'),('`b,c`','`b,c,e`','`e`'),('`b,c`','`b,c,f`','`f`'),('`b,d,f`','`a,b,d,f`','`a`'),('`b,d,f`','`b,c,d,f`','`c`'),('`b,d`','`a,b,d`','`a`'),('`b,d`','`b,c,d`','`c`'),('`b,d`','`b,d,f`','`f`'),('`b,f`','`a,b,f`','`a`'),('`b,f`','`b,c,f`','`c`'),('`b,f`','`b,d,f`','`d`'),('`b`','`a,b`','`a`'),('`b`','`b,c`','`c`'),('`b`','`b,d`','`d`'),('`b`','`b,f`','`f`'),('`c,d,e,f`','`a,c,d,e,f`','`a`'),('`c,d,e,f`','`b,c,d,e,f`','`b`'),('`c,d,e`','`a,c,d,e`','`a`'),('`c,d,e`','`b,c,d,e`','`b`'),('`c,d,e`','`c,d,e,f`','`f`'),('`c,d,f`','`a,c,d,f`','`a`'),('`c,d,f`','`b,c,d,f`','`b`'),('`c,d,f`','`c,d,e,f`','`e`'),('`c,d`','`a,c,d`','`a`'),('`c,d`','`b,c,d`','`b`'),('`c,d`','`c,d,e`','`e`'),('`c,d`','`c,d,f`','`f`'),('`c,e`','`a,c,e`','`a`'),('`c,e`','`b,c,e`','`b`'),('`c,e`','`c,d,e`','`d`'),('`c`','`a,c`','`a`'),('`c`','`b,c`','`b`'),('`c`','`c,d`','`d`'),('`c`','`c,e`','`e`'),('`d,f`','`a,d,f`','`a`'),('`d,f`','`b,d,f`','`b`'),('`d,f`','`c,d,f`','`c`'),('`d`','`a,d`','`a`'),('`d`','`b,d`','`b`'),('`d`','`c,d`','`c`'),('`d`','`d,f`','`f`'),('`e`','`a,e`','`a`'),('`e`','`c,e`','`c`'),('`f`','`b,f`','`b`'),('`f`','`d,f`','`d`');
/*!40000 ALTER TABLE `lattice_rel` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:54
