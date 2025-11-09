import realApi from './realApi.js'

// 仪表板数据服务
class DashboardService {
  // 获取统计概览数据
  static async getStatsOverview() {
    try {
      const response = await realApi.getStatisticsOverview()
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取预约趋势数据
  static async getBookingTrends(period = '7days') {
    try {
      const response = await realApi.getBookingTrend(period)
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取服务类型分布
  static async getServiceDistribution() {
    try {
      const response = await realApi.getServiceDistribution()
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取收入统计
  static async getRevenueStats(type = 'monthly') {
    try {
      const response = await realApi.getRevenueStatistics(type)
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取时段热力图数据
  static async getHeatmapData() {
    try {
      const response = await realApi.getTimeSlotHeatmap()
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取客户满意度数据
  static async getSatisfactionData() {
    try {
      const response = await realApi.getCustomerSatisfaction()
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取实时数据（WebSocket模拟）
  static subscribeToRealTimeData(callback) {
    const interval = setInterval(() => {
      const data = {
        timestamp: new Date(),
        newBooking: Math.random() > 0.7,
        completedService: Math.random() > 0.8,
        onlineUsers: Math.floor(Math.random() * 50) + 100
      }
      callback(data)
    }, 5000)
    
    return () => clearInterval(interval)
  }
}

export default DashboardService