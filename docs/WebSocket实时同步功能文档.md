# WebSocket实时同步功能文档

## 概述

本文档描述了汽车洗车服务预约系统中WebSocket实时同步功能的实现，该功能主要用于实时推送订单状态变更通知给客户端。

## 功能特性

- ✅ 实时订单状态推送
- ✅ 用户会话管理
- ✅ 心跳检测机制
- ✅ 自动重连机制
- ✅ 连接状态监控
- ✅ 错误处理和日志记录

## 技术架构

### 后端架构

```
WebSocketConfig (配置层)
    ↓
OrderStatusWebSocketHandler (处理层)
    ↓
WebSocketService (服务层)
    ↓
WebSocketServiceImpl (实现层)
```

### 前端架构

```
WebSocketManager (管理层)
    ↓
Vue组件 (UI层)
    ↓
用户界面 (展示层)
```

## 后端实现

### 1. WebSocket配置 (WebSocketConfig.java)

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private OrderStatusWebSocketHandler orderStatusWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderStatusWebSocketHandler, "/ws/order-status")
                .setAllowedOrigins("*");
    }
}
```

**功能说明：**
- 启用WebSocket支持
- 注册订单状态WebSocket处理器
- 配置跨域访问策略

### 2. WebSocket处理器 (OrderStatusWebSocketHandler.java)

```java
@Component
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {
    
    // 用户会话映射
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();
    
    // 核心方法
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception;
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception;
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception;
    
    // 业务方法
    public void pushOrderStatusUpdate(Long userId, OrderStatusUpdateMessage message);
    public void broadcastMessage(WebSocketMessage message);
    public int getActiveConnectionCount();
}
```

**功能说明：**
- 管理用户WebSocket会话
- 处理连接建立、消息接收、连接关闭
- 提供订单状态推送功能
- 支持广播消息
- 提供连接数统计

### 3. WebSocket服务 (WebSocketService.java & WebSocketServiceImpl.java)

```java
public interface WebSocketService {
    void pushOrderStatusUpdate(Long userId, Long orderId, String orderNo, 
                              String oldStatus, String newStatus, String updateReason);
    int getActiveConnectionCount();
}

@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Autowired
    private OrderStatusWebSocketHandler orderStatusWebSocketHandler;
    
    @Override
    public void pushOrderStatusUpdate(Long userId, Long orderId, String orderNo, 
                                    String oldStatus, String newStatus, String updateReason) {
        // 创建订单状态更新消息
        OrderStatusUpdateMessage message = new OrderStatusUpdateMessage(
            orderId, orderNo, oldStatus, newStatus, 
            TimeUtils.formatDateTime(TimeUtils.now()), updateReason
        );
        
        // 推送给指定用户
        orderStatusWebSocketHandler.pushOrderStatusUpdate(userId, message);
    }
}
```

**功能说明：**
- 封装WebSocket业务逻辑
- 提供简化的API接口
- 处理消息格式化和时间戳

### 4. 测试控制器 (WebSocketTestController.java)

```java
@RestController
@RequestMapping("/api/test/websocket")
public class WebSocketTestController {
    
    @PostMapping("/push-order-status")
    public String testPushOrderStatus(@RequestParam Long userId, 
                                    @RequestParam Long orderId, ...);
    
    @GetMapping("/connection-count")
    public String getConnectionCount();
    
    @GetMapping("/health")
    public String healthCheck();
}
```

**功能说明：**
- 提供WebSocket功能测试接口
- 支持手动触发订单状态推送
- 提供健康检查和连接数查询

## 前端实现

### 1. WebSocket管理器 (websocket.js)

```javascript
class WebSocketManager {
    constructor() {
        this.ws = null;
        this.userId = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
    }
    
    // 核心方法
    connect(userId);
    disconnect();
    send(message);
    
    // 事件处理
    onOpen(event);
    onMessage(event);
    onClose(event);
    onError(event);
    
    // 重连机制
    reconnect();
    startHeartbeat();
    stopHeartbeat();
}
```

**功能说明：**
- 管理WebSocket连接生命周期
- 实现自动重连机制
- 提供心跳检测
- 处理消息收发

### 2. Vue组件集成

```javascript
// 在Vue组件中使用
import webSocketManager from '@/utils/websocket'

