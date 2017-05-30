CREATE DATABASE  IF NOT EXISTS `Cont_PLG_TM_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Cont_PLG_TM_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Cont_PLG_TM_BN
-- ------------------------------------------------------
-- Server version	5.5.32

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
-- Table structure for table `ADT_RChain_Star_Where_List`
--

DROP TABLE IF EXISTS `ADT_RChain_Star_Where_List`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADT_RChain_Star_Where_List` (
  `rchain` varchar(256) NOT NULL DEFAULT '',
  `rnid` varchar(256) DEFAULT NULL,
  `Entries` varchar(262) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADT_RChain_Star_Where_List`
--

LOCK TABLES `ADT_RChain_Star_Where_List` WRITE;
/*!40000 ALTER TABLE `ADT_RChain_Star_Where_List` DISABLE KEYS */;
INSERT INTO `ADT_RChain_Star_Where_List` VALUES ('`a,b,c,d,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d,e,f`','`d`','`e` = \"T\"'),('`a,b,c,d,e,f`','`d`','`f` = \"T\"'),('`a,b,c,d,e`','`d`','`e` = \"T\"'),('`a,b,c,d,f`','`d`','`f` = \"T\"'),('`a,b,c,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d,e,f`','`c`','`d` = \"T\"'),('`a,b,c,d,e,f`','`c`','`e` = \"T\"'),('`a,b,c,d,e,f`','`c`','`f` = \"T\"'),('`a,b,c,d,e`','`c`','`d` = \"T\"'),('`a,b,c,d,e`','`c`','`e` = \"T\"'),('`a,b,c,d,f`','`c`','`d` = \"T\"'),('`a,b,c,d,f`','`c`','`f` = \"T\"'),('`a,b,d,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d`','`c`','`d` = \"T\"'),('`a,b,c,e,f`','`c`','`e` = \"T\"'),('`a,b,c,e,f`','`c`','`f` = \"T\"'),('`a,b,d,e,f`','`d`','`e` = \"T\"'),('`a,b,d,e,f`','`d`','`f` = \"T\"'),('`a,b,c,e`','`c`','`e` = \"T\"'),('`a,b,d,e`','`d`','`e` = \"T\"'),('`a,b,c,f`','`c`','`f` = \"T\"'),('`a,b,d,f`','`d`','`f` = \"T\"'),('`a,b,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d,e,f`','`b`','`c` = \"T\"'),('`a,b,c,d,e,f`','`b`','`d` = \"T\"'),('`a,b,c,d,e,f`','`b`','`e` = \"T\"'),('`a,b,c,d,e,f`','`b`','`f` = \"T\"'),('`a,b,c,d,e`','`b`','`c` = \"T\"'),('`a,b,c,d,e`','`b`','`d` = \"T\"'),('`a,b,c,d,e`','`b`','`e` = \"T\"'),('`a,b,c,d,f`','`b`','`c` = \"T\"'),('`a,b,c,d,f`','`b`','`d` = \"T\"'),('`a,b,c,d,f`','`b`','`f` = \"T\"'),('`a,c,d,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d`','`b`','`c` = \"T\"'),('`a,b,c,d`','`b`','`d` = \"T\"'),('`a,b,c,e`','`b`','`c` = \"T\"'),('`a,b,c,e`','`b`','`e` = \"T\"'),('`a,c,d,e`','`d`','`e` = \"T\"'),('`a,b,c`','`b`','`c` = \"T\"'),('`a,b,d,e,f`','`b`','`d` = \"T\"'),('`a,b,d,e,f`','`b`','`e` = \"T\"'),('`a,b,d,e,f`','`b`','`f` = \"T\"'),('`a,c,d,e,f`','`c`','`d` = \"T\"'),('`a,c,d,e,f`','`c`','`e` = \"T\"'),('`a,c,d,e,f`','`c`','`f` = \"T\"'),('`a,b,d,e`','`b`','`d` = \"T\"'),('`a,b,d,e`','`b`','`e` = \"T\"'),('`a,c,d,e`','`c`','`d` = \"T\"'),('`a,c,d,e`','`c`','`e` = \"T\"'),('`a,b,d,f`','`b`','`d` = \"T\"'),('`a,b,d,f`','`b`','`f` = \"T\"'),('`a,c,d,f`','`c`','`d` = \"T\"'),('`a,c,d,f`','`c`','`f` = \"T\"'),('`a,d,e,f`','`e`','`f` = \"T\"'),('`a,b,d`','`b`','`d` = \"T\"'),('`a,c,d`','`c`','`d` = \"T\"'),('`a,b,e`','`b`','`e` = \"T\"'),('`a,c,e`','`c`','`e` = \"T\"'),('`a,d,e`','`d`','`e` = \"T\"'),('`a,b,c,d,e,f`','`a`','`b` = \"T\"'),('`a,b,c,d,e,f`','`a`','`c` = \"T\"'),('`a,b,c,d,e,f`','`a`','`d` = \"T\"'),('`a,b,c,d,e,f`','`a`','`e` = \"T\"'),('`a,b,c,d,e,f`','`a`','`f` = \"T\"'),('`a,b,c,d,e`','`a`','`b` = \"T\"'),('`a,b,c,d,e`','`a`','`c` = \"T\"'),('`a,b,c,d,e`','`a`','`d` = \"T\"'),('`a,b,c,d,e`','`a`','`e` = \"T\"'),('`a,b,c,d,f`','`a`','`b` = \"T\"'),('`a,b,c,d,f`','`a`','`c` = \"T\"'),('`a,b,c,d,f`','`a`','`d` = \"T\"'),('`a,b,c,d,f`','`a`','`f` = \"T\"'),('`b,c,d,e,f`','`e`','`f` = \"T\"'),('`a,b,c,d`','`a`','`b` = \"T\"'),('`a,b,c,d`','`a`','`c` = \"T\"'),('`a,b,c,d`','`a`','`d` = \"T\"'),('`a,b,c,e,f`','`a`','`b` = \"T\"'),('`a,b,c,e,f`','`a`','`c` = \"T\"'),('`a,b,c,e,f`','`a`','`e` = \"T\"'),('`a,b,c,e,f`','`a`','`f` = \"T\"'),('`b,c,d,e,f`','`d`','`e` = \"T\"'),('`b,c,d,e,f`','`d`','`f` = \"T\"'),('`a,b,c,e`','`a`','`b` = \"T\"'),('`a,b,c,e`','`a`','`c` = \"T\"'),('`a,b,c,e`','`a`','`e` = \"T\"'),('`b,c,d,e`','`d`','`e` = \"T\"'),('`a,b,c,f`','`a`','`b` = \"T\"'),('`a,b,c,f`','`a`','`c` = \"T\"'),('`a,b,c,f`','`a`','`f` = \"T\"'),('`b,c,d,f`','`d`','`f` = \"T\"'),('`b,c,e,f`','`e`','`f` = \"T\"'),('`a,b,c`','`a`','`b` = \"T\"'),('`a,b,c`','`a`','`c` = \"T\"'),('`a,b,d,f`','`a`','`b` = \"T\"'),('`a,b,d,f`','`a`','`d` = \"T\"'),('`a,b,d,f`','`a`','`f` = \"T\"'),('`b,c,d,f`','`c`','`d` = \"T\"'),('`b,c,d,f`','`c`','`f` = \"T\"'),('`a,b,d`','`a`','`b` = \"T\"'),('`a,b,d`','`a`','`d` = \"T\"'),('`b,c,d`','`c`','`d` = \"T\"'),('`a,b,f`','`a`','`b` = \"T\"'),('`a,b,f`','`a`','`f` = \"T\"'),('`b,c,f`','`c`','`f` = \"T\"'),('`b,d,f`','`d`','`f` = \"T\"'),('`a,b`','`a`','`b` = \"T\"'),('`a,c,d,e,f`','`a`','`c` = \"T\"'),('`a,c,d,e,f`','`a`','`d` = \"T\"'),('`a,c,d,e,f`','`a`','`e` = \"T\"'),('`a,c,d,e,f`','`a`','`f` = \"T\"'),('`b,c,d,e,f`','`b`','`c` = \"T\"'),('`b,c,d,e,f`','`b`','`d` = \"T\"'),('`b,c,d,e,f`','`b`','`e` = \"T\"'),('`b,c,d,e,f`','`b`','`f` = \"T\"'),('`a,c,d,e`','`a`','`c` = \"T\"'),('`a,c,d,e`','`a`','`d` = \"T\"'),('`a,c,d,e`','`a`','`e` = \"T\"'),('`b,c,d,e`','`b`','`c` = \"T\"'),('`b,c,d,e`','`b`','`d` = \"T\"'),('`b,c,d,e`','`b`','`e` = \"T\"'),('`a,c,d,f`','`a`','`c` = \"T\"'),('`a,c,d,f`','`a`','`d` = \"T\"'),('`a,c,d,f`','`a`','`f` = \"T\"'),('`b,c,d,f`','`b`','`c` = \"T\"'),('`b,c,d,f`','`b`','`d` = \"T\"'),('`b,c,d,f`','`b`','`f` = \"T\"'),('`c,d,e,f`','`e`','`f` = \"T\"'),('`a,c,d`','`a`','`c` = \"T\"'),('`a,c,d`','`a`','`d` = \"T\"'),('`b,c,d`','`b`','`c` = \"T\"'),('`b,c,d`','`b`','`d` = \"T\"'),('`a,c,e`','`a`','`c` = \"T\"'),('`a,c,e`','`a`','`e` = \"T\"'),('`b,c,e`','`b`','`c` = \"T\"'),('`b,c,e`','`b`','`e` = \"T\"'),('`c,d,e`','`d`','`e` = \"T\"'),('`a,c`','`a`','`c` = \"T\"'),('`b,c`','`b`','`c` = \"T\"'),('`a,d,f`','`a`','`d` = \"T\"'),('`a,d,f`','`a`','`f` = \"T\"'),('`b,d,f`','`b`','`d` = \"T\"'),('`b,d,f`','`b`','`f` = \"T\"'),('`c,d,f`','`c`','`d` = \"T\"'),('`c,d,f`','`c`','`f` = \"T\"'),('`a,d`','`a`','`d` = \"T\"'),('`b,d`','`b`','`d` = \"T\"'),('`c,d`','`c`','`d` = \"T\"'),('`a,e`','`a`','`e` = \"T\"'),('`c,e`','`c`','`e` = \"T\"'),('`b,f`','`b`','`f` = \"T\"'),('`d,f`','`d`','`f` = \"T\"');
/*!40000 ALTER TABLE `ADT_RChain_Star_Where_List` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:55:52
