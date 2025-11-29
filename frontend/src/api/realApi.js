import request from "./request.js";

// 真实API服务 - 连接后端
export const realApi = {
  // ==================== 认证相关 ====================

  // 用户登录
  async login(username, password) {
    return request.post("/auth/login", {
      username,
      password,
    });
  },

  // 用户注册
  async register(userInfo) {
    return request.post("/auth/register", userInfo);
  },

  // 检查用户名是否存在
  async checkUsername(username) {
    return request.get("/auth/check-username", {
      params: { username },
    });
  },

  // 检查手机号是否存在
  async checkPhone(phone) {
    return request.get("/auth/check-phone", {
      params: { phone },
    });
  },

  // 检查邮箱是否存在
  async checkEmail(email) {
    return request.get("/auth/check-email", {
      params: { email },
    });
  },

  // ==================== 用户相关 ====================

  // 获取用户信息
  async getUserInfo() {
    return request.get("/user/info");
  },

  // 更新用户信息
  async updateUserInfo(userInfo) {
    return request.put("/user/info", userInfo);
  },

  // 修改密码
  async changePassword(oldPassword, newPassword) {
    return request.post("/user/change-password", null, {
      params: { oldPassword, newPassword },
    });
  },

  // ==================== 服务相关 ====================

  // 获取服务列表
  async getServices(params = {}) {
    return request.get("/services/list", { params });
  },

  // 获取服务详情
  async getServiceDetail(id) {
    return request.get(`/services/${id}`);
  },

  // 按分类获取服务
  async getServicesByCategory(category) {
    return request.get(`/services/category/${category}`);
  },

  // 搜索服务
  async searchServices(keyword) {
    return request.get("/services/search", {
      params: { keyword },
    });
  },

  // 获取服务分类
  async getServiceCategories() {
    return request.get("/services/categories");
  },

  // ==================== 管理员相关 ====================

  // 获取所有服务（管理员）
  async getAllServices(params = {}) {
    return request.get("/services/admin/all", { params });
  },

  // 创建服务
  async createService(serviceData) {
    return request.post("/services", serviceData);
  },

  // 更新服务
  async updateService(id, serviceData) {
    return request.put(`/services/${id}`, serviceData);
  },

  // 删除服务
  async deleteService(id) {
    return request.delete(`/services/${id}`);
  },

  // 永久删除服务（管理员专用）
  async permanentlyDeleteService(id) {
    return request.delete(`/services/${id}/permanent`);
  },

  // 更新服务状态
  async updateServiceStatus(id, status) {
    return request.put(`/services/${id}/status`, null, {
      params: { status },
    });
  },

  // ==================== 统计相关 ====================

  // 获取统计概览
  async getStatisticsOverview() {
    return request.get("/statistics/overview");
  },

  // 获取今日统计
  async getTodayStatistics() {
    return request.get("/statistics/today");
  },

  // 获取预约趋势
  async getBookingTrend(period) {
    return request.get("/statistics/booking-trend", {
      params: { period },
    });
  },

  // 获取服务分布
  async getServiceDistribution() {
    return request.get("/statistics/service-distribution");
  },

  // 获取收入统计
  async getRevenueStatistics(type) {
    return request.get("/statistics/revenue", {
      params: { type },
    });
  },

  // 获取时段热力图
  async getTimeSlotHeatmap() {
    return request.get("/statistics/time-slot-heatmap");
  },

  // 获取客户满意度
  async getCustomerSatisfaction() {
    return request.get("/statistics/customer-satisfaction");
  },

  // ==================== 订单相关 ====================

  // 获取订单列表（管理员）
  async getOrderList(params = {}) {
    return request.get("/bookings/admin/all", { params });
  },

  // 获取订单详情
  async getOrderById(id) {
    return request.get(`/bookings/${id}`);
  },

  // 根据订单号获取订单详情
  async getBookingByOrderNo(orderNo) {
    return request.get(`/bookings/order/${encodeURIComponent(orderNo)}`);
  },

  // 更新订单状态
  async updateOrderStatus(orderId, status) {
    return request.put(`/bookings/${orderId}/status`, null, {
      params: { status },
    });
  },

  // 获取用户订单
  async getUserOrders(userId) {
    const normalizedUserId = userId != null ? String(userId).trim() : "";
    console.log("🔍 realApi.getUserOrders被调用，用户ID:", normalizedUserId);
    if (!normalizedUserId) {
      const err = new Error("INVALID_PARAMETERS: userId 缺失或无效");
      console.error("❌ 参数错误:", err.message);
      throw err;
    }

    const url = `/bookings/user/${encodeURIComponent(normalizedUserId)}`;
    console.log("📡 准备发送请求到:", url);

    try {
      const response = await request.get(url, { timeout: 5000 });
      console.log("✅ realApi.getUserOrders请求成功，响应:", response);
      return response;
    } catch (error) {
      console.error("❌ realApi.getUserOrders请求失败:", error);
      console.error("❌ 错误详情:", {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: error.config?.url,
      });
      // 提供更清晰的错误信息
      const status = error.response?.status;
      if (status === 401) {
        error.message = "未授权，请登录后重试";
      } else if (status === 404) {
        error.message = "用户订单不存在或路径错误";
      } else if (status === 500) {
        error.message = "服务器内部错误，请稍后重试";
      }
      throw error;
    }
  },

  // 取消订单
  async cancelOrder(orderId, reason) {
    return request.put(`/bookings/${orderId}/cancel`, null, {
      params: { reason },
    });
  },

  // 删除订单（管理员专用）
  async deleteOrder(orderId) {
    return request.delete(`/bookings/${orderId}`);
  },

  // 永久删除订单（管理员专用）
  async permanentlyDeleteOrder(orderId) {
    return request.delete(`/bookings/${orderId}/permanent`);
  },

  // 创建预约订单
  async createBooking(bookingData) {
    return request.post("/bookings", bookingData);
  },

  // ==================== 用户管理相关 ====================

  // 获取用户列表（管理员专用）
  async getUserList(params = {}) {
    return request.get("/user/admin/list", { params });
  },

  // 获取用户详情
  async getUserById(id) {
    return request.get(`/user/info`);
  },

  // 更新用户信息
  async updateUser(userId, userData) {
    return request.put(`/user/info`, userData);
  },

  // 更新用户状态（管理员专用）
  async updateUserStatus(userId, status) {
    return request.put(`/user/admin/${userId}/status`, null, {
      params: { status },
    });
  },

  // 删除用户（管理员专用）
  async deleteUser(userId) {
    return request.delete(`/user/admin/${userId}`);
  },

  // 永久删除用户（管理员专用）
  async permanentlyDeleteUser(userId) {
    return request.delete(`/user/admin/${userId}/permanent`);
  },

  // 获取用户统计
  async getUserStatistics() {
    return request.get("/statistics/users");
  },

  // ==================== 测试相关 ====================

  // 健康检查
  async healthCheck() {
    return request.get("/test/health");
  },

  // 系统信息
  async getSystemInfo() {
    return request.get("/test/info");
  },

  // Redis测试
  async testRedis() {
    return request.get("/test/redis");
  },

  // 数据库测试
  async testDatabase() {
    return request.get("/test/database");
  },

  // ==================== 数据同步相关 ====================

  // 数据同步健康检查
  async dataSyncHealthCheck() {
    return request.get("/data-sync/health");
  },

  // 数据一致性验证
  async dataSyncConsistencyCheck() {
    return request.get("/data-sync/consistency-check");
  },

  // 数据同步修复
  async dataSyncRepair(repairType) {
    return request.post("/data-sync/repair", null, {
      params: { repairType },
    });
  },

  // 获取同步统计信息
  async dataSyncStatistics() {
    return request.get("/data-sync/statistics");
  },
};

export default realApi;
