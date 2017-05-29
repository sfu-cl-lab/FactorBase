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
-- Table structure for table `Entity_Complement_Edges`
--

DROP TABLE IF EXISTS `Entity_Complement_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Entity_Complement_Edges` (
  `pvid` varchar(65) NOT NULL,
  `child` varchar(131) NOT NULL,
  `parent` varchar(131) NOT NULL,
  PRIMARY KEY (`pvid`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Entity_Complement_Edges`
--

LOCK TABLES `Entity_Complement_Edges` WRITE;
/*!40000 ALTER TABLE `Entity_Complement_Edges` DISABLE KEYS */;
INSERT INTO `Entity_Complement_Edges` VALUES ('Bio0','`activity(Bio0)`','`activity(Bio0)`'),('Bio0','`activity(Bio0)`','`fibros(Bio0)`'),('Bio0','`fibros(Bio0)`','`activity(Bio0)`'),('Bio0','`fibros(Bio0)`','`fibros(Bio0)`'),('dispat0','`age(dispat0)`','`age(dispat0)`'),('dispat0','`age(dispat0)`','`sex(dispat0)`'),('dispat0','`age(dispat0)`','`Type(dispat0)`'),('dispat0','`sex(dispat0)`','`age(dispat0)`'),('dispat0','`sex(dispat0)`','`sex(dispat0)`'),('dispat0','`Type(dispat0)`','`sex(dispat0)`'),('dispat0','`Type(dispat0)`','`Type(dispat0)`'),('indis0','`alb(indis0)`','`alb(indis0)`'),('indis0','`alb(indis0)`','`che(indis0)`'),('indis0','`alb(indis0)`','`dbil(indis0)`'),('indis0','`alb(indis0)`','`got(indis0)`'),('indis0','`alb(indis0)`','`gpt(indis0)`'),('indis0','`alb(indis0)`','`tbil(indis0)`'),('indis0','`alb(indis0)`','`tcho(indis0)`'),('indis0','`alb(indis0)`','`ttt(indis0)`'),('indis0','`che(indis0)`','`che(indis0)`'),('indis0','`che(indis0)`','`dbil(indis0)`'),('indis0','`che(indis0)`','`gpt(indis0)`'),('indis0','`che(indis0)`','`tbil(indis0)`'),('indis0','`che(indis0)`','`tcho(indis0)`'),('indis0','`che(indis0)`','`tp(indis0)`'),('indis0','`che(indis0)`','`ttt(indis0)`'),('indis0','`che(indis0)`','`ztt(indis0)`'),('indis0','`dbil(indis0)`','`alb(indis0)`'),('indis0','`dbil(indis0)`','`dbil(indis0)`'),('indis0','`dbil(indis0)`','`got(indis0)`'),('indis0','`dbil(indis0)`','`tbil(indis0)`'),('indis0','`dbil(indis0)`','`tcho(indis0)`'),('indis0','`dbil(indis0)`','`tp(indis0)`'),('indis0','`dbil(indis0)`','`ttt(indis0)`'),('indis0','`dbil(indis0)`','`ztt(indis0)`'),('indis0','`got(indis0)`','`alb(indis0)`'),('indis0','`got(indis0)`','`che(indis0)`'),('indis0','`got(indis0)`','`dbil(indis0)`'),('indis0','`got(indis0)`','`got(indis0)`'),('indis0','`got(indis0)`','`gpt(indis0)`'),('indis0','`got(indis0)`','`tbil(indis0)`'),('indis0','`got(indis0)`','`tp(indis0)`'),('indis0','`got(indis0)`','`ztt(indis0)`'),('indis0','`gpt(indis0)`','`alb(indis0)`'),('indis0','`gpt(indis0)`','`che(indis0)`'),('indis0','`gpt(indis0)`','`dbil(indis0)`'),('indis0','`gpt(indis0)`','`gpt(indis0)`'),('indis0','`gpt(indis0)`','`tbil(indis0)`'),('indis0','`gpt(indis0)`','`tcho(indis0)`'),('indis0','`gpt(indis0)`','`tp(indis0)`'),('indis0','`gpt(indis0)`','`ttt(indis0)`'),('indis0','`gpt(indis0)`','`ztt(indis0)`'),('indis0','`tbil(indis0)`','`alb(indis0)`'),('indis0','`tbil(indis0)`','`che(indis0)`'),('indis0','`tbil(indis0)`','`gpt(indis0)`'),('indis0','`tbil(indis0)`','`tbil(indis0)`'),('indis0','`tbil(indis0)`','`tp(indis0)`'),('indis0','`tbil(indis0)`','`ttt(indis0)`'),('indis0','`tbil(indis0)`','`ztt(indis0)`'),('indis0','`tcho(indis0)`','`alb(indis0)`'),('indis0','`tcho(indis0)`','`che(indis0)`'),('indis0','`tcho(indis0)`','`dbil(indis0)`'),('indis0','`tcho(indis0)`','`got(indis0)`'),('indis0','`tcho(indis0)`','`gpt(indis0)`'),('indis0','`tcho(indis0)`','`tbil(indis0)`'),('indis0','`tcho(indis0)`','`tcho(indis0)`'),('indis0','`tcho(indis0)`','`tp(indis0)`'),('indis0','`tcho(indis0)`','`ttt(indis0)`'),('indis0','`tp(indis0)`','`alb(indis0)`'),('indis0','`tp(indis0)`','`che(indis0)`'),('indis0','`tp(indis0)`','`dbil(indis0)`'),('indis0','`tp(indis0)`','`got(indis0)`'),('indis0','`tp(indis0)`','`tbil(indis0)`'),('indis0','`tp(indis0)`','`tcho(indis0)`'),('indis0','`tp(indis0)`','`tp(indis0)`'),('indis0','`tp(indis0)`','`ttt(indis0)`'),('indis0','`ttt(indis0)`','`alb(indis0)`'),('indis0','`ttt(indis0)`','`che(indis0)`'),('indis0','`ttt(indis0)`','`dbil(indis0)`'),('indis0','`ttt(indis0)`','`got(indis0)`'),('indis0','`ttt(indis0)`','`gpt(indis0)`'),('indis0','`ttt(indis0)`','`tbil(indis0)`'),('indis0','`ttt(indis0)`','`tcho(indis0)`'),('indis0','`ttt(indis0)`','`tp(indis0)`'),('indis0','`ttt(indis0)`','`ttt(indis0)`'),('indis0','`ttt(indis0)`','`ztt(indis0)`'),('indis0','`ztt(indis0)`','`alb(indis0)`'),('indis0','`ztt(indis0)`','`che(indis0)`'),('indis0','`ztt(indis0)`','`dbil(indis0)`'),('indis0','`ztt(indis0)`','`got(indis0)`'),('indis0','`ztt(indis0)`','`gpt(indis0)`'),('indis0','`ztt(indis0)`','`tbil(indis0)`'),('indis0','`ztt(indis0)`','`tcho(indis0)`'),('indis0','`ztt(indis0)`','`tp(indis0)`'),('indis0','`ztt(indis0)`','`ztt(indis0)`'),('inf0','`dur(inf0)`','`dur(inf0)`');
/*!40000 ALTER TABLE `Entity_Complement_Edges` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:09:23
