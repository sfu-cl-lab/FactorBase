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
-- Table structure for table `btype(Mole0,Atom0)_CP_pairs`
--

DROP TABLE IF EXISTS `btype(Mole0,Atom0)_CP_pairs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `btype(Mole0,Atom0)_CP_pairs` (
  `ChildValue` varchar(200) default NULL,
  `atype(Atom0)` varchar(200) default NULL,
  `a` varchar(200) default NULL,
  `charge(Atom0)` varchar(200) default NULL,
  `elem(Atom0)` varchar(200) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `btype(Mole0,Atom0)_CP_pairs`
--

LOCK TABLES `btype(Mole0,Atom0)_CP_pairs` WRITE;
/*!40000 ALTER TABLE `btype(Mole0,Atom0)_CP_pairs` DISABLE KEYS */;
INSERT INTO `btype(Mole0,Atom0)_CP_pairs` VALUES ('1','0','T','0','b'),('1','0','T','0','c'),('1','0','T','0','h'),('1','0','T','0','f'),('1','0','T','0','i'),('1','0','T','0','n'),('1','0','T','0','o'),('1','0','T','1','b'),('1','0','T','1','c'),('1','0','T','1','h'),('1','0','T','1','f'),('1','0','T','1','i'),('1','0','T','1','n'),('1','0','T','1','o'),('1','0','F','0','b'),('1','0','F','0','c'),('1','0','F','0','h'),('1','0','F','0','f'),('1','0','F','0','i'),('1','0','F','0','n'),('1','0','F','0','o'),('1','0','F','1','b'),('1','0','F','1','c'),('1','0','F','1','h'),('1','0','F','1','f'),('1','0','F','1','i'),('1','0','F','1','n'),('1','0','F','1','o'),('1','1','T','0','b'),('1','1','T','0','c'),('1','1','T','0','h'),('1','1','T','0','f'),('1','1','T','0','i'),('1','1','T','0','n'),('1','1','T','0','o'),('1','1','T','1','b'),('1','1','T','1','c'),('1','1','T','1','h'),('1','1','T','1','f'),('1','1','T','1','i'),('1','1','T','1','n'),('1','1','T','1','o'),('1','1','F','0','b'),('1','1','F','0','c'),('1','1','F','0','h'),('1','1','F','0','f'),('1','1','F','0','i'),('1','1','F','0','n'),('1','1','F','0','o'),('1','1','F','1','b'),('1','1','F','1','c'),('1','1','F','1','h'),('1','1','F','1','f'),('1','1','F','1','i'),('1','1','F','1','n'),('1','1','F','1','o'),('1','2','T','0','b'),('1','2','T','0','c'),('1','2','T','0','h'),('1','2','T','0','f'),('1','2','T','0','i'),('1','2','T','0','n'),('1','2','T','0','o'),('1','2','T','1','b'),('1','2','T','1','c'),('1','2','T','1','h'),('1','2','T','1','f'),('1','2','T','1','i'),('1','2','T','1','n'),('1','2','T','1','o'),('1','2','F','0','b'),('1','2','F','0','c'),('1','2','F','0','h'),('1','2','F','0','f'),('1','2','F','0','i'),('1','2','F','0','n'),('1','2','F','0','o'),('1','2','F','1','b'),('1','2','F','1','c'),('1','2','F','1','h'),('1','2','F','1','f'),('1','2','F','1','i'),('1','2','F','1','n'),('1','2','F','1','o'),('2','0','T','0','b'),('2','0','T','0','c'),('2','0','T','0','h'),('2','0','T','0','f'),('2','0','T','0','i'),('2','0','T','0','n'),('2','0','T','0','o'),('2','0','T','1','b'),('2','0','T','1','c'),('2','0','T','1','h'),('2','0','T','1','f'),('2','0','T','1','i'),('2','0','T','1','n'),('2','0','T','1','o'),('2','0','F','0','b'),('2','0','F','0','c'),('2','0','F','0','h'),('2','0','F','0','f'),('2','0','F','0','i'),('2','0','F','0','n'),('2','0','F','0','o'),('2','0','F','1','b'),('2','0','F','1','c'),('2','0','F','1','h'),('2','0','F','1','f'),('2','0','F','1','i'),('2','0','F','1','n'),('2','0','F','1','o'),('2','1','T','0','b'),('2','1','T','0','c'),('2','1','T','0','h'),('2','1','T','0','f'),('2','1','T','0','i'),('2','1','T','0','n'),('2','1','T','0','o'),('2','1','T','1','b'),('2','1','T','1','c'),('2','1','T','1','h'),('2','1','T','1','f'),('2','1','T','1','i'),('2','1','T','1','n'),('2','1','T','1','o'),('2','1','F','0','b'),('2','1','F','0','c'),('2','1','F','0','h'),('2','1','F','0','f'),('2','1','F','0','i'),('2','1','F','0','n'),('2','1','F','0','o'),('2','1','F','1','b'),('2','1','F','1','c'),('2','1','F','1','h'),('2','1','F','1','f'),('2','1','F','1','i'),('2','1','F','1','n'),('2','1','F','1','o'),('2','2','T','0','b'),('2','2','T','0','c'),('2','2','T','0','h'),('2','2','T','0','f'),('2','2','T','0','i'),('2','2','T','0','n'),('2','2','T','0','o'),('2','2','T','1','b'),('2','2','T','1','c'),('2','2','T','1','h'),('2','2','T','1','f'),('2','2','T','1','i'),('2','2','T','1','n'),('2','2','T','1','o'),('2','2','F','0','b'),('2','2','F','0','c'),('2','2','F','0','h'),('2','2','F','0','f'),('2','2','F','0','i'),('2','2','F','0','n'),('2','2','F','0','o'),('2','2','F','1','b'),('2','2','F','1','c'),('2','2','F','1','h'),('2','2','F','1','f'),('2','2','F','1','i'),('2','2','F','1','n'),('2','2','F','1','o'),('3','0','T','0','b'),('3','0','T','0','c'),('3','0','T','0','h'),('3','0','T','0','f'),('3','0','T','0','i'),('3','0','T','0','n'),('3','0','T','0','o'),('3','0','T','1','b'),('3','0','T','1','c'),('3','0','T','1','h'),('3','0','T','1','f'),('3','0','T','1','i'),('3','0','T','1','n'),('3','0','T','1','o'),('3','0','F','0','b'),('3','0','F','0','c'),('3','0','F','0','h'),('3','0','F','0','f'),('3','0','F','0','i'),('3','0','F','0','n'),('3','0','F','0','o'),('3','0','F','1','b'),('3','0','F','1','c'),('3','0','F','1','h'),('3','0','F','1','f'),('3','0','F','1','i'),('3','0','F','1','n'),('3','0','F','1','o'),('3','1','T','0','b'),('3','1','T','0','c'),('3','1','T','0','h'),('3','1','T','0','f'),('3','1','T','0','i'),('3','1','T','0','n'),('3','1','T','0','o'),('3','1','T','1','b'),('3','1','T','1','c'),('3','1','T','1','h'),('3','1','T','1','f'),('3','1','T','1','i'),('3','1','T','1','n'),('3','1','T','1','o'),('3','1','F','0','b'),('3','1','F','0','c'),('3','1','F','0','h'),('3','1','F','0','f'),('3','1','F','0','i'),('3','1','F','0','n'),('3','1','F','0','o'),('3','1','F','1','b'),('3','1','F','1','c'),('3','1','F','1','h'),('3','1','F','1','f'),('3','1','F','1','i'),('3','1','F','1','n'),('3','1','F','1','o'),('3','2','T','0','b'),('3','2','T','0','c'),('3','2','T','0','h'),('3','2','T','0','f'),('3','2','T','0','i'),('3','2','T','0','n'),('3','2','T','0','o'),('3','2','T','1','b'),('3','2','T','1','c'),('3','2','T','1','h'),('3','2','T','1','f'),('3','2','T','1','i'),('3','2','T','1','n'),('3','2','T','1','o'),('3','2','F','0','b'),('3','2','F','0','c'),('3','2','F','0','h'),('3','2','F','0','f'),('3','2','F','0','i'),('3','2','F','0','n'),('3','2','F','0','o'),('3','2','F','1','b'),('3','2','F','1','c'),('3','2','F','1','h'),('3','2','F','1','f'),('3','2','F','1','i'),('3','2','F','1','n'),('3','2','F','1','o'),('4','0','T','0','b'),('4','0','T','0','c'),('4','0','T','0','h'),('4','0','T','0','f'),('4','0','T','0','i'),('4','0','T','0','n'),('4','0','T','0','o'),('4','0','T','1','b'),('4','0','T','1','c'),('4','0','T','1','h'),('4','0','T','1','f'),('4','0','T','1','i'),('4','0','T','1','n'),('4','0','T','1','o'),('4','0','F','0','b'),('4','0','F','0','c'),('4','0','F','0','h'),('4','0','F','0','f'),('4','0','F','0','i'),('4','0','F','0','n'),('4','0','F','0','o'),('4','0','F','1','b'),('4','0','F','1','c'),('4','0','F','1','h'),('4','0','F','1','f'),('4','0','F','1','i'),('4','0','F','1','n'),('4','0','F','1','o'),('4','1','T','0','b'),('4','1','T','0','c'),('4','1','T','0','h'),('4','1','T','0','f'),('4','1','T','0','i'),('4','1','T','0','n'),('4','1','T','0','o'),('4','1','T','1','b'),('4','1','T','1','c'),('4','1','T','1','h'),('4','1','T','1','f'),('4','1','T','1','i'),('4','1','T','1','n'),('4','1','T','1','o'),('4','1','F','0','b'),('4','1','F','0','c'),('4','1','F','0','h'),('4','1','F','0','f'),('4','1','F','0','i'),('4','1','F','0','n'),('4','1','F','0','o'),('4','1','F','1','b'),('4','1','F','1','c'),('4','1','F','1','h'),('4','1','F','1','f'),('4','1','F','1','i'),('4','1','F','1','n'),('4','1','F','1','o'),('4','2','T','0','b'),('4','2','T','0','c'),('4','2','T','0','h'),('4','2','T','0','f'),('4','2','T','0','i'),('4','2','T','0','n'),('4','2','T','0','o'),('4','2','T','1','b'),('4','2','T','1','c'),('4','2','T','1','h'),('4','2','T','1','f'),('4','2','T','1','i'),('4','2','T','1','n'),('4','2','T','1','o'),('4','2','F','0','b'),('4','2','F','0','c'),('4','2','F','0','h'),('4','2','F','0','f'),('4','2','F','0','i'),('4','2','F','0','n'),('4','2','F','0','o'),('4','2','F','1','b'),('4','2','F','1','c'),('4','2','F','1','h'),('4','2','F','1','f'),('4','2','F','1','i'),('4','2','F','1','n'),('4','2','F','1','o'),('5','0','T','0','b'),('5','0','T','0','c'),('5','0','T','0','h'),('5','0','T','0','f'),('5','0','T','0','i'),('5','0','T','0','n'),('5','0','T','0','o'),('5','0','T','1','b'),('5','0','T','1','c'),('5','0','T','1','h'),('5','0','T','1','f'),('5','0','T','1','i'),('5','0','T','1','n'),('5','0','T','1','o'),('5','0','F','0','b'),('5','0','F','0','c'),('5','0','F','0','h'),('5','0','F','0','f'),('5','0','F','0','i'),('5','0','F','0','n'),('5','0','F','0','o'),('5','0','F','1','b'),('5','0','F','1','c'),('5','0','F','1','h'),('5','0','F','1','f'),('5','0','F','1','i'),('5','0','F','1','n'),('5','0','F','1','o'),('5','1','T','0','b'),('5','1','T','0','c'),('5','1','T','0','h'),('5','1','T','0','f'),('5','1','T','0','i'),('5','1','T','0','n'),('5','1','T','0','o'),('5','1','T','1','b'),('5','1','T','1','c'),('5','1','T','1','h'),('5','1','T','1','f'),('5','1','T','1','i'),('5','1','T','1','n'),('5','1','T','1','o'),('5','1','F','0','b'),('5','1','F','0','c'),('5','1','F','0','h'),('5','1','F','0','f'),('5','1','F','0','i'),('5','1','F','0','n'),('5','1','F','0','o'),('5','1','F','1','b'),('5','1','F','1','c'),('5','1','F','1','h'),('5','1','F','1','f'),('5','1','F','1','i'),('5','1','F','1','n'),('5','1','F','1','o'),('5','2','T','0','b'),('5','2','T','0','c'),('5','2','T','0','h'),('5','2','T','0','f'),('5','2','T','0','i'),('5','2','T','0','n'),('5','2','T','0','o'),('5','2','T','1','b'),('5','2','T','1','c'),('5','2','T','1','h'),('5','2','T','1','f'),('5','2','T','1','i'),('5','2','T','1','n'),('5','2','T','1','o'),('5','2','F','0','b'),('5','2','F','0','c'),('5','2','F','0','h'),('5','2','F','0','f'),('5','2','F','0','i'),('5','2','F','0','n'),('5','2','F','0','o'),('5','2','F','1','b'),('5','2','F','1','c'),('5','2','F','1','h'),('5','2','F','1','f'),('5','2','F','1','i'),('5','2','F','1','n'),('5','2','F','1','o'),('7','0','T','0','b'),('7','0','T','0','c'),('7','0','T','0','h'),('7','0','T','0','f'),('7','0','T','0','i'),('7','0','T','0','n'),('7','0','T','0','o'),('7','0','T','1','b'),('7','0','T','1','c'),('7','0','T','1','h'),('7','0','T','1','f'),('7','0','T','1','i'),('7','0','T','1','n'),('7','0','T','1','o'),('7','0','F','0','b'),('7','0','F','0','c'),('7','0','F','0','h'),('7','0','F','0','f'),('7','0','F','0','i'),('7','0','F','0','n'),('7','0','F','0','o'),('7','0','F','1','b'),('7','0','F','1','c'),('7','0','F','1','h'),('7','0','F','1','f'),('7','0','F','1','i'),('7','0','F','1','n'),('7','0','F','1','o'),('7','1','T','0','b'),('7','1','T','0','c'),('7','1','T','0','h'),('7','1','T','0','f'),('7','1','T','0','i'),('7','1','T','0','n'),('7','1','T','0','o'),('7','1','T','1','b'),('7','1','T','1','c'),('7','1','T','1','h'),('7','1','T','1','f'),('7','1','T','1','i'),('7','1','T','1','n'),('7','1','T','1','o'),('7','1','F','0','b'),('7','1','F','0','c'),('7','1','F','0','h'),('7','1','F','0','f'),('7','1','F','0','i'),('7','1','F','0','n'),('7','1','F','0','o'),('7','1','F','1','b'),('7','1','F','1','c'),('7','1','F','1','h'),('7','1','F','1','f'),('7','1','F','1','i'),('7','1','F','1','n'),('7','1','F','1','o'),('7','2','T','0','b'),('7','2','T','0','c'),('7','2','T','0','h'),('7','2','T','0','f'),('7','2','T','0','i'),('7','2','T','0','n'),('7','2','T','0','o'),('7','2','T','1','b'),('7','2','T','1','c'),('7','2','T','1','h'),('7','2','T','1','f'),('7','2','T','1','i'),('7','2','T','1','n'),('7','2','T','1','o'),('7','2','F','0','b'),('7','2','F','0','c'),('7','2','F','0','h'),('7','2','F','0','f'),('7','2','F','0','i'),('7','2','F','0','n'),('7','2','F','0','o'),('7','2','F','1','b'),('7','2','F','1','c'),('7','2','F','1','h'),('7','2','F','1','f'),('7','2','F','1','i'),('7','2','F','1','n'),('7','2','F','1','o'),('N/A','0','T','0','b'),('N/A','0','T','0','c'),('N/A','0','T','0','h'),('N/A','0','T','0','f'),('N/A','0','T','0','i'),('N/A','0','T','0','n'),('N/A','0','T','0','o'),('N/A','0','T','1','b'),('N/A','0','T','1','c'),('N/A','0','T','1','h'),('N/A','0','T','1','f'),('N/A','0','T','1','i'),('N/A','0','T','1','n'),('N/A','0','T','1','o'),('N/A','0','F','0','b'),('N/A','0','F','0','c'),('N/A','0','F','0','h'),('N/A','0','F','0','f'),('N/A','0','F','0','i'),('N/A','0','F','0','n'),('N/A','0','F','0','o'),('N/A','0','F','1','b'),('N/A','0','F','1','c'),('N/A','0','F','1','h'),('N/A','0','F','1','f'),('N/A','0','F','1','i'),('N/A','0','F','1','n'),('N/A','0','F','1','o'),('N/A','1','T','0','b'),('N/A','1','T','0','c'),('N/A','1','T','0','h'),('N/A','1','T','0','f'),('N/A','1','T','0','i'),('N/A','1','T','0','n'),('N/A','1','T','0','o'),('N/A','1','T','1','b'),('N/A','1','T','1','c'),('N/A','1','T','1','h'),('N/A','1','T','1','f'),('N/A','1','T','1','i'),('N/A','1','T','1','n'),('N/A','1','T','1','o'),('N/A','1','F','0','b'),('N/A','1','F','0','c'),('N/A','1','F','0','h'),('N/A','1','F','0','f'),('N/A','1','F','0','i'),('N/A','1','F','0','n'),('N/A','1','F','0','o'),('N/A','1','F','1','b'),('N/A','1','F','1','c'),('N/A','1','F','1','h'),('N/A','1','F','1','f'),('N/A','1','F','1','i'),('N/A','1','F','1','n'),('N/A','1','F','1','o'),('N/A','2','T','0','b'),('N/A','2','T','0','c'),('N/A','2','T','0','h'),('N/A','2','T','0','f'),('N/A','2','T','0','i'),('N/A','2','T','0','n'),('N/A','2','T','0','o'),('N/A','2','T','1','b'),('N/A','2','T','1','c'),('N/A','2','T','1','h'),('N/A','2','T','1','f'),('N/A','2','T','1','i'),('N/A','2','T','1','n'),('N/A','2','T','1','o'),('N/A','2','F','0','b'),('N/A','2','F','0','c'),('N/A','2','F','0','h'),('N/A','2','F','0','f'),('N/A','2','F','0','i'),('N/A','2','F','0','n'),('N/A','2','F','0','o'),('N/A','2','F','1','b'),('N/A','2','F','1','c'),('N/A','2','F','1','h'),('N/A','2','F','1','f'),('N/A','2','F','1','i'),('N/A','2','F','1','n'),('N/A','2','F','1','o');
/*!40000 ALTER TABLE `btype(Mole0,Atom0)_CP_pairs` ENABLE KEYS */;
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
