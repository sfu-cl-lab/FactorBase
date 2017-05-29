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
-- Table structure for table `AppearsTeamAway`
--

DROP TABLE IF EXISTS `AppearsTeamAway`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AppearsTeamAway` (
  `TeamID` varchar(40) NOT NULL DEFAULT '',
  `MatchID` varchar(40) NOT NULL DEFAULT '',
  `shot_eff_AT` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`TeamID`,`MatchID`),
  KEY `fk_AppearsTeamAway_1_idx` (`TeamID`),
  KEY `fk_AppearsTeamAway_1_idx1` (`MatchID`),
  CONSTRAINT `fk_AppearsTeamAway_1` FOREIGN KEY (`TeamID`) REFERENCES `Teams` (`TeamID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_AppearsTeamAway_2` FOREIGN KEY (`MatchID`) REFERENCES `MatchComp` (`MatchID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AppearsTeamAway`
--

LOCK TABLES `AppearsTeamAway` WRITE;
/*!40000 ALTER TABLE `AppearsTeamAway` DISABLE KEYS */;
INSERT INTO `AppearsTeamAway` VALUES ('1','119','0.7273'),('1','150','0.6923'),('1','177','0.8000'),('1','18','0.6667'),('1','203','0.7273'),('1','227','0.2222'),('1','250','0.0000'),('1','272','0.7500'),('1','274','0.7778'),('1','275','0.4444'),('1','277','0.4000'),('1','280','0.4375'),('1','281','0.5000'),('1','284','0.8333'),('1','285','0.1429'),('1','288','0.1429'),('1','290','0.6923'),('1','53','0.4000'),('1','88','0.5000'),('11','111','0.6923'),('11','141','0.3333'),('11','171','0.5556'),('11','174','0.2000'),('11','175','0.4286'),('11','178','0.5000'),('11','179','0.3333'),('11','182','0.7143'),('11','184','0.2857'),('11','186','0.4000'),('11','187','0.2500'),('11','190','0.4167'),('11','191','0.0000'),('11','193','0.4444'),('11','196','0.4444'),('11','198','0.3077'),('11','46','0.6667'),('11','79','0.3750'),('11','9','0.0000'),('110','127','0.3750'),('110','158','0.0000'),('110','185','0.5000'),('110','212','0.2000'),('110','236','0.3333'),('110','25','0.2000'),('110','257','0.0000'),('110','278','0.2857'),('110','296','0.0000'),('110','311','0.3571'),('110','326','0.0000'),('110','339','0.4000'),('110','341','0.2500'),('110','344','0.2500'),('110','345','0.2000'),('110','348','0.2000'),('110','349','0.4000'),('110','62','0.2857'),('110','96','0.3750'),('111','106','0.3750'),('111','138','0.5455'),('111','168','0.4444'),('111','195','0.1429'),('111','222','0.4444'),('111','246','0.5000'),('111','267','0.4286'),('111','287','0.3333'),('111','305','0.1818'),('111','322','0.3889'),('111','336','0.3333'),('111','347','0.6667'),('111','357','0.4444'),('111','36','0.6364'),('111','365','0.3333'),('111','372','0.4286'),('111','375','0.5714'),('111','379','0.5000'),('111','71','0.4000'),('14','116','0.6250'),('14','13','0.5000'),('14','145','0.3333'),('14','173','0.5385'),('14','199','0.4000'),('14','226','0.4615'),('14','228','0.4286'),('14','230','0.4000'),('14','232','0.4167'),('14','234','0.5000'),('14','235','0.4375'),('14','238','0.3750'),('14','240','0.5000'),('14','241','0.3333'),('14','243','0.2500'),('14','245','0.5294'),('14','248','0.6190'),('14','49','0.5000'),('14','84','0.5455'),('3','1','0.6250'),('3','10','0.4444'),('3','12','0.6000'),('3','14','0.7000'),('3','15','0.6667'),('3','17','0.5714'),('3','19','0.2500'),('3','21','0.6875'),('3','24','0.5455'),('3','26','0.4167'),('3','28','0.5000'),('3','3','0.5625'),('3','30','0.5714'),('3','31','0.5000'),('3','34','0.4286'),('3','35','0.5000'),('3','38','0.7778'),('3','6','0.3077'),('3','7','0.5833'),('30','110','0.1429'),('30','112','0.7000'),('30','113','0.4286'),('30','115','0.4286'),('30','118','0.6250'),('30','120','0.1667'),('30','122','0.5000'),('30','124','0.3333'),('30','125','0.4545'),('30','128','0.2857'),('30','130','0.4615'),('30','131','0.4286'),('30','133','0.4000'),('30','135','0.5714'),('30','137','0.7500'),('30','140','0.4375'),('30','42','0.8571'),('30','5','1.0000'),('30','75','0.5000'),('35','103','0.4000'),('35','136','0.6667'),('35','165','0.6667'),('35','194','0.4000'),('35','220','0.5000'),('35','244','0.4286'),('35','266','0.2500'),('35','286','0.5000'),('35','303','0.4167'),('35','319','0.5000'),('35','33','0.2857'),('35','333','0.4545'),('35','346','0.3571'),('35','355','0.5000'),('35','363','0.5000'),('35','370','0.2000'),('35','376','0.1429'),('35','378','0.5600'),('35','69','0.5000'),('39','107','0.2857'),('39','139','0.2500'),('39','169','0.2857'),('39','197','0.2500'),('39','224','0.1667'),('39','247','0.2222'),('39','269','0.3636'),('39','289','0.6250'),('39','308','0.8000'),('39','324','0.2857'),('39','338','0.3846'),('39','350','0.5714'),('39','360','0.5556'),('39','368','0.4286'),('39','37','0.3333'),('39','374','0.6000'),('39','377','0.6154'),('39','380','0.6667'),('39','73','0.5000'),('4','121','0.6250'),('4','152','0.3333'),('4','180','0.3750'),('4','20','1.0000'),('4','206','0.5714'),('4','229','0.2500'),('4','251','0.5000'),('4','271','0.8333'),('4','291','0.7500'),('4','293','0.1667'),('4','295','0.3750'),('4','297','0.5455'),('4','300','0.7500'),('4','302','0.3333'),('4','304','0.7000'),('4','306','0.2000'),('4','307','0.5000'),('4','55','0.4615'),('4','90','0.5000'),('43','117','0.4667'),('43','147','0.3333'),('43','16','0.0000'),('43','176','0.2727'),('43','201','0.4167'),('43','225','0.4286'),('43','249','0.4667'),('43','252','0.5000'),('43','254','0.6875'),('43','255','0.3333'),('43','258','0.5455'),('43','259','0.2500'),('43','262','0.4615'),('43','263','0.4737'),('43','265','0.3333'),('43','268','0.5455'),('43','270','0.3846'),('43','52','0.4667'),('43','85','0.5000'),('45','123','0.5000'),('45','153','0.6000'),('45','181','0.1429'),('45','208','0.3333'),('45','22','0.4000'),('45','231','0.8750'),('45','253','0.3333'),('45','273','0.2500'),('45','292','0.4444'),('45','310','0.3846'),('45','312','0.6000'),('45','314','0.2000'),('45','316','0.6000'),('45','318','0.4000'),('45','320','0.5000'),('45','321','0.1818'),('45','323','0.6429'),('45','57','0.8750'),('45','92','0.2500'),('5','100','0.2500'),('5','102','-1.0000'),('5','104','0.5833'),('5','105','0.3750'),('5','108','0.5556'),('5','39','0.2500'),('5','4','0.6667'),('5','76','0.3333'),('5','78','0.5000'),('5','80','0.4000'),('5','81','0.3750'),('5','83','0.2000'),('5','86','1.0000'),('5','87','0.3750'),('5','89','0.3333'),('5','91','0.5000'),('5','93','0.7500'),('5','95','0.6000'),('5','97','0.6667'),('52','126','0.3333'),('52','156','0.2500'),('52','183','0.5000'),('52','209','0.4167'),('52','23','0.5714'),('52','233','0.2000'),('52','256','1.0000'),('52','276','0.0000'),('52','294','0.5455'),('52','309','0.2500'),('52','325','0.8333'),('52','328','0.1667'),('52','329','0.4000'),('52','331','0.3333'),('52','334','0.1333'),('52','335','0.2857'),('52','337','0.5882'),('52','60','1.0000'),('52','94','0.4118'),('54','11','0.4000'),('54','114','0.6667'),('54','143','0.6250'),('54','172','0.6667'),('54','200','0.5000'),('54','202','0.5000'),('54','204','0.6667'),('54','205','0.4615'),('54','207','0.3750'),('54','210','0.3333'),('54','211','0.2500'),('54','213','0.4444'),('54','215','0.5833'),('54','218','0.5556'),('54','219','0.3571'),('54','221','0.6000'),('54','223','0.4444'),('54','48','0.2000'),('54','82','0.3333'),('56','129','0.3333'),('56','160','0.3750'),('56','188','0.0000'),('56','214','0.5455'),('56','237','0.3077'),('56','260','0.4286'),('56','27','0.3333'),('56','279','0.1429'),('56','298','0.8000'),('56','313','0.4167'),('56','327','0.5455'),('56','340','0.2500'),('56','351','0.3333'),('56','353','0.5714'),('56','356','0.3750'),('56','358','0.7500'),('56','359','0.6000'),('56','64','0.3333'),('56','98','0.3333'),('6','101','0.7500'),('6','134','0.6364'),('6','164','0.2667'),('6','192','0.2941'),('6','217','0.6667'),('6','242','0.4286'),('6','264','0.3333'),('6','283','0.5625'),('6','301','0.6667'),('6','317','0.3529'),('6','32','0.4545'),('6','332','0.4615'),('6','343','0.6923'),('6','354','0.1111'),('6','361','0.5000'),('6','369','0.5500'),('6','371','0.5000'),('6','373','0.4545'),('6','68','0.2857'),('7','2','0.0000'),('7','40','0.5556'),('7','41','0.4545'),('7','43','0.7778'),('7','45','0.4286'),('7','47','0.1667'),('7','50','0.5000'),('7','51','0.5714'),('7','54','0.2000'),('7','56','0.5833'),('7','58','0.5000'),('7','59','0.3333'),('7','61','0.2500'),('7','63','0.3333'),('7','65','0.2857'),('7','67','0.3333'),('7','70','0.3750'),('7','72','0.3333'),('7','74','0.4000'),('8','109','0.5625'),('8','142','0.8000'),('8','144','0.2727'),('8','146','0.2857'),('8','148','0.5000'),('8','149','0.4211'),('8','151','0.5294'),('8','154','0.3529'),('8','155','0.2500'),('8','157','0.4667'),('8','159','0.3846'),('8','162','0.3077'),('8','163','0.3889'),('8','166','0.2500'),('8','167','0.2222'),('8','170','0.5385'),('8','44','0.7000'),('8','77','0.5556'),('8','8','0.2857'),('80','132','0.3077'),('80','161','0.2500'),('80','189','1.0000'),('80','216','0.6250'),('80','239','0.3333'),('80','261','0.5000'),('80','282','0.6000'),('80','29','0.2500'),('80','299','0.0000'),('80','315','0.3000'),('80','330','0.4000'),('80','342','0.2857'),('80','352','0.3000'),('80','362','0.3333'),('80','364','0.2222'),('80','366','0.6000'),('80','367','0.6923'),('80','66','0.5556'),('80','99','0.4444');
/*!40000 ALTER TABLE `AppearsTeamAway` ENABLE KEYS */;
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
