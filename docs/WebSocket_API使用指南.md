# WebSocket API使用指南

## 快速开始

### 1. 连接WebSocket

```javascript
// 连接到WebSocket服务
const userId = 1; // 用户ID
const ws = new WebSocket(`ws://localhost:8080/ws/order-status?userId=${userId}`);

// 监听连接事件
ws.onopen = function(event) {
    console.log('WebSocket连接已建立');
};

ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
    
    // 处理订单状态更新
    if (message.type === 'order_status_update') {
        handleOrderStatusUpdate(message.data);
    }
};

ws.onclose = function(event) {
    console.log('WebSocket连接已关闭');
};

ws.onerror = function(error) {
    console.error('WebSocket错误:', error);
};
```

### 2. 发送心跳

```javascript
// 发送心跳保持连接
function sendHeartbeat() {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send('ping');
    }
}

// 每30秒发送一次心跳
setInterval(sendHeartbeat, 30000);
```

### 3. 处理订单状态更新

```javascript
function handleOrderStatusUpdate(data) {
    const { orderId, orderNo, oldStatus, newStatus, updateTime, updateReason } = data;
    
    console.log(`订单 ${orderNo} 状态从 ${oldStatus} 更新为 ${newStatus}`);
    console.log(`更新时间: ${updateTime}`);
    console.log(`更新原因: ${updateReason}`);
    
    // 更新UI显示
    updateOrderStatusInUI(orderId, newStatus);
    
    // 显示通知
    showNotification(`订单状态已更新为: ${getStatusText(newStatus)}`);
}
```

## API接口

### WebSocket连接

**连接地址：** `ws://localhost:8080/ws/order-status`

**必需参数：**
- `userId`: 用户ID (数字)

**连接示例：**
```
ws://localhost:8080/ws/order-status?userId=1
```

### 消息格式

#### 1. 订单状态更新消息

**接收消息格式：**
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

#### 2. 连接确认消息

**接收消息格式：**
```json
{
    "type": "connection",
    "data": "connected",
    "message": "WebSocket连接成功"
}
```

#### 3. 心跳消息

**发送：** `"ping"`

**接收：**
```json
{
    "type": "pong",
    "data": "pong",
    "message": "心跳响应"
}
```

### 订单状态值

| 状态值 | 中文描述 | 说明 |
|--------|----------|------|
| `pending` | 待处理 | 订单已提交，等待确认 |
| `confirmed` | 已确认 | 订单已确认，等待服务 |
| `in_progress` | 进行中 | 正在提供服务 |
| `completed` | 已完成 | 服务已完成 |
| `cancelled` | 已取消 | 订单已取消 |

## 测试接口

### 1. 推送订单状态更新

**接口：** `POST /api/test/websocket/push-order-status`

**参数：**
- `userId`: 目标用户ID
- `orderId`: 订单ID
- `orderNo`: 订单号
- `oldStatus`: 原状态
- `newStatus`: 新状态
- `reason`: 更新原因 (可选)

**示例：**
```bash
curl -X POST "http://localhost:8080/api/test/websocket/push-order-status?userId=1&orderId=1001&orderNo=ORDER-2025-001&oldStatus=pending&newStatus=confirmed&reason=测试更新"
```

### 2. 获取连接数

**接口：** `GET /api/test/websocket/connection-count`

**示例：**
```bash
curl "http://localhost:8080/api/test/websocket/connection-count"
```

### 3. 健康检查

**接口：** `GET /api/test/websocket/health`

**示例：**
```bash
curl "http://localhost:8080/api/test/websocket/health"
```

## Vue.js集成示例

### 1. 创建WebSocket服务

```javascript
// utils/websocket.js
class WebSocketService {
    constructor() {
        this.ws = null;
        this.userId = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
        this.heartbeatTimer = null;
    }
    
    connect(userId) {
        this.userId = userId;
        this.ws = new WebSocket(`ws://localhost:8080/ws/order-status?userId=${userId}`);
        
        this.ws.onopen = this.onOpen.bind(this);
        this.ws.onmessage = this.onMessage.bind(this);
        this.ws.onclose = this.onClose.bind(this);
        this.ws.onerror = this.onError.bind(this);
    }
    
    onOpen(event) {
        console.log('WebSocket连接已建立');
        this.reconnectAttempts = 0;
        this.startHeartbeat();
    }
    
    onMessage(event) {
        const message = JSON.parse(event.data);
        
        if (message.type === 'order_status_update') {
            // 触发全局事件
            window.dispatchEvent(new CustomEvent('orderStatusUpdate', {
                detail: message.data
            }));
        }
    }
    
    onClose(event) {
        console.log('WebSocket连接已关闭');
        this.stopHeartbeat();
        this.reconnect();
    }
    
    onError(error) {
        console.error('WebSocket错误:', error);
    }
    
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.ws.send('ping');
            }
        }, 30000);
    }
    
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }
    
    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect(this.userId);
            }, this.reconnectInterval);
        }
    }
    
    disconnect() {
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
}

