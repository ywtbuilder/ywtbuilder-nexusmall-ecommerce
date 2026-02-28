-- =================================================================
-- Admin API 集成测试用最小化 DDL — 包含 RBAC 表 + 业务基础表
-- 适用于 TestContainers MySQL 8.0 容器
-- =================================================================

-- ums_admin（后台用户）
CREATE TABLE IF NOT EXISTS `ums_admin` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(64)  DEFAULT NULL,
    `password`    VARCHAR(64)  DEFAULT NULL,
    `icon`        VARCHAR(500) DEFAULT NULL,
    `email`       VARCHAR(100) DEFAULT NULL,
    `nick_name`   VARCHAR(200) DEFAULT NULL,
    `note`        VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME     DEFAULT NULL,
    `login_time`  DATETIME     DEFAULT NULL,
    `status`      INT          DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_role（角色）
CREATE TABLE IF NOT EXISTS `ums_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `admin_count` INT          DEFAULT 0,
    `create_time` DATETIME     DEFAULT NULL,
    `status`      INT          DEFAULT 1,
    `sort`        INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_resource（资源）
CREATE TABLE IF NOT EXISTS `ums_resource` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `create_time` DATETIME     DEFAULT NULL,
    `name`        VARCHAR(200) DEFAULT NULL,
    `url`         VARCHAR(200) DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `category_id` BIGINT       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_resource_category（资源分类）
CREATE TABLE IF NOT EXISTS `ums_resource_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `create_time` DATETIME     DEFAULT NULL,
    `name`        VARCHAR(200) DEFAULT NULL,
    `sort`        INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_admin_role_relation（用户-角色关系）
