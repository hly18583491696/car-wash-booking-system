# 汽车洗车服务预约系统

一个现代化的汽车洗车服务预约管理系统，包含完整的前后端功能。

## 项目结构

```
毕业设计/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue.js 前端
├── sql/             # 数据库脚本
├── docs/            # 文档
└── docker/          # Docker 配置
```

## 技术栈

### 后端
- Spring Boot 3.x
- MyBatis Plus
- JWT 认证
- Redis 缓存
- MySQL 数据库

### 前端
- Vue.js 3.x
- Element Plus UI
- Vite 构建工具
- ECharts 数据可视化

## 功能特性

- ✅ 用户注册/登录系统
- ✅ 服务预约管理
- ✅ 数据统计仪表板
- ✅ 管理员后台系统
- ✅ 响应式设计
- ✅ 主题切换功能

## 快速开始

### 端口统一说明
前端开发端口统一为 `3000`，后端服务端口统一为 `8080`。

### 后端启动
```bash
cd backend
./mvnw spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 前端开发环境变量
将 `frontend/.env.development` 中的 `VITE_API_BASE_URL` 设置为 `http://localhost:8080/api`。

### WebSocket
前端直接使用 `ws://localhost:8080/ws/...` 与后端连接。

- 订单状态推送地址：`ws://localhost:8080/ws/order-status?userId=<当前用户ID>`
- 心跳机制：使用 `ping/pong`，前端定期发送 `ping`，后端返回 `pong`
- 可选参数：可附加 `token` 用于扩展认证（后端当前按 `userId` 校验）

## 数据库配置

1. 创建MySQL数据库
2. 执行 `sql/init.sql` 初始化数据
3. 配置数据库连接信息

## Git 远程仓库配置

如果您需要将代码推送到远程仓库：

```bash
# 添加远程仓库
git remote add origin <您的远程仓库URL>

# 推送代码
git push -u origin master
```

## 许可证

MIT License

## 联系方式

如有问题请联系：team@carwash.com