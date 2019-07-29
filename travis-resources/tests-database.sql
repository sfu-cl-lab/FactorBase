-- MySQL dump 10.13  Distrib 5.6.41, for Linux (x86_64)
--
-- Host: localhost    Database: tests-database
-- ------------------------------------------------------
-- Server version	5.6.41

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
-- Current Database: `tests-database`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `tests-database` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `tests-database`;

--
-- Table structure for table `prof0_counts`
--

DROP TABLE IF EXISTS `prof0_counts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prof0_counts` (
  `MULT` bigint(21) NOT NULL DEFAULT '0',
  `popularity(prof0)` varchar(45) DEFAULT NULL,
  `teachingability(prof0)` varchar(45) DEFAULT NULL,
  KEY `prof0_Index` (`popularity(prof0)`,`teachingability(prof0)`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prof0_counts`
--

LOCK TABLES `prof0_counts` WRITE;
/*!40000 ALTER TABLE `prof0_counts` DISABLE KEYS */;
INSERT INTO `prof0_counts` VALUES (2,'1','2'),(1,'2','2'),(3,'2','3');
/*!40000 ALTER TABLE `prof0_counts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sort-merge-t1`
--

DROP TABLE IF EXISTS `sort-merge-t1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sort-merge-t1` (
  `MULT` int(11) DEFAULT NULL,
  `attr1` varchar(6) DEFAULT NULL,
  `attr2` varchar(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sort-merge-t1`
--

LOCK TABLES `sort-merge-t1` WRITE;
/*!40000 ALTER TABLE `sort-merge-t1` DISABLE KEYS */;
INSERT INTO `sort-merge-t1` VALUES (10000,'match1','match2'),(1000,'match3','match4'),(100,'match1','miss1'),(10,'miss1','match1'),(1,'miss1','miss2');
/*!40000 ALTER TABLE `sort-merge-t1` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sort-merge-t2`
--

DROP TABLE IF EXISTS `sort-merge-t2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sort-merge-t2` (
  `MULT` int(11) DEFAULT NULL,
  `attr1` varchar(6) DEFAULT NULL,
  `attr2` varchar(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sort-merge-t2`
--

LOCK TABLES `sort-merge-t2` WRITE;
/*!40000 ALTER TABLE `sort-merge-t2` DISABLE KEYS */;
INSERT INTO `sort-merge-t2` VALUES (5,'match1','match2'),(4,'match3','match4'),(3,'match1','miss2'),(2,'miss2','match1'),(1,'miss2','miss1');
/*!40000 ALTER TABLE `sort-merge-t2` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t1`
--

DROP TABLE IF EXISTS `t1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t1` (
  `s_id` varchar(10) NOT NULL DEFAULT '',
  `attr1` tinyint(4) DEFAULT NULL,
  `attr2` tinyint(4) DEFAULT NULL,
  `attr3` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`s_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t1`
--

LOCK TABLES `t1` WRITE;
/*!40000 ALTER TABLE `t1` DISABLE KEYS */;
INSERT INTO `t1` VALUES ('A',NULL,NULL,NULL),('B',NULL,NULL,NULL),('Jack',3,1,'a'),('Jack2',NULL,1,'a'),('Kim',2,1,'b'),('Kim2',2,NULL,'b'),('Paul',1,2,'c'),('Paul2',1,2,NULL);
/*!40000 ALTER TABLE `t1` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t2`
--

DROP TABLE IF EXISTS `t2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t2` (
  `s_id` varchar(10) NOT NULL DEFAULT '',
  `attr1` tinyint(4) DEFAULT NULL,
  `attr2` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`s_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t2`
--

LOCK TABLES `t2` WRITE;
/*!40000 ALTER TABLE `t2` DISABLE KEYS */;
INSERT INTO `t2` VALUES ('B',NULL,NULL),('C',1,2),('Jack',3,1),('Kim2',2,NULL);
/*!40000 ALTER TABLE `t2` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-07-30 16:38:13
