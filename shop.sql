/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3306
 Source Schema         : shop

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 14/05/2021 23:13:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for shop_cart
-- ----------------------------
DROP TABLE IF EXISTS `shop_cart`;
CREATE TABLE `shop_cart`  (
  `cart_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '购物车id',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `product_id` int(11) NULL DEFAULT NULL COMMENT '商品id',
  `product_amount` int(11) NULL DEFAULT 0 COMMENT '商品数量',
  PRIMARY KEY (`cart_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_cart
-- ----------------------------

-- ----------------------------
-- Table structure for shop_category
-- ----------------------------
DROP TABLE IF EXISTS `shop_category`;
CREATE TABLE `shop_category`  (
  `category_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `category_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `category_parentId` int(11) NULL DEFAULT 0 COMMENT '当父分类为0时，代表根分类',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品的分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_category
-- ----------------------------
INSERT INTO `shop_category` VALUES (7, '装饰摆件', 0);
INSERT INTO `shop_category` VALUES (8, '创意家居', 7);
INSERT INTO `shop_category` VALUES (9, '生活雕塑', 7);
INSERT INTO `shop_category` VALUES (10, '布艺软饰', 0);
INSERT INTO `shop_category` VALUES (11, '时尚抱枕', 10);
INSERT INTO `shop_category` VALUES (12, '田园桌布', 10);
INSERT INTO `shop_category` VALUES (13, '花与花瓶', 0);
INSERT INTO `shop_category` VALUES (14, '干花花艺', 13);
INSERT INTO `shop_category` VALUES (16, '花瓶花器', 13);
INSERT INTO `shop_category` VALUES (17, '蜡艺香薰', 0);
INSERT INTO `shop_category` VALUES (18, '复古香炉', 17);
INSERT INTO `shop_category` VALUES (20, '香薰精油', 17);
INSERT INTO `shop_category` VALUES (21, '墙饰壁挂', 0);
INSERT INTO `shop_category` VALUES (22, '简约现代', 21);
INSERT INTO `shop_category` VALUES (23, '浪漫美式', 21);
INSERT INTO `shop_category` VALUES (24, '浓情欧式', 21);

-- ----------------------------
-- Table structure for shop_image
-- ----------------------------
DROP TABLE IF EXISTS `shop_image`;
CREATE TABLE `shop_image`  (
  `image_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '图片id',
  `image_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片路径',
  `product_id` int(11) NULL DEFAULT 0 COMMENT '商品id',
  PRIMARY KEY (`image_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品的图片' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_image
-- ----------------------------
INSERT INTO `shop_image` VALUES (52, '4a942eee-3591-4a36-8104-1d056735b1a6.jpg', 44);
INSERT INTO `shop_image` VALUES (53, 'bc094143-0c42-42e4-85b7-e07d8b9a4ac2.jpg', 45);
INSERT INTO `shop_image` VALUES (54, '47d1326e-4637-48f7-9a72-df6a56bd1479.jpg', 45);
INSERT INTO `shop_image` VALUES (55, 'a4dbe409-61c5-4065-88b9-7c7f1d5aa702.jpg', 45);
INSERT INTO `shop_image` VALUES (56, '714389c0-960d-4611-91d1-6e8ee74a0906.jpg', 46);
INSERT INTO `shop_image` VALUES (57, '505051fd-5698-4243-be84-56e89759894f.jpg', 46);
INSERT INTO `shop_image` VALUES (58, 'd5fa8332-6a23-4545-b40f-966938161dfd.jpg', 46);
INSERT INTO `shop_image` VALUES (59, '7fceb9ee-0c56-42ab-847d-7d52375870f6.jpg', 47);
INSERT INTO `shop_image` VALUES (60, 'aacd64d4-dc4e-497e-b859-f440dd0f5cc2.jpg', 47);
INSERT INTO `shop_image` VALUES (61, '04b61480-e1db-4d30-af26-a7ca2d24166b.jpg', 48);
INSERT INTO `shop_image` VALUES (62, '02df9e72-6082-4d39-8da2-92f232cec7fe.jpg', 48);
INSERT INTO `shop_image` VALUES (63, 'fd2c38f7-9c43-48dc-8d39-ab4d3d88640a.jpg', 48);
INSERT INTO `shop_image` VALUES (64, '44b1cb71-c93a-42d5-ab42-406af96b8f13.jpg', 49);
INSERT INTO `shop_image` VALUES (65, '9275d728-1a34-45df-b3b5-594a1421dc1c.jpg', 49);
INSERT INTO `shop_image` VALUES (66, '0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg', 50);
INSERT INTO `shop_image` VALUES (67, '6f0b9cca-1840-4a42-86ff-7f44d239b90b.jpg', 50);
INSERT INTO `shop_image` VALUES (68, '79577f8c-8238-478d-8a16-0da8a5ace94d.jpg', 50);
INSERT INTO `shop_image` VALUES (69, '7ffb2f80-6e8f-4d19-b128-d149f6766614.jpg', 51);
INSERT INTO `shop_image` VALUES (70, '2a8db663-27d5-4ae8-b890-ca82979453ab.jpg', 51);
INSERT INTO `shop_image` VALUES (71, 'b82d3ad1-37ee-4f1d-80eb-70a32363d068.jpg', 51);
INSERT INTO `shop_image` VALUES (72, 'be1e3972-25f2-403c-a156-8c2261044ea0.jpg', 52);
INSERT INTO `shop_image` VALUES (73, '8b90f15b-5d17-433a-92d5-39ddbb668053.jpg', 52);
INSERT INTO `shop_image` VALUES (77, 'bd3ce2c2-efa9-4548-81db-6c8299c1285f.jpg', 53);
INSERT INTO `shop_image` VALUES (78, '7ab8075d-66ab-4e52-952e-e8f679716c66.jpg', 53);
INSERT INTO `shop_image` VALUES (79, '27804fd9-02b4-4752-b895-4bd56d81136a.jpg', 53);
INSERT INTO `shop_image` VALUES (80, '20532cfe-4ea9-43ae-99a1-086af3c608f7.jpg', 54);
INSERT INTO `shop_image` VALUES (81, '52aaab34-37ac-492b-b153-ffc49981c0f7.jpg', 54);
INSERT INTO `shop_image` VALUES (82, 'cf3ba37c-6f32-42a2-9595-97e26edf289e.jpg', 54);
INSERT INTO `shop_image` VALUES (83, 'deec894d-c99a-4281-99f5-64a346a444e8.jpg', 55);
INSERT INTO `shop_image` VALUES (84, '32c21631-c0b8-4d71-9603-33592eb4706e.jpg', 56);
INSERT INTO `shop_image` VALUES (85, '87005616-2e08-4c3b-ba8e-79dcdc42cbed.jpg', 56);
INSERT INTO `shop_image` VALUES (92, 'aa4228d1-25bf-499e-8130-beb8be2217be.jpg', 60);
INSERT INTO `shop_image` VALUES (93, 'ff1e4a11-115d-4680-abc0-77b44fe58c0f.jpg', 61);
INSERT INTO `shop_image` VALUES (94, 'd27e4948-c07e-4072-9066-c8abeb2e2f20.jpg', 62);
INSERT INTO `shop_image` VALUES (95, '7d790227-d868-4276-a2d0-1fc088c53430.jpg', 62);
INSERT INTO `shop_image` VALUES (96, 'e984f2c8-aef8-4e23-9d16-db38bf9dba9f.jpg', 62);
INSERT INTO `shop_image` VALUES (97, '000d3b6a-1838-4872-a9a1-1a8e186c7b02.jpg', 63);
INSERT INTO `shop_image` VALUES (98, '6a2ebed1-2b48-4eb6-a378-d56d000acd0a.jpg', 63);
INSERT INTO `shop_image` VALUES (99, 'd00d29c4-b742-45ae-960c-aaa6b10c45bb.jpg', 64);
INSERT INTO `shop_image` VALUES (100, '083d3b3a-4290-49e4-bd0d-ab166616c876.jpg', 64);
INSERT INTO `shop_image` VALUES (101, 'ee398cbb-c787-4f7b-ace5-f3b6cfcfdcf2.jpg', 65);
INSERT INTO `shop_image` VALUES (102, '317d44ba-7a5a-43da-a9ee-4bfee7d5d758.jpg', 65);
INSERT INTO `shop_image` VALUES (103, 'b22ea160-342a-4161-bcf6-c5660b6b4a42.jpg', 66);
INSERT INTO `shop_image` VALUES (104, '2010164d-bbd7-4e47-86e1-06ad046f5c1d.jpg', 66);

-- ----------------------------
-- Table structure for shop_order
-- ----------------------------
DROP TABLE IF EXISTS `shop_order`;
CREATE TABLE `shop_order`  (
  `order_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `order_num` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `total_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总金额',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '订单创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '订单更新时间',
  `pay_method` smallint(6) NULL DEFAULT 0 COMMENT '0：支付宝，1：微信，2：银联，3：货到付款',
  `express_delivery` smallint(6) NULL DEFAULT 0 COMMENT '0：顺丰，1：百世，2：圆通，3：中通',
  `order_status` smallint(6) NULL DEFAULT 0 COMMENT '0：待支付，1：已支付',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_order
-- ----------------------------
INSERT INTO `shop_order` VALUES (17, 2, '655f36dd89', 403.00, '2020-12-23 19:01:08', '2020-12-23 19:01:21', 0, 0, 1);
INSERT INTO `shop_order` VALUES (21, 2, 'fb8a2fd72b', 91.00, '2020-12-23 19:06:36', '2020-12-23 19:06:44', 0, 0, 1);
INSERT INTO `shop_order` VALUES (23, 2, '202ca2cbed', 260.00, '2020-12-23 23:47:44', '2020-12-23 23:47:44', 0, 0, 0);
INSERT INTO `shop_order` VALUES (24, 3, '4c35f9f331', 582.00, '2020-12-24 10:10:00', '2020-12-24 10:10:17', 0, 0, 1);
INSERT INTO `shop_order` VALUES (25, 2, 'b74e281764', 208.00, '2021-01-07 15:58:02', '2021-01-07 15:58:02', 0, 0, 0);
INSERT INTO `shop_order` VALUES (26, 2, '4d2d670898', 778.00, '2021-01-07 15:58:21', '2021-01-07 15:58:28', 0, 0, 1);
INSERT INTO `shop_order` VALUES (27, 3, '597dfe0bab', 285.00, '2021-01-07 16:10:44', '2021-01-07 16:10:47', 0, 0, 1);
INSERT INTO `shop_order` VALUES (28, 2, 'd02398c57f', 252.00, '2021-01-07 16:44:05', '2021-01-07 16:44:05', 0, 0, 0);

-- ----------------------------
-- Table structure for shop_orderitem
-- ----------------------------
DROP TABLE IF EXISTS `shop_orderitem`;
CREATE TABLE `shop_orderitem`  (
  `orderItem_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单明细表id',
  `order_id` int(11) NULL DEFAULT NULL COMMENT '订单id',
  `product_id` int(11) NULL DEFAULT NULL COMMENT '商品id',
  `product_amount` int(11) NOT NULL DEFAULT 0 COMMENT '商品数量',
  PRIMARY KEY (`orderItem_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_orderitem
-- ----------------------------
INSERT INTO `shop_orderitem` VALUES (16, 17, 46, 4);
INSERT INTO `shop_orderitem` VALUES (17, 17, 52, 3);
INSERT INTO `shop_orderitem` VALUES (21, 21, 48, 7);
INSERT INTO `shop_orderitem` VALUES (23, 23, 52, 4);
INSERT INTO `shop_orderitem` VALUES (24, 24, 49, 3);
INSERT INTO `shop_orderitem` VALUES (25, 24, 66, 8);
INSERT INTO `shop_orderitem` VALUES (26, 25, 46, 4);
INSERT INTO `shop_orderitem` VALUES (27, 26, 47, 5);
INSERT INTO `shop_orderitem` VALUES (28, 26, 63, 3);
INSERT INTO `shop_orderitem` VALUES (29, 26, 60, 8);
INSERT INTO `shop_orderitem` VALUES (30, 27, 50, 5);
INSERT INTO `shop_orderitem` VALUES (31, 27, 54, 4);
INSERT INTO `shop_orderitem` VALUES (32, 28, 66, 4);

-- ----------------------------
-- Table structure for shop_product
-- ----------------------------
DROP TABLE IF EXISTS `shop_product`;
CREATE TABLE `shop_product`  (
  `product_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `product_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `product_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '商品价格',
  `product_stock` int(11) NULL DEFAULT 0 COMMENT '商品库存',
  `category_id` int(11) NULL DEFAULT NULL COMMENT '分类id',
  `product_detail` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品详情',
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商城商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_product
-- ----------------------------
INSERT INTO `shop_product` VALUES (44, '可爱叮当猫抱枕', 30.00, 12, 11, '时尚现代哆啦A梦抱枕');
INSERT INTO `shop_product` VALUES (45, '简约棉麻抱枕', 23.00, 10, 11, '在沙发上坐或卧躺时抱用的抱枕，也用来装饰自家的沙发');
INSERT INTO `shop_product` VALUES (46, '法式乡村田园抱枕', 52.00, 19, 11, '法式乡村田园绣花印花棉麻抱枕');
INSERT INTO `shop_product` VALUES (47, '地中海格子桌布', 26.00, 10, 12, '地中海格子印花桌布台布,爱丁堡纯棉加厚桌布,茶几桌旗盖巾定制');
INSERT INTO `shop_product` VALUES (48, '清新简约防水桌布', 13.00, 11, 12, '北欧ins风桌布简约布艺加厚棉麻小清新防水桌布');
INSERT INTO `shop_product` VALUES (49, '北欧清雅桌布', 26.00, 5, 12, '钟爱一生布艺桌布仿亚麻桌垫北欧餐桌布');
INSERT INTO `shop_product` VALUES (50, '蝴蝶兰仿真花', 21.00, 1, 14, '轻奢蝴蝶兰仿真花假花兰花餐桌花客厅茶几玄关摆设装饰花艺摆件');
INSERT INTO `shop_product` VALUES (51, '郁金香', 26.00, 25, 14, '进口郁金香，上海广州深圳速递全国同城送花礼物');
INSERT INTO `shop_product` VALUES (52, '清新紫罗兰', 65.00, 18, 14, '紫罗兰盆栽花卉室内植物好养非洲堇四季开花不断带花苞小绿植');
INSERT INTO `shop_product` VALUES (53, '欧式花瓶', 54.00, 36, 16, '创意摆件欧式花瓶三件套家居新婚房摆设客厅电视柜玄关装饰品礼品');
INSERT INTO `shop_product` VALUES (54, '轻奢花瓶', 45.00, 9, 16, '轻奢花瓶摆件客厅插花玻璃水养北欧创意简约电视柜餐桌干花装饰品');
INSERT INTO `shop_product` VALUES (55, '亲嘴鱼摆件', 56.00, 9, 8, '亲嘴鱼摆件墨绿色描金现代家居装饰品年年有鱼创意抽象工艺品摆设');
INSERT INTO `shop_product` VALUES (56, '玻璃装水果盘篮创意拼盘', 23.00, 5, 8, '玻璃装水果盘篮创意拼盘客厅家用大号网红北欧现代简约茶几干果盘');
INSERT INTO `shop_product` VALUES (60, '长颈鹿漫步图', 36.00, 7, 22, '长颈鹿漫步草原马赛马拉图片');
INSERT INTO `shop_product` VALUES (61, '长寿花', 26.00, 14, 16, '长寿花又称家乐花、矮生伽蓝菜、圣诞伽蓝菜、寿星花！');
INSERT INTO `shop_product` VALUES (62, '动物模型', 56.00, 9, 8, '动物雕塑是造型艺术的一种。又称动物雕刻！');
INSERT INTO `shop_product` VALUES (63, '精美橱柜', 120.00, 12, 22, '櫥櫃精美的程度，讓人浮想聯翩。');
INSERT INTO `shop_product` VALUES (64, '美丽香炉', 28.00, 6, 18, '仿古莲花盘香塔香家用道场室内香薰炉');
INSERT INTO `shop_product` VALUES (65, '精致香炉', 56.00, 7, 18, ' 材质：陶瓷风格: 中式特点：香炉，盘香蚊香等居家香氛！');
INSERT INTO `shop_product` VALUES (66, '人物模型', 63.00, 11, 23, '游戏人物免费3D模型和3D模型效果图图片');

-- ----------------------------
-- Table structure for shop_user
-- ----------------------------
DROP TABLE IF EXISTS `shop_user`;
CREATE TABLE `shop_user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `user_password` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `user_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户电话',
  `user_email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `user_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '北京' COMMENT '用户地址',
  `user_status` smallint(6) NOT NULL DEFAULT 0 COMMENT '1:表示管理员，0:表示客户',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商城用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_user
-- ----------------------------
INSERT INTO `shop_user` VALUES (2, 'admin', '000', '15808425010', 'hurtlife2020@qq.com', '北京', 1);
INSERT INTO `shop_user` VALUES (3, 'jack', '123', '13990849315', '', '', 0);
INSERT INTO `shop_user` VALUES (4, 'mike', '000', NULL, NULL, NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;
