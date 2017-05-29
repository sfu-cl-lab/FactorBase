CREATE DATABASE  IF NOT EXISTS `imdb_MovieLens_BN` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `imdb_MovieLens_BN`;
-- MySQL dump 10.13  Distrib 5.1.69, for redhat-linux-gnu (x86_64)
--
-- Host: cs-oschulte-01.cs.sfu.ca    Database: imdb_MovieLens_BN
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
-- Table structure for table `cast_num(movies0,actors0)_CP`
--

DROP TABLE IF EXISTS `cast_num(movies0,actors0)_CP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cast_num(movies0,actors0)_CP` (
  `MULT` decimal(42,0) DEFAULT NULL,
  `ChildValue` int(11) NOT NULL,
  `avg_revenue(directors0)` int(11) NOT NULL,
  `a_quality(actors0)` int(2) NOT NULL,
  `isEnglish(movies0)` enum('T','F') NOT NULL,
  `year(movies0)` int(11) NOT NULL,
  `ParentSum` bigint(20) DEFAULT NULL,
  `CP` float(7,6) DEFAULT NULL,
  `likelihood` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cast_num(movies0,actors0)_CP`
--

LOCK TABLES `cast_num(movies0,actors0)_CP` WRITE;
/*!40000 ALTER TABLE `cast_num(movies0,actors0)_CP` DISABLE KEYS */;
INSERT INTO `cast_num(movies0,actors0)_CP` VALUES ('420',0,0,0,'T',1,2565,0.163743,-1.80946),('3054',0,0,0,'T',2,3310,0.922659,-0.0804956),('4451',0,0,0,'T',4,5328,0.835398,-0.179847),('140',0,0,0,'F',1,140,1.000000,0),('25',0,0,0,'F',3,28,0.892857,-0.113329),('966',0,0,0,'F',4,1478,0.653586,-0.425281),('806',0,0,2,'T',2,2039,0.395292,-0.928131),('210',0,0,2,'T',3,18285,0.011485,-4.46671),('693',0,0,2,'T',4,11454,0.060503,-2.80506),('2',0,0,2,'F',4,27,0.074074,-2.60269),('54',0,0,3,'T',1,3683,0.014662,-4.2225),('2451',0,0,3,'T',2,18463,0.132752,-2.01927),('1439',0,0,3,'T',3,124304,0.011576,-4.45882),('1584',0,0,3,'T',4,63217,0.025057,-3.6866),('123',0,0,3,'F',1,123,1.000000,0),('24',0,0,3,'F',2,39,0.615385,-0.485507),('359',0,0,3,'F',3,550,0.652727,-0.426596),('187',0,0,3,'F',4,6563,0.028493,-3.5581),('21589',0,0,4,'T',2,148554,0.145328,-1.92876),('1961',0,0,4,'T',3,185208,0.010588,-4.54803),('6122',0,0,4,'T',4,38504,0.158996,-1.83888),('84',0,0,4,'F',2,337,0.249258,-1.38927),('1956',0,0,4,'F',3,14796,0.132198,-2.02345),('5428',0,0,4,'F',4,14064,0.385950,-0.952047),('2',0,0,5,'T',4,2,1.000000,0),('45',0,1,1,'T',4,2267,0.019850,-3.91955),('4',0,1,2,'T',3,864,0.004630,-5.3752),('118',0,1,2,'T',4,11508,0.010254,-4.58009),('251',0,1,3,'T',2,6804,0.036890,-3.29981),('281',0,1,3,'T',3,52761,0.005326,-5.23515),('7480',0,1,3,'T',4,168807,0.044311,-3.11652),('99',0,1,3,'F',3,1120,0.088393,-2.42596),('562',0,1,3,'F',4,12437,0.045188,-3.09692),('28',0,1,4,'T',1,2369,0.011819,-4.43805),('6483',0,1,4,'T',2,126001,0.051452,-2.96711),('3379',0,1,4,'T',3,50880,0.066411,-2.71189),('38771',0,1,4,'T',4,153892,0.251936,-1.37858),('2384',0,1,4,'F',1,5038,0.473204,-0.748229),('494',0,1,4,'F',2,16089,0.030704,-3.48336),('3832',0,1,4,'F',3,51751,0.074047,-2.60306),('9650',0,1,4,'F',4,143384,0.067302,-2.69857),('1034',0,1,5,'T',2,23872,0.043314,-3.13928),('552',0,2,2,'T',3,4362,0.126547,-2.06714),('577',0,2,2,'T',4,47198,0.012225,-4.40427),('149',0,2,3,'T',2,12143,0.012270,-4.4006),('21011',0,2,3,'T',3,195726,0.107349,-2.23167),('13975',0,2,3,'T',4,727282,0.019215,-3.95206),('343',0,2,3,'F',3,9295,0.036902,-3.29949),('1083',0,2,3,'F',4,18769,0.057702,-2.85246),('27183',0,2,4,'T',2,284253,0.095630,-2.34727),('17752',0,2,4,'T',3,481015,0.036905,-3.29941),('37886',0,2,4,'T',4,882973,0.042907,-3.14872),('2107',0,2,4,'F',2,8729,0.241379,-1.42139),('3065',0,2,4,'F',3,34859,0.087926,-2.43126),('27247',0,2,4,'F',4,339250,0.080315,-2.5218),('1263',0,2,5,'T',3,3789,0.333333,-1.09861),('40',0,3,1,'T',4,1448,0.027624,-3.58907),('200',0,3,2,'T',3,10167,0.019671,-3.92861),('5087',0,3,2,'T',4,117591,0.043260,-3.14053),('14478',0,3,3,'T',2,31414,0.460877,-0.774624),('8059',0,3,3,'T',3,622391,0.012948,-4.34681),('56696',0,3,3,'T',4,3239282,0.017503,-4.04538),('813',0,3,3,'F',4,23427,0.034704,-3.3609),('209304',0,3,4,'T',2,449293,0.465852,-0.763887),('56759',0,3,4,'T',3,1516266,0.037433,-3.2852),('94374',0,3,4,'T',4,3549311,0.026589,-3.62726),('300',0,3,4,'F',2,2600,0.115385,-2.15948),('650',0,3,4,'F',3,20722,0.031368,-3.46197),('3157',0,3,4,'F',4,73277,0.043083,-3.14463),('2117',0,4,2,'T',4,195136,0.010849,-4.52368),('996',0,4,3,'T',2,2418,0.411911,-0.886948),('21849',0,4,3,'T',3,1369851,0.015950,-4.1383),('175692',0,4,3,'T',4,11932027,0.014724,-4.21828),('3823',0,4,3,'F',4,22086,0.173096,-1.75391),('248295',0,4,4,'T',2,469844,0.528463,-0.637782),('124504',0,4,4,'T',3,4120754,0.030214,-3.49945),('418752',0,4,4,'T',4,13928862,0.030064,-3.50443),('12256',0,4,4,'F',4,251976,0.048640,-3.02331),('17832',0,4,5,'T',3,217907,0.081833,-2.50307),('7037',0,4,5,'T',4,854759,0.008233,-4.7996),('1545',1,0,0,'T',1,2565,0.602339,-0.506935),('160',1,0,0,'T',2,3310,0.048338,-3.02954),('842',1,0,0,'T',4,5328,0.158033,-1.84495),('3',1,0,0,'F',3,28,0.107143,-2.23359),('320',1,0,0,'F',4,1478,0.216509,-1.53012),('3',1,0,1,'T',2,12,0.250000,-1.38629),('2',1,0,1,'T',3,13,0.153846,-1.8718),('17',1,0,1,'T',4,119,0.142857,-1.94591),('59',1,0,2,'T',1,60,0.983333,-0.0168075),('490',1,0,2,'T',2,2039,0.240314,-1.42581),('2139',1,0,2,'T',3,18285,0.116981,-2.14574),('2040',1,0,2,'T',4,11454,0.178104,-1.72539),('10',1,0,2,'F',3,46,0.217391,-1.52606),('2',1,0,2,'F',4,27,0.074074,-2.60269),('2170',1,0,3,'T',1,3683,0.589194,-0.529),('8174',1,0,3,'T',2,18463,0.442723,-0.814811),('27189',1,0,3,'T',3,124304,0.218730,-1.51992),('11228',1,0,3,'T',4,63217,0.177610,-1.72817),('4',1,0,3,'F',2,39,0.102564,-2.27727),('54',1,0,3,'F',3,550,0.098182,-2.32093),('1718',1,0,3,'F',4,6563,0.261771,-1.34029),('3796',1,0,4,'T',1,6901,0.550065,-0.597719),('47786',1,0,4,'T',2,148554,0.321674,-1.13422),('39401',1,0,4,'T',3,185208,0.212739,-1.54769),('3811',1,0,4,'T',4,38504,0.098977,-2.31287),('1190',1,0,4,'F',1,2856,0.416667,-0.875468),('115',1,0,4,'F',2,337,0.341246,-1.07515),('3827',1,0,4,'F',3,14796,0.258651,-1.35228),('1853',1,0,4,'F',4,14064,0.131755,-2.02681),('146',1,1,1,'T',4,2267,0.064402,-2.74261),('164',1,1,2,'T',3,864,0.189815,-1.66171),('817',1,1,2,'T',4,11508,0.070994,-2.64516),('3715',1,1,3,'T',2,6804,0.546002,-0.605133),('7291',1,1,3,'T',3,52761,0.138189,-1.97913),('23714',1,1,3,'T',4,168807,0.140480,-1.96269),('417',1,1,3,'F',3,1120,0.372321,-0.987999),('1112',1,1,3,'F',4,12437,0.089411,-2.41451),('1998',1,1,4,'T',1,2369,0.843394,-0.170321),('38233',1,1,4,'T',2,126001,0.303434,-1.19259),('11236',1,1,4,'T',3,50880,0.220833,-1.51035),('14734',1,1,4,'T',4,153892,0.095742,-2.3461),('1226',1,1,4,'F',1,5038,0.243351,-1.41325),('6425',1,1,4,'F',2,16089,0.399341,-0.91794),('9804',1,1,4,'F',3,51751,0.189446,-1.66365),('14895',1,1,4,'F',4,143384,0.103882,-2.2645),('2482',1,1,5,'T',2,23872,0.103971,-2.26364),('194',1,2,1,'T',4,873,0.222222,-1.50408),('73',1,2,2,'T',3,4362,0.016735,-4.09025),('3380',1,2,2,'T',4,47198,0.071613,-2.63648),('6628',1,2,3,'T',2,12143,0.545829,-0.60545),('36373',1,2,3,'T',3,195726,0.185836,-1.68289),('113592',1,2,3,'T',4,727282,0.156187,-1.8567),('1304',1,2,3,'F',3,9295,0.140290,-1.96404),('4955',1,2,3,'F',4,18769,0.263999,-1.33181),('95729',1,2,4,'T',2,284253,0.336774,-1.08834),('90932',1,2,4,'T',3,481015,0.189042,-1.66579),('96550',1,2,4,'T',4,882973,0.109346,-2.21324),('1505',1,2,4,'F',2,8729,0.172414,-1.75786),('6110',1,2,4,'F',3,34859,0.175278,-1.74138),('40746',1,2,4,'F',4,339250,0.120106,-2.11938),('1050',1,2,5,'T',2,6300,0.166667,-1.79176),('112',1,3,1,'T',4,1448,0.077348,-2.55944),('6',1,3,2,'T',1,6,1.000000,0),('816',1,3,2,'T',3,10167,0.080260,-2.52248),('4763',1,3,2,'T',4,117591,0.040505,-3.20633),('4',1,3,2,'F',4,100,0.040000,-3.21888),('5835',1,3,3,'T',2,31414,0.185745,-1.68338),('105793',1,3,3,'T',3,622391,0.169978,-1.77209),('415498',1,3,3,'T',4,3239282,0.128269,-2.05363),('6009',1,3,3,'F',4,23427,0.256499,-1.36063),('9',1,3,4,'T',1,51,0.176471,-1.7346),('99092',1,3,4,'T',2,449293,0.220551,-1.51163),('227775',1,3,4,'T',3,1516266,0.150221,-1.89565),('353312',1,3,4,'T',4,3549311,0.099544,-2.30716),('500',1,3,4,'F',2,2600,0.192308,-1.64866),('7533',1,3,4,'F',3,20722,0.363527,-1.0119),('11457',1,3,4,'F',4,73277,0.156352,-1.85565),('822',1,3,5,'F',3,10514,0.078181,-2.54873),('342',1,4,1,'T',4,7810,0.043790,-3.12835),('420',1,4,2,'T',3,22742,0.018468,-3.99172),('6017',1,4,2,'T',4,195136,0.030835,-3.4791),('267',1,4,3,'T',2,2418,0.110422,-2.20345),('221697',1,4,3,'T',3,1369851,0.161840,-1.82115),('1413754',1,4,3,'T',4,11932027,0.118484,-2.13298),('1523',1,4,3,'F',3,3046,0.500000,-0.693147),('4013',1,4,3,'F',4,22086,0.181699,-1.7054),('51877',1,4,4,'T',2,469844,0.110413,-2.20353),('447621',1,4,4,'T',3,4120754,0.108626,-2.21984),('1131174',1,4,4,'T',4,13928862,0.081211,-2.5107),('3351',1,4,4,'F',3,41342,0.081056,-2.51261),('24233',1,4,4,'F',4,251976,0.096172,-2.34162),('3948',1,4,5,'T',3,217907,0.018118,-4.01085),('15586',1,4,5,'T',4,854759,0.018234,-4.00447),('1001',1,4,5,'F',3,1001,1.000000,0),('600',2,0,0,'T',1,2565,0.233918,-1.45278),('96',2,0,0,'T',2,3310,0.029003,-3.54036),('35',2,0,0,'T',4,5328,0.006569,-5.02539),('192',2,0,0,'F',4,1478,0.129905,-2.04095),('9',2,0,1,'T',2,12,0.750000,-0.287682),('10',2,0,1,'T',3,13,0.769231,-0.262364),('102',2,0,1,'T',4,119,0.857143,-0.154151),('1',2,0,2,'T',1,60,0.016667,-4.09432),('743',2,0,2,'T',2,2039,0.364394,-1.00952),('6529',2,0,2,'T',3,18285,0.357069,-1.02983),('5720',2,0,2,'T',4,11454,0.499389,-0.69437),('36',2,0,2,'F',3,46,0.782609,-0.245122),('9',2,0,2,'F',4,27,0.333333,-1.09861),('1459',2,0,3,'T',1,3683,0.396144,-0.925977),('7660',2,0,3,'T',2,18463,0.414884,-0.879756),('62749',2,0,3,'T',3,124304,0.504803,-0.683587),('25503',2,0,3,'T',4,63217,0.403420,-0.907777),('11',2,0,3,'F',2,39,0.282051,-1.26567),('115',2,0,3,'F',3,550,0.209091,-1.56499),('3158',2,0,3,'F',4,6563,0.481182,-0.73151),('3105',2,0,4,'T',1,6901,0.449935,-0.798652),('74924',2,0,4,'T',2,148554,0.504355,-0.684475),('96824',2,0,4,'T',3,185208,0.522785,-0.648585),('12418',2,0,4,'T',4,38504,0.322512,-1.13161),('1666',2,0,4,'F',1,2856,0.583333,-0.538997),('137',2,0,4,'F',2,337,0.406528,-0.900102),('6497',2,0,4,'F',3,14796,0.439105,-0.823017),('2087',2,0,4,'F',4,14064,0.148393,-1.90789),('702',2,1,1,'T',4,2267,0.309660,-1.17228),('549',2,1,2,'T',3,864,0.635417,-0.453474),('4104',2,1,2,'T',4,11508,0.356621,-1.03108),('2719',2,1,3,'T',2,6804,0.399618,-0.917246),('17772',2,1,3,'T',3,52761,0.336840,-1.08815),('60117',2,1,3,'T',4,168807,0.356129,-1.03246),('559',2,1,3,'F',3,1120,0.499107,-0.694935),('2886',2,1,3,'F',4,12437,0.232050,-1.4608),('343',2,1,4,'T',1,2369,0.144787,-1.93249),('73158',2,1,4,'T',2,126001,0.580614,-0.543669),('23981',2,1,4,'T',3,50880,0.471325,-0.752207),('34357',2,1,4,'T',4,153892,0.223254,-1.49945),('1428',2,1,4,'F',1,5038,0.283446,-1.26073),('9169',2,1,4,'F',2,16089,0.569892,-0.562308),('22352',2,1,4,'F',3,51751,0.431914,-0.839529),('37631',2,1,4,'F',4,143384,0.262449,-1.3377),('13795',2,1,5,'T',2,23872,0.577874,-0.548399),('485',2,2,1,'T',4,873,0.555556,-0.587786),('527',2,2,2,'T',2,527,1.000000,0),('2375',2,2,2,'T',3,4362,0.544475,-0.607933),('15881',2,2,2,'T',4,47198,0.336476,-1.08923),('3506',2,2,3,'T',2,12143,0.288726,-1.24228),('76971',2,2,3,'T',3,195726,0.393259,-0.933287),('277094',2,2,3,'T',4,727282,0.380999,-0.964959),('4784',2,2,3,'F',3,9295,0.514685,-0.6642),('7012',2,2,3,'F',4,18769,0.373595,-0.984583),('145119',2,2,4,'T',2,284253,0.510528,-0.67231),('226816',2,2,4,'T',3,481015,0.471536,-0.75176),('301494',2,2,4,'T',4,882973,0.341453,-1.07455),('3913',2,2,4,'F',2,8729,0.448276,-0.802346),('13334',2,2,4,'F',3,34859,0.382512,-0.960995),('114513',2,2,4,'F',4,339250,0.337548,-1.08605),('5250',2,2,5,'T',2,6300,0.833333,-0.182322),('2526',2,2,5,'T',3,3789,0.666667,-0.405465),('456',2,3,1,'T',4,1448,0.314917,-1.15545),('3432',2,3,2,'T',3,10167,0.337563,-1.086),('39757',2,3,2,'T',4,117591,0.338096,-1.08443),('75',2,3,2,'F',4,100,0.750000,-0.287682),('8575',2,3,3,'T',2,31414,0.272967,-1.2984),('245303',2,3,3,'T',3,622391,0.394130,-0.931075),('1145712',2,3,3,'T',4,3239282,0.353693,-1.03933),('522',2,3,3,'F',3,522,1.000000,0),('9305',2,3,3,'F',4,23427,0.397191,-0.923338),('42',2,3,4,'T',1,51,0.823529,-0.194157),('124441',2,3,4,'T',2,449293,0.276971,-1.28384),('631596',2,3,4,'T',3,1516266,0.416547,-0.875756),('1069304',2,3,4,'T',4,3549311,0.301271,-1.19975),('1500',2,3,4,'F',2,2600,0.576923,-0.550046),('11717',2,3,4,'F',3,20722,0.565438,-0.570155),('32736',2,3,4,'F',4,73277,0.446743,-0.805772),('1880',2,3,5,'T',2,1880,1.000000,0),('789',2,3,5,'T',3,789,1.000000,0),('1280',2,3,5,'T',4,5760,0.222222,-1.50408),('9692',2,3,5,'F',3,10514,0.921819,-0.0814064),('2930',2,4,1,'T',4,7810,0.375160,-0.980403),('4921',2,4,2,'T',3,22742,0.216384,-1.5307),('38907',2,4,2,'T',4,195136,0.199384,-1.61252),('1155',2,4,3,'T',2,2418,0.477667,-0.738841),('481695',2,4,3,'T',3,1369851,0.351640,-1.04515),('3829746',2,4,3,'T',4,11932027,0.320964,-1.13643),('1349',2,4,3,'F',3,3046,0.442876,-0.814465),('10087',2,4,3,'F',4,22086,0.456715,-0.783696),('89516',2,4,4,'T',2,469844,0.190523,-1.65798),('1341845',2,4,4,'T',3,4120754,0.325631,-1.12199),('3458915',2,4,4,'T',4,13928862,0.248327,-1.39301),('16276',2,4,4,'F',3,41342,0.393692,-0.932186),('70903',2,4,4,'F',4,251976,0.281388,-1.26802),('81947',2,4,5,'T',3,217907,0.376064,-0.977996),('148045',2,4,5,'T',4,854759,0.173201,-1.7533),('1',3,0,1,'T',3,13,0.076923,-2.56495),('9407',3,0,2,'T',3,18285,0.514465,-0.664628),('3001',3,0,2,'T',4,11454,0.262005,-1.33939),('14',3,0,2,'F',4,27,0.518519,-0.656779),('178',3,0,3,'T',2,18463,0.009641,-4.64173),('32927',3,0,3,'T',3,124304,0.264891,-1.32844),('24902',3,0,3,'T',4,63217,0.393913,-0.931625),('22',3,0,3,'F',3,550,0.040000,-3.21888),('1500',3,0,3,'F',4,6563,0.228554,-1.47598),('4255',3,0,4,'T',2,148554,0.028643,-3.55285),('47022',3,0,4,'T',3,185208,0.253888,-1.37086),('16153',3,0,4,'T',4,38504,0.419515,-0.868656),('1',3,0,4,'F',2,337,0.002967,-5.8202),('2516',3,0,4,'F',3,14796,0.170046,-1.77169),('4696',3,0,4,'F',4,14064,0.333902,-1.09691),('1374',3,1,1,'T',4,2267,0.606087,-0.500732),('147',3,1,2,'T',3,864,0.170139,-1.77114),('6469',3,1,2,'T',4,11508,0.562131,-0.57602),('11',3,1,2,'F',3,11,1.000000,0),('119',3,1,3,'T',2,6804,0.017490,-4.04613),('27417',3,1,3,'T',3,52761,0.519645,-0.654609),('77496',3,1,3,'T',4,168807,0.459080,-0.778531),('45',3,1,3,'F',3,1120,0.040179,-3.21441),('7877',3,1,3,'F',4,12437,0.633352,-0.456729),('8127',3,1,4,'T',2,126001,0.064499,-2.74111),('12284',3,1,4,'T',3,50880,0.241431,-1.42117),('66030',3,1,4,'T',4,153892,0.429067,-0.846142),('1',3,1,4,'F',2,16089,0.000062,-9.68838),('15763',3,1,4,'F',3,51751,0.304593,-1.18878),('81208',3,1,4,'F',4,143384,0.566367,-0.568513),('6561',3,1,5,'T',2,23872,0.274841,-1.29156),('194',3,2,1,'T',4,873,0.222222,-1.50408),('1362',3,2,2,'T',3,4362,0.312242,-1.16398),('27360',3,2,2,'T',4,47198,0.579686,-0.545269),('242',3,2,2,'F',3,242,1.000000,0),('129',3,2,2,'F',4,129,1.000000,0),('1860',3,2,3,'T',2,12143,0.153175,-1.87617),('61371',3,2,3,'T',3,195726,0.313556,-1.15978),('322621',3,2,3,'T',4,727282,0.443598,-0.812837),('301',3,2,3,'F',2,301,1.000000,0),('2864',3,2,3,'F',3,9295,0.308123,-1.17726),('5719',3,2,3,'F',4,18769,0.304705,-1.18841),('16222',3,2,4,'T',2,284253,0.057069,-2.86349),('145515',3,2,4,'T',3,481015,0.302517,-1.19562),('447043',3,2,4,'T',4,882973,0.506293,-0.68064),('1204',3,2,4,'F',2,8729,0.137931,-1.981),('12350',3,2,4,'F',3,34859,0.354284,-1.03766),('156744',3,2,4,'F',4,339250,0.462031,-0.772123),('840',3,3,1,'T',4,1448,0.580110,-0.544538),('5719',3,3,2,'T',3,10167,0.562506,-0.575353),('67984',3,3,2,'T',4,117591,0.578139,-0.547941),('21',3,3,2,'F',4,100,0.210000,-1.56065),('2526',3,3,3,'T',2,31414,0.080410,-2.52062),('263236',3,3,3,'T',3,622391,0.422943,-0.860518),('1621376',3,3,3,'T',4,3239282,0.500536,-0.692076),('7300',3,3,3,'F',4,23427,0.311606,-1.16602),('16456',3,3,4,'T',2,449293,0.036626,-3.307),('600136',3,3,4,'T',3,1516266,0.395799,-0.926849),('2032321',3,3,4,'T',4,3549311,0.572596,-0.557575),('300',3,3,4,'F',2,2600,0.115385,-2.15948),('822',3,3,4,'F',3,20722,0.039668,-3.22721),('25927',3,3,4,'F',4,73277,0.353822,-1.03896),('4480',3,3,5,'T',4,5760,0.777778,-0.251314),('4538',3,4,1,'T',4,7810,0.581050,-0.542919),('17401',3,4,2,'T',3,22742,0.765148,-0.267686),('148095',3,4,2,'T',4,195136,0.758932,-0.275843),('644610',3,4,3,'T',3,1369851,0.470569,-0.753813),('6512835',3,4,3,'T',4,11932027,0.545828,-0.605451),('174',3,4,3,'F',3,3046,0.057124,-2.86253),('4163',3,4,3,'F',4,22086,0.188490,-1.66871),('80156',3,4,4,'T',2,469844,0.170601,-1.76843),('2206784',3,4,4,'T',3,4120754,0.535529,-0.6245),('8920021',3,4,4,'T',4,13928862,0.640398,-0.445665),('21715',3,4,4,'F',3,41342,0.525253,-0.643875),('144584',3,4,4,'F',4,251976,0.573801,-0.555473),('114180',3,4,5,'T',3,217907,0.523985,-0.646292),('684091',3,4,5,'T',4,854759,0.800332,-0.222729);
/*!40000 ALTER TABLE `cast_num(movies0,actors0)_CP` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-30 16:38:56
