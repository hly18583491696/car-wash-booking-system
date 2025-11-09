// 模拟订单API - 用于测试状态更新功能
let mockOrders = [
  {
    id: 1,
    userName: '张三',
    userPhone: '13800138001',
    serviceName: '基础洗车',
    appointmentTime: '2024-10-25 10:00:00',
    price: 50,
    status: 'pending'
  },
  {
    id: 2,
    userName: '李四',
    userPhone: '13800138002',
    serviceName: '精洗服务',
    appointmentTime: '2024-10-25 14:00:00',
    price: 100,
    status: 'confirmed'
  },
  {
    id: 3,
    userName: '王五',
    userPhone: '13800138003',
    serviceName: '打蜡服务',
    appointmentTime: '2024-10-25 16:00:00',
    price: 150,
    status: 'processing'
  },
  {
    id: 4,
    userName: '赵六',
    userPhone: '13800138004',
    serviceName: '内饰清洁',
    appointmentTime: '2024-10-25 09:00:00',
    price: 80,
    status: 'completed'
  }
]

export const mockOrderApi = {
  // 获取订单列表
  async getOrderList() {
    console.log('🔄 Mock API: 获取订单列表')
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log('📋 Mock API: 返回订单数据', mockOrders)
        resolve({
          data: [...mockOrders],
          success: true
        })
      }, 300) // 模拟网络延迟
    })
  },

  // 更新订单状态
  async updateOrderStatus(orderId, status) {
    console.log(`🔄 Mock API: 更新订单状态 ${orderId} -> ${status}`)
    
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const order = mockOrders.find(o => o.id === orderId)
        if (order) {
          const oldStatus = order.status
          order.status = status
          console.log(`✅ Mock API: 订单状态更新成功 ${orderId}: ${oldStatus} -> ${status}`)
          resolve({
            data: order,
            success: true,
            message: '状态更新成功'
          })
        } else {
          console.log(`❌ Mock API: 订单不存在 ${orderId}`)
          reject(new Error('订单不存在'))
        }
      }, 200) // 模拟网络延迟
    })
  },

  // 更新预约状态（别名）
  async updateBookingStatus(bookingId, status) {
    return this.updateOrderStatus(bookingId, status)
  },

  // 获取订单详情
  async getOrderById(id) {
    console.log(`🔄 Mock API: 获取订单详情 ${id}`)
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const order = mockOrders.find(o => o.id === id)
        if (order) {
          resolve({
            data: order,
            success: true
          })
        } else {
          reject(new Error('订单不存在'))
        }
      }, 200)
    })
  },

  // 重置测试数据
  resetTestData() {
    console.log('🔄 Mock API: 重置测试数据')
    mockOrders = [
      {
        id: 1,
        userName: '张三',
        userPhone: '13800138001',
        serviceName: '基础洗车',
        appointmentTime: '2024-10-25 10:00:00',
        price: 50,
        status: 'pending'
      },
      {
        id: 2,
        userName: '李四',
        userPhone: '13800138002',
        serviceName: '精洗服务',
        appointmentTime: '2024-10-25 14:00:00',
        price: 100,
        status: 'confirmed'
      },
      {
        id: 3,
        userName: '王五',
        userPhone: '13800138003',
        serviceName: '打蜡服务',
        appointmentTime: '2024-10-25 16:00:00',
        price: 150,
        status: 'processing'
      },
      {
        id: 4,
        userName: '赵六',
        userPhone: '13800138004',
        serviceName: '内饰清洁',
        appointmentTime: '2024-10-25 09:00:00',
        price: 80,
        status: 'completed'
      }
    ]
  },

  // 获取当前测试数据
  getCurrentData() {
    return [...mockOrders]
  }
}

// 导出默认的模拟API
export default mockOrderApi