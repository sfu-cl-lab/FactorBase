CREATE DATABASE  IF NOT EXISTS `Hepatitis_std` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Hepatitis_std`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Hepatitis_std
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
-- Table structure for table `Bio`
--

DROP TABLE IF EXISTS `Bio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Bio` (
  `fibros` varchar(45) NOT NULL,
  `activity` varchar(45) NOT NULL,
  `b_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`b_id`),
  KEY `Hepatitis_fibros` (`fibros`),
  KEY `Hepatitis_activity` (`activity`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=latin1 AVG_ROW_LENGTH=512;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Bio`
--

LOCK TABLES `Bio` WRITE;
/*!40000 ALTER TABLE `Bio` DISABLE KEYS */;
INSERT INTO `Bio` VALUES ('1','1',1),('2','2',2),('3','3',3),('1','0',4),('1','1',5),('1','2',6),('4','3',7),('1','2',8),('3','2',9),('2','2',10),('4','1',11),('3','3',12),('4','2',13),('3','2',14),('4','2',15),('2','1',16),('4','3',17),('2','3',18),('0','1',19),('0','0',20),('2','1',21),('3','4',22),('4','4',23),('1','4',24),('4','1',25),('2','4',26),('3','1',27),('3','1',28),('2','3',29),('1','3',30),('0','4',31),('1','3',32);
/*!40000 ALTER TABLE `Bio` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 14:59:47
