# MCP Server Alipay 启动问题解决指南

## 问题描述
启动 `mcp-server-alipay` 时出现以下错误：
```
failed to initialize MCP client for mcp-server-alipay: transport error: context deadline exceeded
```

## 问题原因分析

### 1. 环境变量缺失（主要问题）
从错误日志分析，最常见的原因是缺少必要的环境变量：
- `AP_APP_ID` - 支付宝应用ID
- `AP_APP_KEY` - 支付宝应用私钥
- `AP_PUB_KEY` - 支付宝公钥

### 2. 网络连接问题（次要问题）
"context deadline exceeded" 错误通常表示：
- 网络连接超时
- 防火墙阻止连接
- 支付宝网关地址无法访问
- DNS解析问题

## 解决方案

### 步骤1：配置必要环境变量

1. 编辑 `.env.alipay` 文件，填入正确的配置信息：
   ```env
   AP_APP_ID=你的支付宝应用ID
   AP_APP_KEY=你的支付宝应用私钥（去除头尾和换行符）
   AP_PUB_KEY=支付宝公钥（从支付宝开放平台获取，去除头尾和换行符）
   AP_NOTIFY_URL=http://your-domain.com/api/payment/callback/alipay
   AP_RETURN_URL=http://your-domain.com/payment/success
   ```

2. 项目中已有的支付宝配置参考：
   - 配置文件位置：`backend/src/main/resources/application-payment.yml`
   - 可以参考其中的配置项，但注意MCP Server需要的环境变量格式不同

3. 确保密钥格式正确：
   - 私钥示例（去除头尾）：
     ```
     MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7J1...
     ```
   - 不要包含：
     ```
     -----BEGIN PRIVATE KEY-----
     ...
     -----END PRIVATE KEY-----
     ```

### 步骤2：检查网络连接

1. 测试支付宝网关连通性：
   ```bash
   ping openapi.alipay.com
   ```

2. 如果在企业网络环境中，可能需要配置代理：
   ```env
   HTTP_PROXY=http://proxy.company.com:8080
   HTTPS_PROXY=http://proxy.company.com:8080
   ```

### 步骤3：启动服务

运行 `start-mcp-server-alipay.bat` 脚本启动服务。

## 常见问题排查

### 1. 密钥格式问题
确保所有密钥都已去除头尾标识和换行符。

### 2. 网络超时
如果网络较慢，可以尝试增加超时时间（如果有相关配置项）。

### 3. 防火墙阻止
检查防火墙设置，确保允许出站网络连接。

### 4. DNS解析问题
尝试使用IP地址直接连接支付宝网关。

## 文件说明

- `.env.alipay`: 配置文件，包含所有必要的环境变量
- `start-mcp-server-alipay.bat`: 启动脚本，自动加载配置并启动服务

## 注意事项

1. 确保回调地址可以被外网访问（生产环境）
2. 本地测试时可以使用localhost地址
3. 在生产环境中，不要将密钥硬编码在代码中
4. 定期更新密钥以确保安全性
5. 如果使用沙箱环境，需要设置 `AP_CURRENT_ENV=sandbox`

## 获取支付宝配置的步骤

1. 登录支付宝开放平台：https://open.alipay.com/
2. 进入控制台，找到你的应用
3. 在"应用信息"中获取 `APP ID`
4. 在"开发设置"中获取或生成密钥对：
   - 生成RSA密钥对（推荐RSA2）
   - 上传应用公钥到支付宝平台
   - 保存支付宝公钥用于验签
5. 设置正确的回调地址