export default new WebSocketService();
```

### 2. 在Vue组件中使用

```vue
<template>
    <div class="orders-page">
        <div v-if="connectionStatus" class="connection-status">
            WebSocket: {{ connectionStatus }}
        </div>
        
        <div v-for="order in orders" :key="order.id" class="order-item">
            <h3>订单号: {{ order.orderNo }}</h3>
            <p>状态: {{ getStatusText(order.status) }}</p>
            <p v-if="order.lastUpdate">最后更新: {{ order.lastUpdate }}</p>
        </div>
    </div>
</template>

<script>
import webSocketService from '@/utils/websocket';

export default {
    name: 'OrdersPage',
    data() {
        return {
            orders: [],
            connectionStatus: '未连接'
        };
    },
    
    mounted() {
        // 获取用户ID
        const userId = this.$store.getters.userId;
        
        // 连接WebSocket
        webSocketService.connect(userId);
        
        // 监听订单状态更新
        window.addEventListener('orderStatusUpdate', this.handleOrderStatusUpdate);
        
        // 监听连接状态变化
        this.updateConnectionStatus();
    },
    
    beforeUnmount() {
        // 清理WebSocket连接
        webSocketService.disconnect();
        
        // 移除事件监听
        window.removeEventListener('orderStatusUpdate', this.handleOrderStatusUpdate);
    },
    
    methods: {
        handleOrderStatusUpdate(event) {
            const { orderId, newStatus, updateTime, updateReason } = event.detail;
            
            // 更新本地订单状态
            const order = this.orders.find(o => o.id === orderId);
            if (order) {
                order.status = newStatus;
                order.lastUpdate = updateTime;
                
                // 显示通知
                this.$message({
                    type: 'info',
                    message: `订单 ${order.orderNo} 状态已更新为: ${this.getStatusText(newStatus)}`,
                    duration: 3000
                });
            }
        },
        
        getStatusText(status) {
            const statusMap = {
                'pending': '待处理',
                'confirmed': '已确认',
                'in_progress': '进行中',
                'completed': '已完成',
                'cancelled': '已取消'
            };
            return statusMap[status] || status;
        },
        
        updateConnectionStatus() {
            // 定期检查连接状态
            setInterval(() => {
                if (webSocketService.ws) {
                    switch (webSocketService.ws.readyState) {
                        case WebSocket.CONNECTING:
                            this.connectionStatus = '连接中';
                            break;
                        case WebSocket.OPEN:
                            this.connectionStatus = '已连接';
                            break;
                        case WebSocket.CLOSING:
                            this.connectionStatus = '断开中';
                            break;
                        case WebSocket.CLOSED:
                            this.connectionStatus = '已断开';
                            break;
                        default:
                            this.connectionStatus = '未知';
                    }
                } else {
                    this.connectionStatus = '未连接';
                }
            }, 1000);
        }
    }
};
</script>

<style scoped>
.connection-status {
    padding: 10px;
    background-color: #f0f0f0;
    border-radius: 4px;
    margin-bottom: 20px;
    text-align: center;
    font-weight: bold;
}

.order-item {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 15px;
    background-color: #fff;
}
</style>
```

## React集成示例

### 1. 创建WebSocket Hook

```javascript
// hooks/useWebSocket.js
import { useEffect, useRef, useState } from 'react';

