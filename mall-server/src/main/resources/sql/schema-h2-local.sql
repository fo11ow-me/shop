-- H2 schema for mall test database
-- Uses MERGE for idempotent seed data insertion

CREATE TABLE IF NOT EXISTS sys_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(32) DEFAULT '',
    gender INT DEFAULT 0,
    address VARCHAR(255) DEFAULT '',
    pwd VARCHAR(255) DEFAULT '',
    avatar VARCHAR(255) DEFAULT '',
    birthday DATE DEFAULT NULL,
    phone VARCHAR(20) DEFAULT '',
    email VARCHAR(64) DEFAULT '',
    remark VARCHAR(255) DEFAULT '',
    role INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_code ON sys_user(code);

CREATE TABLE IF NOT EXISTS pms_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pms_product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10,2) DEFAULT 0,
    stock INT DEFAULT 0,
    category_id INT DEFAULT 0,
    detail CLOB DEFAULT '',
    status INT DEFAULT 1,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pms_product_img (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    url VARCHAR(255) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS oms_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    address_id INT DEFAULT 0,
    order_sn VARCHAR(32) NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    pay_method INT DEFAULT 0,
    express_delivery INT DEFAULT 0,
    status INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(32) DEFAULT '';
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(20) DEFAULT '';
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS recipient_address VARCHAR(255) DEFAULT '';
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS payment_time DATETIME DEFAULT NULL;
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS delivery_time DATETIME DEFAULT NULL;
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS receipt_time DATETIME DEFAULT NULL;

CREATE TABLE IF NOT EXISTS oms_order_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(128) DEFAULT '',
    product_price DECIMAL(10,2) DEFAULT 0,
    product_img VARCHAR(255) DEFAULT '',
    amount INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS oms_cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    amount INT DEFAULT 1,
    is_selected INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Seed data (idempotent via MERGE)

MERGE INTO sys_user (id, code, name, pwd, phone, email, role, status, is_deleted) KEY(id) VALUES
(1, 'admin', '管理员', '$2a$10$ZqZA72kYQbX4mcxfTYXoEuN.xKUNQCXYWpYpkkjwRjZGyfkEG6.He', '13800000000', 'admin@mall.com', 1, 1, 0),
(2, 'user', '测试用户', '$2a$10$ZqZA72kYQbX4mcxfTYXoEuN.xKUNQCXYWpYpkkjwRjZGyfkEG6.He', '13900000000', 'user@mall.com', 0, 1, 0);

-- 一级分类
MERGE INTO pms_category (id, name, parent_id, is_deleted) KEY(id) VALUES
(1, '仿真花/干花', 0, 0),
(2, '花瓶花器', 0, 0),
(3, '靠垫抱枕', 0, 0),
(4, '桌布家纺', 0, 0),
(5, '家居摆件', 0, 0),
(6, '香薰用品', 0, 0),
(7, '置物收纳', 0, 0),
(8, '装饰壁饰', 0, 0),
(9, '杯具餐具', 0, 0),
(10, '创意家居', 0, 0);

-- 二级分类
MERGE INTO pms_category (id, name, parent_id, is_deleted) KEY(id) VALUES
(11, '仿真花', 1, 0),
(12, '干花束', 1, 0),
(21, '陶瓷花瓶', 2, 0),
(22, '花器', 2, 0),
(31, '三角靠垫', 3, 0),
(32, '方形抱枕', 3, 0),
(41, '餐桌布', 4, 0),
(42, '装饰桌布', 4, 0),
(51, '动物摆件', 5, 0),
(52, '人物摆件', 5, 0),
(53, '创意摆件', 5, 0),
(61, '香薰机', 6, 0),
(62, '香炉', 6, 0),
(71, '壁挂置物架', 7, 0),
(72, '桌面置物架', 7, 0),
(81, '装饰画', 8, 0),
(82, '铁艺壁饰', 8, 0),
(91, '玻璃杯', 9, 0),
(92, '碗碟套装', 9, 0),
(101, '小家具', 10, 0),
(102, '创意文具', 10, 0);

-- 商品数据
MERGE INTO pms_product (id, name, price, stock, category_id, detail, status, version, is_deleted) KEY(id) VALUES
(1, '绿植花束装饰', 89.00, 55, 11, '清新绿植搭配，仿真工艺，无需打理', 1, 0, 0),
(2, '仿真兰花盆栽', 128.00, 40, 11, '高仿真兰花，绢布花瓣，陶瓷底座', 1, 0, 0),
(3, '牡丹仿真花摆件', 158.00, 32, 11, '富贵牡丹造型，丝绸花瓣，花瓶搭配', 1, 0, 0),
(4, '仿真马蹄莲插花', 99.00, 45, 11, '白色马蹄莲，仿真水珠工艺，玻璃瓶搭配', 1, 0, 0),
(5, '仿真文心兰花艺', 119.00, 38, 11, '文心兰仿真花，含花盆，适合客厅餐桌', 1, 0, 0),
(6, '干花束装饰', 59.00, 60, 12, '天然干花束，多种花材搭配，ins风', 1, 0, 0),
(7, '仿真花摆件套装', 139.00, 35, 11, '组合仿真花艺，含花瓶，送礼佳品', 1, 0, 0),
(8, '北欧风陶瓷花瓶', 79.00, 50, 21, '简约北欧设计，哑光釉面，三种尺寸可选', 1, 0, 0),
(9, '素烧花器花瓶', 69.00, 42, 22, '日式素烧工艺，粗陶质感，适合干花', 1, 0, 0),
(10, '壁挂花器', 89.00, 28, 22, '铁艺玻璃壁挂花器，免钉安装，可水培', 1, 0, 0),
(11, '哆啦A梦三角靠垫', 99.00, 45, 31, '正版授权，短毛绒面料，45×45cm，可拆洗', 1, 0, 0),
(12, 'ins风纯色抱枕', 59.00, 55, 32, '棉麻面料，45×45cm，含芯，多色可选', 1, 0, 0),
(13, '装饰花卉抱枕', 69.00, 40, 32, '数码印花，短毛绒，45×45cm，隐藏拉链', 1, 0, 0),
(14, '蕾丝边棉麻桌布', 89.00, 38, 42, '棉麻混纺，蕾丝花边，防水涂层，多尺寸', 1, 0, 0),
(15, '条纹餐桌布', 79.00, 42, 41, '纯棉材质，经典条纹，可机洗，140×180cm', 1, 0, 0),
(16, '格子图案桌布', 69.00, 50, 42, '复古格纹，涤棉面料，防滑底，多色可选', 1, 0, 0),
(17, '民族风印花桌布', 99.00, 35, 42, '民族风印花图案，厚实面料，褶皱处理', 1, 0, 0),
(18, '情侣驯鹿摆件', 129.00, 30, 51, '树脂材质，一雄一雌一对，北欧风格，20cm高', 1, 0, 0),
(19, '波点苹果摆件', 89.00, 25, 53, '陶瓷材质，波点图案，手工上色，装饰品', 1, 0, 0),
(20, '水手造型摆件', 108.00, 22, 52, '复古做旧工艺，树脂材质，海洋风格', 1, 0, 0),
(21, '鱼形装饰摆件', 79.00, 35, 53, '金属+木质，现代简约，桌面装饰', 1, 0, 0),
(22, '大象吉祥摆件', 138.00, 20, 51, '树脂仿石纹理，寓意吉祥，客厅玄关装饰', 1, 0, 0),
(23, '超声波香薰机', 159.00, 40, 61, '500ml大容量，静音超声波雾化，带LED氛围灯', 1, 0, 0),
(24, '复古铜香炉', 189.00, 12, 62, '纯铜铸造，复古做旧，葫芦造型，含香插', 1, 0, 0),
(25, '简约壁挂置物架', 59.00, 48, 71, '实木搁板，铁艺支架，承重10kg，免钉安装', 1, 0, 0),
(26, '创意铁艺置物架', 139.00, 22, 72, '铁艺手工焊接，多层设计，可做花架/书架', 1, 0, 0),
(27, '自行车立体装饰画', 128.00, 18, 81, '立体浮雕装饰画框，自行车主题，复古做旧', 1, 0, 0),
(28, '铁艺山水壁饰', 168.00, 15, 82, '手工铁艺锻打，山水意境，客厅玄关装饰', 1, 0, 0),
(29, '动物系列装饰画', 89.00, 30, 81, '高清微喷，无框画，防水涂层，50×70cm', 1, 0, 0),
(30, '孔雀挂钟照片墙', 199.00, 12, 82, '铁艺孔雀造型+挂钟，创意墙面装饰组合', 1, 0, 0),
(31, 'ins风玻璃杯套装', 69.00, 60, 91, '高硼硅玻璃，6只装，300ml，耐热耐冷', 1, 0, 0),
(32, '日式玻璃碗碟套装', 159.00, 35, 92, '钢化玻璃材质，12件套，可微波炉加热', 1, 0, 0),
(33, '欧式铁艺小圆桌', 358.00, 15, 101, '锻铁框架，钢化玻璃桌面，直径60cm，阳台/花园适用', 1, 0, 0),
(34, '克鲁克斯辐射计', 139.00, 20, 102, '物理光学演示器，太阳能驱动旋转，桌面装饰/教学模型', 1, 0, 0),
(35, '狗狗造型书挡', 99.00, 25, 102, '树脂材质，仿木纹理，一对装，桌面书架收纳', 1, 0, 0);

-- 商品图片
MERGE INTO pms_product_img (id, product_id, url, is_deleted) KEY(id) VALUES
(1,  1,  '0f73c1cf-10ba-44e2-b8c5-4f5f7366d894.jpg', 0),
(2,  2,  '50acedf1-418a-4693-8a60-37e342ca37a8.jpg', 0),
(3,  2,  '8b90f15b-5d17-433a-92d5-39ddbb668053.jpg', 0),
(4,  2,  '6f0b9cca-1840-4a42-86ff-7f44d239b90b.jpg', 0),
(5,  3,  '8bc1c95a-4d17-4f09-9f11-274810cc8463.jpg', 0),
(6,  4,  'b82d3ad1-37ee-4f1d-80eb-70a32363d068.jpg', 0),
(7,  5,  'acfa5218-1029-4150-b2e7-0ef8dff649cd.jpg', 0),
(8,  5,  '64adc2c9-3591-46a3-b785-4beb4b03569d.jpg', 0),
(9,  6,  '79577f8c-8238-478d-8a16-0da8a5ace94d.jpg', 0),
(10, 6,  'be1e3972-25f2-403c-a156-8c2261044ea0.jpg', 0),
(11, 6,  'c23671f2-b6eb-447f-b702-1d8814744cd2.jpg', 0),
(12, 7,  'adc97f36-8d7b-4711-888f-e76931e352ac.jpg', 0),
(13, 8,  '20532cfe-4ea9-43ae-99a1-086af3c608f7.jpg', 0),
(14, 8,  'ff1e4a11-115d-4680-abc0-77b44fe58c0f.jpg', 0),
(15, 9,  '7ab8075d-66ab-4e52-952e-e8f679716c66.jpg', 0),
(16, 9,  'bd3ce2c2-efa9-4548-81db-6c8299c1285f.jpg', 0),
(17, 10, '7ffb2f80-6e8f-4d19-b128-d149f6766614.jpg', 0),
(18, 10, '2a8db663-27d5-4ae8-b890-ca82979453ab.jpg', 0),
(19, 10, 'cf3ba37c-6f32-42a2-9595-97e26edf289e.jpg', 0),
(20, 11, '47d1326e-4637-48f7-9a72-df6a56bd1479.jpg', 0),
(21, 11, '4a942eee-3591-4a36-8104-1d056735b1a6.jpg', 0),
(22, 12, '505051fd-5698-4243-be84-56e89759894f.jpg', 0),
(23, 12, 'bc094143-0c42-42e4-85b7-e07d8b9a4ac2.jpg', 0),
(24, 12, 'a4dbe409-61c5-4065-88b9-7c7f1d5aa702.jpg', 0),
(25, 13, '714389c0-960d-4611-91d1-6e8ee74a0906.jpg', 0),
(26, 13, 'd5fa8332-6a23-4545-b40f-966938161dfd.jpg', 0),
(27, 14, '04b61480-e1db-4d30-af26-a7ca2d24166b.jpg', 0),
(28, 15, '44b1cb71-c93a-42d5-ab42-406af96b8f13.jpg', 0),
(29, 16, '7fceb9ee-0c56-42ab-847d-7d52375870f6.jpg', 0),
(30, 16, 'aacd64d4-dc4e-497e-b859-f440dd0f5cc2.jpg', 0),
(31, 16, 'fd2c38f7-9c43-48dc-8d39-ab4d3d88640a.jpg', 0),
(32, 17, '9275d728-1a34-45df-b3b5-594a1421dc1c.jpg', 0),
(33, 18, '2010164d-bbd7-4e47-86e1-06ad046f5c1d.jpg', 0),
(34, 18, 'f7e944fd-afd9-4849-8610-07affd40bb2d.jpg', 0),
(35, 19, 'a5190494-289b-4796-b748-496846267a25.jpg', 0),
(36, 20, 'b22ea160-342a-4161-bcf6-c5660b6b4a42.jpg', 0),
(37, 21, 'deec894d-c99a-4281-99f5-64a346a444e8.jpg', 0),
(38, 22, 'e984f2c8-aef8-4e23-9d16-db38bf9dba9f.jpg', 0),
(39, 23, '083d3b3a-4290-49e4-bd0d-ab166616c876.jpg', 0),
(40, 23, 'ee398cbb-c787-4f7b-ace5-f3b6cfcfdcf2.jpg', 0),
(41, 24, '317d44ba-7a5a-43da-a9ee-4bfee7d5d758.jpg', 0),
(42, 24, 'd00d29c4-b742-45ae-960c-aaa6b10c45bb.jpg', 0),
(43, 25, '03b87769-2fb9-4aa6-91fe-6ace5af714ad.jpg', 0),
(44, 25, '64080592-7787-437c-aa48-fcb5db72bd51.jpg', 0),
(45, 26, '6a2ebed1-2b48-4eb6-a378-d56d000acd0a.jpg', 0),
(46, 27, '27804fd9-02b4-4752-b895-4bd56d81136a.jpg', 0),
(47, 28, '52aaab34-37ac-492b-b153-ffc49981c0f7.jpg', 0),
(48, 29, 'aa4228d1-25bf-499e-8130-beb8be2217be.jpg', 0),
(49, 30, '6ef68b42-b89e-4ae7-808a-76c4b6d5eb13.jpg', 0),
(50, 30, '3381c278-8483-44b8-95e5-235e52322c76.jpg', 0),
(51, 30, '7d790227-d868-4276-a2d0-1fc088c53430.jpg', 0),
(52, 31, '32c21631-c0b8-4d71-9603-33592eb4706e.jpg', 0),
(53, 32, '87005616-2e08-4c3b-ba8e-79dcdc42cbed.jpg', 0),
(54, 33, '02df9e72-6082-4d39-8da2-92f232cec7fe.jpg', 0),
(55, 34, '000d3b6a-1838-4872-a9a1-1a8e186c7b02.jpg', 0),
(56, 35, 'd27e4948-c07e-4072-9066-c8abeb2e2f20.jpg', 0);

-- 测试订单
MERGE INTO oms_order (id, user_id, order_sn, total_amount, pay_method, status, is_deleted) KEY(id) VALUES
(1, 2, '202605010001', 89.00, 1, 0, 0);

MERGE INTO oms_order_item (id, order_id, product_id, product_name, product_price, amount, is_deleted) KEY(id) VALUES
(1, 1, 1, '绿植花束装饰', 89.00, 1, 0);
