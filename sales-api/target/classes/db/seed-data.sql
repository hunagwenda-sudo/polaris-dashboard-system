-- ============================================
-- 清理旧数据 + 插入真实测试数据
-- 执行顺序：先清外键依赖表，再清主表
-- ============================================
USE sales_hub;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE biz_service_record;
TRUNCATE TABLE biz_daily_record;
TRUNCATE TABLE sys_audit_log;
TRUNCATE TABLE sys_role_permission;
DELETE FROM sys_user;
DELETE FROM sys_group;
DELETE FROM sys_team;
DELETE FROM sys_role;
DELETE FROM sys_permission;
-- 保留 sys_dict 不动

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

-- service（无特殊权限，仅客服日报录入）
INSERT INTO sys_role_permission (role_code, permission_id)
SELECT 'service', id FROM sys_permission WHERE code IN ('record:create');

-- ============================================
-- 团队（3个）
-- ============================================
INSERT INTO sys_team (id, name, target_dgmv) VALUES
(1, '猛虎突击队', 600000.00),
(2, '雄鹰战队',   500000.00),
(3, '蛟龙小队',   450000.00);

-- ============================================
-- 小组（2个）
-- ============================================
INSERT INTO sys_group (id, name, target_dgmv) VALUES
(1, '抖音攻坚组', 300000.00),
(2, '小红书精英组', 250000.00);

-- ============================================
-- 用户（密码均为 123456）
-- BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ============================================
SET @pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

-- 管理员
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, birthday, hire_date) VALUES
(1, 'admin', @pwd, '张明远', '13800001001', 'admin', NULL, NULL, 'K1', '1985-06-15', '2020-01-10');

-- 猛虎突击队（team 1）
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, birthday, hire_date, target_dgmv) VALUES
(2,  'liwei',    @pwd, '李伟',   '13800001002', 'partner', 1, NULL, 'K1', '1988-03-22', '2021-04-01', 80000.00),
(3,  'wangfang', @pwd, '王芳',   '13800001003', 'sales',   1, 1,    'K1', '1995-09-10', '2022-07-15', 60000.00),
(4,  'zhaojun',  @pwd, '赵俊',   '13800001004', 'sales',   1, 1,    'K1', '1996-12-05', '2023-01-08', 55000.00),
(5,  'sunlei',   @pwd, '孙磊',   '13800001005', 'sales',   1, NULL, 'K1', '1997-04-18', '2023-06-20', 50000.00);

-- 雄鹰战队（team 2）
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, birthday, hire_date, target_dgmv) VALUES
(6,  'chenxia',  @pwd, '陈霞',   '13800001006', 'partner', 2, NULL, 'K1', '1987-11-30', '2021-02-14', 85000.00),
(7,  'liuyang',  @pwd, '刘洋',   '13800001007', 'sales',   2, 2,    'K1', '1994-07-25', '2022-03-10', 65000.00),
(8,  'huangmin', @pwd, '黄敏',   '13800001008', 'sales',   2, 2,    'K1', '1998-01-14', '2023-09-01', 45000.00),
(9,  'zhoujie',  @pwd, '周杰',   '13800001009', 'sales',   2, NULL, 'K1', '1993-05-08', '2022-11-20', 70000.00);

-- 蛟龙小队（team 3）
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, birthday, hire_date, target_dgmv) VALUES
(10, 'wuqiang',  @pwd, '吴强',   '13800001010', 'partner', 3, NULL, 'K1', '1989-08-20', '2021-08-01', 75000.00),
(11, 'zhengyu',  @pwd, '郑宇',   '13800001011', 'sales',   3, NULL, 'K1', '1996-02-28', '2023-03-15', 55000.00),
(12, 'xuting',   @pwd, '徐婷',   '13800001012', 'sales',   3, NULL, 'K1', '1999-10-12', '2024-01-10', 40000.00);

-- 客服人员（无团队）
INSERT INTO sys_user (id, username, password, name, phone, role, team_id, group_id, level, birthday, hire_date) VALUES
(13, 'linxiao',  @pwd, '林晓',   '13800001013', 'service', NULL, NULL, 'K1', '1997-06-03', '2023-05-10'),
(14, 'hejing',   @pwd, '何静',   '13800001014', 'service', NULL, NULL, 'K1', '1998-08-22', '2023-08-15');