export const useWebSocket = (userId) => {
    const ws = useRef(null);
    const [connectionStatus, setConnectionStatus] = useState('Disconnected');
    const [lastMessage, setLastMessage] = useState(null);
    
    useEffect(() => {
        if (!userId) return;
        
        // 连接WebSocket
        ws.current = new WebSocket(`ws://localhost:8080/ws/order-status?userId=${userId}`);
        
        ws.current.onopen = () => {
            setConnectionStatus('Connected');
            console.log('WebSocket连接已建立');
        };
        
        ws.current.onmessage = (event) => {
            const message = JSON.parse(event.data);
            setLastMessage(message);
            
            if (message.type === 'order_status_update') {
                // 触发自定义事件
                window.dispatchEvent(new CustomEvent('orderStatusUpdate', {
                    detail: message.data
                }));
            }
        };
        
        ws.current.onclose = () => {
            setConnectionStatus('Disconnected');
            console.log('WebSocket连接已关闭');
        };
        
        ws.current.onerror = (error) => {
            setConnectionStatus('Error');
            console.error('WebSocket错误:', error);
        };
        
        // 清理函数
        return () => {
            if (ws.current) {
                ws.current.close();
            }
        };
    }, [userId]);
    
    const sendMessage = (message) => {
        if (ws.current && ws.current.readyState === WebSocket.OPEN) {
            ws.current.send(message);
        }
    };
    
    return {
        connectionStatus,
        lastMessage,
        sendMessage
    };
};
```

### 2. 在React组件中使用

```javascript
// components/OrderList.js
import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

const OrderList = ({ userId }) => {
    const [orders, setOrders] = useState([]);
    const { connectionStatus, sendMessage } = useWebSocket(userId);
    
    useEffect(() => {
        // 监听订单状态更新
        const handleOrderStatusUpdate = (event) => {
            const { orderId, newStatus, updateTime } = event.detail;
            
            setOrders(prevOrders => 
                prevOrders.map(order => 
                    order.id === orderId 
                        ? { ...order, status: newStatus, lastUpdate: updateTime }
                        : order
                )
            );
        };
        
        window.addEventListener('orderStatusUpdate', handleOrderStatusUpdate);
        
        return () => {
            window.removeEventListener('orderStatusUpdate', handleOrderStatusUpdate);
        };
    }, []);
    
    const sendHeartbeat = () => {
        sendMessage('ping');
    };
    
    return (
        <div className="order-list">
            <div className="connection-status">
                WebSocket状态: {connectionStatus}
                <button onClick={sendHeartbeat}>发送心跳</button>
            </div>
            
            {orders.map(order => (
                <div key={order.id} className="order-item">
                    <h3>订单号: {order.orderNo}</h3>
                    <p>状态: {getStatusText(order.status)}</p>
                    {order.lastUpdate && (
                        <p>最后更新: {order.lastUpdate}</p>
                    )}
                </div>
            ))}
        </div>
    );
};

const getStatusText = (status) => {
    const statusMap = {
        'pending': '待处理',
        'confirmed': '已确认',
        'in_progress': '进行中',
        'completed': '已完成',
        'cancelled': '已取消'
    };
    return statusMap[status] || status;
};

export default OrderList;
```

## 错误处理

### 1. 连接错误

```javascript
ws.onerror = function(error) {
    console.error('WebSocket连接错误:', error);
    
    // 显示错误提示
    showErrorMessage('WebSocket连接失败，请检查网络连接');
    
    // 尝试重连
    setTimeout(() => {
        reconnectWebSocket();
    }, 5000);
};
```

### 2. 消息解析错误

```javascript
ws.onmessage = function(event) {
    try {
        const message = JSON.parse(event.data);
        handleMessage(message);
    } catch (error) {
        console.error('消息解析错误:', error);
        console.log('原始消息:', event.data);
    }
};
```

### 3. 重连机制

```javascript
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;
const reconnectInterval = 3000;

function reconnectWebSocket() {
    if (reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++;
        console.log(`尝试重连 (${reconnectAttempts}/${maxReconnectAttempts})`);
        
        setTimeout(() => {
            connectWebSocket();
        }, reconnectInterval * reconnectAttempts);
    } else {
        console.error('重连失败，已达到最大重试次数');
        showErrorMessage('无法连接到服务器，请刷新页面重试');
    }
}
```

## 最佳实践

### 1. 连接管理

- 在用户登录后立即建立WebSocket连接
- 在页面卸载前主动关闭连接
- 实现自动重连机制
- 定期发送心跳保持连接

### 2. 消息处理

- 始终验证消息格式
- 使用try-catch处理JSON解析
- 为不同类型的消息实现不同的处理逻辑
- 记录重要的消息和错误

### 3. 用户体验

- 显示连接状态给用户
- 在连接断开时显示提示
- 实现消息通知功能
- 提供手动重连选项

### 4. 性能优化

- 避免频繁的DOM操作
- 使用防抖处理高频消息
- 合理设置心跳间隔
- 及时清理事件监听器

---

**文档版本：** 1.0  
**最后更新：** 2025-11-02  
**适用版本：** 汽车洗车服务预约系统 v1.0