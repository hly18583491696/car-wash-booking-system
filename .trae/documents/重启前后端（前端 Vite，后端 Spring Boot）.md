## 当前状态
- 后端正在运行：终端 3（`java -jar`，端口 `8080`，profile `dev`）与终端 7（`start-backend.bat`）
- Redis正在运行：终端 6（`redis-server.exe`，端口 `6379`）
- 前端未运行：未见开发服务器终端

## 前端信息
- 目录：`d:\Study\Code\毕业设计\frontend`
- 启动脚本：`npm run dev`（Vite 开发服务器）
- 端口：`3000`（`vite.config.js`，`strictPort: true`；可临时覆盖：`npm run dev -- --port 3001`）
- 代理：`/api` → `http://localhost:8080`（与后端一致）

## 后端信息
- 目录：`d:\Study\Code\毕业设计\backend`
- 启动方式（二选一）：
  - `start-backend.bat`
  - 直接命令：`java -Dspring.profiles.active=dev -Dserver.port=8080 -jar target/carwash-reservation-system-1.0.0.jar`
- 端口：`8080`
- 健康检查：`curl http://localhost:8080/api/debug/health`
- 外部依赖：MySQL `localhost:3306`（`carwash_db`）、Redis `localhost:6379`

## 重启步骤
1) 停止后端
- 在终端 3 与终端 7 分别按 `Ctrl + C` 结束运行；如无法结束，使用 `taskkill /F /IM java.exe`（谨慎，可能影响其他 Java 程序）
- 关闭健康检查的终端 4（无需保留），保留 Redis 终端 6

2) 启动后端
- 切换到后端目录：`cd d:\Study\Code\毕业设计\backend`
- 执行：`start-backend.bat`（或上述 `java -jar` 命令）
- 观察日志出现“Started …”后，运行：`curl http://localhost:8080/api/debug/health`，确认返回正常

3) 启动前端
- 切换到前端目录：`cd d:\Study\Code\毕业设计\frontend`
- 执行：`npm run dev`（如端口冲突则：`npm run dev -- --port 3001`）
- 访问：`http://localhost:3000`，确认页面可加载且接口请求（`/api/*`）成功

## 验证要点
- 健康接口：`/api/debug/health` 与页面功能正常
- 端口占用排查：`netstat -ano | findstr :8080` / `netstat -ano | findstr :3000`；必要时 `taskkill /PID <pid> /F`
- 环境变量：开发环境 `.env.development` 指向 `http://localhost:8080/api`，与代理一致；若后端端口变更，需同步调整或使用代理覆盖

## 结果
- 成功重启后端（8080）与前端（3000），Redis 保持运行；前端通过 `/api` 代理访问后端，系统恢复到可交互状态。