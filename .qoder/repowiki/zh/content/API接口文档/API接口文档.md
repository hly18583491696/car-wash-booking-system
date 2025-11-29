# API接口文档

<cite>
**本文档引用的文件**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java)
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java)
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java)
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java)
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java)
- [TimeSlotController.java](file://backend/src/main/java/com/carwash/controller/TimeSlotController.java)
- [LoginRequest.java](file://backend/src/main/java/com/carwash/dto/LoginRequest.java)
- [RegisterRequest.java](file://backend/src/main/java/com/carwash/dto/RegisterRequest.java)
- [BookingRequest.java](file://backend/src/main/java/com/carwash/dto/BookingRequest.java)
- [PaymentRequest.java](file://backend/src/main/java/com/carwash/dto/PaymentRequest.java)
- [auth.js](file://frontend/src/api/auth.js)
- [realApi.js](file://frontend/src/api/realApi.js)
- [request.js](file://frontend/src/api/request.js)
- [api.js](file://frontend/src/config/api.js)
</cite>

## 目录
1. [简介](#简介)
2. [认证管理](#认证管理)
3. [用户管理](#用户管理)
4. [服务管理](#服务管理)
5. [预约管理](#预约管理)
6. [支付管理](#支付管理)
7. [时间段管理](#时间段管理)
8. [JWT认证机制](#jwt认证机制)
9. [前端API调用示例](#前端api调用示例)

## 简介
本文档详细描述了汽车洗车服务预约系统的公共API接口。系统采用RESTful架构，所有接口均返回统一的JSON格式响应。接口通过JWT（JSON Web Token）进行身份验证和授权，确保系统安全。文档涵盖了认证、用户、服务、预约、支付等核心功能模块的API接口。

## 认证管理
认证管理接口负责用户注册、登录和身份验证相关功能。

### 用户注册
创建新用户账户。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/auth/register`
- **请求头**: 无特殊要求
- **请求体**:
```json
{
  "username": "string, 用户名，3-20字符，仅字母、数字和下划线",
  "password": "string, 密码，6-20字符",
  "confirmPassword": "string, 确认密码",
  "realName": "string, 真实姓名",
  "phone": "string, 手机号，11位数字",
  "email": "string, 邮箱地址",
  "smsCode": "string, 6位短信验证码"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

- **错误响应 (400)**
```json
{
  "code": 400,
  "message": "用户名长度必须在3-20个字符之间",
  "data": null
}
```

**Section sources**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L37-L43)
- [RegisterRequest.java](file://backend/src/main/java/com/carwash/dto/RegisterRequest.java#L19-L66)
- [realApi.js](file://frontend/src/api/realApi.js#L16-L18)

### 用户登录
用户登录系统，获取JWT令牌。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/auth/login`
- **请求头**: 无特殊要求
- **请求体**:
```json
{
  "username": "string, 用户名或手机号",
  "password": "string, 密码",
  "rememberMe": "boolean, 是否记住我，默认false"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "string, JWT令牌",
    "tokenType": "string, 令牌类型，通常为Bearer",
    "expiresIn": "number, 令牌过期时间（秒）",
    "user": {
      "id": "number, 用户ID",
      "username": "string, 用户名",
      "realName": "string, 真实姓名",
      "phone": "string, 手机号",
      "email": "string, 邮箱",
      "role": "string, 用户角色"
    }
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxxxx",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "testuser",
      "realName": "测试用户",
      "phone": "13800138000",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

- **错误响应 (401)**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

**Section sources**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L48-L58)
- [LoginRequest.java](file://backend/src/main/java/com/carwash/dto/LoginRequest.java#L19-L26)
- [realApi.js](file://frontend/src/api/realApi.js#L8-L12)

### 检查用户名
检查用户名是否已被使用。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/auth/check-username`
- **请求参数**:
  - `username` (query): 要检查的用户名

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**响应示例**
- **可用 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

- **已存在 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": false
}
```

**Section sources**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L63-L68)
- [realApi.js](file://frontend/src/api/realApi.js#L21-L24)

### 检查手机号
检查手机号是否已被使用。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/auth/check-phone`
- **请求参数**:
  - `phone` (query): 要检查的手机号

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**响应示例**
- **可用 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

- **已存在 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": false
}
```

**Section sources**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L73-L78)
- [realApi.js](file://frontend/src/api/realApi.js#L28-L31)

### 检查邮箱
检查邮箱是否已被使用。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/auth/check-email`
- **请求参数**:
  - `email` (query): 要检查的邮箱地址

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**响应示例**
- **可用 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

- **已存在 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": false
}
```

**Section sources**
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L83-L88)
- [realApi.js](file://frontend/src/api/realApi.js#L35-L38)

## 用户管理
用户管理接口用于获取和更新用户信息。

### 获取用户信息
获取当前登录用户的详细信息。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/user/info`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 用户ID",
    "username": "string, 用户名",
    "realName": "string, 真实姓名",
    "phone": "string, 手机号",
    "email": "string, 邮箱",
    "avatar": "string, 头像URL",
    "role": "string, 用户角色",
    "status": "number, 用户状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "realName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": "/avatar/default.png",
    "role": "USER",
    "status": 1,
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **未授权 (401)**
```json
{
  "code": 401,
  "message": "未授权，请重新登录",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L35-L42)
- [realApi.js](file://frontend/src/api/realApi.js#L44-L45)

### 更新用户信息
更新当前登录用户的信息。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/user/info`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "realName": "string, 真实姓名",
  "phone": "string, 手机号",
  "email": "string, 邮箱",
  "avatar": "string, 头像URL"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "用户信息更新成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "用户信息更新成功",
  "data": null
}
```

- **未授权 (401)**
```json
{
  "code": 401,
  "message": "未授权，请重新登录",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L47-L54)
- [realApi.js](file://frontend/src/api/realApi.js#L49-L51)

### 修改密码
修改当前用户的密码。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/user/change-password`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**:
  - `oldPassword` (query): 旧密码
  - `newPassword` (query): 新密码

**响应格式**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

- **密码错误 (400)**
```json
{
  "code": 400,
  "message": "原密码不正确",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L59-L66)
- [realApi.js](file://frontend/src/api/realApi.js#L54-L58)

### 获取用户列表（管理员）
管理员获取所有用户列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/user/admin/list`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 用户ID",
      "username": "string, 用户名",
      "realName": "string, 真实姓名",
      "phone": "string, 手机号",
      "email": "string, 邮箱",
      "role": "string, 用户角色",
      "status": "number, 用户状态",
      "createTime": "string, 创建时间"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "phone": "13800138001",
      "email": "admin@example.com",
      "role": "ADMIN",
      "status": 1,
      "createTime": "2025-01-01T10:00:00"
    },
    {
      "id": 2,
      "username": "user",
      "realName": "普通用户",
      "phone": "13800138002",
      "email": "user@example.com",
      "role": "USER",
      "status": 1,
      "createTime": "2025-01-01T10:00:00"
    }
  ]
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L71-L77)
- [realApi.js](file://frontend/src/api/realApi.js#L252-L254)

### 更新用户状态（管理员）
管理员更新用户状态。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/user/admin/{userId}/status`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `userId` (path): 用户ID
- **请求参数**:
  - `status` (query): 用户状态（1-正常，0-禁用）

**响应格式**
```json
{
  "code": 200,
  "message": "用户状态更新成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "用户状态更新成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L82-L88)
- [realApi.js](file://frontend/src/api/realApi.js#L267-L271)

### 删除用户（管理员）
管理员删除用户（软删除）。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/user/admin/{userId}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `userId` (path): 用户ID

**响应格式**
```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L93-L98)
- [realApi.js](file://frontend/src/api/realApi.js#L274-L276)

### 永久删除用户（管理员）
管理员永久删除用户（硬删除）。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/user/admin/{userId}/permanent`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `userId` (path): 用户ID

**响应格式**
```json
{
  "code": 200,
  "message": "用户已永久删除",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "用户已永久删除",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [UserController.java](file://backend/src/main/java/com/carwash/controller/UserController.java#L104-L118)
- [realApi.js](file://frontend/src/api/realApi.js#L279-L281)

## 服务管理
服务管理接口用于管理洗车服务项目。

### 获取服务列表
获取所有可用的洗车服务项目。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/list`
- **请求参数**:
  - `current` (query): 当前页码，默认1
  - `size` (query): 每页数量，默认10

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "number, 服务ID",
        "name": "string, 服务名称",
        "description": "string, 服务描述",
        "price": "number, 价格",
        "duration": "number, 服务时长（分钟）",
        "category": "string, 服务分类",
        "status": "number, 服务状态",
        "image": "string, 服务图片URL",
        "createTime": "string, 创建时间"
      }
    ],
    "total": "number, 总记录数",
    "size": "number, 每页数量",
    "current": "number, 当前页码",
    "pages": "number, 总页数"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "标准洗车",
        "description": "基础洗车服务",
        "price": 30,
        "duration": 30,
        "category": "basic",
        "status": 1,
        "image": "/images/services/basic.png",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L41-L48)
- [realApi.js](file://frontend/src/api/realApi.js#L63-L64)

### 获取服务详情
根据服务ID获取服务详细信息。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/{id}`
- **路径参数**:
  - `id` (path): 服务ID

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 服务ID",
    "name": "string, 服务名称",
    "description": "string, 服务描述",
    "price": "number, 价格",
    "duration": "number, 服务时长（分钟）",
    "category": "string, 服务分类",
    "status": "number, 服务状态",
    "image": "string, 服务图片URL",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "标准洗车",
    "description": "基础洗车服务",
    "price": 30,
    "duration": 30,
    "category": "basic",
    "status": 1,
    "image": "/images/services/basic.png",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **服务不存在 (404)**
```json
{
  "code": 404,
  "message": "服务不存在",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L53-L58)
- [realApi.js](file://frontend/src/api/realApi.js#L68-L69)

### 按分类获取服务
根据服务分类获取服务列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/category/{category}`
- **路径参数**:
  - `category` (path): 服务分类

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 服务ID",
      "name": "string, 服务名称",
      "description": "string, 服务描述",
      "price": "number, 价格",
      "duration": "number, 服务时长（分钟）",
      "category": "string, 服务分类",
      "status": "number, 服务状态",
      "image": "string, 服务图片URL"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "标准洗车",
      "description": "基础洗车服务",
      "price": 30,
      "duration": 30,
      "category": "basic",
      "status": 1,
      "image": "/images/services/basic.png"
    }
  ]
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L63-L68)
- [realApi.js](file://frontend/src/api/realApi.js#L73-L74)

### 搜索服务
根据关键词搜索服务。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/search`
- **请求参数**:
  - `keyword` (query): 搜索关键词

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 服务ID",
      "name": "string, 服务名称",
      "description": "string, 服务描述",
      "price": "number, 价格",
      "duration": "number, 服务时长（分钟）",
      "category": "string, 服务分类",
      "status": "number, 服务状态",
      "image": "string, 服务图片URL"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "标准洗车",
      "description": "基础洗车服务",
      "price": 30,
      "duration": 30,
      "category": "basic",
      "status": 1,
      "image": "/images/services/basic.png"
    }
  ]
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L73-L78)
- [realApi.js](file://frontend/src/api/realApi.js#L78-L82)

### 获取服务分类
获取所有服务分类列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/categories`

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    "basic",
    "premium",
    "deluxe"
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    "basic",
    "premium",
    "deluxe"
  ]
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L83-L88)
- [realApi.js](file://frontend/src/api/realApi.js#L85-L86)

### 创建服务（管理员）
管理员创建新的洗车服务项目。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/services`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "name": "string, 服务名称",
  "description": "string, 服务描述",
  "price": "number, 价格",
  "duration": "number, 服务时长（分钟）",
  "category": "string, 服务分类",
  "image": "string, 服务图片URL"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L95-L102)
- [realApi.js](file://frontend/src/api/realApi.js#L97-L98)

### 更新服务（管理员）
管理员更新洗车服务项目信息。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/services/{id}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `id` (path): 服务ID
- **请求体**:
```json
{
  "name": "string, 服务名称",
  "description": "string, 服务描述",
  "price": "number, 价格",
  "duration": "number, 服务时长（分钟）",
  "category": "string, 服务分类",
  "image": "string, 服务图片URL"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "服务更新成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "服务更新成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L107-L114)
- [realApi.js](file://frontend/src/api/realApi.js#L102-L103)

### 删除服务（管理员）
管理员删除服务项目。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/services/{id}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `id` (path): 服务ID

**响应格式**
```json
{
  "code": 200,
  "message": "服务删除成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "服务删除成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L119-L125)
- [realApi.js](file://frontend/src/api/realApi.js#L107-L108)

### 永久删除服务（管理员）
管理员永久删除服务项目。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/services/{id}/permanent`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `id` (path): 服务ID

**响应格式**
```json
{
  "code": 200,
  "message": "服务已永久删除",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "服务已永久删除",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L131-L138)
- [realApi.js](file://frontend/src/api/realApi.js#L112-L113)

### 获取所有服务（管理员）
管理员获取所有服务项目（包括下架的）。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/services/admin/all`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**:
  - `current` (query): 当前页码，默认1
  - `size` (query): 每页数量，默认10

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "number, 服务ID",
        "name": "string, 服务名称",
        "description": "string, 服务描述",
        "price": "number, 价格",
        "duration": "number, 服务时长（分钟）",
        "category": "string, 服务分类",
        "status": "number, 服务状态",
        "image": "string, 服务图片URL",
        "createTime": "string, 创建时间",
        "updateTime": "string, 更新时间"
      }
    ],
    "total": "number, 总记录数",
    "size": "number, 每页数量",
    "current": "number, 当前页码",
    "pages": "number, 总页数"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "标准洗车",
        "description": "基础洗车服务",
        "price": 30,
        "duration": 30,
        "category": "basic",
        "status": 1,
        "image": "/images/services/basic.png",
        "createTime": "2025-01-01T10:00:00",
        "updateTime": "2025-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L143-L153)
- [realApi.js](file://frontend/src/api/realApi.js#L92-L93)

### 更新服务状态（管理员）
管理员更新服务的上架/下架状态。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/services/{id}/status`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `id` (path): 服务ID
- **请求参数**:
  - `status` (query): 服务状态（1-上架，0-下架）

**响应格式**
```json
{
  "code": 200,
  "message": "服务状态更新成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "服务状态更新成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [ServiceController.java](file://backend/src/main/java/com/carwash/controller/ServiceController.java#L158-L165)
- [realApi.js](file://frontend/src/api/realApi.js#L117-L121)

## 预约管理
预约管理接口用于管理用户预约订单。

### 创建预约订单
创建新的预约订单。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/bookings`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "userId": "number, 用户ID",
  "serviceId": "number, 服务ID",
  "timeSlotId": "number, 时间段ID",
  "bookingDate": "string, 预约日期，格式YYYY-MM-DD",
  "carNumber": "string, 车牌号",
  "carModel": "string, 车型",
  "contactPhone": "string, 联系电话",
  "notes": "string, 备注信息"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 订单ID",
    "orderNo": "string, 订单号",
    "userId": "number, 用户ID",
    "serviceId": "number, 服务ID",
    "timeSlotId": "number, 时间段ID",
    "bookingDate": "string, 预约日期",
    "carNumber": "string, 车牌号",
    "carModel": "string, 车型",
    "contactPhone": "string, 联系电话",
    "notes": "string, 备注信息",
    "status": "string, 订单状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "BK202501010001",
    "userId": 1,
    "serviceId": 1,
    "timeSlotId": 1,
    "bookingDate": "2025-01-02",
    "carNumber": "粤A12345",
    "carModel": "丰田凯美瑞",
    "contactPhone": "13800138000",
    "notes": "请小心清洗",
    "status": "pending",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **参数错误 (400)**
```json
{
  "code": 400,
  "message": "用户ID不能为空",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L38-L64)
- [BookingRequest.java](file://backend/src/main/java/com/carwash/dto/BookingRequest.java#L18-L54)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 获取用户订单列表
获取指定用户的订单列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/bookings/user/{userId}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `userId` (path): 用户ID

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 订单ID",
      "orderNo": "string, 订单号",
      "userId": "number, 用户ID",
      "serviceId": "number, 服务ID",
      "timeSlotId": "number, 时间段ID",
      "bookingDate": "string, 预约日期",
      "carNumber": "string, 车牌号",
      "carModel": "string, 车型",
      "contactPhone": "string, 联系电话",
      "notes": "string, 备注信息",
      "status": "string, 订单状态",
      "createTime": "string, 创建时间",
      "updateTime": "string, 更新时间"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderNo": "BK202501010001",
      "userId": 1,
      "serviceId": 1,
      "timeSlotId": 1,
      "bookingDate": "2025-01-02",
      "carNumber": "粤A12345",
      "carModel": "丰田凯美瑞",
      "contactPhone": "13800138000",
      "notes": "请小心清洗",
      "status": "pending",
      "createTime": "2025-01-01T10:00:00",
      "updateTime": "2025-01-01T10:00:00"
    }
  ]
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L69-L75)
- [realApi.js](file://frontend/src/api/realApi.js#L189-L225)

### 获取订单详情
根据订单ID获取订单详细信息。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/bookings/{bookingId}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `bookingId` (path): 订单ID

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 订单ID",
    "orderNo": "string, 订单号",
    "userId": "number, 用户ID",
    "serviceId": "number, 服务ID",
    "timeSlotId": "number, 时间段ID",
    "bookingDate": "string, 预约日期",
    "carNumber": "string, 车牌号",
    "carModel": "string, 车型",
    "contactPhone": "string, 联系电话",
    "notes": "string, 备注信息",
    "status": "string, 订单状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "BK202501010001",
    "userId": 1,
    "serviceId": 1,
    "timeSlotId": 1,
    "bookingDate": "2025-01-02",
    "carNumber": "粤A12345",
    "carModel": "丰田凯美瑞",
    "contactPhone": "13800138000",
    "notes": "请小心清洗",
    "status": "pending",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **订单不存在 (404)**
```json
{
  "code": 404,
  "message": "订单不存在",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L80-L85)
- [realApi.js](file://frontend/src/api/realApi.js#L172-L173)

### 根据订单号获取订单详情
根据订单号获取订单详细信息。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/bookings/order/{orderNo}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `orderNo` (path): 订单号

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 订单ID",
    "orderNo": "string, 订单号",
    "userId": "number, 用户ID",
    "serviceId": "number, 服务ID",
    "timeSlotId": "number, 时间段ID",
    "bookingDate": "string, 预约日期",
    "carNumber": "string, 车牌号",
    "carModel": "string, 车型",
    "contactPhone": "string, 联系电话",
    "notes": "string, 备注信息",
    "status": "string, 订单状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "BK202501010001",
    "userId": 1,
    "serviceId": 1,
    "timeSlotId": 1,
    "bookingDate": "2025-01-02",
    "carNumber": "粤A12345",
    "carModel": "丰田凯美瑞",
    "contactPhone": "13800138000",
    "notes": "请小心清洗",
    "status": "pending",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **订单不存在 (404)**
```json
{
  "code": 404,
  "message": "订单不存在",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L90-L95)
- [realApi.js](file://frontend/src/api/realApi.js#L177-L178)

### 取消订单
取消指定的订单。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/bookings/{bookingId}/cancel`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `bookingId` (path): 订单ID
- **请求参数**:
  - `reason` (query): 取消原因

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L100-L105)
- [realApi.js](file://frontend/src/api/realApi.js#L228-L231)

### 删除订单（管理员）
管理员删除订单（软删除）。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/bookings/{bookingId}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `bookingId` (path): 订单ID

**响应格式**
```json
{
  "code": 200,
  "message": "订单删除成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "订单删除成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L111-L124)
- [realApi.js](file://frontend/src/api/realApi.js#L235-L236)

### 永久删除订单（管理员）
管理员永久删除订单。

**接口信息**
- **HTTP方法**: DELETE
- **URL路径**: `/api/bookings/{bookingId}/permanent`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `bookingId` (path): 订单ID

**响应格式**
```json
{
  "code": 200,
  "message": "订单已永久删除",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "订单已永久删除",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L130-L143)
- [realApi.js](file://frontend/src/api/realApi.js#L240-L241)

### 更新订单状态（管理员）
管理员更新订单状态。

**接口信息**
- **HTTP方法**: PUT
- **URL路径**: `/api/bookings/{bookingId}/status`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `bookingId` (path): 订单ID
- **请求参数**:
  - `status` (query): 订单状态（pending, confirmed, in_progress, completed, cancelled）

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 订单ID",
    "orderNo": "string, 订单号",
    "userId": "number, 用户ID",
    "serviceId": "number, 服务ID",
    "timeSlotId": "number, 时间段ID",
    "bookingDate": "string, 预约日期",
    "carNumber": "string, 车牌号",
    "carModel": "string, 车型",
    "contactPhone": "string, 联系电话",
    "notes": "string, 备注信息",
    "status": "string, 订单状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "BK202501010001",
    "userId": 1,
    "serviceId": 1,
    "timeSlotId": 1,
    "bookingDate": "2025-01-02",
    "carNumber": "粤A12345",
    "carModel": "丰田凯美瑞",
    "contactPhone": "13800138000",
    "notes": "请小心清洗",
    "status": "confirmed",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

- **无效状态 (400)**
```json
{
  "code": 400,
  "message": "无效的订单状态: invalid_status",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L148-L166)
- [realApi.js](file://frontend/src/api/realApi.js#L182-L185)

### 获取所有订单（管理员）
管理员获取所有订单列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/bookings/admin/all`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 订单ID",
      "orderNo": "string, 订单号",
      "userId": "number, 用户ID",
      "serviceId": "number, 服务ID",
      "timeSlotId": "number, 时间段ID",
      "bookingDate": "string, 预约日期",
      "carNumber": "string, 车牌号",
      "carModel": "string, 车型",
      "contactPhone": "string, 联系电话",
      "notes": "string, 备注信息",
      "status": "string, 订单状态",
      "createTime": "string, 创建时间",
      "updateTime": "string, 更新时间"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderNo": "BK202501010001",
      "userId": 1,
      "serviceId": 1,
      "timeSlotId": 1,
      "bookingDate": "2025-01-02",
      "carNumber": "粤A12345",
      "carModel": "丰田凯美瑞",
      "contactPhone": "13800138000",
      "notes": "请小心清洗",
      "status": "confirmed",
      "createTime": "2025-01-01T10:00:00",
      "updateTime": "2025-01-01T10:00:00"
    }
  ]
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L184-L191)
- [realApi.js](file://frontend/src/api/realApi.js#L167-L168)

### 数据同步检查（管理员）
执行数据同步检查。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/bookings/admin/sync-check`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalBookings": "number, 总订单数",
    "statusDistribution": {
      "pending": "number, 待确认订单数",
      "confirmed": "number, 已确认订单数",
      "in_progress": "number, 进行中订单数",
      "completed": "number, 已完成订单数",
      "cancelled": "number, 已取消订单数"
    },
    "lastSyncTime": "string, 最后同步时间",
    "syncStatus": "string, 同步状态"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalBookings": 5,
    "statusDistribution": {
      "pending": 1,
      "confirmed": 2,
      "in_progress": 1,
      "completed": 1,
      "cancelled": 0
    },
    "lastSyncTime": "2025-01-01T10:00:00",
    "syncStatus": "success"
  }
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/carwash/controller/BookingController.java#L196-L227)

## 支付管理
支付管理接口用于处理支付相关操作。

### 创建支付订单
创建支付订单。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/create`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "orderNo": "string, 订单号",
  "amount": "number, 支付金额",
  "paymentMethod": "string, 支付方式（wechat, alipay, credit_card）",
  "channel": "string, 支付渠道（qr, app, h5）",
  "description": "string, 支付描述",
  "returnUrl": "string, 支付成功回调地址",
  "notifyUrl": "string, 支付异步通知地址"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "paymentNo": "string, 支付单号",
    "orderNo": "string, 订单号",
    "amount": "number, 支付金额",
    "paymentMethod": "string, 支付方式",
    "channel": "string, 支付渠道",
    "status": "string, 支付状态",
    "qrCodeUrl": "string, 二维码支付链接",
    "appId": "string, APP支付应用ID",
    "timeStamp": "string, 时间戳",
    "nonceStr": "string, 随机字符串",
    "package": "string, 支付包",
    "signType": "string, 签名类型",
    "paySign": "string, 支付签名"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "paymentNo": "PAY202501010001",
    "orderNo": "BK202501010001",
    "amount": 30,
    "paymentMethod": "wechat",
    "channel": "qr",
    "status": "pending",
    "qrCodeUrl": "https://pay.weixin.qq.com/qrcode/xxxx"
  }
}
```

- **参数错误 (400)**
```json
{
  "code": 400,
  "message": "订单号不能为空",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L47-L63)
- [PaymentRequest.java](file://backend/src/main/java/com/carwash/dto/PaymentRequest.java#L20-L62)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 查询支付状态
查询支付订单状态。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/status/{paymentNo}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `paymentNo` (path): 支付单号

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "paymentNo": "string, 支付单号",
    "orderNo": "string, 订单号",
    "amount": "number, 支付金额",
    "paymentMethod": "string, 支付方式",
    "channel": "string, 支付渠道",
    "status": "string, 支付状态",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "paymentNo": "PAY202501010001",
    "orderNo": "BK202501010001",
    "amount": 30,
    "paymentMethod": "wechat",
    "channel": "qr",
    "status": "completed",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:05:00"
  }
}
```

- **支付单不存在 (404)**
```json
{
  "code": 404,
  "message": "支付单不存在",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L68-L78)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 根据订单号查询支付信息
根据订单号查询支付信息。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/order/{orderNo}`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **路径参数**:
  - `orderNo` (path): 订单号

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "number, 支付记录ID",
    "paymentNo": "string, 支付单号",
    "orderNo": "string, 订单号",
    "amount": "number, 支付金额",
    "paymentMethod": "string, 支付方式",
    "channel": "string, 支付渠道",
    "status": "string, 支付状态",
    "clientIp": "string, 客户端IP",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "paymentNo": "PAY202501010001",
    "orderNo": "BK202501010001",
    "amount": 30,
    "paymentMethod": "wechat",
    "channel": "qr",
    "status": "completed",
    "clientIp": "192.168.1.1",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:05:00"
  }
}
```

- **支付信息不存在 (404)**
```json
{
  "code": 404,
  "message": "支付信息不存在",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L83-L93)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 获取用户支付记录
获取当前用户的支付记录。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/user/payments`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 支付记录ID",
      "paymentNo": "string, 支付单号",
      "orderNo": "string, 订单号",
      "amount": "number, 支付金额",
      "paymentMethod": "string, 支付方式",
      "channel": "string, 支付渠道",
      "status": "string, 支付状态",
      "clientIp": "string, 客户端IP",
      "createTime": "string, 创建时间",
      "updateTime": "string, 更新时间"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "paymentNo": "PAY202501010001",
      "orderNo": "BK202501010001",
      "amount": 30,
      "paymentMethod": "wechat",
      "channel": "qr",
      "status": "completed",
      "clientIp": "192.168.1.1",
      "createTime": "2025-01-01T10:00:00",
      "updateTime": "2025-01-01T10:05:00"
    }
  ]
}
```

- **未授权 (401)**
```json
{
  "code": 401,
  "message": "未授权，请重新登录",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L98-L109)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 申请退款
用户申请退款。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/refund`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "paymentNo": "string, 支付单号",
  "refundAmount": "number, 退款金额",
  "reason": "string, 退款原因"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "refundNo": "string, 退款单号",
    "paymentNo": "string, 支付单号",
    "refundAmount": "number, 退款金额",
    "status": "string, 退款状态",
    "reason": "string, 退款原因",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "refundNo": "REF202501010001",
    "paymentNo": "PAY202501010001",
    "refundAmount": 30,
    "status": "pending",
    "reason": "服务不满意",
    "createTime": "2025-01-01T10:10:00",
    "updateTime": "2025-01-01T10:10:00"
  }
}
```

- **参数错误 (400)**
```json
{
  "code": 400,
  "message": "支付单号不能为空",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L114-L127)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 微信支付回调
微信支付异步通知回调接口。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/callback/wechat`
- **请求头**: 无特殊要求
- **请求参数**: 微信支付系统发送的回调参数

**响应格式**
```xml
<xml>
  <return_code><![CDATA[SUCCESS]]></return_code>
  <return_msg><![CDATA[OK]]></return_msg>
</xml>
```

**响应示例**
- **成功响应 (200)**
```xml
<xml>
  <return_code><![CDATA[SUCCESS]]></return_code>
  <return_msg><![CDATA[OK]]></return_msg>
</xml>
```

- **失败响应 (200)**
```xml
<xml>
  <return_code><![CDATA[FAIL]]></return_code>
  <return_msg><![CDATA[处理失败]]></return_msg>
</xml>
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L132-L147)

### 支付宝支付回调
支付宝支付异步通知回调接口。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/callback/alipay`
- **请求头**: 无特殊要求
- **请求参数**: 支付宝支付系统发送的回调参数

**响应格式**
```
success
```

**响应示例**
- **成功响应 (200)**
```
success
```

- **失败响应 (200)**
```
fail
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L153-L165)

### 虚拟支付回调
虚拟支付异步通知回调接口（开发测试用）。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/callback/virtual`
- **请求头**: 无特殊要求
- **请求参数**: 虚拟支付系统发送的回调参数

**响应格式**
```
ok
```

**响应示例**
- **成功响应 (200)**
```
ok
```

- **失败响应 (200)**
```
fail
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L170-L181)

### 管理员退款操作
管理员执行退款操作。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/admin/refund`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求体**:
```json
{
  "paymentNo": "string, 支付单号",
  "refundAmount": "number, 退款金额",
  "reason": "string, 退款原因"
}
```

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "refundNo": "string, 退款单号",
    "paymentNo": "string, 支付单号",
    "refundAmount": "number, 退款金额",
    "status": "string, 退款状态",
    "reason": "string, 退款原因",
    "createTime": "string, 创建时间",
    "updateTime": "string, 更新时间"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "refundNo": "REF202501010001",
    "paymentNo": "PAY202501010001",
    "refundAmount": 30,
    "status": "completed",
    "reason": "客户要求退款",
    "createTime": "2025-01-01T10:10:00",
    "updateTime": "2025-01-01T10:15:00"
  }
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L186-L199)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 取消过期支付订单（管理员）
管理员取消过期的支付订单。

**接口信息**
- **HTTP方法**: POST
- **URL路径**: `/api/payment/admin/cancel-expired`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "取消过期支付订单成功",
  "data": null
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "取消过期支付订单成功",
  "data": null
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L205-L216)

### 获取所有支付记录（管理员）
管理员获取所有支付记录。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/admin/records`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 支付记录ID",
      "paymentNo": "string, 支付单号",
      "orderNo": "string, 订单号",
      "amount": "number, 支付金额",
      "paymentMethod": "string, 支付方式",
      "channel": "string, 支付渠道",
      "status": "string, 支付状态",
      "clientIp": "string, 客户端IP",
      "createTime": "string, 创建时间",
      "updateTime": "string, 更新时间"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "paymentNo": "PAY202501010001",
      "orderNo": "BK202501010001",
      "amount": 30,
      "paymentMethod": "wechat",
      "channel": "qr",
      "status": "completed",
      "clientIp": "192.168.1.1",
      "createTime": "2025-01-01T10:00:00",
      "updateTime": "2025-01-01T10:05:00"
    }
  ]
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L221-L232)
- [realApi.js](file://frontend/src/api/realApi.js#L245-L247)

### 分页查询支付审计日志（管理员）
管理员分页查询支付审计日志。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/admin/audits`
- **请求头**: 
  - `Authorization: Bearer <JWT令牌>`
- **请求参数**:
  - `current` (query): 当前页码，默认1
  - `size` (query): 每页数量，默认10
  - `paymentNo` (query): 支付单号
  - `orderNo` (query): 订单号
  - `eventType` (query): 事件类型
  - `status` (query): 状态

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "number, 审计记录ID",
        "paymentNo": "string, 支付单号",
        "eventType": "string, 事件类型",
        "eventData": "object, 事件数据",
        "status": "string, 状态",
        "operatorId": "number, 操作员ID",
        "createTime": "string, 创建时间"
      }
    ],
    "total": "number, 总记录数",
    "size": "number, 每页数量",
    "current": "number, 当前页码",
    "pages": "number, 总页数"
  }
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "paymentNo": "PAY202501010001",
        "eventType": "CREATE",
        "eventData": {},
        "status": "SUCCESS",
        "operatorId": 1,
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

- **权限不足 (403)**
```json
{
  "code": 403,
  "message": "拒绝访问",
  "data": null
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L237-L254)

### 获取RSA公钥
获取前端加密所需的RSA公钥。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/payment/security/public-key`
- **请求头**: 无特殊要求
- **请求参数**: 无

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----"
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----"
}
```

**Section sources**
- [PaymentController.java](file://backend/src/main/java/com/carwash/controller/PaymentController.java#L259-L269)

## 时间段管理
时间段管理接口用于获取可用的预约时间段。

### 获取可用时间段
根据日期获取可用的预约时间段列表。

**接口信息**
- **HTTP方法**: GET
- **URL路径**: `/api/time-slots/available`
- **请求参数**:
  - `date` (query): 日期，格式YYYY-MM-DD

**响应格式**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "number, 时间段ID",
      "startTime": "string, 开始时间，格式HH:mm",
      "endTime": "string, 结束时间，格式HH:mm",
      "status": "number, 状态（0-不可用，1-可用）",
      "date": "string, 日期，格式YYYY-MM-DD"
    }
  ]
}
```

**响应示例**
- **成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "startTime": "09:00",
      "endTime": "09:30",
      "status": 1,
      "date": "2025-01-02"
    },
    {
      "id": 2,
      "startTime": "09:30",
      "endTime": "10:00",
      "status": 1,
      "date": "2025-01-02"
    }
  ]
}
```

**Section sources**
- [TimeSlotController.java](file://backend/src/main/java/com/carwash/controller/TimeSlotController.java#L31-L36)

## JWT认证机制
系统采用JWT（JSON Web Token）进行身份验证和授权，确保API接口的安全性。

### 认证流程
1. 用户通过`/api/auth/login`接口进行登录，提供用户名和密码
2. 服务器验证用户凭证，验证通过后生成JWT令牌
3. 服务器将JWT令牌返回给客户端
4. 客户端在后续请求中将JWT令牌放在`Authorization`请求头中
5. 服务器验证JWT令牌的有效性，验证通过后处理请求

### 请求头格式
所有需要认证的接口都需要在请求头中包含JWT令牌：
```
Authorization: Bearer <JWT令牌>
```

### 令牌有效期
JWT令牌具有有效期，系统默认设置为24小时。过期后需要重新登录获取新的令牌。

### 权限控制
系统通过JWT令牌中的用户角色信息进行权限控制：
- `USER`角色：可以访问用户相关接口和创建预约等基本功能
- `ADMIN`角色：具有管理员权限，可以访问所有接口，包括用户管理、服务管理等

### 无认证接口
以下接口不需要JWT认证：
- `/api/auth/register`：用户注册
- `/api/auth/login`：用户登录
- `/api/auth/check-username`：检查用户名
- `/api/auth/check-phone`：检查手机号
- `/api/auth/check-email`：检查邮箱

### 前端实现
前端通过`request.js`中的请求拦截器自动处理JWT令牌：
1. 检查请求是否需要认证
2. 从`localStorage`中获取JWT令牌
3. 将令牌添加到`Authorization`请求头中
4. 发送请求

当收到401未授权响应时，前端会自动清除本地存储的令牌信息。

**Section sources**
- [request.js](file://frontend/src/api/request.js#L15-L48)
- [auth.js](file://frontend/src/api/auth.js#L21-L24)
- [AuthController.java](file://backend/src/main/java/com/carwash/controller/AuthController.java#L48-L58)

## 前端API调用示例
以下示例展示了前端如何调用API接口。

### 用户登录
```javascript
import { authApi } from '@/api/auth.js';

async function login() {
  try {
    const response = await authApi.login('username', 'password');
    console.log('登录成功:', response);
    
    // 保存用户信息到本地存储
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('tokenType', response.data.tokenType);
    localStorage.setItem('user', JSON.stringify(response.data.user));
  } catch (error) {
    console.error('登录失败:', error);
  }
}
```

### 获取用户信息
```javascript
import { authApi } from '@/api/auth.js';

async function getUserInfo() {
  try {
    const response = await authApi.getUserInfo();
    console.log('用户信息:', response.data);
    return response.data;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    throw error;
  }
}
```

### 创建预约订单
```javascript
import { realApi } from '@/api/realApi.js';

async function createBooking() {
  const bookingData = {
    userId: 1,
    serviceId: 1,
    timeSlotId: 1,
    bookingDate: '2025-01-02',
    carNumber: '粤A12345',
    carModel: '丰田凯美瑞',
    contactPhone: '13800138000',
    notes: '请小心清洗'
  };
  
  try {
    const response = await realApi.createBooking(bookingData);
    console.log('预约创建成功:', response.data);
    return response.data;
  } catch (error) {
    console.error('预约创建失败:', error);
    throw error;
  }
}
```

### 获取服务列表
```javascript
import { realApi } from '@/api/realApi.js';

async function getServices() {
  try {
    const response = await realApi.getServices({ current: 1, size: 10 });
    console.log('服务列表:', response.data);
    return response.data;
  } catch (error) {
    console.error('获取服务列表失败:', error);
    throw error;
  }
}
```

### 支付订单
```javascript
import { realApi } from '@/api/realApi.js';

async function createPayment() {
  const paymentData = {
    orderNo: 'BK202501010001',
    amount: 30,
    paymentMethod: 'wechat',
    channel: 'qr',
    description: '洗车服务费用'
  };
  
  try {
    const response = await realApi.createPayment(paymentData);
    console.log('支付订单创建成功:', response.data);
    
    // 显示支付二维码
    if (response.data.qrCodeUrl) {
      showQRCode(response.data.qrCodeUrl);
    }
    
    return response.data;
  } catch (error) {
    console.error('创建支付订单失败:', error);
    throw error;
  }
}
```

**Section sources**
- [auth.js](file://frontend/src/api/auth.js#L9-L13)
- [realApi.js](file://frontend/src/api/realApi.js#L8-L12)
- [request.js](file://frontend/src/api/request.js#L6-L12)