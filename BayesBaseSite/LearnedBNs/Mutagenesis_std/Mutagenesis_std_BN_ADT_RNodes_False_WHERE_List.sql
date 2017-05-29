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
-- Table structure for table `ADT_RNodes_False_WHERE_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_False_WHERE_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_False_WHERE_List` (
  `rnid` varchar(10) default NULL,
  `Entries` varchar(303) character set utf8 default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_False_WHERE_List`
--

LOCK TABLES `ADT_RNodes_False_WHERE_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_False_WHERE_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_False_WHERE_List` VALUES ('`a`','`a_star`.`ind1(Mole0)`=`a_flat`.`ind1(Mole0)`'),('`b`','`b_star`.`ind1(Mole0)`=`b_flat`.`ind1(Mole0)`'),('`a`','`a_star`.`inda(Mole0)`=`a_flat`.`inda(Mole0)`'),('`b`','`b_star`.`inda(Mole0)`=`b_flat`.`inda(Mole0)`'),('`a`','`a_star`.`label(Mole0)`=`a_flat`.`label(Mole0)`'),('`b`','`b_star`.`label(Mole0)`=`b_flat`.`label(Mole0)`'),('`a`','`a_star`.`logp(Mole0)`=`a_flat`.`logp(Mole0)`'),('`b`','`b_star`.`logp(Mole0)`=`b_flat`.`logp(Mole0)`'),('`a`','`a_star`.`lumo(Mole0)`=`a_flat`.`lumo(Mole0)`'),('`b`','`b_star`.`lumo(Mole0)`=`b_flat`.`lumo(Mole0)`'),('`a`','`a_star`.`atype(Atom0)`=`a_flat`.`atype(Atom0)`'),('`b`','`b_star`.`atype(Atom0)`=`b_flat`.`atype(Atom0)`'),('`a`','`a_star`.`charge(Atom0)`=`a_flat`.`charge(Atom0)`'),('`b`','`b_star`.`charge(Atom0)`=`b_flat`.`charge(Atom0)`'),('`a`','`a_star`.`elem(Atom0)`=`a_flat`.`elem(Atom0)`'),('`b`','`b_star`.`elem(Atom0)`=`b_flat`.`elem(Atom0)`');
/*!40000 ALTER TABLE `ADT_RNodes_False_WHERE_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:14
