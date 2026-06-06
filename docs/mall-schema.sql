-- mall MySQL schema
-- Generated from schema-h2-local.sql, adapted for MySQL 8.0

DROP TABLE IF EXISTS oms_cart;
DROP TABLE IF EXISTS oms_order_item;
DROP TABLE IF EXISTS oms_order;
DROP TABLE IF EXISTS pms_product_img;
DROP TABLE IF EXISTS pms_product;
DROP TABLE IF EXISTS pms_category;
DROP TABLE IF EXISTS sms_seckill_session;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE UNIQUE INDEX idx_user_code ON sys_user(code);

CREATE TABLE pms_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pms_product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10,2) DEFAULT 0,
    stock INT DEFAULT 0,
    category_id INT DEFAULT 0,
    detail TEXT DEFAULT NULL,
    status INT DEFAULT 1,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pms_product_img (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    url VARCHAR(255) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oms_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    address_id INT DEFAULT 0,
    order_sn VARCHAR(32) NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    pay_method INT DEFAULT 0,
    express_delivery INT DEFAULT 0,
    status INT DEFAULT 0,
    recipient_name VARCHAR(32) DEFAULT '',
    recipient_phone VARCHAR(20) DEFAULT '',
    recipient_address VARCHAR(255) DEFAULT '',
    payment_time DATETIME DEFAULT NULL,
    delivery_time DATETIME DEFAULT NULL,
    receipt_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oms_order_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(128) DEFAULT '',
    product_price DECIMAL(10,2) DEFAULT 0,
    product_img VARCHAR(255) DEFAULT '',
    amount INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oms_cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    amount INT DEFAULT 1,
    is_selected INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data: admin + test user
INSERT INTO sys_user (id, code, name, pwd, phone, email, role, status, is_deleted) VALUES
(1, 'admin', '管理员', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4sEPhMNPfFhpYN76uO', '13800000000', 'admin@mall.com', 1, 1, 0),
(2, 'user', '测试用户', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4sEPhMNPfFhpYN76uO', '13900000000', 'user@mall.com', 0, 1, 0);
