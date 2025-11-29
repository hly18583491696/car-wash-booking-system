import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import orderPollingManager from "@/utils/orderPolling";

class WebSocketManager {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.heartbeatInterval = 30000;
    this.isConnecting = false;
    this.messageHandlers = new Map();
    this.connectionCallbacks = [];
    this.fallbackToPolling = false; // 是否已切换到轮询模式
  }

  // 连接WebSocket
  connect() {
    if (
      this.isConnecting ||
      (this.ws && this.ws.readyState === WebSocket.OPEN)
    ) {
      return;
    }

    this.isConnecting = true;
    const userStore = useUserStore();
    const userId = userStore.userInfo?.id;
    const token = userStore.token;

    // 后端要求使用 userId 作为连接参数；无登录信息则不连接
    if (!userId) {
      console.warn("WebSocket连接失败：用户未登录或缺少用户ID");
      this.isConnecting = false;
      return;
    }

    try {
      // 构建WebSocket URL：按后端要求使用 userId；token作为可选参数便于后续扩展
      const wsUrl = `ws://localhost:8080/ws/order-status?userId=${encodeURIComponent(userId)}${token ? `&token=${encodeURIComponent(token)}` : ""}`;
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log("WebSocket连接成功");
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.startHeartbeat();

        // 如果正在使用轮询模式，关闭它
        if (this.fallbackToPolling) {
          this.disablePollingFallback();
          ElMessage({
            type: "success",
            message: "实时连接已恢复",
            duration: 3000,
          });
        }

        // 通知连接成功的回调
        this.connectionCallbacks.forEach((callback) => {
          try {
            callback(true);
          } catch (error) {
            console.error("连接回调执行失败:", error);
          }
        });
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleMessage(message);
        } catch (error) {
          console.error("WebSocket消息解析失败:", error, event.data);
        }
      };

      this.ws.onclose = (event) => {
        console.log("WebSocket连接关闭:", event.code, event.reason);
        this.isConnecting = false;
        this.stopHeartbeat();

        // 通知连接断开的回调
        this.connectionCallbacks.forEach((callback) => {
          try {
            callback(false);
          } catch (error) {
            console.error("连接回调执行失败:", error);
          }
        });

        // 如果不是主动关闭，尝试重连
        if (
          event.code !== 1000 &&
          this.reconnectAttempts < this.maxReconnectAttempts
        ) {
          this.scheduleReconnect();
        } else if (
          event.code !== 1000 &&
          this.reconnectAttempts >= this.maxReconnectAttempts
        ) {
          // 重连次数达到上限，切换到轮询模式
          this.enablePollingFallback();
        }
      };

      this.ws.onerror = (error) => {
        console.error("WebSocket连接错误:", error);
        this.isConnecting = false;
      };
    } catch (error) {
      console.error("WebSocket连接创建失败:", error);
      this.isConnecting = false;
    }
  }

  // 断开连接
  disconnect() {
    this.stopHeartbeat();
    this.clearReconnectTimer();
    this.disablePollingFallback(); // 停止轮询

    if (this.ws) {
      this.ws.close(1000, "主动断开连接");
      this.ws = null;
    }
  }

  // 发送消息
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message));
        return true;
      } catch (error) {
        console.error("WebSocket发送消息失败:", error);
        return false;
      }
    } else {
      console.warn("WebSocket未连接，无法发送消息");
      return false;
    }
  }

  // 处理接收到的消息
  handleMessage(message) {
    console.log("收到WebSocket消息:", message);

    switch (message.type) {
      case "heartbeat":
        // 历史心跳类型，保持兼容
        break;
      case "pong":
        // 当前心跳响应类型
        break;

      case "order_status_update":
        this.handleOrderStatusUpdate(message.data);
        break;

      default:
        console.log("未知消息类型:", message.type);
    }

    // 调用注册的消息处理器
    const handlers = this.messageHandlers.get(message.type) || [];
    handlers.forEach((handler) => {
      try {
        handler(message.data);
      } catch (error) {
        console.error("消息处理器执行失败:", error);
      }
    });
  }

  // 处理订单状态更新
  handleOrderStatusUpdate(data) {
    const { orderId, newStatus, oldStatus } = data;

    // 显示状态更新通知
    const statusText = this.getStatusText(newStatus);
    ElMessage({
      type: "info",
      message: `订单 ${orderId} 状态已更新为：${statusText}`,
      duration: 3000,
    });

    // 触发全局事件，让其他组件可以监听
    window.dispatchEvent(
      new CustomEvent("orderStatusUpdate", {
        detail: data,
      }),
    );
  }

  // 获取状态文本
  getStatusText(status) {
    const statusMap = {
      pending: "待确认",
      confirmed: "已确认",
      in_progress: "服务中",
      completed: "已完成",
      cancelled: "已取消",
    };
    return statusMap[status] || status;
  }

  // 注册消息处理器
  onMessage(type, handler) {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, []);
    }
    this.messageHandlers.get(type).push(handler);

    // 返回取消注册的函数
    return () => {
      const handlers = this.messageHandlers.get(type);
      if (handlers) {
        const index = handlers.indexOf(handler);
        if (index > -1) {
          handlers.splice(index, 1);
        }
      }
    };
  }

  // 注册连接状态回调
  onConnectionChange(callback) {
    this.connectionCallbacks.push(callback);

    // 返回取消注册的函数
    return () => {
      const index = this.connectionCallbacks.indexOf(callback);
      if (index > -1) {
        this.connectionCallbacks.splice(index, 1);
      }
    };
  }

  // 开始心跳
  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      // 与后端保持一致，发送纯文本 'ping'
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send("ping");
        } catch (e) {
          console.error("发送心跳失败:", e);
        }
      }
    }, this.heartbeatInterval);
  }

  // 停止心跳
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  // 安排重连
  scheduleReconnect() {
    this.clearReconnectTimer();
    this.reconnectAttempts++;

    console.log(
      `WebSocket重连尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`,
    );

    this.reconnectTimer = setTimeout(() => {
      this.connect();
    }, this.reconnectInterval);
  }

  // 清除重连定时器
  clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  // 获取连接状态
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN;
  }

  // 获取连接状态文本
  getConnectionStatus() {
    if (!this.ws) return "disconnected";

    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return "connecting";
      case WebSocket.OPEN:
        return "connected";
      case WebSocket.CLOSING:
        return "closing";
      case WebSocket.CLOSED:
        return "disconnected";
      default:
        return "unknown";
    }
  }

  /**
   * 启用轮询备用方案
   */
  enablePollingFallback() {
    if (this.fallbackToPolling) {
      return;
    }

    console.log("⚠️ WebSocket连接失败，切换到轮询模式");
    this.fallbackToPolling = true;

    const userStore = useUserStore();
    const userId = userStore.userInfo?.id;

    if (userId) {
      orderPollingManager.startPolling(userId);

      // 监听轮询的状态变化，转发给WebSocket的监听器
      orderPollingManager.onStatusChange((data) => {
        this.handleMessage({
          type: "order_status_update",
          data: data,
        });
      });

      ElMessage({
        type: "warning",
        message: "实时连接中断，已切换到轮询模式",
        duration: 5000,
      });
    }
  }

  /**
   * 禁用轮询备用方案
   */
  disablePollingFallback() {
    if (!this.fallbackToPolling) {
      return;
    }

    console.log("✅ 关闭轮询模式");
    this.fallbackToPolling = false;
    orderPollingManager.stopPolling();
  }

  /**
   * 检查是否已切换到轮询模式
   */
  isUsingPolling() {
    return this.fallbackToPolling;
  }
}

// 创建全局WebSocket管理器实例
const webSocketManager = new WebSocketManager();

export default webSocketManager;
