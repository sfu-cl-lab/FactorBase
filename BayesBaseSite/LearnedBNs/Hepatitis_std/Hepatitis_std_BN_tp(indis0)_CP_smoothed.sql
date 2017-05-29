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
-- Table structure for table `tp(indis0)_CP_smoothed`
--

DROP TABLE IF EXISTS `tp(indis0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tp(indis0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL,
  `gpt(indis0)` varchar(10) DEFAULT NULL,
  `ztt(indis0)` varchar(45) DEFAULT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tp(indis0)_CP_smoothed`
--

LOCK TABLES `tp(indis0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `tp(indis0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `tp(indis0)_CP_smoothed` VALUES ('8','0','0','0',24,0.333333,NULL),('50','0','0','1',222,0.225224,NULL),('18','0','0','2',128,0.140626,NULL),('49','0','0','3',145,0.337931,NULL),('2','0','0','4',76,0.026316,NULL),('5','0','1','0',26,0.192307,NULL),('101','0','1','1',463,0.218143,NULL),('19','0','1','2',182,0.104395,NULL),('98','0','1','3',578,0.169550,NULL),('38','0','1','4',360,0.105556,NULL),('54','0','1','5',710,0.076057,NULL),('2','0','2','0',64,0.031250,NULL),('18','0','2','1',163,0.110429,NULL),('8','0','2','2',204,0.039216,NULL),('19','0','2','3',586,0.032423,NULL),('11','0','2','4',526,0.020912,NULL),('49','0','2','5',1438,0.034075,NULL),('2','0','3','1',19,0.105263,NULL),('2','1','0','0',24,0.083333,NULL),('128','1','0','1',222,0.576577,NULL),('108','1','0','2',128,0.843750,NULL),('87','1','0','3',145,0.600000,NULL),('56','1','0','4',76,0.736842,NULL),('29','1','0','5',57,0.508772,NULL),('3','1','1','0',26,0.115385,NULL),('252','1','1','1',463,0.544276,NULL),('96','1','1','2',182,0.527473,NULL),('252','1','1','3',578,0.435986,NULL),('142','1','1','4',360,0.394444,NULL),('347','1','1','5',710,0.488732,NULL),('2','1','2','0',64,0.031250,NULL),('90','1','2','1',163,0.552147,NULL),('71','1','2','2',204,0.348039,NULL),('173','1','2','3',586,0.295222,NULL),('161','1','2','4',526,0.306084,NULL),('336','1','2','5',1438,0.233658,NULL),('7','1','3','1',19,0.368421,NULL),('4','1','3','3',26,0.153846,NULL),('2','1','3','4',7,0.285714,NULL),('3','1','3','5',39,0.076923,NULL),('43','2','0','1',222,0.193694,NULL),('4','2','0','3',145,0.027586,NULL),('17','2','0','4',76,0.223684,NULL),('21','2','0','5',57,0.368421,NULL),('2','2','1','0',26,0.076923,NULL),('96','2','1','1',463,0.207343,NULL),('37','2','1','2',182,0.203297,NULL),('179','2','1','3',578,0.309689,NULL),('137','2','1','4',360,0.380556,NULL),('217','2','1','5',710,0.305634,NULL),('46','2','2','1',163,0.282209,NULL),('90','2','2','2',204,0.441176,NULL),('284','2','2','3',586,0.484642,NULL),('161','2','2','4',526,0.306084,NULL),('375','2','2','5',1438,0.260779,NULL),('5','2','3','1',19,0.263158,NULL),('10','2','3','3',26,0.384615,NULL),('7','2','3','5',39,0.179487,NULL),('13','3','0','0',24,0.541667,NULL),('5','3','0','3',145,0.034483,NULL),('6','3','0','5',57,0.105263,NULL),('16','3','1','0',26,0.615385,NULL),('14','3','1','1',463,0.030238,NULL),('30','3','1','2',182,0.164835,NULL),('49','3','1','3',578,0.084775,NULL),('43','3','1','4',360,0.119444,NULL),('92','3','1','5',710,0.129577,NULL),('59','3','2','0',64,0.921875,NULL),('9','3','2','1',163,0.055215,NULL),('35','3','2','2',204,0.171569,NULL),('110','3','2','3',586,0.187713,NULL),('193','3','2','4',526,0.366920,NULL),('678','3','2','5',1438,0.471488,NULL),('3','3','3','0',6,0.500000,NULL),('5','3','3','1',19,0.263158,NULL),('11','3','3','3',26,0.423077,NULL),('3','3','3','4',7,0.428571,NULL),('28','3','3','5',39,0.717949,NULL),('1','0','0','5',57,0.017544,NULL),('1','0','3','0',6,0.166666,NULL),('1','0','3','2',4,0.250000,NULL),('1','0','3','3',26,0.038462,NULL),('1','0','3','4',7,0.142858,NULL),('1','0','3','5',39,0.025641,NULL),('1','1','3','0',6,0.166667,NULL),('1','1','3','2',4,0.250000,NULL),('1','2','0','0',24,0.041667,NULL),('1','2','0','2',128,0.007812,NULL),('1','2','2','0',64,0.015625,NULL),('1','2','3','0',6,0.166667,NULL),('1','2','3','2',4,0.250000,NULL),('1','2','3','4',7,0.142857,NULL),('1','3','0','1',222,0.004505,NULL),('1','3','0','2',128,0.007812,NULL),('1','3','0','4',76,0.013158,NULL),('1','3','3','2',4,0.250000,NULL);
/*!40000 ALTER TABLE `tp(indis0)_CP_smoothed` ENABLE KEYS */;
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
