# Redis安装和配置指南

## 问题分析

MCP Server Redis启动失败，错误信息：
```
failed to initialize MCP client for Redis: transport error: context deadline exceeded
```

通过测试命令发现：
```
[Redis Error] AggregateError [ECONNREFUSED]:
```

这表明Redis服务没有在指定地址（localhost:6379）上运行，连接被拒绝。

## 解决方案

### 方案一：Windows系统安装Redis

#### 1. 下载Redis

访问Redis官方GitHub发布页面：
https://github.com/redis/redis/releases

下载适用于Windows的Redis版本，或者使用Microsoft的Windows端口：
https://github.com/microsoftarchive/redis/releases

#### 2. 安装Redis

##### 方法A：使用Chocolatey（推荐）
```cmd
choco install redis
```

##### 方法B：手动安装
1. 下载Redis压缩包
2. 解压到目录，如：`C:\redis`
3. 将Redis目录添加到系统PATH环境变量

#### 3. 启动Redis服务

##### 作为临时服务启动：
```cmd
redis-server
```

##### 作为Windows服务安装并启动：
```cmd
redis-server --service-install redis.windows.conf --loglevel verbose
redis-server --service-start
```

#### 4. 验证Redis是否运行

打开新命令提示符窗口，执行：
```cmd
redis-cli ping
```

如果返回 `PONG`，说明Redis服务正在运行。

### 方案二：使用Docker运行Redis（推荐）

#### 1. 安装Docker Desktop

访问 https://www.docker.com/products/docker-desktop 下载并安装Docker Desktop

#### 2. 运行Redis容器

```cmd
docker run -d -p 6379:6379 --name redis-server redis
```

#### 3. 验证Redis是否运行

```cmd
docker exec -it redis-server redis-cli ping
```

如果返回 `PONG`，说明Redis服务正在运行。

### 方案三：使用WSL（Windows Subsystem for Linux）

#### 1. 安装WSL和Ubuntu

```cmd
wsl --install
```

#### 2. 在WSL中安装Redis

```bash
sudo apt update
sudo apt install redis-server
```

#### 3. 启动Redis服务

```bash
sudo service redis-server start
```

#### 4. 验证Redis是否运行

```bash
redis-cli ping
```

## 配置项目中的Redis连接

你的项目中Redis配置在 `backend/src/main/resources/application.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000ms
```

确保这些配置与你的Redis服务配置一致。

## 启动MCP Server Redis

在Redis服务正常运行后，执行以下命令启动MCP Server Redis：

```cmd
npx -y @modelcontextprotocol/server-redis redis://localhost:6379
```

## 常见问题排查

### 1. 端口被占用
检查6379端口是否被其他程序占用：
```cmd
netstat -an | findstr :6379
```

### 2. 防火墙阻止
确保Windows防火墙允许Redis端口通信。

### 3. Redis配置文件问题
如果使用自定义Redis配置文件，确保bind地址设置正确：
```
bind 127.0.0.1
port 6379
```

### 4. 权限问题
在某些情况下，可能需要以管理员权限运行Redis服务。

## 验证连接

创建一个简单的测试脚本来验证Redis连接：

```javascript
// test-redis-connection.js
const redis = require('redis');

const client = redis.createClient({
  url: 'redis://localhost:6379'
});

client.on('error', (err) => console.log('Redis Client Error', err));

async function testConnection() {
  try {
    await client.connect();
    console.log('Connected to Redis');
    
    await client.set('test-key', 'test-value');
    const value = await client.get('test-key');
    console.log('Retrieved value:', value);
    
    await client.quit();
  } catch (error) {
    console.error('Connection failed:', error);
  }
}

testConnection();
```

运行测试：
```cmd
node test-redis-connection.js
```

## 项目启动顺序

建议按以下顺序启动项目组件：

1. 启动数据库（MySQL）
2. 启动Redis服务
3. 启动后端服务
4. 启动前端服务
5. 启动MCP Server Redis

这样可以确保所有依赖服务都已准备就绪。