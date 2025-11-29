## 问题概述
- 头像上传在 `AvatarUploader.vue:84` 调用 `POST /api/auth/csrf` 返回 401（未授权）。结合全局请求拦截器与接口保护逻辑，问题本质是“认证环节失败或状态未持久化”，而非 CSRF 机制本身。

## 检查与修复步骤

### 1) 组件侧：确保 CSRF 请求带认证并友好处理
- 代码位置：`frontend/src/components/AvatarUploader.vue:78-87`
- 要点：
  - 使用统一请求实例 `request`（已使用），它会为非白名单接口自动附加 `Authorization`。
  - 在 `fetchCsrf` 前检查本地登录状态（`localStorage.token` 或 `AuthManager.isAuthenticated`）。无 token 时直接提示并中断。
  - 捕获 401 时，在组件内显示“登录已过期，请重新登录”的明确提示，并在 `confirmCrop` 中拦截流程，避免继续上传；可以选择触发路由跳转至登录页。
- 计划改动（示意）：
  - 在 `confirmCrop` 的 `try/catch` 中对 `err.response?.status === 401` 分支增加特定提示与引导。

### 2) 登录状态管理：确认登录与持久化
- 代码位置：
  - 登录流程：`frontend/src/views/Login.vue:132-160`
  - 持久化逻辑：`frontend/src/utils/auth.js:74-120`
- 要点：
  - 验证登录接口响应后，`AuthManager.login` 成功写入 `token`、`tokenType`、`userInfo` 至 `localStorage`。
  - 使用 `AuthManager.isAuthenticated()` 检查状态，确保在进入个人中心与上传流程前已登录。
  - 如需主动验证 token 有效性，调用 `realApi.getUserInfo`（`frontend/src/api/realApi.js:44-46`）；失败时清理本地并跳转登录。

### 3) 全局拦截器：统一处理 401
- 代码位置：`frontend/src/api/request.js:97-142`
- 要点：
  - 在响应拦截器 error 分支中，检测 `error.response?.status === 401`：
    - 清理本地 `token`、`userInfo`；
    - 通过 `ElMessage` 提示“未授权，请登录后重试”；
    - 路由重定向到登录页（或 guest 页），并携带 `redirect`。
  - 保持其它错误码的现有处理逻辑。

### 4) 测试流程
- 获取 CSRF 成功：
  - 登录 → 在控制台执行 `request.post('/auth/csrf', {})` → 应返回 `{ code:200, data:<token> }`。
- 完整上传：
  - 选择图片 → 裁剪 → 上传；后端返回 `avatarUrl` 与 `thumbUrl`，前端更新头像显示。
- token 过期恢复：
  - 手动清除 `localStorage.token` 或使用过期 token → 调用 `fetchCsrf` → 应触发 401 → 全局拦截器提示并跳转登录；重新登录后再次上传成功。

### 5) 友好提示改进
- 代码位置：
  - `frontend/src/components/AvatarUploader.vue:89-119`（`confirmCrop` 捕获错误处）
- 要点：
  - 针对 401 增加特定文案：“登录已过期，请重新登录后上传头像”。
  - 针对大小/格式错误沿用现有提示。
- use-button.ts（第76行）为 Element Plus 内部文件，不做直接改动；外层在组件点击事件中做好错误提示与流程中断即可。

## 额外注意
- 后端安全配置：当前 `SecurityConfig` 允许 `/api/auth/**` 公开，但在 `AuthController.csrf` 使用 `@PreAuthorize` 强制认证（方法级别）。因此 401 是预期的未登录表现。
- 若需要更严格的路由保护，可在 `backend/src/main/java/com/carwash/config/SecurityConfig.java:76-98` 增加 `.requestMatchers("/api/auth/csrf").authenticated()` 放在 `permitAll` 之前，以便更直观。

## 交付项
- 组件内 401 友好提示与流程拦截（不上传、不裁剪关闭前给出明确提示）
- 全局拦截器统一 401 处理：清理状态、提示、重定向
- 登录状态自检与（可选）主动 token 验证
- 手动与端到端测试步骤文档（按上文流程）

请确认以上方案，我将按此逐步实施并复测端到端上传流程，确保 401 场景下的体验与恢复路径一致可用。