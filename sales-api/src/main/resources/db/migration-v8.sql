-- migration-v8: 渠道账号管理 + 用户渠道分配
USE sales_hub;

-- 1. 渠道账号表
CREATE TABLE IF NOT EXISTS sys_platform_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_code VARCHAR(32) NOT NULL COMMENT '关联 sys_dict platform code',
    account_name VARCHAR(100) NOT NULL COMMENT '账号名称',
    sort INT DEFAULT 0,
    status VARCHAR(16) DEFAULT 'active',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_platform_code (platform_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 用户渠道分配表（替代 sys_user.required_platforms）
CREATE TABLE IF NOT EXISTS sys_user_platform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    platform_code VARCHAR(32) NOT NULL,
    account_id BIGINT NOT NULL COMMENT '关联 sys_platform_account.id',
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    UNIQUE KEY uk_user_account (user_id, account_id, deleted),
    CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_up_account FOREIGN KEY (account_id) REFERENCES sys_platform_account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. biz_daily_record 新增 account_id 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'biz_daily_record' AND COLUMN_NAME = 'account_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE biz_daily_record ADD COLUMN account_id BIGINT DEFAULT NULL COMMENT ''关联 sys_platform_account.id'' AFTER platform',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '迁移 v8 完成' AS status;