export default {
    mounted() {
        // 连接WebSocket
        const userId = this.$store.getters.userId;
        webSocketManager.connect(userId);
        
        // 监听订单状态更新
        window.addEventListener('orderStatusUpdate', this.handleOrderStatusUpdate);
    },
    
    beforeUnmount() {
        // 清理WebSocket连接
        webSocketManager.disconnect();
        window.removeEventListener('orderStatusUpdate', this.handleOrderStatusUpdate);
    },
    
    methods: {
        handleOrderStatusUpdate(event) {
            const { orderId, newStatus } = event.detail;
            // 更新本地订单状态
            this.updateLocalOrderStatus(orderId, newStatus);
        }
    }
}
```

## 消息格式

### 1. WebSocket消息格式

```json
{
    "type": "message_type",
    "data": "message_data",
    "message": "human_readable_message"
}
```

### 2. 订单状态更新消息

```json
{
    "type": "order_status_update",
    "data": {
        "orderId": 1001,
        "orderNo": "ORDER-2025-001",
        "oldStatus": "pending",
        "newStatus": "confirmed",
        "updateTime": "2025-11-02 11:20:00",
        "updateReason": "管理员确认订单"
    },
    "message": "订单状态已更新"
}
```

### 3. 心跳消息

```json
// 客户端发送
"ping"

// 服务端响应
{
    "type": "pong",
    "data": "pong",
    "message": "心跳响应"
}
```

## API接口

### 1. WebSocket连接

**连接地址：** `ws://localhost:8080/ws/order-status?userId={userId}`

**参数说明：**
- `userId`: 用户ID，必填

**连接示例：**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/order-status?userId=1');
```

### 2. 测试接口

#### 推送订单状态更新
```http
POST /api/test/websocket/push-order-status
?userId=1&orderId=1001&orderNo=ORDER-2025-001
&oldStatus=pending&newStatus=confirmed&reason=测试更新
```

#### 获取连接数
```http
GET /api/test/websocket/connection-count
```

#### 健康检查
```http
GET /api/test/websocket/health
```

## 部署配置

### 1. Maven依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. 应用配置

```yaml
# application.yml
server:
  port: 8080

spring:
  websocket:
    # WebSocket相关配置
    max-text-message-buffer-size: 8192
    max-binary-message-buffer-size: 8192
```

## 测试指南

### 1. 功能测试

1. **连接测试**
   - 打开测试页面：`http://localhost:3000/test-websocket-api.html`
   - 输入用户ID，点击"连接WebSocket"
   - 观察连接状态变化

2. **心跳测试**
   - 连接成功后，点击"发送心跳"
   - 观察是否收到"pong"响应

3. **订单状态推送测试**
   - 填写订单信息
   - 点击"推送订单状态更新"
   - 观察WebSocket是否实时接收到状态更新

### 2. 性能测试

1. **并发连接测试**
   - 使用多个浏览器标签页同时连接
   - 检查连接数统计是否正确

2. **消息推送测试**
   - 向多个用户同时推送消息
   - 检查消息是否准确送达

### 3. 异常测试

1. **网络断开测试**
   - 断开网络连接
   - 观察重连机制是否正常工作

2. **服务重启测试**
   - 重启后端服务
   - 检查客户端是否能自动重连

## 监控和日志

### 1. 日志配置

```java
// 在各个组件中使用Logger
private static final Logger log = LoggerFactory.getLogger(ClassName.class);

// 关键事件日志
log.info("WebSocket连接建立: {}", session.getId());
log.info("推送订单状态更新: userId={}, orderId={}", userId, orderId);
log.error("WebSocket传输错误", exception);
```

### 2. 监控指标

- 当前活跃连接数
- 消息发送成功率
- 连接建立/断开频率
- 错误发生率

## 故障排除

### 1. 常见问题

**问题1：WebSocket连接失败**
- 检查后端服务是否启动
- 检查端口是否被占用
- 检查防火墙设置

**问题2：消息推送失败**
- 检查用户ID是否正确
- 检查WebSocket连接状态
- 查看后端日志错误信息

**问题3：自动重连不工作**
- 检查前端重连逻辑
- 检查网络连接状态
- 查看浏览器控制台错误

### 2. 调试技巧

1. **启用详细日志**
   ```yaml
   logging:
     level:
       com.carwash.websocket: DEBUG
   ```

2. **使用浏览器开发者工具**
   - Network标签查看WebSocket连接
   - Console标签查看JavaScript错误

3. **使用测试工具**
   - 使用提供的测试页面进行功能验证
   - 使用WebSocket客户端工具进行连接测试

## 未来优化

### 1. 功能扩展

- [ ] 支持消息持久化
- [ ] 支持消息确认机制
- [ ] 支持分组广播
- [ ] 支持消息优先级

### 2. 性能优化

- [ ] 实现连接池管理
- [ ] 优化消息序列化
- [ ] 实现负载均衡
- [ ] 添加缓存机制

### 3. 安全增强

- [ ] 添加身份验证
- [ ] 实现消息加密
- [ ] 添加访问控制
- [ ] 实现防重放攻击

---

**文档版本：** 1.0  
**最后更新：** 2025-11-02  
**维护人员：** 开发团队