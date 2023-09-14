-- MariaDB dump 10.19  Distrib 10.4.28-MariaDB, for osx10.18 (arm64)
--
-- Host: localhost    Database: unielwin
-- ------------------------------------------------------
-- Server version	10.4.28-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `RA`
--

CREATE DATABASE  IF NOT EXISTS `unielwin` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `unielwin`;

DROP TABLE IF EXISTS `RA`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RA` (
  `capability` varchar(45) DEFAULT NULL,
  `prof_id` int(11) NOT NULL DEFAULT 0,
  `student_id` int(11) NOT NULL DEFAULT 0,
  `salary` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`prof_id`,`student_id`),
  KEY `FK_u2base_1` (`student_id`),
  KEY `FK_u2base_2` (`prof_id`),
  KEY `RA_capability` (`capability`) USING HASH,
  KEY `RA_salary` (`salary`) USING HASH,
  KEY `HashIn` (`capability`,`prof_id`,`student_id`,`salary`),
  CONSTRAINT `FK_RA_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_RA_2` FOREIGN KEY (`prof_id`) REFERENCES `prof` (`prof_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RA`
--

LOCK TABLES `RA` WRITE;
/*!40000 ALTER TABLE `RA` DISABLE KEYS */;
INSERT INTO `RA` VALUES ('1',5,5,'low'),('1',6,15,'med'),('1',7,11,'med'),('1',7,20,'low'),('1',8,12,'med'),('2',5,14,'low'),('2',6,5,'low'),('2',6,16,'med'),('2',8,13,'med'),('3',4,17,'med'),('3',5,18,'high'),('3',5,27,'med'),('3',5,28,'med'),('3',7,22,'med'),('3',8,21,'low'),('3',9,10,'high'),('4',5,26,'high'),('4',6,8,'high'),('4',6,9,'high'),('4',7,7,'high'),('4',7,8,'high'),('5',6,23,'high'),('5',6,24,'high'),('5',6,25,'high'),('5',7,19,'high');
/*!40000 ALTER TABLE `RA` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course` (
  `course_id` int(11) NOT NULL,
  `rating` varchar(45) DEFAULT NULL,
  `diff` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  KEY `course_rating` (`rating`) USING HASH,
  KEY `course_diff` (`diff`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (4,'2','1'),(5,'2','2'),(6,'2','1'),(7,'2','2'),(8,'2','1'),(9,'2','2'),(10,'2','1'),(11,'1','2'),(12,'1','1'),(13,'1','2');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prof`
--

DROP TABLE IF EXISTS `prof`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prof` (
  `prof_id` int(11) NOT NULL DEFAULT 0,
  `popularity` varchar(45) DEFAULT NULL,
  `teachingability` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`prof_id`),
  KEY `prof_popularity` (`popularity`) USING HASH,
  KEY `prof_teachingability` (`teachingability`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prof`
--

LOCK TABLES `prof` WRITE;
/*!40000 ALTER TABLE `prof` DISABLE KEYS */;
INSERT INTO `prof` VALUES (4,'2','3'),(5,'2','3'),(6,'2','3'),(7,'2','2'),(8,'1','2'),(9,'1','2');
/*!40000 ALTER TABLE `prof` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registration`
--

DROP TABLE IF EXISTS `registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `registration` (
  `course_id` int(11) NOT NULL DEFAULT 0,
  `student_id` int(11) NOT NULL DEFAULT 0,
  `grade` varchar(45) NOT NULL,
  `sat` varchar(45) NOT NULL,
  PRIMARY KEY (`course_id`,`student_id`),
  KEY `FK_u2base_1` (`student_id`),
  KEY `FK_u2base_2` (`course_id`),
  KEY `registration_sat` (`sat`) USING HASH,
  KEY `registration_grade` (`grade`) USING HASH,
  CONSTRAINT `FK_registration_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_registration_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registration`
--

LOCK TABLES `registration` WRITE;
/*!40000 ALTER TABLE `registration` DISABLE KEYS */;
INSERT INTO `registration` VALUES (4,4,'1','1'),(4,6,'1','1'),(4,7,'1','1'),(4,12,'2','2'),(4,18,'2','2'),(4,29,'3','2'),(4,38,'4','3'),(4,40,'1','1'),(5,4,'1','1'),(5,5,'2','2'),(5,6,'2','2'),(5,15,'2','1'),(5,18,'3','2'),(5,25,'3','2'),(5,29,'4','3'),(5,34,'3','2'),(5,38,'4','3'),(5,39,'1','1'),(5,41,'1','1'),(6,4,'1','1'),(6,6,'1','1'),(6,7,'1','1'),(6,9,'1','1'),(6,13,'1','1'),(6,19,'2','3'),(6,25,'3','3'),(6,30,'4','3'),(6,41,'1','1'),(7,4,'2','1'),(7,5,'2','2'),(7,9,'2','2'),(7,15,'2','1'),(7,20,'2','2'),(7,30,'4','3'),(7,40,'1','1'),(8,5,'1','1'),(8,6,'1','1'),(8,9,'1','2'),(8,10,'1','1'),(8,11,'2','2'),(8,14,'1','1'),(8,20,'3','3'),(8,22,'3','2'),(8,30,'4','3'),(8,41,'1','1'),(9,6,'1','1'),(9,10,'2','2'),(9,12,'2','1'),(9,15,'2','1'),(9,23,'2','2'),(9,31,'3','2'),(9,33,'3','2'),(9,35,'3','2'),(10,6,'1','1'),(10,11,'1','2'),(10,14,'1','1'),(10,17,'3','3'),(10,18,'1','1'),(10,23,'2','1'),(10,32,'3','2'),(10,36,'4','3'),(11,7,'2','1'),(11,8,'1','1'),(11,14,'2','1'),(11,16,'2','1'),(11,17,'2','1'),(11,23,'3','3'),(11,28,'4','3'),(11,32,'4','3'),(11,37,'3','3'),(11,41,'2','2'),(12,8,'1','1'),(12,10,'1','1'),(12,11,'1','2'),(12,17,'2','2'),(12,21,'1','1'),(12,22,'2','1'),(12,23,'1','1'),(12,24,'1','1'),(12,26,'2','3'),(12,28,'3','2'),(12,33,'4','3'),(12,37,'4','3'),(13,7,'2','1'),(13,14,'2','2'),(13,16,'3','2'),(13,21,'2','2'),(13,26,'3','3'),(13,27,'3','2'),(13,33,'4','3'),(13,37,'4','3'),(13,41,'2','2');
/*!40000 ALTER TABLE `registration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student` (
  `student_id` int(11) NOT NULL DEFAULT 0,
  `intelligence` varchar(45) DEFAULT NULL,
  `ranking` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  KEY `student_intelligence` (`intelligence`) USING HASH,
  KEY `student_ranking` (`ranking`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (4,'3','1'),(5,'3','1'),(6,'3','2'),(7,'3','1'),(8,'3','1'),(9,'3','2'),(10,'3','1'),(11,'3','2'),(12,'2','2'),(13,'2','2'),(14,'2','2'),(15,'2','3'),(16,'2','3'),(17,'2','3'),(18,'2','3'),(19,'2','3'),(20,'2','3'),(21,'2','2'),(22,'2','4'),(23,'2','3'),(24,'2','2'),(25,'1','4'),(26,'1','4'),(27,'1','4'),(28,'1','4'),(29,'1','5'),(30,'1','5'),(31,'1','5'),(32,'1','5'),(33,'1','5'),(34,'1','4'),(35,'1','4'),(36,'1','5'),(37,'1','5'),(38,'1','5'),(39,'3','1'),(40,'3','1'),(41,'3','1');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-09-13 16:39:48
