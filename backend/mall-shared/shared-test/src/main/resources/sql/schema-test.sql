-- =================================================================
-- 集成测试用最小化 DDL — 仅包含 App 核心链路需要的表
-- 适用于 TestContainers MySQL 8.0 容器
-- =================================================================

-- ums_member_level（会员等级）
CREATE TABLE IF NOT EXISTS `ums_member_level` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `name`                      VARCHAR(100) DEFAULT NULL,
    `growth_point`              INT          DEFAULT 0,
    `default_status`            INT          DEFAULT 0 COMMENT '0->非默认;1->默认',
    `free_freight_point`        INT          DEFAULT 0,
    `comment_growth_point`      INT          DEFAULT 0,
    `priviledge_free_freight`   INT          DEFAULT 0,
    `priviledge_sign_in`        INT          DEFAULT 0,
    `priviledge_comment`        INT          DEFAULT 0,
    `priviledge_promotion`      INT          DEFAULT 0,
    `priviledge_member_price`   INT          DEFAULT 0,
    `priviledge_birthday`       INT          DEFAULT 0,
    `note`                      VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_member（会员）
CREATE TABLE IF NOT EXISTS `ums_member` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `member_level_id`           BIGINT       DEFAULT NULL,
    `username`                  VARCHAR(64)  NOT NULL,
    `password`                  VARCHAR(255) NOT NULL,
    `nickname`                  VARCHAR(64)  DEFAULT NULL,
    `phone`                     VARCHAR(20)  DEFAULT NULL,
    `status`                    INT          DEFAULT 1 COMMENT '1->启用;0->禁用',
    `create_time`               DATETIME     DEFAULT NULL,
    `icon`                      VARCHAR(500) DEFAULT NULL,
    `avatar_url`                VARCHAR(500) DEFAULT NULL,
    `gender`                    INT          DEFAULT NULL,
    `birthday`                  DATE         DEFAULT NULL,
    `city`                      VARCHAR(64)  DEFAULT NULL,
    `job`                       VARCHAR(100) DEFAULT NULL,
    `personalized_signature`    VARCHAR(200) DEFAULT NULL,
    `source_type`               INT          DEFAULT NULL,
    `integration`               INT          DEFAULT 0,
    `growth`                    INT          DEFAULT 0,
    `luckey_count`              INT          DEFAULT 0,
    `history_integration`       INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_brand（品牌）
CREATE TABLE IF NOT EXISTS `pms_brand` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `name`                      VARCHAR(64)  DEFAULT NULL,
    `first_letter`              VARCHAR(8)   DEFAULT NULL,
    `sort`                      INT          DEFAULT 0,
    `factory_status`            INT          DEFAULT 0,
    `show_status`               INT          DEFAULT 1,
    `product_count`             INT          DEFAULT 0,
    `product_comment_count`     INT          DEFAULT 0,
    `logo`                      VARCHAR(500) DEFAULT NULL,
    `big_pic`                   VARCHAR(500) DEFAULT NULL,
    `brand_story`               TEXT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_product_category（商品分类）
