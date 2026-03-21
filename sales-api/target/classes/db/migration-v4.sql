-- ============================================
-- 迁移脚本 v4：渠道平台支持自定义 icon
-- ============================================
USE sales_hub;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_dict' AND COLUMN_NAME = 'icon_url');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_dict ADD COLUMN icon_url VARCHAR(500) DEFAULT NULL COMMENT ''图标路径'' AFTER label',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '迁移 v4 完成' AS status;
