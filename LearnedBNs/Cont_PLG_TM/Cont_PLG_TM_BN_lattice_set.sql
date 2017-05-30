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
-- Table structure for table `lattice_set`
--

DROP TABLE IF EXISTS `lattice_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_set` (
  `name` varchar(256) NOT NULL DEFAULT '',
  `length` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`,`length`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_set`
--

LOCK TABLES `lattice_set` WRITE;
/*!40000 ALTER TABLE `lattice_set` DISABLE KEYS */;
INSERT INTO `lattice_set` VALUES ('`a,b,c,d,e,f`',6),('`a,b,c,d,e`',5),('`a,b,c,d,f`',5),('`a,b,c,d`',4),('`a,b,c,e,f`',5),('`a,b,c,e`',4),('`a,b,c,f`',4),('`a,b,c`',3),('`a,b,d,e,f`',5),('`a,b,d,e`',4),('`a,b,d,f`',4),('`a,b,d`',3),('`a,b,e,f`',4),('`a,b,e`',3),('`a,b,f`',3),('`a,b`',2),('`a,c,d,e,f`',5),('`a,c,d,e`',4),('`a,c,d,f`',4),('`a,c,d`',3),('`a,c,e`',3),('`a,c`',2),('`a,d,e,f`',4),('`a,d,e`',3),('`a,d,f`',3),('`a,d`',2),('`a,e`',2),('`a`',1),('`b,c,d,e,f`',5),('`b,c,d,e`',4),('`b,c,d,f`',4),('`b,c,d`',3),('`b,c,e,f`',4),('`b,c,e`',3),('`b,c,f`',3),('`b,c`',2),('`b,d,f`',3),('`b,d`',2),('`b,f`',2),('`b`',1),('`c,d,e,f`',4),('`c,d,e`',3),('`c,d,f`',3),('`c,d`',2),('`c,e`',2),('`c`',1),('`d,f`',2),('`d`',1),('`e`',1),('`f`',1);
/*!40000 ALTER TABLE `lattice_set` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:53
