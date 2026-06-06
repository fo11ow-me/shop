-- ===============================================
-- mall 商城数据库初始化脚本
-- ===============================================

-- ----------------------------
-- 1. 权限简化：用户新增 role 字段 (0=用户, 1=管理员)
-- ----------------------------
ALTER TABLE sys_user ADD COLUMN role INT DEFAULT 0 AFTER remark;
UPDATE sys_user SET role = 1 WHERE code = 'admin';

-- ----------------------------
-- 2. 删除旧的权限关联表
-- ----------------------------
DROP TABLE IF EXISTS per_role_menu;
DROP TABLE IF EXISTS per_user_role;
DROP TABLE IF EXISTS per_menu;
DROP TABLE IF EXISTS per_role;

-- ----------------------------
-- 3. 订单表结构变更
-- ----------------------------
ALTER TABLE oms_order ADD COLUMN payment_sn VARCHAR(32) DEFAULT NULL AFTER order_sn;
ALTER TABLE oms_order ADD COLUMN recipient_name VARCHAR(32) DEFAULT '' AFTER status;
ALTER TABLE oms_order ADD COLUMN recipient_phone VARCHAR(20) DEFAULT '' AFTER recipient_name;
ALTER TABLE oms_order ADD COLUMN recipient_address VARCHAR(255) DEFAULT '' AFTER recipient_phone;
ALTER TABLE oms_order ADD COLUMN payment_time DATETIME DEFAULT NULL AFTER recipient_address;
ALTER TABLE oms_order ADD COLUMN delivery_time DATETIME DEFAULT NULL AFTER payment_time;
ALTER TABLE oms_order ADD COLUMN receipt_time DATETIME DEFAULT NULL AFTER delivery_time;

-- ----------------------------
-- 2. 种子数据 — 一级分类
-- ----------------------------
INSERT INTO pms_category (id, name, parent_id, create_time) VALUES
(1, '仿真花/干花', 0, NOW()),
(2, '花瓶花器', 0, NOW()),
(3, '靠垫抱枕', 0, NOW()),
(4, '桌布家纺', 0, NOW()),
(5, '家居摆件', 0, NOW()),
(6, '香薰用品', 0, NOW()),
(7, '置物收纳', 0, NOW()),
(8, '装饰壁饰', 0, NOW()),
(9, '杯具餐具', 0, NOW()),
(10, '创意家居', 0, NOW());

-- ----------------------------
-- 二级分类
-- ----------------------------
INSERT INTO pms_category (id, name, parent_id, create_time) VALUES
(11, '仿真花', 1, NOW()),
(12, '干花束', 1, NOW()),
(21, '陶瓷花瓶', 2, NOW()),
(22, '花器', 2, NOW()),
(31, '三角靠垫', 3, NOW()),
(32, '方形抱枕', 3, NOW()),
(41, '餐桌布', 4, NOW()),
(42, '装饰桌布', 4, NOW()),
(51, '动物摆件', 5, NOW()),
(52, '人物摆件', 5, NOW()),
(53, '创意摆件', 5, NOW()),
(61, '香薰机', 6, NOW()),
(62, '香炉', 6, NOW()),
(71, '壁挂置物架', 7, NOW()),
(72, '桌面置物架', 7, NOW()),
(81, '装饰画', 8, NOW()),
(82, '铁艺壁饰', 8, NOW()),
(91, '玻璃杯', 9, NOW()),
(92, '碗碟套装', 9, NOW()),
(101, '小家具', 10, NOW()),
(102, '创意文具', 10, NOW());

