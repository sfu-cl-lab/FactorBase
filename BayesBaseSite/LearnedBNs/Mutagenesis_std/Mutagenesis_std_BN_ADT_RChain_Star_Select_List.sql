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
-- Table structure for table `ADT_RChain_Star_Select_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_Select_List` (
  `rchain` varchar(256) default NULL,
  `rnid` varchar(256) default NULL,
  `Entries` varchar(199) character set utf8 default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Select_List`
--

LOCK TABLES `ADT_RChain_Star_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b`','`b`','`ind1(Mole0)`'),('`a,b`','`a`','`ind1(Mole0)`'),('`a,b`','`b`','`inda(Mole0)`'),('`a,b`','`a`','`inda(Mole0)`'),('`a,b`','`b`','`label(Mole0)`'),('`a,b`','`a`','`label(Mole0)`'),('`a,b`','`b`','`logp(Mole0)`'),('`a,b`','`a`','`logp(Mole0)`'),('`a,b`','`b`','`lumo(Mole0)`'),('`a,b`','`a`','`lumo(Mole0)`'),('`a,b`','`b`','`atype(Atom0)`'),('`a,b`','`a`','`atype(Atom0)`'),('`a,b`','`b`','`charge(Atom0)`'),('`a,b`','`a`','`charge(Atom0)`'),('`a,b`','`b`','`elem(Atom0)`'),('`a,b`','`a`','`elem(Atom0)`'),('`a,b`','`b`','`btype(Mole0,Atom0)`'),('`a,b`','`b`','`a`'),('`a,b`','`a`','`b`'),('`a`','`a`','`atype(Atom0)`'),('`b`','`b`','`atype(Atom0)`'),('`a`','`a`','`charge(Atom0)`'),('`b`','`b`','`charge(Atom0)`'),('`a`','`a`','`elem(Atom0)`'),('`b`','`b`','`elem(Atom0)`'),('`a`','`a`','`ind1(Mole0)`'),('`b`','`b`','`ind1(Mole0)`'),('`a`','`a`','`inda(Mole0)`'),('`b`','`b`','`inda(Mole0)`'),('`a`','`a`','`label(Mole0)`'),('`b`','`b`','`label(Mole0)`'),('`a`','`a`','`logp(Mole0)`'),('`b`','`b`','`logp(Mole0)`'),('`a`','`a`','`lumo(Mole0)`'),('`b`','`b`','`lumo(Mole0)`');
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:17
