-- ===============================================
-- mall 商城数据库初始化脚本
-- 用途: 首次部署或重建数据库时执行
-- 兼容: MySQL 8.0+
-- ===============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===============================================
-- 1. 清理废弃的权限关联表（已简化为 sys_user.role 字段）
-- ===============================================
DROP TABLE IF EXISTS per_role_menu;
DROP TABLE IF EXISTS per_user_role;
DROP TABLE IF EXISTS per_menu;
DROP TABLE IF EXISTS per_role;

-- ===============================================
-- 2. 核心表
-- ===============================================

-- 系统用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          INT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `code`        VARCHAR(20)     DEFAULT ''               COMMENT '用户编码',
    `name`        VARCHAR(20)     NOT NULL DEFAULT ''      COMMENT '用户姓名',
    `gender`      TINYINT UNSIGNED DEFAULT 0               COMMENT '性别(0=男,1=女)',
    `pwd`         CHAR(60)        DEFAULT NULL             COMMENT '密码(BCrypt)',
    `avatar`      VARCHAR(50)     DEFAULT NULL             COMMENT '头像',
    `birthday`    DATE            DEFAULT NULL             COMMENT '生日',
    `phone`       CHAR(11)        DEFAULT NULL             COMMENT '电话',
    `email`       VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `address`     VARCHAR(200)    DEFAULT NULL             COMMENT '地址',
    `remark`      VARCHAR(200)    DEFAULT NULL             COMMENT '备注',
    `role`        INT             DEFAULT 0                COMMENT '角色(0=用户,1=管理员)',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1      COMMENT '状态(0=禁用,1=启用)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '逻辑删除(0=未删,1=已删)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

-- ===============================================
-- 3. 商品相关表
-- ===============================================

-- 商品分类表
CREATE TABLE IF NOT EXISTS `pms_category` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    `name`        VARCHAR(30)     NOT NULL                 COMMENT '分类名称',
    `parent_id`   INT             DEFAULT 0                COMMENT '父分类ID(0=一级分类)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `pms_product` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '商品ID',
    `name`        VARCHAR(30)     NOT NULL                 COMMENT '商品名称',
    `price`       DECIMAL(10,2)   DEFAULT 0.00             COMMENT '价格',
    `stock`       INT             DEFAULT 0                COMMENT '库存',
    `category_id` INT             DEFAULT NULL             COMMENT '分类ID',
    `detail`      VARCHAR(100)    DEFAULT NULL             COMMENT '商品详情',
    `status`      INT             DEFAULT 1                COMMENT '状态(0=下架,1=上架)',
    `version`     INT             NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS `pms_product_img` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
    `url`         VARCHAR(255)    NOT NULL                 COMMENT '图片OSS key',
    `product_id`  INT             NOT NULL                 COMMENT '商品ID',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_url` (`product_id`, `url`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';

-- ===============================================
-- 4. 订单与购物车
-- ===============================================

-- 购物车表
CREATE TABLE IF NOT EXISTS `oms_cart` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '购物车ID',
    `user_id`     INT             DEFAULT NULL             COMMENT '用户ID',
    `product_id`  INT             DEFAULT NULL             COMMENT '商品ID',
    `amount`      INT             DEFAULT 0                COMMENT '数量',
    `is_selected` TINYINT         DEFAULT 1                COMMENT '是否选中(0=否,1=是)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `oms_order` (
    `id`                 INT             NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    `user_id`            INT             DEFAULT NULL             COMMENT '用户ID',
    `address_id`         INT             DEFAULT NULL             COMMENT '收货地址ID',
    `order_sn`           VARCHAR(30)     DEFAULT NULL             COMMENT '订单编号',
    `payment_sn`         VARCHAR(32)     DEFAULT ''               COMMENT '支付流水号',
    `total_amount`       DECIMAL(10,2)   DEFAULT 0.00             COMMENT '总金额',
    `create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `pay_method`         TINYINT         DEFAULT 0                COMMENT '支付方式(0=支付宝,1=微信,2=银联,3=货到付款)',
    `express_delivery`   TINYINT         DEFAULT 0                COMMENT '快递(0=顺丰,1=百世,2=圆通,3=中通)',
    `status`             TINYINT         DEFAULT 0                COMMENT '状态(0=待支付,1=已支付,2=库存不足)',
    `recipient_name`     VARCHAR(32)     DEFAULT ''               COMMENT '收件人',
    `recipient_phone`    VARCHAR(20)     DEFAULT ''               COMMENT '收件人电话',
    `recipient_address`  VARCHAR(255)    DEFAULT ''               COMMENT '收件地址',
    `payment_time`       DATETIME        DEFAULT NULL             COMMENT '支付时间',
    `delivery_time`      DATETIME        DEFAULT NULL             COMMENT '发货时间',
    `receipt_time`       DATETIME        DEFAULT NULL             COMMENT '收货时间',
    `is_deleted`         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

