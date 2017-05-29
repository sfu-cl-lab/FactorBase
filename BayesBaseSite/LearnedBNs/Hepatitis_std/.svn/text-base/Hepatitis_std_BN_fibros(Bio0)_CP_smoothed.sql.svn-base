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
-- Table structure for table `fibros(Bio0)_CP_smoothed`
--

DROP TABLE IF EXISTS `fibros(Bio0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fibros(Bio0)_CP_smoothed` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) NOT NULL,
  `tbil(indis0)` varchar(45) DEFAULT NULL,
  `tcho(indis0)` varchar(45) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fibros(Bio0)_CP_smoothed`
--

LOCK TABLES `fibros(Bio0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `fibros(Bio0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `fibros(Bio0)_CP_smoothed` VALUES ('9','0','0','0','1',359,0.025070,NULL),('70','0','0','1','1',600,0.116667,NULL),('10','0','0','2','1',161,0.062112,NULL),('2','0','0','3','1',27,0.074075,NULL),('35','0','1','0','1',615,0.056911,NULL),('230','0','1','1','1',2650,0.086792,NULL),('79','0','1','2','1',1233,0.064072,NULL),('7','0','1','3','1',352,0.019886,NULL),('86','1','0','0','1',359,0.239554,NULL),('338','1','0','1','1',600,0.563333,NULL),('57','1','0','2','1',161,0.354037,NULL),('7','1','0','3','1',27,0.259259,NULL),('191','1','1','0','1',615,0.310569,NULL),('1197','1','1','1','1',2650,0.451698,NULL),('566','1','1','2','1',1233,0.459043,NULL),('170','1','1','3','1',352,0.482955,NULL),('74','2','0','0','1',359,0.206128,NULL),('67','2','0','1','1',600,0.111667,NULL),('21','2','0','2','1',161,0.130435,NULL),('6','2','0','3','1',27,0.222222,NULL),('87','2','1','0','1',615,0.141463,NULL),('529','2','1','1','1',2650,0.199623,NULL),('270','2','1','2','1',1233,0.218978,NULL),('71','2','1','3','1',352,0.201705,NULL),('128','3','0','0','1',359,0.356546,NULL),('78','3','0','1','1',600,0.130000,NULL),('30','3','0','2','1',161,0.186335,NULL),('115','3','1','0','1',615,0.186992,NULL),('405','3','1','1','1',2650,0.152830,NULL),('144','3','1','2','1',1233,0.116788,NULL),('31','3','1','3','1',352,0.088068,NULL),('62','4','0','0','1',359,0.172702,NULL),('47','4','0','1','1',600,0.078333,NULL),('43','4','0','2','1',161,0.267081,NULL),('11','4','0','3','1',27,0.407407,NULL),('187','4','1','0','1',615,0.304065,NULL),('289','4','1','1','1',2650,0.109057,NULL),('174','4','1','2','1',1233,0.141119,NULL),('73','4','1','3','1',352,0.207386,NULL),('1','3','0','3','1',27,0.037037,NULL);
/*!40000 ALTER TABLE `fibros(Bio0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:19
