-- 季度快照表：每季度结束时保存每个运营的职级和总DGMV
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
