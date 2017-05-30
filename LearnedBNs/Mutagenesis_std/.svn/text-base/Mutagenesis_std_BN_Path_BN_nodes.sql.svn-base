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
-- Table structure for table `Path_BN_nodes`
--

DROP TABLE IF EXISTS `Path_BN_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_BN_nodes` (
  `Rchain` varchar(256) NOT NULL default '',
  `node` varchar(199) character set utf8 NOT NULL default '',
  KEY `HashIndex` (`Rchain`,`node`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BN_nodes`
--

LOCK TABLES `Path_BN_nodes` WRITE;
/*!40000 ALTER TABLE `Path_BN_nodes` DISABLE KEYS */;
INSERT INTO `Path_BN_nodes` VALUES ('`a,b`','`atype(Atom0)`'),('`a,b`','`btype(Mole0,Atom0)`'),('`a,b`','`charge(Atom0)`'),('`a,b`','`elem(Atom0)`'),('`a,b`','`ind1(Mole0)`'),('`a,b`','`inda(Mole0)`'),('`a,b`','`label(Mole0)`'),('`a,b`','`logp(Mole0)`'),('`a,b`','`lumo(Mole0)`'),('`a`','`atype(Atom0)`'),('`a`','`btype(Mole0,Atom0)`'),('`a`','`charge(Atom0)`'),('`a`','`elem(Atom0)`'),('`a`','`ind1(Mole0)`'),('`a`','`inda(Mole0)`'),('`a`','`label(Mole0)`'),('`a`','`logp(Mole0)`'),('`a`','`lumo(Mole0)`'),('`b`','`atype(Atom0)`'),('`b`','`charge(Atom0)`'),('`b`','`elem(Atom0)`'),('`b`','`ind1(Mole0)`'),('`b`','`inda(Mole0)`'),('`b`','`label(Mole0)`'),('`b`','`logp(Mole0)`'),('`b`','`lumo(Mole0)`');
/*!40000 ALTER TABLE `Path_BN_nodes` ENABLE KEYS */;
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
