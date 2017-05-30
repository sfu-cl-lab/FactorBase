CREATE DATABASE  IF NOT EXISTS `MovieLens_TQ_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `MovieLens_TQ_BN`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: MovieLens_TQ_BN
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
INSERT INTO `1Nodes` VALUES ('`Action(item20)`','Action','item20',1),('`Age(User0)`','Age','User0',1),('`Drama(item20)`','Drama','item20',1),('`Gender(User0)`','Gender','User0',1),('`Horror(item20)`','Horror','item20',1);
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
INSERT INTO `1Nodes_From_List` VALUES ('`Action(item20)`','item2 AS item20'),('`Age(User0)`','User AS User0'),('`Drama(item20)`','item2 AS item20'),('`Gender(User0)`','User AS User0'),('`Horror(item20)`','item2 AS item20');
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
INSERT INTO `1Nodes_Select_List` VALUES ('`Action(item20)`','item20.Action AS `Action(item20)`'),('`Age(User0)`','User0.Age AS `Age(User0)`'),('`Drama(item20)`','item20.Drama AS `Drama(item20)`'),('`Gender(User0)`','User0.Gender AS `Gender(User0)`'),('`Horror(item20)`','item20.Horror AS `Horror(item20)`');
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
INSERT INTO `2Nodes` VALUES ('`rating(User0,item20)`','rating','User0','item20','u2base',1);
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
INSERT INTO `2Nodes_From_List` VALUES ('`rating(User0,item20)`','u2base AS `a`');
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
INSERT INTO `2Nodes_Select_List` VALUES ('`rating(User0,item20)`','`a`.rating AS `rating(User0,item20)`');
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
  `Entries` varchar(146) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_From_List`
--