ALTER TABLE `oms_order` ADD COLUMN `seckill_session_id` INT DEFAULT NULL COMMENT '秒杀场次ID' AFTER `user_id`;
ALTER TABLE `oms_order` ADD UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_session_id`);

-- 订单明细表
CREATE TABLE IF NOT EXISTS `oms_order_item` (
    `id`            INT             NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    `order_id`      INT             DEFAULT NULL             COMMENT '订单ID',
    `product_id`    INT             DEFAULT NULL             COMMENT '商品ID',
    `product_name`  VARCHAR(100)    DEFAULT NULL             COMMENT '商品名称',
    `product_price` DECIMAL(10,2)   DEFAULT 0.00             COMMENT '商品价格',
    `product_img`   VARCHAR(500)    DEFAULT NULL             COMMENT '商品图片',
    `amount`        INT             NOT NULL DEFAULT 0       COMMENT '数量',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- ===============================================
-- 5. 支付表
-- ===============================================

CREATE TABLE IF NOT EXISTS `pay_payment` (
    `id`             INT             NOT NULL AUTO_INCREMENT  COMMENT '支付ID',
    `order_id`       INT             DEFAULT NULL             COMMENT '订单ID',
    `pay_sn`         VARCHAR(30)     DEFAULT NULL             COMMENT '支付流水号',
    `amount`         DECIMAL(10,2)   DEFAULT NULL             COMMENT '支付金额',
    `method`         TINYINT         DEFAULT 0                COMMENT '支付方式(0=支付宝,1=微信)',
    `status`         TINYINT         DEFAULT 0                COMMENT '状态(0=待支付,1=已支付)',
    `transaction_id` VARCHAR(64)     DEFAULT NULL             COMMENT '第三方交易号',
    `code_url`       VARCHAR(255)    DEFAULT NULL             COMMENT '微信扫码链接',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`     TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_sn` (`pay_sn`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付表';

-- ===============================================
-- 6. 秒杀相关表
-- ===============================================

-- 秒杀场次表
CREATE TABLE IF NOT EXISTS `sms_seckill_session` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '场次ID',
    `product_id`  INT             NOT NULL                 COMMENT '商品ID',
    `seckill_price` DECIMAL(10,2) NOT NULL                 COMMENT '秒杀价',
    `seckill_stock` INT           NOT NULL                 COMMENT '秒杀库存',
    `start_time`  DATETIME        NOT NULL                 COMMENT '开始时间',
    `end_time`    DATETIME        NOT NULL                 COMMENT '结束时间',
    `version`     INT             NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_start_end` (`start_time`, `end_time`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀场次表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS `sms_seckill_order` (
    `id`          INT             NOT NULL AUTO_INCREMENT  COMMENT '秒杀订单ID',
    `user_id`     INT             NOT NULL                 COMMENT '用户ID',
    `session_id`  INT             NOT NULL                 COMMENT '场次ID',
    `product_id`  INT             NOT NULL                 COMMENT '商品ID',
    `order_id`    INT             DEFAULT NULL             COMMENT '关联订单ID',
    `status`      TINYINT         NOT NULL DEFAULT 0       COMMENT '状态(0=排队中,1=成功,2=已取消)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted`  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_session` (`user_id`, `session_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀订单表';

ALTER TABLE `pms_product` ADD INDEX `idx_update_time` (`update_time`);
ALTER TABLE `sms_seckill_session` ADD INDEX `idx_start_end` (`start_time`, `end_time`);

CREATE TABLE IF NOT EXISTS `reconcile_log` (
    `id`           BIGINT          NOT NULL                COMMENT '雪花算法主键',
    `seckill_id`   INT             NOT NULL                COMMENT '秒杀场次ID',
    `user_id`      INT             NOT NULL                COMMENT '用户ID',
    `operation`    VARCHAR(20)     NOT NULL                COMMENT '操作类型：DEDUCT/ROLLBACK',
    `stock_before` INT             DEFAULT NULL            COMMENT '变更前库存',
    `stock_after`  INT             DEFAULT NULL            COMMENT '变更后库存',
    `create_time`  DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_seckill_id` (`seckill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀库存对账日志';

-- ===============================================
-- 7. 种子数据
-- ===============================================

-- 管理员账号（用户名: admin, 密码: 123）
INSERT INTO `sys_user` (`id`, `code`, `name`, `gender`, `pwd`, `avatar`, `birthday`, `phone`, `email`, `address`, `remark`, `role`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 'admin', '管理员', 0, '$2a$10$Nwice5e9Sqt34HBz/VpBkumMtP5NJdDZFFPjsuulo2PIHZNHY19em', '613877ab0e79497eba980767073cadb7.jpg', '2000-12-06', '13900000000', 'admin@mall.com', '', '', 1, 1, '2022-01-22 19:46:27', NULL, 0)
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 一级分类
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `create_time`) VALUES
(1, '仿真花/干花', 0, NOW()),
(2, '花瓶花器', 0, NOW()),
(3, '靠垫抱枕', 0, NOW()),
(4, '桌布家纺', 0, NOW()),
(5, '家居摆件', 0, NOW()),
(6, '香薰用品', 0, NOW()),
(7, '置物收纳', 0, NOW()),
(8, '装饰壁饰', 0, NOW()),
(9, '杯具餐具', 0, NOW()),
(10, '创意家居', 0, NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 二级分类
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `create_time`) VALUES
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
(102, '创意文具', 10, NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 商品数据
INSERT INTO `pms_product` (`id`, `name`, `price`, `stock`, `category_id`, `detail`, `create_time`) VALUES
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
(19, '波点苹果摆件', 89.00, 25, 53, '陶瓷材质，波点图案，手工上色', NOW()),
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
(35, '狗狗造型书挡', 99.00, 25, 102, '树脂材质，仿木纹理，一对装，桌面书架收纳', NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 商品图片
INSERT INTO `pms_product_img` (`url`, `product_id`, `create_time`) VALUES
-- 绿植花束 (1)
('product-1-1.jpg', 1, NOW()),
-- 仿真兰花 (2)
('product-2-1.jpg', 2, NOW()),
('product-2-2.jpg', 2, NOW()),
('product-2-3.jpg', 2, NOW()),
-- 牡丹仿真花 (3)
('product-3-1.jpg', 3, NOW()),
-- 马蹄莲插花 (4)
('product-4-1.jpg', 4, NOW()),
-- 仿真文心兰 (5)
('product-5-1.jpg', 5, NOW()),
('product-5-2.jpg', 5, NOW()),
-- 干花束 (6)
('product-6-1.jpg', 6, NOW()),
('product-6-2.jpg', 6, NOW()),
('product-6-3.jpg', 6, NOW()),
-- 仿真花套装 (7)
('product-7-1.jpg', 7, NOW()),
-- 陶瓷花瓶 (8)
('product-8-1.jpg', 8, NOW()),
('product-8-2.jpg', 8, NOW()),
-- 素烧花器 (9)
('product-9-1.jpg', 9, NOW()),
('product-9-2.jpg', 9, NOW()),
-- 壁挂花器 (10)
('product-10-1.jpg', 10, NOW()),
('product-10-2.jpg', 10, NOW()),
('product-10-3.jpg', 10, NOW()),
-- 哆啦A梦靠垫 (11)
('product-11-1.jpg', 11, NOW()),
('product-11-2.jpg', 11, NOW()),
-- ins纯色抱枕 (12)
('product-12-1.jpg', 12, NOW()),
('product-12-2.jpg', 12, NOW()),
('product-12-3.jpg', 12, NOW()),
-- 装饰花卉抱枕 (13)
('product-13-1.jpg', 13, NOW()),
('product-13-2.jpg', 13, NOW()),
-- 蕾丝棉麻桌布 (14)
('product-14-1.jpg', 14, NOW()),
-- 条纹餐桌布 (15)
('product-15-1.jpg', 15, NOW()),
-- 格子桌布 (16)
('product-16-1.jpg', 16, NOW()),
('product-16-2.jpg', 16, NOW()),
('product-16-3.jpg', 16, NOW()),
-- 印花桌布 (17)
('product-17-1.jpg', 17, NOW()),
-- 驯鹿摆件 (18)
('product-18-1.jpg', 18, NOW()),
('product-18-2.jpg', 18, NOW()),
-- 波点苹果 (19)
('product-19-1.jpg', 19, NOW()),
-- 水手摆件 (20)
('product-20-1.jpg', 20, NOW()),
-- 鱼形摆件 (21)
('product-21-1.jpg', 21, NOW()),
-- 大象摆件 (22)
('product-22-1.jpg', 22, NOW()),
-- 香薰机 (23)
('product-23-1.jpg', 23, NOW()),
('product-23-2.jpg', 23, NOW()),
-- 铜香炉 (24)
('product-24-1.jpg', 24, NOW()),
('product-24-2.jpg', 24, NOW()),
-- 壁挂置物架 (25)
('product-25-1.jpg', 25, NOW()),
('product-25-2.jpg', 25, NOW()),
-- 铁艺置物架 (26)
('product-26-1.jpg', 26, NOW()),
-- 自行车装饰画 (27)
('product-27-1.jpg', 27, NOW()),
-- 山水壁饰 (28)
('product-28-1.jpg', 28, NOW()),
-- 动物装饰画 (29)
('product-29-1.jpg', 29, NOW()),
-- 孔雀挂钟 (30)
('product-30-1.jpg', 30, NOW()),
('product-30-2.jpg', 30, NOW()),
('product-30-3.jpg', 30, NOW()),
-- 玻璃杯套装 (31)
('product-31-1.jpg', 31, NOW()),
-- 碗碟套装 (32)
('product-32-1.jpg', 32, NOW()),
-- 铁艺小圆桌 (33)
('product-33-1.jpg', 33, NOW()),
-- 辐射计 (34)
('product-34-1.jpg', 34, NOW()),
-- 书挡 (35)
('product-35-1.jpg', 35, NOW())
ON DUPLICATE KEY UPDATE `url`=VALUES(`url`);

-- 秒杀场次种子数据
INSERT INTO `sms_seckill_session` (`product_id`, `seckill_price`, `seckill_stock`, `start_time`, `end_time`) VALUES
(1, 29.90, 100, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(5, 39.90, 50, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
