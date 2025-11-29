/**
 * 离线状态缓存管理器
 * 使用IndexedDB存储订单数据，支持离线访问
 */

class OfflineStorageManager {
  constructor() {
    this.dbName = "CarWashDB";
    this.dbVersion = 1;
    this.storeName = "orders";
    this.db = null;
  }

  /**
   * 初始化数据库
   */
  async initDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.dbVersion);

      request.onerror = () => {
        console.error("❌ 打开IndexedDB失败:", request.error);
        reject(request.error);
      };

      request.onsuccess = () => {
        this.db = request.result;
        console.log("✅ IndexedDB初始化成功");
        resolve(this.db);
      };

      request.onupgradeneeded = (event) => {
        const db = event.target.result;

        // 创建订单存储对象
        if (!db.objectStoreNames.contains(this.storeName)) {
          const objectStore = db.createObjectStore(this.storeName, {
            keyPath: "id",
          });

          // 创建索引
          objectStore.createIndex("userId", "userId", { unique: false });
          objectStore.createIndex("status", "status", { unique: false });
          objectStore.createIndex("updatedAt", "updatedAt", { unique: false });

          console.log("📦 创建订单存储对象和索引");
        }
      };
    });
  }

  /**
   * 确保数据库已初始化
   */
  async ensureDB() {
    if (!this.db) {
      await this.initDB();
    }
  }

  /**
   * 保存单个订单
   */
  async saveOrder(order) {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readwrite");
        const objectStore = transaction.objectStore(this.storeName);

        // 添加缓存时间戳
        const orderWithTimestamp = {
          ...order,
          cachedAt: new Date().toISOString(),
        };

        const request = objectStore.put(orderWithTimestamp);

        request.onsuccess = () => {
          console.log("✅ 订单已保存到缓存:", order.id);
          resolve(true);
        };

        request.onerror = () => {
          console.error("❌ 保存订单失败:", request.error);
          reject(request.error);
        };
      });
    } catch (error) {
      console.error("❌ saveOrder异常:", error);
      return false;
    }
  }

  /**
   * 批量保存订单
   */
  async saveOrders(orders) {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readwrite");
        const objectStore = transaction.objectStore(this.storeName);

        const timestamp = new Date().toISOString();

        orders.forEach((order) => {
          const orderWithTimestamp = {
            ...order,
            cachedAt: timestamp,
          };
          objectStore.put(orderWithTimestamp);
        });

        transaction.oncomplete = () => {
          console.log(`✅ 批量保存${orders.length}个订单到缓存`);
          resolve(true);
        };

        transaction.onerror = () => {
          console.error("❌ 批量保存订单失败:", transaction.error);
          reject(transaction.error);
        };
      });
    } catch (error) {
      console.error("❌ saveOrders异常:", error);
      return false;
    }
  }

  /**
   * 获取用户的所有订单
   */
  async getUserOrders(userId) {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readonly");
        const objectStore = transaction.objectStore(this.storeName);
        const index = objectStore.index("userId");

        const request = index.getAll(userId);

        request.onsuccess = () => {
          const orders = request.result || [];
          console.log(`📦 从缓存加载${orders.length}个订单，用户ID:`, userId);
          resolve(orders);
        };

        request.onerror = () => {
          console.error("❌ 获取订单失败:", request.error);
          reject(request.error);
        };
      });
    } catch (error) {
      console.error("❌ getUserOrders异常:", error);
      return [];
    }
  }

  /**
   * 获取单个订单
   */
  async getOrder(orderId) {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readonly");
        const objectStore = transaction.objectStore(this.storeName);

        const request = objectStore.get(orderId);

        request.onsuccess = () => {
          resolve(request.result || null);
        };

        request.onerror = () => {
          console.error("❌ 获取订单失败:", request.error);
          reject(request.error);
        };
      });
    } catch (error) {
      console.error("❌ getOrder异常:", error);
      return null;
    }
  }

  /**
   * 删除订单
   */
  async deleteOrder(orderId) {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readwrite");
        const objectStore = transaction.objectStore(this.storeName);

        const request = objectStore.delete(orderId);

        request.onsuccess = () => {
          console.log("✅ 订单已从缓存删除:", orderId);
          resolve(true);
        };

        request.onerror = () => {
          console.error("❌ 删除订单失败:", request.error);
          reject(request.error);
        };
      });
    } catch (error) {
      console.error("❌ deleteOrder异常:", error);
      return false;
    }
  }

  /**
   * 清空所有订单缓存
   */
  async clearAllOrders() {
    try {
      await this.ensureDB();

      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([this.storeName], "readwrite");
        const objectStore = transaction.objectStore(this.storeName);

        const request = objectStore.clear();

        request.onsuccess = () => {
          console.log("🗑️ 已清空所有订单缓存");
          resolve(true);
        };

        request.onerror = () => {
          console.error("❌ 清空缓存失败:", request.error);
          reject(request.error);
        };
      });
    } catch (error) {
      console.error("❌ clearAllOrders异常:", error);
      return false;
    }
  }

  /**
   * 更新订单状态
   */
  async updateOrderStatus(orderId, newStatus) {
    try {
      const order = await this.getOrder(orderId);

      if (!order) {
        console.warn("⚠️ 订单不存在于缓存中:", orderId);
        return false;
      }

      order.status = newStatus;
      order.updatedAt = new Date().toISOString();

      return await this.saveOrder(order);
    } catch (error) {
      console.error("❌ updateOrderStatus异常:", error);
      return false;
    }
  }

  /**
   * 检查是否有缓存数据
   */
  async hasCachedData(userId) {
    try {
      const orders = await this.getUserOrders(userId);
      return orders.length > 0;
    } catch (error) {
      console.error("❌ hasCachedData异常:", error);
      return false;
    }
  }
}

// 创建全局实例
const offlineStorage = new OfflineStorageManager();

export default offlineStorage;

/**
 * Composable: 使用离线存储
 */
export function useOfflineStorage() {
  /**
   * 同步数据到缓存
   */
  const syncToCache = async (orders) => {
    return await offlineStorage.saveOrders(orders);
  };

  /**
   * 从缓存加载数据
   */
  const loadFromCache = async (userId) => {
    return await offlineStorage.getUserOrders(userId);
  };

  /**
   * 更新缓存中的订单状态
   */
  const updateCachedOrderStatus = async (orderId, newStatus) => {
    return await offlineStorage.updateOrderStatus(orderId, newStatus);
  };

  /**
   * 清空缓存
   */
  const clearCache = async () => {
    return await offlineStorage.clearAllOrders();
  };

  /**
   * 检查是否有缓存
   */
  const hasCache = async (userId) => {
    return await offlineStorage.hasCachedData(userId);
  };

  return {
    syncToCache,
    loadFromCache,
    updateCachedOrderStatus,
    clearCache,
    hasCache,
  };
}

