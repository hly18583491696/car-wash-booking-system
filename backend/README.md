# 汽车洗车预约系统 - 后端

## 📋 项目说明

这是汽车洗车预约系统的后端部分，基于SpringBoot 2.7.14开发。

## 🛠️ 技术栈

- **SpringBoot 2.7.14** - 核心框架
- **Spring Security + JWT** - 安全认证
- **MyBatis Plus 3.5.3.1** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存
- **Knife4j** - API文档

## 📁 项目结构

```
backend/
├── src/main/java/com/carwash/
│   ├── CarwashApplication.java          # 启动类
│   ├── common/                          # 公共类
│   │   ├── BusinessException.java       # 业务异常
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── JwtAuthenticationFilter.java # JWT过滤器
│   │   ├── JwtAuthenticationEntryPoint.java # JWT入口点
│   │   ├── constants/                   # 常量类
│   │   ├── enums/                       # 枚举类
│   │   ├── result/                      # 响应结果类
│   │   └── utils/                       # 工具类
│   ├── config/                          # 配置类
│   │   ├── CorsConfig.java             # 跨域配置
│   │   ├── MybatisPlusConfig.java      # MyBatis Plus配置
│   │   ├── RedisConfig.java            # Redis配置
│   │   ├── SecurityConfig.java         # 安全配置
│   │   ├── SwaggerConfig.java          # API文档配置
│   │   └── WebConfig.java              # Web配置
│   ├── controller/                      # 控制器
│   │   └── TestController.java         # 测试控制器
│   ├── dto/                            # 数据传输对象
│   ├── entity/                         # 实体类
│   │   └── User.java                   # 用户实体
│   ├── mapper/                         # 数据访问层
│   │   └── UserMapper.java             # 用户Mapper
│   ├── service/                        # 服务层
│   │   └── impl/                       # 服务实现
│   │       └── UserDetailsServiceImpl.java
│   └── utils/                          # 工具类
│       └── JwtUtils.java               # JWT工具
└── src/main/resources/
    ├── application.yml                  # 应用配置
    └── mapper/                         # MyBatis映射文件
```

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+ (可选，项目包含Maven Wrapper)

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE carwash_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carwash_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3. Redis配置

确保Redis服务已启动，默认配置：
- 主机：localhost
- 端口：6379
- 无密码

### 4. 安装依赖

#### 方式一：使用批处理脚本（推荐）
```bash
# Windows
install-dependencies.bat
```

#### 方式二：使用Maven命令
```bash
# 如果已安装Maven
mvn clean install -DskipTests

# 或使用Maven Wrapper
./mvnw clean install -DskipTests  # Linux/Mac
mvnw.cmd clean install -DskipTests  # Windows
```

#### 方式三：使用IDE
在IntelliJ IDEA或Eclipse中导入项目，IDE会自动下载依赖。

### 5. 启动应用

#### 方式一：IDE启动
在IDE中运行 `CarwashApplication.java` 的main方法

#### 方式二：Maven命令启动
```bash
mvn spring-boot:run
```

#### 方式三：jar包启动
```bash
mvn clean package -DskipTests
java -jar target/carwash-reservation-system-1.0.0.jar
```

### 6. 验证启动

启动成功后，访问以下地址验证：

- **健康检查**: http://localhost:8080/api/test/health
- **系统信息**: http://localhost:8080/api/test/info
- **API文档**: http://localhost:8080/doc.html

## 📝 模块4完成情况

### ✅ 已完成的配置

1. **Maven依赖配置** ✅
   - SpringBoot Web、Security、Redis
   - MyBatis Plus、MySQL驱动
   - JWT、FastJSON、Knife4j等

2. **应用配置文件** ✅
   - 数据库连接配置
   - Redis缓存配置
   - JWT配置
   - MyBatis Plus配置
   - 日志配置

3. **Spring Security配置** ✅
   - JWT认证过滤器
   - 安全过滤器链
   - 密码编码器
   - 认证入口点

4. **MyBatis Plus配置** ✅
   - 分页插件
   - 乐观锁插件
   - 自动填充处理器
   - 逻辑删除配置

5. **Redis缓存配置** ✅
   - RedisTemplate配置
   - 序列化配置
   - Redis工具类

6. **跨域配置** ✅
   - CORS配置源
   - 跨域过滤器
   - 允许所有域名和方法

7. **统一异常处理** ✅
   - 全局异常处理器
   - 业务异常类
   - 统一响应结果类
   - 各种异常类型处理

### 📋 配置文件说明

#### application.yml
- 服务端口：8080
- 上下文路径：/api
- 数据库：MySQL 8.0
- Redis：本地默认配置
- JWT：24小时过期
- 日志：debug级别

#### 安全配置
- JWT Token认证
- 无状态会话管理
- 公开接口：/api/auth/**, /api/public/**
- 管理员接口：/api/admin/**

#### 数据库配置
- 自动驼峰命名转换
- 逻辑删除支持
- 自动填充创建/更新时间
- 分页插件支持

## 🔧 开发指南

### 添加新的API接口

1. 在 `controller` 包下创建控制器
2. 使用 `@Tag` 和 `@Operation` 注解添加API文档
3. 返回统一的 `Result<T>` 格式

### 异常处理

- 业务异常：抛出 `BusinessException`
- 系统异常：由全局异常处理器统一处理
- 参数校验：使用 `@Valid` 注解

### 缓存使用

```java
@Autowired
private RedisUtils redisUtils;

// 设置缓存
redisUtils.set("key", value, 30, TimeUnit.MINUTES);

// 获取缓存
String value = redisUtils.get("key", String.class);
```

## 🐛 常见问题

### 1. 依赖下载失败
- 检查网络连接
- 配置Maven镜像源
- 使用IDE的Maven功能

### 2. 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库连接信息
- 确认数据库已创建

### 3. Redis连接失败
- 检查Redis服务是否启动
- 验证Redis配置信息

### 4. 端口占用
- 修改 `application.yml` 中的端口号
- 或停止占用8080端口的进程

## 📈 下一步计划

模块4（基础框架配置）已完成，接下来将进行：

1. **模块5**: 用户认证系统开发
2. **模块6**: 服务管理系统开发
3. **模块7**: 预约管理系统开发

## 📞 技术支持

如遇到问题，请检查：
1. 环境配置是否正确
2. 依赖是否完整下载
3. 数据库和Redis是否正常运行
4. 配置文件是否正确

---

**模块4完成状态**: ✅ 100%完成  
**最后更新**: 2025年9月24日