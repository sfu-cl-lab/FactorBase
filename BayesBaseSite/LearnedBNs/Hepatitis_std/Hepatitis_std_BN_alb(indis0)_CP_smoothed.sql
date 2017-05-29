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
-- Table structure for table `alb(indis0)_CP_smoothed`
--

DROP TABLE IF EXISTS `alb(indis0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alb(indis0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL,
  `tp(indis0)` varchar(45) DEFAULT NULL,
  `ztt(indis0)` varchar(45) DEFAULT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alb(indis0)_CP_smoothed`
--

LOCK TABLES `alb(indis0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `alb(indis0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `alb(indis0)_CP_smoothed` VALUES ('10','0','0','0',14,0.714286,NULL),('154','0','0','1',169,0.911243,NULL),('43','0','0','2',44,0.977273,NULL),('164','0','0','3',165,0.993939,NULL),('49','0','0','4',50,0.980000,NULL),('102','0','0','5',103,0.990291,NULL),('2','0','1','0',6,0.333333,NULL),('207','0','1','1',475,0.435789,NULL),('158','0','1','2',274,0.576642,NULL),('420','0','1','3',514,0.817121,NULL),('325','0','1','4',359,0.905292,NULL),('703','0','1','5',713,0.985975,NULL),('2','0','2','0',3,0.666667,NULL),('43','0','2','1',188,0.228723,NULL),('35','0','2','2',127,0.275591,NULL),('147','0','2','3',475,0.309474,NULL),('275','0','2','4',314,0.875796,NULL),('594','0','2','5',618,0.961165,NULL),('10','0','3','0',89,0.112360,NULL),('5','0','3','1',27,0.185185,NULL),('2','0','3','2',65,0.030769,NULL),('10','0','3','3',173,0.057803,NULL),('59','0','3','4',238,0.247899,NULL),('656','0','3','5',802,0.817955,NULL),('4','1','0','0',14,0.285714,NULL),('15','1','0','1',169,0.088757,NULL),('4','1','1','0',6,0.666667,NULL),('268','1','1','1',475,0.564211,NULL),('116','1','1','2',274,0.423358,NULL),('94','1','1','3',514,0.182879,NULL),('34','1','1','4',359,0.094708,NULL),('10','1','1','5',713,0.014025,NULL),('145','1','2','1',188,0.771277,NULL),('92','1','2','2',127,0.724409,NULL),('328','1','2','3',475,0.690526,NULL),('39','1','2','4',314,0.124204,NULL),('24','1','2','5',618,0.038835,NULL),('79','1','3','0',89,0.887640,NULL),('22','1','3','1',27,0.814815,NULL),('63','1','3','2',65,0.969231,NULL),('163','1','3','3',173,0.942197,NULL),('179','1','3','4',238,0.752101,NULL),('146','1','3','5',802,0.182045,NULL),('1','1','0','2',44,0.022727,NULL),('1','1','0','3',165,0.006061,NULL),('1','1','0','4',50,0.020000,NULL),('1','1','0','5',103,0.009709,NULL),('1','1','2','0',3,0.333333,NULL);
/*!40000 ALTER TABLE `alb(indis0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:21
