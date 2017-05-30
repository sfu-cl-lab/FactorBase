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
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b`','`a`','`diff(course0)`'),('`a,b`','`b`','`popularity(prof0)`'),('`a,b`','`a`','`rating(course0)`'),('`a,b`','`b`','`teachingability(prof0)`'),('`a,b`','`b`','`intelligence(student0)`'),('`a,b`','`a`','`intelligence(student0)`'),('`a,b`','`b`','`ranking(student0)`'),('`a,b`','`a`','`ranking(student0)`'),('`a,b`','`b`','`capability(prof0,student0)`'),('`a,b`','`a`','`grade(course0,student0)`'),('`a,b`','`b`','`salary(prof0,student0)`'),('`a,b`','`a`','`sat(course0,student0)`'),('`a,b`','`b`','`a`'),('`a,b`','`a`','`b`'),('`a,b`','`b`','`diff(course0)`'),('`a,b`','`a`','`popularity(prof0)`'),('`a,b`','`b`','`rating(course0)`'),('`a,b`','`a`','`teachingability(prof0)`'),('`b`','`b`','`diff(course0)`'),('`a`','`a`','`intelligence(student0)`'),('`b`','`b`','`intelligence(student0)`'),('`a`','`a`','`popularity(prof0)`'),('`a`','`a`','`ranking(student0)`'),('`b`','`b`','`ranking(student0)`'),('`b`','`b`','`rating(course0)`'),('`a`','`a`','`teachingability(prof0)`');
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

-- Dump completed on 2013-08-30 15:08:14