-- ----------------------------
-- 6. 商品数据
-- ----------------------------
INSERT INTO pms_product (id, name, price, stock, category_id, detail, create_time) VALUES
-- 仿真花/干花
(1, '绿植花束装饰', 89.00, 55, 11, '清新绿植搭配，仿真工艺，无需打理', NOW()),
(2, '仿真兰花盆栽', 128.00, 40, 11, '高仿真兰花，绢布花瓣，陶瓷底座', NOW()),
(3, '牡丹仿真花摆件', 158.00, 32, 11, '富贵牡丹造型，丝绸花瓣，花瓶搭配', NOW()),
(4, '仿真马蹄莲插花', 99.00, 45, 11, '白色马蹄莲，仿真水珠工艺，玻璃瓶搭配', NOW()),
(5, '仿真文心兰花艺', 119.00, 38, 11, '文心兰仿真花，含花盆，适合客厅餐桌', NOW()),
(6, '干花束装饰', 59.00, 60, 12, '天然干花束，多种花材搭配，ins风', NOW()),
(7, '仿真花摆件套装', 139.00, 35, 11, '组合仿真花艺，含花瓶，送礼佳品', NOW()),
-- 花瓶花器
(8, '北欧风陶瓷花瓶', 79.00, 50, 21, '简约北欧设计，哑光釉面，三种尺寸可选', NOW()),
(9, '素烧花器花瓶', 69.00, 42, 22, '日式素烧工艺，粗陶质感，适合干花', NOW()),
(10, '壁挂花器', 89.00, 28, 22, '铁艺玻璃壁挂花器，免钉安装，可水培', NOW()),
-- 靠垫抱枕
(11, '哆啦A梦三角靠垫', 99.00, 45, 31, '正版授权，短毛绒面料，45×45cm，可拆洗', NOW()),
(12, 'ins风纯色抱枕', 59.00, 55, 32, '棉麻面料，45×45cm，含芯，多色可选', NOW()),
(13, '装饰花卉抱枕', 69.00, 40, 32, '数码印花，短毛绒，45×45cm，隐藏拉链', NOW()),
-- 桌布家纺
(14, '蕾丝边棉麻桌布', 89.00, 38, 42, '棉麻混纺，蕾丝花边，防水涂层，多尺寸', NOW()),
(15, '条纹餐桌布', 79.00, 42, 41, '纯棉材质，经典条纹，可机洗，140×180cm', NOW()),
(16, '格子图案桌布', 69.00, 50, 42, '复古格纹，涤棉面料，防滑底，多色可选', NOW()),
(17, '民族风印花桌布', 99.00, 35, 42, '民族风印花图案，厚实面料，褶皱处理', NOW()),
-- 家居摆件
(18, '情侣驯鹿摆件', 129.00, 30, 51, '树脂材质，一雄一雌一对，北欧风格，20cm高', NOW()),
(19, '波点苹果摆件', 89.00, 25, 53, '陶瓷材质，波点图案，手工上色，装饰品', NOW()),
(20, '水手造型摆件', 108.00, 22, 52, '复古做旧工艺，树脂材质，海洋风格', NOW()),
(21, '鱼形装饰摆件', 79.00, 35, 53, '金属+木质，现代简约，桌面装饰', NOW()),
(22, '大象吉祥摆件', 138.00, 20, 51, '树脂仿石纹理，寓意吉祥，客厅玄关装饰', NOW()),
-- 香薰用品
(23, '超声波香薰机', 159.00, 40, 61, '500ml大容量，静音超声波雾化，带LED氛围灯', NOW()),
(24, '复古铜香炉', 189.00, 12, 62, '纯铜铸造，复古做旧，葫芦造型，含香插', NOW()),
-- 置物收纳
(25, '简约壁挂置物架', 59.00, 48, 71, '实木搁板，铁艺支架，承重10kg，免钉安装', NOW()),
(26, '创意铁艺置物架', 139.00, 22, 72, '铁艺手工焊接，多层设计，可做花架/书架', NOW()),
-- 装饰壁饰
(27, '自行车立体装饰画', 128.00, 18, 81, '立体浮雕装饰画框，自行车主题，复古做旧', NOW()),
(28, '铁艺山水壁饰', 168.00, 15, 82, '手工铁艺锻打，山水意境，客厅玄关装饰', NOW()),
(29, '动物系列装饰画', 89.00, 30, 81, '高清微喷，无框画，防水涂层，50×70cm', NOW()),
(30, '孔雀挂钟照片墙', 199.00, 12, 82, '铁艺孔雀造型+挂钟，创意墙面装饰组合', NOW()),
-- 杯具餐具
(31, 'ins风玻璃杯套装', 69.00, 60, 91, '高硼硅玻璃，6只装，300ml，耐热耐冷', NOW()),
(32, '日式玻璃碗碟套装', 159.00, 35, 92, '钢化玻璃材质，12件套，可微波炉加热', NOW()),
-- 创意家居
(33, '欧式铁艺小圆桌', 358.00, 15, 101, '锻铁框架，钢化玻璃桌面，直径60cm，阳台/花园适用', NOW()),
(34, '克鲁克斯辐射计', 139.00, 20, 102, '物理光学演示器，太阳能驱动旋转，桌面装饰/教学模型', NOW()),
(35, '狗狗造型书挡', 99.00, 25, 102, '树脂材质，仿木纹理，一对装，桌面书架收纳', NOW());

