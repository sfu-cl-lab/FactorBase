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
-- Table structure for table `ADT_RNodes_False_Select_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_False_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_False_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(151) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_False_Select_List`
--

LOCK TABLES `ADT_RNodes_False_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_False_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_False_Select_List` VALUES ('`a`','(`a_star`.MULT-`a_flat`.MULT) AS \"MULT\"'),('`b`','(`b_star`.MULT-`b_flat`.MULT) AS \"MULT\"'),('`c`','(`c_star`.MULT-`c_flat`.MULT) AS \"MULT\"'),('`a`','`a_star`.`activity(Bio0)`'),('`b`','`b_star`.`alb(indis0)`'),('`b`','`b_star`.`che(indis0)`'),('`b`','`b_star`.`dbil(indis0)`'),('`c`','`c_star`.`dur(inf0)`'),('`a`','`a_star`.`fibros(Bio0)`'),('`b`','`b_star`.`got(indis0)`'),('`b`','`b_star`.`gpt(indis0)`'),('`b`','`b_star`.`tbil(indis0)`'),('`b`','`b_star`.`tcho(indis0)`'),('`b`','`b_star`.`tp(indis0)`'),('`b`','`b_star`.`ttt(indis0)`'),('`b`','`b_star`.`ztt(indis0)`'),('`a`','`a_star`.`age(dispat0)`'),('`b`','`b_star`.`age(dispat0)`'),('`c`','`c_star`.`age(dispat0)`'),('`a`','`a_star`.`sex(dispat0)`'),('`b`','`b_star`.`sex(dispat0)`'),('`c`','`c_star`.`sex(dispat0)`'),('`a`','`a_star`.`Type(dispat0)`'),('`b`','`b_star`.`Type(dispat0)`'),('`c`','`c_star`.`Type(dispat0)`');
/*!40000 ALTER TABLE `ADT_RNodes_False_Select_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:21
