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
-- Table structure for table `RelationsParents`
--

DROP TABLE IF EXISTS `RelationsParents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RelationsParents` (
  `ChildNode` varchar(197) NOT NULL default '',
  `rnid` varchar(197) default NULL,
  `2node` varchar(197) NOT NULL default '',
  `NumAtts` bigint(20) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RelationsParents`
--

LOCK TABLES `RelationsParents` WRITE;
/*!40000 ALTER TABLE `RelationsParents` DISABLE KEYS */;
INSERT INTO `RelationsParents` VALUES ('`salary(prof0,student0)`','`a`','`capability(prof0,student0)`',5),('`diff(course0)`','`b`','`grade(course0,student0)`',4),('`sat(course0,student0)`','`b`','`grade(course0,student0)`',4),('`b`','`a`','`salary(prof0,student0)`',3),('`b`','`a`','`a`',1),('`capability(prof0,student0)`','`a`','`a`',1),('`grade(course0,student0)`','`b`','`b`',1),('`intelligence(student0)`','`a`','`a`',1),('`popularity(prof0)`','`a`','`a`',1),('`popularity(prof0)`','`b`','`b`',1),('`ranking(student0)`','`a`','`a`',1),('`rating(course0)`','`b`','`b`',1),('`salary(prof0,student0)`','`a`','`a`',1),('`sat(course0,student0)`','`b`','`b`',1);
/*!40000 ALTER TABLE `RelationsParents` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:09
