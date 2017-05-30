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
-- Table structure for table `salary(prof0,student0)_CP`
--

DROP TABLE IF EXISTS `salary(prof0,student0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `salary(prof0,student0)_CP` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(45) default NULL,
  `a` varchar(5) default NULL,
  `capability(prof0,student0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salary(prof0,student0)_CP`
--

LOCK TABLES `salary(prof0,student0)_CP` WRITE;
/*!40000 ALTER TABLE `salary(prof0,student0)_CP` DISABLE KEYS */;
INSERT INTO `salary(prof0,student0)_CP` VALUES ('20','high','T','3',70,0.285714,-1.25276),('50','high','T','4',50,1.000000,0),('40','high','T','5',40,1.000000,0),('20','low','T','1',50,0.400000,-0.916291),('20','low','T','2',40,0.500000,-0.693147),('10','low','T','3',70,0.142857,-1.94591),('30','med','T','1',50,0.600000,-0.510826),('20','med','T','2',40,0.500000,-0.693147),('40','med','T','3',70,0.571429,-0.559615),('2030','N/A','F','N/A',2030,1.000000,0);
/*!40000 ALTER TABLE `salary(prof0,student0)_CP` ENABLE KEYS */;
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
