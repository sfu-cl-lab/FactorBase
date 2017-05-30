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
-- Table structure for table `Scores`
--

DROP TABLE IF EXISTS `Scores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Scores` (
  `Fid` varchar(256) NOT NULL,
  `LogLikelihood` float default NULL,
  `Parameters` bigint(20) default NULL,
  `SampleSize` bigint(20) default NULL,
  `BIC` float default NULL,
  `AIC` float default NULL,
  `BICNormal` float default NULL,
  `AICNormal` float default NULL,
  PRIMARY KEY  (`Fid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Scores`
--

LOCK TABLES `Scores` WRITE;
/*!40000 ALTER TABLE `Scores` DISABLE KEYS */;
INSERT INTO `Scores` VALUES ('`action(item20)`',-10.3445,6,1582762,-106.337,-16.3445,-6.71845e-05,-1.03266e-05),('`Age(User0)`',-49.1712,24,1582762,-440.935,-73.1712,-0.000278586,-4.62301e-05),('`a`',-3.03938,1,1582762,-20.3535,-4.03938,-1.28595e-05,-2.55211e-06),('`drama(item20)`',-35.8583,24,1582762,-414.309,-59.8583,-0.000261763,-3.78189e-05),('`Gender(User0)`',-19.9039,12,1582762,-211.104,-31.9039,-0.000133377,-2.01571e-05),('`horror(item20)`',-2.9622,1,1582762,-20.1991,-3.9622,-1.27619e-05,-2.50334e-06),('`rating(User0,item20)`',-17.4886,16,1582762,-263.372,-33.4886,-0.0001664,-2.11583e-05);
/*!40000 ALTER TABLE `Scores` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:15
