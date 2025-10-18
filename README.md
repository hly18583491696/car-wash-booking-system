# 汽车洗车服务预约系统

## 📋 项目简介

基于SpringBoot + Vue.js的前后端分离汽车洗车服务预约系统，提供用户在线预约洗车服务、管理员后台管理等功能。

## 🚀 技术栈

### 后端技术
- **SpringBoot 2.7.14** - 核心框架
- **Spring Security + JWT** - 安全认证
- **MyBatis Plus** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存和会话管理
- **Maven** - 依赖管理

### 前端技术
- **Vue.js 3** - 前端框架
- **Element Plus** - UI组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端
- **Vite** - 构建工具

## 📁 项目结构

```
毕业设计/
├── backend/                 # 后端SpringBoot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/carwash/
│   │   │   │   ├── controller/     # 控制器层
│   │   │   │   ├── service/        # 服务层
│   │   │   │   ├── mapper/         # 数据访问层
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── common/         # 公共类
│   │   │   │   └── utils/          # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml # 应用配置
│   │   │       └── mapper/         # MyBatis映射文件
│   │   └── test/                   # 测试代码
│   └── pom.xml                     # Maven配置
├── frontend/                # 前端Vue项目
│   ├── src/
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── api/                    # API接口
│   │   └── assets/                 # 静态资源
│   ├── public/                     # 公共文件
│   ├── package.json                # 依赖配置
│   └── vite.config.js              # Vite配置
├── sql/                     # 数据库脚本
├── docs/                    # 项目文档
└── docker/                  # Docker配置
```

## 🛠️ 环境要求

- **JDK 17+**
- **Node.js 18+**
- **MySQL 8.0+**
- **Redis 7.0+**
- **Maven 3.8+**

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd 毕业设计
```

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

### 4. 数据库配置

1. 创建数据库 `carwash_db`
2. 执行 `sql/init.sql` 初始化数据库表
3. 修改 `backend/src/main/resources/application.yml` 中的数据库连接配置

## 📱 功能模块

### 用户端功能
- ✅ 用户注册/登录
- ✅ 服务项目浏览
- ⏳ 在线预约服务
- ⏳ 订单管理
- ⏳ 个人信息管理
- ⏳ AI智能客服

### 管理端功能
- ⏳ 用户管理
- ⏳ 服务项目管理
- ⏳ 订单管理
- ⏳ 时间段管理
- ⏳ 数据统计分析

## 🔧 开发进度

- [x] 项目初始化
- [x] 环境搭建
- [x] 基础框架搭建
- [x] 首页和登录页面
- [ ] 用户认证系统
- [ ] 服务管理模块
- [ ] 预约系统
- [ ] 订单管理
- [ ] 管理后台
- [ ] AI客服集成
- [ ] 系统测试
- [ ] 部署上线

## 📝 API文档

API文档将在后端启动后访问：`http://localhost:8080/api/doc.html`

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目仅用于学习和毕业设计目的。

## 📞 联系方式

如有问题，请联系项目维护者。

---

**注意**: 这是一个毕业设计项目，正在积极开发中。部分功能可能尚未完成。