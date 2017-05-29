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
-- Table structure for table `Attribute_Value`
--

DROP TABLE IF EXISTS `Attribute_Value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Attribute_Value` (
  `COLUMN_NAME` varchar(30) DEFAULT NULL,
  `VALUE` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Attribute_Value`
--

LOCK TABLES `Attribute_Value` WRITE;
/*!40000 ALTER TABLE `Attribute_Value` DISABLE KEYS */;
INSERT INTO `Attribute_Value` VALUES ('activity','0'),('activity','1'),('activity','2'),('activity','3'),('activity','4'),('fibros','0'),('fibros','1'),('fibros','2'),('fibros','3'),('fibros','4'),('age','0'),('age','1'),('age','2'),('age','3'),('age','4'),('age','5'),('age','6'),('sex','0'),('sex','1'),('Type','0'),('Type','1'),('alb','0'),('alb','1'),('che','0'),('che','1'),('che','2'),('che','3'),('che','4'),('che','5'),('che','6'),('che','7'),('che','8'),('che','9'),('dbil','0'),('dbil','1'),('got','0'),('got','1'),('got','2'),('got','3'),('got','4'),('gpt','0'),('gpt','1'),('gpt','2'),('gpt','3'),('tbil','0'),('tbil','1'),('tcho','0'),('tcho','1'),('tcho','2'),('tcho','3'),('tp','0'),('tp','1'),('tp','2'),('tp','3'),('ttt','0'),('ttt','1'),('ttt','2'),('ttt','3'),('ttt','4'),('ttt','5'),('ztt','0'),('ztt','1'),('ztt','2'),('ztt','3'),('ztt','4'),('ztt','5'),('dur','0'),('dur','1'),('dur','2'),('dur','3'),('dur','4'),('`a`','True'),('`a`','False'),('`b`','True'),('`b`','False'),('`c`','True'),('`c`','False');
/*!40000 ALTER TABLE `Attribute_Value` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:22
