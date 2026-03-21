-- migration-v5: 新增 remind_enabled 字段，控制是否提醒业绩填写
ALTER TABLE sys_user ADD COLUMN remind_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否提醒填报 1=提醒 0=不提醒';