CREATE TABLE IF NOT EXISTS `pms_product_category` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`                 BIGINT       DEFAULT 0,
    `name`                      VARCHAR(64)  DEFAULT NULL,
    `level`                     INT          DEFAULT 0,
    `product_count`             INT          DEFAULT 0,
    `product_unit`              VARCHAR(16)  DEFAULT NULL,
    `nav_status`                INT          DEFAULT 0,
    `show_status`               INT          DEFAULT 1,
    `sort`                      INT          DEFAULT 0,
    `icon`                      VARCHAR(500) DEFAULT NULL,
    `keywords`                  VARCHAR(255) DEFAULT NULL,
    `description`               TEXT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_product（商品）
CREATE TABLE IF NOT EXISTS `pms_product` (
    `id`                            BIGINT         NOT NULL AUTO_INCREMENT,
    `brand_id`                      BIGINT         DEFAULT NULL,
    `product_category_id`           BIGINT         DEFAULT NULL,
    `feight_template_id`            BIGINT         DEFAULT NULL,
    `product_attribute_category_id` BIGINT         DEFAULT NULL,
    `name`                          VARCHAR(200)   NOT NULL,
    `pic`                           VARCHAR(500)   DEFAULT NULL,
    `product_sn`                    VARCHAR(64)    DEFAULT NULL,
    `delete_status`                 INT            DEFAULT 0,
    `publish_status`                INT            DEFAULT 1,
    `new_status`                    INT            DEFAULT 0,
    `recommand_status`              INT            DEFAULT 0,
    `verify_status`                 INT            DEFAULT 0,
    `sort`                          INT            DEFAULT 0,
    `sale`                          INT            DEFAULT 0,
    `price`                         DECIMAL(10,2)  DEFAULT NULL,
    `promotion_price`               DECIMAL(10,2)  DEFAULT NULL,
    `gift_growth`                   INT            DEFAULT 0,
    `gift_point`                    INT            DEFAULT 0,
    `use_point_limit`               INT            DEFAULT 0,
    `sub_title`                     VARCHAR(255)   DEFAULT NULL,
    `original_price`                DECIMAL(10,2)  DEFAULT NULL,
    `stock`                         INT            DEFAULT 0,
    `low_stock`                     INT            DEFAULT 0,
    `unit`                          VARCHAR(16)    DEFAULT NULL,
    `weight`                        DECIMAL(10,2)  DEFAULT NULL,
    `preview_status`                INT            DEFAULT 0,
    `service_ids`                   VARCHAR(64)    DEFAULT NULL,
    `keywords`                      VARCHAR(255)   DEFAULT NULL,
    `note`                          VARCHAR(255)   DEFAULT NULL,
    `album_pics`                    VARCHAR(1000)  DEFAULT NULL,
    `detail_title`                  VARCHAR(255)   DEFAULT NULL,
    `promotion_start_time`          DATETIME       DEFAULT NULL,
    `promotion_end_time`            DATETIME       DEFAULT NULL,
    `promotion_per_limit`           INT            DEFAULT 0,
    `promotion_type`                INT            DEFAULT 0,
    `brand_name`                    VARCHAR(64)    DEFAULT NULL,
    `product_category_name`         VARCHAR(64)    DEFAULT NULL,
    `description`                   TEXT,
    `detail_desc`                   TEXT,
    `detail_html`                   TEXT,
    `detail_mobile_html`            TEXT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_sku_stock（SKU 库存）
CREATE TABLE IF NOT EXISTS `pms_sku_stock` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `product_id`                BIGINT         DEFAULT NULL,
    `sku_code`                  VARCHAR(64)    DEFAULT NULL,
    `price`                     DECIMAL(10,2)  DEFAULT NULL,
    `stock`                     INT            DEFAULT 0,
    `low_stock`                 INT            DEFAULT 0,
    `pic`                       VARCHAR(500)   DEFAULT NULL,
    `sale`                      INT            DEFAULT 0,
    `promotion_price`           DECIMAL(10,2)  DEFAULT NULL,
    `lock_stock`                INT            DEFAULT 0,
    `sp_data`                   VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_cart_item（购物车）
CREATE TABLE IF NOT EXISTS `oms_cart_item` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `product_id`                BIGINT         DEFAULT NULL,
    `product_sku_id`            BIGINT         DEFAULT NULL,
    `member_id`                 BIGINT         DEFAULT NULL,
    `quantity`                  INT            DEFAULT 1,
    `price`                     DECIMAL(10,2)  DEFAULT NULL,
    `product_pic`               VARCHAR(500)   DEFAULT NULL,
    `product_name`              VARCHAR(200)   DEFAULT NULL,
    `product_sub_title`         VARCHAR(255)   DEFAULT NULL,
    `product_sku_code`          VARCHAR(64)    DEFAULT NULL,
    `member_nickname`           VARCHAR(64)    DEFAULT NULL,
    `create_date`               DATETIME       DEFAULT NULL,
    `modify_date`               DATETIME       DEFAULT NULL,
    `delete_status`             INT            DEFAULT 0,
    `product_category_id`       BIGINT         DEFAULT NULL,
    `product_brand`             VARCHAR(64)    DEFAULT NULL,
    `product_sn`                VARCHAR(64)    DEFAULT NULL,
    `product_attr`              VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order（订单）
CREATE TABLE IF NOT EXISTS `oms_order` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `member_id`                 BIGINT         DEFAULT NULL,
    `coupon_id`                 BIGINT         DEFAULT NULL,
    `order_sn`                  VARCHAR(64)    DEFAULT NULL,
    `create_time`               DATETIME       DEFAULT NULL,
    `member_username`           VARCHAR(64)    DEFAULT NULL,
    `total_amount`              DECIMAL(10,2)  DEFAULT NULL,
    `pay_amount`                DECIMAL(10,2)  DEFAULT NULL,
    `freight_amount`            DECIMAL(10,2)  DEFAULT NULL,
    `promotion_amount`          DECIMAL(10,2)  DEFAULT NULL,
    `integration_amount`        DECIMAL(10,2)  DEFAULT NULL,
    `coupon_amount`             DECIMAL(10,2)  DEFAULT NULL,
    `discount_amount`           DECIMAL(10,2)  DEFAULT NULL,
    `pay_type`                  INT            DEFAULT 0,
    `source_type`               INT            DEFAULT 0,
    `status`                    INT            DEFAULT 0,
    `order_type`                INT            DEFAULT 0,
    `delivery_company`          VARCHAR(64)    DEFAULT NULL,
    `delivery_sn`               VARCHAR(64)    DEFAULT NULL,
    `auto_confirm_day`          INT            DEFAULT NULL,
    `integration`               INT            DEFAULT NULL,
    `growth`                    INT            DEFAULT NULL,
    `promotion_info`            VARCHAR(255)   DEFAULT NULL,
    `bill_type`                 INT            DEFAULT NULL,
    `bill_header`               VARCHAR(200)   DEFAULT NULL,
    `bill_content`              VARCHAR(200)   DEFAULT NULL,
    `bill_receiver_phone`       VARCHAR(20)    DEFAULT NULL,
    `bill_receiver_email`       VARCHAR(100)   DEFAULT NULL,
    `receiver_name`             VARCHAR(64)    DEFAULT NULL,
    `receiver_phone`            VARCHAR(20)    DEFAULT NULL,
    `receiver_post_code`        VARCHAR(16)    DEFAULT NULL,
    `receiver_province`         VARCHAR(64)    DEFAULT NULL,
    `receiver_city`             VARCHAR(64)    DEFAULT NULL,
    `receiver_region`           VARCHAR(64)    DEFAULT NULL,
    `receiver_detail_address`   VARCHAR(255)   DEFAULT NULL,
    `note`                      VARCHAR(500)   DEFAULT NULL,
    `confirm_status`            INT            DEFAULT 0,
    `delete_status`             INT            DEFAULT 0,
    `use_integration`           INT            DEFAULT NULL,
    `payment_time`              DATETIME       DEFAULT NULL,
    `delivery_time`             DATETIME       DEFAULT NULL,
    `receive_time`              DATETIME       DEFAULT NULL,
    `comment_time`              DATETIME       DEFAULT NULL,
    `modify_time`               DATETIME       DEFAULT NULL,
    `payment_id`                VARCHAR(64)    DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_item（订单项）
CREATE TABLE IF NOT EXISTS `oms_order_item` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`                  BIGINT         DEFAULT NULL,
    `order_sn`                  VARCHAR(64)    DEFAULT NULL,
    `product_id`                BIGINT         DEFAULT NULL,
    `product_pic`               VARCHAR(500)   DEFAULT NULL,
    `product_name`              VARCHAR(200)   DEFAULT NULL,
    `product_brand`             VARCHAR(64)    DEFAULT NULL,
    `product_sn`                VARCHAR(64)    DEFAULT NULL,
    `product_price`             DECIMAL(10,2)  DEFAULT NULL,
    `product_quantity`          INT            DEFAULT 1,
    `product_sku_id`            BIGINT         DEFAULT NULL,
    `product_sku_code`          VARCHAR(64)    DEFAULT NULL,
    `product_category_id`       BIGINT         DEFAULT NULL,
    `promotion_name`            VARCHAR(200)   DEFAULT NULL,
    `promotion_amount`          DECIMAL(10,2)  DEFAULT NULL,
    `coupon_amount`             DECIMAL(10,2)  DEFAULT NULL,
    `integration_amount`        DECIMAL(10,2)  DEFAULT NULL,
    `real_amount`               DECIMAL(10,2)  DEFAULT NULL,
    `gift_integration`          INT            DEFAULT 0,
    `gift_growth`               INT            DEFAULT 0,
    `product_attr`              VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_setting（订单设置）
