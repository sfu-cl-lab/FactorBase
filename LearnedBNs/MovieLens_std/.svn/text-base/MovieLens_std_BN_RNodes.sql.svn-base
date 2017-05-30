CREATE DATABASE  IF NOT EXISTS `MovieLens_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `MovieLens_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: MovieLens_std_BN
-- ------------------------------------------------------
-- Server version	5.0.95

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
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Table structure for table `RNodes`
--

DROP TABLE IF EXISTS `RNodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes` (
  `orig_rnid` varchar(199) character set utf8 NOT NULL default '',
  `TABLE_NAME` varchar(64) character set utf8 NOT NULL default '',
  `pvid1` varchar(65) character set utf8 NOT NULL default '',
  `pvid2` varchar(65) character set utf8 NOT NULL default '',
  `COLUMN_NAME1` varchar(64) character set utf8 NOT NULL default '',
  `COLUMN_NAME2` varchar(64) character set utf8 NOT NULL default '',
  `main` int(11) NOT NULL default '0',
  `rnid` varchar(10) default NULL,
  KEY `Index` (`pvid1`,`pvid2`,`TABLE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes`
--

LOCK TABLES `RNodes` WRITE;
/*!40000 ALTER TABLE `RNodes` DISABLE KEYS */;
INSERT INTO `RNodes` VALUES ('`u2base(User0,item20)`','u2base','User0','item20','user_id','item_id',1,'`a`');
/*!40000 ALTER TABLE `RNodes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:15
