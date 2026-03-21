# 团队销售大盘系统 - 开发任务清单

## 技术栈
- 前端：Vue 3 + Vue Router + Pinia + Tailwind CSS + ECharts
- 后端：Java 24 + Spring Boot 3.4.4 + MyBatis-Plus 3.5.10.1
- 数据库：MySQL 8
- 认证：JWT + Spring Security

---

## 阶段一：后端基础搭建

### 1.1 项目初始化
- [x] 创建 Spring Boot 项目（Maven Wrapper）
- [x] 配置 MySQL 数据源、连接池（HikariCP）
- [x] 配置 MyBatis-Plus（分页插件、驼峰映射）
- [x] 统一响应格式封装（Result<T>）
- [x] 全局异常处理（@ControllerAdvice）
- [x] 跨域配置（CORS，允许前端 localhost 访问）

### 1.2 数据库设计
- [x] `sys_user` 用户表
- [x] `sys_team` 团队表
- [x] `sys_role` 角色表
- [x] `sys_permission` 权限表
- [x] `sys_role_permission` 角色权限关联表
- [x] `biz_daily_record` 每日业绩表
- [x] `biz_quarter_target` 季度目标表
- [x] 编写建表 SQL + 初始化种子数据

### 1.3 认证与权限
- [x] JWT 工具类（生成、解析、验证 token）
- [x] 登录接口 POST /api/auth/login
- [x] Spring Security 过滤器链配置
- [x] 基于角色的接口权限控制（合伙人/组长/销售）
- [x] 获取当前用户信息 GET /api/auth/me

---

## 阶段二：核心业务接口

### 2.1 每日业绩录入
- [x] POST /api/records — 提交当日业绩（支持多平台批量提交）
- [x] 校验：同一用户同一日期同一平台不能重复提交
- [x] 自动计算 DGMV = GMV - 退款

### 2.2 业绩查看
- [x] GET /api/records — 查询业绩列表（支持按人员、日期筛选，分页）
- [x] GET /api/records/summary — 筛选结果 DGMV 合计

### 2.3 销售大盘看板（Dashboard）
- [x] GET /api/dashboard/personal — 个人战报
- [x] GET /api/dashboard/leaderboard/daily — 昨日龙虎榜 Top 5
- [x] GET /api/dashboard/leaderboard/quarter — 季度累计英雄榜
- [x] GET /api/dashboard/team-battle — 团队争霸数据
- [x] GET /api/dashboard/channel — 渠道分布
- [x] GET /api/dashboard/announcement — 喜报数据

### 2.4 团队管理
- [x] GET /api/teams — 团队列表（含成员数、季度累计、目标、完成率）
- [x] POST /api/teams — 创建团队
- [x] PUT /api/teams/:id — 编辑团队
- [x] POST /api/teams/:id/members — 添加成员到团队

### 2.5 人员管理
- [x] GET /api/users — 人员列表（支持搜索姓名/团队/角色，分页）
- [x] POST /api/users — 添加人员
- [x] PUT /api/users/:id — 编辑人员信息
- [x] PUT /api/users/:id/status — 启用/禁用

### 2.6 权限管理
- [x] GET /api/roles — 角色列表（含权限明细、人数统计）
- [x] PUT /api/roles/:id/permissions — 更新角色权限

---

## 阶段三：前端对接

### 3.1 基础设施
- [x] 封装 Axios 请求实例（baseURL、拦截器、token 注入、错误处理）
- [x] Pinia store：用户状态（token、用户信息、角色）
- [x] 路由守卫（未登录跳转登录页、权限路由控制）
- [x] 登录页面

### 3.2 页面对接（替换 mock 数据为 API 调用）
- [x] Dashboard 看板 — 对接 5 个 dashboard 接口
- [x] 每日业绩录入 — 对接提交接口
- [x] 业绩查看 — 对接查询接口 + 分页
- [x] 团队管理 — 对接团队 CRUD
- [x] 人员管理 — 对接人员 CRUD + 搜索
- [x] 权限管理 — 对接角色权限接口

---

## 阶段四：完善与部署

### 4.1 业务完善
- [ ] 职级自动计算逻辑（根据季度DGMV区间判定K3-K6）
- [ ] 数据导出（Excel 导出业绩明细）
- [x] 操作日志记录（异步审计日志 sys_audit_log）
- [x] Redis 缓存（权限缓存 30min + Dashboard 缓存 60s）
- [x] 权限系统重构（基于 permission code 的细粒度授权）
- [x] 字典管理（渠道/平台动态配置）
- [x] 数据库迁移脚本（migration-v2.sql）

### 4.2 部署
- [ ] 后端打包（Maven package → jar）
- [ ] 前端打包（npm run build → dist）
- [ ] Nginx 配置（前端静态文件 + 反向代理后端 API）
- [ ] MySQL 生产环境配置
- [ ] 环境变量管理（application-prod.yml）

---

## 建议开发顺序
1. ~~阶段一（已完成）~~ → 2. ~~阶段二（已完成）~~ → 3. ~~阶段三（已完成）~~ → 4. 阶段四
