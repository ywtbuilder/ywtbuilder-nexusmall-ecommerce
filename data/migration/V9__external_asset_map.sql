-- ============================================================
-- V9: 外链资源迁移映射表（追溯 + 回滚）
-- 目标：
--   1) 记录 source_url -> pms_asset 的映射结果
--   2) 记录每次字段改写前后值，支持按 batch_no 回滚
--   3) 迁移脚本可重复执行（幂等）
-- ============================================================

CREATE TABLE IF NOT EXISTS pms_external_asset_map (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_url VARCHAR(700) NOT NULL COMMENT '原始外链 URL',
  normalized_url VARCHAR(700) NOT NULL COMMENT '归一化后的 URL',
  asset_id BIGINT NULL COMMENT '映射到的 pms_asset.id（失败/兜底可为空）',
  status VARCHAR(16) NOT NULL COMMENT 'success | fallback | failed',
  error_message VARCHAR(1000) NULL COMMENT '失败原因',
  batch_no VARCHAR(64) NOT NULL COMMENT '迁移批次号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pms_external_asset_map_source_url (source_url),
  KEY idx_pms_external_asset_map_batch (batch_no, id),
  KEY idx_pms_external_asset_map_status (status, id),
  KEY idx_pms_external_asset_map_asset (asset_id),
  CONSTRAINT fk_pms_external_asset_map_asset
    FOREIGN KEY (asset_id) REFERENCES pms_asset(id),
  CONSTRAINT chk_pms_external_asset_map_status
    CHECK (status IN ('success', 'fallback', 'failed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外链资源映射表';

CREATE TABLE IF NOT EXISTS pms_external_asset_ref (
  id BIGINT NOT NULL AUTO_INCREMENT,
  map_id BIGINT NULL COMMENT '关联 pms_external_asset_map.id；字段级改写可为空',
  table_name VARCHAR(64) NOT NULL COMMENT '改写数据表',
  column_name VARCHAR(64) NOT NULL COMMENT '改写字段名',
  row_id BIGINT NOT NULL COMMENT '改写记录主键',
  old_value MEDIUMTEXT NULL COMMENT '改写前',
  new_value MEDIUMTEXT NULL COMMENT '改写后',
  batch_no VARCHAR(64) NOT NULL COMMENT '迁移批次号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pms_external_asset_ref_batch (batch_no, id),
  KEY idx_pms_external_asset_ref_table_row (table_name, row_id, id),
  KEY idx_pms_external_asset_ref_map (map_id),
  CONSTRAINT fk_pms_external_asset_ref_map
    FOREIGN KEY (map_id) REFERENCES pms_external_asset_map(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外链字段改写审计表';
