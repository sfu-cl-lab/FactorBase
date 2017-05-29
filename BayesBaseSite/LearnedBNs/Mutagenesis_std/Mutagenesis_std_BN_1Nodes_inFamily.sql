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
-- Table structure for table `1Nodes_inFamily`
--

DROP TABLE IF EXISTS `1Nodes_inFamily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_inFamily` (
  `ChildNode` varchar(197) NOT NULL,
  `1node` varchar(197) NOT NULL,
  `NumAtts` bigint(21) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_inFamily`
--

LOCK TABLES `1Nodes_inFamily` WRITE;
/*!40000 ALTER TABLE `1Nodes_inFamily` DISABLE KEYS */;
INSERT INTO `1Nodes_inFamily` VALUES ('`a`','`atype(Atom0)`',3),('`btype(Mole0,Atom0)`','`atype(Atom0)`',3),('`atype(Atom0)`','`charge(Atom0)`',2),('`btype(Mole0,Atom0)`','`charge(Atom0)`',2),('`b`','`charge(Atom0)`',2),('`atype(Atom0)`','`elem(Atom0)`',7),('`a`','`elem(Atom0)`',7),('`btype(Mole0,Atom0)`','`elem(Atom0)`',7),('`charge(Atom0)`','`elem(Atom0)`',7),('`a`','`ind1(Mole0)`',2),('`b`','`ind1(Mole0)`',2),('`logp(Mole0)`','`ind1(Mole0)`',2),('`lumo(Mole0)`','`inda(Mole0)`',2),('`ind1(Mole0)`','`label(Mole0)`',2),('`logp(Mole0)`','`label(Mole0)`',2),('`lumo(Mole0)`','`label(Mole0)`',2),('`inda(Mole0)`','`logp(Mole0)`',10);
/*!40000 ALTER TABLE `1Nodes_inFamily` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:16
