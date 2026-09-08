-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: mall
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `oms_cart`
--

DROP TABLE IF EXISTS `oms_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `amount` int DEFAULT '1',
  `is_selected` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_cart`
--

LOCK TABLES `oms_cart` WRITE;
/*!40000 ALTER TABLE `oms_cart` DISABLE KEYS */;
INSERT INTO `oms_cart` VALUES (3,3,1,3,1,'2026-06-15 06:08:45','2026-06-15 06:08:45',0);
/*!40000 ALTER TABLE `oms_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_order`
--

DROP TABLE IF EXISTS `oms_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `seckill_session_id` int DEFAULT NULL COMMENT '秒杀场次ID（非秒杀订单为NULL）',
  `address_id` int DEFAULT NULL COMMENT '收货地址ID',
  `order_sn` varchar(64) NOT NULL COMMENT '订单编号',
  `payment_sn` varchar(64) DEFAULT '' COMMENT '支付单号',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `pay_method` int DEFAULT '0' COMMENT '支付方式（0=未选择，1=微信，2=支付宝）',
  `express_delivery` tinyint DEFAULT '0' COMMENT '快递公司（0=顺丰，1=百世，2=圆通，3=中通）',
  `status` int DEFAULT '0' COMMENT '订单状态（0=待支付，1=已支付，2=已发货，3=已收货，4=已取消）',
  `recipient_name` varchar(32) DEFAULT '' COMMENT '收件人姓名',
  `recipient_phone` varchar(20) DEFAULT '' COMMENT '收件人电话',
  `recipient_address` varchar(255) DEFAULT '' COMMENT '收件人地址',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receipt_time` datetime DEFAULT NULL COMMENT '收货时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除（0=正常，1=已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_seckill` (`user_id`,`seckill_session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_order`
--

LOCK TABLES `oms_order` WRITE;
/*!40000 ALTER TABLE `oms_order` DISABLE KEYS */;
INSERT INTO `oms_order` VALUES (9,1,NULL,NULL,'2026061514355116863488','PAY17815053707007650',89.00,0,0,1,'','','','2026-06-15 14:36:11',NULL,NULL,'2026-06-15 06:35:51','2026-06-15 06:36:11',1),(10,1,NULL,NULL,'2026061514415004870912','PAY17815057108203216',89.00,0,0,3,'','','','2026-06-15 14:41:51','2026-07-02 21:15:43','2026-07-02 21:16:01','2026-06-15 06:41:50','2026-06-15 06:41:50',0),(13,1,6,NULL,'2026070221143038424832','PAY17829980927928002',100.00,3,2,1,'','','','2026-07-02 21:14:53',NULL,NULL,'2026-07-02 21:14:30','2026-07-02 21:14:30',0),(14,2,NULL,2,'2026090516162414206208','PAY17885962992919558',168.00,2,0,1,'测试用户','13900000000','广东省广州市天河区演示路 1 号','2026-09-05 16:18:19',NULL,NULL,'2026-09-05 16:16:24','2026-09-05 16:16:24',0);
/*!40000 ALTER TABLE `oms_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oms_order_item`
--

DROP TABLE IF EXISTS `oms_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oms_order_item` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL COMMENT '商品名称（下单时快照）',
  `product_img` varchar(512) DEFAULT '' COMMENT '商品图片',
  `product_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品成交单价',
  `amount` int DEFAULT '1' COMMENT '购买数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除（0=正常，1=已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oms_order_item`
--

LOCK TABLES `oms_order_item` WRITE;
/*!40000 ALTER TABLE `oms_order_item` DISABLE KEYS */;
INSERT INTO `oms_order_item` VALUES (9,9,1,'绿植花束装饰','0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg',89.00,1,'2026-06-15 06:35:52','2026-06-15 06:35:52',0),(10,10,1,'绿植花束装饰','0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg',89.00,1,'2026-06-15 06:41:50','2026-06-15 06:41:50',0),(13,13,34,'克鲁克斯辐射计','000d3b6a-1838-4872-a9a1-1a8e186c7b02.jpg',100.00,1,'2026-07-02 21:14:30','2026-07-02 21:14:30',0),(14,14,1,'绿植花束装饰','0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg',89.00,1,'2026-09-05 16:16:24','2026-09-05 16:16:24',0),(15,14,8,'北欧风陶瓷花瓶','20532cfe-4ea9-43ae-99a1-086af3c608f7.jpg',79.00,1,'2026-09-05 16:16:24','2026-09-05 16:16:24',0);
/*!40000 ALTER TABLE `oms_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_category`
--

DROP TABLE IF EXISTS `pms_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `sort` int DEFAULT '0',
  `icon` varchar(255) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_category`
--

LOCK TABLES `pms_category` WRITE;
/*!40000 ALTER TABLE `pms_category` DISABLE KEYS */;
INSERT INTO `pms_category` VALUES (1,0,'仿真花/干花',1,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(2,0,'花瓶花器',2,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(3,0,'靠垫抱枕',3,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(4,0,'桌布家纺',4,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(5,0,'家居摆件',5,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(6,0,'香薰用品',6,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(7,0,'置物收纳',7,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(8,0,'装饰壁饰',8,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(9,0,'杯具餐具',9,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(10,0,'创意家居',10,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(11,1,'绿植花束',1,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(12,1,'仿真兰花',2,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(13,1,'牡丹仿真花',3,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(14,2,'陶瓷花瓶',4,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(15,2,'素烧花器',5,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(16,3,'三角靠垫',6,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(17,3,'纯色抱枕',7,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(18,4,'棉麻桌布',8,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(19,4,'条纹餐桌布',9,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(20,5,'驯鹿摆件',10,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(21,5,'波点苹果',11,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(22,6,'香薰机',12,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(23,6,'铜香炉',13,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(24,7,'壁挂置物架',14,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(25,7,'铁艺置物架',15,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(26,8,'立体装饰画',16,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(27,8,'山水壁饰',17,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(28,9,'玻璃杯套装',18,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(29,9,'碗碟套装',19,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0),(30,10,'铁艺小圆桌',20,'','2026-06-08 05:28:41','2026-06-08 06:07:08',0);
/*!40000 ALTER TABLE `pms_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_product`
--

DROP TABLE IF EXISTS `pms_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_product` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(128) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品原价',
  `stock` int DEFAULT '0' COMMENT '商品库存（秒杀下单时扣减此字段）',
  `detail` text COMMENT '商品详情',
  `category_id` int DEFAULT '0' COMMENT '分类ID',
  `status` int DEFAULT '1' COMMENT '商品状态（0=下架，1=上架）',
  `version` int DEFAULT '1' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除（0=正常，1=已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_product`
--

LOCK TABLES `pms_product` WRITE;
/*!40000 ALTER TABLE `pms_product` DISABLE KEYS */;
INSERT INTO `pms_product` VALUES (1,'绿植花束装饰',89.00,97,'清新绿植花束，为家居增添自然气息',11,1,1,'2026-06-08 05:29:30','2026-09-05 16:16:24',0),(2,'仿真兰花盆栽',128.00,80,'高仿真兰花，优雅绽放四季如春',12,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(3,'牡丹仿真花摆件',158.00,60,'富贵牡丹仿真花，客厅卧室皆宜',13,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(4,'仿真马蹄莲插花',99.00,120,'马蹄莲仿真插花，简约现代风格',11,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(5,'仿真文心兰花艺',119.00,90,'文心兰仿真花艺，温馨浪漫',11,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(6,'干花束装饰',59.00,150,'自然干花束，持久保存',11,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(7,'仿真花摆件套装',139.00,70,'多款仿真花组合，一摆即美',11,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(8,'北欧风陶瓷花瓶',79.00,49,'简约北欧设计，哑光釉面',14,1,1,'2026-06-08 05:29:30','2026-09-05 16:16:24',0),(9,'素烧花器花瓶',69.00,50,'素烧工艺，质朴自然',15,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(10,'壁挂花器',89.00,50,'创意壁挂，节省空间',14,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(11,'哆啦A梦三角靠垫',99.00,80,'可爱造型，舒适支撑',16,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(12,'ins风纯色抱枕',59.00,100,'简约纯色，百搭家居',17,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(13,'装饰花卉抱枕',69.00,90,'花卉图案，点缀沙发',17,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(14,'蕾丝边棉麻桌布',89.00,60,'精致蕾丝边，优雅餐桌',18,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(15,'条纹餐桌布',79.00,70,'经典条纹，简约大方',19,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(16,'格子图案桌布',69.00,80,'田园风格，温馨用餐',18,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(17,'民族风印花桌布',99.00,50,'民族风图案，个性装饰',18,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(18,'情侣驯鹿摆件',129.00,40,'可爱驯鹿，甜蜜装饰',20,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(19,'波点苹果摆件',89.00,60,'波点设计，趣味苹果造型',21,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(20,'水手造型摆件',108.00,45,'水手风格，航海主题',20,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(21,'鱼形装饰摆件',79.00,70,'创意鱼形，海洋风情',20,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(22,'大象吉祥摆件',138.00,35,'吉祥大象，招财纳福',20,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(23,'超声波香薰机',159.00,30,'静音加湿，香薰氛围',22,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(24,'复古铜香炉',189.00,25,'复古铜制，禅意生活',23,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(25,'简约壁挂置物架',59.00,80,'壁挂收纳，节省空间',24,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(26,'创意铁艺置物架',139.00,40,'铁艺设计，工业风格',25,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(27,'自行车立体装饰画',128.00,35,'立体效果，创意墙面',26,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(28,'铁艺山水壁饰',168.00,30,'铁艺山水，中式韵味',27,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(29,'动物系列装饰画',89.00,50,'可爱动物，童趣装饰',26,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(30,'孔雀挂钟照片墙',199.00,20,'孔雀造型，实用装饰',26,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(31,'ins风玻璃杯套装',69.00,100,'简约玻璃杯，夏日清凉',28,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(32,'日式玻璃碗碟套装',159.00,40,'日式风格，精致餐具',29,1,1,'2026-06-08 05:29:30','2026-06-08 06:07:08',0),(33,'欧式铁艺小圆桌',358.00,15,'欧式铁艺，阳台小桌',30,1,1,'2026-06-08 05:29:30','2026-07-02 12:57:07',0),(34,'克鲁克斯辐射计',139.00,24,'物理原理，科技装饰',30,1,1,'2026-06-08 05:29:30','2026-07-02 21:14:30',0),(35,'狗狗造型书挡',99.00,60,'可爱狗狗，实用书挡',30,1,1,'2026-06-08 05:29:30','2026-07-02 12:55:30',0);
/*!40000 ALTER TABLE `pms_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pms_product_img`
--

DROP TABLE IF EXISTS `pms_product_img`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pms_product_img` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `url` varchar(512) NOT NULL,
  `sort` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pms_product_img`
--

LOCK TABLES `pms_product_img` WRITE;
/*!40000 ALTER TABLE `pms_product_img` DISABLE KEYS */;
INSERT INTO `pms_product_img` VALUES (1,1,'0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg',0,'2026-06-08 05:29:30',0),(2,2,'50acedf1-418a-4693-8a60-37e342ca37a8.jpg',0,'2026-06-08 05:29:30',0),(3,3,'8bc1c95a-4d17-4f09-9f11-274810cc8463.jpg',0,'2026-06-08 05:29:30',0),(4,4,'b82d3ad1-37ee-4f1d-80eb-70a32363d068.jpg',0,'2026-06-08 05:29:30',0),(5,5,'acfa5218-1029-4150-b2e7-0ef8dff649cd.jpg',0,'2026-06-08 05:29:30',0),(6,6,'79577f8c-8238-478d-8a16-0da8a5ace94d.jpg',0,'2026-06-08 05:29:30',0),(7,7,'adc97f36-8d7b-4711-888f-e76931e352ac.jpg',0,'2026-06-08 05:29:30',0),(8,8,'20532cfe-4ea9-43ae-99a1-086af3c608f7.jpg',0,'2026-06-08 05:29:30',0),(9,9,'7ab8075d-66ab-4e52-952e-e8f679716c66.jpg',0,'2026-06-08 05:29:30',0),(10,10,'7ffb2f80-6e8f-4d19-b128-d149f6766614.jpg',0,'2026-06-08 05:29:30',0),(11,11,'47d1326e-4637-48f7-9a72-df6a56bd1479.jpg',0,'2026-06-08 05:29:30',0),(12,12,'505051fd-5698-4243-be84-56e89759894f.jpg',0,'2026-06-08 05:29:30',0),(13,13,'714389c0-960d-4611-91d1-6e8ee74a0906.jpg',0,'2026-06-08 05:29:30',0),(14,14,'04b61480-e1db-4d30-af26-a7ca2d24166b.jpg',0,'2026-06-08 05:29:30',0),(15,15,'44b1cb71-c93a-42d5-ab42-406af96b8f13.jpg',0,'2026-06-08 05:29:30',0),(16,16,'7fceb9ee-0c56-42ab-847d-7d52375870f6.jpg',0,'2026-06-08 05:29:30',0),(17,17,'9275d728-1a34-45df-b3b5-594a1421dc1c.jpg',0,'2026-06-08 05:29:30',0),(18,18,'2010164d-bbd7-4e47-86e1-06ad046f5c1d.jpg',0,'2026-06-08 05:29:30',0),(19,19,'a5190494-289b-4796-b748-496846267a25.jpg',0,'2026-06-08 05:29:30',0),(20,20,'b22ea160-342a-4161-bcf6-c5660b6b4a42.jpg',0,'2026-06-08 05:29:30',0),(21,21,'deec894d-c99a-4281-99f5-64a346a444e8.jpg',0,'2026-06-08 05:29:30',0),(22,22,'e984f2c8-aef8-4e23-9d16-db38bf9dba9f.jpg',0,'2026-06-08 05:29:30',0),(23,23,'083d3b3a-4290-49e4-bd0d-ab166616c876.jpg',0,'2026-06-08 05:29:30',0),(24,24,'317d44ba-7a5a-43da-a9ee-4bfee7d5d758.jpg',0,'2026-06-08 05:29:30',0),(25,25,'03b87769-2fb9-4aa6-91fe-6ace5af714ad.jpg',0,'2026-06-08 05:29:30',0),(26,26,'6a2ebed1-2b48-4eb6-a378-d56d000acd0a.jpg',0,'2026-06-08 05:29:30',0),(27,27,'27804fd9-02b4-4752-b895-4bd56d81136a.jpg',0,'2026-06-08 05:29:30',0),(28,28,'52aaab34-37ac-492b-b153-ffc49981c0f7.jpg',0,'2026-06-08 05:29:30',0),(29,29,'aa4228d1-25bf-499e-8130-beb8be2217be.jpg',0,'2026-06-08 05:29:30',0),(30,30,'6ef68b42-b89e-4ae7-808a-76c4b6d5eb13.jpg',0,'2026-06-08 05:29:30',0),(31,31,'32c21631-c0b8-4d71-9603-33592eb4706e.jpg',0,'2026-06-08 05:29:30',0),(32,32,'87005616-2e08-4c3b-ba8e-79dcdc42cbed.jpg',0,'2026-06-08 05:29:30',0),(33,33,'02df9e72-6082-4d39-8da2-92f232cec7fe.jpg',0,'2026-06-08 05:29:30',0),(34,34,'000d3b6a-1838-4872-a9a1-1a8e186c7b02.jpg',0,'2026-06-08 05:29:30',0),(35,35,'d27e4948-c07e-4072-9066-c8abeb2e2f20.jpg',0,'2026-06-08 05:29:30',0);
/*!40000 ALTER TABLE `pms_product_img` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_seckill_session`
--

DROP TABLE IF EXISTS `sms_seckill_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_seckill_session` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `seckill_price` decimal(10,2) DEFAULT '0.00' COMMENT '秒杀价格',
  `seckill_stock` int DEFAULT '0' COMMENT '秒杀库存（加载至Redis作为初始库存）',
  `start_time` datetime NOT NULL COMMENT '秒杀开始时间',
  `end_time` datetime NOT NULL COMMENT '秒杀结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除（0=正常，1=已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_seckill_session`
--

LOCK TABLES `sms_seckill_session` WRITE;
/*!40000 ALTER TABLE `sms_seckill_session` DISABLE KEYS */;
INSERT INTO `sms_seckill_session` VALUES (1,1,29.90,100,'2026-06-15 06:40:30','2026-06-15 08:40:30','2026-06-15 06:40:30','2026-06-15 06:40:30',0),(2,5,39.90,50,'2026-06-15 07:40:30','2026-06-15 09:40:30','2026-06-15 06:40:30','2026-06-15 06:40:30',0),(3,33,230.00,100,'2026-07-01 00:00:00','2026-07-04 00:00:00','2026-07-02 12:53:30','2026-07-02 12:53:30',0),(4,35,20.00,100,'2026-07-01 00:00:00','2026-07-03 00:00:00','2026-07-02 12:54:05','2026-07-02 12:54:05',0),(5,31,68.99,100,'2026-07-01 00:00:00','2026-07-03 00:00:00','2026-07-02 12:54:05','2026-07-02 12:54:05',0),(6,34,100.00,100,'2026-07-01 00:00:00','2026-07-03 00:00:00','2026-07-02 12:54:05','2026-07-02 12:54:05',0),(7,1,19.90,100,'2026-09-06 10:00:00','2026-09-06 22:00:00','2026-09-05 16:02:46','2026-09-05 16:02:46',0),(8,5,39.90,50,'2026-09-08 10:00:00','2026-09-08 22:00:00','2026-09-05 16:02:46','2026-09-05 16:02:46',0);
/*!40000 ALTER TABLE `sms_seckill_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `gender` int DEFAULT '0',
  `address` varchar(255) DEFAULT '',
  `pwd` varchar(255) NOT NULL,
  `avatar` varchar(255) DEFAULT '',
  `birthday` date DEFAULT NULL,
  `phone` varchar(20) DEFAULT '',
  `email` varchar(64) DEFAULT '',
  `remark` text,
  `role` int DEFAULT '0',
  `status` int DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','管理员',0,'广州','$2a$10$lLLBS06gXc.vGYeQto8SEOMciEOY16lfu8o41PMWD12RUpWFpBqwe','',NULL,'13800000000','admin@mall.com',NULL,1,1,'2026-06-08 05:28:41','2026-09-05 16:13:56',0),(2,'user','测试用户',0,'广东省广州市天河区演示路 1 号','$2a$10$lLLBS06gXc.vGYeQto8SEOMciEOY16lfu8o41PMWD12RUpWFpBqwe','',NULL,'13900000000','user@mall.com',NULL,0,1,'2026-06-08 05:28:41','2026-09-05 16:13:56',0),(3,'testuser_qa','testuser_qa',0,'','$2a$10$6MnUUbgScAKQUNbbHDEz.eYCUXWNmgEqMgPFwR5zwWFG8BXg1SsJ6','',NULL,'13800138000','',NULL,0,1,'2026-06-15 06:05:00','2026-06-15 06:05:00',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-07  4:39:05
