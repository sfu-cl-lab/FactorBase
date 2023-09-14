CREATE DATABASE  IF NOT EXISTS `unielwin` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `unielwin`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: unielwin
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
-- Table structure for table `RA`
--

DROP TABLE IF EXISTS `RA`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RA` (
  `capability` varchar(45) default NULL,
  `prof_id` int(11) NOT NULL default '0',
  `student_id` int(11) NOT NULL default '0',
  `salary` varchar(45) default NULL,
  PRIMARY KEY  (`prof_id`,`student_id`),
  KEY `FK_u2base_1` (`student_id`),
  KEY `FK_u2base_2` (`prof_id`),
  KEY `RA_capability` USING HASH (`capability`),
  KEY `RA_salary` USING HASH (`salary`),
  CONSTRAINT `FK_RA_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_RA_2` FOREIGN KEY (`prof_id`) REFERENCES `prof` (`prof_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RA`
--

LOCK TABLES `RA` WRITE;
/*!40000 ALTER TABLE `RA` DISABLE KEYS */;
INSERT INTO `RA` VALUES ('3',4,17,'med'),('1',5,5,'low'),('2',5,14,'low'),('3',5,18,'high'),('4',5,26,'high'),('3',5,27,'med'),('3',5,28,'med'),('2',6,5,'low'),('4',6,8,'high'),('4',6,9,'high'),('1',6,15,'med'),('2',6,16,'med'),('5',6,23,'high'),('5',6,24,'high'),('5',6,25,'high'),('4',7,7,'high'),('4',7,8,'high'),('1',7,11,'med'),('5',7,19,'high'),('1',7,20,'low'),('3',7,22,'med'),('1',8,12,'med'),('2',8,13,'med'),('3',8,21,'low'),('3',9,10,'high');
/*!40000 ALTER TABLE `RA` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-29 15:40:01
