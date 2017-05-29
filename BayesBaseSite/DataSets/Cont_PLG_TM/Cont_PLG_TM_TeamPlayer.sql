CREATE DATABASE  IF NOT EXISTS `Cont_PLG_TM` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Cont_PLG_TM`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Cont_PLG_TM
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
-- Table structure for table `TeamPlayer`
--

DROP TABLE IF EXISTS `TeamPlayer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TeamPlayer` (
  `TeamID` varchar(40) NOT NULL DEFAULT '',
  `PlayerID` varchar(40) NOT NULL,
  PRIMARY KEY (`TeamID`,`PlayerID`),
  KEY `fk_TeamPlayer_1_idx` (`PlayerID`),
  KEY `fk_TeamPlayer_2_idx` (`TeamID`),
  CONSTRAINT `fk_TeamPlayer_1` FOREIGN KEY (`PlayerID`) REFERENCES `Players` (`PlayerID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_TeamPlayer_2` FOREIGN KEY (`TeamID`) REFERENCES `Teams` (`TeamID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TeamPlayer`
--

LOCK TABLES `TeamPlayer` WRITE;
/*!40000 ALTER TABLE `TeamPlayer` DISABLE KEYS */;
INSERT INTO `TeamPlayer` VALUES ('30','10089'),('5','103903'),('14','103955'),('8','10425'),('7','10451'),('54','10454'),('11','10466'),('54','105086'),('39','105190'),('30','105322'),('39','10561'),('52','1059'),('1','106603'),('7','10738'),('54','107853'),('5','10954'),('45','11078'),('56','11248'),('8','11278'),('56','1131'),('8','11334'),('4','11378'),('7','114042'),('35','11467'),('110','11721'),('54','11735'),('80','11829'),('80','11883'),('111','11911'),('54','1195'),('14','12002'),('52','1212'),('110','12150'),('52','1216'),('3','12297'),('8','12303'),('14','1231'),('110','12413'),('43','12450'),('54','1256'),('52','12674'),('6','12679'),('5','1274'),('11','12745'),('35','12765'),('30','12780'),('110','12813'),('1','12882'),('1','13017'),('30','1307'),('3','13227'),('5','13239'),('14','13308'),('7','13310'),('3','13439'),('30','1344'),('5','13524'),('1','14075'),('45','1409'),('6','1411'),('80','14166'),('52','1420'),('35','14278'),('35','14279'),('1','14295'),('8','14402'),('111','14469'),('110','14489'),('43','14664'),('30','14668'),('4','14775'),('45','14898'),('56','14919'),('39','14947'),('1','14965'),('56','15073'),('43','15076'),('6','15109'),('80','15114'),('43','15157'),('30','15188'),('45','15201'),('45','15237'),('54','15284'),('11','15337'),('30','15395'),('80','15398'),('7','15405'),('80','15428'),('111','15498'),('3','15675'),('43','15749'),('56','15864'),('30','1587'),('54','15903'),('3','15943'),('39','15944'),('54','15968'),('4','16005'),('111','16045'),('5','16058'),('30','1615'),('3','1619'),('35','16210'),('111','16234'),('39','16236'),('43','1632'),('110','1640'),('8','16734'),('110','1679'),('54','16854'),('7','1710'),('3','17127'),('8','1718'),('43','17336'),('6','17349'),('7','17468'),('43','17476'),('6','17500'),('7','1764'),('7','1765'),('39','17687'),('8','17694'),('45','17812'),('11','17891'),('1','1795'),('80','17955'),('4','17974'),('56','17997'),('35','18008'),('5','1801'),('6','1803'),('14','1809'),('54','1812'),('14','1814'),('45','18144'),('35','18151'),('11','1821'),('110','18215'),('7','1822'),('54','1827'),('52','1840'),('56','1841'),('30','18428'),('35','18430'),('52','18586'),('8','18658'),('54','1869'),('7','18737'),('4','18753'),('110','18804'),('39','18818'),('39','18832'),('4','18846'),('1','18892'),('39','18953'),('39','19008'),('56','19057'),('39','19084'),('45','19124'),('35','19151'),('80','19159'),('3','19160'),('5','19188'),('52','19196'),('52','19197'),('45','19236'),('35','19272'),('45','19321'),('6','1934'),('45','19341'),('80','1936'),('56','1940'),('110','1945'),('80','1950'),('111','19556'),('39','19557'),('45','19569'),('5','19602'),('52','19645'),('80','19688'),('110','19714'),('52','19740'),('39','1989'),('45','19916'),('30','19930'),('52','19946'),('30','19958'),('43','19959'),('30','2004'),('39','20066'),('35','20141'),('52','2019'),('14','20208'),('54','20226'),('6','20298'),('43','20312'),('1','2034'),('5','20359'),('39','20450'),('35','20452'),('3','20467'),('4','20480'),('7','20481'),('43','20492'),('8','2051'),('56','20531'),('80','20589'),('43','20658'),('43','20664'),('1','20695'),('4','21060'),('80','21083'),('3','21091'),('14','21094'),('7','2160'),('110','2399'),('1','2404'),('54','2559'),('45','2562'),('110','2570'),('14','26725'),('14','26756'),('14','26793'),('35','26900'),('1','27258'),('4','27341'),('35','27348'),('35','27440'),('7','27450'),('30','27696'),('52','27698'),('6','28097'),('6','28146'),('111','28147'),('5','28157'),('30','28183'),('4','28301'),('54','28342'),('56','28448'),('56','28468'),('111','28491'),('8','28495'),('45','28499'),('56','28541'),('3','28566'),('39','28568'),('11','28593'),('5','28654'),('1','3'),('5','3118'),('52','3119'),('11','32317'),('110','32318'),('11','3273'),('111','3296'),('56','33223'),('111','33298'),('35','3332'),('111','34285'),('110','34296'),('56','34392'),('5','35865'),('1','363'),('30','3630'),('110','3658'),('14','3665'),('6','36903'),('3','36968'),('6','37055'),('54','37084'),('35','37269'),('52','3731'),('111','37334'),('56','37339'),('8','37352'),('56','3736'),('43','37572'),('8','37634'),('4','37639'),('1','37642'),('6','37742'),('3','37748'),('39','37847'),('8','3785'),('110','37869'),('4','37939'),('39','38251'),('6','38290'),('45','38297'),('30','38328'),('4','38429'),('6','38462'),('1','38530'),('8','3897'),('54','39104'),('11','39202'),('80','39215'),('80','39217'),('35','39253'),('14','39336'),('54','39439'),('80','39464'),('1','39725'),('52','39765'),('45','39776'),('35','39895'),('39','39982'),('14','40142'),('8','40146'),('45','40204'),('39','40231'),('6','40275'),('110','40349'),('4','40387'),('39','40399'),('45','40451'),('5','40501'),('80','40555'),('3','40564'),('39','40616'),('39','40660'),('4','40725'),('8','40755'),('54','4098'),('8','41135'),('11','41184'),('39','41208'),('45','41262'),('8','41270'),('5','4142'),('7','41705'),('45','41727'),('3','41792'),('7','41823'),('52','4202'),('54','42425'),('3','42427'),('8','42428'),('43','42493'),('54','42518'),('43','42544'),('52','4255'),('43','42593'),('4','42758'),('80','42996'),('1','43020'),('111','43035'),('14','43191'),('1','43250'),('3','43274'),('8','43670'),('45','4374'),('39','44302'),('11','4445'),('4','4454'),('7','45139'),('110','45158'),('30','45175'),('6','4611'),('54','46581'),('45','46695'),('14','4719'),('80','47390'),('5','4740'),('4','47412'),('111','48730'),('111','49013'),('5','49323'),('11','49384'),('7','49493'),('45','49539'),('111','49724'),('5','49806'),('54','4990'),('6','49944'),('111','50023'),('1','50175'),('14','50232'),('45','50264'),('43','5043'),('111','50471'),('111','50472'),('11','5075'),('7','51484'),('3','51507'),('80','51511'),('5','51887'),('1','51922'),('7','51938'),('1','51940'),('56','5207'),('7','52477'),('6','52876'),('80','5288'),('52','53238'),('56','53371'),('8','53392'),('6','54469'),('1','54771'),('1','54772'),('111','55415'),('80','55422'),('14','55548'),('11','55586'),('4','55604'),('110','5589'),('1','55909'),('3','56864'),('52','5692'),('5','56944'),('14','56979'),('35','57069'),('3','57214'),('52','5730'),('45','5741'),('35','5750'),('11','57835'),('6','58621'),('56','58771'),('11','58778'),('14','58786'),('7','58845'),('5','58900'),('7','59013'),('56','59125'),('4','59304'),('30','59325'),('3','59936'),('11','59949'),('35','60232'),('11','60865'),('111','61538'),('11','61933'),('4','61944'),('11','6219'),('4','6240'),('11','62419'),('6','6254'),('52','6286'),('54','6298'),('56','63370'),('43','65807'),('8','66749'),('56','66797'),('30','66978'),('110','6769'),('52','67731'),('80','68815'),('80','73459'),('1','74208'),('111','74230'),('43','7459'),('43','7551'),('30','75773'),('45','75880'),('56','7631'),('39','7635'),('1','76359'),('11','7645'),('35','7670'),('30','7712'),('7','77800'),('8','78056'),('14','78108'),('39','78774'),('4','7933'),('6','7958'),('35','79843'),('56','80061'),('11','80181'),('30','80235'),('3','80254'),('52','80442'),('7','80979'),('54','81025'),('3','81880'),('30','82263'),('30','82514'),('5','83428'),('39','8358'),('39','83701'),('5','8371'),('11','8378'),('7','8380'),('14','8432'),('8','8442'),('1','8595'),('3','8597'),('5','86168'),('3','86364'),('39','87835'),('5','87860'),('5','88658'),('11','88894'),('5','89328'),('11','9007'),('14','9047'),('3','90801'),('35','9089'),('111','9110'),('1','91126'),('52','91128'),('14','91979'),('30','92372'),('5','92488'),('52','92790'),('43','93666'),('54','9493'),('6','95703'),('14','9631'),('110','973'),('4','97612'),('30','9765'),('30','98464'),('56','98769'),('4','999');
/*!40000 ALTER TABLE `TeamPlayer` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-16 15:52:48
