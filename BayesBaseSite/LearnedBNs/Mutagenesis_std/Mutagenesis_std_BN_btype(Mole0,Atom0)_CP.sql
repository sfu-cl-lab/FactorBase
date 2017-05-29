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
-- Table structure for table `btype(Mole0,Atom0)_CP`
--

DROP TABLE IF EXISTS `btype(Mole0,Atom0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `btype(Mole0,Atom0)_CP` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `atype(Atom0)` varchar(45) default NULL,
  `a` varchar(5) default NULL,
  `charge(Atom0)` varchar(45) default NULL,
  `elem(Atom0)` char(1) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `btype(Mole0,Atom0)_CP`
--

LOCK TABLES `btype(Mole0,Atom0)_CP` WRITE;
/*!40000 ALTER TABLE `btype(Mole0,Atom0)_CP` DISABLE KEYS */;
INSERT INTO `btype(Mole0,Atom0)_CP` VALUES ('1','1','0','T','0','b',1,1.000000,0),('102','1','0','T','0','c',102,1.000000,0),('1457','1','0','T','0','h',1457,1.000000,0),('1','1','0','T','1','b',1,1.000000,0),('38','1','0','T','1','h',38,1.000000,0),('140','1','1','T','0','c',1681,0.083284,-2.4855),('13','1','1','T','1','c',15,0.866667,-0.1431),('88','1','2','T','0','c',540,0.162963,-1.81423),('11','1','2','T','0','f',11,1.000000,0),('1','1','2','T','0','i',1,1.000000,0),('38','1','2','T','0','n',71,0.535211,-0.625094),('24','1','2','T','0','o',477,0.050314,-2.98947),('8','1','2','T','1','c',9,0.888889,-0.117783),('220','1','2','T','1','n',253,0.869565,-0.139762),('6','2','1','T','0','c',1681,0.003569,-5.63547),('1','2','2','T','0','n',71,0.014085,-4.26264),('447','2','2','T','0','o',477,0.937107,-0.0649578),('33','2','2','T','1','n',253,0.130435,-2.03688),('1','3','2','T','0','n',71,0.014085,-4.26264),('2','4','2','T','0','o',477,0.004193,-5.47434),('2','5','2','T','0','o',477,0.004193,-5.47434),('1535','7','1','T','0','c',1681,0.913147,-0.0908584),('2','7','1','T','1','c',15,0.133333,-2.01491),('452','7','2','T','0','c',540,0.837037,-0.177887),('31','7','2','T','0','n',71,0.436620,-0.828692),('2','7','2','T','0','o',477,0.004193,-5.47434),('1','7','2','T','1','c',9,0.111111,-2.19723),('184','N/A','0','F','0','b',184,1.000000,0),('19323','N/A','0','F','0','c',19323,1.000000,0),('274008','N/A','0','F','0','h',274008,1.000000,0),('184','N/A','0','F','1','b',184,1.000000,0),('6992','N/A','0','F','1','h',6992,1.000000,0),('317814','N/A','1','F','0','c',317814,1.000000,0),('2945','N/A','1','F','1','c',2945,1.000000,0),('103060','N/A','2','F','0','c',103060,1.000000,0),('2394','N/A','2','F','0','f',2394,1.000000,0),('184','N/A','2','F','0','i',184,1.000000,0),('13434','N/A','2','F','0','n',13434,1.000000,0),('108303','N/A','2','F','0','o',108303,1.000000,0),('1656','N/A','2','F','1','c',1656,1.000000,0),('50067','N/A','2','F','1','n',50067,1.000000,0);
/*!40000 ALTER TABLE `btype(Mole0,Atom0)_CP` ENABLE KEYS */;
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
