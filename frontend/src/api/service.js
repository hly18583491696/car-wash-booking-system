import realApi from './realApi.js'

// 服务相关API
export const serviceApi = {
  // 获取服务列表
  async getServiceList() {
    try {
      const response = await realApi.getServices()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取服务详情
  async getServiceById(id) {
    try {
      const response = await realApi.getServiceDetail(id)
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取可用时间段
  async getAvailableTimeSlots(serviceId, date) {
    try {
      // 这个API需要在后端实现
      const response = await realApi.getServices()
      return response
    } catch (error) {
      throw error
    }
  },

  // 创建预约
  async createBooking(bookingData) {
    try {
      console.log('📝 创建预约请求数据:', bookingData)
      
      // 确保必要字段存在
      if (!bookingData.userId) {
        throw new Error('用户ID不能为空')
      }
      if (!bookingData.serviceId) {
        throw new Error('服务ID不能为空')
      }
      
      const response = await realApi.createBooking(bookingData)
      console.log('✅ 创建预约响应:', response)
      return response
    } catch (error) {
      console.error('❌ 创建预约失败:', error)
      throw error
    }
  },

  // 获取用户预约列表
  async getUserBookings(userId) {
    try {
      const response = await realApi.getUserOrders(userId)
      return response
    } catch (error) {
      throw error
    }
  },

  // 取消预约
  async cancelBooking(bookingId) {
    try {
      const response = await realApi.cancelOrder(bookingId, '用户取消')
      return response
    } catch (error) {
      throw error
    }
  },

  // 删除服务（管理员专用）
  async deleteService(serviceId) {
    try {
      console.log('🗑️ 删除服务，服务ID:', serviceId)
      const response = await realApi.deleteService(serviceId)
      console.log('✅ 服务删除成功')
      return response
    } catch (error) {
      console.error('❌ 删除服务失败:', error)
      throw error
    }
  },

  // 永久删除服务（管理员专用）
  async permanentlyDeleteService(serviceId) {
    try {
      console.log('🗑️ 永久删除服务，服务ID:', serviceId)
      const response = await realApi.permanentlyDeleteService(serviceId)
      console.log('✅ 服务永久删除成功')
      return response
    } catch (error) {
      console.error('❌ 永久删除服务失败:', error)
      throw error
    }
  }
}

export default serviceApi