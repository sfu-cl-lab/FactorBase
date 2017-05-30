CREATE DATABASE  IF NOT EXISTS `Financial_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Financial_std_BN`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Financial_std_BN
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
INSERT INTO `1Nodes` VALUES ('`amount(loan0)`','amount','loan0',1),('`amount2(trans0)`','amount2','trans0',1),('`amount3(order20)`','amount3','order20',1),('`balance(trans0)`','balance','trans0',1),('`bank_to(order20)`','bank_to','order20',1),('`duration(loan0)`','duration','loan0',1),('`frequency(acc0)`','frequency','acc0',1),('`k_symbol(order20)`','k_symbol','order20',1),('`operation(trans0)`','operation','trans0',1),('`payments(loan0)`','payments','loan0',1),('`status(loan0)`','status','loan0',1),('`type(trans0)`','type','trans0',1);
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
INSERT INTO `1Nodes_From_List` VALUES ('`amount(loan0)`','loan AS loan0'),('`amount2(trans0)`','trans AS trans0'),('`amount3(order20)`','order2 AS order20'),('`balance(trans0)`','trans AS trans0'),('`bank_to(order20)`','order2 AS order20'),('`duration(loan0)`','loan AS loan0'),('`frequency(acc0)`','acc AS acc0'),('`k_symbol(order20)`','order2 AS order20'),('`operation(trans0)`','trans AS trans0'),('`payments(loan0)`','loan AS loan0'),('`status(loan0)`','loan AS loan0'),('`type(trans0)`','trans AS trans0');
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
INSERT INTO `1Nodes_Select_List` VALUES ('`amount(loan0)`','loan0.amount AS `amount(loan0)`'),('`amount2(trans0)`','trans0.amount2 AS `amount2(trans0)`'),('`amount3(order20)`','order20.amount3 AS `amount3(order20)`'),('`balance(trans0)`','trans0.balance AS `balance(trans0)`'),('`bank_to(order20)`','order20.bank_to AS `bank_to(order20)`'),('`duration(loan0)`','loan0.duration AS `duration(loan0)`'),('`frequency(acc0)`','acc0.frequency AS `frequency(acc0)`'),('`k_symbol(order20)`','order20.k_symbol AS `k_symbol(order20)`'),('`operation(trans0)`','trans0.operation AS `operation(trans0)`'),('`payments(loan0)`','loan0.payments AS `payments(loan0)`'),('`status(loan0)`','loan0.status AS `status(loan0)`'),('`type(trans0)`','trans0.type AS `type(trans0)`');
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
  `Entries` varchar(147) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_From_List`
--

LOCK TABLES `ADT_PVariables_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_From_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_From_List` VALUES ('acc0','Financial_std.acc AS acc0'),('loan0','Financial_std.loan AS loan0'),('order20','Financial_std.order2 AS order20'),('trans0','Financial_std.trans AS trans0');
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
INSERT INTO `ADT_PVariables_GroupBy_List` VALUES ('loan0','`amount(loan0)`'),('trans0','`amount2(trans0)`'),('order20','`amount3(order20)`'),('trans0','`balance(trans0)`'),('order20','`bank_to(order20)`'),('loan0','`duration(loan0)`'),('acc0','`frequency(acc0)`'),('order20','`k_symbol(order20)`'),('trans0','`operation(trans0)`'),('loan0','`payments(loan0)`'),('loan0','`status(loan0)`'),('trans0','`type(trans0)`');
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
INSERT INTO `ADT_PVariables_Select_List` VALUES ('acc0','count(*) as \"MULT\"'),('loan0','count(*) as \"MULT\"'),('order20','count(*) as \"MULT\"'),('trans0','count(*) as \"MULT\"'),('loan0','loan0.amount AS `amount(loan0)`'),('trans0','trans0.amount2 AS `amount2(trans0)`'),('order20','order20.amount3 AS `amount3(order20)`'),('trans0','trans0.balance AS `balance(trans0)`'),('order20','order20.bank_to AS `bank_to(order20)`'),('loan0','loan0.duration AS `duration(loan0)`'),('acc0','acc0.frequency AS `frequency(acc0)`'),('order20','order20.k_symbol AS `k_symbol(order20)`'),('trans0','trans0.operation AS `operation(trans0)`'),('loan0','loan0.payments AS `payments(loan0)`'),('loan0','loan0.status AS `status(loan0)`'),('trans0','trans0.type AS `type(trans0)`');
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
INSERT INTO `ADT_RChain_Star_From_List` VALUES ('`a,b,c`','`c`','`a,b_CT`'),('`a,b,c`','`b`','`a,c_CT`'),('`a,b`','`b`','`a_CT`'),('`a,c`','`c`','`a_CT`'),('`a,b,c`','`a`','`b,c_CT`'),('`a,b`','`a`','`b_CT`'),('`b,c`','`c`','`b_CT`'),('`a,c`','`a`','`c_CT`'),('`b,c`','`b`','`c_CT`'),('`a,b,c`','`c`','`order20_counts`'),('`a,b,c`','`b`','`trans0_counts`'),('`a,b`','`b`','`trans0_counts`'),('`a,c`','`c`','`order20_counts`'),('`a,b,c`','`a`','`acc0_counts`'),('`a,b`','`a`','`acc0_counts`'),('`b,c`','`c`','`order20_counts`'),('`a,c`','`a`','`acc0_counts`'),('`b,c`','`b`','`trans0_counts`');
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
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b,c`','`c`','`amount(loan0)`'),('`a,b,c`','`b`','`amount(loan0)`'),('`a,b`','`b`','`amount(loan0)`'),('`a,c`','`c`','`amount(loan0)`'),('`a,b,c`','`c`','`amount2(trans0)`'),('`a,b,c`','`a`','`amount2(trans0)`'),('`a,b`','`a`','`amount2(trans0)`'),('`b,c`','`c`','`amount2(trans0)`'),('`a,b,c`','`b`','`amount3(order20)`'),('`a,b,c`','`a`','`amount3(order20)`'),('`a,c`','`a`','`amount3(order20)`'),('`b,c`','`b`','`amount3(order20)`'),('`a,b,c`','`c`','`balance(trans0)`'),('`a,b,c`','`a`','`balance(trans0)`'),('`a,b`','`a`','`balance(trans0)`'),('`b,c`','`c`','`balance(trans0)`'),('`a,b,c`','`b`','`bank_to(order20)`'),('`a,b,c`','`a`','`bank_to(order20)`'),('`a,c`','`a`','`bank_to(order20)`'),('`b,c`','`b`','`bank_to(order20)`'),('`a,b,c`','`c`','`duration(loan0)`'),('`a,b,c`','`b`','`duration(loan0)`'),('`a,b`','`b`','`duration(loan0)`'),('`a,c`','`c`','`duration(loan0)`'),('`a,b,c`','`b`','`k_symbol(order20)`'),('`a,b,c`','`a`','`k_symbol(order20)`'),('`a,c`','`a`','`k_symbol(order20)`'),('`b,c`','`b`','`k_symbol(order20)`'),('`a,b,c`','`c`','`operation(trans0)`'),('`a,b,c`','`a`','`operation(trans0)`'),('`a,b`','`a`','`operation(trans0)`'),('`b,c`','`c`','`operation(trans0)`'),('`a,b,c`','`c`','`payments(loan0)`'),('`a,b,c`','`b`','`payments(loan0)`'),('`a,b`','`b`','`payments(loan0)`'),('`a,c`','`c`','`payments(loan0)`'),('`a,b,c`','`c`','`status(loan0)`'),('`a,b,c`','`b`','`status(loan0)`'),('`a,b`','`b`','`status(loan0)`'),('`a,c`','`c`','`status(loan0)`'),('`a,b,c`','`c`','`type(trans0)`'),('`a,b,c`','`a`','`type(trans0)`'),('`a,b`','`a`','`type(trans0)`'),('`b,c`','`c`','`type(trans0)`'),('`a,b,c`','`a`','`amount(loan0)`'),('`a,b`','`a`','`amount(loan0)`'),('`b,c`','`c`','`amount(loan0)`'),('`a,c`','`a`','`amount(loan0)`'),('`b,c`','`b`','`amount(loan0)`'),('`a,b,c`','`a`','`duration(loan0)`'),('`a,b`','`a`','`duration(loan0)`'),('`b,c`','`c`','`duration(loan0)`'),('`a,c`','`a`','`duration(loan0)`'),('`b,c`','`b`','`duration(loan0)`'),('`a,b,c`','`c`','`frequency(acc0)`'),('`a,b,c`','`b`','`frequency(acc0)`'),('`a,b`','`b`','`frequency(acc0)`'),('`a,c`','`c`','`frequency(acc0)`'),('`a,b,c`','`a`','`payments(loan0)`'),('`a,b`','`a`','`payments(loan0)`'),('`b,c`','`c`','`payments(loan0)`'),('`a,c`','`a`','`payments(loan0)`'),('`b,c`','`b`','`payments(loan0)`'),('`a,b,c`','`a`','`status(loan0)`'),('`a,b`','`a`','`status(loan0)`'),('`b,c`','`c`','`status(loan0)`'),('`a,c`','`a`','`status(loan0)`'),('`b,c`','`b`','`status(loan0)`'),('`a,b,c`','`c`','`a`'),('`a,b,c`','`b`','`a`'),('`a,b`','`b`','`a`'),('`a,c`','`c`','`a`'),('`a,b,c`','`c`','`b`'),('`a,b,c`','`a`','`b`'),('`a,b`','`a`','`b`'),('`b,c`','`c`','`b`'),('`a,b,c`','`b`','`c`'),('`a,b,c`','`a`','`c`'),('`a,c`','`a`','`c`'),('`b,c`','`b`','`c`'),('`a,b,c`','`b`','`amount2(trans0)`'),('`a,b`','`b`','`amount2(trans0)`'),('`b,c`','`b`','`amount2(trans0)`'),('`a,b,c`','`c`','`amount3(order20)`'),('`a,c`','`c`','`amount3(order20)`'),('`b,c`','`c`','`amount3(order20)`'),('`a,b,c`','`b`','`balance(trans0)`'),('`a,b`','`b`','`balance(trans0)`'),('`b,c`','`b`','`balance(trans0)`'),('`a,b,c`','`c`','`bank_to(order20)`'),('`a,c`','`c`','`bank_to(order20)`'),('`b,c`','`c`','`bank_to(order20)`'),('`a,b,c`','`a`','`frequency(acc0)`'),('`a,b`','`a`','`frequency(acc0)`'),('`a,c`','`a`','`frequency(acc0)`'),('`a,b,c`','`c`','`k_symbol(order20)`'),('`a,c`','`c`','`k_symbol(order20)`'),('`b,c`','`c`','`k_symbol(order20)`'),('`a,b,c`','`b`','`operation(trans0)`'),('`a,b`','`b`','`operation(trans0)`'),('`b,c`','`b`','`operation(trans0)`'),('`a,b,c`','`b`','`type(trans0)`'),('`a,b`','`b`','`type(trans0)`'),('`b,c`','`b`','`type(trans0)`'),('`a`','`a`','`amount(loan0)`'),('`b`','`b`','`amount(loan0)`'),('`c`','`c`','`amount(loan0)`'),('`b`','`b`','`amount2(trans0)`'),('`c`','`c`','`amount3(order20)`'),('`b`','`b`','`balance(trans0)`'),('`c`','`c`','`bank_to(order20)`'),('`a`','`a`','`duration(loan0)`'),('`b`','`b`','`duration(loan0)`'),('`c`','`c`','`duration(loan0)`'),('`a`','`a`','`frequency(acc0)`'),('`c`','`c`','`k_symbol(order20)`'),('`b`','`b`','`operation(trans0)`'),('`a`','`a`','`payments(loan0)`'),('`b`','`b`','`payments(loan0)`'),('`c`','`c`','`payments(loan0)`'),('`a`','`a`','`status(loan0)`'),('`b`','`b`','`status(loan0)`'),('`c`','`c`','`status(loan0)`'),('`b`','`b`','`type(trans0)`');
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
INSERT INTO `ADT_RNodes_1Nodes_GroupBY_List` VALUES ('`a`','`amount(loan0)`'),('`b`','`amount2(trans0)`'),('`c`','`amount3(order20)`'),('`b`','`balance(trans0)`'),('`c`','`bank_to(order20)`'),('`a`','`duration(loan0)`'),('`c`','`k_symbol(order20)`'),('`b`','`operation(trans0)`'),('`a`','`payments(loan0)`'),('`a`','`status(loan0)`'),('`b`','`type(trans0)`'),('`b`','`amount(loan0)`'),('`c`','`amount(loan0)`'),('`b`','`duration(loan0)`'),('`c`','`duration(loan0)`'),('`a`','`frequency(acc0)`'),('`b`','`payments(loan0)`'),('`c`','`payments(loan0)`'),('`b`','`status(loan0)`'),('`c`','`status(loan0)`');
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
INSERT INTO `ADT_RNodes_1Nodes_Select_List` VALUES ('`a`','sum(`a_counts`.`MULT`) as \"MULT\"'),('`b`','sum(`b_counts`.`MULT`) as \"MULT\"'),('`c`','sum(`c_counts`.`MULT`) as \"MULT\"'),('`a`','`amount(loan0)`'),('`b`','`amount2(trans0)`'),('`c`','`amount3(order20)`'),('`b`','`balance(trans0)`'),('`c`','`bank_to(order20)`'),('`a`','`duration(loan0)`'),('`c`','`k_symbol(order20)`'),('`b`','`operation(trans0)`'),('`a`','`payments(loan0)`'),('`a`','`status(loan0)`'),('`b`','`type(trans0)`'),('`b`','`amount(loan0)`'),('`c`','`amount(loan0)`'),('`b`','`duration(loan0)`'),('`c`','`duration(loan0)`'),('`a`','`frequency(acc0)`'),('`b`','`payments(loan0)`'),('`c`','`payments(loan0)`'),('`b`','`status(loan0)`'),('`c`','`status(loan0)`');
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
INSERT INTO `ADT_RNodes_False_Select_List` VALUES ('`a`','(`a_star`.MULT-`a_flat`.MULT) AS \"MULT\"'),('`b`','(`b_star`.MULT-`b_flat`.MULT) AS \"MULT\"'),('`c`','(`c_star`.MULT-`c_flat`.MULT) AS \"MULT\"'),('`a`','`a_star`.`amount(loan0)`'),('`b`','`b_star`.`amount2(trans0)`'),('`c`','`c_star`.`amount3(order20)`'),('`b`','`b_star`.`balance(trans0)`'),('`c`','`c_star`.`bank_to(order20)`'),('`a`','`a_star`.`duration(loan0)`'),('`c`','`c_star`.`k_symbol(order20)`'),('`b`','`b_star`.`operation(trans0)`'),('`a`','`a_star`.`payments(loan0)`'),('`a`','`a_star`.`status(loan0)`'),('`b`','`b_star`.`type(trans0)`'),('`b`','`b_star`.`amount(loan0)`'),('`c`','`c_star`.`amount(loan0)`'),('`b`','`b_star`.`duration(loan0)`'),('`c`','`c_star`.`duration(loan0)`'),('`a`','`a_star`.`frequency(acc0)`'),('`b`','`b_star`.`payments(loan0)`'),('`c`','`c_star`.`payments(loan0)`'),('`b`','`b_star`.`status(loan0)`'),('`c`','`c_star`.`status(loan0)`');
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
INSERT INTO `ADT_RNodes_False_WHERE_List` VALUES ('`a`','`a_star`.`amount(loan0)`=`a_flat`.`amount(loan0)`'),('`b`','`b_star`.`amount2(trans0)`=`b_flat`.`amount2(trans0)`'),('`c`','`c_star`.`amount3(order20)`=`c_flat`.`amount3(order20)`'),('`b`','`b_star`.`balance(trans0)`=`b_flat`.`balance(trans0)`'),('`c`','`c_star`.`bank_to(order20)`=`c_flat`.`bank_to(order20)`'),('`a`','`a_star`.`duration(loan0)`=`a_flat`.`duration(loan0)`'),('`c`','`c_star`.`k_symbol(order20)`=`c_flat`.`k_symbol(order20)`'),('`b`','`b_star`.`operation(trans0)`=`b_flat`.`operation(trans0)`'),('`a`','`a_star`.`payments(loan0)`=`a_flat`.`payments(loan0)`'),('`a`','`a_star`.`status(loan0)`=`a_flat`.`status(loan0)`'),('`b`','`b_star`.`type(trans0)`=`b_flat`.`type(trans0)`'),('`b`','`b_star`.`amount(loan0)`=`b_flat`.`amount(loan0)`'),('`c`','`c_star`.`amount(loan0)`=`c_flat`.`amount(loan0)`'),('`b`','`b_star`.`duration(loan0)`=`b_flat`.`duration(loan0)`'),('`c`','`c_star`.`duration(loan0)`=`c_flat`.`duration(loan0)`'),('`a`','`a_star`.`frequency(acc0)`=`a_flat`.`frequency(acc0)`'),('`b`','`b_star`.`payments(loan0)`=`b_flat`.`payments(loan0)`'),('`c`','`c_star`.`payments(loan0)`=`c_flat`.`payments(loan0)`'),('`b`','`b_star`.`status(loan0)`=`b_flat`.`status(loan0)`'),('`c`','`c_star`.`status(loan0)`=`c_flat`.`status(loan0)`');
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
INSERT INTO `ADT_RNodes_Star_From_List` VALUES ('`a`','`loan0_counts`'),('`b`','`trans0_counts`'),('`c`','`order20_counts`'),('`a`','`acc0_counts`'),('`b`','`loan0_counts`'),('`c`','`loan0_counts`');
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
INSERT INTO `ADT_RNodes_Star_Select_List` VALUES ('`a`','`amount(loan0)`'),('`b`','`amount2(trans0)`'),('`c`','`amount3(order20)`'),('`b`','`balance(trans0)`'),('`c`','`bank_to(order20)`'),('`a`','`duration(loan0)`'),('`c`','`k_symbol(order20)`'),('`b`','`operation(trans0)`'),('`a`','`payments(loan0)`'),('`a`','`status(loan0)`'),('`b`','`type(trans0)`'),('`b`','`amount(loan0)`'),('`c`','`amount(loan0)`'),('`b`','`duration(loan0)`'),('`c`','`duration(loan0)`'),('`a`','`frequency(acc0)`'),('`b`','`payments(loan0)`'),('`c`','`payments(loan0)`'),('`b`','`status(loan0)`'),('`c`','`status(loan0)`');
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
INSERT INTO `AttributeColumns` VALUES ('acc','frequency'),('loan','amount'),('loan','duration'),('loan','payments'),('loan','status'),('order2','amount3'),('order2','bank_to'),('order2','k_symbol'),('trans','amount2'),('trans','balance'),('trans','operation'),('trans','type');
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
INSERT INTO `Attribute_Value` VALUES ('frequency','afterTrans'),('frequency','Monthly'),('frequency','Weekly'),('amount','0'),('amount','1'),('amount','2'),('amount','3'),('amount','4'),('duration','12'),('duration','24'),('duration','36'),('duration','48'),('duration','60'),('payments','0'),('payments','1'),('payments','2'),('payments','3'),('payments','4'),('status','A'),('status','B'),('amount3','0'),('amount3','1'),('amount3','2'),('amount3','3'),('amount3','4'),('bank_to','AB'),('bank_to','CD'),('bank_to','EF'),('bank_to','GH'),('bank_to','IJ'),('bank_to','KL'),('bank_to','MN'),('bank_to','OP'),('bank_to','QR'),('bank_to','ST'),('bank_to','UV'),('bank_to','WX'),('bank_to','YZ'),('k_symbol','Household'),('k_symbol','Insurance'),('k_symbol','Leasing'),('k_symbol','Loan'),('amount2','0'),('amount2','1'),('amount2','2'),('amount2','3'),('balance','0'),('balance','1'),('balance','2'),('balance','3'),('operation','ccw'),('operation','cic'),('operation','Collection'),('operation','cw'),('operation','Remittance'),('type','Credit'),('type','VYBER'),('type','Withdrawal');
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
INSERT INTO `EntityTables` VALUES ('acc','account_id'),('loan','loan_id'),('order2','order_id'),('trans','transaction_id');
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
INSERT INTO `FNodes` VALUES ('`amount(loan0)`','amount','1Node',1),('`amount2(trans0)`','amount2','1Node',1),('`amount3(order20)`','amount3','1Node',1),('`a`','Loan_Acc','Rnode',1),('`balance(trans0)`','balance','1Node',1),('`bank_to(order20)`','bank_to','1Node',1),('`b`','Loan_Order','Rnode',1),('`c`','Loan_Trans','Rnode',1),('`duration(loan0)`','duration','1Node',1),('`frequency(acc0)`','frequency','1Node',1),('`k_symbol(order20)`','k_symbol','1Node',1),('`operation(trans0)`','operation','1Node',1),('`payments(loan0)`','payments','1Node',1),('`status(loan0)`','status','1Node',1),('`type(trans0)`','type','1Node',1);
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
INSERT INTO `FNodes_pvars` VALUES ('`amount(loan0)`','loan0'),('`amount2(trans0)`','trans0'),('`amount3(order20)`','order20'),('`balance(trans0)`','trans0'),('`bank_to(order20)`','order20'),('`duration(loan0)`','loan0'),('`frequency(acc0)`','acc0'),('`k_symbol(order20)`','order20'),('`operation(trans0)`','trans0'),('`payments(loan0)`','loan0'),('`status(loan0)`','loan0'),('`type(trans0)`','trans0');
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
INSERT INTO `ForeignKeyColumns` VALUES ('Loan_Acc','account_id','acc','account_id','FK_rel1_1',2),('Loan_Acc','loan_id','loan','loan_id','FK_rel1_2',1),('Loan_Order','loan_id','loan','loan_id','FK_rel5_2',2),('Loan_Order','transaction_id','trans','transaction_id','FK_rel5_1',1),('Loan_Trans','loan_id','loan','loan_id','FK_rel2_2',2),('Loan_Trans','order_id','order2','order_id','FK_rel2_1',1);
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
INSERT INTO `ForeignKeys_pvars` VALUES ('Loan_Acc','acc','account_id','acc0',0,2),('Loan_Acc','loan','loan_id','loan0',0,1),('Loan_Order','loan','loan_id','loan0',0,2),('Loan_Order','trans','transaction_id','trans0',0,1),('Loan_Trans','loan','loan_id','loan0',0,2),('Loan_Trans','order2','order_id','order20',0,1);
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
INSERT INTO `InputColumns` VALUES ('acc','account_id',NULL,NULL,'PRIMARY',1),('loan','loan_id',NULL,NULL,'PRIMARY',1),('Loan_Acc','account_id',NULL,NULL,'PRIMARY',2),('Loan_Acc','loan_id',NULL,NULL,'PRIMARY',1),('Loan_Order','loan_id',NULL,NULL,'PRIMARY',2),('Loan_Order','transaction_id',NULL,NULL,'PRIMARY',1),('Loan_Trans','loan_id',NULL,NULL,'PRIMARY',2),('Loan_Trans','order_id',NULL,NULL,'PRIMARY',1),('order2','order_id',NULL,NULL,'PRIMARY',1),('trans','transaction_id',NULL,NULL,'PRIMARY',1);
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
INSERT INTO `PVariables` VALUES ('acc0','acc',0),('loan0','loan',0),('order20','order2',0),('trans0','trans',0);
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
INSERT INTO `PVariables_From_List` VALUES ('acc0','acc AS acc0'),('loan0','loan AS loan0'),('order20','order2 AS order20'),('trans0','trans AS trans0');
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
INSERT INTO `PVariables_GroupBy_List` VALUES ('loan0','`amount(loan0)`'),('trans0','`amount2(trans0)`'),('order20','`amount3(order20)`'),('trans0','`balance(trans0)`'),('order20','`bank_to(order20)`'),('loan0','`duration(loan0)`'),('acc0','`frequency(acc0)`'),('order20','`k_symbol(order20)`'),('trans0','`operation(trans0)`'),('loan0','`payments(loan0)`'),('loan0','`status(loan0)`'),('trans0','`type(trans0)`');
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
INSERT INTO `PVariables_Select_List` VALUES ('acc0','count(*) as \"MULT\"'),('loan0','count(*) as \"MULT\"'),('order20','count(*) as \"MULT\"'),('trans0','count(*) as \"MULT\"'),('loan0','loan0.amount AS `amount(loan0)`'),('trans0','trans0.amount2 AS `amount2(trans0)`'),('order20','order20.amount3 AS `amount3(order20)`'),('trans0','trans0.balance AS `balance(trans0)`'),('order20','order20.bank_to AS `bank_to(order20)`'),('loan0','loan0.duration AS `duration(loan0)`'),('acc0','acc0.frequency AS `frequency(acc0)`'),('order20','order20.k_symbol AS `k_symbol(order20)`'),('trans0','trans0.operation AS `operation(trans0)`'),('loan0','loan0.payments AS `payments(loan0)`'),('loan0','loan0.status AS `status(loan0)`'),('trans0','trans0.type AS `type(trans0)`');
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
INSERT INTO `Path_BN_nodes` VALUES ('`a,b,c`','`amount(loan0)`'),('`a,b,c`','`amount2(trans0)`'),('`a,b,c`','`amount3(order20)`'),('`a,b,c`','`a`'),('`a,b,c`','`balance(trans0)`'),('`a,b,c`','`bank_to(order20)`'),('`a,b,c`','`b`'),('`a,b,c`','`c`'),('`a,b,c`','`duration(loan0)`'),('`a,b,c`','`frequency(acc0)`'),('`a,b,c`','`k_symbol(order20)`'),('`a,b,c`','`operation(trans0)`'),('`a,b,c`','`payments(loan0)`'),('`a,b,c`','`status(loan0)`'),('`a,b,c`','`type(trans0)`'),('`a,b`','`amount(loan0)`'),('`a,b`','`amount2(trans0)`'),('`a,b`','`a`'),('`a,b`','`balance(trans0)`'),('`a,b`','`b`'),('`a,b`','`duration(loan0)`'),('`a,b`','`frequency(acc0)`'),('`a,b`','`operation(trans0)`'),('`a,b`','`payments(loan0)`'),('`a,b`','`status(loan0)`'),('`a,b`','`type(trans0)`'),('`a,c`','`amount(loan0)`'),('`a,c`','`amount3(order20)`'),('`a,c`','`a`'),('`a,c`','`bank_to(order20)`'),('`a,c`','`c`'),('`a,c`','`duration(loan0)`'),('`a,c`','`frequency(acc0)`'),('`a,c`','`k_symbol(order20)`'),('`a,c`','`payments(loan0)`'),('`a,c`','`status(loan0)`'),('`a`','`amount(loan0)`'),('`a`','`a`'),('`a`','`duration(loan0)`'),('`a`','`frequency(acc0)`'),('`a`','`payments(loan0)`'),('`a`','`status(loan0)`'),('`b,c`','`amount(loan0)`'),('`b,c`','`amount2(trans0)`'),('`b,c`','`amount3(order20)`'),('`b,c`','`balance(trans0)`'),('`b,c`','`bank_to(order20)`'),('`b,c`','`b`'),('`b,c`','`c`'),('`b,c`','`duration(loan0)`'),('`b,c`','`k_symbol(order20)`'),('`b,c`','`operation(trans0)`'),('`b,c`','`payments(loan0)`'),('`b,c`','`status(loan0)`'),('`b,c`','`type(trans0)`'),('`b`','`amount(loan0)`'),('`b`','`amount2(trans0)`'),('`b`','`balance(trans0)`'),('`b`','`b`'),('`b`','`duration(loan0)`'),('`b`','`operation(trans0)`'),('`b`','`payments(loan0)`'),('`b`','`status(loan0)`'),('`b`','`type(trans0)`'),('`c`','`amount(loan0)`'),('`c`','`amount3(order20)`'),('`c`','`bank_to(order20)`'),('`c`','`c`'),('`c`','`duration(loan0)`'),('`c`','`k_symbol(order20)`'),('`c`','`payments(loan0)`'),('`c`','`status(loan0)`');
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
INSERT INTO `RChain_pvars` VALUES ('`a,b,c`','loan0'),('`a,b,c`','acc0'),('`a,b,c`','trans0'),('`a,b,c`','order20'),('`a,b`','loan0'),('`a,b`','acc0'),('`a,b`','trans0'),('`a,c`','loan0'),('`a,c`','acc0'),('`a,c`','order20'),('`a`','loan0'),('`a`','acc0'),('`b,c`','trans0'),('`b,c`','loan0'),('`b,c`','order20'),('`b`','trans0'),('`b`','loan0'),('`c`','order20'),('`c`','loan0');
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
INSERT INTO `RNodes` VALUES ('`Loan_Acc(loan0,acc0)`','Loan_Acc','loan0','acc0','loan_id','account_id',1,'`a`'),('`Loan_Order(trans0,loan0)`','Loan_Order','trans0','loan0','transaction_id','loan_id',1,'`b`'),('`Loan_Trans(order20,loan0)`','Loan_Trans','order20','loan0','order_id','loan_id',1,'`c`');
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
INSERT INTO `RNodes_1Nodes` VALUES ('`a`','Loan_Acc','`amount(loan0)`','amount','loan0'),('`b`','Loan_Order','`amount2(trans0)`','amount2','trans0'),('`c`','Loan_Trans','`amount3(order20)`','amount3','order20'),('`b`','Loan_Order','`balance(trans0)`','balance','trans0'),('`c`','Loan_Trans','`bank_to(order20)`','bank_to','order20'),('`a`','Loan_Acc','`duration(loan0)`','duration','loan0'),('`c`','Loan_Trans','`k_symbol(order20)`','k_symbol','order20'),('`b`','Loan_Order','`operation(trans0)`','operation','trans0'),('`a`','Loan_Acc','`payments(loan0)`','payments','loan0'),('`a`','Loan_Acc','`status(loan0)`','status','loan0'),('`b`','Loan_Order','`type(trans0)`','type','trans0'),('`b`','Loan_Order','`amount(loan0)`','amount','loan0'),('`c`','Loan_Trans','`amount(loan0)`','amount','loan0'),('`b`','Loan_Order','`duration(loan0)`','duration','loan0'),('`c`','Loan_Trans','`duration(loan0)`','duration','loan0'),('`a`','Loan_Acc','`frequency(acc0)`','frequency','acc0'),('`b`','Loan_Order','`payments(loan0)`','payments','loan0'),('`c`','Loan_Trans','`payments(loan0)`','payments','loan0'),('`b`','Loan_Order','`status(loan0)`','status','loan0'),('`c`','Loan_Trans','`status(loan0)`','status','loan0');
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
INSERT INTO `RNodes_BN_Nodes` VALUES ('`a`','`amount(loan0)`',1),('`b`','`amount2(trans0)`',1),('`c`','`amount3(order20)`',1),('`b`','`balance(trans0)`',1),('`c`','`bank_to(order20)`',1),('`a`','`duration(loan0)`',1),('`c`','`k_symbol(order20)`',1),('`b`','`operation(trans0)`',1),('`a`','`payments(loan0)`',1),('`a`','`status(loan0)`',1),('`b`','`type(trans0)`',1),('`b`','`amount(loan0)`',1),('`c`','`amount(loan0)`',1),('`b`','`duration(loan0)`',1),('`c`','`duration(loan0)`',1),('`a`','`frequency(acc0)`',1),('`b`','`payments(loan0)`',1),('`c`','`payments(loan0)`',1),('`b`','`status(loan0)`',1),('`c`','`status(loan0)`',1),('`a`','`a`',1),('`b`','`b`',1),('`c`','`c`',1);
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
  `Entries` varchar(147) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_From_List`
--

LOCK TABLES `RNodes_From_List` WRITE;
/*!40000 ALTER TABLE `RNodes_From_List` DISABLE KEYS */;
INSERT INTO `RNodes_From_List` VALUES ('`a`','Financial_std.loan AS loan0'),('`b`','Financial_std.trans AS trans0'),('`c`','Financial_std.order2 AS order20'),('`a`','Financial_std.acc AS acc0'),('`b`','Financial_std.loan AS loan0'),('`c`','Financial_std.loan AS loan0'),('`a`','Financial_std.Loan_Acc AS `a`'),('`b`','Financial_std.Loan_Order AS `b`'),('`c`','Financial_std.Loan_Trans AS `c`'),('`a`','(select \"T\" as `a`) as `temp_a`'),('`b`','(select \"T\" as `b`) as `temp_b`'),('`c`','(select \"T\" as `c`) as `temp_c`');
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
INSERT INTO `RNodes_GroupBy_List` VALUES ('`a`','`amount(loan0)`'),('`b`','`amount2(trans0)`'),('`c`','`amount3(order20)`'),('`b`','`balance(trans0)`'),('`c`','`bank_to(order20)`'),('`a`','`duration(loan0)`'),('`c`','`k_symbol(order20)`'),('`b`','`operation(trans0)`'),('`a`','`payments(loan0)`'),('`a`','`status(loan0)`'),('`b`','`type(trans0)`'),('`b`','`amount(loan0)`'),('`c`','`amount(loan0)`'),('`b`','`duration(loan0)`'),('`c`','`duration(loan0)`'),('`a`','`frequency(acc0)`'),('`b`','`payments(loan0)`'),('`c`','`payments(loan0)`'),('`b`','`status(loan0)`'),('`c`','`status(loan0)`'),('`a`','`a`'),('`b`','`b`'),('`c`','`c`');
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
INSERT INTO `RNodes_Select_List` VALUES ('`a`','count(*) as \"MULT\"'),('`b`','count(*) as \"MULT\"'),('`c`','count(*) as \"MULT\"'),('`a`','loan0.amount AS `amount(loan0)`'),('`b`','trans0.amount2 AS `amount2(trans0)`'),('`c`','order20.amount3 AS `amount3(order20)`'),('`b`','trans0.balance AS `balance(trans0)`'),('`c`','order20.bank_to AS `bank_to(order20)`'),('`a`','loan0.duration AS `duration(loan0)`'),('`c`','order20.k_symbol AS `k_symbol(order20)`'),('`b`','trans0.operation AS `operation(trans0)`'),('`a`','loan0.payments AS `payments(loan0)`'),('`a`','loan0.status AS `status(loan0)`'),('`b`','trans0.type AS `type(trans0)`'),('`b`','loan0.amount AS `amount(loan0)`'),('`c`','loan0.amount AS `amount(loan0)`'),('`b`','loan0.duration AS `duration(loan0)`'),('`c`','loan0.duration AS `duration(loan0)`'),('`a`','acc0.frequency AS `frequency(acc0)`'),('`b`','loan0.payments AS `payments(loan0)`'),('`c`','loan0.payments AS `payments(loan0)`'),('`b`','loan0.status AS `status(loan0)`'),('`c`','loan0.status AS `status(loan0)`'),('`a`','`a`'),('`b`','`b`'),('`c`','`c`');
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
INSERT INTO `RNodes_Where_List` VALUES ('`a`','`a`.loan_id = loan0.loan_id'),('`b`','`b`.transaction_id = trans0.transaction_id'),('`c`','`c`.order_id = order20.order_id'),('`a`','`a`.account_id = acc0.account_id'),('`b`','`b`.loan_id = loan0.loan_id'),('`c`','`c`.loan_id = loan0.loan_id');
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
INSERT INTO `RNodes_pvars` VALUES ('`a`','loan0','loan','loan_id','loan_id'),('`b`','trans0','trans','transaction_id','transaction_id'),('`c`','order20','order2','order_id','order_id'),('`a`','acc0','acc','account_id','account_id'),('`b`','loan0','loan','loan_id','loan_id'),('`c`','loan0','loan','loan_id','loan_id');
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
INSERT INTO `RelationTables` VALUES ('Loan_Acc',0,0),('Loan_Order',0,0),('Loan_Trans',0,0);
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
INSERT INTO `lattice_mapping` VALUES ('`Loan_Acc(loan0,acc0),Loan_Order(trans0,loan0),Loan_Trans(order20,loan0)`','`a,b,c`'),('`Loan_Acc(loan0,acc0),Loan_Order(trans0,loan0)`','`a,b`'),('`Loan_Acc(loan0,acc0),Loan_Trans(order20,loan0)`','`a,c`'),('`Loan_Acc(loan0,acc0)`','`a`'),('`Loan_Order(trans0,loan0),Loan_Trans(order20,loan0)`','`b,c`'),('`Loan_Order(trans0,loan0)`','`b`'),('`Loan_Trans(order20,loan0)`','`c`');
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-09-30 14:49:38
