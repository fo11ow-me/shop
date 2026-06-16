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
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS payment_sn VARCHAR(64) DEFAULT '';
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS payment_time DATETIME DEFAULT NULL;
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS delivery_time DATETIME DEFAULT NULL;
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS receipt_time DATETIME DEFAULT NULL;
ALTER TABLE oms_order ADD COLUMN IF NOT EXISTS seckill_session_id INT DEFAULT NULL;
ALTER TABLE oms_order ADD CONSTRAINT IF NOT EXISTS uk_user_seckill UNIQUE (user_id, seckill_session_id);

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
(1, 'admin', '管理员', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4sEPhMNPfFhpYN76uO', '13800000000', 'admin@mall.com', 1, 1, 0),
(2, 'user', '测试用户', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4sEPhMNPfFhpYN76uO', '13900000000', 'user@mall.com', 0, 1, 0);

MERGE INTO pms_category (id, name, parent_id, is_deleted) KEY(id) VALUES
(1, '电子产品', 0, 0),
(2, '手机', 1, 0),
(3, '服装', 0, 0);

MERGE INTO pms_product (id, name, price, stock, category_id, detail, status, version, is_deleted) KEY(id) VALUES
(1, 'iPhone 15', 6999.00, 100, 2, '苹果最新款手机', 1, 0, 0),
(2, 'MacBook Pro', 14999.00, 50, 1, '苹果笔记本电脑', 1, 0, 0),
(3, 'T恤', 99.00, 200, 3, '纯棉T恤', 0, 0, 0);

MERGE INTO pms_product_img (id, product_id, url, is_deleted) KEY(id) VALUES
(1, 1, 'https://example.com/iphone15.jpg', 0),
(2, 2, 'https://example.com/macbook.jpg', 0);

MERGE INTO oms_order (id, user_id, order_sn, total_amount, pay_method, status, is_deleted) KEY(id) VALUES
(1, 2, '202605010001', 6999.00, 1, 0, 0),
(2, 2, '202605010002', 14999.00, 2, 1, 0);

MERGE INTO oms_order_item (id, order_id, product_id, product_name, product_price, amount, is_deleted) KEY(id) VALUES
(1, 1, 1, 'iPhone 15', 6999.00, 1, 0),
(2, 2, 2, 'MacBook Pro', 14999.00, 1, 0);

MERGE INTO oms_cart (id, user_id, product_id, amount, is_selected) KEY(id) VALUES
(1, 2, 1, 2, 1),
(2, 2, 3, 1, 0);

CREATE TABLE IF NOT EXISTS sms_seckill_session (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    seckill_price DECIMAL(10,2) DEFAULT 0,
    seckill_stock INT DEFAULT 0,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

-- Active session: start in past, end in future (always valid during test)
-- Upcoming session: both start and end in far future
MERGE INTO sms_seckill_session (id, product_id, seckill_price, seckill_stock, start_time, end_time, is_deleted) KEY(id) VALUES
(1, 1, 5999.00, 10, '2025-01-01 00:00:00', '2099-12-31 23:59:59', 0),
(2, 2, 12999.00, 5, '2099-01-01 00:00:00', '2099-12-31 23:59:59', 0);
