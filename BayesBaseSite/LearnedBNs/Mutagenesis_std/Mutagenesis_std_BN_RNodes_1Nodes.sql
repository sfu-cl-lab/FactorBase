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
-- Table structure for table `RNodes_1Nodes`
--

DROP TABLE IF EXISTS `RNodes_1Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_1Nodes` (
  `rnid` varchar(10) default NULL,
  `TABLE_NAME` varchar(64) character set utf8 NOT NULL default '',
  `1nid` varchar(133) character set utf8 NOT NULL default '',
  `COLUMN_NAME` varchar(64) character set utf8 NOT NULL default '',
  `pvid` varchar(65) character set utf8 NOT NULL default ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_1Nodes`
--

LOCK TABLES `RNodes_1Nodes` WRITE;
/*!40000 ALTER TABLE `RNodes_1Nodes` DISABLE KEYS */;
INSERT INTO `RNodes_1Nodes` VALUES ('`a`','bond','`ind1(Mole0)`','ind1','Mole0'),('`b`','moleatm','`ind1(Mole0)`','ind1','Mole0'),('`a`','bond','`inda(Mole0)`','inda','Mole0'),('`b`','moleatm','`inda(Mole0)`','inda','Mole0'),('`a`','bond','`label(Mole0)`','label','Mole0'),('`b`','moleatm','`label(Mole0)`','label','Mole0'),('`a`','bond','`logp(Mole0)`','logp','Mole0'),('`b`','moleatm','`logp(Mole0)`','logp','Mole0'),('`a`','bond','`lumo(Mole0)`','lumo','Mole0'),('`b`','moleatm','`lumo(Mole0)`','lumo','Mole0'),('`a`','bond','`atype(Atom0)`','atype','Atom0'),('`b`','moleatm','`atype(Atom0)`','atype','Atom0'),('`a`','bond','`charge(Atom0)`','charge','Atom0'),('`b`','moleatm','`charge(Atom0)`','charge','Atom0'),('`a`','bond','`elem(Atom0)`','elem','Atom0'),('`b`','moleatm','`elem(Atom0)`','elem','Atom0');
/*!40000 ALTER TABLE `RNodes_1Nodes` ENABLE KEYS */;
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