CREATE TABLE IF NOT EXISTS `oms_order_setting` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `flash_order_overtime`      INT            DEFAULT 60,
    `normal_order_overtime`     INT            DEFAULT 120,
    `confirm_overtime`          INT            DEFAULT 15,
    `finish_overtime`           INT            DEFAULT 7,
    `comment_overtime`          INT            DEFAULT 7,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_operate_history（订单操作历史）
CREATE TABLE IF NOT EXISTS `oms_order_operate_history` (
    `id`                        BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`                  BIGINT         DEFAULT NULL,
    `operate_man`               VARCHAR(64)    DEFAULT NULL,
    `order_status`              INT            DEFAULT NULL,
    `note`                      VARCHAR(500)   DEFAULT NULL,
    `create_time`               DATETIME       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 种子数据 =====

-- 默认会员等级
INSERT INTO `ums_member_level` (`name`, `growth_point`, `default_status`) VALUES ('普通会员', 0, 1);

-- 默认订单设置
INSERT INTO `oms_order_setting` (`flash_order_overtime`, `normal_order_overtime`, `confirm_overtime`, `finish_overtime`, `comment_overtime`)
VALUES (60, 120, 15, 7, 7);

-- 测试品牌
INSERT INTO `pms_brand` (`name`, `first_letter`, `sort`, `show_status`, `factory_status`) VALUES ('测试品牌', 'C', 0, 1, 1);

-- 测试分类
INSERT INTO `pms_product_category` (`parent_id`, `name`, `level`, `show_status`, `nav_status`, `sort`)
VALUES (0, '测试分类', 0, 1, 1, 0);

-- 测试商品
INSERT INTO `pms_product` (`brand_id`, `product_category_id`, `name`, `product_sn`, `price`, `stock`, `publish_status`, `verify_status`, `delete_status`, `sub_title`, `brand_name`, `product_category_name`)
VALUES (1, 1, '测试商品A', 'TEST-001', 99.00, 100, 1, 1, 0, '测试副标题', '测试品牌', '测试分类');

-- 测试 SKU
INSERT INTO `pms_sku_stock` (`product_id`, `sku_code`, `price`, `stock`, `low_stock`, `lock_stock`)
VALUES (1, 'SKU-TEST-001', 99.00, 100, 10, 0);
