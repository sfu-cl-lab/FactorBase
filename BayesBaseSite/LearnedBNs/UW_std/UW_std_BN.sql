CREATE DATABASE  IF NOT EXISTS `UW_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `UW_std_BN`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: UW_std_BN
-- ------------------------------------------------------
-- Server version	5.5.34

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
-- Table structure for table `1Nodes`
--

DROP TABLE IF EXISTS `1Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes` (
  `1nid` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `main` int(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes`
--

LOCK TABLES `1Nodes` WRITE;
/*!40000 ALTER TABLE `1Nodes` DISABLE KEYS */;
INSERT INTO `1Nodes` VALUES ('`courseLevel(course0)`','courseLevel','course0',1),('`hasPosition(person0)`','hasPosition','person0',1),('`hasPosition(person1)`','hasPosition','person1',0),('`inPhase(person0)`','inPhase','person0',1),('`inPhase(person1)`','inPhase','person1',0),('`professor(person0)`','professor','person0',1),('`professor(person1)`','professor','person1',0),('`student(person0)`','student','person0',1),('`student(person1)`','student','person1',0),('`yearsInProgram(person0)`','yearsInProgram','person0',1),('`yearsInProgram(person1)`','yearsInProgram','person1',0);
/*!40000 ALTER TABLE `1Nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `1Nodes_From_List`
--

DROP TABLE IF EXISTS `1Nodes_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_From_List` (
  `1nid` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(133) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_From_List`
--

LOCK TABLES `1Nodes_From_List` WRITE;
/*!40000 ALTER TABLE `1Nodes_From_List` DISABLE KEYS */;
INSERT INTO `1Nodes_From_List` VALUES ('`courseLevel(course0)`','course AS course0'),('`hasPosition(person0)`','person AS person0'),('`hasPosition(person1)`','person AS person1'),('`inPhase(person0)`','person AS person0'),('`inPhase(person1)`','person AS person1'),('`professor(person0)`','person AS person0'),('`professor(person1)`','person AS person1'),('`student(person0)`','person AS person0'),('`student(person1)`','person AS person1'),('`yearsInProgram(person0)`','person AS person0'),('`yearsInProgram(person1)`','person AS person1');
/*!40000 ALTER TABLE `1Nodes_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `1Nodes_Select_List`
--

DROP TABLE IF EXISTS `1Nodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `1Nodes_Select_List` (
  `1nid` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(267) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `1Nodes_Select_List`
--

LOCK TABLES `1Nodes_Select_List` WRITE;
/*!40000 ALTER TABLE `1Nodes_Select_List` DISABLE KEYS */;
INSERT INTO `1Nodes_Select_List` VALUES ('`courseLevel(course0)`','course0.courseLevel AS `courseLevel(course0)`'),('`hasPosition(person0)`','person0.hasPosition AS `hasPosition(person0)`'),('`hasPosition(person1)`','person1.hasPosition AS `hasPosition(person1)`'),('`inPhase(person0)`','person0.inPhase AS `inPhase(person0)`'),('`inPhase(person1)`','person1.inPhase AS `inPhase(person1)`'),('`professor(person0)`','person0.professor AS `professor(person0)`'),('`professor(person1)`','person1.professor AS `professor(person1)`'),('`student(person0)`','person0.student AS `student(person0)`'),('`student(person1)`','person1.student AS `student(person1)`'),('`yearsInProgram(person0)`','person0.yearsInProgram AS `yearsInProgram(person0)`'),('`yearsInProgram(person1)`','person1.yearsInProgram AS `yearsInProgram(person1)`');
/*!40000 ALTER TABLE `1Nodes_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `2Nodes`
--

DROP TABLE IF EXISTS `2Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `2Nodes` (
  `2nid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid1` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid2` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `main` int(11) NOT NULL DEFAULT '0',
  KEY `index` (`pvid1`,`pvid2`,`TABLE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `2Nodes`
--

LOCK TABLES `2Nodes` WRITE;
/*!40000 ALTER TABLE `2Nodes` DISABLE KEYS */;
/*!40000 ALTER TABLE `2Nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `2Nodes_From_List`
--

DROP TABLE IF EXISTS `2Nodes_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `2Nodes_From_List` (
  `2nid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(78) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `2Nodes_From_List`
--

LOCK TABLES `2Nodes_From_List` WRITE;
/*!40000 ALTER TABLE `2Nodes_From_List` DISABLE KEYS */;
/*!40000 ALTER TABLE `2Nodes_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `2Nodes_Select_List`
--

DROP TABLE IF EXISTS `2Nodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `2Nodes_Select_List` (
  `2nid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(278) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `2Nodes_Select_List`
--

LOCK TABLES `2Nodes_Select_List` WRITE;
/*!40000 ALTER TABLE `2Nodes_Select_List` DISABLE KEYS */;
/*!40000 ALTER TABLE `2Nodes_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_PVariables_From_List`
--

DROP TABLE IF EXISTS `ADT_PVariables_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_PVariables_From_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(140) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_From_List`
--

LOCK TABLES `ADT_PVariables_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_From_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_From_List` VALUES ('course0','UW_std.course AS course0'),('person0','UW_std.person AS person0'),('person1','UW_std.person AS person1');
/*!40000 ALTER TABLE `ADT_PVariables_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_PVariables_GroupBy_List`
--

DROP TABLE IF EXISTS `ADT_PVariables_GroupBy_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_PVariables_GroupBy_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_GroupBy_List`
--

LOCK TABLES `ADT_PVariables_GroupBy_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_GroupBy_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_GroupBy_List` VALUES ('course0','`courseLevel(course0)`'),('person0','`hasPosition(person0)`'),('person1','`hasPosition(person1)`'),('person0','`inPhase(person0)`'),('person1','`inPhase(person1)`'),('person0','`professor(person0)`'),('person1','`professor(person1)`'),('person0','`student(person0)`'),('person1','`student(person1)`'),('person0','`yearsInProgram(person0)`'),('person1','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_PVariables_GroupBy_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_PVariables_Select_List`
--

DROP TABLE IF EXISTS `ADT_PVariables_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_PVariables_Select_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(267) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_Select_List`
--

LOCK TABLES `ADT_PVariables_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_Select_List` VALUES ('course0','count(*) as \"MULT\"'),('person0','count(*) as \"MULT\"'),('person1','count(*) as \"MULT\"'),('course0','course0.courseLevel AS `courseLevel(course0)`'),('person0','person0.hasPosition AS `hasPosition(person0)`'),('person1','person1.hasPosition AS `hasPosition(person1)`'),('person0','person0.inPhase AS `inPhase(person0)`'),('person1','person1.inPhase AS `inPhase(person1)`'),('person0','person0.professor AS `professor(person0)`'),('person1','person1.professor AS `professor(person1)`'),('person0','person0.student AS `student(person0)`'),('person1','person1.student AS `student(person1)`'),('person0','person0.yearsInProgram AS `yearsInProgram(person0)`'),('person1','person1.yearsInProgram AS `yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_PVariables_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RChain_Star_From_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_From_List` (
  `rchain` varchar(20) NOT NULL DEFAULT '',
  `rnid` varchar(20) DEFAULT NULL,
  `Entries` varchar(74) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_From_List`
--

LOCK TABLES `ADT_RChain_Star_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_From_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_From_List` VALUES ('`a,b,c`','`c`','`a,b_CT`'),('`a,b,c`','`b`','`a,c_CT`'),('`a,b`','`b`','`a_CT`'),('`a,c`','`c`','`a_CT`'),('`a,b,c`','`a`','`b,c_CT`'),('`a,b`','`a`','`b_CT`'),('`b,c`','`c`','`b_CT`'),('`a,c`','`a`','`c_CT`'),('`b,c`','`b`','`c_CT`'),('`a,b`','`b`','`course0_counts`'),('`a,c`','`c`','`course0_counts`'),('`a,b`','`a`','`person1_counts`'),('`b,c`','`c`','`person1_counts`'),('`a,c`','`a`','`person0_counts`'),('`b,c`','`b`','`person0_counts`');
/*!40000 ALTER TABLE `ADT_RChain_Star_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RChain_Star_Select_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_Select_List` (
  `rchain` varchar(20) DEFAULT NULL,
  `rnid` varchar(20) DEFAULT NULL,
  `Entries` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Select_List`
--

LOCK TABLES `ADT_RChain_Star_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b,c`','`c`','`courseLevel(course0)`'),('`a,b,c`','`a`','`courseLevel(course0)`'),('`a,b`','`a`','`courseLevel(course0)`'),('`b,c`','`c`','`courseLevel(course0)`'),('`a,b,c`','`b`','`courseLevel(course0)`'),('`a,c`','`a`','`courseLevel(course0)`'),('`b,c`','`b`','`courseLevel(course0)`'),('`a,b,c`','`c`','`hasPosition(person0)`'),('`a,b,c`','`b`','`hasPosition(person0)`'),('`a,b`','`b`','`hasPosition(person0)`'),('`a,c`','`c`','`hasPosition(person0)`'),('`a,b,c`','`c`','`inPhase(person0)`'),('`a,b,c`','`b`','`inPhase(person0)`'),('`a,b`','`b`','`inPhase(person0)`'),('`a,c`','`c`','`inPhase(person0)`'),('`a,b,c`','`c`','`professor(person0)`'),('`a,b,c`','`b`','`professor(person0)`'),('`a,b`','`b`','`professor(person0)`'),('`a,c`','`c`','`professor(person0)`'),('`a,b,c`','`c`','`student(person0)`'),('`a,b,c`','`b`','`student(person0)`'),('`a,b`','`b`','`student(person0)`'),('`a,c`','`c`','`student(person0)`'),('`a,b,c`','`c`','`yearsInProgram(person0)`'),('`a,b,c`','`b`','`yearsInProgram(person0)`'),('`a,b`','`b`','`yearsInProgram(person0)`'),('`a,c`','`c`','`yearsInProgram(person0)`'),('`a,b,c`','`a`','`hasPosition(person0)`'),('`a,b`','`a`','`hasPosition(person0)`'),('`b,c`','`c`','`hasPosition(person0)`'),('`a,b,c`','`c`','`hasPosition(person1)`'),('`a,b,c`','`b`','`hasPosition(person1)`'),('`a,b`','`b`','`hasPosition(person1)`'),('`a,c`','`c`','`hasPosition(person1)`'),('`a,b,c`','`a`','`hasPosition(person1)`'),('`a,c`','`a`','`hasPosition(person1)`'),('`b,c`','`b`','`hasPosition(person1)`'),('`a,b,c`','`a`','`inPhase(person0)`'),('`a,b`','`a`','`inPhase(person0)`'),('`b,c`','`c`','`inPhase(person0)`'),('`a,b,c`','`c`','`inPhase(person1)`'),('`a,b,c`','`b`','`inPhase(person1)`'),('`a,b`','`b`','`inPhase(person1)`'),('`a,c`','`c`','`inPhase(person1)`'),('`a,b,c`','`a`','`inPhase(person1)`'),('`a,c`','`a`','`inPhase(person1)`'),('`b,c`','`b`','`inPhase(person1)`'),('`a,b,c`','`a`','`professor(person0)`'),('`a,b`','`a`','`professor(person0)`'),('`b,c`','`c`','`professor(person0)`'),('`a,b,c`','`c`','`professor(person1)`'),('`a,b,c`','`b`','`professor(person1)`'),('`a,b`','`b`','`professor(person1)`'),('`a,c`','`c`','`professor(person1)`'),('`a,b,c`','`a`','`professor(person1)`'),('`a,c`','`a`','`professor(person1)`'),('`b,c`','`b`','`professor(person1)`'),('`a,b,c`','`a`','`student(person0)`'),('`a,b`','`a`','`student(person0)`'),('`b,c`','`c`','`student(person0)`'),('`a,b,c`','`c`','`student(person1)`'),('`a,b,c`','`b`','`student(person1)`'),('`a,b`','`b`','`student(person1)`'),('`a,c`','`c`','`student(person1)`'),('`a,b,c`','`a`','`student(person1)`'),('`a,c`','`a`','`student(person1)`'),('`b,c`','`b`','`student(person1)`'),('`a,b,c`','`a`','`yearsInProgram(person0)`'),('`a,b`','`a`','`yearsInProgram(person0)`'),('`b,c`','`c`','`yearsInProgram(person0)`'),('`a,b,c`','`c`','`yearsInProgram(person1)`'),('`a,b,c`','`b`','`yearsInProgram(person1)`'),('`a,b`','`b`','`yearsInProgram(person1)`'),('`a,c`','`c`','`yearsInProgram(person1)`'),('`a,b,c`','`a`','`yearsInProgram(person1)`'),('`a,c`','`a`','`yearsInProgram(person1)`'),('`b,c`','`b`','`yearsInProgram(person1)`'),('`a,b`','`b`','`courseLevel(course0)`'),('`a,c`','`c`','`courseLevel(course0)`'),('`a,c`','`a`','`hasPosition(person0)`'),('`b,c`','`b`','`hasPosition(person0)`'),('`a,b`','`a`','`hasPosition(person1)`'),('`b,c`','`c`','`hasPosition(person1)`'),('`a,c`','`a`','`inPhase(person0)`'),('`b,c`','`b`','`inPhase(person0)`'),('`a,b`','`a`','`inPhase(person1)`'),('`b,c`','`c`','`inPhase(person1)`'),('`a,c`','`a`','`professor(person0)`'),('`b,c`','`b`','`professor(person0)`'),('`a,b`','`a`','`professor(person1)`'),('`b,c`','`c`','`professor(person1)`'),('`a,c`','`a`','`student(person0)`'),('`b,c`','`b`','`student(person0)`'),('`a,b`','`a`','`student(person1)`'),('`b,c`','`c`','`student(person1)`'),('`a,c`','`a`','`yearsInProgram(person0)`'),('`b,c`','`b`','`yearsInProgram(person0)`'),('`a,b`','`a`','`yearsInProgram(person1)`'),('`b,c`','`c`','`yearsInProgram(person1)`'),('`b`','`b`','`courseLevel(course0)`'),('`c`','`c`','`courseLevel(course0)`'),('`a`','`a`','`hasPosition(person0)`'),('`b`','`b`','`hasPosition(person0)`'),('`a`','`a`','`hasPosition(person1)`'),('`c`','`c`','`hasPosition(person1)`'),('`a`','`a`','`inPhase(person0)`'),('`b`','`b`','`inPhase(person0)`'),('`a`','`a`','`inPhase(person1)`'),('`c`','`c`','`inPhase(person1)`'),('`a`','`a`','`professor(person0)`'),('`b`','`b`','`professor(person0)`'),('`a`','`a`','`professor(person1)`'),('`c`','`c`','`professor(person1)`'),('`a`','`a`','`student(person0)`'),('`b`','`b`','`student(person0)`'),('`a`','`a`','`student(person1)`'),('`c`','`c`','`student(person1)`'),('`a`','`a`','`yearsInProgram(person0)`'),('`b`','`b`','`yearsInProgram(person0)`'),('`a`','`a`','`yearsInProgram(person1)`'),('`c`','`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RChain_Star_Where_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_Where_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_Where_List` (
  `rchain` varchar(20) NOT NULL DEFAULT '',
  `rnid` varchar(20) DEFAULT NULL,
  `Entries` varchar(26) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Where_List`
--

LOCK TABLES `ADT_RChain_Star_Where_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Where_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Where_List` VALUES ('`a,b,c`','`b`','`c` = \"T\"'),('`a,b,c`','`a`','`b` = \"T\"'),('`a,b,c`','`a`','`c` = \"T\"'),('`a,b`','`a`','`b` = \"T\"'),('`a,c`','`a`','`c` = \"T\"'),('`b,c`','`b`','`c` = \"T\"');
/*!40000 ALTER TABLE `ADT_RChain_Star_Where_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_1Nodes_FROM_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_1Nodes_FROM_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_1Nodes_FROM_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(19) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_1Nodes_FROM_List`
--

LOCK TABLES `ADT_RNodes_1Nodes_FROM_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_FROM_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_1Nodes_FROM_List` VALUES ('`a`','`a_counts`'),('`b`','`b_counts`'),('`c`','`c_counts`');
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_FROM_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_1Nodes_GroupBY_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_1Nodes_GroupBY_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_1Nodes_GroupBY_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_1Nodes_GroupBY_List`
--

LOCK TABLES `ADT_RNodes_1Nodes_GroupBY_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_GroupBY_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_1Nodes_GroupBY_List` VALUES ('`b`','`courseLevel(course0)`'),('`c`','`courseLevel(course0)`'),('`a`','`hasPosition(person0)`'),('`a`','`inPhase(person0)`'),('`a`','`professor(person0)`'),('`a`','`student(person0)`'),('`a`','`yearsInProgram(person0)`'),('`b`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`'),('`b`','`inPhase(person0)`'),('`a`','`inPhase(person1)`'),('`c`','`inPhase(person1)`'),('`b`','`professor(person0)`'),('`a`','`professor(person1)`'),('`c`','`professor(person1)`'),('`b`','`student(person0)`'),('`a`','`student(person1)`'),('`c`','`student(person1)`'),('`b`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_GroupBY_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_1Nodes_Select_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_1Nodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_1Nodes_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(133) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_1Nodes_Select_List`
--

LOCK TABLES `ADT_RNodes_1Nodes_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_1Nodes_Select_List` VALUES ('`a`','sum(`a_counts`.`MULT`) as \"MULT\"'),('`b`','sum(`b_counts`.`MULT`) as \"MULT\"'),('`c`','sum(`c_counts`.`MULT`) as \"MULT\"'),('`b`','`courseLevel(course0)`'),('`c`','`courseLevel(course0)`'),('`a`','`hasPosition(person0)`'),('`a`','`inPhase(person0)`'),('`a`','`professor(person0)`'),('`a`','`student(person0)`'),('`a`','`yearsInProgram(person0)`'),('`b`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`'),('`b`','`inPhase(person0)`'),('`a`','`inPhase(person1)`'),('`c`','`inPhase(person1)`'),('`b`','`professor(person0)`'),('`a`','`professor(person1)`'),('`c`','`professor(person1)`'),('`b`','`student(person0)`'),('`a`','`student(person1)`'),('`c`','`student(person1)`'),('`b`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RNodes_1Nodes_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_False_FROM_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_False_FROM_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_False_FROM_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(17) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_False_FROM_List`
--

LOCK TABLES `ADT_RNodes_False_FROM_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_False_FROM_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_False_FROM_List` VALUES ('`a`','`a_star`'),('`b`','`b_star`'),('`c`','`c_star`'),('`a`','`a_flat`'),('`b`','`b_flat`'),('`c`','`c_flat`');
/*!40000 ALTER TABLE `ADT_RNodes_False_FROM_List` ENABLE KEYS */;
UNLOCK TABLES;

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
INSERT INTO `ADT_RNodes_False_Select_List` VALUES ('`a`','(`a_star`.MULT-`a_flat`.MULT) AS \"MULT\"'),('`b`','(`b_star`.MULT-`b_flat`.MULT) AS \"MULT\"'),('`c`','(`c_star`.MULT-`c_flat`.MULT) AS \"MULT\"'),('`b`','`b_star`.`courseLevel(course0)`'),('`c`','`c_star`.`courseLevel(course0)`'),('`a`','`a_star`.`hasPosition(person0)`'),('`a`','`a_star`.`inPhase(person0)`'),('`a`','`a_star`.`professor(person0)`'),('`a`','`a_star`.`student(person0)`'),('`a`','`a_star`.`yearsInProgram(person0)`'),('`b`','`b_star`.`hasPosition(person0)`'),('`a`','`a_star`.`hasPosition(person1)`'),('`c`','`c_star`.`hasPosition(person1)`'),('`b`','`b_star`.`inPhase(person0)`'),('`a`','`a_star`.`inPhase(person1)`'),('`c`','`c_star`.`inPhase(person1)`'),('`b`','`b_star`.`professor(person0)`'),('`a`','`a_star`.`professor(person1)`'),('`c`','`c_star`.`professor(person1)`'),('`b`','`b_star`.`student(person0)`'),('`a`','`a_star`.`student(person1)`'),('`c`','`c_star`.`student(person1)`'),('`b`','`b_star`.`yearsInProgram(person0)`'),('`a`','`a_star`.`yearsInProgram(person1)`'),('`c`','`c_star`.`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RNodes_False_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_False_WHERE_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_False_WHERE_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_False_WHERE_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(303) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_False_WHERE_List`
--

LOCK TABLES `ADT_RNodes_False_WHERE_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_False_WHERE_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_False_WHERE_List` VALUES ('`b`','`b_star`.`courseLevel(course0)`=`b_flat`.`courseLevel(course0)`'),('`c`','`c_star`.`courseLevel(course0)`=`c_flat`.`courseLevel(course0)`'),('`a`','`a_star`.`hasPosition(person0)`=`a_flat`.`hasPosition(person0)`'),('`a`','`a_star`.`inPhase(person0)`=`a_flat`.`inPhase(person0)`'),('`a`','`a_star`.`professor(person0)`=`a_flat`.`professor(person0)`'),('`a`','`a_star`.`student(person0)`=`a_flat`.`student(person0)`'),('`a`','`a_star`.`yearsInProgram(person0)`=`a_flat`.`yearsInProgram(person0)`'),('`b`','`b_star`.`hasPosition(person0)`=`b_flat`.`hasPosition(person0)`'),('`a`','`a_star`.`hasPosition(person1)`=`a_flat`.`hasPosition(person1)`'),('`c`','`c_star`.`hasPosition(person1)`=`c_flat`.`hasPosition(person1)`'),('`b`','`b_star`.`inPhase(person0)`=`b_flat`.`inPhase(person0)`'),('`a`','`a_star`.`inPhase(person1)`=`a_flat`.`inPhase(person1)`'),('`c`','`c_star`.`inPhase(person1)`=`c_flat`.`inPhase(person1)`'),('`b`','`b_star`.`professor(person0)`=`b_flat`.`professor(person0)`'),('`a`','`a_star`.`professor(person1)`=`a_flat`.`professor(person1)`'),('`c`','`c_star`.`professor(person1)`=`c_flat`.`professor(person1)`'),('`b`','`b_star`.`student(person0)`=`b_flat`.`student(person0)`'),('`a`','`a_star`.`student(person1)`=`a_flat`.`student(person1)`'),('`c`','`c_star`.`student(person1)`=`c_flat`.`student(person1)`'),('`b`','`b_star`.`yearsInProgram(person0)`=`b_flat`.`yearsInProgram(person0)`'),('`a`','`a_star`.`yearsInProgram(person1)`=`a_flat`.`yearsInProgram(person1)`'),('`c`','`c_star`.`yearsInProgram(person1)`=`c_flat`.`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RNodes_False_WHERE_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_Star_From_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_Star_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_Star_From_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(74) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_Star_From_List`
--

LOCK TABLES `ADT_RNodes_Star_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_Star_From_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_Star_From_List` VALUES ('`a`','`person0_counts`'),('`b`','`course0_counts`'),('`c`','`course0_counts`'),('`a`','`person1_counts`'),('`b`','`person0_counts`'),('`c`','`person1_counts`');
/*!40000 ALTER TABLE `ADT_RNodes_Star_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ADT_RNodes_Star_Select_List`
--

DROP TABLE IF EXISTS `ADT_RNodes_Star_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RNodes_Star_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RNodes_Star_Select_List`
--

LOCK TABLES `ADT_RNodes_Star_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RNodes_Star_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RNodes_Star_Select_List` VALUES ('`b`','`courseLevel(course0)`'),('`c`','`courseLevel(course0)`'),('`a`','`hasPosition(person0)`'),('`a`','`inPhase(person0)`'),('`a`','`professor(person0)`'),('`a`','`student(person0)`'),('`a`','`yearsInProgram(person0)`'),('`b`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`'),('`b`','`inPhase(person0)`'),('`a`','`inPhase(person1)`'),('`c`','`inPhase(person1)`'),('`b`','`professor(person0)`'),('`a`','`professor(person1)`'),('`c`','`professor(person1)`'),('`b`','`student(person0)`'),('`a`','`student(person1)`'),('`c`','`student(person1)`'),('`b`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `ADT_RNodes_Star_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AttributeColumns`
--

DROP TABLE IF EXISTS `AttributeColumns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AttributeColumns` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AttributeColumns`
--

LOCK TABLES `AttributeColumns` WRITE;
/*!40000 ALTER TABLE `AttributeColumns` DISABLE KEYS */;
INSERT INTO `AttributeColumns` VALUES ('course','courseLevel'),('person','hasPosition'),('person','inPhase'),('person','professor'),('person','student'),('person','yearsInProgram');
/*!40000 ALTER TABLE `AttributeColumns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Attribute_Value`
--

DROP TABLE IF EXISTS `Attribute_Value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Attribute_Value` (
  `COLUMN_NAME` varchar(30) DEFAULT NULL,
  `VALUE` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Attribute_Value`
--

LOCK TABLES `Attribute_Value` WRITE;
/*!40000 ALTER TABLE `Attribute_Value` DISABLE KEYS */;
INSERT INTO `Attribute_Value` VALUES ('courseLevel','Level_300'),('courseLevel','Level_400'),('courseLevel','Level_500'),('hasPosition','0'),('hasPosition','Faculty'),('hasPosition','Faculty_adj'),('hasPosition','Faculty_aff'),('hasPosition','Faculty_eme'),('inPhase','0'),('inPhase','Post_Generals'),('inPhase','Post_Quals'),('inPhase','Pre_Quals'),('professor','0'),('professor','1'),('student','0'),('student','1'),('yearsInProgram','0'),('yearsInProgram','Year_1'),('yearsInProgram','Year_10'),('yearsInProgram','Year_12'),('yearsInProgram','Year_2'),('yearsInProgram','Year_3'),('yearsInProgram','Year_4'),('yearsInProgram','Year_5'),('yearsInProgram','Year_6'),('yearsInProgram','Year_7'),('yearsInProgram','Year_8'),('yearsInProgram','Year_9');
/*!40000 ALTER TABLE `Attribute_Value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ContextEdges`
--

DROP TABLE IF EXISTS `ContextEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContextEdges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ContextEdges`
--

LOCK TABLES `ContextEdges` WRITE;
/*!40000 ALTER TABLE `ContextEdges` DISABLE KEYS */;
/*!40000 ALTER TABLE `ContextEdges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EntityTables`
--

DROP TABLE IF EXISTS `EntityTables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EntityTables` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EntityTables`
--

LOCK TABLES `EntityTables` WRITE;
/*!40000 ALTER TABLE `EntityTables` DISABLE KEYS */;
INSERT INTO `EntityTables` VALUES ('course','course_id'),('person','p_id');
/*!40000 ALTER TABLE `EntityTables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary table structure for view `Entity_BN_nodes`
--

DROP TABLE IF EXISTS `Entity_BN_nodes`;
/*!50001 DROP VIEW IF EXISTS `Entity_BN_nodes`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `Entity_BN_nodes` (
  `pvid` tinyint NOT NULL,
  `node` tinyint NOT NULL
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `Entity_BayesNets`
--

DROP TABLE IF EXISTS `Entity_BayesNets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Entity_BayesNets` (
  `pvid` varchar(65) NOT NULL,
  `child` varchar(131) NOT NULL,
  `parent` varchar(131) NOT NULL,
  PRIMARY KEY (`pvid`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Entity_BayesNets`
--

LOCK TABLES `Entity_BayesNets` WRITE;
/*!40000 ALTER TABLE `Entity_BayesNets` DISABLE KEYS */;
/*!40000 ALTER TABLE `Entity_BayesNets` ENABLE KEYS */;
UNLOCK TABLES;

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
/*!40000 ALTER TABLE `Entity_Complement_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FNodes`
--

DROP TABLE IF EXISTS `FNodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FNodes` (
  `Fid` varchar(199) NOT NULL DEFAULT '',
  `FunctorName` varchar(64) DEFAULT NULL,
  `Type` varchar(5) DEFAULT NULL,
  `main` int(11) DEFAULT NULL,
  PRIMARY KEY (`Fid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FNodes`
--

LOCK TABLES `FNodes` WRITE;
/*!40000 ALTER TABLE `FNodes` DISABLE KEYS */;
INSERT INTO `FNodes` VALUES ('`a`','advisedBy','Rnode',1),('`b`','taughtBy','Rnode',1),('`courseLevel(course0)`','courseLevel','1Node',1),('`c`','taughtBy','Rnode',0),('`hasPosition(person0)`','hasPosition','1Node',1),('`hasPosition(person1)`','hasPosition','1Node',0),('`inPhase(person0)`','inPhase','1Node',1),('`inPhase(person1)`','inPhase','1Node',0),('`professor(person0)`','professor','1Node',1),('`professor(person1)`','professor','1Node',0),('`student(person0)`','student','1Node',1),('`student(person1)`','student','1Node',0),('`yearsInProgram(person0)`','yearsInProgram','1Node',1),('`yearsInProgram(person1)`','yearsInProgram','1Node',0);
/*!40000 ALTER TABLE `FNodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FNodes_pvars`
--

DROP TABLE IF EXISTS `FNodes_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FNodes_pvars` (
  `Fid` varchar(199) NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FNodes_pvars`
--

LOCK TABLES `FNodes_pvars` WRITE;
/*!40000 ALTER TABLE `FNodes_pvars` DISABLE KEYS */;
INSERT INTO `FNodes_pvars` VALUES ('`courseLevel(course0)`','course0'),('`hasPosition(person0)`','person0'),('`hasPosition(person1)`','person1'),('`inPhase(person0)`','person0'),('`inPhase(person1)`','person1'),('`professor(person0)`','person0'),('`professor(person1)`','person1'),('`student(person0)`','person0'),('`student(person1)`','person1'),('`yearsInProgram(person0)`','person0'),('`yearsInProgram(person1)`','person1');
/*!40000 ALTER TABLE `FNodes_pvars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ForeignKeyColumns`
--

DROP TABLE IF EXISTS `ForeignKeyColumns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ForeignKeyColumns` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_COLUMN_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `CONSTRAINT_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `ORDINAL_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ForeignKeyColumns`
--

LOCK TABLES `ForeignKeyColumns` WRITE;
/*!40000 ALTER TABLE `ForeignKeyColumns` DISABLE KEYS */;
INSERT INTO `ForeignKeyColumns` VALUES ('advisedBy','p_id','person','p_id','FK_advisedBy_person',1),('advisedBy','p_id_dummy','person','p_id','FK_advisedBy_person_2',2),('taughtBy','course_id','course','course_id','FK_taught_course',1),('taughtBy','p_id','person','p_id','FK_taught_person',2);
/*!40000 ALTER TABLE `ForeignKeyColumns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ForeignKeys_pvars`
--

DROP TABLE IF EXISTS `ForeignKeys_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ForeignKeys_pvars` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `index_number` bigint(20) NOT NULL DEFAULT '0',
  `ARGUMENT_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ForeignKeys_pvars`
--

LOCK TABLES `ForeignKeys_pvars` WRITE;
/*!40000 ALTER TABLE `ForeignKeys_pvars` DISABLE KEYS */;
INSERT INTO `ForeignKeys_pvars` VALUES ('advisedBy','person','p_id','person0',0,1),('advisedBy','person','p_id_dummy','person0',0,2),('advisedBy','person','p_id','person1',1,1),('advisedBy','person','p_id_dummy','person1',1,2),('taughtBy','course','course_id','course0',0,1),('taughtBy','person','p_id','person0',0,2),('taughtBy','person','p_id','person1',1,2);
/*!40000 ALTER TABLE `ForeignKeys_pvars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `InheritedEdges`
--

DROP TABLE IF EXISTS `InheritedEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `InheritedEdges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `InheritedEdges`
--

LOCK TABLES `InheritedEdges` WRITE;
/*!40000 ALTER TABLE `InheritedEdges` DISABLE KEYS */;
/*!40000 ALTER TABLE `InheritedEdges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `InputColumns`
--

DROP TABLE IF EXISTS `InputColumns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `InputColumns` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_TABLE_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `REFERENCED_COLUMN_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `CONSTRAINT_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `ORDINAL_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `InputColumns`
--

LOCK TABLES `InputColumns` WRITE;
/*!40000 ALTER TABLE `InputColumns` DISABLE KEYS */;
INSERT INTO `InputColumns` VALUES ('advisedBy','p_id_dummy',NULL,NULL,'PRIMARY',2),('advisedBy','p_id',NULL,NULL,'PRIMARY',1),('course','course_id',NULL,NULL,'PRIMARY',1),('person','p_id',NULL,NULL,'PRIMARY',1),('taughtBy','course_id',NULL,NULL,'PRIMARY',1),('taughtBy','p_id',NULL,NULL,'PRIMARY',2);
/*!40000 ALTER TABLE `InputColumns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Knowledge_Forbidden_Edges`
--

DROP TABLE IF EXISTS `Knowledge_Forbidden_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Knowledge_Forbidden_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Knowledge_Forbidden_Edges`
--

LOCK TABLES `Knowledge_Forbidden_Edges` WRITE;
/*!40000 ALTER TABLE `Knowledge_Forbidden_Edges` DISABLE KEYS */;
/*!40000 ALTER TABLE `Knowledge_Forbidden_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Knowledge_Required_Edges`
--

DROP TABLE IF EXISTS `Knowledge_Required_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Knowledge_Required_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Knowledge_Required_Edges`
--

LOCK TABLES `Knowledge_Required_Edges` WRITE;
/*!40000 ALTER TABLE `Knowledge_Required_Edges` DISABLE KEYS */;
/*!40000 ALTER TABLE `Knowledge_Required_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `LearnedEdges`
--

DROP TABLE IF EXISTS `LearnedEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LearnedEdges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `LearnedEdges`
--

LOCK TABLES `LearnedEdges` WRITE;
/*!40000 ALTER TABLE `LearnedEdges` DISABLE KEYS */;
/*!40000 ALTER TABLE `LearnedEdges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NewLearnedEdges`
--

DROP TABLE IF EXISTS `NewLearnedEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NewLearnedEdges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NewLearnedEdges`
--

LOCK TABLES `NewLearnedEdges` WRITE;
/*!40000 ALTER TABLE `NewLearnedEdges` DISABLE KEYS */;
/*!40000 ALTER TABLE `NewLearnedEdges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NoPKeys`
--

DROP TABLE IF EXISTS `NoPKeys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NoPKeys` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NoPKeys`
--

LOCK TABLES `NoPKeys` WRITE;
/*!40000 ALTER TABLE `NoPKeys` DISABLE KEYS */;
/*!40000 ALTER TABLE `NoPKeys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PVariables`
--

DROP TABLE IF EXISTS `PVariables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PVariables` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `index_number` bigint(20) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PVariables`
--

LOCK TABLES `PVariables` WRITE;
/*!40000 ALTER TABLE `PVariables` DISABLE KEYS */;
INSERT INTO `PVariables` VALUES ('course0','course',0),('person0','person',0),('person1','person',1);
/*!40000 ALTER TABLE `PVariables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PVariables_From_List`
--

DROP TABLE IF EXISTS `PVariables_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PVariables_From_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(133) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PVariables_From_List`
--

LOCK TABLES `PVariables_From_List` WRITE;
/*!40000 ALTER TABLE `PVariables_From_List` DISABLE KEYS */;
INSERT INTO `PVariables_From_List` VALUES ('course0','course AS course0'),('person0','person AS person0');
/*!40000 ALTER TABLE `PVariables_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PVariables_GroupBy_List`
--

DROP TABLE IF EXISTS `PVariables_GroupBy_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PVariables_GroupBy_List` (
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `Entries` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PVariables_GroupBy_List`
--

LOCK TABLES `PVariables_GroupBy_List` WRITE;
/*!40000 ALTER TABLE `PVariables_GroupBy_List` DISABLE KEYS */;
INSERT INTO `PVariables_GroupBy_List` VALUES ('course0','`courseLevel(course0)`'),('person0','`hasPosition(person0)`'),('person1','`hasPosition(person1)`'),('person0','`inPhase(person0)`'),('person1','`inPhase(person1)`'),('person0','`professor(person0)`'),('person1','`professor(person1)`'),('person0','`student(person0)`'),('person1','`student(person1)`'),('person0','`yearsInProgram(person0)`'),('person1','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `PVariables_GroupBy_List` ENABLE KEYS */;
UNLOCK TABLES;

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
INSERT INTO `PVariables_Select_List` VALUES ('course0','count(*) as \"MULT\"'),('person0','count(*) as \"MULT\"'),('person1','count(*) as \"MULT\"'),('course0','course0.courseLevel AS `courseLevel(course0)`'),('person0','person0.hasPosition AS `hasPosition(person0)`'),('person0','person0.inPhase AS `inPhase(person0)`'),('person0','person0.professor AS `professor(person0)`'),('person0','person0.student AS `student(person0)`'),('person0','person0.yearsInProgram AS `yearsInProgram(person0)`');
/*!40000 ALTER TABLE `PVariables_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Path_Aux_Edges`
--

DROP TABLE IF EXISTS `Path_Aux_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_Aux_Edges` (
  `Rchain` varchar(20) NOT NULL DEFAULT '',
  `child` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `parent` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Aux_Edges`
--

LOCK TABLES `Path_Aux_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Aux_Edges` DISABLE KEYS */;
INSERT INTO `Path_Aux_Edges` VALUES ('`a,b,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,b,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,b,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,b,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,b,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,b,c`','`hasPosition(person1)`','`professor(person0)`'),('`a,b,c`','`hasPosition(person1)`','`professor(person1)`'),('`a,b,c`','`hasPosition(person1)`','`student(person0)`'),('`a,b,c`','`hasPosition(person1)`','`student(person1)`'),('`a,b,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,b,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,b,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,b,c`','`inPhase(person1)`','`inPhase(person0)`'),('`a,b,c`','`inPhase(person1)`','`inPhase(person1)`'),('`a,b,c`','`inPhase(person1)`','`professor(person0)`'),('`a,b,c`','`inPhase(person1)`','`professor(person1)`'),('`a,b,c`','`inPhase(person1)`','`student(person0)`'),('`a,b,c`','`inPhase(person1)`','`student(person1)`'),('`a,b,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`professor(person1)`','`courseLevel(course0)`'),('`a,b,c`','`professor(person1)`','`hasPosition(person0)`'),('`a,b,c`','`professor(person1)`','`hasPosition(person1)`'),('`a,b,c`','`professor(person1)`','`inPhase(person0)`'),('`a,b,c`','`professor(person1)`','`inPhase(person1)`'),('`a,b,c`','`professor(person1)`','`professor(person0)`'),('`a,b,c`','`professor(person1)`','`professor(person1)`'),('`a,b,c`','`professor(person1)`','`student(person0)`'),('`a,b,c`','`professor(person1)`','`student(person1)`'),('`a,b,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`student(person1)`','`courseLevel(course0)`'),('`a,b,c`','`student(person1)`','`hasPosition(person0)`'),('`a,b,c`','`student(person1)`','`hasPosition(person1)`'),('`a,b,c`','`student(person1)`','`inPhase(person0)`'),('`a,b,c`','`student(person1)`','`inPhase(person1)`'),('`a,b,c`','`student(person1)`','`professor(person0)`'),('`a,b,c`','`student(person1)`','`professor(person1)`'),('`a,b,c`','`student(person1)`','`student(person0)`'),('`a,b,c`','`student(person1)`','`student(person1)`'),('`a,b,c`','`student(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`student(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,b,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`student(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`student(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a,b`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,b`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,b`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,b`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,b`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,b`','`hasPosition(person1)`','`professor(person0)`'),('`a,b`','`hasPosition(person1)`','`professor(person1)`'),('`a,b`','`hasPosition(person1)`','`student(person0)`'),('`a,b`','`hasPosition(person1)`','`student(person1)`'),('`a,b`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,b`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,b`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,b`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,b`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,b`','`inPhase(person1)`','`inPhase(person0)`'),('`a,b`','`inPhase(person1)`','`inPhase(person1)`'),('`a,b`','`inPhase(person1)`','`professor(person0)`'),('`a,b`','`inPhase(person1)`','`professor(person1)`'),('`a,b`','`inPhase(person1)`','`student(person0)`'),('`a,b`','`inPhase(person1)`','`student(person1)`'),('`a,b`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,b`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,b`','`professor(person1)`','`courseLevel(course0)`'),('`a,b`','`professor(person1)`','`hasPosition(person0)`'),('`a,b`','`professor(person1)`','`hasPosition(person1)`'),('`a,b`','`professor(person1)`','`inPhase(person0)`'),('`a,b`','`professor(person1)`','`inPhase(person1)`'),('`a,b`','`professor(person1)`','`professor(person0)`'),('`a,b`','`professor(person1)`','`professor(person1)`'),('`a,b`','`professor(person1)`','`student(person0)`'),('`a,b`','`professor(person1)`','`student(person1)`'),('`a,b`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,b`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,b`','`student(person1)`','`courseLevel(course0)`'),('`a,b`','`student(person1)`','`hasPosition(person0)`'),('`a,b`','`student(person1)`','`hasPosition(person1)`'),('`a,b`','`student(person1)`','`inPhase(person0)`'),('`a,b`','`student(person1)`','`inPhase(person1)`'),('`a,b`','`student(person1)`','`professor(person0)`'),('`a,b`','`student(person1)`','`professor(person1)`'),('`a,b`','`student(person1)`','`student(person0)`'),('`a,b`','`student(person1)`','`student(person1)`'),('`a,b`','`student(person1)`','`yearsInProgram(person0)`'),('`a,b`','`student(person1)`','`yearsInProgram(person1)`'),('`a,b`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,b`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,b`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,b`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,b`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,b`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,b`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,b`','`yearsInProgram(person1)`','`student(person0)`'),('`a,b`','`yearsInProgram(person1)`','`student(person1)`'),('`a,b`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,b`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,c`','`hasPosition(person1)`','`professor(person0)`'),('`a,c`','`hasPosition(person1)`','`professor(person1)`'),('`a,c`','`hasPosition(person1)`','`student(person0)`'),('`a,c`','`hasPosition(person1)`','`student(person1)`'),('`a,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,c`','`inPhase(person1)`','`inPhase(person0)`'),('`a,c`','`inPhase(person1)`','`inPhase(person1)`'),('`a,c`','`inPhase(person1)`','`professor(person0)`'),('`a,c`','`inPhase(person1)`','`professor(person1)`'),('`a,c`','`inPhase(person1)`','`student(person0)`'),('`a,c`','`inPhase(person1)`','`student(person1)`'),('`a,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,c`','`professor(person1)`','`courseLevel(course0)`'),('`a,c`','`professor(person1)`','`hasPosition(person0)`'),('`a,c`','`professor(person1)`','`hasPosition(person1)`'),('`a,c`','`professor(person1)`','`inPhase(person0)`'),('`a,c`','`professor(person1)`','`inPhase(person1)`'),('`a,c`','`professor(person1)`','`professor(person0)`'),('`a,c`','`professor(person1)`','`professor(person1)`'),('`a,c`','`professor(person1)`','`student(person0)`'),('`a,c`','`professor(person1)`','`student(person1)`'),('`a,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,c`','`student(person1)`','`courseLevel(course0)`'),('`a,c`','`student(person1)`','`hasPosition(person0)`'),('`a,c`','`student(person1)`','`hasPosition(person1)`'),('`a,c`','`student(person1)`','`inPhase(person0)`'),('`a,c`','`student(person1)`','`inPhase(person1)`'),('`a,c`','`student(person1)`','`professor(person0)`'),('`a,c`','`student(person1)`','`professor(person1)`'),('`a,c`','`student(person1)`','`student(person0)`'),('`a,c`','`student(person1)`','`student(person1)`'),('`a,c`','`student(person1)`','`yearsInProgram(person0)`'),('`a,c`','`student(person1)`','`yearsInProgram(person1)`'),('`a,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,c`','`yearsInProgram(person1)`','`student(person0)`'),('`a,c`','`yearsInProgram(person1)`','`student(person1)`'),('`a,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a`','`hasPosition(person1)`','`inPhase(person0)`'),('`a`','`hasPosition(person1)`','`inPhase(person1)`'),('`a`','`hasPosition(person1)`','`professor(person0)`'),('`a`','`hasPosition(person1)`','`professor(person1)`'),('`a`','`hasPosition(person1)`','`student(person0)`'),('`a`','`hasPosition(person1)`','`student(person1)`'),('`a`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a`','`inPhase(person1)`','`hasPosition(person0)`'),('`a`','`inPhase(person1)`','`hasPosition(person1)`'),('`a`','`inPhase(person1)`','`inPhase(person0)`'),('`a`','`inPhase(person1)`','`inPhase(person1)`'),('`a`','`inPhase(person1)`','`professor(person0)`'),('`a`','`inPhase(person1)`','`professor(person1)`'),('`a`','`inPhase(person1)`','`student(person0)`'),('`a`','`inPhase(person1)`','`student(person1)`'),('`a`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a`','`professor(person1)`','`hasPosition(person0)`'),('`a`','`professor(person1)`','`hasPosition(person1)`'),('`a`','`professor(person1)`','`inPhase(person0)`'),('`a`','`professor(person1)`','`inPhase(person1)`'),('`a`','`professor(person1)`','`professor(person0)`'),('`a`','`professor(person1)`','`professor(person1)`'),('`a`','`professor(person1)`','`student(person0)`'),('`a`','`professor(person1)`','`student(person1)`'),('`a`','`professor(person1)`','`yearsInProgram(person0)`'),('`a`','`professor(person1)`','`yearsInProgram(person1)`'),('`a`','`student(person1)`','`hasPosition(person0)`'),('`a`','`student(person1)`','`hasPosition(person1)`'),('`a`','`student(person1)`','`inPhase(person0)`'),('`a`','`student(person1)`','`inPhase(person1)`'),('`a`','`student(person1)`','`professor(person0)`'),('`a`','`student(person1)`','`professor(person1)`'),('`a`','`student(person1)`','`student(person0)`'),('`a`','`student(person1)`','`student(person1)`'),('`a`','`student(person1)`','`yearsInProgram(person0)`'),('`a`','`student(person1)`','`yearsInProgram(person1)`'),('`a`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a`','`yearsInProgram(person1)`','`professor(person0)`'),('`a`','`yearsInProgram(person1)`','`professor(person1)`'),('`a`','`yearsInProgram(person1)`','`student(person0)`'),('`a`','`yearsInProgram(person1)`','`student(person1)`'),('`a`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`b,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`b,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`b,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`b,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`b,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`b,c`','`hasPosition(person1)`','`professor(person0)`'),('`b,c`','`hasPosition(person1)`','`professor(person1)`'),('`b,c`','`hasPosition(person1)`','`student(person0)`'),('`b,c`','`hasPosition(person1)`','`student(person1)`'),('`b,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`b,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`b,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`b,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`b,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`b,c`','`inPhase(person1)`','`inPhase(person0)`'),('`b,c`','`inPhase(person1)`','`inPhase(person1)`'),('`b,c`','`inPhase(person1)`','`professor(person0)`'),('`b,c`','`inPhase(person1)`','`professor(person1)`'),('`b,c`','`inPhase(person1)`','`student(person0)`'),('`b,c`','`inPhase(person1)`','`student(person1)`'),('`b,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`b,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`b,c`','`professor(person1)`','`courseLevel(course0)`'),('`b,c`','`professor(person1)`','`hasPosition(person0)`'),('`b,c`','`professor(person1)`','`hasPosition(person1)`'),('`b,c`','`professor(person1)`','`inPhase(person0)`'),('`b,c`','`professor(person1)`','`inPhase(person1)`'),('`b,c`','`professor(person1)`','`professor(person0)`'),('`b,c`','`professor(person1)`','`professor(person1)`'),('`b,c`','`professor(person1)`','`student(person0)`'),('`b,c`','`professor(person1)`','`student(person1)`'),('`b,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`b,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`b,c`','`student(person1)`','`courseLevel(course0)`'),('`b,c`','`student(person1)`','`hasPosition(person0)`'),('`b,c`','`student(person1)`','`hasPosition(person1)`'),('`b,c`','`student(person1)`','`inPhase(person0)`'),('`b,c`','`student(person1)`','`inPhase(person1)`'),('`b,c`','`student(person1)`','`professor(person0)`'),('`b,c`','`student(person1)`','`professor(person1)`'),('`b,c`','`student(person1)`','`student(person0)`'),('`b,c`','`student(person1)`','`student(person1)`'),('`b,c`','`student(person1)`','`yearsInProgram(person0)`'),('`b,c`','`student(person1)`','`yearsInProgram(person1)`'),('`b,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`b,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`b,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`b,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`b,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`b,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`b,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`b,c`','`yearsInProgram(person1)`','`student(person0)`'),('`b,c`','`yearsInProgram(person1)`','`student(person1)`'),('`b,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`b,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`','`inPhase(person1)`'),('`c`','`hasPosition(person1)`','`professor(person1)`'),('`c`','`hasPosition(person1)`','`student(person1)`'),('`c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`c`','`inPhase(person1)`','`courseLevel(course0)`'),('`c`','`inPhase(person1)`','`hasPosition(person1)`'),('`c`','`inPhase(person1)`','`inPhase(person1)`'),('`c`','`inPhase(person1)`','`professor(person1)`'),('`c`','`inPhase(person1)`','`student(person1)`'),('`c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`c`','`professor(person1)`','`courseLevel(course0)`'),('`c`','`professor(person1)`','`hasPosition(person1)`'),('`c`','`professor(person1)`','`inPhase(person1)`'),('`c`','`professor(person1)`','`professor(person1)`'),('`c`','`professor(person1)`','`student(person1)`'),('`c`','`professor(person1)`','`yearsInProgram(person1)`'),('`c`','`student(person1)`','`courseLevel(course0)`'),('`c`','`student(person1)`','`hasPosition(person1)`'),('`c`','`student(person1)`','`inPhase(person1)`'),('`c`','`student(person1)`','`professor(person1)`'),('`c`','`student(person1)`','`student(person1)`'),('`c`','`student(person1)`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`c`','`yearsInProgram(person1)`','`professor(person1)`'),('`c`','`yearsInProgram(person1)`','`student(person1)`'),('`c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `Path_Aux_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Path_BN_nodes`
--

DROP TABLE IF EXISTS `Path_BN_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_BN_nodes` (
  `Rchain` varchar(20) NOT NULL DEFAULT '',
  `node` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  KEY `HashIndex` (`Rchain`,`node`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BN_nodes`
--

LOCK TABLES `Path_BN_nodes` WRITE;
/*!40000 ALTER TABLE `Path_BN_nodes` DISABLE KEYS */;
INSERT INTO `Path_BN_nodes` VALUES ('`a,b,c`','`courseLevel(course0)`'),('`a,b,c`','`hasPosition(person0)`'),('`a,b,c`','`hasPosition(person1)`'),('`a,b,c`','`inPhase(person0)`'),('`a,b,c`','`inPhase(person1)`'),('`a,b,c`','`professor(person0)`'),('`a,b,c`','`professor(person1)`'),('`a,b,c`','`student(person0)`'),('`a,b,c`','`student(person1)`'),('`a,b,c`','`yearsInProgram(person0)`'),('`a,b,c`','`yearsInProgram(person1)`'),('`a,b`','`courseLevel(course0)`'),('`a,b`','`hasPosition(person0)`'),('`a,b`','`hasPosition(person1)`'),('`a,b`','`inPhase(person0)`'),('`a,b`','`inPhase(person1)`'),('`a,b`','`professor(person0)`'),('`a,b`','`professor(person1)`'),('`a,b`','`student(person0)`'),('`a,b`','`student(person1)`'),('`a,b`','`yearsInProgram(person0)`'),('`a,b`','`yearsInProgram(person1)`'),('`a,c`','`courseLevel(course0)`'),('`a,c`','`hasPosition(person0)`'),('`a,c`','`hasPosition(person1)`'),('`a,c`','`inPhase(person0)`'),('`a,c`','`inPhase(person1)`'),('`a,c`','`professor(person0)`'),('`a,c`','`professor(person1)`'),('`a,c`','`student(person0)`'),('`a,c`','`student(person1)`'),('`a,c`','`yearsInProgram(person0)`'),('`a,c`','`yearsInProgram(person1)`'),('`a`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`'),('`a`','`inPhase(person0)`'),('`a`','`inPhase(person1)`'),('`a`','`professor(person0)`'),('`a`','`professor(person1)`'),('`a`','`student(person0)`'),('`a`','`student(person1)`'),('`a`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`'),('`b,c`','`courseLevel(course0)`'),('`b,c`','`hasPosition(person0)`'),('`b,c`','`hasPosition(person1)`'),('`b,c`','`inPhase(person0)`'),('`b,c`','`inPhase(person1)`'),('`b,c`','`professor(person0)`'),('`b,c`','`professor(person1)`'),('`b,c`','`student(person0)`'),('`b,c`','`student(person1)`'),('`b,c`','`yearsInProgram(person0)`'),('`b,c`','`yearsInProgram(person1)`'),('`b`','`courseLevel(course0)`'),('`b`','`hasPosition(person0)`'),('`b`','`inPhase(person0)`'),('`b`','`professor(person0)`'),('`b`','`student(person0)`'),('`b`','`yearsInProgram(person0)`'),('`c`','`courseLevel(course0)`'),('`c`','`hasPosition(person1)`'),('`c`','`inPhase(person1)`'),('`c`','`professor(person1)`'),('`c`','`student(person1)`'),('`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `Path_BN_nodes` ENABLE KEYS */;
UNLOCK TABLES;

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
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BayesNets`
--

LOCK TABLES `Path_BayesNets` WRITE;
/*!40000 ALTER TABLE `Path_BayesNets` DISABLE KEYS */;
/*!40000 ALTER TABLE `Path_BayesNets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Path_Complement_Edges`
--

DROP TABLE IF EXISTS `Path_Complement_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_Complement_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Complement_Edges`
--

LOCK TABLES `Path_Complement_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Complement_Edges` DISABLE KEYS */;
/*!40000 ALTER TABLE `Path_Complement_Edges` ENABLE KEYS */;
UNLOCK TABLES;

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
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Forbidden_Edges`
--

LOCK TABLES `Path_Forbidden_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Forbidden_Edges` DISABLE KEYS */;
INSERT INTO `Path_Forbidden_Edges` VALUES ('`a,b,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,b,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,b,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,b,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,b,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,b,c`','`hasPosition(person1)`','`professor(person0)`'),('`a,b,c`','`hasPosition(person1)`','`professor(person1)`'),('`a,b,c`','`hasPosition(person1)`','`student(person0)`'),('`a,b,c`','`hasPosition(person1)`','`student(person1)`'),('`a,b,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,b,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,b,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,b,c`','`inPhase(person1)`','`inPhase(person0)`'),('`a,b,c`','`inPhase(person1)`','`inPhase(person1)`'),('`a,b,c`','`inPhase(person1)`','`professor(person0)`'),('`a,b,c`','`inPhase(person1)`','`professor(person1)`'),('`a,b,c`','`inPhase(person1)`','`student(person0)`'),('`a,b,c`','`inPhase(person1)`','`student(person1)`'),('`a,b,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`professor(person1)`','`courseLevel(course0)`'),('`a,b,c`','`professor(person1)`','`hasPosition(person0)`'),('`a,b,c`','`professor(person1)`','`hasPosition(person1)`'),('`a,b,c`','`professor(person1)`','`inPhase(person0)`'),('`a,b,c`','`professor(person1)`','`inPhase(person1)`'),('`a,b,c`','`professor(person1)`','`professor(person0)`'),('`a,b,c`','`professor(person1)`','`professor(person1)`'),('`a,b,c`','`professor(person1)`','`student(person0)`'),('`a,b,c`','`professor(person1)`','`student(person1)`'),('`a,b,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`student(person1)`','`courseLevel(course0)`'),('`a,b,c`','`student(person1)`','`hasPosition(person0)`'),('`a,b,c`','`student(person1)`','`hasPosition(person1)`'),('`a,b,c`','`student(person1)`','`inPhase(person0)`'),('`a,b,c`','`student(person1)`','`inPhase(person1)`'),('`a,b,c`','`student(person1)`','`professor(person0)`'),('`a,b,c`','`student(person1)`','`professor(person1)`'),('`a,b,c`','`student(person1)`','`student(person0)`'),('`a,b,c`','`student(person1)`','`student(person1)`'),('`a,b,c`','`student(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`student(person1)`','`yearsInProgram(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,b,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`student(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`student(person1)`'),('`a,b,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,b,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a,b`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,b`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,b`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,b`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,b`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,b`','`hasPosition(person1)`','`professor(person0)`'),('`a,b`','`hasPosition(person1)`','`professor(person1)`'),('`a,b`','`hasPosition(person1)`','`student(person0)`'),('`a,b`','`hasPosition(person1)`','`student(person1)`'),('`a,b`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,b`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,b`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,b`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,b`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,b`','`inPhase(person1)`','`inPhase(person0)`'),('`a,b`','`inPhase(person1)`','`inPhase(person1)`'),('`a,b`','`inPhase(person1)`','`professor(person0)`'),('`a,b`','`inPhase(person1)`','`professor(person1)`'),('`a,b`','`inPhase(person1)`','`student(person0)`'),('`a,b`','`inPhase(person1)`','`student(person1)`'),('`a,b`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,b`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,b`','`professor(person1)`','`courseLevel(course0)`'),('`a,b`','`professor(person1)`','`hasPosition(person0)`'),('`a,b`','`professor(person1)`','`hasPosition(person1)`'),('`a,b`','`professor(person1)`','`inPhase(person0)`'),('`a,b`','`professor(person1)`','`inPhase(person1)`'),('`a,b`','`professor(person1)`','`professor(person0)`'),('`a,b`','`professor(person1)`','`professor(person1)`'),('`a,b`','`professor(person1)`','`student(person0)`'),('`a,b`','`professor(person1)`','`student(person1)`'),('`a,b`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,b`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,b`','`student(person1)`','`courseLevel(course0)`'),('`a,b`','`student(person1)`','`hasPosition(person0)`'),('`a,b`','`student(person1)`','`hasPosition(person1)`'),('`a,b`','`student(person1)`','`inPhase(person0)`'),('`a,b`','`student(person1)`','`inPhase(person1)`'),('`a,b`','`student(person1)`','`professor(person0)`'),('`a,b`','`student(person1)`','`professor(person1)`'),('`a,b`','`student(person1)`','`student(person0)`'),('`a,b`','`student(person1)`','`student(person1)`'),('`a,b`','`student(person1)`','`yearsInProgram(person0)`'),('`a,b`','`student(person1)`','`yearsInProgram(person1)`'),('`a,b`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,b`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,b`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,b`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,b`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,b`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,b`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,b`','`yearsInProgram(person1)`','`student(person0)`'),('`a,b`','`yearsInProgram(person1)`','`student(person1)`'),('`a,b`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,b`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`a,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`a,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`a,c`','`hasPosition(person1)`','`professor(person0)`'),('`a,c`','`hasPosition(person1)`','`professor(person1)`'),('`a,c`','`hasPosition(person1)`','`student(person0)`'),('`a,c`','`hasPosition(person1)`','`student(person1)`'),('`a,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`a,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`a,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`a,c`','`inPhase(person1)`','`inPhase(person0)`'),('`a,c`','`inPhase(person1)`','`inPhase(person1)`'),('`a,c`','`inPhase(person1)`','`professor(person0)`'),('`a,c`','`inPhase(person1)`','`professor(person1)`'),('`a,c`','`inPhase(person1)`','`student(person0)`'),('`a,c`','`inPhase(person1)`','`student(person1)`'),('`a,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a,c`','`professor(person1)`','`courseLevel(course0)`'),('`a,c`','`professor(person1)`','`hasPosition(person0)`'),('`a,c`','`professor(person1)`','`hasPosition(person1)`'),('`a,c`','`professor(person1)`','`inPhase(person0)`'),('`a,c`','`professor(person1)`','`inPhase(person1)`'),('`a,c`','`professor(person1)`','`professor(person0)`'),('`a,c`','`professor(person1)`','`professor(person1)`'),('`a,c`','`professor(person1)`','`student(person0)`'),('`a,c`','`professor(person1)`','`student(person1)`'),('`a,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`a,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`a,c`','`student(person1)`','`courseLevel(course0)`'),('`a,c`','`student(person1)`','`hasPosition(person0)`'),('`a,c`','`student(person1)`','`hasPosition(person1)`'),('`a,c`','`student(person1)`','`inPhase(person0)`'),('`a,c`','`student(person1)`','`inPhase(person1)`'),('`a,c`','`student(person1)`','`professor(person0)`'),('`a,c`','`student(person1)`','`professor(person1)`'),('`a,c`','`student(person1)`','`student(person0)`'),('`a,c`','`student(person1)`','`student(person1)`'),('`a,c`','`student(person1)`','`yearsInProgram(person0)`'),('`a,c`','`student(person1)`','`yearsInProgram(person1)`'),('`a,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`a,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`a,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`a,c`','`yearsInProgram(person1)`','`student(person0)`'),('`a,c`','`yearsInProgram(person1)`','`student(person1)`'),('`a,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`a`','`hasPosition(person1)`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`','`hasPosition(person1)`'),('`a`','`hasPosition(person1)`','`inPhase(person0)`'),('`a`','`hasPosition(person1)`','`inPhase(person1)`'),('`a`','`hasPosition(person1)`','`professor(person0)`'),('`a`','`hasPosition(person1)`','`professor(person1)`'),('`a`','`hasPosition(person1)`','`student(person0)`'),('`a`','`hasPosition(person1)`','`student(person1)`'),('`a`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`a`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`a`','`inPhase(person1)`','`hasPosition(person0)`'),('`a`','`inPhase(person1)`','`hasPosition(person1)`'),('`a`','`inPhase(person1)`','`inPhase(person0)`'),('`a`','`inPhase(person1)`','`inPhase(person1)`'),('`a`','`inPhase(person1)`','`professor(person0)`'),('`a`','`inPhase(person1)`','`professor(person1)`'),('`a`','`inPhase(person1)`','`student(person0)`'),('`a`','`inPhase(person1)`','`student(person1)`'),('`a`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`a`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`a`','`professor(person1)`','`hasPosition(person0)`'),('`a`','`professor(person1)`','`hasPosition(person1)`'),('`a`','`professor(person1)`','`inPhase(person0)`'),('`a`','`professor(person1)`','`inPhase(person1)`'),('`a`','`professor(person1)`','`professor(person0)`'),('`a`','`professor(person1)`','`professor(person1)`'),('`a`','`professor(person1)`','`student(person0)`'),('`a`','`professor(person1)`','`student(person1)`'),('`a`','`professor(person1)`','`yearsInProgram(person0)`'),('`a`','`professor(person1)`','`yearsInProgram(person1)`'),('`a`','`student(person1)`','`hasPosition(person0)`'),('`a`','`student(person1)`','`hasPosition(person1)`'),('`a`','`student(person1)`','`inPhase(person0)`'),('`a`','`student(person1)`','`inPhase(person1)`'),('`a`','`student(person1)`','`professor(person0)`'),('`a`','`student(person1)`','`professor(person1)`'),('`a`','`student(person1)`','`student(person0)`'),('`a`','`student(person1)`','`student(person1)`'),('`a`','`student(person1)`','`yearsInProgram(person0)`'),('`a`','`student(person1)`','`yearsInProgram(person1)`'),('`a`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`a`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`a`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`a`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`a`','`yearsInProgram(person1)`','`professor(person0)`'),('`a`','`yearsInProgram(person1)`','`professor(person1)`'),('`a`','`yearsInProgram(person1)`','`student(person0)`'),('`a`','`yearsInProgram(person1)`','`student(person1)`'),('`a`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`b,c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`b,c`','`hasPosition(person1)`','`hasPosition(person0)`'),('`b,c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`b,c`','`hasPosition(person1)`','`inPhase(person0)`'),('`b,c`','`hasPosition(person1)`','`inPhase(person1)`'),('`b,c`','`hasPosition(person1)`','`professor(person0)`'),('`b,c`','`hasPosition(person1)`','`professor(person1)`'),('`b,c`','`hasPosition(person1)`','`student(person0)`'),('`b,c`','`hasPosition(person1)`','`student(person1)`'),('`b,c`','`hasPosition(person1)`','`yearsInProgram(person0)`'),('`b,c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`b,c`','`inPhase(person1)`','`courseLevel(course0)`'),('`b,c`','`inPhase(person1)`','`hasPosition(person0)`'),('`b,c`','`inPhase(person1)`','`hasPosition(person1)`'),('`b,c`','`inPhase(person1)`','`inPhase(person0)`'),('`b,c`','`inPhase(person1)`','`inPhase(person1)`'),('`b,c`','`inPhase(person1)`','`professor(person0)`'),('`b,c`','`inPhase(person1)`','`professor(person1)`'),('`b,c`','`inPhase(person1)`','`student(person0)`'),('`b,c`','`inPhase(person1)`','`student(person1)`'),('`b,c`','`inPhase(person1)`','`yearsInProgram(person0)`'),('`b,c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`b,c`','`professor(person1)`','`courseLevel(course0)`'),('`b,c`','`professor(person1)`','`hasPosition(person0)`'),('`b,c`','`professor(person1)`','`hasPosition(person1)`'),('`b,c`','`professor(person1)`','`inPhase(person0)`'),('`b,c`','`professor(person1)`','`inPhase(person1)`'),('`b,c`','`professor(person1)`','`professor(person0)`'),('`b,c`','`professor(person1)`','`professor(person1)`'),('`b,c`','`professor(person1)`','`student(person0)`'),('`b,c`','`professor(person1)`','`student(person1)`'),('`b,c`','`professor(person1)`','`yearsInProgram(person0)`'),('`b,c`','`professor(person1)`','`yearsInProgram(person1)`'),('`b,c`','`student(person1)`','`courseLevel(course0)`'),('`b,c`','`student(person1)`','`hasPosition(person0)`'),('`b,c`','`student(person1)`','`hasPosition(person1)`'),('`b,c`','`student(person1)`','`inPhase(person0)`'),('`b,c`','`student(person1)`','`inPhase(person1)`'),('`b,c`','`student(person1)`','`professor(person0)`'),('`b,c`','`student(person1)`','`professor(person1)`'),('`b,c`','`student(person1)`','`student(person0)`'),('`b,c`','`student(person1)`','`student(person1)`'),('`b,c`','`student(person1)`','`yearsInProgram(person0)`'),('`b,c`','`student(person1)`','`yearsInProgram(person1)`'),('`b,c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`b,c`','`yearsInProgram(person1)`','`hasPosition(person0)`'),('`b,c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`b,c`','`yearsInProgram(person1)`','`inPhase(person0)`'),('`b,c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`b,c`','`yearsInProgram(person1)`','`professor(person0)`'),('`b,c`','`yearsInProgram(person1)`','`professor(person1)`'),('`b,c`','`yearsInProgram(person1)`','`student(person0)`'),('`b,c`','`yearsInProgram(person1)`','`student(person1)`'),('`b,c`','`yearsInProgram(person1)`','`yearsInProgram(person0)`'),('`b,c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`'),('`c`','`hasPosition(person1)`','`courseLevel(course0)`'),('`c`','`hasPosition(person1)`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`','`inPhase(person1)`'),('`c`','`hasPosition(person1)`','`professor(person1)`'),('`c`','`hasPosition(person1)`','`student(person1)`'),('`c`','`hasPosition(person1)`','`yearsInProgram(person1)`'),('`c`','`inPhase(person1)`','`courseLevel(course0)`'),('`c`','`inPhase(person1)`','`hasPosition(person1)`'),('`c`','`inPhase(person1)`','`inPhase(person1)`'),('`c`','`inPhase(person1)`','`professor(person1)`'),('`c`','`inPhase(person1)`','`student(person1)`'),('`c`','`inPhase(person1)`','`yearsInProgram(person1)`'),('`c`','`professor(person1)`','`courseLevel(course0)`'),('`c`','`professor(person1)`','`hasPosition(person1)`'),('`c`','`professor(person1)`','`inPhase(person1)`'),('`c`','`professor(person1)`','`professor(person1)`'),('`c`','`professor(person1)`','`student(person1)`'),('`c`','`professor(person1)`','`yearsInProgram(person1)`'),('`c`','`student(person1)`','`courseLevel(course0)`'),('`c`','`student(person1)`','`hasPosition(person1)`'),('`c`','`student(person1)`','`inPhase(person1)`'),('`c`','`student(person1)`','`professor(person1)`'),('`c`','`student(person1)`','`student(person1)`'),('`c`','`student(person1)`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`','`courseLevel(course0)`'),('`c`','`yearsInProgram(person1)`','`hasPosition(person1)`'),('`c`','`yearsInProgram(person1)`','`inPhase(person1)`'),('`c`','`yearsInProgram(person1)`','`professor(person1)`'),('`c`','`yearsInProgram(person1)`','`student(person1)`'),('`c`','`yearsInProgram(person1)`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `Path_Forbidden_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Path_Required_Edges`
--

DROP TABLE IF EXISTS `Path_Required_Edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_Required_Edges` (
  `Rchain` varchar(256) NOT NULL,
  `child` varchar(197) NOT NULL,
  `parent` varchar(197) NOT NULL,
  PRIMARY KEY (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_Required_Edges`
--

LOCK TABLES `Path_Required_Edges` WRITE;
/*!40000 ALTER TABLE `Path_Required_Edges` DISABLE KEYS */;
/*!40000 ALTER TABLE `Path_Required_Edges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RChain_pvars`
--

DROP TABLE IF EXISTS `RChain_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RChain_pvars` (
  `rchain` varchar(20) NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RChain_pvars`
--

LOCK TABLES `RChain_pvars` WRITE;
/*!40000 ALTER TABLE `RChain_pvars` DISABLE KEYS */;
INSERT INTO `RChain_pvars` VALUES ('`a,b,c`','person0'),('`a,b,c`','person1'),('`a,b,c`','course0'),('`a,b`','person0'),('`a,b`','person1'),('`a,b`','course0'),('`a,c`','person0'),('`a,c`','person1'),('`a,c`','course0'),('`a`','person0'),('`a`','person1'),('`b,c`','course0'),('`b,c`','person0'),('`b,c`','person1'),('`b`','course0'),('`b`','person0'),('`c`','course0'),('`c`','person1');
/*!40000 ALTER TABLE `RChain_pvars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes`
--

DROP TABLE IF EXISTS `RNodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes` (
  `orig_rnid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid1` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid2` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME1` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME2` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `main` int(11) NOT NULL DEFAULT '0',
  `rnid` varchar(10) DEFAULT NULL,
  KEY `Index` (`pvid1`,`pvid2`,`TABLE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes`
--

LOCK TABLES `RNodes` WRITE;
/*!40000 ALTER TABLE `RNodes` DISABLE KEYS */;
INSERT INTO `RNodes` VALUES ('`advisedBy(person0,person1)`','advisedBy','person0','person1','p_id','p_id_dummy',1,'`a`'),('`taughtBy(course0,person0)`','taughtBy','course0','person0','course_id','p_id',1,'`b`'),('`taughtBy(course0,person1)`','taughtBy','course0','person1','course_id','p_id',0,'`c`');
/*!40000 ALTER TABLE `RNodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_1Nodes`
--

DROP TABLE IF EXISTS `RNodes_1Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_1Nodes` (
  `rnid` varchar(10) DEFAULT NULL,
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `1nid` varchar(133) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_1Nodes`
--

LOCK TABLES `RNodes_1Nodes` WRITE;
/*!40000 ALTER TABLE `RNodes_1Nodes` DISABLE KEYS */;
INSERT INTO `RNodes_1Nodes` VALUES ('`b`','taughtBy','`courseLevel(course0)`','courseLevel','course0'),('`c`','taughtBy','`courseLevel(course0)`','courseLevel','course0'),('`a`','advisedBy','`hasPosition(person0)`','hasPosition','person0'),('`a`','advisedBy','`inPhase(person0)`','inPhase','person0'),('`a`','advisedBy','`professor(person0)`','professor','person0'),('`a`','advisedBy','`student(person0)`','student','person0'),('`a`','advisedBy','`yearsInProgram(person0)`','yearsInProgram','person0'),('`b`','taughtBy','`hasPosition(person0)`','hasPosition','person0'),('`a`','advisedBy','`hasPosition(person1)`','hasPosition','person1'),('`c`','taughtBy','`hasPosition(person1)`','hasPosition','person1'),('`b`','taughtBy','`inPhase(person0)`','inPhase','person0'),('`a`','advisedBy','`inPhase(person1)`','inPhase','person1'),('`c`','taughtBy','`inPhase(person1)`','inPhase','person1'),('`b`','taughtBy','`professor(person0)`','professor','person0'),('`a`','advisedBy','`professor(person1)`','professor','person1'),('`c`','taughtBy','`professor(person1)`','professor','person1'),('`b`','taughtBy','`student(person0)`','student','person0'),('`a`','advisedBy','`student(person1)`','student','person1'),('`c`','taughtBy','`student(person1)`','student','person1'),('`b`','taughtBy','`yearsInProgram(person0)`','yearsInProgram','person0'),('`a`','advisedBy','`yearsInProgram(person1)`','yearsInProgram','person1'),('`c`','taughtBy','`yearsInProgram(person1)`','yearsInProgram','person1');
/*!40000 ALTER TABLE `RNodes_1Nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_2Nodes`
--

DROP TABLE IF EXISTS `RNodes_2Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_2Nodes` (
  `rnid` varchar(10) DEFAULT NULL,
  `2nid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_2Nodes`
--

LOCK TABLES `RNodes_2Nodes` WRITE;
/*!40000 ALTER TABLE `RNodes_2Nodes` DISABLE KEYS */;
/*!40000 ALTER TABLE `RNodes_2Nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_BN_Nodes`
--

DROP TABLE IF EXISTS `RNodes_BN_Nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_BN_Nodes` (
  `rnid` varchar(10) DEFAULT NULL,
  `Fid` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `main` int(11) DEFAULT NULL,
  KEY `Index_rnid` (`rnid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_BN_Nodes`
--

LOCK TABLES `RNodes_BN_Nodes` WRITE;
/*!40000 ALTER TABLE `RNodes_BN_Nodes` DISABLE KEYS */;
INSERT INTO `RNodes_BN_Nodes` VALUES ('`b`','`courseLevel(course0)`',1),('`c`','`courseLevel(course0)`',1),('`a`','`hasPosition(person0)`',1),('`a`','`inPhase(person0)`',1),('`a`','`professor(person0)`',1),('`a`','`student(person0)`',1),('`a`','`yearsInProgram(person0)`',1),('`b`','`hasPosition(person0)`',1),('`a`','`hasPosition(person1)`',0),('`c`','`hasPosition(person1)`',0),('`b`','`inPhase(person0)`',1),('`a`','`inPhase(person1)`',0),('`c`','`inPhase(person1)`',0),('`b`','`professor(person0)`',1),('`a`','`professor(person1)`',0),('`c`','`professor(person1)`',0),('`b`','`student(person0)`',1),('`a`','`student(person1)`',0),('`c`','`student(person1)`',0),('`b`','`yearsInProgram(person0)`',1),('`a`','`yearsInProgram(person1)`',0),('`c`','`yearsInProgram(person1)`',0);
/*!40000 ALTER TABLE `RNodes_BN_Nodes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_From_List`
--

DROP TABLE IF EXISTS `RNodes_From_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_From_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(140) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_From_List`
--

LOCK TABLES `RNodes_From_List` WRITE;
/*!40000 ALTER TABLE `RNodes_From_List` DISABLE KEYS */;
INSERT INTO `RNodes_From_List` VALUES ('`a`','UW_std.person AS person0'),('`b`','UW_std.course AS course0'),('`c`','UW_std.course AS course0'),('`a`','UW_std.person AS person1'),('`b`','UW_std.person AS person0'),('`c`','UW_std.person AS person1'),('`a`','UW_std.advisedBy AS `a`'),('`b`','UW_std.taughtBy AS `b`'),('`c`','UW_std.taughtBy AS `c`');
/*!40000 ALTER TABLE `RNodes_From_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_GroupBy_List`
--

DROP TABLE IF EXISTS `RNodes_GroupBy_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_GroupBy_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_GroupBy_List`
--

LOCK TABLES `RNodes_GroupBy_List` WRITE;
/*!40000 ALTER TABLE `RNodes_GroupBy_List` DISABLE KEYS */;
INSERT INTO `RNodes_GroupBy_List` VALUES ('`b`','`courseLevel(course0)`'),('`c`','`courseLevel(course0)`'),('`a`','`hasPosition(person0)`'),('`a`','`inPhase(person0)`'),('`a`','`professor(person0)`'),('`a`','`student(person0)`'),('`a`','`yearsInProgram(person0)`'),('`b`','`hasPosition(person0)`'),('`a`','`hasPosition(person1)`'),('`c`','`hasPosition(person1)`'),('`b`','`inPhase(person0)`'),('`a`','`inPhase(person1)`'),('`c`','`inPhase(person1)`'),('`b`','`professor(person0)`'),('`a`','`professor(person1)`'),('`c`','`professor(person1)`'),('`b`','`student(person0)`'),('`a`','`student(person1)`'),('`c`','`student(person1)`'),('`b`','`yearsInProgram(person0)`'),('`a`','`yearsInProgram(person1)`'),('`c`','`yearsInProgram(person1)`');
/*!40000 ALTER TABLE `RNodes_GroupBy_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_Select_List`
--

DROP TABLE IF EXISTS `RNodes_Select_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_Select_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(278) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_Select_List`
--

LOCK TABLES `RNodes_Select_List` WRITE;
/*!40000 ALTER TABLE `RNodes_Select_List` DISABLE KEYS */;
INSERT INTO `RNodes_Select_List` VALUES ('`a`','count(*) as \"MULT\"'),('`b`','count(*) as \"MULT\"'),('`c`','count(*) as \"MULT\"'),('`b`','course0.courseLevel AS `courseLevel(course0)`'),('`c`','course0.courseLevel AS `courseLevel(course0)`'),('`a`','person0.hasPosition AS `hasPosition(person0)`'),('`a`','person0.inPhase AS `inPhase(person0)`'),('`a`','person0.professor AS `professor(person0)`'),('`a`','person0.student AS `student(person0)`'),('`a`','person0.yearsInProgram AS `yearsInProgram(person0)`'),('`b`','person0.hasPosition AS `hasPosition(person0)`'),('`a`','person1.hasPosition AS `hasPosition(person1)`'),('`c`','person1.hasPosition AS `hasPosition(person1)`'),('`b`','person0.inPhase AS `inPhase(person0)`'),('`a`','person1.inPhase AS `inPhase(person1)`'),('`c`','person1.inPhase AS `inPhase(person1)`'),('`b`','person0.professor AS `professor(person0)`'),('`a`','person1.professor AS `professor(person1)`'),('`c`','person1.professor AS `professor(person1)`'),('`b`','person0.student AS `student(person0)`'),('`a`','person1.student AS `student(person1)`'),('`c`','person1.student AS `student(person1)`'),('`b`','person0.yearsInProgram AS `yearsInProgram(person0)`'),('`a`','person1.yearsInProgram AS `yearsInProgram(person1)`'),('`c`','person1.yearsInProgram AS `yearsInProgram(person1)`');
/*!40000 ALTER TABLE `RNodes_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_Where_List`
--

DROP TABLE IF EXISTS `RNodes_Where_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_Where_List` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(208) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_Where_List`
--

LOCK TABLES `RNodes_Where_List` WRITE;
/*!40000 ALTER TABLE `RNodes_Where_List` DISABLE KEYS */;
INSERT INTO `RNodes_Where_List` VALUES ('`a`','`a`.p_id = person0.p_id'),('`b`','`b`.course_id = course0.course_id'),('`c`','`c`.course_id = course0.course_id'),('`a`','`a`.p_id_dummy = person1.p_id'),('`b`','`b`.p_id = person0.p_id'),('`c`','`c`.p_id = person1.p_id');
/*!40000 ALTER TABLE `RNodes_Where_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RNodes_pvars`
--

DROP TABLE IF EXISTS `RNodes_pvars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RNodes_pvars` (
  `rnid` varchar(10) DEFAULT NULL,
  `pvid` varchar(65) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `COLUMN_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `REFERENCED_COLUMN_NAME` varchar(64) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_pvars`
--

LOCK TABLES `RNodes_pvars` WRITE;
/*!40000 ALTER TABLE `RNodes_pvars` DISABLE KEYS */;
INSERT INTO `RNodes_pvars` VALUES ('`a`','person0','person','p_id','p_id'),('`b`','course0','course','course_id','course_id'),('`c`','course0','course','course_id','course_id'),('`a`','person1','person','p_id_dummy','p_id'),('`b`','person0','person','p_id','p_id'),('`c`','person1','person','p_id','p_id');
/*!40000 ALTER TABLE `RNodes_pvars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RelationTables`
--

DROP TABLE IF EXISTS `RelationTables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RelationTables` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `SelfRelationship` bigint(21) DEFAULT NULL,
  `Many_OneRelationship` bigint(21) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RelationTables`
--

LOCK TABLES `RelationTables` WRITE;
/*!40000 ALTER TABLE `RelationTables` DISABLE KEYS */;
INSERT INTO `RelationTables` VALUES ('advisedBy',1,0),('taughtBy',0,0);
/*!40000 ALTER TABLE `RelationTables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Rnodes_join_columnname_list`
--

DROP TABLE IF EXISTS `Rnodes_join_columnname_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Rnodes_join_columnname_list` (
  `rnid` varchar(10) DEFAULT NULL,
  `Entries` varchar(227) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Rnodes_join_columnname_list`
--

LOCK TABLES `Rnodes_join_columnname_list` WRITE;
/*!40000 ALTER TABLE `Rnodes_join_columnname_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `Rnodes_join_columnname_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SchemaEdges`
--

DROP TABLE IF EXISTS `SchemaEdges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SchemaEdges` (
  `Rchain` varchar(20) NOT NULL DEFAULT '',
  `child` varchar(199) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `parent` varchar(10) DEFAULT NULL,
  KEY `HashIn` (`Rchain`,`child`,`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SchemaEdges`
--

LOCK TABLES `SchemaEdges` WRITE;
/*!40000 ALTER TABLE `SchemaEdges` DISABLE KEYS */;
/*!40000 ALTER TABLE `SchemaEdges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TernaryRelations`
--

DROP TABLE IF EXISTS `TernaryRelations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TernaryRelations` (
  `TABLE_NAME` varchar(64) CHARACTER SET utf8 NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TernaryRelations`
--

LOCK TABLES `TernaryRelations` WRITE;
/*!40000 ALTER TABLE `TernaryRelations` DISABLE KEYS */;
/*!40000 ALTER TABLE `TernaryRelations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lattice_mapping`
--

DROP TABLE IF EXISTS `lattice_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_mapping` (
  `orig_rnid` varchar(200) NOT NULL DEFAULT '',
  `rnid` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`orig_rnid`,`rnid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_mapping`
--

LOCK TABLES `lattice_mapping` WRITE;
/*!40000 ALTER TABLE `lattice_mapping` DISABLE KEYS */;
INSERT INTO `lattice_mapping` VALUES ('`advisedBy(person0,person1),taughtBy(course0,person0),taughtBy(course0,person1)`','`a,b,c`'),('`advisedBy(person0,person1),taughtBy(course0,person0)`','`a,b`'),('`advisedBy(person0,person1),taughtBy(course0,person1)`','`a,c`'),('`advisedBy(person0,person1)`','`a`'),('`taughtBy(course0,person0),taughtBy(course0,person1)`','`b,c`'),('`taughtBy(course0,person0)`','`b`'),('`taughtBy(course0,person1)`','`c`');
/*!40000 ALTER TABLE `lattice_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lattice_membership`
--

DROP TABLE IF EXISTS `lattice_membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_membership` (
  `name` varchar(20) NOT NULL DEFAULT '',
  `member` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`name`,`member`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_membership`
--

LOCK TABLES `lattice_membership` WRITE;
/*!40000 ALTER TABLE `lattice_membership` DISABLE KEYS */;
INSERT INTO `lattice_membership` VALUES ('`a,b,c`','`a`'),('`a,b,c`','`b`'),('`a,b,c`','`c`'),('`a,b`','`a`'),('`a,b`','`b`'),('`a,c`','`a`'),('`a,c`','`c`'),('`a`','`a`'),('`b,c`','`b`'),('`b,c`','`c`'),('`b`','`b`'),('`c`','`c`');
/*!40000 ALTER TABLE `lattice_membership` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lattice_rel`
--

DROP TABLE IF EXISTS `lattice_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_rel` (
  `parent` varchar(20) NOT NULL DEFAULT '',
  `child` varchar(20) NOT NULL DEFAULT '',
  `removed` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`parent`,`child`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_rel`
--

LOCK TABLES `lattice_rel` WRITE;
/*!40000 ALTER TABLE `lattice_rel` DISABLE KEYS */;
INSERT INTO `lattice_rel` VALUES ('EmptySet','`a`','`a`'),('EmptySet','`b`','`b`'),('EmptySet','`c`','`c`'),('`a,b`','`a,b,c`','`c`'),('`a,c`','`a,b,c`','`b`'),('`a`','`a,b`','`b`'),('`a`','`a,c`','`c`'),('`b,c`','`a,b,c`','`a`'),('`b`','`a,b`','`a`'),('`b`','`b,c`','`c`'),('`c`','`a,c`','`a`'),('`c`','`b,c`','`b`');
/*!40000 ALTER TABLE `lattice_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lattice_set`
--

DROP TABLE IF EXISTS `lattice_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lattice_set` (
  `name` varchar(20) NOT NULL DEFAULT '',
  `length` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`,`length`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lattice_set`
--

LOCK TABLES `lattice_set` WRITE;
/*!40000 ALTER TABLE `lattice_set` DISABLE KEYS */;
INSERT INTO `lattice_set` VALUES ('`a,b,c`',3),('`a,b`',2),('`a,c`',2),('`a`',1),('`b,c`',2),('`b`',1),('`c`',1);
/*!40000 ALTER TABLE `lattice_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `Entity_BN_nodes`
--

/*!50001 DROP TABLE IF EXISTS `Entity_BN_nodes`*/;
/*!50001 DROP VIEW IF EXISTS `Entity_BN_nodes`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `Entity_BN_nodes` AS select `Entity_BayesNets`.`pvid` AS `pvid`,`Entity_BayesNets`.`child` AS `node` from `Entity_BayesNets` order by `Entity_BayesNets`.`pvid` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-09-30 14:47:33
