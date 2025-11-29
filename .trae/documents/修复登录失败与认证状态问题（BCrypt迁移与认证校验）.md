## 问题根因
- 后端已切换 `PasswordEncoder` 至 BCrypt（backend/src/main/java/com/carwash/config/SecurityConfig.java:44-47）。旧用户使用“明文存储”的密码将无法通过 Spring Security 的认证，导致登录接口返回 `code: 1007`（用户名或密码错误）。
- 头像上传获取 CSRF 401 同源于“未登录或令牌无效”，属于认证失败的后续表现。

## 修复总体策略
1. 保持 BCrypt 加密；对现有用户（尤其是 admin）进行密码迁移或重新注册，使数据库中 `users.password` 为 BCrypt 哈希（以 `$2a$`/`$2b$` 开头）。
2. 完整验证登录流程与令牌持久化，确保 `localStorage` 写入 `token`、`tokenType`、`userInfo` 后，后续请求附带 `Authorization`。
3. 优化前端 401 错误处理与重定向，确保过期令牌时自动引导登录并带 `redirect` 回到原页面。

## 具体执行步骤

### A. 数据侧迁移（优先）
- 方案一：直接注册新用户并用于验证
  - 使用 `POST /api/auth/register` 创建新用户（满足密码复杂度与邮箱格式）。
  - 用新用户账号进行 `POST /api/auth/login` 验证成功后，进行头像上传流程验证。
- 方案二：为现有 admin 进行密码哈希迁移
  - 在数据库中将 admin 的 `password` 更新为 BCrypt 哈希（建议使用强度 10 的哈希，如 `$2a$10$...`）。
  - 同时保留 `role='admin'`、`status=1`。
  - 注意：BCrypt 值需要与目标明文一致；示例可通过临时脚本生成，但本次仅提供迁移指导。

### B. 后端登录与认证校验
- 核对认证链：
  - 用户加载：`backend/src/main/java/com/carwash/service/impl/UserDetailsServiceImpl.java:31-60`
  - 认证执行：`backend/src/main/java/com/carwash/service/impl/UserServiceImpl.java:120-158` 使用 `AuthenticationManager`。
- 检查 `users.password` 是否为 BCrypt（非明文）。若为明文则认证必失败。
- 检查 `users.status=1`（启用）与 `deleted=0`。

### C. 前端登录持久化与请求头附加
- 登录写入：`frontend/src/utils/auth.js:74-120`（`AuthManager.login` 将写入 `token`、`tokenType`、`userInfo`、`userRole`）。
- 全局请求头：`frontend/src/api/request.js:14-67`，非白名单接口自动附加 `Authorization`。
- CSRF 获取：`frontend/src/components/AvatarUploader.vue:78-87` 使用统一 `request.post('/auth/csrf')`；若未登录则提示并中断。

### D. 401 错误处理与重定向（前端）
- 响应拦截器：`frontend/src/api/request.js:94-140` 已在 401 分支清理本地并跳转到 `'/login?redirect=<当前页>'`；确认此行为满足预期。
- 组件级：`frontend/src/components/AvatarUploader.vue:112-118` 在上传流程捕获 401 时，给出明确提示与重定向。

### E. 测试验证
1. 成功路径：
  - 新用户注册 → 登录成功（`POST /auth/login` 返回 `code:200`）→ `localStorage` 有 `token` → `POST /auth/csrf` 返回 `code:200` → 裁剪并上传头像，返回 `avatarUrl` 与 `thumbUrl`。
2. 失败恢复（过期令牌）：
  - 清空或伪造 `token` → 调用 CSRF 返回 401 → 前端自动跳转登录 → 重新登录后恢复流程。
3. admin 账号（可选）：
  - 迁移 admin 密码为 BCrypt 后，使用 admin 登录并执行上传流程。

## 后续改进建议
- 修改 `changePassword` 使用 `PasswordEncoder.matches` 验证旧密码，并用 `encode` 存储新密码，保证与 BCrypt 一致（backend/src/main/java/com/carwash/service/impl/UserServiceImpl.java）。
- 如需保留种子 admin 账户，在应用启动时通过一次性的初始化逻辑写入已哈希的 admin 密码（避免明文）。

## 备注
- 本次为计划与排查指导，不涉及直接改动。确认方案后，我将：
  - 如选择“注册新用户验证”，协助执行端到端测试；
  - 如选择“迁移 admin 密码”，提供哈希生成与更新指引并完成验证。