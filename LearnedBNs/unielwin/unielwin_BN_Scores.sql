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
INSERT INTO `Scores` VALUES ('`a`',-4.69465,2,2280,-24.8532,-6.69465,-0.0109005,-0.00293625),('`b`',-5.08025,4,2280,-41.0882,-9.08025,-0.0180212,-0.00398257),('`capability(prof0,student0)`',-8.157,8,2280,-78.1695,-16.157,-0.0342848,-0.00708641),('`diff(course0)`',-6.80014,5,2280,-52.2599,-11.8001,-0.022921,-0.0051755),('`grade(course0,student0)`',-9.29571,18,2280,-157.766,-27.2957,-0.0691957,-0.0119718),('`intelligence(student0)`',-6.9487,4,2280,-44.8251,-10.9487,-0.0196601,-0.00480206),('`popularity(prof0)`',-4.42901,8,2280,-70.7135,-12.429,-0.0310147,-0.00545132),('`ranking(student0)`',-12.9907,24,2280,-211.548,-36.9907,-0.0927841,-0.016224),('`rating(course0)`',-2.98118,2,2280,-21.4262,-4.98118,-0.00939747,-0.00218473),('`salary(prof0,student0)`',-6.5717,12,2280,-105.927,-18.5717,-0.046459,-0.00814548),('`sat(course0,student0)`',-7.65207,10,2280,-92.6234,-17.6521,-0.0406243,-0.00774213),('`teachingability(prof0)`',-1.38629,1,2280,-10.5045,-2.38629,-0.00460725,-0.00104662);
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

-- Dump completed on 2013-08-30 15:08:10
