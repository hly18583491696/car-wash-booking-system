## 目标
- 解决登录失败（code:1007）导致的认证不足问题，确保能够成功获取 CSRF 并完成头像上传。
- 完成用户注册/迁移、认证持久化、全局 401 处理、组件内友好提示与端到端验证。

## 步骤一：登录与注册验证
- 使用“注册新用户”路径进行端到端验证：
  - 在注册页提交：username、email、password（8–20，含字母数字）、confirmPassword。
  - 在登录页使用新账号登录，确认 `localStorage` 写入 `token`、`tokenType`、`userInfo`。
- 若必须使用 admin 账户：进行密码迁移（BCrypt 哈希），确保 `users.password` 为 `$2a$`/`$2b$` 开头的哈希并保持 `status=1`。

## 步骤二：组件与请求层检查
- AvatarUploader.vue（第 78–87 行）：
  - 确认 `fetchCsrf` 使用统一请求实例 `request.post('/auth/csrf')`，在未登录时直接提示并中断。
- 上传调用（第 92–101 行）：
  - 随请求附带 `X-CSRF-Token`；其余认证由 `request` 的拦截器自动附加。

## 步骤三：全局 401 处理与重定向
- 在 `request.js` 响应拦截器中：
  - 捕获 401，清理本地 `token`、`userInfo`、`userRole`；使用 `ElMessage` 提示“未授权，请重新登录”；跳转到 `'/login?redirect=<当前页>'`。
- 确认组件内 `confirmCrop` 捕获 401 时的提示与跳转逻辑一致，避免继续上传流程。

## 步骤四：端到端测试
- 获取 CSRF：登录后直接调用 `request.post('/auth/csrf', {})` 应返回 `{ code:200, data: <token> }`。
- 完整上传：选择图片→裁剪→上传，验证 `avatarUrl` 与 `thumbUrl` 返回，并页面头像更新。
- 过期令牌恢复：清空或伪造 `token` 后再获取 CSRF → 触发 401 → 自动跳转登录 → 重新登录后恢复上传成功。

## 步骤五：后端一致性（改进建议）
- 修改 `changePassword` 使用 `passwordEncoder.matches` 验证旧密码并用 `encode` 存储新密码，确保与 BCrypt 策略一致。
- 如需保留种子 admin 账户，提供一次性初始化逻辑写入已哈希密码，避免明文。

## 交付与验证
- 完成注册/登录验证与 CSRF 获取；完成头像上传端到端。
- 提供验证日志（请求头含 Authorization、返回 code:200、头像 URL 生效）。
- 如需，我将执行端到端操作并回传网络日志与结果，确认流程稳定可用。