-- ============================================
-- 回填团队/小组组长
-- ============================================
UPDATE sys_team SET leader_id = 2  WHERE id = 1;
UPDATE sys_team SET leader_id = 6  WHERE id = 2;
UPDATE sys_team SET leader_id = 10 WHERE id = 3;
UPDATE sys_group SET leader_id = 3 WHERE id = 1;
UPDATE sys_group SET leader_id = 7 WHERE id = 2;

-- ============================================
-- 销售业绩数据（最近14天，真实感数据）
-- 每人每天1~3个渠道，金额有波动
-- ============================================

-- === 昨天 (CURDATE() - 1) ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
-- 李伟 (id=2)
(2, CURDATE() - INTERVAL 1 DAY, 'DY',  32500.00, 1800.00, 30700.00),
(2, CURDATE() - INTERVAL 1 DAY, 'XHS', 12000.00,  600.00, 11400.00),
-- 王芳 (id=3)
(3, CURDATE() - INTERVAL 1 DAY, 'DY',  18200.00,  900.00, 17300.00),
(3, CURDATE() - INTERVAL 1 DAY, 'KS',   8500.00,  350.00,  8150.00),
-- 赵俊 (id=4)
(4, CURDATE() - INTERVAL 1 DAY, 'DY',  15600.00,  700.00, 14900.00),
(4, CURDATE() - INTERVAL 1 DAY, 'SPH',  6200.00,  200.00,  6000.00),
-- 孙磊 (id=5)
(5, CURDATE() - INTERVAL 1 DAY, 'DY',  11800.00,  500.00, 11300.00),
-- 陈霞 (id=6)
(6, CURDATE() - INTERVAL 1 DAY, 'DY',  28000.00, 1500.00, 26500.00),
(6, CURDATE() - INTERVAL 1 DAY, 'XHS', 15500.00,  800.00, 14700.00),
-- 刘洋 (id=7)
(7, CURDATE() - INTERVAL 1 DAY, 'XHS', 22000.00, 1100.00, 20900.00),
(7, CURDATE() - INTERVAL 1 DAY, 'DY',   9800.00,  400.00,  9400.00),
-- 黄敏 (id=8)
(8, CURDATE() - INTERVAL 1 DAY, 'DY',   7500.00,  300.00,  7200.00),
-- 周杰 (id=9)
(9, CURDATE() - INTERVAL 1 DAY, 'DY',  19500.00,  850.00, 18650.00),
(9, CURDATE() - INTERVAL 1 DAY, 'KS',   6800.00,  250.00,  6550.00),
-- 吴强 (id=10)
(10, CURDATE() - INTERVAL 1 DAY, 'KS',  21000.00, 1200.00, 19800.00),
(10, CURDATE() - INTERVAL 1 DAY, 'DY',  13500.00,  600.00, 12900.00),
-- 郑宇 (id=11)
(11, CURDATE() - INTERVAL 1 DAY, 'DY',  16800.00,  750.00, 16050.00),
-- 徐婷 (id=12)
(12, CURDATE() - INTERVAL 1 DAY, 'SPH',  9200.00,  400.00,  8800.00),
(12, CURDATE() - INTERVAL 1 DAY, 'DY',   5500.00,  200.00,  5300.00);

-- === 前天 (CURDATE() - 2) ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 2 DAY, 'DY',  29800.00, 1600.00, 28200.00),
(2, CURDATE() - INTERVAL 2 DAY, 'KS',   8500.00,  400.00,  8100.00),
(3, CURDATE() - INTERVAL 2 DAY, 'DY',  16500.00,  800.00, 15700.00),
(4, CURDATE() - INTERVAL 2 DAY, 'DY',  14200.00,  650.00, 13550.00),
(5, CURDATE() - INTERVAL 2 DAY, 'DY',  13000.00,  550.00, 12450.00),
(5, CURDATE() - INTERVAL 2 DAY, 'XHS',  5200.00,  200.00,  5000.00),
(6, CURDATE() - INTERVAL 2 DAY, 'DY',  25500.00, 1300.00, 24200.00),
(7, CURDATE() - INTERVAL 2 DAY, 'XHS', 19800.00,  950.00, 18850.00),
(8, CURDATE() - INTERVAL 2 DAY, 'DY',   8200.00,  350.00,  7850.00),
(9, CURDATE() - INTERVAL 2 DAY, 'DY',  17800.00,  800.00, 17000.00),
(10, CURDATE() - INTERVAL 2 DAY, 'KS', 18500.00, 1000.00, 17500.00),
(11, CURDATE() - INTERVAL 2 DAY, 'DY', 15200.00,  700.00, 14500.00),
(12, CURDATE() - INTERVAL 2 DAY, 'SPH', 7800.00,  300.00,  7500.00);

