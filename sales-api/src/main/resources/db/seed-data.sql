-- ============================================
-- 生产种子数据：仅角色、权限、管理员
-- ============================================
USE sales_hub;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE biz_quarterly_snapshot;
TRUNCATE TABLE biz_service_record;
TRUNCATE TABLE biz_daily_record;
TRUNCATE TABLE sys_user_platform;
TRUNCATE TABLE sys_platform_account;
TRUNCATE TABLE sys_audit_log;
TRUNCATE TABLE sys_role_permission;
DELETE FROM sys_user;
DELETE FROM sys_group;
DELETE FROM sys_team;
DELETE FROM sys_role;
DELETE FROM sys_permission;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 角色（4个）
-- ============================================
INSERT INTO sys_role (code, name, description) VALUES
('admin',   '管理员', '最高权限，全局管理'),
('partner', '合伙人', '管理本团队成员和业绩'),
('sales',   '运营',   '基层运营人员'),
('service', '客服',   '客服人员，录入客服日报');

-- ============================================
-- 权限
-- ============================================
INSERT INTO sys_permission (code, name) VALUES
('dashboard:view', '查看大盘'),
('record:create', '录入业绩'),
('record:view', '查看业绩'),
('record:view_all', '查看所有人业绩'),
('team:manage', '团队管理'),
('user:manage', '人员管理'),
('dict:manage', '字典管理'),
('group:manage', '小组管理');

-- admin 全部权限
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'admin', id FROM sys_permission;

-- partner
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'partner', id FROM sys_permission WHERE code IN ('dashboard:view','record:create','record:view','record:view_all','team:manage','group:manage');

-- sales
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'sales', id FROM sys_permission WHERE code IN ('dashboard:view','record:create','record:view');

-- service
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'service', id FROM sys_permission WHERE code IN ('record:create');

-- ============================================
-- 管理员（密码 123456）
-- ============================================
SET @pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, password_changed) VALUES
(1, 'admin', @pwd, '甘永志', '13800000001', 'admin', NULL, NULL, 'K1', 1);

-- ============================================
-- 字典数据：平台
-- ============================================
INSERT IGNORE INTO sys_dict (type, code, label, sort) VALUES
('platform', 'XHS', '小红书', 1),
('platform', 'DY', '抖音', 2),
('platform', 'KS', '快手', 3),
('platform', 'SPH', '视频号', 4),
('platform', 'TB', '淘宝直播', 5);

-- 字典数据：职级晋升阈值
INSERT IGNORE INTO sys_dict (type, code, label, sort) VALUES
('level_threshold', 'K1_K2', '50000', 1),
('level_threshold', 'K2_K3', '80000', 2),
('level_threshold', 'K3_K4', '100000', 3),
('level_threshold', 'K4_K5', '150000', 4),
('level_threshold', 'K5_K6', '200000', 5);

SELECT '种子数据插入完成' AS status;
