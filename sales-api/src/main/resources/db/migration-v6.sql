-- migration-v6: 新增 password_changed 字段，支持首次登录强制改密
USE sales_hub;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password_changed');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN password_changed TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否已修改密码 0=未修改 1=已修改'' AFTER status',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '迁移 v6 完成' AS status;
