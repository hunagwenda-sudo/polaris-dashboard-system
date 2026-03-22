-- migration-v5: 新增 remind_enabled 字段，控制是否提醒业绩填写
USE sales_hub;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'remind_enabled');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN remind_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否提醒填报 1=提醒 0=不提醒'' AFTER status',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '迁移 v5 完成' AS status;
