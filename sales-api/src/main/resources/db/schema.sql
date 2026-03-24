-- ============================================
-- 团队销售大盘系统 - 数据库建表脚本
-- 仅包含表结构，种子数据见 seed-data.sql
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
    leader_ids VARCHAR(255) DEFAULT NULL COMMENT '负责人ID列表(逗号分隔)',
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
    estimated_level VARCHAR(10) DEFAULT NULL COMMENT '预估职级（用于播报变化检测）',
    target_dgmv DECIMAL(14,2) DEFAULT 0 COMMENT '个人季度目标DGMV',
    status VARCHAR(16) DEFAULT 'active',
    password_changed TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否已修改密码 0=未修改 1=已修改',
    remind_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否提醒填报 1=提醒 0=不提醒',
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
    account_note VARCHAR(100) DEFAULT NULL COMMENT '账号备注',
    account_id BIGINT DEFAULT NULL COMMENT '关联 sys_platform_account.id',
    gmv DECIMAL(14,2) DEFAULT 0,
    refund DECIMAL(14,2) DEFAULT 0,
    dgmv DECIMAL(14,2) DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_date (record_date),
    UNIQUE KEY uk_user_date_platform_account (user_id, record_date, platform, account_note, deleted),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    label VARCHAR(2000) NOT NULL COMMENT '显示名称',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '图标路径',
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