-- ----------------------------
-- 7. 商品图片
-- ----------------------------
INSERT INTO pms_product_img (url, product_id, create_time) VALUES
-- 绿植花束 (1)
('0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg', 1, NOW()),
-- 仿真兰花 (2)
('50acedf1-418a-4693-8a60-37e342ca37a8.jpg', 2, NOW()),
('8b90f15b-5d17-433a-92d5-39ddbb668053.jpg', 2, NOW()),
('6f0b9cca-1840-4a42-86ff-7f44d239b90b.jpg', 2, NOW()),
-- 牡丹仿真花 (3)
('8bc1c95a-4d17-4f09-9f11-274810cc8463.jpg', 3, NOW()),
-- 马蹄莲插花 (4)
('b82d3ad1-37ee-4f1d-80eb-70a32363d068.jpg', 4, NOW()),
-- 仿真文心兰 (5)
('acfa5218-1029-4150-b2e7-0ef8dff649cd.jpg', 5, NOW()),
('64adc2c9-3591-46a3-b785-4beb4b03569d.jpg', 5, NOW()),
-- 干花束 (6)
('79577f8c-8238-478d-8a16-0da8a5ace94d.jpg', 6, NOW()),
('be1e3972-25f2-403c-a156-8c2261044ea0.jpg', 6, NOW()),
('c23671f2-b6eb-447f-b702-1d8814744cd2.jpg', 6, NOW()),
-- 仿真花套装 (7)
('adc97f36-8d7b-4711-888f-e76931e352ac.jpg', 7, NOW()),
-- 陶瓷花瓶 (8)
('20532cfe-4ea9-43ae-99a1-086af3c608f7.jpg', 8, NOW()),
('ff1e4a11-115d-4680-abc0-77b44fe58c0f.jpg', 8, NOW()),
-- 素烧花器 (9)
('7ab8075d-66ab-4e52-952e-e8f679716c66.jpg', 9, NOW()),
('bd3ce2c2-efa9-4548-81db-6c8299c1285f.jpg', 9, NOW()),
-- 壁挂花器 (10)
('7ffb2f80-6e8f-4d19-b128-d149f6766614.jpg', 10, NOW()),
('2a8db663-27d5-4ae8-b890-ca82979453ab.jpg', 10, NOW()),
('cf3ba37c-6f32-42a2-9595-97e26edf289e.jpg', 10, NOW()),
-- 哆啦A梦靠垫 (11)
('47d1326e-4637-48f7-9a72-df6a56bd1479.jpg', 11, NOW()),
('4a942eee-3591-4a36-8104-1d056735b1a6.jpg', 11, NOW()),
-- ins纯色抱枕 (12)
('505051fd-5698-4243-be84-56e89759894f.jpg', 12, NOW()),
('bc094143-0c42-42e4-85b7-e07d8b9a4ac2.jpg', 12, NOW()),
('a4dbe409-61c5-4065-88b9-7c7f1d5aa702.jpg', 12, NOW()),
-- 装饰花卉抱枕 (13)
('714389c0-960d-4611-91d1-6e8ee74a0906.jpg', 13, NOW()),
('d5fa8332-6a23-4545-b40f-966938161dfd.jpg', 13, NOW()),
-- 蕾丝棉麻桌布 (14)
('04b61480-e1db-4d30-af26-a7ca2d24166b.jpg', 14, NOW()),
-- 条纹餐桌布 (15)
('44b1cb71-c93a-42d5-ab42-406af96b8f13.jpg', 15, NOW()),
-- 格子桌布 (16)
('7fceb9ee-0c56-42ab-847d-7d52375870f6.jpg', 16, NOW()),
('aacd64d4-dc4e-497e-b859-f440dd0f5cc2.jpg', 16, NOW()),
('fd2c38f7-9c43-48dc-8d39-ab4d3d88640a.jpg', 16, NOW()),
-- 印花桌布 (17)
('9275d728-1a34-45df-b3b5-594a1421dc1c.jpg', 17, NOW()),
-- 驯鹿摆件 (18)
('2010164d-bbd7-4e47-86e1-06ad046f5c1d.jpg', 18, NOW()),
('f7e944fd-afd9-4849-8610-07affd40bb2d.jpg', 18, NOW()),
-- 波点苹果 (19)
('a5190494-289b-4796-b748-496846267a25.jpg', 19, NOW()),
-- 水手摆件 (20)
('b22ea160-342a-4161-bcf6-c5660b6b4a42.jpg', 20, NOW()),
-- 鱼形摆件 (21)
('deec894d-c99a-4281-99f5-64a346a444e8.jpg', 21, NOW()),
-- 大象摆件 (22)
('e984f2c8-aef8-4e23-9d16-db38bf9dba9f.jpg', 22, NOW()),
-- 香薰机 (23)
('083d3b3a-4290-49e4-bd0d-ab166616c876.jpg', 23, NOW()),
('ee398cbb-c787-4f7b-ace5-f3b6cfcfdcf2.jpg', 23, NOW()),
-- 铜香炉 (24)
('317d44ba-7a5a-43da-a9ee-4bfee7d5d758.jpg', 24, NOW()),
('d00d29c4-b742-45ae-960c-aaa6b10c45bb.jpg', 24, NOW()),
-- 壁挂置物架 (25)
('03b87769-2fb9-4aa6-91fe-6ace5af714ad.jpg', 25, NOW()),
('64080592-7787-437c-aa48-fcb5db72bd51.jpg', 25, NOW()),
-- 铁艺置物架 (26)
('6a2ebed1-2b48-4eb6-a378-d56d000acd0a.jpg', 26, NOW()),
-- 自行车装饰画 (27)
('27804fd9-02b4-4752-b895-4bd56d81136a.jpg', 27, NOW()),
-- 山水壁饰 (28)
('52aaab34-37ac-492b-b153-ffc49981c0f7.jpg', 28, NOW()),
-- 动物装饰画 (29)
('aa4228d1-25bf-499e-8130-beb8be2217be.jpg', 29, NOW()),
-- 孔雀挂钟 (30)
('6ef68b42-b89e-4ae7-808a-76c4b6d5eb13.jpg', 30, NOW()),
('3381c278-8483-44b8-95e5-235e52322c76.jpg', 30, NOW()),
('7d790227-d868-4276-a2d0-1fc088c53430.jpg', 30, NOW()),
-- 玻璃杯套装 (31)
('32c21631-c0b8-4d71-9603-33592eb4706e.jpg', 31, NOW()),
-- 碗碟套装 (32)
('87005616-2e08-4c3b-ba8e-79dcdc42cbed.jpg', 32, NOW()),
-- 铁艺小圆桌 (33)
('02df9e72-6082-4d39-8da2-92f232cec7fe.jpg', 33, NOW()),
-- 辐射计 (34)
('000d3b6a-1838-4872-a9a1-1a8e186c7b02.jpg', 34, NOW()),
-- 书挡 (35)
('d27e4948-c07e-4072-9066-c8abeb2e2f20.jpg', 35, NOW());

-- ----------------------------
-- 28. 秒杀活动场次表
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_session`;
CREATE TABLE `sms_seckill_session` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `seckill_stock` INT NOT NULL COMMENT '秒杀库存',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动场次表';

-- 测试种子数据
INSERT INTO `sms_seckill_session` (`product_id`, `seckill_price`, `seckill_stock`, `start_time`, `end_time`) VALUES
(1, 29.90, 100, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(5, 39.90, 50, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR));
