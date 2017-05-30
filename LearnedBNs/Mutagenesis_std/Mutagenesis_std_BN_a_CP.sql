CREATE DATABASE  IF NOT EXISTS `Mutagenesis_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Mutagenesis_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: Mutagenesis_std_BN
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
-- Table structure for table `a_CP`
--

DROP TABLE IF EXISTS `a_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `a_CP` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(5) default NULL,
  `atype(Atom0)` varchar(45) default NULL,
  `elem(Atom0)` char(1) default NULL,
  `ind1(Mole0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `a_CP`
--

LOCK TABLES `a_CP` WRITE;
/*!40000 ALTER TABLE `a_CP` DISABLE KEYS */;
INSERT INTO `a_CP` VALUES ('169','F','0','b','0',170,0.994118,-0.00589939),('199','F','0','b','1',200,0.995000,-0.00501254),('8898','F','0','c','0',8925,0.996975,-0.00302958),('10425','F','0','c','1',10500,0.992857,-0.00716865),('129223','F','0','h','0',129795,0.995593,-0.00441673),('151777','F','0','h','1',152700,0.993955,-0.00606333),('147511','F','1','c','0',148155,0.995653,-0.0043565),('173248','F','1','c','1',174300,0.993964,-0.00605427),('48266','F','2','c','0',48365,0.997953,-0.0020491),('56450','F','2','c','1',56900,0.992091,-0.00794044),('1099','F','2','f','0',1105,0.994570,-0.00544478),('1295','F','2','f','1',1300,0.996154,-0.0038534),('85','F','2','i','0',85,1.000000,0),('99','F','2','i','1',100,0.990000,-0.0100503),('29155','F','2','n','0',29325,0.994203,-0.0058139),('34346','F','2','n','1',34500,0.995536,-0.00447396),('49759','F','2','o','0',49980,0.995578,-0.00443181),('58544','F','2','o','1',58800,0.995646,-0.00436351),('1','T','0','b','0',170,0.005882,-5.13586),('1','T','0','b','1',200,0.005000,-5.29832),('27','T','0','c','0',8925,0.003025,-5.80084),('75','T','0','c','1',10500,0.007143,-4.94162),('572','T','0','h','0',129795,0.004407,-5.42456),('923','T','0','h','1',152700,0.006045,-5.10852),('644','T','1','c','0',148155,0.004347,-5.43827),('1052','T','1','c','1',174300,0.006036,-5.11001),('99','T','2','c','0',48365,0.002047,-6.19138),('450','T','2','c','1',56900,0.007909,-4.83975),('6','T','2','f','0',1105,0.005430,-5.21582),('5','T','2','f','1',1300,0.003846,-5.56072),('1','T','2','i','1',100,0.010000,-4.60517),('170','T','2','n','0',29325,0.005797,-5.15041),('154','T','2','n','1',34500,0.004464,-5.41171),('221','T','2','o','0',49980,0.004422,-5.42116),('256','T','2','o','1',58800,0.004354,-5.43666);
/*!40000 ALTER TABLE `a_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:15
