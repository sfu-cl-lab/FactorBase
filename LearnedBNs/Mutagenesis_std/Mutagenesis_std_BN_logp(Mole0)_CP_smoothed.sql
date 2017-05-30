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
-- Table structure for table `logp(Mole0)_CP_smoothed`
--

DROP TABLE IF EXISTS `logp(Mole0)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logp(Mole0)_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `ind1(Mole0)` varchar(45) default NULL,
  `label(Mole0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logp(Mole0)_CP_smoothed`
--

LOCK TABLES `logp(Mole0)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `logp(Mole0)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `logp(Mole0)_CP_smoothed` VALUES ('14680','0','0','2',288697,0.050851,NULL),('14680','1','0','1',127228,0.115383,NULL),('24466','1','0','2',288697,0.084746,NULL),('24466','2','0','1',127228,0.192300,NULL),('127219','2','0','2',288697,0.440666,NULL),('24466','2','1','1',469738,0.052084,NULL),('53824','3','0','1',127228,0.423052,NULL),('78289','3','0','2',288697,0.271181,NULL),('53824','3','1','1',469738,0.114583,NULL),('29359','4','0','1',127228,0.230759,NULL),('29359','4','0','2',288697,0.101695,NULL),('63610','4','1','1',469738,0.135416,NULL),('4894','4','1','2',19582,0.249923,NULL),('4894','5','0','1',127228,0.038466,NULL),('14680','5','0','2',288697,0.050849,NULL),('176149','5','1','1',469738,0.374994,NULL),('68503','6','1','1',469738,0.145832,NULL),('68503','7','1','1',469738,0.145832,NULL),('14680','8','1','1',469738,0.031251,NULL),('4894','8','1','2',19582,0.249923,NULL),('9787','9','1','2',19582,0.499796,NULL),('1','0','0','1',127228,0.000008,NULL),('1','0','1','2',19582,0.000052,NULL),('1','0','1','1',469738,0.000004,NULL),('1','1','1','2',19582,0.000051,NULL),('1','1','1','1',469738,0.000002,NULL),('1','2','1','2',19582,0.000051,NULL),('1','3','1','2',19582,0.000051,NULL),('1','5','1','2',19582,0.000051,NULL),('1','6','0','2',288697,0.000003,NULL),('1','6','0','1',127228,0.000008,NULL),('1','6','1','2',19582,0.000051,NULL),('1','7','0','2',288697,0.000003,NULL),('1','7','0','1',127228,0.000008,NULL),('1','7','1','2',19582,0.000051,NULL),('1','8','0','2',288697,0.000003,NULL),('1','8','0','1',127228,0.000008,NULL),('1','9','0','2',288697,0.000003,NULL),('1','9','0','1',127228,0.000008,NULL),('1','9','1','1',469738,0.000002,NULL);
/*!40000 ALTER TABLE `logp(Mole0)_CP_smoothed` ENABLE KEYS */;
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
