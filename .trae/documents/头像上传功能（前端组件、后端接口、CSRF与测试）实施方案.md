## 概述
- 在个人中心页的头像区域（frontend/src/views/Profile.vue:18-24）集成“头像上传”完整流程：选择/拖拽、预览、裁剪、进度、大小与类型校验；后端接收、校验、存储、缩略图生成与用户头像字段更新；返回规范的处理结果。
- 兼容移动端交互；增加 CSRF 保护策略；预留断点续传设计（可选）。

## 前端实现

### 组件与交互
- 新增 `frontend/src/components/AvatarUploader.vue`，并在 `Profile.vue` 的头像按钮（:21）替换为该组件。
- 功能：
  - 点击区域触发文件选择；支持拖拽上传（阻止默认行为，高亮边框）。
  - 允许文件类型：`image/jpeg,image/png,image/gif`；大小限制：`<= 2MB`（可配置）。
  - 选择后立即本地预览；进入裁剪编辑弹窗（居中 1:1 裁剪框，可缩放/移动）。
  - 裁剪完成后生成 Blob 上传；展示 `ElProgress` 上传进度（Axios/XHR progress）。
  - 上传成功：更新 `userInfo.avatar` 显示新头像；失败：`ElMessage.error` 友好提示。
- 技术：Vue 3 + Element Plus；裁剪库建议使用 `cropperjs`（新增依赖），移动端支持触摸手势。

### 组件结构要点
- Props：`uploadUrl='/api/user/avatar/upload'`，`maxSize=2*1024*1024`，`accept='image/*'`。
- Emits：`success(newAvatarUrl)`；失败 `error(message)`。
- 状态：`file`、`previewUrl`、`croppedBlob`、`progress`、`dragOver`、`csrfToken`。
- 逻辑：
  - beforeUpload 校验类型/大小；
  - 打开裁剪弹窗 → 生成最终裁剪 Blob；
  - 获取 CSRF token（见后端 `/api/auth/csrf`），在请求头带 `X-CSRF-Token`；
  - 发起 `POST` 上传，`FormData` 包含：`file`、`fileName`、`width/height`、`cropMeta`。

### 兼容与体验
- 移动端：文件选择优先；裁剪支持触摸；拖拽在移动端降级。
- 进度条与状态提示：`ElProgress` + `ElMessage`；错误明确说明：格式、大小、网络、服务错误。

## 后端实现

### 接口设计
- `POST /api/user/avatar/upload`（认证接口，需 `USER` 或 `ADMIN`）
  - 请求：`multipart/form-data`，字段：`file`（必填），`fileName`（可选），`cropMeta`（可选，JSON），`width/height`（可选）。
  - 头：`Authorization: Bearer <JWT>`；`X-CSRF-Token: <token>`。
  - 返回：`{ code:200, message:'上传成功', data:{ avatarUrl:'/uploads/avatars/<uid>/<file>.jpg', thumbUrl:'/uploads/avatars/<uid>/thumb_<file>.jpg' } }`。

### 校验与处理
- 文件校验：
  - 大小：`<= 2MB`；
  - 类型：白名单 `image/jpeg/png/gif`；通过 `MultipartFile.getContentType()` 与魔数读取（ImageIO）双重校验；
  - 文件名：清理非法字符，使用 `UUID` 命名避免冲突。
- 存储：
  - 目录：`uploads/avatars/<userId>/`（与 `WebConfig.java:35-38` 中静态资源映射一致）；
  - 原图保存；生成缩略图（如 256x256）并保存 `thumb_` 前缀；
  - 依赖：建议引入 `net.coobird:thumbnailator` 生成缩略图（或使用 `ImageIO`/`BufferedImage` 手工缩放）。
- 业务更新：
  - 更新 `users.avatar` 字段（`backend/src/main/java/com/carwash/entity/User.java:57-60`）；
  - 返回完整 URL（前端直接可用）。

