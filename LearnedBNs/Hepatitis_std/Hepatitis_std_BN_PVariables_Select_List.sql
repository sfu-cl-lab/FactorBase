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
-- Table structure for table `PVariables_Select_List`
--

DROP TABLE IF EXISTS `PVariables_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PVariables_Select_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(267) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PVariables_Select_List`
--

LOCK TABLES `PVariables_Select_List` WRITE;
/*!40000 ALTER TABLE `PVariables_Select_List` DISABLE KEYS */;
INSERT INTO `PVariables_Select_List` VALUES ('Bio0','count(*) as \"MULT\"'),('dispat0','count(*) as \"MULT\"'),('indis0','count(*) as \"MULT\"'),('inf0','count(*) as \"MULT\"'),('Bio0','Bio0.activity AS `activity(Bio0)`'),('dispat0','dispat0.age AS `age(dispat0)`'),('indis0','indis0.alb AS `alb(indis0)`'),('indis0','indis0.che AS `che(indis0)`'),('indis0','indis0.dbil AS `dbil(indis0)`'),('inf0','inf0.dur AS `dur(inf0)`'),('Bio0','Bio0.fibros AS `fibros(Bio0)`'),('indis0','indis0.got AS `got(indis0)`'),('indis0','indis0.gpt AS `gpt(indis0)`'),('dispat0','dispat0.sex AS `sex(dispat0)`'),('indis0','indis0.tbil AS `tbil(indis0)`'),('indis0','indis0.tcho AS `tcho(indis0)`'),('indis0','indis0.tp AS `tp(indis0)`'),('indis0','indis0.ttt AS `ttt(indis0)`'),('dispat0','dispat0.Type AS `Type(dispat0)`'),('indis0','indis0.ztt AS `ztt(indis0)`');
/*!40000 ALTER TABLE `PVariables_Select_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:19
