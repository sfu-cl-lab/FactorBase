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
-- Table structure for table `gpt(indis0)_CP`
--

DROP TABLE IF EXISTS `gpt(indis0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gpt(indis0)_CP` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(10) DEFAULT NULL,
  `got(indis0)` varchar(10) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gpt(indis0)_CP`
--

LOCK TABLES `gpt(indis0)_CP` WRITE;
/*!40000 ALTER TABLE `gpt(indis0)_CP` DISABLE KEYS */;
INSERT INTO `gpt(indis0)_CP` VALUES ('501','0','0','1',713,0.702665,-0.352875),('123','0','1','1',1534,0.080183,-2.52344),('4','0','2','1',1707,0.002343,-6.05632),('211','1','0','1',713,0.295933,-1.21762),('1340','1','1','1',1534,0.873533,-0.135209),('688','1','2','1',1707,0.403046,-0.908705),('56','1','3','1',1998,0.028028,-3.57455),('1','2','0','1',713,0.001403,-6.56914),('71','2','1','1',1534,0.046284,-3.07296),('1015','2','2','1',1707,0.594610,-0.51985),('1870','2','3','1',1998,0.935936,-0.0662082),('72','3','3','1',1998,0.036036,-3.32324),('5','3','4','1',5,1.000000,0);
/*!40000 ALTER TABLE `gpt(indis0)_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:24
