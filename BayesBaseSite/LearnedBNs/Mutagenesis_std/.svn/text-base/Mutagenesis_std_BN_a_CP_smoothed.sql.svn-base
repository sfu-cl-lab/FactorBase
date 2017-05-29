CREATE DATABASE  IF NOT EXISTS `Mutagenesis_std_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Mutagenesis_std_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: kripke.cs.sfu.ca    Database: Mutagenesis_std_BN
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
-- Table structure for table `a_CP_smoothed`
--

DROP TABLE IF EXISTS `a_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `a_CP_smoothed` (
  `MULT` decimal(41,0) default NULL,
  `ChildValue` varchar(5) default NULL,
  `atype(Atom0)` varchar(45) default NULL,
  `elem(Atom0)` char(1) default NULL,
  `ind1(Mole0)` varchar(45) default NULL,
  `ParentSum` bigint(20) default NULL,
  `CP` float(7,6) default NULL,
  `likelihood` float default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `a_CP_smoothed`
--

LOCK TABLES `a_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `a_CP_smoothed` DISABLE KEYS */;
INSERT INTO `a_CP_smoothed` VALUES ('170','F','0','b','0',172,0.988372,NULL),('200','F','0','b','1',202,0.990099,NULL),('8899','F','0','c','0',8927,0.996863,NULL),('10426','F','0','c','1',10502,0.992763,NULL),('129224','F','0','h','0',129797,0.995585,NULL),('151778','F','0','h','1',152702,0.993949,NULL),('147512','F','1','c','0',148157,0.995647,NULL),('173249','F','1','c','1',174302,0.993959,NULL),('48267','F','2','c','0',48367,0.997932,NULL),('56451','F','2','c','1',56902,0.992074,NULL),('1100','F','2','f','0',1107,0.993677,NULL),('1296','F','2','f','1',1302,0.995392,NULL),('86','F','2','i','0',87,0.988506,NULL),('100','F','2','i','1',102,0.980392,NULL),('29156','F','2','n','0',29327,0.994169,NULL),('34347','F','2','n','1',34502,0.995508,NULL),('49760','F','2','o','0',49982,0.995558,NULL),('58545','F','2','o','1',58802,0.995629,NULL),('2','T','0','b','0',172,0.011628,NULL),('2','T','0','b','1',202,0.009901,NULL),('28','T','0','c','0',8927,0.003137,NULL),('76','T','0','c','1',10502,0.007237,NULL),('573','T','0','h','0',129797,0.004415,NULL),('924','T','0','h','1',152702,0.006051,NULL),('645','T','1','c','0',148157,0.004353,NULL),('1053','T','1','c','1',174302,0.006041,NULL),('100','T','2','c','0',48367,0.002068,NULL),('451','T','2','c','1',56902,0.007926,NULL),('7','T','2','f','0',1107,0.006323,NULL),('6','T','2','f','1',1302,0.004608,NULL),('2','T','2','i','1',102,0.019608,NULL),('171','T','2','n','0',29327,0.005831,NULL),('155','T','2','n','1',34502,0.004492,NULL),('222','T','2','o','0',49982,0.004442,NULL),('257','T','2','o','1',58802,0.004371,NULL),('1','F','0','f','0',2,0.500000,NULL),('1','F','0','f','1',2,0.500000,NULL),('1','F','0','i','0',2,0.500000,NULL),('1','F','0','i','1',2,0.500000,NULL),('1','F','0','n','0',2,0.500000,NULL),('1','F','0','n','1',2,0.500000,NULL),('1','F','0','o','0',2,0.500000,NULL),('1','F','0','o','1',2,0.500000,NULL),('1','F','1','b','0',2,0.500000,NULL),('1','F','1','b','1',2,0.500000,NULL),('1','F','1','h','0',2,0.500000,NULL),('1','F','1','h','1',2,0.500000,NULL),('1','F','1','f','0',2,0.500000,NULL),('1','F','1','f','1',2,0.500000,NULL),('1','F','1','i','0',2,0.500000,NULL),('1','F','1','i','1',2,0.500000,NULL),('1','F','1','n','0',2,0.500000,NULL),('1','F','1','n','1',2,0.500000,NULL),('1','F','1','o','0',2,0.500000,NULL),('1','F','1','o','1',2,0.500000,NULL),('1','F','2','b','0',2,0.500000,NULL),('1','F','2','b','1',2,0.500000,NULL),('1','F','2','h','0',2,0.500000,NULL),('1','F','2','h','1',2,0.500000,NULL),('1','T','0','f','0',2,0.500000,NULL),('1','T','0','f','1',2,0.500000,NULL),('1','T','0','i','0',2,0.500000,NULL),('1','T','0','i','1',2,0.500000,NULL),('1','T','0','n','0',2,0.500000,NULL),('1','T','0','n','1',2,0.500000,NULL),('1','T','0','o','0',2,0.500000,NULL),('1','T','0','o','1',2,0.500000,NULL),('1','T','1','b','0',2,0.500000,NULL),('1','T','1','b','1',2,0.500000,NULL),('1','T','1','h','0',2,0.500000,NULL),('1','T','1','h','1',2,0.500000,NULL),('1','T','1','f','0',2,0.500000,NULL),('1','T','1','f','1',2,0.500000,NULL),('1','T','1','i','0',2,0.500000,NULL),('1','T','1','i','1',2,0.500000,NULL),('1','T','1','n','0',2,0.500000,NULL),('1','T','1','n','1',2,0.500000,NULL),('1','T','1','o','0',2,0.500000,NULL),('1','T','1','o','1',2,0.500000,NULL),('1','T','2','b','0',2,0.500000,NULL),('1','T','2','b','1',2,0.500000,NULL),('1','T','2','h','0',2,0.500000,NULL),('1','T','2','h','1',2,0.500000,NULL),('1','T','2','i','0',87,0.011494,NULL);
/*!40000 ALTER TABLE `a_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:07:16