CREATE TABLE IF NOT EXISTS `ums_admin_role_relation` (
    `id`       BIGINT NOT NULL AUTO_INCREMENT,
    `admin_id` BIGINT DEFAULT NULL,
    `role_id`  BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_role_resource_relation（角色-资源关系）
CREATE TABLE IF NOT EXISTS `ums_role_resource_relation` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `role_id`     BIGINT DEFAULT NULL,
    `resource_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_menu（菜单）
CREATE TABLE IF NOT EXISTS `ums_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       DEFAULT 0,
    `create_time` DATETIME     DEFAULT NULL,
    `title`       VARCHAR(100) DEFAULT NULL,
    `level`       INT          DEFAULT 0,
    `sort`        INT          DEFAULT 0,
    `name`        VARCHAR(100) DEFAULT NULL,
    `icon`        VARCHAR(200) DEFAULT NULL,
    `hidden`      INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_role_menu_relation（角色-菜单关系）
CREATE TABLE IF NOT EXISTS `ums_role_menu_relation` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT DEFAULT NULL,
    `menu_id` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ums_member_level（会员等级 — 被部分 admin 端点引用）
CREATE TABLE IF NOT EXISTS `ums_member_level` (
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `name`                      VARCHAR(100) DEFAULT NULL,
    `growth_point`              INT          DEFAULT 0,
    `default_status`            INT          DEFAULT 0,
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

-- pms_brand（品牌）
CREATE TABLE IF NOT EXISTS `pms_brand` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
    `name`                  VARCHAR(64)  DEFAULT NULL,
    `first_letter`          VARCHAR(8)   DEFAULT NULL,
    `sort`                  INT          DEFAULT 0,
    `factory_status`        INT          DEFAULT 0,
    `show_status`           INT          DEFAULT 1,
    `product_count`         INT          DEFAULT 0,
    `product_comment_count` INT          DEFAULT 0,
    `logo`                  VARCHAR(500) DEFAULT NULL,
    `big_pic`               VARCHAR(500) DEFAULT NULL,
    `brand_story`           TEXT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_product_category（商品分类）
CREATE TABLE IF NOT EXISTS `pms_product_category` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`     BIGINT       DEFAULT 0,
    `name`          VARCHAR(64)  DEFAULT NULL,
    `level`         INT          DEFAULT 0,
    `product_count` INT          DEFAULT 0,
    `product_unit`  VARCHAR(16)  DEFAULT NULL,
    `nav_status`    INT          DEFAULT 0,
    `show_status`   INT          DEFAULT 1,
    `sort`          INT          DEFAULT 0,
    `icon`          VARCHAR(500) DEFAULT NULL,
    `keywords`      VARCHAR(255) DEFAULT NULL,
    `description`   TEXT,
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

-- pms_product_attribute_category（商品属性分类）
CREATE TABLE IF NOT EXISTS `pms_product_attribute_category` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(64)  DEFAULT NULL,
    `attribute_count` INT          DEFAULT 0,
    `param_count`     INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_product_attribute（商品属性）
CREATE TABLE IF NOT EXISTS `pms_product_attribute` (
    `id`                            BIGINT       NOT NULL AUTO_INCREMENT,
    `product_attribute_category_id` BIGINT       DEFAULT NULL,
    `name`                          VARCHAR(64)  DEFAULT NULL,
    `select_type`                   INT          DEFAULT 0,
    `input_type`                    INT          DEFAULT 0,
    `input_list`                    VARCHAR(255) DEFAULT NULL,
    `sort`                          INT          DEFAULT 0,
    `filter_type`                   INT          DEFAULT 0,
    `search_type`                   INT          DEFAULT 0,
    `related_status`                INT          DEFAULT 0,
    `hand_add_status`               INT          DEFAULT 0,
    `type`                          INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- pms_sku_stock（SKU 库存）
CREATE TABLE IF NOT EXISTS `pms_sku_stock` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT,
    `product_id`      BIGINT         DEFAULT NULL,
    `sku_code`        VARCHAR(64)    DEFAULT NULL,
    `price`           DECIMAL(10,2)  DEFAULT NULL,
    `stock`           INT            DEFAULT 0,
    `low_stock`       INT            DEFAULT 0,
    `pic`             VARCHAR(500)   DEFAULT NULL,
    `sale`            INT            DEFAULT 0,
    `promotion_price` DECIMAL(10,2)  DEFAULT NULL,
    `lock_stock`      INT            DEFAULT 0,
    `sp_data`         VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order（订单）
CREATE TABLE IF NOT EXISTS `oms_order` (
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT,
    `member_id`               BIGINT         DEFAULT NULL,
    `coupon_id`               BIGINT         DEFAULT NULL,
    `order_sn`                VARCHAR(64)    DEFAULT NULL,
    `create_time`             DATETIME       DEFAULT NULL,
    `member_username`         VARCHAR(64)    DEFAULT NULL,
    `total_amount`            DECIMAL(10,2)  DEFAULT NULL,
    `pay_amount`              DECIMAL(10,2)  DEFAULT NULL,
    `freight_amount`          DECIMAL(10,2)  DEFAULT NULL,
    `promotion_amount`        DECIMAL(10,2)  DEFAULT NULL,
    `integration_amount`      DECIMAL(10,2)  DEFAULT NULL,
    `coupon_amount`           DECIMAL(10,2)  DEFAULT NULL,
    `discount_amount`         DECIMAL(10,2)  DEFAULT NULL,
    `pay_type`                INT            DEFAULT 0,
    `source_type`             INT            DEFAULT 0,
    `status`                  INT            DEFAULT 0,
    `order_type`              INT            DEFAULT 0,
    `delivery_company`        VARCHAR(64)    DEFAULT NULL,
    `delivery_sn`             VARCHAR(64)    DEFAULT NULL,
    `auto_confirm_day`        INT            DEFAULT NULL,
    `integration`             INT            DEFAULT NULL,
    `growth`                  INT            DEFAULT NULL,
    `promotion_info`          VARCHAR(255)   DEFAULT NULL,
    `bill_type`               INT            DEFAULT NULL,
    `bill_header`             VARCHAR(200)   DEFAULT NULL,
    `bill_content`            VARCHAR(200)   DEFAULT NULL,
    `bill_receiver_phone`     VARCHAR(20)    DEFAULT NULL,
    `bill_receiver_email`     VARCHAR(100)   DEFAULT NULL,
    `receiver_name`           VARCHAR(64)    DEFAULT NULL,
    `receiver_phone`          VARCHAR(20)    DEFAULT NULL,
    `receiver_post_code`      VARCHAR(16)    DEFAULT NULL,
    `receiver_province`       VARCHAR(64)    DEFAULT NULL,
    `receiver_city`           VARCHAR(64)    DEFAULT NULL,
    `receiver_region`         VARCHAR(64)    DEFAULT NULL,
    `receiver_detail_address` VARCHAR(255)   DEFAULT NULL,
    `note`                    VARCHAR(500)   DEFAULT NULL,
    `confirm_status`          INT            DEFAULT 0,
    `delete_status`           INT            DEFAULT 0,
    `use_integration`         INT            DEFAULT NULL,
    `payment_time`            DATETIME       DEFAULT NULL,
    `delivery_time`           DATETIME       DEFAULT NULL,
    `receive_time`            DATETIME       DEFAULT NULL,
    `comment_time`            DATETIME       DEFAULT NULL,
    `modify_time`             DATETIME       DEFAULT NULL,
    `payment_id`              VARCHAR(64)    DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_item（订单项）
CREATE TABLE IF NOT EXISTS `oms_order_item` (
    `id`                 BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`           BIGINT         DEFAULT NULL,
    `order_sn`           VARCHAR(64)    DEFAULT NULL,
    `product_id`         BIGINT         DEFAULT NULL,
    `product_pic`        VARCHAR(500)   DEFAULT NULL,
    `product_name`       VARCHAR(200)   DEFAULT NULL,
    `product_brand`      VARCHAR(64)    DEFAULT NULL,
    `product_sn`         VARCHAR(64)    DEFAULT NULL,
    `product_price`      DECIMAL(10,2)  DEFAULT NULL,
    `product_quantity`   INT            DEFAULT 1,
    `product_sku_id`     BIGINT         DEFAULT NULL,
    `product_sku_code`   VARCHAR(64)    DEFAULT NULL,
    `product_category_id` BIGINT        DEFAULT NULL,
    `promotion_name`     VARCHAR(200)   DEFAULT NULL,
    `promotion_amount`   DECIMAL(10,2)  DEFAULT NULL,
    `coupon_amount`      DECIMAL(10,2)  DEFAULT NULL,
    `integration_amount` DECIMAL(10,2)  DEFAULT NULL,
    `real_amount`        DECIMAL(10,2)  DEFAULT NULL,
    `gift_integration`   INT            DEFAULT 0,
    `gift_growth`        INT            DEFAULT 0,
    `product_attr`       VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_setting（订单设置）
CREATE TABLE IF NOT EXISTS `oms_order_setting` (
    `id`                   BIGINT NOT NULL AUTO_INCREMENT,
    `flash_order_overtime`  INT   DEFAULT 60,
    `normal_order_overtime` INT   DEFAULT 120,
    `confirm_overtime`      INT   DEFAULT 15,
    `finish_overtime`       INT   DEFAULT 7,
    `comment_overtime`      INT   DEFAULT 7,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_operate_history
CREATE TABLE IF NOT EXISTS `oms_order_operate_history` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`     BIGINT       DEFAULT NULL,
    `operate_man`  VARCHAR(64)  DEFAULT NULL,
    `order_status` INT          DEFAULT NULL,
    `note`         VARCHAR(500) DEFAULT NULL,
    `create_time`  DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_return_apply（退货申请）
CREATE TABLE IF NOT EXISTS `oms_order_return_apply` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`          BIGINT         DEFAULT NULL,
    `company_address_id` BIGINT        DEFAULT NULL,
    `product_id`        BIGINT         DEFAULT NULL,
    `order_sn`          VARCHAR(64)    DEFAULT NULL,
    `create_time`       DATETIME       DEFAULT NULL,
    `member_username`   VARCHAR(64)    DEFAULT NULL,
    `return_amount`     DECIMAL(10,2)  DEFAULT NULL,
    `return_name`       VARCHAR(100)   DEFAULT NULL,
    `return_phone`      VARCHAR(20)    DEFAULT NULL,
    `status`            INT            DEFAULT 0,
    `handle_time`       DATETIME       DEFAULT NULL,
    `product_pic`       VARCHAR(500)   DEFAULT NULL,
    `product_name`      VARCHAR(200)   DEFAULT NULL,
    `product_brand`     VARCHAR(64)    DEFAULT NULL,
    `product_attr`      VARCHAR(500)   DEFAULT NULL,
    `product_count`     INT            DEFAULT 1,
    `product_price`     DECIMAL(10,2)  DEFAULT NULL,
    `product_real_price` DECIMAL(10,2) DEFAULT NULL,
    `reason`            VARCHAR(200)   DEFAULT NULL,
    `description`       VARCHAR(500)   DEFAULT NULL,
    `proof_pics`        VARCHAR(1000)  DEFAULT NULL,
    `handle_note`       VARCHAR(500)   DEFAULT NULL,
    `handle_man`        VARCHAR(100)   DEFAULT NULL,
    `receive_man`       VARCHAR(100)   DEFAULT NULL,
    `receive_time`      DATETIME       DEFAULT NULL,
    `receive_note`      VARCHAR(500)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_order_return_reason（退货原因）
CREATE TABLE IF NOT EXISTS `oms_order_return_reason` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) DEFAULT NULL,
    `sort`        INT          DEFAULT 0,
    `status`      INT          DEFAULT 1,
    `create_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- oms_company_address（公司地址）
CREATE TABLE IF NOT EXISTS `oms_company_address` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `address_name`    VARCHAR(200) DEFAULT NULL,
    `send_status`     INT          DEFAULT NULL,
    `receive_status`  INT          DEFAULT NULL,
    `name`            VARCHAR(64)  DEFAULT NULL,
    `phone`           VARCHAR(20)  DEFAULT NULL,
    `province`        VARCHAR(64)  DEFAULT NULL,
    `city`            VARCHAR(64)  DEFAULT NULL,
    `region`          VARCHAR(64)  DEFAULT NULL,
    `detail_address`  VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_coupon（优惠券）
CREATE TABLE IF NOT EXISTS `sms_coupon` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT,
    `type`          INT            DEFAULT 0,
    `name`          VARCHAR(100)   DEFAULT NULL,
    `platform`      INT            DEFAULT 0,
    `count`         INT            DEFAULT 0,
    `amount`        DECIMAL(10,2)  DEFAULT NULL,
    `per_limit`     INT            DEFAULT 1,
    `min_point`     DECIMAL(10,2)  DEFAULT NULL,
    `start_time`    DATETIME       DEFAULT NULL,
    `end_time`      DATETIME       DEFAULT NULL,
    `use_type`      INT            DEFAULT 0,
    `note`          VARCHAR(200)   DEFAULT NULL,
    `publish_count` INT            DEFAULT 0,
    `use_count`     INT            DEFAULT 0,
    `receive_count` INT            DEFAULT 0,
    `enable_time`   DATETIME       DEFAULT NULL,
    `code`          VARCHAR(64)    DEFAULT NULL,
    `member_level`  INT            DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_flash_promotion（秒杀活动）
CREATE TABLE IF NOT EXISTS `sms_flash_promotion` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(200) DEFAULT NULL,
    `start_date`  DATE         DEFAULT NULL,
    `end_date`    DATE         DEFAULT NULL,
    `status`      INT          DEFAULT 0,
    `create_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_flash_promotion_session（秒杀场次）
CREATE TABLE IF NOT EXISTS `sms_flash_promotion_session` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(200) DEFAULT NULL,
    `start_time`  TIME         DEFAULT NULL,
    `end_time`    TIME         DEFAULT NULL,
    `status`      INT          DEFAULT 0,
    `create_time` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_flash_promotion_product_relation
CREATE TABLE IF NOT EXISTS `sms_flash_promotion_product_relation` (
    `id`                         BIGINT         NOT NULL AUTO_INCREMENT,
    `flash_promotion_id`         BIGINT         DEFAULT NULL,
    `flash_promotion_session_id` BIGINT         DEFAULT NULL,
    `product_id`                 BIGINT         DEFAULT NULL,
    `flash_promotion_price`      DECIMAL(10,2)  DEFAULT NULL,
    `flash_promotion_count`      INT            DEFAULT 0,
    `flash_promotion_limit`      INT            DEFAULT 1,
    `sort`                       INT            DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_home_advertise（首页广告）
CREATE TABLE IF NOT EXISTS `sms_home_advertise` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) DEFAULT NULL,
    `type`        INT          DEFAULT 0,
    `pic`         VARCHAR(500) DEFAULT NULL,
    `start_time`  DATETIME     DEFAULT NULL,
    `end_time`    DATETIME     DEFAULT NULL,
    `status`      INT          DEFAULT 1,
    `click_count` INT          DEFAULT 0,
    `order_count` INT          DEFAULT 0,
    `url`         VARCHAR(500) DEFAULT NULL,
    `note`        VARCHAR(500) DEFAULT NULL,
    `sort`        INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_home_brand（首页推荐品牌）
CREATE TABLE IF NOT EXISTS `sms_home_brand` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `brand_id`         BIGINT       DEFAULT NULL,
    `brand_name`       VARCHAR(64)  DEFAULT NULL,
    `recommend_status` INT          DEFAULT 1,
    `sort`             INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_home_new_product（首页新品推荐）
CREATE TABLE IF NOT EXISTS `sms_home_new_product` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `product_id`       BIGINT       DEFAULT NULL,
    `product_name`     VARCHAR(200) DEFAULT NULL,
    `recommend_status` INT          DEFAULT 1,
    `sort`             INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_home_recommend_product（首页推荐商品）
CREATE TABLE IF NOT EXISTS `sms_home_recommend_product` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `product_id`       BIGINT       DEFAULT NULL,
    `product_name`     VARCHAR(200) DEFAULT NULL,
    `recommend_status` INT          DEFAULT 1,
    `sort`             INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sms_home_recommend_subject（首页推荐专题）
CREATE TABLE IF NOT EXISTS `sms_home_recommend_subject` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `subject_id`       BIGINT       DEFAULT NULL,
    `subject_name`     VARCHAR(200) DEFAULT NULL,
    `recommend_status` INT          DEFAULT 1,
    `sort`             INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 种子数据 =====

-- 超级管理员（密码 admin123, BCrypt）
INSERT INTO `ums_admin` (`username`, `password`, `nick_name`, `status`, `create_time`)
VALUES ('admin', '$2a$10$NZ5o7r2E.ayT2ZoxgjlI.eJ6OEYqjH7INR/F.mXDbjZJi9HF0YCZS', '系统管理员', 1, NOW());

-- 默认角色
INSERT INTO `ums_role` (`name`, `description`, `status`, `sort`, `create_time`)
VALUES ('超级管理员', '拥有所有权限', 1, 0, NOW());

-- 管理员-角色关联
INSERT INTO `ums_admin_role_relation` (`admin_id`, `role_id`) VALUES (1, 1);

-- 默认订单设置
INSERT INTO `oms_order_setting` (`flash_order_overtime`, `normal_order_overtime`, `confirm_overtime`, `finish_overtime`, `comment_overtime`)
VALUES (60, 120, 15, 7, 7);

-- 默认退货原因
INSERT INTO `oms_order_return_reason` (`name`, `sort`, `status`, `create_time`) VALUES ('质量问题', 0, 1, NOW());
INSERT INTO `oms_order_return_reason` (`name`, `sort`, `status`, `create_time`) VALUES ('尺码太大', 1, 1, NOW());

-- 测试品牌
INSERT INTO `pms_brand` (`name`, `first_letter`, `sort`, `show_status`, `factory_status`) VALUES ('测试品牌', 'C', 0, 1, 1);

-- 测试分类
INSERT INTO `pms_product_category` (`parent_id`, `name`, `level`, `show_status`, `nav_status`, `sort`)
VALUES (0, '测试分类', 0, 1, 1, 0);

-- 测试商品
INSERT INTO `pms_product` (`brand_id`, `product_category_id`, `name`, `product_sn`, `price`, `stock`, `publish_status`, `verify_status`, `delete_status`, `sub_title`, `brand_name`, `product_category_name`)
VALUES (1, 1, '管理端测试商品', 'ADMIN-TEST-001', 199.00, 50, 1, 1, 0, '管理端测试', '测试品牌', '测试分类');

-- 测试订单
INSERT INTO `oms_order` (`member_id`, `order_sn`, `create_time`, `member_username`, `total_amount`, `pay_amount`, `status`, `delete_status`, `receiver_name`, `receiver_phone`)
VALUES (1, 'TEST-ORDER-001', NOW(), 'testuser', 199.00, 199.00, 0, 0, '张三', '13800138000');