-- === 3天前 ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 3 DAY, 'DY',  31000.00, 1700.00, 29300.00),
(3, CURDATE() - INTERVAL 3 DAY, 'DY',  17800.00,  850.00, 16950.00),
(3, CURDATE() - INTERVAL 3 DAY, 'XHS',  7200.00,  300.00,  6900.00),
(4, CURDATE() - INTERVAL 3 DAY, 'DY',  13500.00,  600.00, 12900.00),
(5, CURDATE() - INTERVAL 3 DAY, 'DY',  10500.00,  450.00, 10050.00),
(6, CURDATE() - INTERVAL 3 DAY, 'DY',  27000.00, 1400.00, 25600.00),
(6, CURDATE() - INTERVAL 3 DAY, 'XHS', 13200.00,  650.00, 12550.00),
(7, CURDATE() - INTERVAL 3 DAY, 'XHS', 20500.00, 1000.00, 19500.00),
(8, CURDATE() - INTERVAL 3 DAY, 'DY',   6800.00,  280.00,  6520.00),
(9, CURDATE() - INTERVAL 3 DAY, 'DY',  18200.00,  800.00, 17400.00),
(10, CURDATE() - INTERVAL 3 DAY, 'KS', 19800.00, 1100.00, 18700.00),
(11, CURDATE() - INTERVAL 3 DAY, 'DY', 14500.00,  650.00, 13850.00),
(11, CURDATE() - INTERVAL 3 DAY, 'TB',  4200.00,  150.00,  4050.00),
(12, CURDATE() - INTERVAL 3 DAY, 'SPH', 8500.00,  350.00,  8150.00);

-- === 4天前 ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 4 DAY, 'DY',  28500.00, 1500.00, 27000.00),
(3, CURDATE() - INTERVAL 4 DAY, 'DY',  19500.00,  950.00, 18550.00),
(4, CURDATE() - INTERVAL 4 DAY, 'DY',  16000.00,  750.00, 15250.00),
(4, CURDATE() - INTERVAL 4 DAY, 'KS',   5500.00,  200.00,  5300.00),
(5, CURDATE() - INTERVAL 4 DAY, 'DY',  12200.00,  500.00, 11700.00),
(6, CURDATE() - INTERVAL 4 DAY, 'DY',  26000.00, 1350.00, 24650.00),
(7, CURDATE() - INTERVAL 4 DAY, 'XHS', 21500.00, 1050.00, 20450.00),
(8, CURDATE() - INTERVAL 4 DAY, 'DY',   9000.00,  400.00,  8600.00),
(9, CURDATE() - INTERVAL 4 DAY, 'DY',  20000.00,  900.00, 19100.00),
(10, CURDATE() - INTERVAL 4 DAY, 'KS', 20500.00, 1150.00, 19350.00),
(10, CURDATE() - INTERVAL 4 DAY, 'DY', 11000.00,  500.00, 10500.00),
(11, CURDATE() - INTERVAL 4 DAY, 'DY', 13800.00,  600.00, 13200.00),
(12, CURDATE() - INTERVAL 4 DAY, 'SPH', 6500.00,  250.00,  6250.00);

-- === 5天前 ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 5 DAY, 'DY',  33000.00, 1900.00, 31100.00),
(2, CURDATE() - INTERVAL 5 DAY, 'XHS', 10500.00,  500.00, 10000.00),
(3, CURDATE() - INTERVAL 5 DAY, 'DY',  15800.00,  750.00, 15050.00),
(4, CURDATE() - INTERVAL 5 DAY, 'DY',  14800.00,  680.00, 14120.00),
(5, CURDATE() - INTERVAL 5 DAY, 'DY',  11500.00,  480.00, 11020.00),
(6, CURDATE() - INTERVAL 5 DAY, 'DY',  29500.00, 1600.00, 27900.00),
(7, CURDATE() - INTERVAL 5 DAY, 'XHS', 18500.00,  900.00, 17600.00),
(7, CURDATE() - INTERVAL 5 DAY, 'DY',   8200.00,  350.00,  7850.00),
(8, CURDATE() - INTERVAL 5 DAY, 'DY',   7200.00,  300.00,  6900.00),
(9, CURDATE() - INTERVAL 5 DAY, 'DY',  16500.00,  750.00, 15750.00),
(9, CURDATE() - INTERVAL 5 DAY, 'KS',   7500.00,  300.00,  7200.00),
(10, CURDATE() - INTERVAL 5 DAY, 'KS', 22000.00, 1250.00, 20750.00),
(11, CURDATE() - INTERVAL 5 DAY, 'DY', 17000.00,  800.00, 16200.00),
(12, CURDATE() - INTERVAL 5 DAY, 'DY',  8000.00,  350.00,  7650.00),
(12, CURDATE() - INTERVAL 5 DAY, 'SPH', 4500.00,  180.00,  4320.00);

