-- ============================================================
-- V5: 商品图片 BLOB 存储 + 规格参数 + 内容归档
-- 目标：所有图片本体入库（LONGBLOB），运行时只读 MySQL，
--       禁止访问 CDN / 本地磁盘 / 外链
-- ============================================================

-- 1. 图片资源表（按 SHA-256 去重存储，LONGBLOB）
CREATE TABLE IF NOT EXISTS pms_asset (
  id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
  image_hash      VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hex, 用于去重和 ETag',
  mime_type       VARCHAR(50)  NOT NULL COMMENT '如 image/jpeg, image/png, image/webp',
  width           INT          DEFAULT 0 COMMENT '图片宽度 px',
  height          INT          DEFAULT 0 COMMENT '图片高度 px',
  file_size       INT          DEFAULT 0 COMMENT '文件字节数',
  original_filename VARCHAR(500) COMMENT '原始文件名',
  image_data      LONGBLOB     NOT NULL COMMENT '图片二进制本体',
  created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_image_hash (image_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片资源 BLOB 存储（hash 去重）';

-- 2. 商品-图片关联表
CREATE TABLE IF NOT EXISTS pms_product_image (
  id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
  product_id      BIGINT       NOT NULL COMMENT 'FK → pms_product.id',
  asset_id        BIGINT       NOT NULL COMMENT 'FK → pms_asset.id',
  image_type      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=轮播图 1=详情图 2=规格图 3=SKU图 4=评论图 5=其他',
  sort_order      INT          DEFAULT 0 COMMENT '排序（同类型内从小到大）',
  created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_product_id (product_id),
  INDEX idx_asset_id (asset_id),
  INDEX idx_type_sort (product_id, image_type, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品-图片关联';

-- 3. 商品规格参数表（结构化 key-value）
CREATE TABLE IF NOT EXISTS pms_product_spec (
  id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
  product_id      BIGINT       NOT NULL COMMENT 'FK → pms_product.id',
  spec_group      VARCHAR(100) COMMENT '参数组名（如"基本参数"、"规格与包装"）',
  spec_name       VARCHAR(200) NOT NULL COMMENT '参数名',
  spec_value      TEXT         COMMENT '参数值',
  sort_order      INT          DEFAULT 0,
  INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格参数（结构化 key-value）';

-- 4. 商品内容归档表（原始导出包 + 文本备份）
CREATE TABLE IF NOT EXISTS pms_product_content (
  id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
  product_id          BIGINT       NOT NULL COMMENT 'FK → pms_product.id',
  description_text    TEXT         COMMENT '纯文本描述 / 卖点',
  spec_raw_json       MEDIUMTEXT   COMMENT '原始规格 JSON（productAttributeVO 原文）',
  sku_raw_json        MEDIUMTEXT   COMMENT '原始 SKU JSON（colorSizeVO 原文）',
  comment_summary     TEXT         COMMENT '评论概要（好评率/评论数/标签）',
  raw_api_json        LONGTEXT     COMMENT '原始 API 响应完整 JSON（回溯审计用）',
  file_structure      TEXT         COMMENT '原始导出包文件结构摘要',
  source_dir          VARCHAR(500) COMMENT '原始导出包目录路径',
  import_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
  UNIQUE KEY uk_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品内容归档（原始数据备份）';

-- 5. 确保 MySQL max_allowed_packet 支持大 BLOB
-- 运行前请在 Docker MySQL 中执行：
-- SET GLOBAL max_allowed_packet = 67108864;  -- 64MB

-- 6. 确保 album_pics 字段为 TEXT（兼容历史数据，重复执行安全）
ALTER TABLE pms_product MODIFY COLUMN album_pics TEXT;
-- 确保 detail_mobile_html 为 MEDIUMTEXT（存储改写后的 HTML）
ALTER TABLE pms_product MODIFY COLUMN detail_mobile_html MEDIUMTEXT;
