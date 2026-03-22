-- ============================================
-- 团队销售大盘系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS sales_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sales_hub;

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL,
    permission_id BIGINT NOT NULL,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_code (role_code),
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_role_perm (role_code, permission_id, deleted),
    CONSTRAINT fk_rp_role_code FOREIGN KEY (role_code) REFERENCES sys_role(code) ON UPDATE CASCADE,
    CONSTRAINT fk_rp_permission_id FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 团队表
CREATE TABLE IF NOT EXISTS sys_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    leader_id BIGINT,
    target_dgmv DECIMAL(14,2) DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小组表（与团队平级，互不隶属）
CREATE TABLE IF NOT EXISTS sys_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    leader_id BIGINT,
    target_dgmv DECIMAL(14,2) DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    avatar VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(32) NOT NULL DEFAULT 'sales',
    team_id BIGINT,
    group_id BIGINT,
    level VARCHAR(10) DEFAULT 'K1',
    target_dgmv DECIMAL(14,2) DEFAULT 0 COMMENT '个人季度目标DGMV',
    status VARCHAR(16) DEFAULT 'active',
    password_changed TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否已修改密码 0=未修改 1=已修改',
    required_platforms VARCHAR(200) DEFAULT NULL COMMENT '需要每日填报的平台code，逗号分隔',
    birthday DATE COMMENT '生日',
    hire_date DATE COMMENT '入职日期',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_team_id (team_id),
    INDEX idx_role (role),
    CONSTRAINT fk_user_role FOREIGN KEY (role) REFERENCES sys_role(code) ON UPDATE CASCADE,
    CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES sys_team(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_group FOREIGN KEY (group_id) REFERENCES sys_group(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 给 sys_team.leader_id 和 sys_group.leader_id 加外键（用户表建完后）
ALTER TABLE sys_team ADD CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_group ADD CONSTRAINT fk_group_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id) ON DELETE SET NULL;

-- 每日业绩表
CREATE TABLE IF NOT EXISTS biz_daily_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    platform VARCHAR(32) NOT NULL,
    account_id BIGINT DEFAULT NULL COMMENT '关联 sys_platform_account.id',
    gmv DECIMAL(14,2) DEFAULT 0,
    refund DECIMAL(14,2) DEFAULT 0,
    dgmv DECIMAL(14,2) DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_date (record_date),
    UNIQUE KEY uk_user_date_platform (user_id, record_date, platform, deleted),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志表
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

-- 字典表
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(32) NOT NULL COMMENT '字典类型，如 platform',
    code VARCHAR(64) NOT NULL COMMENT '字典编码',
    label VARCHAR(100) NOT NULL COMMENT '显示名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(16) DEFAULT 'active' COMMENT 'active/inactive',
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (type),
    UNIQUE KEY uk_type_code (type, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 渠道账号表
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

-- 用户渠道分配表
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

-- 季度快照表
CREATE TABLE IF NOT EXISTS biz_quarterly_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) COMMENT '冗余姓名',
    team_id BIGINT COMMENT '所属团队',
    team_name VARCHAR(100) COMMENT '冗余团队名',
    quarter VARCHAR(10) NOT NULL COMMENT '季度标识，如 2026-Q1',
    level VARCHAR(10) COMMENT '该季度最终确定职级',
    estimated_level VARCHAR(10) COMMENT '按DGMV估算职级',
    total_dgmv DECIMAL(14,2) DEFAULT 0 COMMENT '该季度总DGMV',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_quarter (quarter),
    INDEX idx_user_quarter (user_id, quarter),
    UNIQUE KEY uk_user_quarter (user_id, quarter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 种子数据
-- ============================================

-- 角色
INSERT INTO sys_role (code, name, description) VALUES
('admin', '管理员', '最高权限，全局管理'),
('partner', '合伙人', '管理本团队成员和业绩，查看团队数据'),
('sales', '运营', '基层员工，录入和查看个人业绩');

-- 权限
INSERT INTO sys_permission (code, name) VALUES
('dashboard:view', '查看大盘'),
('record:create', '录入业绩'),
('record:view', '查看业绩'),
('record:view_all', '查看所有人业绩'),
('team:manage', '团队管理'),
('user:manage', '人员管理'),
('dict:manage', '字典管理'),
('group:manage', '小组管理');

-- 角色权限分配
-- 管理员：全部权限
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'admin', id FROM sys_permission;

-- 合伙人：看板 + 录入 + 查看 + 查看全部 + 团队管理
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'partner', id FROM sys_permission WHERE code IN ('dashboard:view','record:create','record:view','record:view_all','team:manage','group:manage');

-- 运营：看板 + 录入 + 查看个人
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'sales', id FROM sys_permission WHERE code IN ('dashboard:view','record:create','record:view');

-- 团队
INSERT INTO sys_team (id, name, target_dgmv) VALUES
(1, '猛虎突击队', 500000.00),
(2, '雄鹰战队', 450000.00),
(3, '蛟龙小队', 400000.00);

-- 用户 (密码均为 BCrypt 加密的 "123456")
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, level) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张总', '13800000001', 'admin', NULL, 'K6'),
(2, 'partner1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李队长', '13800000002', 'partner', 1, 'K5'),
(3, 'sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王小明', '13800000003', 'sales', 1, 'K3'),
(4, 'sales2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵小红', '13800000004', 'sales', 1, 'K2'),
(5, 'partner2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈队长', '13800000005', 'partner', 2, 'K5'),
(6, 'sales3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘大伟', '13800000006', 'sales', 2, 'K3'),
(7, 'sales4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙小丽', '13800000007', 'sales', 2, 'K1'),
(8, 'partner3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周队长', '13800000008', 'partner', 3, 'K4'),
(9, 'sales5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴小强', '13800000009', 'sales', 3, 'K3'),
(10, 'sales6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郑小美', '13800000010', 'sales', 3, 'K2');

-- 回填团队组长（用户插入后）
UPDATE sys_team SET leader_id = 2 WHERE id = 1;
UPDATE sys_team SET leader_id = 5 WHERE id = 2;
UPDATE sys_team SET leader_id = 8 WHERE id = 3;

-- 模拟业绩数据 (最近几天)
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(3, CURDATE() - INTERVAL 1 DAY, 'DY', 15000.00, 500.00, 14500.00),
(3, CURDATE() - INTERVAL 1 DAY, 'XHS', 8000.00, 200.00, 7800.00),
(4, CURDATE() - INTERVAL 1 DAY, 'DY', 12000.00, 800.00, 11200.00),
(6, CURDATE() - INTERVAL 1 DAY, 'KS', 20000.00, 1000.00, 19000.00),
(6, CURDATE() - INTERVAL 1 DAY, 'DY', 10000.00, 300.00, 9700.00),
(7, CURDATE() - INTERVAL 1 DAY, 'SPH', 9000.00, 400.00, 8600.00),
(9, CURDATE() - INTERVAL 1 DAY, 'DY', 18000.00, 600.00, 17400.00),
(9, CURDATE() - INTERVAL 1 DAY, 'TB', 5000.00, 100.00, 4900.00),
(10, CURDATE() - INTERVAL 1 DAY, 'XHS', 7000.00, 300.00, 6700.00),
(2, CURDATE() - INTERVAL 1 DAY, 'DY', 25000.00, 1500.00, 23500.00),
(5, CURDATE() - INTERVAL 1 DAY, 'DY', 22000.00, 1200.00, 20800.00),
(8, CURDATE() - INTERVAL 1 DAY, 'KS', 16000.00, 700.00, 15300.00),
(3, CURDATE() - INTERVAL 2 DAY, 'DY', 13000.00, 400.00, 12600.00),
(6, CURDATE() - INTERVAL 2 DAY, 'DY', 17000.00, 900.00, 16100.00),
(9, CURDATE() - INTERVAL 2 DAY, 'DY', 14000.00, 500.00, 13500.00);

-- 字典数据：平台
INSERT INTO sys_dict (type, code, label, sort) VALUES
('platform', 'XHS', '小红书', 1),
('platform', 'DY', '抖音', 2),
('platform', 'KS', '快手', 3),
('platform', 'SPH', '视频号', 4),
('platform', 'TB', '淘宝直播', 5);

-- 字典数据：职级晋升阈值（DGMV 金额）
INSERT INTO sys_dict (type, code, label, sort) VALUES
('level_threshold', 'K1_K2', '50000', 1),
('level_threshold', 'K2_K3', '80000', 2),
('level_threshold', 'K3_K4', '100000', 3),
('level_threshold', 'K4_K5', '150000', 4),
('level_threshold', 'K5_K6', '200000', 5);
