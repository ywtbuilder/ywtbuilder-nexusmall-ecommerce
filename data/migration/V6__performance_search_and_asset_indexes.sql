-- ============================================================
-- V6: App 核心链路性能索引与约束补强
-- 目标：
--   1) 用户端查询高频索引补齐
--   2) 来源唯一性与枚举约束补齐
--   3) 可重复执行（幂等）
-- ============================================================

SET @db := DATABASE();

-- ------------------------------------------------------------
-- pms_product：列表/搜索可见性复合索引
-- ------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'pms_product'
      AND index_name = 'idx_app_visible_category_sort'
);
SET @sql := IF(
    @idx_exists = 0,
    'ALTER TABLE pms_product ADD INDEX idx_app_visible_category_sort (publish_status, delete_status, product_category_id, sort, id)',
    'SELECT ''skip idx_app_visible_category_sort'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- pms_sku_stock：按商品维度读取库存/价格
-- ------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'pms_sku_stock'
      AND index_name = 'idx_product_stock_price'
);
SET @sql := IF(
    @idx_exists = 0,
    'ALTER TABLE pms_sku_stock ADD INDEX idx_product_stock_price (product_id, stock, price, id)',
    'SELECT ''skip idx_product_stock_price'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- pms_product_image：详情页图片类型 + 排序读取
-- ------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'pms_product_image'
      AND index_name = 'idx_product_image_type_sort_id'
);
SET @sql := IF(
    @idx_exists = 0,
    'ALTER TABLE pms_product_image ADD INDEX idx_product_image_type_sort_id (product_id, image_type, sort_order, id)',
    'SELECT ''skip idx_product_image_type_sort_id'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- pms_product_source：来源唯一约束（若表存在）
-- ------------------------------------------------------------
SET @table_exists := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = @db
      AND table_name = 'pms_product_source'
);
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = @db
      AND table_name = 'pms_product_source'
      AND index_name = 'uq_source_platform_item'
);
SET @sql := IF(
    @table_exists = 1 AND @idx_exists = 0,
    'ALTER TABLE pms_product_source ADD UNIQUE KEY uq_source_platform_item (source_platform, source_item_id)',
    'SELECT ''skip uq_source_platform_item'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- pms_product_image.image_type 白名单 CHECK（若未存在）
-- ------------------------------------------------------------
SET @table_exists := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = @db
      AND table_name = 'pms_product_image'
);
SET @chk_exists := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = @db
      AND table_name = 'pms_product_image'
      AND constraint_type = 'CHECK'
      AND constraint_name = 'chk_pms_product_image_type'
);
SET @sql := IF(
    @table_exists = 1 AND @chk_exists = 0,
    'ALTER TABLE pms_product_image ADD CONSTRAINT chk_pms_product_image_type CHECK (image_type IN (0,1,2,3,4,5))',
    'SELECT ''skip chk_pms_product_image_type'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- pms_asset_source.media_kind 白名单 CHECK（若表存在且未存在）
-- ------------------------------------------------------------
SET @table_exists := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = @db
      AND table_name = 'pms_asset_source'
);
SET @chk_exists := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = @db
      AND table_name = 'pms_asset_source'
      AND constraint_type = 'CHECK'
      AND constraint_name = 'chk_pms_asset_source_media_kind'
);
SET @sql := IF(
    @table_exists = 1 AND @chk_exists = 0,
    'ALTER TABLE pms_asset_source ADD CONSTRAINT chk_pms_asset_source_media_kind CHECK (media_kind IN (1,2,3,4,5,9))',
    'SELECT ''skip chk_pms_asset_source_media_kind'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
