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
-- Table structure for table `lattice_membership`
--

DROP TABLE IF EXISTS `lattice_membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_membership` (
  `name` varchar(256) NOT NULL DEFAULT '',
  `member` varchar(256) NOT NULL DEFAULT '',
  PRIMARY KEY (`name`,`member`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_membership`
--

LOCK TABLES `lattice_membership` WRITE;
/*!40000 ALTER TABLE `lattice_membership` DISABLE KEYS */;
INSERT INTO `lattice_membership` VALUES ('`a,b,c,d,e,f`','`a`'),('`a,b,c,d,e,f`','`b`'),('`a,b,c,d,e,f`','`c`'),('`a,b,c,d,e,f`','`d`'),('`a,b,c,d,e,f`','`e`'),('`a,b,c,d,e,f`','`f`'),('`a,b,c,d,e`','`a`'),('`a,b,c,d,e`','`b`'),('`a,b,c,d,e`','`c`'),('`a,b,c,d,e`','`d`'),('`a,b,c,d,e`','`e`'),('`a,b,c,d,f`','`a`'),('`a,b,c,d,f`','`b`'),('`a,b,c,d,f`','`c`'),('`a,b,c,d,f`','`d`'),('`a,b,c,d,f`','`f`'),('`a,b,c,d`','`a`'),('`a,b,c,d`','`b`'),('`a,b,c,d`','`c`'),('`a,b,c,d`','`d`'),('`a,b,c,e,f`','`a`'),('`a,b,c,e,f`','`b`'),('`a,b,c,e,f`','`c`'),('`a,b,c,e,f`','`e`'),('`a,b,c,e,f`','`f`'),('`a,b,c,e`','`a`'),('`a,b,c,e`','`b`'),('`a,b,c,e`','`c`'),('`a,b,c,e`','`e`'),('`a,b,c,f`','`a`'),('`a,b,c,f`','`b`'),('`a,b,c,f`','`c`'),('`a,b,c,f`','`f`'),('`a,b,c`','`a`'),('`a,b,c`','`b`'),('`a,b,c`','`c`'),('`a,b,d,e,f`','`a`'),('`a,b,d,e,f`','`b`'),('`a,b,d,e,f`','`d`'),('`a,b,d,e,f`','`e`'),('`a,b,d,e,f`','`f`'),('`a,b,d,e`','`a`'),('`a,b,d,e`','`b`'),('`a,b,d,e`','`d`'),('`a,b,d,e`','`e`'),('`a,b,d,f`','`a`'),('`a,b,d,f`','`b`'),('`a,b,d,f`','`d`'),('`a,b,d,f`','`f`'),('`a,b,d`','`a`'),('`a,b,d`','`b`'),('`a,b,d`','`d`'),('`a,b,e,f`','`a`'),('`a,b,e,f`','`b`'),('`a,b,e,f`','`e`'),('`a,b,e,f`','`f`'),('`a,b,e`','`a`'),('`a,b,e`','`b`'),('`a,b,e`','`e`'),('`a,b,f`','`a`'),('`a,b,f`','`b`'),('`a,b,f`','`f`'),('`a,b`','`a`'),('`a,b`','`b`'),('`a,c,d,e,f`','`a`'),('`a,c,d,e,f`','`c`'),('`a,c,d,e,f`','`d`'),('`a,c,d,e,f`','`e`'),('`a,c,d,e,f`','`f`'),('`a,c,d,e`','`a`'),('`a,c,d,e`','`c`'),('`a,c,d,e`','`d`'),('`a,c,d,e`','`e`'),('`a,c,d,f`','`a`'),('`a,c,d,f`','`c`'),('`a,c,d,f`','`d`'),('`a,c,d,f`','`f`'),('`a,c,d`','`a`'),('`a,c,d`','`c`'),('`a,c,d`','`d`'),('`a,c,e`','`a`'),('`a,c,e`','`c`'),('`a,c,e`','`e`'),('`a,c`','`a`'),('`a,c`','`c`'),('`a,d,e,f`','`a`'),('`a,d,e,f`','`d`'),('`a,d,e,f`','`e`'),('`a,d,e,f`','`f`'),('`a,d,e`','`a`'),('`a,d,e`','`d`'),('`a,d,e`','`e`'),('`a,d,f`','`a`'),('`a,d,f`','`d`'),('`a,d,f`','`f`'),('`a,d`','`a`'),('`a,d`','`d`'),('`a,e`','`a`'),('`a,e`','`e`'),('`a`','`a`'),('`b,c,d,e,f`','`b`'),('`b,c,d,e,f`','`c`'),('`b,c,d,e,f`','`d`'),('`b,c,d,e,f`','`e`'),('`b,c,d,e,f`','`f`'),('`b,c,d,e`','`b`'),('`b,c,d,e`','`c`'),('`b,c,d,e`','`d`'),('`b,c,d,e`','`e`'),('`b,c,d,f`','`b`'),('`b,c,d,f`','`c`'),('`b,c,d,f`','`d`'),('`b,c,d,f`','`f`'),('`b,c,d`','`b`'),('`b,c,d`','`c`'),('`b,c,d`','`d`'),('`b,c,e,f`','`b`'),('`b,c,e,f`','`c`'),('`b,c,e,f`','`e`'),('`b,c,e,f`','`f`'),('`b,c,e`','`b`'),('`b,c,e`','`c`'),('`b,c,e`','`e`'),('`b,c,f`','`b`'),('`b,c,f`','`c`'),('`b,c,f`','`f`'),('`b,c`','`b`'),('`b,c`','`c`'),('`b,d,f`','`b`'),('`b,d,f`','`d`'),('`b,d,f`','`f`'),('`b,d`','`b`'),('`b,d`','`d`'),('`b,f`','`b`'),('`b,f`','`f`'),('`b`','`b`'),('`c,d,e,f`','`c`'),('`c,d,e,f`','`d`'),('`c,d,e,f`','`e`'),('`c,d,e,f`','`f`'),('`c,d,e`','`c`'),('`c,d,e`','`d`'),('`c,d,e`','`e`'),('`c,d,f`','`c`'),('`c,d,f`','`d`'),('`c,d,f`','`f`'),('`c,d`','`c`'),('`c,d`','`d`'),('`c,e`','`c`'),('`c,e`','`e`'),('`c`','`c`'),('`d,f`','`d`'),('`d,f`','`f`'),('`d`','`d`'),('`e`','`e`'),('`f`','`f`');
/*!40000 ALTER TABLE `lattice_membership` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:55
