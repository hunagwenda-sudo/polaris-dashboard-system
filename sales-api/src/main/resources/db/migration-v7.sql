-- migration-v7: 新增 required_platforms 字段，支持每日填报平台多选
USE sales_hub;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'required_platforms');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN required_platforms VARCHAR(200) DEFAULT NULL COMMENT ''需要每日填报的平台code，逗号分隔''',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '迁移 v7 完成' AS status;
