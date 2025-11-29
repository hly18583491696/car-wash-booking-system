/**
 * 订单状态轮询备用方案
 * 当WebSocket断开时自动切换到轮询模式
 */

import { ref } from "vue";
import { orderApi } from "@/api/order";
import { ElMessage } from "element-plus";

class OrderPollingManager {
  constructor() {
    this.pollingInterval = null;
    this.isPolling = false;
    this.pollingDelay = 5000; // 轮询间隔5秒
    this.callbacks = new Set();
    this.lastOrderData = new Map(); // 存储上次的订单数据，用于对比
  }

  /**
   * 开始轮询
   */
  startPolling(userId) {
    if (this.isPolling) {
      console.log("⚠️ 轮询已在进行中");
      return;
    }

    console.log("🔄 启动订单状态轮询，用户ID:", userId);
    this.isPolling = true;

    // 立即执行一次
    this.poll(userId);

    // 设置定时轮询
    this.pollingInterval = setInterval(() => {
      this.poll(userId);
    }, this.pollingDelay);
  }

  /**
   * 停止轮询
   */
  stopPolling() {
    if (!this.isPolling) {
      return;
    }

    console.log("⏹️ 停止订单状态轮询");
    this.isPolling = false;

    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  /**
   * 执行一次轮询
   */
  async poll(userId) {
    try {
      const response = await orderApi.getUserOrders(userId);

      if (response.success && response.data) {
        const orders = response.data;

        // 检查订单状态变化
        orders.forEach((order) => {
          const lastOrder = this.lastOrderData.get(order.id);

          if (lastOrder && lastOrder.status !== order.status) {
            console.log(
              `🔄 轮询检测到订单状态变化: ${order.id}, ${lastOrder.status} -> ${order.status}`,
            );

            // 通知所有监听器
            this.notifyCallbacks({
              orderId: order.id,
              orderNo: order.orderNo,
              oldStatus: lastOrder.status,
              newStatus: order.status,
              updateTime: order.updatedAt,
            });
          }

          // 更新缓存
          this.lastOrderData.set(order.id, {
            id: order.id,
            status: order.status,
            updatedAt: order.updatedAt,
          });
        });
      }
    } catch (error) {
      console.error("❌ 轮询订单状态失败:", error);
      // 轮询失败不需要提示用户，避免频繁打扰
    }
  }

  /**
   * 注册状态变化回调
   */
  onStatusChange(callback) {
    this.callbacks.add(callback);

    // 返回取消注册的函数
    return () => {
      this.callbacks.delete(callback);
    };
  }

  /**
   * 通知所有回调函数
   */
  notifyCallbacks(data) {
    this.callbacks.forEach((callback) => {
      try {
        callback(data);
      } catch (error) {
        console.error("❌ 轮询回调执行失败:", error);
      }
    });
  }

  /**
   * 设置轮询间隔
   */
  setPollingDelay(delay) {
    this.pollingDelay = delay;

    // 如果正在轮询，重新启动以应用新的间隔
    if (this.isPolling && this.pollingInterval) {
      clearInterval(this.pollingInterval);
      const userId = this.getCurrentUserId();
      if (userId) {
        this.pollingInterval = setInterval(() => {
          this.poll(userId);
        }, this.pollingDelay);
      }
    }
  }

  /**
   * 清空缓存数据
   */
  clearCache() {
    this.lastOrderData.clear();
  }

  /**
   * 获取当前用户ID（需要从store获取）
   */
  getCurrentUserId() {
    // 这里应该从用户store获取，为了避免循环依赖，暂时返回null
    return null;
  }
}

// 创建全局轮询管理器实例
const orderPollingManager = new OrderPollingManager();

export default orderPollingManager;

/**
 * Composable: 使用订单轮询
 */
export function useOrderPolling() {
  const isPolling = ref(false);

  /**
   * 开始轮询
   */
  const startPolling = (userId) => {
    orderPollingManager.startPolling(userId);
    isPolling.value = true;
  };

  /**
   * 停止轮询
   */
  const stopPolling = () => {
    orderPollingManager.stopPolling();
    isPolling.value = false;
  };

  /**
   * 监听状态变化
   */
  const onStatusChange = (callback) => {
    return orderPollingManager.onStatusChange(callback);
  };

  /**
   * 设置轮询间隔
   */
  const setPollingDelay = (delay) => {
    orderPollingManager.setPollingDelay(delay);
  };

  return {
    isPolling,
    startPolling,
    stopPolling,
    onStatusChange,
    setPollingDelay,
  };
}
