CREATE DATABASE  IF NOT EXISTS `UW_std` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `UW_std`;
-- MySQL dump 10.13  Distrib 5.6.13, for Win32 (x86)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: UW_std
-- ------------------------------------------------------
-- Server version	5.5.34

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
-- Table structure for table `advisedBy`
--

DROP TABLE IF EXISTS `advisedBy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advisedBy` (
  `p_id` int(11) NOT NULL DEFAULT '0',
  `p_id_dummy` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`p_id_dummy`,`p_id`),
  KEY `FK_u2base_1` (`p_id`),
  KEY `FK_u2base_2` (`p_id_dummy`),
  CONSTRAINT `FK_advisedBy_person` FOREIGN KEY (`p_id`) REFERENCES `person` (`p_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_advisedBy_person_2` FOREIGN KEY (`p_id_dummy`) REFERENCES `person` (`p_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advisedBy`
--

LOCK TABLES `advisedBy` WRITE;
/*!40000 ALTER TABLE `advisedBy` DISABLE KEYS */;
INSERT INTO `advisedBy` VALUES (6,29),(6,165),(9,335),(13,240),(14,150),(18,335),(21,211),(37,79),(41,394),(45,211),(45,415),(62,104),(63,415),(67,98),(67,375),(68,201),(75,331),(80,234),(81,342),(81,393),(83,349),(89,104),(92,101),(96,5),(99,104),(100,104),(100,235),(113,342),(113,394),(116,124),(118,5),(122,72),(126,213),(129,179),(129,234),(130,124),(141,331),(142,342),(148,171),(154,124),(154,235),(155,101),(157,72),(159,57),(159,201),(163,393),(176,407),(183,5),(200,72),(204,104),(206,72),(206,342),(208,319),(212,180),(217,72),(217,342),(218,101),(226,324),(228,342),(228,393),(228,394),(239,171),(242,29),(242,165),(249,331),(253,101),(257,240),(262,292),(262,415),(263,5),(265,168),(266,7),(272,7),(275,79),(276,407),(280,101),(286,171),(288,165),(300,342),(303,29),(303,165),(309,378),(312,319),(314,415),(318,185),(318,319),(320,150),(348,324),(352,292),(352,415),(353,319),(357,124),(362,5),(362,335),(368,180),(374,179),(376,107),(376,179),(380,79),(381,168),(384,240),(384,407),(391,235),(403,234),(404,72),(411,373),(418,171),(419,101),(426,179),(426,235),(429,335),(432,240),(435,279);
/*!40000 ALTER TABLE `advisedBy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course` (
  `course_id` int(11) NOT NULL,
  `courseLevel` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  KEY `course_courseLevel` (`courseLevel`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (5,'Level_300'),(11,'Level_300'),(18,'Level_300'),(104,'Level_300'),(124,'Level_300'),(146,'Level_300'),(147,'Level_300'),(165,'Level_300'),(8,'Level_400'),(20,'Level_400'),(21,'Level_400'),(24,'Level_400'),(27,'Level_400'),(28,'Level_400'),(30,'Level_400'),(38,'Level_400'),(41,'Level_400'),(44,'Level_400'),(45,'Level_400'),(48,'Level_400'),(49,'Level_400'),(51,'Level_400'),(52,'Level_400'),(53,'Level_400'),(57,'Level_400'),(62,'Level_400'),(68,'Level_400'),(75,'Level_400'),(80,'Level_400'),(82,'Level_400'),(89,'Level_400'),(93,'Level_400'),(97,'Level_400'),(107,'Level_400'),(110,'Level_400'),(118,'Level_400'),(122,'Level_400'),(125,'Level_400'),(126,'Level_400'),(128,'Level_400'),(137,'Level_400'),(143,'Level_400'),(148,'Level_400'),(151,'Level_400'),(154,'Level_400'),(157,'Level_400'),(159,'Level_400'),(161,'Level_400'),(164,'Level_400'),(174,'Level_400'),(0,'Level_500'),(1,'Level_500'),(2,'Level_500'),(3,'Level_500'),(4,'Level_500'),(7,'Level_500'),(9,'Level_500'),(12,'Level_500'),(13,'Level_500'),(14,'Level_500'),(15,'Level_500'),(16,'Level_500'),(19,'Level_500'),(23,'Level_500'),(29,'Level_500'),(32,'Level_500'),(34,'Level_500'),(35,'Level_500'),(36,'Level_500'),(39,'Level_500'),(40,'Level_500'),(46,'Level_500'),(50,'Level_500'),(54,'Level_500'),(56,'Level_500'),(61,'Level_500'),(63,'Level_500'),(64,'Level_500'),(65,'Level_500'),(66,'Level_500'),(67,'Level_500'),(71,'Level_500'),(74,'Level_500'),(76,'Level_500'),(77,'Level_500'),(79,'Level_500'),(83,'Level_500'),(84,'Level_500'),(85,'Level_500'),(86,'Level_500'),(87,'Level_500'),(88,'Level_500'),(91,'Level_500'),(98,'Level_500'),(101,'Level_500'),(103,'Level_500'),(108,'Level_500'),(109,'Level_500'),(114,'Level_500'),(115,'Level_500'),(116,'Level_500'),(117,'Level_500'),(119,'Level_500'),(120,'Level_500'),(121,'Level_500'),(123,'Level_500'),(129,'Level_500'),(131,'Level_500'),(132,'Level_500'),(134,'Level_500'),(135,'Level_500'),(136,'Level_500'),(138,'Level_500'),(139,'Level_500'),(141,'Level_500'),(144,'Level_500'),(149,'Level_500'),(150,'Level_500'),(152,'Level_500'),(153,'Level_500'),(155,'Level_500'),(156,'Level_500'),(158,'Level_500'),(160,'Level_500'),(162,'Level_500'),(166,'Level_500'),(167,'Level_500'),(168,'Level_500'),(169,'Level_500'),(170,'Level_500'),(172,'Level_500'),(173,'Level_500');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `p_id` int(11) NOT NULL DEFAULT '0',
  `professor` varchar(11) NOT NULL DEFAULT '0',
  `student` varchar(11) NOT NULL DEFAULT '0',
  `hasPosition` varchar(11) NOT NULL DEFAULT '0',
  `inPhase` varchar(40) DEFAULT NULL,
  `yearsInProgram` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`p_id`),
  KEY `person_hasPosition` (`hasPosition`),
  KEY `person_inPhase` (`inPhase`),
  KEY `person_yearInProgram` (`yearsInProgram`),
  KEY `person_professor` (`professor`),
  KEY `person_student` (`student`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES (3,'0','1','0','0','0'),(4,'0','1','0','0','0'),(5,'1','0','Faculty','0','0'),(6,'0','1','0','Post_Quals','Year_2'),(7,'1','0','Faculty_adj','0','0'),(9,'0','1','0','Post_Generals','Year_5'),(13,'0','1','0','Post_Generals','Year_7'),(14,'0','1','0','Post_Generals','Year_10'),(15,'0','1','0','Post_Quals','Year_3'),(18,'0','1','0','Pre_Quals','Year_3'),(19,'0','1','0','Pre_Quals','Year_1'),(20,'0','1','0','Pre_Quals','Year_1'),(21,'0','1','0','Post_Generals','Year_5'),(22,'1','0','Faculty_eme','0','0'),(23,'0','1','0','0','0'),(27,'0','1','0','Pre_Quals','Year_1'),(29,'1','0','Faculty_adj','0','0'),(31,'0','1','0','0','0'),(35,'0','1','0','0','0'),(36,'0','1','0','0','0'),(37,'0','1','0','Pre_Quals','Year_1'),(38,'0','1','0','0','0'),(39,'0','1','0','0','0'),(40,'1','0','Faculty','0','0'),(41,'0','1','0','Post_Quals','Year_5'),(42,'0','1','0','Pre_Quals','Year_1'),(45,'0','1','0','Post_Generals','Year_5'),(46,'1','0','Faculty','0','0'),(51,'0','1','0','Pre_Quals','Year_2'),(52,'1','0','Faculty','0','0'),(57,'1','0','0','0','0'),(58,'0','1','0','0','0'),(61,'0','1','0','0','0'),(62,'0','1','0','Pre_Quals','Year_2'),(63,'0','1','0','Post_Generals','Year_5'),(64,'1','0','0','0','0'),(67,'0','1','0','Post_Generals','Year_6'),(68,'0','1','0','Post_Generals','Year_5'),(70,'0','1','0','Pre_Quals','Year_1'),(71,'0','1','0','0','0'),(72,'1','0','Faculty','0','0'),(73,'0','1','0','Post_Quals','Year_4'),(75,'0','1','0','Post_Generals','Year_6'),(76,'0','1','0','0','0'),(77,'0','1','0','0','0'),(79,'1','0','Faculty','0','0'),(80,'0','1','0','Post_Generals','Year_6'),(81,'0','1','0','Post_Generals','Year_6'),(82,'1','0','Faculty','0','0'),(83,'0','1','0','Post_Quals','Year_5'),(84,'0','1','0','0','0'),(85,'0','1','0','0','0'),(86,'0','1','0','0','0'),(87,'0','1','0','0','0'),(88,'0','1','0','0','0'),(89,'0','1','0','Post_Generals','Year_5'),(90,'0','1','0','0','0'),(92,'0','1','0','Post_Generals','Year_5'),(94,'0','1','0','Pre_Quals','Year_1'),(96,'0','1','0','Post_Generals','Year_5'),(98,'1','0','Faculty','0','0'),(99,'0','1','0','Post_Quals','Year_2'),(100,'0','1','0','Post_Quals','Year_5'),(101,'1','0','Faculty','0','0'),(102,'0','1','0','0','0'),(103,'1','0','Faculty_aff','0','0'),(104,'1','0','Faculty','0','0'),(105,'0','1','0','0','0'),(107,'1','0','Faculty','0','0'),(108,'0','1','0','0','0'),(111,'1','0','Faculty_adj','0','0'),(113,'0','1','0','Post_Generals','Year_4'),(115,'1','0','Faculty','0','0'),(116,'0','1','0','Pre_Quals','Year_3'),(118,'0','1','0','Post_Generals','Year_4'),(119,'0','1','0','0','0'),(122,'0','1','0','Post_Quals','Year_4'),(123,'0','1','0','0','0'),(124,'1','0','Faculty','0','0'),(125,'0','1','0','0','0'),(126,'0','1','0','Post_Quals','Year_5'),(129,'0','1','0','Post_Generals','Year_6'),(130,'0','1','0','Post_Generals','Year_8'),(131,'0','1','0','0','0'),(138,'0','1','0','0','0'),(139,'0','1','0','Post_Quals','Year_3'),(140,'0','1','0','0','0'),(141,'0','1','0','Post_Generals','Year_6'),(142,'0','1','0','Post_Generals','Year_9'),(144,'0','1','0','0','0'),(146,'0','1','0','0','0'),(148,'0','1','0','Post_Quals','Year_5'),(149,'0','1','0','Post_Quals','Year_5'),(150,'1','0','Faculty','0','0'),(154,'0','1','0','Post_Quals','Year_4'),(155,'0','1','0','Pre_Quals','Year_2'),(157,'0','1','0','Post_Quals','Year_4'),(158,'0','1','0','0','0'),(159,'0','1','0','Post_Quals','Year_2'),(161,'0','1','0','Post_Generals','Year_7'),(163,'0','1','0','Post_Quals','Year_4'),(165,'1','0','Faculty','0','0'),(166,'1','0','0','0','0'),(167,'0','1','0','0','0'),(168,'1','0','Faculty','0','0'),(171,'1','0','Faculty','0','0'),(172,'0','1','0','Pre_Quals','Year_1'),(175,'0','1','0','Post_Generals','Year_2'),(176,'0','1','0','Post_Quals','Year_2'),(178,'0','1','0','0','0'),(179,'1','0','Faculty','0','0'),(180,'1','0','Faculty','0','0'),(181,'1','0','0','0','0'),(182,'0','1','0','Post_Quals','Year_3'),(183,'0','1','0','Pre_Quals','Year_4'),(185,'1','0','Faculty_adj','0','0'),(186,'0','1','0','Pre_Quals','Year_1'),(187,'0','1','0','Pre_Quals','Year_1'),(188,'0','1','0','0','0'),(189,'1','0','Faculty_adj','0','0'),(190,'0','1','0','0','0'),(191,'0','1','0','Post_Quals','Year_4'),(193,'0','1','0','Pre_Quals','Year_1'),(195,'0','1','0','0','0'),(198,'0','1','0','0','0'),(200,'0','1','0','Post_Quals','Year_4'),(201,'1','0','Faculty','0','0'),(203,'0','1','0','0','0'),(204,'0','1','0','Post_Generals','Year_6'),(205,'0','1','0','Pre_Quals','Year_1'),(206,'0','1','0','Post_Generals','Year_6'),(207,'0','1','0','0','0'),(208,'0','1','0','Post_Quals','Year_4'),(211,'1','0','Faculty','0','0'),(212,'0','1','0','Post_Generals','Year_7'),(213,'1','0','Faculty','0','0'),(214,'0','1','0','0','0'),(217,'0','1','0','Post_Generals','Year_5'),(218,'0','1','0','Post_Generals','Year_12'),(222,'0','1','0','Pre_Quals','Year_1'),(223,'0','1','0','0','0'),(226,'0','1','0','Post_Quals','Year_4'),(228,'0','1','0','Post_Quals','Year_3'),(230,'0','1','0','0','0'),(231,'1','0','0','0','0'),(232,'0','1','0','0','0'),(233,'0','1','0','Pre_Quals','Year_1'),(234,'1','0','Faculty','0','0'),(235,'1','0','Faculty','0','0'),(237,'0','1','0','0','0'),(239,'0','1','0','Post_Quals','Year_4'),(240,'1','0','Faculty','0','0'),(241,'0','1','0','Post_Quals','Year_3'),(242,'0','1','0','Post_Generals','Year_5'),(248,'1','0','0','0','0'),(249,'0','1','0','Post_Generals','Year_7'),(253,'0','1','0','Post_Generals','Year_5'),(255,'0','1','0','Post_Generals','Year_5'),(257,'0','1','0','Post_Generals','Year_7'),(258,'0','1','0','0','0'),(259,'0','1','0','0','0'),(261,'0','1','0','0','0'),(262,'0','1','0','Post_Generals','Year_7'),(263,'0','1','0','Post_Generals','Year_6'),(265,'0','1','0','Post_Generals','Year_9'),(266,'0','1','0','Post_Quals','Year_5'),(267,'1','0','0','0','0'),(269,'0','1','0','0','0'),(270,'0','1','0','Pre_Quals','Year_1'),(271,'0','1','0','0','0'),(272,'0','1','0','Post_Quals','Year_2'),(274,'0','1','0','0','0'),(275,'0','1','0','Post_Generals','Year_5'),(276,'0','1','0','Pre_Quals','Year_3'),(277,'0','1','0','Pre_Quals','Year_1'),(278,'0','1','0','Pre_Quals','Year_2'),(279,'1','0','Faculty','0','0'),(280,'0','1','0','Pre_Quals','Year_3'),(283,'0','1','0','Pre_Quals','Year_1'),(284,'0','1','0','Post_Quals','Year_3'),(286,'0','1','0','Post_Quals','Year_3'),(287,'0','1','0','0','0'),(288,'0','1','0','Post_Generals','Year_5'),(290,'1','0','Faculty','0','0'),(292,'1','0','Faculty_aff','0','0'),(293,'1','0','Faculty_aff','0','0'),(294,'0','1','0','0','0'),(296,'0','1','0','0','0'),(297,'1','0','Faculty_eme','0','0'),(298,'1','0','Faculty','0','0'),(299,'0','1','0','Pre_Quals','Year_3'),(300,'0','1','0','Post_Generals','Year_8'),(303,'0','1','0','Post_Quals','Year_4'),(306,'0','1','0','0','0'),(309,'0','1','0','Post_Quals','Year_3'),(310,'0','1','0','0','0'),(311,'0','1','0','Post_Quals','Year_3'),(312,'0','1','0','Pre_Quals','Year_4'),(314,'0','1','0','Post_Generals','Year_4'),(315,'0','1','0','0','0'),(317,'0','1','0','0','0'),(318,'0','1','0','Pre_Quals','Year_5'),(319,'1','0','Faculty','0','0'),(320,'0','1','0','Post_Quals','Year_3'),(321,'0','1','0','0','0'),(322,'0','1','0','0','0'),(324,'1','0','Faculty','0','0'),(325,'0','1','0','0','0'),(326,'1','0','0','0','0'),(327,'0','1','0','0','0'),(328,'0','1','0','0','0'),(331,'1','0','Faculty','0','0'),(333,'0','1','0','Pre_Quals','Year_2'),(335,'1','0','Faculty','0','0'),(340,'0','1','0','0','0'),(342,'1','0','Faculty','0','0'),(343,'0','1','0','Pre_Quals','Year_1'),(347,'0','1','0','0','0'),(348,'0','1','0','Post_Quals','Year_3'),(349,'1','0','Faculty_adj','0','0'),(350,'0','1','0','0','0'),(351,'1','0','Faculty','0','0'),(352,'0','1','0','Post_Generals','Year_5'),(353,'0','1','0','Post_Quals','Year_4'),(354,'0','1','0','0','0'),(356,'0','1','0','0','0'),(357,'0','1','0','Post_Quals','Year_4'),(358,'0','1','0','0','0'),(361,'0','1','0','Post_Generals','Year_6'),(362,'0','1','0','Post_Quals','Year_3'),(363,'0','1','0','Pre_Quals','Year_3'),(364,'1','0','0','0','0'),(368,'0','1','0','Post_Generals','Year_4'),(370,'1','0','0','0','0'),(373,'1','0','Faculty','0','0'),(374,'0','1','0','Post_Generals','Year_12'),(375,'1','0','Faculty_eme','0','0'),(376,'0','1','0','Post_Quals','Year_4'),(377,'0','1','0','Pre_Quals','Year_1'),(378,'1','0','Faculty','0','0'),(380,'0','1','0','Post_Generals','Year_6'),(381,'0','1','0','Post_Generals','Year_10'),(382,'0','1','0','Post_Quals','Year_3'),(383,'0','1','0','Pre_Quals','Year_2'),(384,'0','1','0','Post_Quals','Year_3'),(390,'0','1','0','Pre_Quals','Year_2'),(391,'0','1','0','Post_Quals','Year_4'),(392,'0','1','0','0','0'),(393,'1','0','Faculty','0','0'),(394,'1','0','Faculty','0','0'),(397,'0','1','0','0','0'),(398,'0','1','0','Pre_Quals','Year_1'),(400,'0','1','0','0','0'),(401,'0','1','0','0','0'),(402,'0','1','0','Pre_Quals','Year_2'),(403,'0','1','0','Post_Generals','Year_12'),(404,'0','1','0','Post_Generals','Year_4'),(406,'0','1','0','Post_Generals','Year_5'),(407,'1','0','Faculty','0','0'),(408,'0','1','0','Pre_Quals','Year_2'),(410,'0','1','0','0','0'),(411,'0','1','0','Post_Generals','Year_6'),(412,'0','1','0','Post_Quals','Year_3'),(415,'1','0','Faculty','0','0'),(416,'0','1','0','Pre_Quals','Year_1'),(417,'0','1','0','Pre_Quals','Year_1'),(418,'0','1','0','Post_Quals','Year_3'),(419,'0','1','0','Post_Generals','Year_7'),(420,'0','1','0','0','0'),(422,'0','1','0','Post_Quals','Year_3'),(424,'0','1','0','0','0'),(426,'0','1','0','Post_Quals','Year_5'),(427,'0','1','0','Post_Quals','Year_4'),(428,'0','1','0','0','0'),(429,'0','1','0','Post_Quals','Year_5'),(431,'0','1','0','Pre_Quals','Year_2'),(432,'0','1','0','Post_Quals','Year_5'),(435,'0','1','0','Post_Quals','Year_4');
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taughtBy`
--

DROP TABLE IF EXISTS `taughtBy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `taughtBy` (
  `course_id` int(11) NOT NULL DEFAULT '0',
  `p_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`course_id`,`p_id`),
  KEY `FK_2` (`course_id`),
  KEY `FK_1` (`p_id`),
  CONSTRAINT `FK_taught_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_taught_person` FOREIGN KEY (`p_id`) REFERENCES `person` (`p_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taughtBy`
--

LOCK TABLES `taughtBy` WRITE;
/*!40000 ALTER TABLE `taughtBy` DISABLE KEYS */;
INSERT INTO `taughtBy` VALUES (0,40),(1,40),(2,180),(3,279),(4,107),(7,415),(8,297),(9,235),(11,52),(11,57),(11,298),(11,324),(11,331),(12,79),(12,211),(12,407),(13,72),(13,342),(14,124),(15,292),(16,79),(16,240),(18,107),(18,213),(18,290),(18,326),(18,373),(18,375),(19,5),(19,370),(20,180),(21,22),(21,99),(23,179),(24,79),(24,150),(24,211),(24,240),(24,407),(27,165),(27,331),(28,394),(29,298),(30,290),(32,319),(34,179),(36,181),(38,104),(38,124),(38,204),(38,255),(39,415),(40,165),(40,298),(40,378),(41,351),(44,171),(44,293),(44,415),(46,335),(48,107),(48,213),(48,375),(49,64),(49,189),(49,248),(49,263),(50,171),(51,5),(51,18),(51,166),(52,168),(53,189),(53,248),(57,150),(61,107),(62,101),(63,335),(64,79),(66,165),(66,298),(67,394),(68,201),(68,324),(68,331),(71,5),(74,104),(74,124),(75,267),(76,319),(77,52),(77,165),(77,324),(79,72),(80,98),(80,101),(80,180),(82,407),(84,324),(88,235),(89,394),(91,331),(93,351),(97,324),(98,103),(101,279),(101,394),(103,201),(104,165),(104,181),(104,364),(108,279),(110,351),(115,72),(115,342),(116,375),(117,181),(118,351),(119,324),(120,82),(120,235),(121,52),(122,378),(123,150),(124,9),(124,46),(124,335),(125,351),(126,165),(128,150),(129,213),(129,373),(132,319),(134,240),(136,394),(137,165),(138,335),(139,235),(141,150),(143,211),(143,407),(144,278),(144,331),(146,335),(147,52),(147,57),(147,165),(147,201),(147,324),(147,331),(147,364),(148,351),(149,331),(150,351),(151,82),(151,179),(151,234),(151,235),(151,267),(151,290),(153,342),(153,394),(156,240),(157,72),(157,342),(157,394),(158,240),(159,394),(160,331),(161,201),(161,298),(161,331),(162,213),(164,351),(165,75),(165,141),(165,181),(165,231),(165,364),(166,235),(167,98),(168,240),(170,79),(170,211),(170,407),(172,46),(172,335),(173,171),(174,267);
/*!40000 ALTER TABLE `taughtBy` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-01-21 11:39:32
