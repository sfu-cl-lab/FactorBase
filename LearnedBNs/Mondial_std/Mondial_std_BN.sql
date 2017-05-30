CREATE DATABASE  IF NOT EXISTS `Mondial_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Mondial_std_BN`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Mondial_std_BN
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
INSERT INTO `1Nodes` VALUES ('`agricul(eco0)`','agricul','eco0',1),('`class(country0)`','class','country0',1),('`class(country1)`','class','country1',0),('`continent(country0)`','continent','country0',1),('`continent(country1)`','continent','country1',0),('`gdp(eco0)`','gdp','eco0',1),('`govern(country0)`','govern','country0',1),('`govern(country1)`','govern','country1',0),('`industry(eco0)`','industry','eco0',1),('`inflation(eco0)`','inflation','eco0',1),('`percentage(country0)`','percentage','country0',1),('`percentage(country1)`','percentage','country1',0),('`popu(country0)`','popu','country0',1),('`popu(country1)`','popu','country1',0),('`service(eco0)`','service','eco0',1);
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
INSERT INTO `1Nodes_From_List` VALUES ('`agricul(eco0)`','eco AS eco0'),('`class(country0)`','country AS country0'),('`class(country1)`','country AS country1'),('`continent(country0)`','country AS country0'),('`continent(country1)`','country AS country1'),('`gdp(eco0)`','eco AS eco0'),('`govern(country0)`','country AS country0'),('`govern(country1)`','country AS country1'),('`industry(eco0)`','eco AS eco0'),('`inflation(eco0)`','eco AS eco0'),('`percentage(country0)`','country AS country0'),('`percentage(country1)`','country AS country1'),('`popu(country0)`','country AS country0'),('`popu(country1)`','country AS country1'),('`service(eco0)`','eco AS eco0');
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
INSERT INTO `1Nodes_Select_List` VALUES ('`agricul(eco0)`','eco0.agricul AS `agricul(eco0)`'),('`class(country0)`','country0.class AS `class(country0)`'),('`class(country1)`','country1.class AS `class(country1)`'),('`continent(country0)`','country0.continent AS `continent(country0)`'),('`continent(country1)`','country1.continent AS `continent(country1)`'),('`gdp(eco0)`','eco0.gdp AS `gdp(eco0)`'),('`govern(country0)`','country0.govern AS `govern(country0)`'),('`govern(country1)`','country1.govern AS `govern(country1)`'),('`industry(eco0)`','eco0.industry AS `industry(eco0)`'),('`inflation(eco0)`','eco0.inflation AS `inflation(eco0)`'),('`percentage(country0)`','country0.percentage AS `percentage(country0)`'),('`percentage(country1)`','country1.percentage AS `percentage(country1)`'),('`popu(country0)`','country0.popu AS `popu(country0)`'),('`popu(country1)`','country1.popu AS `popu(country1)`'),('`service(eco0)`','eco0.service AS `service(eco0)`');
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
  `Entries` varchar(145) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_PVariables_From_List`
--

LOCK TABLES `ADT_PVariables_From_List` WRITE;
/*!40000 ALTER TABLE `ADT_PVariables_From_List` DISABLE KEYS */;
INSERT INTO `ADT_PVariables_From_List` VALUES ('country0','Mondial_std.country AS country0'),('country1','Mondial_std.country AS country1'),('eco0','Mondial_std.eco AS eco0');
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
INSERT INTO `ADT_PVariables_GroupBy_List` VALUES ('eco0','`agricul(eco0)`'),('country0','`class(country0)`'),('country1','`class(country1)`'),('country0','`continent(country0)`'),('country1','`continent(country1)`'),('eco0','`gdp(eco0)`'),('country0','`govern(country0)`'),('country1','`govern(country1)`'),('eco0','`industry(eco0)`'),('eco0','`inflation(eco0)`'),('country0','`percentage(country0)`'),('country1','`percentage(country1)`'),('country0','`popu(country0)`'),('country1','`popu(country1)`'),('eco0','`service(eco0)`');
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
INSERT INTO `ADT_PVariables_Select_List` VALUES ('country0','count(*) as \"MULT\"'),('country1','count(*) as \"MULT\"'),('eco0','count(*) as \"MULT\"'),('eco0','eco0.agricul AS `agricul(eco0)`'),('country0','country0.class AS `class(country0)`'),('country1','country1.class AS `class(country1)`'),('country0','country0.continent AS `continent(country0)`'),('country1','country1.continent AS `continent(country1)`'),('eco0','eco0.gdp AS `gdp(eco0)`'),('country0','country0.govern AS `govern(country0)`'),('country1','country1.govern AS `govern(country1)`'),('eco0','eco0.industry AS `industry(eco0)`'),('eco0','eco0.inflation AS `inflation(eco0)`'),('country0','country0.percentage AS `percentage(country0)`'),('country1','country1.percentage AS `percentage(country1)`'),('country0','country0.popu AS `popu(country0)`'),('country1','country1.popu AS `popu(country1)`'),('eco0','eco0.service AS `service(eco0)`');
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
INSERT INTO `ADT_RChain_Star_From_List` VALUES ('`a,b,c`','`c`','`a,b_CT`'),('`a,b,c`','`b`','`a,c_CT`'),('`a,b`','`b`','`a_CT`'),('`a,c`','`c`','`a_CT`'),('`a,b,c`','`a`','`b,c_CT`'),('`a,b`','`a`','`b_CT`'),('`b,c`','`c`','`b_CT`'),('`a,c`','`a`','`c_CT`'),('`b,c`','`b`','`c_CT`'),('`a,b`','`b`','`eco0_counts`'),('`a,c`','`c`','`eco0_counts`'),('`a,b`','`a`','`country1_counts`'),('`b,c`','`c`','`country1_counts`'),('`a,c`','`a`','`country0_counts`'),('`b,c`','`b`','`country0_counts`');
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
INSERT INTO `ADT_RChain_Star_Select_List` VALUES ('`a,b,c`','`c`','`class(country0)`'),('`a,b,c`','`b`','`class(country0)`'),('`a,b`','`b`','`class(country0)`'),('`a,c`','`c`','`class(country0)`'),('`a,b,c`','`a`','`class(country0)`'),('`a,b`','`a`','`class(country0)`'),('`b,c`','`c`','`class(country0)`'),('`a,b,c`','`b`','`class(country1)`'),('`a,b,c`','`a`','`class(country1)`'),('`a,c`','`a`','`class(country1)`'),('`b,c`','`b`','`class(country1)`'),('`a,b,c`','`c`','`continent(country0)`'),('`a,b,c`','`b`','`continent(country0)`'),('`a,b`','`b`','`continent(country0)`'),('`a,c`','`c`','`continent(country0)`'),('`a,b,c`','`a`','`continent(country0)`'),('`a,b`','`a`','`continent(country0)`'),('`b,c`','`c`','`continent(country0)`'),('`a,b,c`','`b`','`continent(country1)`'),('`a,b,c`','`a`','`continent(country1)`'),('`a,c`','`a`','`continent(country1)`'),('`b,c`','`b`','`continent(country1)`'),('`a,b,c`','`c`','`govern(country0)`'),('`a,b,c`','`b`','`govern(country0)`'),('`a,b`','`b`','`govern(country0)`'),('`a,c`','`c`','`govern(country0)`'),('`a,b,c`','`a`','`govern(country0)`'),('`a,b`','`a`','`govern(country0)`'),('`b,c`','`c`','`govern(country0)`'),('`a,b,c`','`b`','`govern(country1)`'),('`a,b,c`','`a`','`govern(country1)`'),('`a,c`','`a`','`govern(country1)`'),('`b,c`','`b`','`govern(country1)`'),('`a,b,c`','`c`','`percentage(country0)`'),('`a,b,c`','`b`','`percentage(country0)`'),('`a,b`','`b`','`percentage(country0)`'),('`a,c`','`c`','`percentage(country0)`'),('`a,b,c`','`a`','`percentage(country0)`'),('`a,b`','`a`','`percentage(country0)`'),('`b,c`','`c`','`percentage(country0)`'),('`a,b,c`','`b`','`percentage(country1)`'),('`a,b,c`','`a`','`percentage(country1)`'),('`a,c`','`a`','`percentage(country1)`'),('`b,c`','`b`','`percentage(country1)`'),('`a,b,c`','`c`','`popu(country0)`'),('`a,b,c`','`b`','`popu(country0)`'),('`a,b`','`b`','`popu(country0)`'),('`a,c`','`c`','`popu(country0)`'),('`a,b,c`','`a`','`popu(country0)`'),('`a,b`','`a`','`popu(country0)`'),('`b,c`','`c`','`popu(country0)`'),('`a,b,c`','`b`','`popu(country1)`'),('`a,b,c`','`a`','`popu(country1)`'),('`a,c`','`a`','`popu(country1)`'),('`b,c`','`b`','`popu(country1)`'),('`a,b,c`','`c`','`agricul(eco0)`'),('`a,b,c`','`a`','`agricul(eco0)`'),('`a,b`','`a`','`agricul(eco0)`'),('`b,c`','`c`','`agricul(eco0)`'),('`a,b,c`','`b`','`agricul(eco0)`'),('`a,c`','`a`','`agricul(eco0)`'),('`b,c`','`b`','`agricul(eco0)`'),('`a,b,c`','`c`','`class(country1)`'),('`a,b`','`b`','`class(country1)`'),('`a,c`','`c`','`class(country1)`'),('`a,b,c`','`c`','`continent(country1)`'),('`a,b`','`b`','`continent(country1)`'),('`a,c`','`c`','`continent(country1)`'),('`a,b,c`','`c`','`gdp(eco0)`'),('`a,b,c`','`a`','`gdp(eco0)`'),('`a,b`','`a`','`gdp(eco0)`'),('`b,c`','`c`','`gdp(eco0)`'),('`a,b,c`','`b`','`gdp(eco0)`'),('`a,c`','`a`','`gdp(eco0)`'),('`b,c`','`b`','`gdp(eco0)`'),('`a,b,c`','`c`','`govern(country1)`'),('`a,b`','`b`','`govern(country1)`'),('`a,c`','`c`','`govern(country1)`'),('`a,b,c`','`c`','`industry(eco0)`'),('`a,b,c`','`a`','`industry(eco0)`'),('`a,b`','`a`','`industry(eco0)`'),('`b,c`','`c`','`industry(eco0)`'),('`a,b,c`','`b`','`industry(eco0)`'),('`a,c`','`a`','`industry(eco0)`'),('`b,c`','`b`','`industry(eco0)`'),('`a,b,c`','`c`','`inflation(eco0)`'),('`a,b,c`','`a`','`inflation(eco0)`'),('`a,b`','`a`','`inflation(eco0)`'),('`b,c`','`c`','`inflation(eco0)`'),('`a,b,c`','`b`','`inflation(eco0)`'),('`a,c`','`a`','`inflation(eco0)`'),('`b,c`','`b`','`inflation(eco0)`'),('`a,b,c`','`c`','`percentage(country1)`'),('`a,b`','`b`','`percentage(country1)`'),('`a,c`','`c`','`percentage(country1)`'),('`a,b,c`','`c`','`popu(country1)`'),('`a,b`','`b`','`popu(country1)`'),('`a,c`','`c`','`popu(country1)`'),('`a,b,c`','`c`','`service(eco0)`'),('`a,b,c`','`a`','`service(eco0)`'),('`a,b`','`a`','`service(eco0)`'),('`b,c`','`c`','`service(eco0)`'),('`a,b,c`','`b`','`service(eco0)`'),('`a,c`','`a`','`service(eco0)`'),('`b,c`','`b`','`service(eco0)`'),('`a,b,c`','`c`','`a`'),('`a,b,c`','`b`','`a`'),('`a,b`','`b`','`a`'),('`a,c`','`c`','`a`'),('`a,b,c`','`c`','`b`'),('`a,b,c`','`a`','`b`'),('`a,b`','`a`','`b`'),('`b,c`','`c`','`b`'),('`a,b,c`','`b`','`c`'),('`a,b,c`','`a`','`c`'),('`a,c`','`a`','`c`'),('`b,c`','`b`','`c`'),('`a,b`','`b`','`agricul(eco0)`'),('`a,c`','`c`','`agricul(eco0)`'),('`a,c`','`a`','`class(country0)`'),('`b,c`','`b`','`class(country0)`'),('`a,b`','`a`','`class(country1)`'),('`b,c`','`c`','`class(country1)`'),('`a,c`','`a`','`continent(country0)`'),('`b,c`','`b`','`continent(country0)`'),('`a,b`','`a`','`continent(country1)`'),('`b,c`','`c`','`continent(country1)`'),('`a,b`','`b`','`gdp(eco0)`'),('`a,c`','`c`','`gdp(eco0)`'),('`a,c`','`a`','`govern(country0)`'),('`b,c`','`b`','`govern(country0)`'),('`a,b`','`a`','`govern(country1)`'),('`b,c`','`c`','`govern(country1)`'),('`a,b`','`b`','`industry(eco0)`'),('`a,c`','`c`','`industry(eco0)`'),('`a,b`','`b`','`inflation(eco0)`'),('`a,c`','`c`','`inflation(eco0)`'),('`a,c`','`a`','`percentage(country0)`'),('`b,c`','`b`','`percentage(country0)`'),('`a,b`','`a`','`percentage(country1)`'),('`b,c`','`c`','`percentage(country1)`'),('`a,c`','`a`','`popu(country0)`'),('`b,c`','`b`','`popu(country0)`'),('`a,b`','`a`','`popu(country1)`'),('`b,c`','`c`','`popu(country1)`'),('`a,b`','`b`','`service(eco0)`'),('`a,c`','`c`','`service(eco0)`'),('`b`','`b`','`agricul(eco0)`'),('`c`','`c`','`agricul(eco0)`'),('`a`','`a`','`class(country0)`'),('`b`','`b`','`class(country0)`'),('`c`','`c`','`class(country1)`'),('`a`','`a`','`class(country1)`'),('`a`','`a`','`continent(country0)`'),('`b`','`b`','`continent(country0)`'),('`c`','`c`','`continent(country1)`'),('`a`','`a`','`continent(country1)`'),('`b`','`b`','`gdp(eco0)`'),('`c`','`c`','`gdp(eco0)`'),('`a`','`a`','`govern(country0)`'),('`b`','`b`','`govern(country0)`'),('`c`','`c`','`govern(country1)`'),('`a`','`a`','`govern(country1)`'),('`b`','`b`','`industry(eco0)`'),('`c`','`c`','`industry(eco0)`'),('`b`','`b`','`inflation(eco0)`'),('`c`','`c`','`inflation(eco0)`'),('`a`','`a`','`percentage(country0)`'),('`b`','`b`','`percentage(country0)`'),('`c`','`c`','`percentage(country1)`'),('`a`','`a`','`percentage(country1)`'),('`a`','`a`','`popu(country0)`'),('`b`','`b`','`popu(country0)`'),('`c`','`c`','`popu(country1)`'),('`a`','`a`','`popu(country1)`'),('`b`','`b`','`service(eco0)`'),('`c`','`c`','`service(eco0)`');
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
INSERT INTO `ADT_RNodes_1Nodes_GroupBY_List` VALUES ('`a`','`class(country0)`'),('`b`','`class(country0)`'),('`c`','`class(country1)`'),('`a`','`continent(country0)`'),('`b`','`continent(country0)`'),('`c`','`continent(country1)`'),('`a`','`govern(country0)`'),('`b`','`govern(country0)`'),('`c`','`govern(country1)`'),('`a`','`percentage(country0)`'),('`b`','`percentage(country0)`'),('`c`','`percentage(country1)`'),('`a`','`popu(country0)`'),('`b`','`popu(country0)`'),('`c`','`popu(country1)`'),('`b`','`agricul(eco0)`'),('`c`','`agricul(eco0)`'),('`a`','`class(country1)`'),('`a`','`continent(country1)`'),('`b`','`gdp(eco0)`'),('`c`','`gdp(eco0)`'),('`a`','`govern(country1)`'),('`b`','`industry(eco0)`'),('`c`','`industry(eco0)`'),('`b`','`inflation(eco0)`'),('`c`','`inflation(eco0)`'),('`a`','`percentage(country1)`'),('`a`','`popu(country1)`'),('`b`','`service(eco0)`'),('`c`','`service(eco0)`');
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
INSERT INTO `ADT_RNodes_1Nodes_Select_List` VALUES ('`a`','sum(`a_counts`.`MULT`) as \"MULT\"'),('`b`','sum(`b_counts`.`MULT`) as \"MULT\"'),('`c`','sum(`c_counts`.`MULT`) as \"MULT\"'),('`a`','`class(country0)`'),('`b`','`class(country0)`'),('`c`','`class(country1)`'),('`a`','`continent(country0)`'),('`b`','`continent(country0)`'),('`c`','`continent(country1)`'),('`a`','`govern(country0)`'),('`b`','`govern(country0)`'),('`c`','`govern(country1)`'),('`a`','`percentage(country0)`'),('`b`','`percentage(country0)`'),('`c`','`percentage(country1)`'),('`a`','`popu(country0)`'),('`b`','`popu(country0)`'),('`c`','`popu(country1)`'),('`b`','`agricul(eco0)`'),('`c`','`agricul(eco0)`'),('`a`','`class(country1)`'),('`a`','`continent(country1)`'),('`b`','`gdp(eco0)`'),('`c`','`gdp(eco0)`'),('`a`','`govern(country1)`'),('`b`','`industry(eco0)`'),('`c`','`industry(eco0)`'),('`b`','`inflation(eco0)`'),('`c`','`inflation(eco0)`'),('`a`','`percentage(country1)`'),('`a`','`popu(country1)`'),('`b`','`service(eco0)`'),('`c`','`service(eco0)`');
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
INSERT INTO `ADT_RNodes_False_Select_List` VALUES ('`a`','(`a_star`.MULT-`a_flat`.MULT) AS \"MULT\"'),('`b`','(`b_star`.MULT-`b_flat`.MULT) AS \"MULT\"'),('`c`','(`c_star`.MULT-`c_flat`.MULT) AS \"MULT\"'),('`a`','`a_star`.`class(country0)`'),('`b`','`b_star`.`class(country0)`'),('`c`','`c_star`.`class(country1)`'),('`a`','`a_star`.`continent(country0)`'),('`b`','`b_star`.`continent(country0)`'),('`c`','`c_star`.`continent(country1)`'),('`a`','`a_star`.`govern(country0)`'),('`b`','`b_star`.`govern(country0)`'),('`c`','`c_star`.`govern(country1)`'),('`a`','`a_star`.`percentage(country0)`'),('`b`','`b_star`.`percentage(country0)`'),('`c`','`c_star`.`percentage(country1)`'),('`a`','`a_star`.`popu(country0)`'),('`b`','`b_star`.`popu(country0)`'),('`c`','`c_star`.`popu(country1)`'),('`b`','`b_star`.`agricul(eco0)`'),('`c`','`c_star`.`agricul(eco0)`'),('`a`','`a_star`.`class(country1)`'),('`a`','`a_star`.`continent(country1)`'),('`b`','`b_star`.`gdp(eco0)`'),('`c`','`c_star`.`gdp(eco0)`'),('`a`','`a_star`.`govern(country1)`'),('`b`','`b_star`.`industry(eco0)`'),('`c`','`c_star`.`industry(eco0)`'),('`b`','`b_star`.`inflation(eco0)`'),('`c`','`c_star`.`inflation(eco0)`'),('`a`','`a_star`.`percentage(country1)`'),('`a`','`a_star`.`popu(country1)`'),('`b`','`b_star`.`service(eco0)`'),('`c`','`c_star`.`service(eco0)`');
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
INSERT INTO `ADT_RNodes_False_WHERE_List` VALUES ('`a`','`a_star`.`class(country0)`=`a_flat`.`class(country0)`'),('`b`','`b_star`.`class(country0)`=`b_flat`.`class(country0)`'),('`c`','`c_star`.`class(country1)`=`c_flat`.`class(country1)`'),('`a`','`a_star`.`continent(country0)`=`a_flat`.`continent(country0)`'),('`b`','`b_star`.`continent(country0)`=`b_flat`.`continent(country0)`'),('`c`','`c_star`.`continent(country1)`=`c_flat`.`continent(country1)`'),('`a`','`a_star`.`govern(country0)`=`a_flat`.`govern(country0)`'),('`b`','`b_star`.`govern(country0)`=`b_flat`.`govern(country0)`'),('`c`','`c_star`.`govern(country1)`=`c_flat`.`govern(country1)`'),('`a`','`a_star`.`percentage(country0)`=`a_flat`.`percentage(country0)`'),('`b`','`b_star`.`percentage(country0)`=`b_flat`.`percentage(country0)`'),('`c`','`c_star`.`percentage(country1)`=`c_flat`.`percentage(country1)`'),('`a`','`a_star`.`popu(country0)`=`a_flat`.`popu(country0)`'),('`b`','`b_star`.`popu(country0)`=`b_flat`.`popu(country0)`'),('`c`','`c_star`.`popu(country1)`=`c_flat`.`popu(country1)`'),('`b`','`b_star`.`agricul(eco0)`=`b_flat`.`agricul(eco0)`'),('`c`','`c_star`.`agricul(eco0)`=`c_flat`.`agricul(eco0)`'),('`a`','`a_star`.`class(country1)`=`a_flat`.`class(country1)`'),('`a`','`a_star`.`continent(country1)`=`a_flat`.`continent(country1)`'),('`b`','`b_star`.`gdp(eco0)`=`b_flat`.`gdp(eco0)`'),('`c`','`c_star`.`gdp(eco0)`=`c_flat`.`gdp(eco0)`'),('`a`','`a_star`.`govern(country1)`=`a_flat`.`govern(country1)`'),('`b`','`b_star`.`industry(eco0)`=`b_flat`.`industry(eco0)`'),('`c`','`c_star`.`industry(eco0)`=`c_flat`.`industry(eco0)`'),('`b`','`b_star`.`inflation(eco0)`=`b_flat`.`inflation(eco0)`'),('`c`','`c_star`.`inflation(eco0)`=`c_flat`.`inflation(eco0)`'),('`a`','`a_star`.`percentage(country1)`=`a_flat`.`percentage(country1)`'),('`a`','`a_star`.`popu(country1)`=`a_flat`.`popu(country1)`'),('`b`','`b_star`.`service(eco0)`=`b_flat`.`service(eco0)`'),('`c`','`c_star`.`service(eco0)`=`c_flat`.`service(eco0)`');
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
INSERT INTO `ADT_RNodes_Star_From_List` VALUES ('`a`','`country0_counts`'),('`b`','`country0_counts`'),('`c`','`country1_counts`'),('`a`','`country1_counts`'),('`b`','`eco0_counts`'),('`c`','`eco0_counts`');
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
INSERT INTO `ADT_RNodes_Star_Select_List` VALUES ('`a`','`class(country0)`'),('`b`','`class(country0)`'),('`c`','`class(country1)`'),('`a`','`continent(country0)`'),('`b`','`continent(country0)`'),('`c`','`continent(country1)`'),('`a`','`govern(country0)`'),('`b`','`govern(country0)`'),('`c`','`govern(country1)`'),('`a`','`percentage(country0)`'),('`b`','`percentage(country0)`'),('`c`','`percentage(country1)`'),('`a`','`popu(country0)`'),('`b`','`popu(country0)`'),('`c`','`popu(country1)`'),('`b`','`agricul(eco0)`'),('`c`','`agricul(eco0)`'),('`a`','`class(country1)`'),('`a`','`continent(country1)`'),('`b`','`gdp(eco0)`'),('`c`','`gdp(eco0)`'),('`a`','`govern(country1)`'),('`b`','`industry(eco0)`'),('`c`','`industry(eco0)`'),('`b`','`inflation(eco0)`'),('`c`','`inflation(eco0)`'),('`a`','`percentage(country1)`'),('`a`','`popu(country1)`'),('`b`','`service(eco0)`'),('`c`','`service(eco0)`');
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
INSERT INTO `AttributeColumns` VALUES ('country','class'),('country','continent'),('country','govern'),('country','percentage'),('country','popu'),('eco','agricul'),('eco','gdp'),('eco','industry'),('eco','inflation'),('eco','service');
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
INSERT INTO `Attribute_Value` VALUES ('class','0'),('class','1'),('continent','Africa'),('continent','America'),('continent','Asia'),('continent','aus'),('continent','Europe'),('govern','communist'),('govern','democ'),('govern','dependent'),('govern','monarch'),('govern','republic'),('percentage','0'),('percentage','1'),('percentage','2'),('percentage','3'),('percentage','4'),('popu','0'),('popu','1'),('popu','2'),('popu','3'),('popu','4'),('agricul','0'),('agricul','1'),('agricul','2'),('agricul','3'),('agricul','4'),('gdp','0'),('gdp','1'),('gdp','2'),('gdp','3'),('gdp','4'),('industry','0'),('industry','1'),('industry','2'),('industry','3'),('industry','4'),('inflation','0'),('inflation','1'),('inflation','4'),('service','0'),('service','1'),('service','2'),('service','3'),('service','4');
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
INSERT INTO `EntityTables` VALUES ('country','c1_id'),('eco','eco_id');
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
INSERT INTO `FNodes` VALUES ('`agricul(eco0)`','agricul','1Node',1),('`a`','border','Rnode',1),('`b`','ecoR','Rnode',1),('`class(country0)`','class','1Node',1),('`class(country1)`','class','1Node',0),('`continent(country0)`','continent','1Node',1),('`continent(country1)`','continent','1Node',0),('`c`','ecoR','Rnode',0),('`gdp(eco0)`','gdp','1Node',1),('`govern(country0)`','govern','1Node',1),('`govern(country1)`','govern','1Node',0),('`industry(eco0)`','industry','1Node',1),('`inflation(eco0)`','inflation','1Node',1),('`percentage(country0)`','percentage','1Node',1),('`percentage(country1)`','percentage','1Node',0),('`popu(country0)`','popu','1Node',1),('`popu(country1)`','popu','1Node',0),('`service(eco0)`','service','1Node',1);
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
INSERT INTO `FNodes_pvars` VALUES ('`agricul(eco0)`','eco0'),('`class(country0)`','country0'),('`class(country1)`','country1'),('`continent(country0)`','country0'),('`continent(country1)`','country1'),('`gdp(eco0)`','eco0'),('`govern(country0)`','country0'),('`govern(country1)`','country1'),('`industry(eco0)`','eco0'),('`inflation(eco0)`','eco0'),('`percentage(country0)`','country0'),('`percentage(country1)`','country1'),('`popu(country0)`','country0'),('`popu(country1)`','country1'),('`service(eco0)`','eco0');
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
INSERT INTO `ForeignKeyColumns` VALUES ('border','c1_id','country','c1_id','FK_border_2',1),('border','c1_id_dummy','country','c1_id','FK_border_1',2),('ecoR','c1_id','country','c1_id','FK_ecoR_1',1),('ecoR','eco_id','eco','eco_id','FK_ecoR_2',2);
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
INSERT INTO `ForeignKeys_pvars` VALUES ('border','country','c1_id','country0',0,1),('border','country','c1_id_dummy','country0',0,2),('border','country','c1_id','country1',1,1),('border','country','c1_id_dummy','country1',1,2),('ecoR','country','c1_id','country0',0,1),('ecoR','country','c1_id','country1',1,1),('ecoR','eco','eco_id','eco0',0,2);
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
INSERT INTO `InputColumns` VALUES ('border','c1_id',NULL,NULL,'PRIMARY',1),('border','c1_id_dummy',NULL,NULL,'PRIMARY',2),('country','c1_id',NULL,NULL,'PRIMARY',1),('eco','eco_id',NULL,NULL,'PRIMARY',6),('ecoR','c1_id',NULL,NULL,'PRIMARY',1),('ecoR','eco_id',NULL,NULL,'PRIMARY',2);
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
INSERT INTO `PVariables` VALUES ('country0','country',0),('country1','country',1),('eco0','eco',0);
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
INSERT INTO `PVariables_From_List` VALUES ('country0','country AS country0'),('eco0','eco AS eco0');
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
INSERT INTO `PVariables_GroupBy_List` VALUES ('eco0','`agricul(eco0)`'),('country0','`class(country0)`'),('country1','`class(country1)`'),('country0','`continent(country0)`'),('country1','`continent(country1)`'),('eco0','`gdp(eco0)`'),('country0','`govern(country0)`'),('country1','`govern(country1)`'),('eco0','`industry(eco0)`'),('eco0','`inflation(eco0)`'),('country0','`percentage(country0)`'),('country1','`percentage(country1)`'),('country0','`popu(country0)`'),('country1','`popu(country1)`'),('eco0','`service(eco0)`');
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
INSERT INTO `PVariables_Select_List` VALUES ('country0','count(*) as \"MULT\"'),('country1','count(*) as \"MULT\"'),('eco0','count(*) as \"MULT\"'),('eco0','eco0.agricul AS `agricul(eco0)`'),('country0','country0.class AS `class(country0)`'),('country0','country0.continent AS `continent(country0)`'),('eco0','eco0.gdp AS `gdp(eco0)`'),('country0','country0.govern AS `govern(country0)`'),('eco0','eco0.industry AS `industry(eco0)`'),('eco0','eco0.inflation AS `inflation(eco0)`'),('country0','country0.percentage AS `percentage(country0)`'),('country0','country0.popu AS `popu(country0)`'),('eco0','eco0.service AS `service(eco0)`');
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
INSERT INTO `Path_BN_nodes` VALUES ('`a,b,c`','`agricul(eco0)`'),('`a,b,c`','`a`'),('`a,b,c`','`b`'),('`a,b,c`','`class(country0)`'),('`a,b,c`','`class(country1)`'),('`a,b,c`','`continent(country0)`'),('`a,b,c`','`continent(country1)`'),('`a,b,c`','`c`'),('`a,b,c`','`gdp(eco0)`'),('`a,b,c`','`govern(country0)`'),('`a,b,c`','`govern(country1)`'),('`a,b,c`','`industry(eco0)`'),('`a,b,c`','`inflation(eco0)`'),('`a,b,c`','`percentage(country0)`'),('`a,b,c`','`percentage(country1)`'),('`a,b,c`','`popu(country0)`'),('`a,b,c`','`popu(country1)`'),('`a,b,c`','`service(eco0)`'),('`a,b`','`agricul(eco0)`'),('`a,b`','`a`'),('`a,b`','`b`'),('`a,b`','`class(country0)`'),('`a,b`','`class(country1)`'),('`a,b`','`continent(country0)`'),('`a,b`','`continent(country1)`'),('`a,b`','`gdp(eco0)`'),('`a,b`','`govern(country0)`'),('`a,b`','`govern(country1)`'),('`a,b`','`industry(eco0)`'),('`a,b`','`inflation(eco0)`'),('`a,b`','`percentage(country0)`'),('`a,b`','`percentage(country1)`'),('`a,b`','`popu(country0)`'),('`a,b`','`popu(country1)`'),('`a,b`','`service(eco0)`'),('`a,c`','`agricul(eco0)`'),('`a,c`','`a`'),('`a,c`','`class(country0)`'),('`a,c`','`class(country1)`'),('`a,c`','`continent(country0)`'),('`a,c`','`continent(country1)`'),('`a,c`','`c`'),('`a,c`','`gdp(eco0)`'),('`a,c`','`govern(country0)`'),('`a,c`','`govern(country1)`'),('`a,c`','`industry(eco0)`'),('`a,c`','`inflation(eco0)`'),('`a,c`','`percentage(country0)`'),('`a,c`','`percentage(country1)`'),('`a,c`','`popu(country0)`'),('`a,c`','`popu(country1)`'),('`a,c`','`service(eco0)`'),('`a`','`a`'),('`a`','`class(country0)`'),('`a`','`class(country1)`'),('`a`','`continent(country0)`'),('`a`','`continent(country1)`'),('`a`','`govern(country0)`'),('`a`','`govern(country1)`'),('`a`','`percentage(country0)`'),('`a`','`percentage(country1)`'),('`a`','`popu(country0)`'),('`a`','`popu(country1)`'),('`b,c`','`agricul(eco0)`'),('`b,c`','`b`'),('`b,c`','`class(country0)`'),('`b,c`','`class(country1)`'),('`b,c`','`continent(country0)`'),('`b,c`','`continent(country1)`'),('`b,c`','`c`'),('`b,c`','`gdp(eco0)`'),('`b,c`','`govern(country0)`'),('`b,c`','`govern(country1)`'),('`b,c`','`industry(eco0)`'),('`b,c`','`inflation(eco0)`'),('`b,c`','`percentage(country0)`'),('`b,c`','`percentage(country1)`'),('`b,c`','`popu(country0)`'),('`b,c`','`popu(country1)`'),('`b,c`','`service(eco0)`'),('`b`','`agricul(eco0)`'),('`b`','`b`'),('`b`','`class(country0)`'),('`b`','`continent(country0)`'),('`b`','`gdp(eco0)`'),('`b`','`govern(country0)`'),('`b`','`industry(eco0)`'),('`b`','`inflation(eco0)`'),('`b`','`percentage(country0)`'),('`b`','`popu(country0)`'),('`b`','`service(eco0)`'),('`c`','`agricul(eco0)`'),('`c`','`class(country1)`'),('`c`','`continent(country1)`'),('`c`','`c`'),('`c`','`gdp(eco0)`'),('`c`','`govern(country1)`'),('`c`','`industry(eco0)`'),('`c`','`inflation(eco0)`'),('`c`','`percentage(country1)`'),('`c`','`popu(country1)`'),('`c`','`service(eco0)`');
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
INSERT INTO `RChain_pvars` VALUES ('`a,b,c`','country0'),('`a,b,c`','country1'),('`a,b,c`','eco0'),('`a,b`','country0'),('`a,b`','country1'),('`a,b`','eco0'),('`a,c`','country0'),('`a,c`','country1'),('`a,c`','eco0'),('`a`','country0'),('`a`','country1'),('`b,c`','country0'),('`b,c`','eco0'),('`b,c`','country1'),('`b`','country0'),('`b`','eco0'),('`c`','country1'),('`c`','eco0');
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
INSERT INTO `RNodes` VALUES ('`border(country0,country1)`','border','country0','country1','c1_id','c1_id_dummy',1,'`a`'),('`ecoR(country0,eco0)`','ecoR','country0','eco0','c1_id','eco_id',1,'`b`'),('`ecoR(country1,eco0)`','ecoR','country1','eco0','c1_id','eco_id',0,'`c`');
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
INSERT INTO `RNodes_1Nodes` VALUES ('`a`','border','`class(country0)`','class','country0'),('`b`','ecoR','`class(country0)`','class','country0'),('`c`','ecoR','`class(country1)`','class','country1'),('`a`','border','`continent(country0)`','continent','country0'),('`b`','ecoR','`continent(country0)`','continent','country0'),('`c`','ecoR','`continent(country1)`','continent','country1'),('`a`','border','`govern(country0)`','govern','country0'),('`b`','ecoR','`govern(country0)`','govern','country0'),('`c`','ecoR','`govern(country1)`','govern','country1'),('`a`','border','`percentage(country0)`','percentage','country0'),('`b`','ecoR','`percentage(country0)`','percentage','country0'),('`c`','ecoR','`percentage(country1)`','percentage','country1'),('`a`','border','`popu(country0)`','popu','country0'),('`b`','ecoR','`popu(country0)`','popu','country0'),('`c`','ecoR','`popu(country1)`','popu','country1'),('`b`','ecoR','`agricul(eco0)`','agricul','eco0'),('`c`','ecoR','`agricul(eco0)`','agricul','eco0'),('`a`','border','`class(country1)`','class','country1'),('`a`','border','`continent(country1)`','continent','country1'),('`b`','ecoR','`gdp(eco0)`','gdp','eco0'),('`c`','ecoR','`gdp(eco0)`','gdp','eco0'),('`a`','border','`govern(country1)`','govern','country1'),('`b`','ecoR','`industry(eco0)`','industry','eco0'),('`c`','ecoR','`industry(eco0)`','industry','eco0'),('`b`','ecoR','`inflation(eco0)`','inflation','eco0'),('`c`','ecoR','`inflation(eco0)`','inflation','eco0'),('`a`','border','`percentage(country1)`','percentage','country1'),('`a`','border','`popu(country1)`','popu','country1'),('`b`','ecoR','`service(eco0)`','service','eco0'),('`c`','ecoR','`service(eco0)`','service','eco0');
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
INSERT INTO `RNodes_BN_Nodes` VALUES ('`a`','`class(country0)`',1),('`b`','`class(country0)`',1),('`c`','`class(country1)`',0),('`a`','`continent(country0)`',1),('`b`','`continent(country0)`',1),('`c`','`continent(country1)`',0),('`a`','`govern(country0)`',1),('`b`','`govern(country0)`',1),('`c`','`govern(country1)`',0),('`a`','`percentage(country0)`',1),('`b`','`percentage(country0)`',1),('`c`','`percentage(country1)`',0),('`a`','`popu(country0)`',1),('`b`','`popu(country0)`',1),('`c`','`popu(country1)`',0),('`b`','`agricul(eco0)`',1),('`c`','`agricul(eco0)`',1),('`a`','`class(country1)`',0),('`a`','`continent(country1)`',0),('`b`','`gdp(eco0)`',1),('`c`','`gdp(eco0)`',1),('`a`','`govern(country1)`',0),('`b`','`industry(eco0)`',1),('`c`','`industry(eco0)`',1),('`b`','`inflation(eco0)`',1),('`c`','`inflation(eco0)`',1),('`a`','`percentage(country1)`',0),('`a`','`popu(country1)`',0),('`b`','`service(eco0)`',1),('`c`','`service(eco0)`',1),('`a`','`a`',1),('`b`','`b`',1),('`c`','`c`',0);
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
  `Entries` varchar(145) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RNodes_From_List`
