-- Flyway V3: Default order settings (if table exists)
SET @has_oms_order_setting = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oms_order_setting'
);
SET @sql = IF(
    @has_oms_order_setting > 0,
    'INSERT IGNORE INTO oms_order_setting (id, flash_order_overtime, normal_order_overtime, confirm_overtime, finish_overtime, comment_overtime) VALUES (1, 60, 120, 15, 7, 7)',
    'SELECT ''skip oms_order_setting seed'''
);
PREPARE stmt3 FROM @sql;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;
