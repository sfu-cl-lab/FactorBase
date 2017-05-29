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
-- Table structure for table `lumo(Mole0)_CP_smoothed`
--

DROP TABLE IF EXISTS `lumo(Mole0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lumo(Mole0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `inda(Mole0)` varchar(45) default NULL,
  `label(Mole0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lumo(Mole0)_CP_smoothed`
--

LOCK TABLES `lumo(Mole0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `lumo(Mole0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `lumo(Mole0)_CP_smoothed` VALUES ('9787','1','0','1',572490,0.017094,NULL),('29359','2','0','1',572490,0.051283,NULL),('34252','3','0','1',572490,0.059830,NULL),('117433','4','0','1',572490,0.205127,NULL),('4894','4','1','1',24474,0.199967,NULL),('78289','5','0','1',572490,0.136752,NULL),('24466','5','0','2',308268,0.079366,NULL),('19573','5','1','1',24474,0.799747,NULL),('161470','6','0','1',572490,0.282049,NULL),('58717','6','0','2',308268,0.190474,NULL),('122326','7','0','1',572490,0.213674,NULL),('107647','7','0','2',308268,0.349199,NULL),('19573','8','0','1',572490,0.034189,NULL),('97861','8','0','2',308268,0.317454,NULL),('19573','9','0','2',308268,0.063493,NULL),('1','1','0','2',308268,0.000005,NULL),('1','1','1','1',24474,0.000040,NULL),('1','1','1','2',9,0.111112,NULL),('1','2','0','2',308268,0.000003,NULL),('1','2','1','1',24474,0.000041,NULL),('1','2','1','2',9,0.111111,NULL),('1','3','0','2',308268,0.000003,NULL),('1','3','1','1',24474,0.000041,NULL),('1','3','1','2',9,0.111111,NULL),('1','4','0','2',308268,0.000003,NULL),('1','4','1','2',9,0.111111,NULL),('1','5','1','2',9,0.111111,NULL),('1','6','1','1',24474,0.000041,NULL),('1','6','1','2',9,0.111111,NULL),('1','7','1','1',24474,0.000041,NULL),('1','7','1','2',9,0.111111,NULL),('1','8','1','1',24474,0.000041,NULL),('1','8','1','2',9,0.111111,NULL),('1','9','0','1',572490,0.000002,NULL),('1','9','1','1',24474,0.000041,NULL),('1','9','1','2',9,0.111111,NULL);
/*!40000 ALTER TABLE `lumo(Mole0)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:18
