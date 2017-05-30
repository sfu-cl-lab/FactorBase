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
-- Table structure for table `drama(item20)_CP_smoothed`
--

DROP TABLE IF EXISTS `drama(item20)_CP_smoothed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drama(item20)_CP_smoothed` (
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
-- Dumping data for table `drama(item20)_CP_smoothed`
--

LOCK TABLES `drama(item20)_CP_smoothed` WRITE;
/*!40000 ALTER TABLE `drama(item20)_CP_smoothed` DISABLE KEYS */;
INSERT INTO `drama(item20)_CP_smoothed` VALUES ('1972','0','0','0','1',3203,0.615673,NULL),('3438','0','0','0','2',6019,0.571191,NULL),('8037','0','0','0','3',15101,0.532216,NULL),('9278','0','0','0','4',19633,0.472572,NULL),('5344','0','0','0','5',12141,0.440161,NULL),('609935','0','0','0','N/A',1216147,0.501531,NULL),('273','0','0','1','1',302,0.903974,NULL),('455','0','0','1','2',511,0.890411,NULL),('826','0','0','1','3',947,0.872228,NULL),('883','0','0','1','4',947,0.932418,NULL),('444','0','0','1','5',476,0.932773,NULL),('66759','0','0','1','N/A',71168,0.938048,NULL),('1024','0','1','0','1',1134,0.902998,NULL),('2155','0','1','0','2',2477,0.870004,NULL),('4809','0','1','0','3',5631,0.854022,NULL),('5151','0','1','0','4',6423,0.801962,NULL),('2804','0','1','0','5',3859,0.726613,NULL),('164735','0','1','0','N/A',204446,0.805763,NULL),('71','0','1','1','1',72,0.986111,NULL),('152','0','1','1','2',153,0.993464,NULL),('252','0','1','1','3',253,0.996047,NULL),('336','0','1','1','4',337,0.997033,NULL),('199','0','1','1','5',200,0.995000,NULL),('11229','0','1','1','N/A',11230,0.999911,NULL),('1231','1','0','0','1',3203,0.384327,NULL),('2581','1','0','0','2',6019,0.428809,NULL),('7064','1','0','0','3',15101,0.467784,NULL),('10355','1','0','0','4',19633,0.527428,NULL),('6797','1','0','0','5',12141,0.559839,NULL),('606212','1','0','0','N/A',1216147,0.498469,NULL),('29','1','0','1','1',302,0.096026,NULL),('56','1','0','1','2',511,0.109589,NULL),('121','1','0','1','3',947,0.127772,NULL),('64','1','0','1','4',947,0.067582,NULL),('32','1','0','1','5',476,0.067227,NULL),('4409','1','0','1','N/A',71168,0.061952,NULL),('110','1','1','0','1',1134,0.097002,NULL),('322','1','1','0','2',2477,0.129996,NULL),('822','1','1','0','3',5631,0.145978,NULL),('1272','1','1','0','4',6423,0.198038,NULL),('1055','1','1','0','5',3859,0.273387,NULL),('39711','1','1','0','N/A',204446,0.194237,NULL),('1','1','1','1','1',72,0.013889,NULL),('1','1','1','1','2',153,0.006536,NULL),('1','1','1','1','3',253,0.003953,NULL),('1','1','1','1','4',337,0.002967,NULL),('1','1','1','1','5',200,0.005000,NULL),('1','1','1','1','N/A',11230,0.000089,NULL);
/*!40000 ALTER TABLE `drama(item20)_CP_smoothed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 15:06:19
