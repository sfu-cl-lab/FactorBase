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
-- Table structure for table `che(indis0)_CP`
--

DROP TABLE IF EXISTS `che(indis0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `che(indis0)_CP` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` varchar(45) DEFAULT NULL,
  `alb(indis0)` varchar(45) DEFAULT NULL,
  `got(indis0)` varchar(10) DEFAULT NULL,
  `Type(dispat0)` varchar(45) DEFAULT NULL COMMENT 'Type: Categorical; Aggr: B, C',
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `che(indis0)_CP`
--

LOCK TABLES `che(indis0)_CP` WRITE;
/*!40000 ALTER TABLE `che(indis0)_CP` DISABLE KEYS */;
INSERT INTO `che(indis0)_CP` VALUES ('1','0','0','3','1',1532,0.000653,-7.33393),('10','0','1','2','1',510,0.019608,-3.93182),('1','1','0','0','1',432,0.002315,-6.06835),('12','1','0','1','1',989,0.012133,-4.41183),('10','1','0','2','1',1197,0.008354,-4.78501),('3','1','0','3','1',1532,0.001958,-6.23583),('1','1','1','0','1',281,0.003559,-5.63828),('16','1','1','1','1',545,0.029358,-3.52819),('14','1','1','2','1',510,0.027451,-3.59535),('28','1','1','3','1',466,0.060086,-2.81198),('4','3','0','3','1',1532,0.002611,-5.94802),('1','4','1','2','1',510,0.001961,-6.2343),('9','4','1','3','1',466,0.019313,-3.94698),('1','5','0','0','1',432,0.002315,-6.06835),('4','5','0','1','1',989,0.004044,-5.51052),('3','5','0','2','1',1197,0.002506,-5.98907),('235','5','0','3','1',1532,0.153394,-1.87475),('13','5','1','1','1',545,0.023853,-3.73585),('130','5','1','2','1',510,0.254902,-1.36688),('158','5','1','3','1',466,0.339056,-1.08159),('4','5','1','4','1',4,1.000000,0),('411','6','0','0','1',432,0.951389,-0.0498322),('820','6','0','1','1',989,0.829120,-0.18739),('1028','6','0','2','1',1197,0.858814,-0.152203),('1249','6','0','3','1',1532,0.815274,-0.204231),('1','6','0','4','1',1,1.000000,0),('265','6','1','0','1',281,0.943060,-0.0586254),('393','6','1','1','1',545,0.721101,-0.326976),('230','6','1','2','1',510,0.450980,-0.796332),('213','6','1','3','1',466,0.457082,-0.782892),('18','7','0','0','1',432,0.041667,-3.17805),('143','7','0','1','1',989,0.144590,-1.93385),('149','7','0','2','1',1197,0.124478,-2.08363),('35','7','0','3','1',1532,0.022846,-3.77898),('15','7','1','0','1',281,0.053381,-2.9303),('104','7','1','1','1',545,0.190826,-1.65639),('110','7','1','2','1',510,0.215686,-1.53393),('53','7','1','3','1',466,0.113734,-2.17389),('1','8','0','0','1',432,0.002315,-6.06835),('10','8','0','1','1',989,0.010111,-4.59413),('7','8','0','2','1',1197,0.005848,-5.14166),('5','8','0','3','1',1532,0.003264,-5.7248),('19','8','1','1','1',545,0.034862,-3.35636),('15','8','1','2','1',510,0.029412,-3.52635),('4','8','1','3','1',466,0.008584,-4.75786),('1','9','1','3','1',466,0.002146,-6.14415);
/*!40000 ALTER TABLE `che(indis0)_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:20