-- === 6天前 ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 6 DAY, 'DY',  30200.00, 1650.00, 28550.00),
(3, CURDATE() - INTERVAL 6 DAY, 'DY',  17000.00,  800.00, 16200.00),
(3, CURDATE() - INTERVAL 6 DAY, 'KS',   6500.00,  250.00,  6250.00),
(4, CURDATE() - INTERVAL 6 DAY, 'DY',  15200.00,  700.00, 14500.00),
(5, CURDATE() - INTERVAL 6 DAY, 'DY',  10800.00,  450.00, 10350.00),
(6, CURDATE() - INTERVAL 6 DAY, 'DY',  24500.00, 1250.00, 23250.00),
(7, CURDATE() - INTERVAL 6 DAY, 'XHS', 19000.00,  920.00, 18080.00),
(8, CURDATE() - INTERVAL 6 DAY, 'DY',   8500.00,  380.00,  8120.00),
(9, CURDATE() - INTERVAL 6 DAY, 'DY',  21000.00,  950.00, 20050.00),
(10, CURDATE() - INTERVAL 6 DAY, 'KS', 17500.00,  900.00, 16600.00),
(11, CURDATE() - INTERVAL 6 DAY, 'DY', 14000.00,  620.00, 13380.00),
(12, CURDATE() - INTERVAL 6 DAY, 'SPH', 7200.00,  280.00,  6920.00);

-- === 7天前 ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 7 DAY, 'DY',  27500.00, 1400.00, 26100.00),
(2, CURDATE() - INTERVAL 7 DAY, 'TB',   5500.00,  200.00,  5300.00),
(3, CURDATE() - INTERVAL 7 DAY, 'DY',  16200.00,  780.00, 15420.00),
(4, CURDATE() - INTERVAL 7 DAY, 'DY',  13800.00,  620.00, 13180.00),
(5, CURDATE() - INTERVAL 7 DAY, 'DY',  12500.00,  520.00, 11980.00),
(6, CURDATE() - INTERVAL 7 DAY, 'DY',  26500.00, 1350.00, 25150.00),
(6, CURDATE() - INTERVAL 7 DAY, 'XHS', 11000.00,  550.00, 10450.00),
(7, CURDATE() - INTERVAL 7 DAY, 'XHS', 17500.00,  850.00, 16650.00),
(8, CURDATE() - INTERVAL 7 DAY, 'DY',   6500.00,  270.00,  6230.00),
(9, CURDATE() - INTERVAL 7 DAY, 'DY',  18800.00,  850.00, 17950.00),
(10, CURDATE() - INTERVAL 7 DAY, 'KS', 19000.00, 1050.00, 17950.00),
(11, CURDATE() - INTERVAL 7 DAY, 'DY', 15500.00,  700.00, 14800.00),
(12, CURDATE() - INTERVAL 7 DAY, 'SPH', 8800.00,  380.00,  8420.00),
(12, CURDATE() - INTERVAL 7 DAY, 'DY',  4200.00,  150.00,  4050.00);

-- === 8~10天前（稍少一些数据） ===
INSERT INTO biz_daily_record (user_id, record_date, platform, gmv, refund, dgmv) VALUES
(2, CURDATE() - INTERVAL 8 DAY, 'DY',  26000.00, 1300.00, 24700.00),
(3, CURDATE() - INTERVAL 8 DAY, 'DY',  14500.00,  680.00, 13820.00),
(6, CURDATE() - INTERVAL 8 DAY, 'DY',  23000.00, 1200.00, 21800.00),
(7, CURDATE() - INTERVAL 8 DAY, 'XHS', 16000.00,  780.00, 15220.00),
(9, CURDATE() - INTERVAL 8 DAY, 'DY',  17200.00,  780.00, 16420.00),
(10, CURDATE() - INTERVAL 8 DAY, 'KS', 18000.00,  950.00, 17050.00),
(11, CURDATE() - INTERVAL 8 DAY, 'DY', 12500.00,  550.00, 11950.00),