### 安全与 CSRF
- 认证：`@PreAuthorize('hasRole('USER') or hasRole('ADMIN')')`；使用现有 JWT 方案。
- CSRF：保持全局 CSRF 关闭（当前 `SecurityConfig` 无状态 JWT），为上传接口引入“自定义 CSRF”：
  - 新增 `POST /api/auth/csrf`，生成短期有效 CSRF token（签名/HMAC，含 userId 和过期时间），可存储 Redis 并设置 5 分钟有效；
  - 前端在上传请求头携带 `X-CSRF-Token`；后端验证签名、userId 与时间窗；失败返回 403。

## 断点续传（可选加分项）
- 设计接口：
  - `POST /api/user/avatar/upload/init` → 返回 `uploadId`；
  - `POST /api/user/avatar/upload/chunk`（字段：`uploadId`、`index`、`total`、`chunk`）；
  - `POST /api/user/avatar/upload/complete` 合并分片并生成缩略图；
- 分片暂存：`uploads/temp/<uploadId>/`；完成后合并到用户目录并清理临时文件。
- 前端：在 `AvatarUploader.vue` 中针对大文件自动走分片流程；2MB 默认走普通直传。

## 单元测试与端到端测试

### 后端测试
- 使用 `SpringBootTest + MockMvc`：
  - 成功用例：上传合法 JPG/PNG，返回 200 并更新用户头像；
  - 失败用例：超大小、非法类型、缺少 CSRF、未认证；
  - 缩略图存在性与静态资源可访问性校验（GET `/uploads/...` 返回 200）。

### 前端测试
- `Vitest + @vue/test-utils`：
  - 组件渲染与交互：点击/拖拽选择、大小类型校验、错误提示；
  - 裁剪流程：模拟设置裁剪区域并生成 Blob；
  - 上传进度：模拟 XHR progress 回调；成功后 emits `success` 并更新头像。
- 端到端（可选）：新增 `Playwright`，脚本从登录→进入个人中心→选择图像→裁剪→上传→看到新头像显示。

## 配置与运维
- 目录：在项目根确保存在 `uploads/`，启动时自动创建 `uploads/avatars` 与 `uploads/temp`；
- 清理策略：定期清理 `temp` 与过旧的未引用原图（可后续实现）。
- 日志与审计：记录用户 ID、文件名、大小、IP；错误堆栈在服务端日志可追踪。

## 变更清单（拟）
- 前端：
  - `frontend/src/components/AvatarUploader.vue`（新建）
  - `frontend/src/views/Profile.vue`（集成组件，替换原按钮交互）
- 后端：
  - `backend/src/main/java/com/carwash/controller/UserController.java`（新增 `/avatar/upload` 映射）
  - `backend/src/main/java/com/carwash/service/UserService.java` & `impl`（新增更新头像方法）
  - `backend/src/main/java/com/carwash/service/CsrfService.java`（新建，生成/验证 CSRF）
  - `backend/src/main/java/com/carwash/controller/AuthController.java`（新增 `/csrf` 端点）
  - `backend/pom.xml`（可选新增 `thumbnailator` 依赖）
  - `backend/src/main/java/com/carwash/config/WebConfig.java`（已具备 `/uploads/**` 静态资源映射）
- 测试：
  - 后端：`backend/src/test/.../AvatarUploadTests.java`（MockMvc）
  - 前端：`frontend/tests/AvatarUploader.spec.ts`（Vitest）与可选 `playwright` 目录

## 交互协议示例
- 成功：`{ code:200, message:'上传成功', data:{ avatarUrl:'/uploads/avatars/123/20251114_abc.jpg', thumbUrl:'/uploads/avatars/123/thumb_20251114_abc.jpg' } }`
- 失败：`{ code:400|413|415|403, message:'文件大小超限'|'文件类型不支持'|'CSRF校验失败' }`

请确认上述方案；确认后我将按清单逐项实现并补充测试，确保端到端可靠可用。