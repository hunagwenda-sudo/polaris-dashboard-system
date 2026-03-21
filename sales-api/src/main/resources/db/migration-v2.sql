-- ============================================
-- 团队销售大盘系统 - 增量迁移脚本 v2
-- 适用于：已有数据库从旧版本升级到 Task 15 完成后的版本
-- 执行前请确保已备份数据库
-- ============================================

USE sales_hub;

-- ============================================
-- 1. 新增 admin 角色（如果不存在）
-- ============================================
INSERT IGNORE INTO sys_role (code, name, description) VALUES
('admin', '管理员', '超级管理员，可配置所有角色权限');

-- ============================================
-- 2. 新增 dict:manage 权限（如果不存在）
-- ============================================
INSERT IGNORE INTO sys_permission (code, name) VALUES
('dict:manage', '字典管理');

-- ============================================
-- 3. 给 admin 角色分配所有权限
-- ============================================
INSERT IGNORE INTO sys_role_permission (role_code, permission_id)
SELECT 'admin', id FROM sys_permission
WHERE id NOT IN (
    SELECT permission_id FROM sys_role_permission WHERE role_code = 'admin' AND deleted = 0
);

-- ============================================
-- 4. 将原 partner 角色的管理员用户升级为 admin
--    （仅 id=1 的张总，按需调整）
-- ============================================
UPDATE sys_user SET role = 'admin' WHERE id = 1 AND role = 'partner';

-- ============================================
-- 4b. 收敛权限管理：仅 admin 可操作，移除 partner/leader 的 role:manage 和 dict:manage
-- ============================================
DELETE FROM sys_role_permission
WHERE role_code IN ('partner', 'leader')
  AND permission_id IN (SELECT id FROM sys_permission WHERE code IN ('role:manage', 'dict:manage', 'user:manage'))
  AND deleted = 0;

-- ============================================
-- 5. 更新业绩记录中的平台名称：中文 → 编码
-- ============================================
UPDATE biz_daily_record SET platform = 'DY'  WHERE platform = '抖音';
UPDATE biz_daily_record SET platform = 'XHS' WHERE platform = '小红书';
UPDATE biz_daily_record SET platform = 'KS'  WHERE platform = '快手';
UPDATE biz_daily_record SET platform = 'SPH' WHERE platform = '视频号';
UPDATE biz_daily_record SET platform = 'TB'  WHERE platform = '淘宝直播';

-- ============================================
-- 6. 用户表新增 avatar 字段（如果不存在）
-- ============================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'avatar');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) COMMENT ''头像URL'' AFTER phone',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 7. 团队表新增 target_dgmv 字段（如果不存在）
-- ============================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_team' AND COLUMN_NAME = 'target_dgmv');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_team ADD COLUMN target_dgmv DECIMAL(14,2) DEFAULT 0 AFTER leader_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 8. 创建审计日志表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(64),
    action VARCHAR(32) NOT NULL COMMENT 'CREATE/UPDATE/DELETE',
    target_type VARCHAR(64) NOT NULL COMMENT '操作对象类型',
    target_id BIGINT COMMENT '操作对象ID',
    detail TEXT COMMENT '变更详情JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 9. 创建字典表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(32) NOT NULL COMMENT '字典类型',
    code VARCHAR(64) NOT NULL COMMENT '字典编码',
    label VARCHAR(100) NOT NULL COMMENT '显示名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(16) DEFAULT 'active' COMMENT 'active/inactive',
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (type),
    UNIQUE KEY uk_type_code (type, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入字典种子数据（忽略已存在的）
INSERT IGNORE INTO sys_dict (type, code, label, sort) VALUES
('platform', 'XHS', '小红书', 1),
('platform', 'DY', '抖音', 2),
('platform', 'KS', '快手', 3),
('platform', 'SPH', '视频号', 4),
('platform', 'TB', '淘宝直播', 5);

-- ============================================
-- 10. 添加 unique 约束到 sys_role_permission（如果不存在）
-- ============================================
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_role_permission' AND INDEX_NAME = 'uk_role_perm');
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE sys_role_permission ADD UNIQUE KEY uk_role_perm (role_code, permission_id, deleted)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 11. 修改 biz_daily_record 唯一键包含 deleted
--     先删除旧的，再创建新的
-- ============================================
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'biz_daily_record' AND INDEX_NAME = 'uk_user_date_platform');
SET @sql = IF(@idx_exists > 0,
    'ALTER TABLE biz_daily_record DROP INDEX uk_user_date_platform',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE biz_daily_record ADD UNIQUE KEY uk_user_date_platform (user_id, record_date, platform, deleted);

-- ============================================
-- 12. 简化角色：移除 leader，统一为 admin/partner/sales
-- ============================================

-- 将所有 leader 用户迁移为 partner
UPDATE sys_user SET role = 'partner' WHERE role = 'leader';

-- 删除 leader 角色的权限分配
DELETE FROM sys_role_permission WHERE role_code = 'leader';

-- 删除 leader 角色
DELETE FROM sys_role WHERE code = 'leader';

-- 更新 sales 角色描述
UPDATE sys_role SET name = '运营', description = '基层运营人员' WHERE code = 'sales';

-- ============================================
-- 完成
-- ============================================
SELECT '迁移完成' AS status;

-- ============================================
-- 13. 用户表新增 target_dgmv 字段（个人季度目标）
-- ============================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'sales_hub' AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'target_dgmv');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN target_dgmv DECIMAL(14,2) DEFAULT 0 COMMENT ''个人季度目标DGMV'' AFTER level',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 客服日报表
CREATE TABLE IF NOT EXISTS biz_service_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '客服用户ID',
  record_date DATE NOT NULL COMMENT '日期',
  platform VARCHAR(50) NOT NULL COMMENT '渠道编码',
  shift VARCHAR(20) NOT NULL COMMENT '班次: morning/evening',
  reception_count INT NOT NULL DEFAULT 0 COMMENT '接待量',
  reply_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '三分钟回复率(%)',
  praise_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '好评率(%)',
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date_platform_shift (user_id, record_date, platform, shift),
  KEY idx_record_date (record_date),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服日报';
