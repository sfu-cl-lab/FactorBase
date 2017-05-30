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
-- Table structure for table `Path_Forbidden_Edges`
--

DROP TABLE IF EXISTS `Path_Forbidden_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_Forbidden_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY  (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Forbidden_Edges`
--

LOCK TABLES `Path_Forbidden_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Forbidden_Edges` DISABLE KEYS */;
INSERT INTO `Path_Forbidden_Edges` VALUES ('`a,b`','`capability(prof0,student0)`','`capability(prof0,student0)`'),('`a,b`','`capability(prof0,student0)`','`intelligence(student0)`'),('`a,b`','`capability(prof0,student0)`','`popularity(prof0)`'),('`a,b`','`capability(prof0,student0)`','`ranking(student0)`'),('`a,b`','`capability(prof0,student0)`','`salary(prof0,student0)`'),('`a,b`','`capability(prof0,student0)`','`teachingability(prof0)`'),('`a,b`','`diff(course0)`','`diff(course0)`'),('`a,b`','`diff(course0)`','`intelligence(student0)`'),('`a,b`','`diff(course0)`','`ranking(student0)`'),('`a,b`','`diff(course0)`','`rating(course0)`'),('`a,b`','`diff(course0)`','`sat(course0,student0)`'),('`a,b`','`grade(course0,student0)`','`diff(course0)`'),('`a,b`','`grade(course0,student0)`','`grade(course0,student0)`'),('`a,b`','`grade(course0,student0)`','`ranking(student0)`'),('`a,b`','`grade(course0,student0)`','`rating(course0)`'),('`a,b`','`grade(course0,student0)`','`sat(course0,student0)`'),('`a,b`','`intelligence(student0)`','`capability(prof0,student0)`'),('`a,b`','`intelligence(student0)`','`diff(course0)`'),('`a,b`','`intelligence(student0)`','`grade(course0,student0)`'),('`a,b`','`intelligence(student0)`','`intelligence(student0)`'),('`a,b`','`intelligence(student0)`','`popularity(prof0)`'),('`a,b`','`intelligence(student0)`','`ranking(student0)`'),('`a,b`','`intelligence(student0)`','`rating(course0)`'),('`a,b`','`intelligence(student0)`','`salary(prof0,student0)`'),('`a,b`','`intelligence(student0)`','`sat(course0,student0)`'),('`a,b`','`intelligence(student0)`','`teachingability(prof0)`'),('`a,b`','`popularity(prof0)`','`capability(prof0,student0)`'),('`a,b`','`popularity(prof0)`','`intelligence(student0)`'),('`a,b`','`popularity(prof0)`','`popularity(prof0)`'),('`a,b`','`popularity(prof0)`','`ranking(student0)`'),('`a,b`','`popularity(prof0)`','`salary(prof0,student0)`'),('`a,b`','`ranking(student0)`','`capability(prof0,student0)`'),('`a,b`','`ranking(student0)`','`diff(course0)`'),('`a,b`','`ranking(student0)`','`grade(course0,student0)`'),('`a,b`','`ranking(student0)`','`popularity(prof0)`'),('`a,b`','`ranking(student0)`','`ranking(student0)`'),('`a,b`','`ranking(student0)`','`rating(course0)`'),('`a,b`','`ranking(student0)`','`salary(prof0,student0)`'),('`a,b`','`ranking(student0)`','`sat(course0,student0)`'),('`a,b`','`ranking(student0)`','`teachingability(prof0)`'),('`a,b`','`rating(course0)`','`diff(course0)`'),('`a,b`','`rating(course0)`','`grade(course0,student0)`'),('`a,b`','`rating(course0)`','`intelligence(student0)`'),('`a,b`','`rating(course0)`','`ranking(student0)`'),('`a,b`','`rating(course0)`','`rating(course0)`'),('`a,b`','`rating(course0)`','`sat(course0,student0)`'),('`a,b`','`salary(prof0,student0)`','`intelligence(student0)`'),('`a,b`','`salary(prof0,student0)`','`popularity(prof0)`'),('`a,b`','`salary(prof0,student0)`','`ranking(student0)`'),('`a,b`','`salary(prof0,student0)`','`salary(prof0,student0)`'),('`a,b`','`salary(prof0,student0)`','`teachingability(prof0)`'),('`a,b`','`sat(course0,student0)`','`diff(course0)`'),('`a,b`','`sat(course0,student0)`','`intelligence(student0)`'),('`a,b`','`sat(course0,student0)`','`ranking(student0)`'),('`a,b`','`sat(course0,student0)`','`rating(course0)`'),('`a,b`','`sat(course0,student0)`','`sat(course0,student0)`'),('`a,b`','`teachingability(prof0)`','`capability(prof0,student0)`'),('`a,b`','`teachingability(prof0)`','`intelligence(student0)`'),('`a,b`','`teachingability(prof0)`','`popularity(prof0)`'),('`a,b`','`teachingability(prof0)`','`ranking(student0)`'),('`a,b`','`teachingability(prof0)`','`salary(prof0,student0)`'),('`a,b`','`teachingability(prof0)`','`teachingability(prof0)`'),('`a`','`intelligence(student0)`','`intelligence(student0)`'),('`a`','`intelligence(student0)`','`ranking(student0)`'),('`a`','`popularity(prof0)`','`popularity(prof0)`'),('`a`','`ranking(student0)`','`ranking(student0)`'),('`a`','`teachingability(prof0)`','`popularity(prof0)`'),('`a`','`teachingability(prof0)`','`teachingability(prof0)`'),('`b`','`diff(course0)`','`diff(course0)`'),('`b`','`diff(course0)`','`rating(course0)`'),('`b`','`intelligence(student0)`','`intelligence(student0)`'),('`b`','`intelligence(student0)`','`ranking(student0)`'),('`b`','`ranking(student0)`','`ranking(student0)`'),('`b`','`rating(course0)`','`diff(course0)`'),('`b`','`rating(course0)`','`rating(course0)`');
/*!40000 ALTER TABLE `Path_Forbidden_Edges` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:08:10
