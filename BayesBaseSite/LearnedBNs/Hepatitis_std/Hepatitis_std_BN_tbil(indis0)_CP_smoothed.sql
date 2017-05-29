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
-- Table structure for table `tbil(indis0)_CP_smoothed`
--

DROP TABLE IF EXISTS `tbil(indis0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbil(indis0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL,
  `dbil(indis0)` varchar(45) DEFAULT NULL,
  `got(indis0)` varchar(10) DEFAULT NULL,
  `tcho(indis0)` varchar(45) DEFAULT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbil(indis0)_CP_smoothed`
--

LOCK TABLES `tbil(indis0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `tbil(indis0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `tbil(indis0)_CP_smoothed` VALUES ('251','0','0','0','0',256,0.980469,NULL),('15','0','0','0','1',214,0.070093,NULL),('22','0','0','0','2',137,0.160584,NULL),('5','0','0','0','3',17,0.294118,NULL),('36','0','0','1','0',53,0.679245,NULL),('187','0','0','1','1',632,0.295886,NULL),('80','0','0','1','2',406,0.197044,NULL),('17','0','0','1','3',91,0.186813,NULL),('29','0','0','2','0',152,0.190789,NULL),('179','0','0','2','1',667,0.268366,NULL),('56','0','0','2','2',281,0.199288,NULL),('3','0','0','2','3',45,0.066667,NULL),('40','0','0','3','0',164,0.243902,NULL),('217','0','0','3','1',539,0.402597,NULL),('2','0','0','3','2',178,0.011236,NULL),('3','0','1','1','0',42,0.071429,NULL),('2','0','1','2','1',322,0.006211,NULL),('5','1','0','0','0',256,0.019531,NULL),('199','1','0','0','1',214,0.929907,NULL),('115','1','0','0','2',137,0.839416,NULL),('12','1','0','0','3',17,0.705882,NULL),('17','1','0','1','0',53,0.320755,NULL),('445','1','0','1','1',632,0.704114,NULL),('326','1','0','1','2',406,0.802956,NULL),('74','1','0','1','3',91,0.813187,NULL),('123','1','0','2','0',152,0.809211,NULL),('488','1','0','2','1',667,0.731634,NULL),('225','1','0','2','2',281,0.800712,NULL),('42','1','0','2','3',45,0.933333,NULL),('124','1','0','3','0',164,0.756098,NULL),('322','1','0','3','1',539,0.597403,NULL),('176','1','0','3','2',178,0.988764,NULL),('74','1','0','3','3',75,0.986667,NULL),('6','1','1','0','0',7,0.857143,NULL),('65','1','1','0','1',66,0.984848,NULL),('29','1','1','0','2',30,0.966667,NULL),('39','1','1','1','0',42,0.928571,NULL),('202','1','1','1','1',203,0.995074,NULL),('49','1','1','1','2',50,0.980000,NULL),('72','1','1','1','3',73,0.986301,NULL),('121','1','1','2','0',122,0.991803,NULL),('320','1','1','2','1',322,0.993789,NULL),('94','1','1','2','2',95,0.989474,NULL),('38','1','1','2','3',39,0.974359,NULL),('183','1','1','3','0',184,0.994565,NULL),('612','1','1','3','1',613,0.998369,NULL),('217','1','1','3','2',218,0.995413,NULL),('42','1','1','3','3',43,0.976744,NULL),('6','1','1','4','2',7,0.857143,NULL),('1','0','0','3','3',75,0.013333,NULL),('1','0','0','4','0',2,0.500000,NULL),('1','0','0','4','1',2,0.500000,NULL),('1','0','0','4','2',2,0.500000,NULL),('1','0','0','4','3',2,0.500000,NULL),('1','0','1','0','0',7,0.142857,NULL),('1','0','1','0','1',66,0.015152,NULL),('1','0','1','0','2',30,0.033333,NULL),('1','0','1','0','3',2,0.500000,NULL),('1','0','1','1','1',203,0.004926,NULL),('1','0','1','1','2',50,0.020000,NULL),('1','0','1','1','3',73,0.013699,NULL),('1','0','1','2','0',122,0.008197,NULL),('1','0','1','2','2',95,0.010526,NULL),('1','0','1','2','3',39,0.025641,NULL),('1','0','1','3','0',184,0.005435,NULL),('1','0','1','3','1',613,0.001631,NULL),('1','0','1','3','2',218,0.004587,NULL),('1','0','1','3','3',43,0.023256,NULL),('1','0','1','4','0',2,0.500000,NULL),('1','0','1','4','1',2,0.500000,NULL),('1','0','1','4','2',7,0.142857,NULL),('1','0','1','4','3',2,0.500000,NULL),('1','1','0','4','0',2,0.500000,NULL),('1','1','0','4','1',2,0.500000,NULL),('1','1','0','4','2',2,0.500000,NULL),('1','1','0','4','3',2,0.500000,NULL),('1','1','1','0','3',2,0.500000,NULL),('1','1','1','4','0',2,0.500000,NULL),('1','1','1','4','1',2,0.500000,NULL),('1','1','1','4','3',2,0.500000,NULL);
/*!40000 ALTER TABLE `tbil(indis0)_CP_smoothed` ENABLE KEYS */;
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