LOCK TABLES `ADT_PVariables_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_From_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_From_List` VALUES ('item20','MovieLens_TQ.item2 AS item20'),('User0','MovieLens_TQ.User AS User0');
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
INSERT INTO `ADT_PVariables_GroupBy_List` VALUES ('item20','`Action(item20)`'),('User0','`Age(User0)`'),('item20','`Drama(item20)`'),('User0','`Gender(User0)`'),('item20','`Horror(item20)`');
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
INSERT INTO `ADT_PVariables_Select_List` VALUES ('item20','count(*) as \"MULT\"'),('User0','count(*) as \"MULT\"'),('item20','item20.Action AS `Action(item20)`'),('User0','User0.Age AS `Age(User0)`'),('item20','item20.Drama AS `Drama(item20)`'),('User0','User0.Gender AS `Gender(User0)`'),('item20','item20.Horror AS `Horror(item20)`');
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
  `Entries` varchar(199) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Select_List`
--

LOCK TABLES `ADT_RChain_Star_Select_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Select_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a`','`a`','`Action(item20)`'),('`a`','`a`','`Age(User0)`'),('`a`','`a`','`Drama(item20)`'),('`a`','`a`','`Gender(User0)`'),('`a`','`a`','`Horror(item20)`');
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
INSERT INTO `ADT_RNodes_1Nodes_FROM_List` VALUES ('`a`','`a_counts`');
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
INSERT INTO `ADT_RNodes_1Nodes_GroupBY_List` VALUES ('`a`','`Age(User0)`'),('`a`','`Gender(User0)`'),('`a`','`Action(item20)`'),('`a`','`Drama(item20)`'),('`a`','`Horror(item20)`');
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
INSERT INTO `ADT_RNodes_1Nodes_Select_List` VALUES ('`a`','sum(`a_counts`.`MULT`) as \"MULT\"'),('`a`','`Age(User0)`'),('`a`','`Gender(User0)`'),('`a`','`Action(item20)`'),('`a`','`Drama(item20)`'),('`a`','`Horror(item20)`');
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
INSERT INTO `ADT_RNodes_False_FROM_List` VALUES ('`a`','`a_star`'),('`a`','`a_flat`');
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
INSERT INTO `ADT_RNodes_False_Select_List` VALUES ('`a`','(`a_star`.MULT-`a_flat`.MULT) AS \"MULT\"'),('`a`','`a_star`.`Age(User0)`'),('`a`','`a_star`.`Gender(User0)`'),('`a`','`a_star`.`Action(item20)`'),('`a`','`a_star`.`Drama(item20)`'),('`a`','`a_star`.`Horror(item20)`');
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
INSERT INTO `ADT_RNodes_False_WHERE_List` VALUES ('`a`','`a_star`.`Age(User0)`=`a_flat`.`Age(User0)`'),('`a`','`a_star`.`Gender(User0)`=`a_flat`.`Gender(User0)`'),('`a`','`a_star`.`Action(item20)`=`a_flat`.`Action(item20)`'),('`a`','`a_star`.`Drama(item20)`=`a_flat`.`Drama(item20)`'),('`a`','`a_star`.`Horror(item20)`=`a_flat`.`Horror(item20)`');
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
INSERT INTO `ADT_RNodes_Star_From_List` VALUES ('`a`','`User0_counts`'),('`a`','`item20_counts`');
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
INSERT INTO `ADT_RNodes_Star_Select_List` VALUES ('`a`','`Age(User0)`'),('`a`','`Gender(User0)`'),('`a`','`Action(item20)`'),('`a`','`Drama(item20)`'),('`a`','`Horror(item20)`');
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
INSERT INTO `AttributeColumns` VALUES ('item2','Action'),('item2','Drama'),('item2','Horror'),('u2base','rating'),('User','Age'),('User','Gender');
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
INSERT INTO `Attribute_Value` VALUES ('Action','0'),('Action','1'),('Drama','0'),('Drama','1'),('Horror','0'),('Horror','1'),('rating','1'),('rating','2'),('rating','3'),('rating','4'),('rating','5'),('Age','1'),('Age','18'),('Age','25'),('Age','35'),('Age','45'),('Age','50'),('Age','56'),('Gender','F'),('Gender','M');
/*!40000 ALTER TABLE `Attribute_Value` ENABLE KEYS */;
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
INSERT INTO `EntityTables` VALUES ('item2','item_id'),('User','user_id');
/*!40000 ALTER TABLE `EntityTables` ENABLE KEYS */;
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
INSERT INTO `FNodes` VALUES ('`Action(item20)`','Action','1Node',1),('`Age(User0)`','Age','1Node',1),('`a`','u2base','Rnode',1),('`Drama(item20)`','Drama','1Node',1),('`Gender(User0)`','Gender','1Node',1),('`Horror(item20)`','Horror','1Node',1),('`rating(User0,item20)`','rating','2Node',1);
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
INSERT INTO `FNodes_pvars` VALUES ('`rating(User0,item20)`','User0'),('`rating(User0,item20)`','item20'),('`Action(item20)`','item20'),('`Age(User0)`','User0'),('`Drama(item20)`','item20'),('`Gender(User0)`','User0'),('`Horror(item20)`','item20');
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
INSERT INTO `ForeignKeyColumns` VALUES ('u2base','item_id','item2','item_id','fk_u2base_1',2),('u2base','user_id','User','user_id','fk_u2base_2',1);
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
INSERT INTO `ForeignKeys_pvars` VALUES ('u2base','item2','item_id','item20',0,2),('u2base','User','user_id','User0',0,1);
/*!40000 ALTER TABLE `ForeignKeys_pvars` ENABLE KEYS */;
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
INSERT INTO `InputColumns` VALUES ('item2','item_id',NULL,NULL,'PRIMARY',1),('u2base','item_id',NULL,NULL,'PRIMARY',2),('u2base','user_id',NULL,NULL,'PRIMARY',1),('User','user_id',NULL,NULL,'PRIMARY',1);
/*!40000 ALTER TABLE `InputColumns` ENABLE KEYS */;
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
INSERT INTO `PVariables` VALUES ('item20','item2',0),('User0','User',0);
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
INSERT INTO `PVariables_From_List` VALUES ('item20','item2 AS item20'),('User0','User AS User0');
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
INSERT INTO `PVariables_GroupBy_List` VALUES ('item20','`Action(item20)`'),('User0','`Age(User0)`'),('item20','`Drama(item20)`'),('User0','`Gender(User0)`'),('item20','`Horror(item20)`');
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
INSERT INTO `PVariables_Select_List` VALUES ('item20','count(*) as \"MULT\"'),('User0','count(*) as \"MULT\"'),('item20','item20.Action AS `Action(item20)`'),('User0','User0.Age AS `Age(User0)`'),('item20','item20.Drama AS `Drama(item20)`'),('User0','User0.Gender AS `Gender(User0)`'),('item20','item20.Horror AS `Horror(item20)`');
/*!40000 ALTER TABLE `PVariables_Select_List` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Path_BN_nodes`
--

