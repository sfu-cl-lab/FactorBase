CREATE DATABASE  IF NOT EXISTS `Hepatitis_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Hepatitis_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Hepatitis_std_BN
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
-- Table structure for table `RChain_pvars`
--

DROP TABLE IF EXISTS `RChain_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RChain_pvars` (
  `rchain` varchar(256) NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RChain_pvars`
--

LOCK TABLES `RChain_pvars` WRITE;
/*!40000 ALTER TABLE `RChain_pvars` DISABLE KEYS */;
INSERT INTO `RChain_pvars` VALUES ('`a,b,c`','Bio0'),('`a,b,c`','dispat0'),('`a,b,c`','indis0'),('`a,b,c`','inf0'),('`a,b`','Bio0'),('`a,b`','dispat0'),('`a,b`','indis0'),('`a,c`','Bio0'),('`a,c`','dispat0'),('`a,c`','inf0'),('`a`','Bio0'),('`a`','dispat0'),('`b,c`','indis0'),('`b,c`','dispat0'),('`b,c`','inf0'),('`b`','indis0'),('`b`','dispat0'),('`c`','inf0'),('`c`','dispat0');
/*!40000 ALTER TABLE `RChain_pvars` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:18
