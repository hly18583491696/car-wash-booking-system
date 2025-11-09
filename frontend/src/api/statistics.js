import realApi from './realApi.js'

// 统计相关API
export const statisticsApi = {
  // 获取概览统计
  async getOverview() {
    try {
      const response = await realApi.getStatisticsOverview()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取今日统计
  async getTodayStats() {
    try {
      const response = await realApi.getTodayStatistics()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取预约趋势数据
  async getBookingTrend(period) {
    try {
      const response = await realApi.getBookingTrend(period)
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取服务类型分布
  async getServiceDistribution() {
    try {
      const response = await realApi.getServiceDistribution()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取收入统计
  async getRevenueStats(type) {
    try {
      const response = await realApi.getRevenueStatistics(type)
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取时段热力图数据
  async getTimeSlotHeatmap() {
    try {
      const response = await realApi.getTimeSlotHeatmap()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取客户满意度
  async getCustomerSatisfaction() {
    try {
      const response = await realApi.getCustomerSatisfaction()
      return response
    } catch (error) {
      throw error
    }
  }
}

export default statisticsApi