DROP TABLE IF EXISTS `Path_BN_nodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Path_BN_nodes` (
  `Rchain` varchar(20) NOT NULL DEFAULT '',
  `node` varchar(199) CHARACTER SET utf8 DEFAULT NULL,
  KEY `HashIndex` (`Rchain`,`node`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Path_BN_nodes`
--

LOCK TABLES `Path_BN_nodes` WRITE;
/*!40000 ALTER TABLE `Path_BN_nodes` DISABLE KEYS */;
INSERT INTO `Path_BN_nodes` VALUES ('`a`','`Action(item20)`'),('`a`','`Age(User0)`'),('`a`','`a`'),('`a`','`Drama(item20)`'),('`a`','`Gender(User0)`'),('`a`','`Horror(item20)`'),('`a`','`rating(User0,item20)`');
/*!40000 ALTER TABLE `Path_BN_nodes` ENABLE KEYS */;
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
INSERT INTO `RChain_pvars` VALUES ('`a`','User0'),('`a`','item20');
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
INSERT INTO `RNodes` VALUES ('`u2base(User0,item20)`','u2base','User0','item20','user_id','item_id',1,'`a`');
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
INSERT INTO `RNodes_1Nodes` VALUES ('`a`','u2base','`Age(User0)`','Age','User0'),('`a`','u2base','`Gender(User0)`','Gender','User0'),('`a`','u2base','`Action(item20)`','Action','item20'),('`a`','u2base','`Drama(item20)`','Drama','item20'),('`a`','u2base','`Horror(item20)`','Horror','item20');
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
INSERT INTO `RNodes_2Nodes` VALUES ('`a`','`rating(User0,item20)`');
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
  `Fid` varchar(199) CHARACTER SET utf8 DEFAULT NULL,
  `main` int(11) DEFAULT NULL,
  KEY `Index_rnid` (`rnid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_BN_Nodes`
--

LOCK TABLES `RNodes_BN_Nodes` WRITE;
/*!40000 ALTER TABLE `RNodes_BN_Nodes` DISABLE KEYS */;
INSERT INTO `RNodes_BN_Nodes` VALUES ('`a`','`Action(item20)`',1),('`a`','`Age(User0)`',1),('`a`','`Drama(item20)`',1),('`a`','`Gender(User0)`',1),('`a`','`Horror(item20)`',1),('`a`','`rating(User0,item20)`',1),('`a`','`a`',1);
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
  `Entries` varchar(146) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_From_List`
--

LOCK TABLES `RNodes_From_List` WRITE;
/*!40000 ALTER TABLE `RNodes_From_List` DISABLE KEYS */;
INSERT INTO `RNodes_From_List` VALUES ('`a`','MovieLens_TQ.User AS User0'),('`a`','MovieLens_TQ.item2 AS item20'),('`a`','MovieLens_TQ.u2base AS `a`'),('`a`','(select \"T\" as `a`) as `temp_a`');
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
  `Entries` varchar(199) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_GroupBy_List`
--

LOCK TABLES `RNodes_GroupBy_List` WRITE;
/*!40000 ALTER TABLE `RNodes_GroupBy_List` DISABLE KEYS */;
INSERT INTO `RNodes_GroupBy_List` VALUES ('`a`','`Age(User0)`'),('`a`','`Gender(User0)`'),('`a`','`Action(item20)`'),('`a`','`Drama(item20)`'),('`a`','`Horror(item20)`'),('`a`','`rating(User0,item20)`'),('`a`','`a`');
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
INSERT INTO `RNodes_Select_List` VALUES ('`a`','count(*) as \"MULT\"'),('`a`','User0.Age AS `Age(User0)`'),('`a`','User0.Gender AS `Gender(User0)`'),('`a`','item20.Action AS `Action(item20)`'),('`a`','item20.Drama AS `Drama(item20)`'),('`a`','item20.Horror AS `Horror(item20)`'),('`a`','`a`.rating AS `rating(User0,item20)`'),('`a`','`a`');
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
INSERT INTO `RNodes_Where_List` VALUES ('`a`','`a`.user_id = User0.user_id'),('`a`','`a`.item_id = item20.item_id');
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
INSERT INTO `RNodes_pvars` VALUES ('`a`','User0','User','user_id','user_id'),('`a`','item20','item2','item_id','item_id');
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
INSERT INTO `RelationTables` VALUES ('u2base',0,0);
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
INSERT INTO `Rnodes_join_columnname_list` VALUES ('`a`','`rating(User0,item20)` varchar(5)  default  \"N/A\" ');
/*!40000 ALTER TABLE `Rnodes_join_columnname_list` ENABLE KEYS */;
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
INSERT INTO `lattice_mapping` VALUES ('`u2base(User0,item20)`','`a`');
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
INSERT INTO `lattice_membership` VALUES ('`a`','`a`');
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
INSERT INTO `lattice_rel` VALUES ('EmptySet','`a`','`a`');
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
INSERT INTO `lattice_set` VALUES ('`a`',1);
/*!40000 ALTER TABLE `lattice_set` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-09-30 14:50:34
