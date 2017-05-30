CREATE DATABASE  IF NOT EXISTS `MovieLens_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `MovieLens_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: MovieLens_std_BN
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
-- Table structure for table `drama(item20)_CP`
--

DROP TABLE IF EXISTS `drama(item20)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drama(item20)_CP` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(4) default NULL,
  `action(item20)` varchar(4) default NULL,
  `horror(item20)` varchar(4) default NULL,
  `rating(User0,item20)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drama(item20)_CP`
--

LOCK TABLES `drama(item20)_CP` WRITE;
/*!40000 ALTER TABLE `drama(item20)_CP` DISABLE KEYS */;
INSERT INTO `drama(item20)_CP` VALUES ('1971','0','0','0','1',3201,0.615745,-0.484922),('3437','0','0','0','2',6017,0.571215,-0.55999),('8036','0','0','0','3',15099,0.532221,-0.630696),('9277','0','0','0','4',19631,0.472569,-0.749572),('5343','0','0','0','5',12139,0.440152,-0.820635),('609934','0','0','0','N/A',1216145,0.501531,-0.69009),('272','0','0','1','1',300,0.906667,-0.09798),('454','0','0','1','2',509,0.891945,-0.114351),('825','0','0','1','3',945,0.873016,-0.135801),('882','0','0','1','4',945,0.933333,-0.0689932),('443','0','0','1','5',474,0.934599,-0.0676377),('66758','0','0','1','N/A',71166,0.938060,-0.0639414),('1023','0','1','0','1',1132,0.903710,-0.101247),('2154','0','1','0','2',2475,0.870303,-0.138914),('4808','0','1','0','3',5629,0.854148,-0.157651),('5150','0','1','0','4',6421,0.802056,-0.220577),('2803','0','1','0','5',3857,0.726731,-0.319199),('164734','0','1','0','N/A',204444,0.805766,-0.215962),('70','0','1','1','1',70,1.000000,0),('151','0','1','1','2',151,1.000000,0),('251','0','1','1','3',251,1.000000,0),('335','0','1','1','4',335,1.000000,0),('198','0','1','1','5',198,1.000000,0),('11228','0','1','1','N/A',11228,1.000000,0),('1230','1','0','0','1',3201,0.384255,-0.956449),('2580','1','0','0','2',6017,0.428785,-0.8468),('7063','1','0','0','3',15099,0.467779,-0.759759),('10354','1','0','0','4',19631,0.527431,-0.639737),('6796','1','0','0','5',12139,0.559848,-0.58009),('606211','1','0','0','N/A',1216145,0.498469,-0.696214),('28','1','0','1','1',300,0.093333,-2.37158),('55','1','0','1','2',509,0.108055,-2.22511),('120','1','0','1','3',945,0.126984,-2.06369),('63','1','0','1','4',945,0.066667,-2.70805),('31','1','0','1','5',474,0.065401,-2.72722),('4408','1','0','1','N/A',71166,0.061940,-2.78159),('109','1','1','0','1',1132,0.096290,-2.34039),('321','1','1','0','2',2475,0.129697,-2.04255),('821','1','1','0','3',5629,0.145852,-1.92516),('1271','1','1','0','4',6421,0.197944,-1.61977),('1054','1','1','0','5',3857,0.273269,-1.2973),('39710','1','1','0','N/A',204444,0.194234,-1.63869);
/*!40000 ALTER TABLE `drama(item20)_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:18
