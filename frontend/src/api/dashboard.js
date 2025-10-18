import mockApi from './mock.js'

// 仪表板数据服务
class DashboardService {
  // 获取统计概览数据
  static async getStatsOverview() {
    // 模拟API调用
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: {
            todayBookings: {
              value: 156,
              trend: 12.5,
              chartData: [120, 132, 145, 138, 156, 149, 156]
            },
            todayRevenue: {
              value: 8420,
              trend: 8.3,
              chartData: [7200, 7800, 8100, 7900, 8420, 8200, 8420]
            },
            activeUsers: {
              value: 2341,
              trend: -2.1,
              chartData: [2400, 2380, 2350, 2341, 2355, 2341, 2341]
            },
            avgRating: {
              value: 4.8,
              trend: 5.2,
              chartData: [4.6, 4.7, 4.8, 4.7, 4.8, 4.8, 4.8]
            }
          }
        })
      }, 1000)
    })
  }
  
  // 获取预约趋势数据
  static async getBookingTrends(period = '7days') {
    return new Promise((resolve) => {
      setTimeout(() => {
        const data = {
          '7days': {
            labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
            bookings: [45, 52, 38, 65, 72, 89, 94],
            completed: [42, 48, 35, 61, 68, 85, 90]
          },
          '30days': {
            labels: Array.from({length: 30}, (_, i) => `${i + 1}日`),
            bookings: Array.from({length: 30}, () => Math.floor(Math.random() * 50) + 30),
            completed: Array.from({length: 30}, () => Math.floor(Math.random() * 45) + 25)
          },
          '3months': {
            labels: ['1月', '2月', '3月'],
            bookings: [1200, 1350, 1480],
            completed: [1150, 1280, 1420]
          }
        }
        
        resolve({ data: data[period] })
      }, 800)
    })
  }
  
  // 获取服务类型分布
  static async getServiceDistribution() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: [
            { name: '基础洗车', value: 335, color: '#10B981' },
            { name: '精洗套餐', value: 310, color: '#3B82F6' },
            { name: '内饰清洁', value: 234, color: '#8B5CF6' },
            { name: '打蜡服务', value: 135, color: '#F59E0B' },
            { name: '镀膜服务', value: 154, color: '#EF4444' }
          ]
        })
      }, 600)
    })
  }
  
  // 获取收入统计
  static async getRevenueStats(type = 'monthly') {
    return new Promise((resolve) => {
      setTimeout(() => {
        const data = {
          daily: {
            labels: Array.from({length: 30}, (_, i) => `${i + 1}日`),
            revenue: Array.from({length: 30}, () => Math.floor(Math.random() * 2000) + 1000),
            cost: Array.from({length: 30}, () => Math.floor(Math.random() * 1000) + 500),
            profit: Array.from({length: 30}, () => Math.floor(Math.random() * 1000) + 300)
          },
          monthly: {
            labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            revenue: [45000, 52000, 38000, 65000, 72000, 89000, 94000, 87000, 76000, 82000, 91000, 98000],
            cost: [25000, 28000, 22000, 35000, 38000, 45000, 48000, 44000, 38000, 42000, 46000, 49000],
            profit: [20000, 24000, 16000, 30000, 34000, 44000, 46000, 43000, 38000, 40000, 45000, 49000]
          }
        }
        
        resolve({ data: data[type] })
      }, 900)
    })
  }
  
  // 获取时段热力图数据
  static async getHeatmapData() {
    return new Promise((resolve) => {
      setTimeout(() => {
        const hours = ['8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00']
        const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        
        const data = []
        for (let i = 0; i < days.length; i++) {
          for (let j = 0; j < hours.length; j++) {
            // 模拟真实的预约热度分布
            let value = Math.floor(Math.random() * 20)
            
            // 周末和特定时段更热门
            if (i >= 5) value += 5 // 周末
            if (j >= 4 && j <= 7) value += 3 // 下午时段
            if (j >= 9 && j <= 11) value += 2 // 晚上时段
            
            data.push([j, i, Math.min(value, 20)])
          }
        }
        
        resolve({
          data: {
            hours,
            days,
            values: data
          }
        })
      }, 700)
    })
  }
  
  // 获取客户满意度数据
  static async getSatisfactionData() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: {
            overall: 87,
            breakdown: {
              service: 89,
              price: 82,
              speed: 91,
              quality: 85
            },
            trend: [82, 84, 86, 87, 88, 87, 87]
          }
        })
      }, 500)
    })
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