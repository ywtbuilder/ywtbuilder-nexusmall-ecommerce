-- Flyway V2: Add V3-specific columns & tables
-- This migration depends on MySQL 8+.

-- 1) oms_order: add payment_id for payment module integration (if table/column exists)
SET @has_oms_order = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oms_order'
);
SET @has_payment_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'oms_order' AND column_name = 'payment_id'
);
SET @sql = IF(
    @has_oms_order > 0 AND @has_payment_id = 0,
    'ALTER TABLE oms_order ADD COLUMN payment_id VARCHAR(64) NULL COMMENT ''payment transaction id'' AFTER pay_type',
    'SELECT ''skip oms_order.payment_id'''
);
PREPARE stmt1 FROM @sql;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- 2) ums_member: add avatar_url (if table/column exists)
SET @has_ums_member = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'ums_member'
);
SET @has_avatar_url = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ums_member' AND column_name = 'avatar_url'
);
SET @sql = IF(
    @has_ums_member > 0 AND @has_avatar_url = 0,
    'ALTER TABLE ums_member ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT ''member avatar url'' AFTER icon',
    'SELECT ''skip ums_member.avatar_url'''
);
PREPARE stmt2 FROM @sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 3) payment log table (new in V3)
CREATE TABLE IF NOT EXISTS oms_payment_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT 'order id',
    order_sn VARCHAR(64) NOT NULL COMMENT 'order serial number',
    pay_type INT DEFAULT 0 COMMENT '0-unpaid 1-alipay 2-wechat',
    trade_no VARCHAR(128) NULL COMMENT 'third-party transaction id',
    total_amount DECIMAL(10,2) NOT NULL COMMENT 'payment amount',
    pay_status INT DEFAULT 0 COMMENT '0-pending 1-paid 2-refunded',
    callback_content TEXT NULL COMMENT 'gateway callback payload',
    callback_time DATETIME NULL COMMENT 'callback time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_order_sn (order_sn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment log';
