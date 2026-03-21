-- ============================================
-- 迁移脚本 v3：业绩录入支持多账号
-- ============================================
USE sales_hub;

-- 1. biz_daily_record 加 account_note 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'biz_daily_record' AND COLUMN_NAME = 'account_note');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE biz_daily_record ADD COLUMN account_note VARCHAR(100) DEFAULT NULL COMMENT ''账号备注'' AFTER platform',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 去掉旧的 uk_user_date_platform 唯一约束
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'biz_daily_record' AND INDEX_NAME = 'uk_user_date_platform');
SET @sql = IF(@idx_exists > 0,
    'ALTER TABLE biz_daily_record DROP INDEX uk_user_date_platform',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 新增唯一约束：同一人同一天同一渠道同一账号备注只能一条
ALTER TABLE biz_daily_record
    ADD UNIQUE KEY uk_user_date_platform_account (user_id, record_date, platform, account_note, deleted);

SELECT '迁移 v3 完成' AS status;
