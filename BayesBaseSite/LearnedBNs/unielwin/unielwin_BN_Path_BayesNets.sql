CREATE DATABASE  IF NOT EXISTS `unielwin_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `unielwin_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: unielwin_BN
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
-- Table structure for table `Path_BayesNets`
--

DROP TABLE IF EXISTS `Path_BayesNets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_BayesNets` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY  (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BayesNets`
--

LOCK TABLES `Path_BayesNets` WRITE;
/*!40000 ALTER TABLE `Path_BayesNets` DISABLE KEYS */;
INSERT INTO `Path_BayesNets` VALUES ('`a,b`','`a`','`teachingability(prof0)`'),('`a,b`','`b`','`a`'),('`a,b`','`b`','`salary(prof0,student0)`'),('`a,b`','`capability(prof0,student0)`','`a`'),('`a,b`','`diff(course0)`','`grade(course0,student0)`'),('`a,b`','`grade(course0,student0)`','`b`'),('`a,b`','`grade(course0,student0)`','`intelligence(student0)`'),('`a,b`','`intelligence(student0)`','`a`'),('`a,b`','`popularity(prof0)`','`a`'),('`a,b`','`popularity(prof0)`','`b`'),('`a,b`','`popularity(prof0)`','`teachingability(prof0)`'),('`a,b`','`ranking(student0)`','`a`'),('`a,b`','`ranking(student0)`','`intelligence(student0)`'),('`a,b`','`rating(course0)`','`b`'),('`a,b`','`salary(prof0,student0)`','`a`'),('`a,b`','`salary(prof0,student0)`','`capability(prof0,student0)`'),('`a,b`','`sat(course0,student0)`','`b`'),('`a,b`','`sat(course0,student0)`','`grade(course0,student0)`'),('`a,b`','`teachingability(prof0)`',''),('`a`','`a`',''),('`a`','`capability(prof0,student0)`','`a`'),('`a`','`intelligence(student0)`','`a`'),('`a`','`popularity(prof0)`','`a`'),('`a`','`popularity(prof0)`','`teachingability(prof0)`'),('`a`','`ranking(student0)`','`intelligence(student0)`'),('`a`','`salary(prof0,student0)`','`a`'),('`a`','`salary(prof0,student0)`','`capability(prof0,student0)`'),('`a`','`teachingability(prof0)`',''),('`b`','`b`',''),('`b`','`diff(course0)`','`grade(course0,student0)`'),('`b`','`grade(course0,student0)`','`b`'),('`b`','`grade(course0,student0)`','`intelligence(student0)`'),('`b`','`intelligence(student0)`',''),('`b`','`ranking(student0)`','`intelligence(student0)`'),('`b`','`rating(course0)`',''),('`b`','`sat(course0,student0)`','`b`'),('`b`','`sat(course0,student0)`','`grade(course0,student0)`');
/*!40000 ALTER TABLE `Path_BayesNets` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:11