(2, CURDATE() - INTERVAL 9 DAY, 'DY',  28800.00, 1550.00, 27250.00),
(4, CURDATE() - INTERVAL 9 DAY, 'DY',  15500.00,  720.00, 14780.00),
(5, CURDATE() - INTERVAL 9 DAY, 'DY',  11000.00,  460.00, 10540.00),
(6, CURDATE() - INTERVAL 9 DAY, 'DY',  25000.00, 1300.00, 23700.00),
(7, CURDATE() - INTERVAL 9 DAY, 'XHS', 18200.00,  880.00, 17320.00),
(9, CURDATE() - INTERVAL 9 DAY, 'DY',  19500.00,  880.00, 18620.00),
(10, CURDATE() - INTERVAL 9 DAY, 'KS', 16500.00,  850.00, 15650.00),
(12, CURDATE() - INTERVAL 9 DAY, 'SPH', 6800.00,  260.00,  6540.00),

(2, CURDATE() - INTERVAL 10 DAY, 'DY',  25500.00, 1350.00, 24150.00),
(3, CURDATE() - INTERVAL 10 DAY, 'DY',  15000.00,  700.00, 14300.00),
(6, CURDATE() - INTERVAL 10 DAY, 'DY',  22500.00, 1150.00, 21350.00),
(8, CURDATE() - INTERVAL 10 DAY, 'DY',   7800.00,  330.00,  7470.00),
(9, CURDATE() - INTERVAL 10 DAY, 'DY',  16800.00,  760.00, 16040.00),
(10, CURDATE() - INTERVAL 10 DAY, 'KS', 17200.00,  900.00, 16300.00),
(11, CURDATE() - INTERVAL 10 DAY, 'DY', 13200.00,  580.00, 12620.00);

-- ============================================
-- 客服日报数据（最近5天，2个客服）
-- ============================================

-- 林晓 (id=13)
INSERT INTO biz_service_record (user_id, record_date, platform, shift, reception_count, reply_rate, praise_rate) VALUES
(13, CURDATE() - INTERVAL 1 DAY, 'DY',  'morning', 156, 96.5, 98.2),
(13, CURDATE() - INTERVAL 1 DAY, 'XHS', 'morning', 89,  94.8, 97.5),
(13, CURDATE() - INTERVAL 1 DAY, 'KS',  'morning', 72,  97.1, 96.8),
(13, CURDATE() - INTERVAL 2 DAY, 'DY',  'morning', 142, 95.8, 97.9),
(13, CURDATE() - INTERVAL 2 DAY, 'XHS', 'morning', 95,  96.2, 98.1),
(13, CURDATE() - INTERVAL 3 DAY, 'DY',  'evening', 168, 93.5, 96.2),
(13, CURDATE() - INTERVAL 3 DAY, 'KS',  'evening', 78,  95.0, 97.0),
(13, CURDATE() - INTERVAL 4 DAY, 'DY',  'morning', 135, 97.2, 98.5),
(13, CURDATE() - INTERVAL 4 DAY, 'XHS', 'morning', 82,  96.0, 97.8),
(13, CURDATE() - INTERVAL 5 DAY, 'DY',  'morning', 148, 95.5, 97.2);

-- 何静 (id=14)
INSERT INTO biz_service_record (user_id, record_date, platform, shift, reception_count, reply_rate, praise_rate) VALUES
(14, CURDATE() - INTERVAL 1 DAY, 'DY',  'evening', 178, 94.2, 96.8),
(14, CURDATE() - INTERVAL 1 DAY, 'XHS', 'evening', 102, 93.5, 95.2),
(14, CURDATE() - INTERVAL 1 DAY, 'SPH', 'evening', 65,  96.8, 98.0),
(14, CURDATE() - INTERVAL 2 DAY, 'DY',  'evening', 165, 95.0, 97.2),
(14, CURDATE() - INTERVAL 2 DAY, 'XHS', 'evening', 88,  94.5, 96.5),
(14, CURDATE() - INTERVAL 3 DAY, 'DY',  'morning', 152, 96.8, 98.5),
(14, CURDATE() - INTERVAL 3 DAY, 'SPH', 'morning', 58,  97.5, 99.0),
(14, CURDATE() - INTERVAL 4 DAY, 'DY',  'evening', 170, 93.8, 96.0),
(14, CURDATE() - INTERVAL 4 DAY, 'XHS', 'evening', 95,  94.0, 95.8),
(14, CURDATE() - INTERVAL 5 DAY, 'DY',  'evening', 160, 95.2, 97.5),
(14, CURDATE() - INTERVAL 5 DAY, 'KS',  'evening', 70,  96.0, 97.8);

SELECT '种子数据插入完成' AS status;
