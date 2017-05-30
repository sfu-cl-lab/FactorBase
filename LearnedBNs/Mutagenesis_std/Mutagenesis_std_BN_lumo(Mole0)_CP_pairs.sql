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
-- Table structure for table `lumo(Mole0)_CP_pairs`
--

DROP TABLE IF EXISTS `lumo(Mole0)_CP_pairs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lumo(Mole0)_CP_pairs` (
  `ChildValue` varchar(200) default NULL,
  `inda(Mole0)` varchar(200) default NULL,
  `label(Mole0)` varchar(200) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lumo(Mole0)_CP_pairs`
--

LOCK TABLES `lumo(Mole0)_CP_pairs` WRITE;
/*!40000 ALTER TABLE `lumo(Mole0)_CP_pairs` DISABLE KEYS */;
INSERT INTO `lumo(Mole0)_CP_pairs` VALUES ('1','0','1'),('1','0','2'),('1','1','1'),('1','1','2'),('2','0','1'),('2','0','2'),('2','1','1'),('2','1','2'),('3','0','1'),('3','0','2'),('3','1','1'),('3','1','2'),('4','0','1'),('4','0','2'),('4','1','1'),('4','1','2'),('5','0','1'),('5','0','2'),('5','1','1'),('5','1','2'),('6','0','1'),('6','0','2'),('6','1','1'),('6','1','2'),('7','0','1'),('7','0','2'),('7','1','1'),('7','1','2'),('8','0','1'),('8','0','2'),('8','1','1'),('8','1','2'),('9','0','1'),('9','0','2'),('9','1','1'),('9','1','2');
/*!40000 ALTER TABLE `lumo(Mole0)_CP_pairs` ENABLE KEYS */;
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
