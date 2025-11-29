// 模拟订单API - 用于测试状态更新功能
let mockOrders = [
  {
    id: 1,
    userName: "张三",
    userPhone: "13800138001",
    serviceName: "基础洗车",
    appointmentTime: "2024-10-25 10:00:00",
    price: 50,
    status: "pending",
  },
  {
    id: 2,
    userName: "李四",
    userPhone: "13800138002",
    serviceName: "精洗服务",
    appointmentTime: "2024-10-25 14:00:00",
    price: 100,
    status: "confirmed",
  },
  {
    id: 3,
    userName: "王五",
    userPhone: "13800138003",
    serviceName: "打蜡服务",
    appointmentTime: "2024-10-25 16:00:00",
    price: 150,
    status: "processing",
  },
  {
    id: 4,
    userName: "赵六",
    userPhone: "13800138004",
    serviceName: "内饰清洁",
    appointmentTime: "2024-10-25 09:00:00",
    price: 80,
    status: "completed",
  },
];

export const mockOrderApi = {
  // 获取订单列表
  async getOrderList() {
    console.log("🔄 Mock API: 获取订单列表");
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log("📋 Mock API: 返回订单数据", mockOrders);
        resolve({
          data: [...mockOrders],
          success: true,
        });
      }, 300); // 模拟网络延迟
    });
  },

  // 基础订单查询（支持按状态与关键字过滤）
  async getOrders(params = {}) {
    const { status, search } = params;
    console.log("🔄 Mock API: 基础订单查询", params);
    return new Promise((resolve) => {
      setTimeout(() => {
        let list = [...mockOrders];
        if (status) {
          list = list.filter(
            (o) =>
              o.status === status ||
              (status === "in_progress" && o.status === "processing"),
          );
        }
        if (search) {
          const s = String(search).trim();
          list = list.filter(
            (o) =>
              String(o.userName).includes(s) ||
              String(o.userPhone).includes(s) ||
              String(o.serviceName).includes(s),
          );
        }
        resolve({ data: list, success: true });
      }, 200);
    });
  },

  // 分页订单查询（返回 content/totalElements/totalPages）
  async getOrdersPaginated({ page = 1, size = 20, status, search } = {}) {
    console.log("🔄 Mock API: 分页订单查询", { page, size, status, search });
    return new Promise((resolve) => {
      setTimeout(() => {
        let list = [...mockOrders];
        if (status) {
          list = list.filter(
            (o) =>
              o.status === status ||
              (status === "in_progress" && o.status === "processing"),
          );
        }
        if (search) {
          const s = String(search).trim();
          list = list.filter(
            (o) =>
              String(o.userName).includes(s) ||
              String(o.userPhone).includes(s) ||
              String(o.serviceName).includes(s),
          );
        }
        const total = list.length;
        const totalPages = Math.max(1, Math.ceil(total / size));
        const start = (page - 1) * size;
        const end = start + size;
        const content = list.slice(start, end);
        resolve({
          data: { content, totalElements: total, totalPages },
          success: true,
        });
      }, 250);
    });
  },

  // 更新订单状态
  async updateOrderStatus(orderId, status) {
    console.log(`🔄 Mock API: 更新订单状态 ${orderId} -> ${status}`);

    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const order = mockOrders.find((o) => o.id === orderId);
        if (order) {
          const oldStatus = order.status;
          order.status = status;
          console.log(
            `✅ Mock API: 订单状态更新成功 ${orderId}: ${oldStatus} -> ${status}`,
          );
          resolve({
            data: order,
            success: true,
            message: "状态更新成功",
          });
        } else {
          console.log(`❌ Mock API: 订单不存在 ${orderId}`);
          reject(new Error("订单不存在"));
        }
      }, 200); // 模拟网络延迟
    });
  },

  // 更新预约状态（别名）
  async updateBookingStatus(bookingId, status) {
    return this.updateOrderStatus(bookingId, status);
  },

  // 获取订单详情
  async getOrderById(id) {
    console.log(`🔄 Mock API: 获取订单详情 ${id}`);
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const order = mockOrders.find((o) => o.id === id);
        if (order) {
          resolve({
            data: order,
            success: true,
          });
        } else {
          reject(new Error("订单不存在"));
        }
      }, 200);
    });
  },

  // 重置测试数据
  resetTestData() {
    console.log("🔄 Mock API: 重置测试数据");
    mockOrders = [
      {
        id: 1,
        userName: "张三",
        userPhone: "13800138001",
        serviceName: "基础洗车",
        appointmentTime: "2024-10-25 10:00:00",
        price: 50,
        status: "pending",
      },
      {
        id: 2,
        userName: "李四",
        userPhone: "13800138002",
        serviceName: "精洗服务",
        appointmentTime: "2024-10-25 14:00:00",
        price: 100,
        status: "confirmed",
      },
      {
        id: 3,
        userName: "王五",
        userPhone: "13800138003",
        serviceName: "打蜡服务",
        appointmentTime: "2024-10-25 16:00:00",
        price: 150,
        status: "processing",
      },
      {
        id: 4,
        userName: "赵六",
        userPhone: "13800138004",
        serviceName: "内饰清洁",
        appointmentTime: "2024-10-25 09:00:00",
        price: 80,
        status: "completed",
      },
    ];
  },

  // 获取当前测试数据
  getCurrentData() {
    return [...mockOrders];
  },
};

// 导出默认的模拟API
export default mockOrderApi;
