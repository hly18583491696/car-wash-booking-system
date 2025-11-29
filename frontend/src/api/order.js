import realApi from "./realApi.js";
import mockOrderApi from "./mockOrderApi.js";

// 测试模式开关 - 设置为false使用真实后端API
let USE_MOCK_API = false;

// 订单相关API
export const orderApi = {
  // 获取订单列表
  async getOrderList() {
    try {
      if (USE_MOCK_API) {
        console.log("🧪 使用模拟API获取订单列表");
        return await mockOrderApi.getOrderList();
      }
      const response = await realApi.getOrderList();
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 基础订单查询（用于兼容非分页返回）
  async getOrders(params = {}) {
    try {
      if (USE_MOCK_API) {
        console.log("🧪 使用模拟API获取订单（基础查询）");
        return await mockOrderApi.getOrders(params);
      }
      const resp = await realApi.getOrderList(params);
      // 兼容返回结构：若后端返回分页结构，则扁平化为数组
      if (resp && resp.data) {
        const d = resp.data;
        if (Array.isArray(d)) {
          return resp;
        }
        if (Array.isArray(d.content)) {
          return { ...resp, data: d.content };
        }
        if (Array.isArray(d.bookings)) {
          return { ...resp, data: d.bookings };
        }
      }
      return resp;
    } catch (error) {
      throw error;
    }
  },

  // 分页订单查询（AdminScript 期望的结构：content/totalElements/totalPages）
  async getOrdersPaginated({ page = 1, size = 20, status, search } = {}) {
    try {
      if (USE_MOCK_API) {
        console.log("🧪 使用模拟API获取订单（分页查询）");
        return await mockOrderApi.getOrdersPaginated({
          page,
          size,
          status,
          search,
        });
      }

      // 兼容不同参数命名：同时传递 current/page 与 keyword/search
      const params = {
        current: page,
        page,
        size,
        status,
        keyword: search,
        search,
      };
      const resp = await realApi.getOrderList(params);

      // 若后端直接返回数组，则包裹为分页结构
      if (resp && resp.data) {
        const d = resp.data;
        if (Array.isArray(d)) {
          const total = d.length;
          const totalPages = Math.max(1, Math.ceil(total / size));
          const start = (page - 1) * size;
          const end = start + size;
          const pageContent = d.slice(start, end);
          return {
            ...resp,
            data: { content: pageContent, totalElements: total, totalPages },
          };
        }
        // 若已有分页结构，直接返回
        if (Array.isArray(d.content)) {
          return resp;
        }
        // 若是 bookings 字段，转为分页结构
        if (Array.isArray(d.bookings)) {
          const total = d.bookings.length;
          const totalPages = Math.max(1, Math.ceil(total / size));
          const start = (page - 1) * size;
          const end = start + size;
          const pageContent = d.bookings.slice(start, end);
          return {
            ...resp,
            data: { content: pageContent, totalElements: total, totalPages },
          };
        }
      }
      return resp;
    } catch (error) {
      throw error;
    }
  },

  // 获取订单详情
  async getOrderById(id) {
    try {
      const response = await realApi.getOrderById(id);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 更新订单状态
  async updateOrderStatus(orderId, status) {
    try {
      if (USE_MOCK_API) {
        console.log("🧪 使用模拟API更新订单状态");
        return await mockOrderApi.updateOrderStatus(orderId, status);
      }
      const response = await realApi.updateOrderStatus(orderId, status);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 更新预约状态（别名方法，为了兼容）
  async updateBookingStatus(bookingId, status) {
    try {
      if (USE_MOCK_API) {
        console.log("🧪 使用模拟API更新预约状态");
        return await mockOrderApi.updateBookingStatus(bookingId, status);
      }
      const response = await realApi.updateOrderStatus(bookingId, status);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 获取用户订单列表
  async getUserOrders(userId) {
    try {
      const response = await realApi.getUserOrders(userId);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 根据订单号获取订单详情
  async getOrderByNo(orderNo) {
    try {
      // 直接调用真实API按订单号查询
      const response = await realApi.getBookingByOrderNo(orderNo);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 删除订单
  async deleteOrder(orderId) {
    try {
      console.log("🗑️ 删除订单，ID:", orderId);
      const response = await realApi.deleteOrder(orderId);
      console.log("✅ 订单删除成功");
      return response;
    } catch (error) {
      console.error("❌ 删除订单失败:", error);
      throw error;
    }
  },

  // 永久删除订单（硬删除）
  async permanentlyDeleteOrder(orderId) {
    try {
      console.log("🗑️ 永久删除订单，ID:", orderId);
      const response = await realApi.permanentlyDeleteOrder(orderId);
      console.log("✅ 订单永久删除成功");
      return response;
    } catch (error) {
      console.error("❌ 永久删除订单失败:", error);
      throw error;
    }
  },

  // 测试工具方法
  setMockMode(enabled) {
    USE_MOCK_API = enabled;
    console.log(`🔧 模拟API模式: ${enabled ? "开启" : "关闭"}`);
  },

  isMockMode() {
    return USE_MOCK_API;
  },

  resetMockData() {
    if (USE_MOCK_API) {
      mockOrderApi.resetTestData();
      console.log("🔄 模拟数据已重置");
    }
  },

  getMockData() {
    if (USE_MOCK_API) {
      return mockOrderApi.getCurrentData();
    }
    return null;
  },
};

export default orderApi;
