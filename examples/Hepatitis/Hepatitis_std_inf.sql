CREATE DATABASE  IF NOT EXISTS `Hepatitis_std` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `Hepatitis_std`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: Hepatitis_std
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
-- Table structure for table `inf`
--

DROP TABLE IF EXISTS `inf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inf` (
  `dur` varchar(45) DEFAULT NULL,
  `a_id` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`a_id`),
  KEY `inf_dur` (`dur`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AVG_ROW_LENGTH=83;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inf`
--

LOCK TABLES `inf` WRITE;
/*!40000 ALTER TABLE `inf` DISABLE KEYS */;
INSERT INTO `inf` VALUES ('0',2),('0',6),('0',8),('0',9),('0',14),('0',17),('0',28),('0',42),('0',43),('0',50),('0',57),('0',61),('0',64),('0',67),('0',73),('0',75),('0',76),('0',77),('0',80),('0',93),('0',116),('0',121),('0',128),('0',131),('0',132),('0',141),('0',146),('0',155),('0',156),('0',157),('0',169),('0',174),('0',175),('0',177),('0',179),('0',184),('0',189),('0',191),('0',192),('1',1),('1',3),('1',4),('1',7),('1',10),('1',13),('1',15),('1',25),('1',30),('1',31),('1',36),('1',37),('1',48),('1',62),('1',63),('1',72),('1',79),('1',82),('1',83),('1',89),('1',90),('1',92),('1',94),('1',97),('1',99),('1',111),('1',122),('1',136),('1',138),('1',142),('1',144),('1',149),('1',153),('1',172),('1',183),('1',185),('1',187),('1',193),('1',196),('2',5),('2',11),('2',12),('2',16),('2',26),('2',29),('2',35),('2',40),('2',45),('2',47),('2',49),('2',51),('2',53),('2',54),('2',59),('2',68),('2',70),('2',81),('2',85),('2',86),('2',103),('2',106),('2',108),('2',110),('2',112),('2',113),('2',117),('2',124),('2',127),('2',130),('2',140),('2',166),('2',167),('2',168),('2',171),('2',180),('2',181),('2',182),('2',190),('3',19),('3',20),('3',21),('3',22),('3',23),('3',27),('3',33),('3',41),('3',46),('3',55),('3',56),('3',65),('3',66),('3',71),('3',74),('3',87),('3',88),('3',98),('3',100),('3',101),('3',102),('3',104),('3',105),('3',107),('3',109),('3',114),('3',118),('3',119),('3',123),('3',125),('3',126),('3',129),('3',143),('3',145),('3',147),('3',152),('3',159),('3',170),('3',195),('4',18),('4',24),('4',32),('4',34),('4',38),('4',39),('4',44),('4',52),('4',58),('4',60),('4',69),('4',78),('4',84),('4',91),('4',95),('4',96),('4',115),('4',120),('4',133),('4',134),('4',135),('4',137),('4',139),('4',148),('4',150),('4',151),('4',154),('4',158),('4',160),('4',161),('4',162),('4',163),('4',164),('4',165),('4',173),('4',176),('4',178),('4',186),('4',188),('4',194);
/*!40000 ALTER TABLE `inf` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 14:59:47
