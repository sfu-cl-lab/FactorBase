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
-- Table structure for table `tbil(indis0)_CP`
--

DROP TABLE IF EXISTS `tbil(indis0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbil(indis0)_CP` (
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
-- Dumping data for table `tbil(indis0)_CP`
--

LOCK TABLES `tbil(indis0)_CP` WRITE;
/*!40000 ALTER TABLE `tbil(indis0)_CP` DISABLE KEYS */;
INSERT INTO `tbil(indis0)_CP` VALUES ('250','0','0','0','0',254,0.984252,-0.0158733),('14','0','0','0','1',212,0.066038,-2.71753),('21','0','0','0','2',135,0.155556,-1.86075),('4','0','0','0','3',15,0.266667,-1.32175),('35','0','0','1','0',51,0.686275,-0.376477),('186','0','0','1','1',630,0.295238,-1.21997),('79','0','0','1','2',404,0.195545,-1.63196),('16','0','0','1','3',89,0.179775,-1.71605),('28','0','0','2','0',150,0.186667,-1.67843),('178','0','0','2','1',665,0.267669,-1.318),('55','0','0','2','2',279,0.197133,-1.62388),('2','0','0','2','3',43,0.046512,-3.06804),('39','0','0','3','0',162,0.240741,-1.42403),('216','0','0','3','1',537,0.402235,-0.910719),('1','0','0','3','2',176,0.005682,-5.17045),('2','0','1','1','0',40,0.050000,-2.99573),('1','0','1','2','1',320,0.003125,-5.76832),('4','1','0','0','0',254,0.015748,-4.15104),('198','1','0','0','1',212,0.933962,-0.0683195),('114','1','0','0','2',135,0.844444,-0.169077),('11','1','0','0','3',15,0.733333,-0.310155),('16','1','0','1','0',51,0.313725,-1.15924),('444','1','0','1','1',630,0.704762,-0.349895),('325','1','0','1','2',404,0.804455,-0.21759),('73','1','0','1','3',89,0.820225,-0.198177),('122','1','0','2','0',150,0.813333,-0.206615),('487','1','0','2','1',665,0.732331,-0.311523),('224','1','0','2','2',279,0.802867,-0.219566),('41','1','0','2','3',43,0.953488,-0.0476284),('123','1','0','3','0',162,0.759259,-0.275412),('321','1','0','3','1',537,0.597765,-0.514558),('175','1','0','3','2',176,0.994318,-0.0056982),('73','1','0','3','3',73,1.000000,0),('5','1','1','0','0',5,1.000000,0),('64','1','1','0','1',64,1.000000,0),('28','1','1','0','2',28,1.000000,0),('38','1','1','1','0',40,0.950000,-0.0512933),('201','1','1','1','1',201,1.000000,0),('48','1','1','1','2',48,1.000000,0),('71','1','1','1','3',71,1.000000,0),('120','1','1','2','0',120,1.000000,0),('319','1','1','2','1',320,0.996875,-0.0031299),('93','1','1','2','2',93,1.000000,0),('37','1','1','2','3',37,1.000000,0),('182','1','1','3','0',182,1.000000,0),('611','1','1','3','1',611,1.000000,0),('216','1','1','3','2',216,1.000000,0),('41','1','1','3','3',41,1.000000,0),('5','1','1','4','2',5,1.000000,0);
/*!40000 ALTER TABLE `tbil(indis0)_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:23
