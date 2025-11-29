## 前提与备份
- 数据库：`carwash_db`（`backend/src/main/resources/application.yml:13`，驱动 `com.mysql.cj.jdbc.Driver` 于 `:12`，账号于 `:14-15`）
- 维护窗口：停止后端与前端本地服务，避免并发写入
- 备份命令（Windows）：`mysqldump -h localhost -u root -p --databases carwash_db > D:\backup\carwash_db_$(Get-Date -Format yyyy-MM-dd_HH-mm).sql`（使用 `-p` 交互输入密码）

## 现状定位
- 未使用 Flyway/Liquibase；不存在 `schema.sql/data.sql`
- 现有脚本：
  - 基础初始化与插入：`d:\Study\Code\毕业设计\sql\init.sql`（清空并重建核心表、插入用户/服务/时间段/系统配置；截断与插入段起始于 `init.sql:149`）
  - 支付相关表：`backend/src/main/resources/sql/payment_tables.sql`（含 `CREATE TABLE IF NOT EXISTS` 及少量测试插入于 `:69-76`）

## 清空策略（保留结构、处理外键）
- 暂停外键检查：`SET FOREIGN_KEY_CHECKS = 0;`
- 截断顺序（子→父）：
  - `refunds` → `payments` → `payment_audit` → `feedback` → `bookings` → `time_slots` → `services` → `users` → `system_config`
- 重置自增：针对所有有自增的表执行 `ALTER TABLE <table> AUTO_INCREMENT = 1;`
- 恢复外键检查：`SET FOREIGN_KEY_CHECKS = 1;`

## 初始数据准备（匹配结构与关联）
- 基础数据：
  - 用户：管理员与普通用户两条基线（见 `init.sql:184-186`）
  - 服务：11+ 条示例服务（`init.sql:189-201`）
  - 时间段：未来 7 天多时段（`init.sql:202-320`，使用 `CURDATE()` 与 `DATE_ADD`）
  - 系统配置：基础键值（`init.sql:323-336`）
- 支付与退款：仅建表与索引，默认不插入测试支付数据；如需可启用 `payment_tables.sql:69-76`，确保相应 `bookings.order_no` 存在
- 预约示例数据：若需要演示流，可使用 `d:\Study\Code\毕业设计\sql\insert_test_bookings.sql` 与 `insert_test_bookings_user3.sql`，在用户与时间段插入完成后执行

## 执行与批量导入
- 方式 A（推荐）：新建 `reset_seed.sql`（仅包含“清空+插入+校验”，不重建表，全部 `TRUNCATE` 使用上述顺序，`SET FOREIGN_KEY_CHECKS` 包裹，插入段引用 `init.sql` 的 INSERT 片段与需要的支付建表片段）
- 方式 B（现有文件）：
  - 先执行支付建表：`mysql -h localhost -u root -p carwash_db < backend\src\main\resources\sql\payment_tables.sql`
  - 执行基础初始化：`mysql -h localhost -u root -p carwash_db < d:\Study\Code\毕业设计\sql\init.sql`
  - 注意：`init.sql` 包含 `CREATE TABLE`，若表已存在可使用 `mysql --force` 忽略已存在错误，或只执行 `init.sql:149` 之后的段落
- 大数据集：拆分批次导入，多条 `INSERT` 合并为批量；必要时改用 `LOAD DATA INFILE`（需 `local_infile=1`）

## 完成后的验证
- 计数校验：
  - `SELECT COUNT(*) FROM users/services/time_slots/system_config;`
  - `SELECT COUNT(*) FROM bookings/feedback/payments/refunds/payment_audit;`（期望为空，除非启用示例插入）
- 关联有效性：
  - `SELECT b.id, u.id, s.id, t.id FROM bookings b JOIN users u ON b.user_id=u.id JOIN services s ON b.service_id=s.id JOIN time_slots t ON b.time_slot_id=t.id LIMIT 5;`
  - `SHOW CREATE TABLE refunds;` 确认 `FOREIGN KEY (payment_id) REFERENCES payments(id)`（参考 `payment_tables.sql:55-56`）
- 关键业务数据：抽样检查时间段覆盖与服务价格、系统配置键值

## 风险与回滚
- 失败或异常：使用备份文件 `mysqldump` 回滚 `mysql -h localhost -u root -p < backup.sql`
- 并发访问风险：务必在维护窗口执行并停止应用连接

## 产出与后续
- 数据库处于“干净 + 基线数据”状态，可用于联调与演示
- 完成后重启后端与前端服务并做端到端验收
- 将操作记录与数据基线更新至计划书中文档