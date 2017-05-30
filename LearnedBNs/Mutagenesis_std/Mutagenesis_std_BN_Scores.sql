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
INSERT INTO `Scores` VALUES ('`atype(Atom0)`',-6.37156,28,905205,-396.789,-34.3716,-0.000438341,-3.7971e-05),('`a`',-90.1812,42,905205,-756.431,-132.181,-0.000835646,-0.000146024),('`btype(Mole0,Atom0)`',-46.3101,420,905205,-5853.31,-466.31,-0.00646628,-0.000515143),('`b`',0,28,905205,-384.046,-28,-0.000424264,-3.09322e-05),('`charge(Atom0)`',-11.4777,7,905205,-118.967,-18.4777,-0.000131425,-2.04127e-05),('`elem(Atom0)`',-28.8704,6,905205,-140.036,-34.8704,-0.000154701,-3.85221e-05),('`ind1(Mole0)`',-4.60804,2,905205,-36.6479,-6.60804,-4.04857e-05,-7.30004e-06),('`inda(Mole0)`',-5.93492,140,905205,-1932.1,-145.935,-0.00213443,-0.000161218),('`label(Mole0)`',-1.49356,1,905205,-16.703,-2.49356,-1.84522e-05,-2.75469e-06),('`logp(Mole0)`',-41.1125,36,905205,-575.998,-77.1125,-0.000636318,-8.51879e-05),('`lumo(Mole0)`',-30.5951,32,905205,-500.1,-62.5951,-0.000552471,-6.91502e-05);
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

-- Dump completed on 2013-08-30 15:07:14