--

LOCK TABLES `RNodes_From_List` WRITE;
/*!40000 ALTER TABLE `RNodes_From_List` DISABLE KEYS */;
INSERT INTO `RNodes_From_List` VALUES ('`a`','Mondial_std.country AS country0'),('`b`','Mondial_std.country AS country0'),('`c`','Mondial_std.country AS country1'),('`a`','Mondial_std.country AS country1'),('`b`','Mondial_std.eco AS eco0'),('`c`','Mondial_std.eco AS eco0'),('`a`','Mondial_std.border AS `a`'),('`b`','Mondial_std.ecoR AS `b`'),('`c`','Mondial_std.ecoR AS `c`'),('`a`','(select \"T\" as `a`) as `temp_a`'),('`b`','(select \"T\" as `b`) as `temp_b`'),('`c`','(select \"T\" as `c`) as `temp_c`');
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
INSERT INTO `RNodes_GroupBy_List` VALUES ('`a`','`class(country0)`'),('`b`','`class(country0)`'),('`c`','`class(country1)`'),('`a`','`continent(country0)`'),('`b`','`continent(country0)`'),('`c`','`continent(country1)`'),('`a`','`govern(country0)`'),('`b`','`govern(country0)`'),('`c`','`govern(country1)`'),('`a`','`percentage(country0)`'),('`b`','`percentage(country0)`'),('`c`','`percentage(country1)`'),('`a`','`popu(country0)`'),('`b`','`popu(country0)`'),('`c`','`popu(country1)`'),('`b`','`agricul(eco0)`'),('`c`','`agricul(eco0)`'),('`a`','`class(country1)`'),('`a`','`continent(country1)`'),('`b`','`gdp(eco0)`'),('`c`','`gdp(eco0)`'),('`a`','`govern(country1)`'),('`b`','`industry(eco0)`'),('`c`','`industry(eco0)`'),('`b`','`inflation(eco0)`'),('`c`','`inflation(eco0)`'),('`a`','`percentage(country1)`'),('`a`','`popu(country1)`'),('`b`','`service(eco0)`'),('`c`','`service(eco0)`'),('`a`','`a`'),('`b`','`b`'),('`c`','`c`');
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
INSERT INTO `RNodes_Select_List` VALUES ('`a`','count(*) as \"MULT\"'),('`b`','count(*) as \"MULT\"'),('`c`','count(*) as \"MULT\"'),('`a`','country0.class AS `class(country0)`'),('`b`','country0.class AS `class(country0)`'),('`c`','country1.class AS `class(country1)`'),('`a`','country0.continent AS `continent(country0)`'),('`b`','country0.continent AS `continent(country0)`'),('`c`','country1.continent AS `continent(country1)`'),('`a`','country0.govern AS `govern(country0)`'),('`b`','country0.govern AS `govern(country0)`'),('`c`','country1.govern AS `govern(country1)`'),('`a`','country0.percentage AS `percentage(country0)`'),('`b`','country0.percentage AS `percentage(country0)`'),('`c`','country1.percentage AS `percentage(country1)`'),('`a`','country0.popu AS `popu(country0)`'),('`b`','country0.popu AS `popu(country0)`'),('`c`','country1.popu AS `popu(country1)`'),('`b`','eco0.agricul AS `agricul(eco0)`'),('`c`','eco0.agricul AS `agricul(eco0)`'),('`a`','country1.class AS `class(country1)`'),('`a`','country1.continent AS `continent(country1)`'),('`b`','eco0.gdp AS `gdp(eco0)`'),('`c`','eco0.gdp AS `gdp(eco0)`'),('`a`','country1.govern AS `govern(country1)`'),('`b`','eco0.industry AS `industry(eco0)`'),('`c`','eco0.industry AS `industry(eco0)`'),('`b`','eco0.inflation AS `inflation(eco0)`'),('`c`','eco0.inflation AS `inflation(eco0)`'),('`a`','country1.percentage AS `percentage(country1)`'),('`a`','country1.popu AS `popu(country1)`'),('`b`','eco0.service AS `service(eco0)`'),('`c`','eco0.service AS `service(eco0)`'),('`a`','`a`'),('`b`','`b`'),('`c`','`c`');
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
INSERT INTO `RNodes_Where_List` VALUES ('`a`','`a`.c1_id = country0.c1_id'),('`b`','`b`.c1_id = country0.c1_id'),('`c`','`c`.c1_id = country1.c1_id'),('`a`','`a`.c1_id_dummy = country1.c1_id'),('`b`','`b`.eco_id = eco0.eco_id'),('`c`','`c`.eco_id = eco0.eco_id');
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
INSERT INTO `RNodes_pvars` VALUES ('`a`','country0','country','c1_id','c1_id'),('`b`','country0','country','c1_id','c1_id'),('`c`','country1','country','c1_id','c1_id'),('`a`','country1','country','c1_id_dummy','c1_id'),('`b`','eco0','eco','eco_id','eco_id'),('`c`','eco0','eco','eco_id','eco_id');
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
INSERT INTO `RelationTables` VALUES ('border',1,0),('ecoR',0,0);
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
INSERT INTO `lattice_mapping` VALUES ('`border(country0,country1),ecoR(country0,eco0),ecoR(country1,eco0)`','`a,b,c`'),('`border(country0,country1),ecoR(country0,eco0)`','`a,b`'),('`border(country0,country1),ecoR(country1,eco0)`','`a,c`'),('`border(country0,country1)`','`a`'),('`ecoR(country0,eco0),ecoR(country1,eco0)`','`b,c`'),('`ecoR(country0,eco0)`','`b`'),('`ecoR(country1,eco0)`','`c`');
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

-- Dump completed on 2014-09-30 14:55:00
