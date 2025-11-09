// 模拟API服务 - 用于前端开发测试
// 模拟后端API响应，直接使用本地数据
import { TimeUtils } from '@/utils/timeUtils'

class MockApiService {
  constructor() {
    // 模拟用户数据
    this.users = [
      {
        id: 1,
        username: 'admin',
        password: 'admin123',
        realName: 'Admin',
        phone: '13800138000',
        email: 'admin@carwash.com',
        role: 'admin',
        status: 1,
        avatar: ''
      },
      {
        id: 2,
        username: 'user001',
        password: 'user123',
        realName: 'User',
        phone: '13800138001',
        email: 'user001@example.com',
        role: 'user',
        status: 1,
        avatar: ''
      }
    ]

    // 模拟服务数据
    this.services = [
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗、轮胎清洁、玻璃擦拭',
        price: 30.00,
        duration: 30,
        category: '基础服务',
        status: 1
      },
      {
        id: 2,
        name: '精致洗车',
        description: '基础洗车 + 内饰清洁 + 仪表台护理',
        price: 50.00,
        duration: 45,
        category: '精致服务',
        status: 1
      },
      {
        id: 3,
        name: '豪华洗车',
        description: '精致洗车 + 打蜡 + 轮胎上光',
        price: 80.00,
        duration: 60,
        category: '豪华服务',
        status: 1
      }
    ]
  }

  // 模拟登录API
  async login(username, password) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const user = this.users.find(u => 
          (u.username === username || u.phone === username) && u.password === password
        )
        
        if (user && user.status === 1) {
          const token = 'mock_jwt_token_' + TimeUtils.toTimestamp(TimeUtils.now())
          resolve({
            code: 200,
            message: '登录成功',
            data: {
              token,
              tokenType: 'Bearer',
              user: {
                id: user.id,
                username: user.username,
                realName: user.realName,
                phone: user.phone,
                email: user.email,
                role: user.role,
                avatar: user.avatar || ''
              }
            }
          })
        } else {
          reject({
            code: 401,
            message: '用户名或密码错误'
          })
        }
      }, 500)
    })
  }

  // 模拟获取用户信息API
  async getUserInfo(token) {
    return new Promise((resolve) => {
      setTimeout(() => {
        // 从token中解析用户ID（简单模拟）
        const userId = token.includes('admin') ? 1 : 2
        const user = this.users.find(u => u.id === userId)
        
        resolve({
          code: 200,
          message: '获取成功',
          data: {
            id: user.id,
            username: user.username,
            realName: user.realName,
            phone: user.phone,
            email: user.email,
            role: user.role,
            avatar: user.avatar || ''
          }
        })
      }, 300)
    })
  }

  // 模拟获取服务列表API
  async getServices() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          message: '获取成功',
          data: this.services.filter(s => s.status === 1)
        })
      }, 300)
    })
  }

  // 模拟仪表板数据API
  async getDashboardData() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          message: '获取成功',
          data: {
            stats: {
              todayBookings: 15,
              todayRevenue: 1250.00,
              activeUsers: 89,
              avgRating: 4.8
            },
            bookingTrend: [
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(6, 'day')), bookings: 12 },
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(5, 'day')), bookings: 18 },
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(4, 'day')), bookings: 15 },
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(3, 'day')), bookings: 22 },
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(2, 'day')), bookings: 19 },
              { date: TimeUtils.formatDate(TimeUtils.today().subtract(1, 'day')), bookings: 25 },
              { date: TimeUtils.formatDate(TimeUtils.today()), bookings: 15 }
            ],
            serviceDistribution: [
              { name: '基础洗车', value: 45 },
              { name: '精致洗车', value: 30 },
              { name: '豪华洗车', value: 20 },
              { name: '其他服务', value: 5 }
            ],
            revenueData: [
              { month: '1月', revenue: 8500, cost: 5200, profit: 3300 },
              { month: '2月', revenue: 9200, cost: 5800, profit: 3400 },
              { month: '3月', revenue: 10800, cost: 6500, profit: 4300 },
              { month: '4月', revenue: 11500, cost: 7200, profit: 4300 },
              { month: '5月', revenue: 12200, cost: 7800, profit: 4400 },
              { month: '6月', revenue: 13800, cost: 8500, profit: 5300 }
            ],
            timeSlotHeatmap: [
              { time: '09:00', bookings: 8 },
              { time: '10:00', bookings: 12 },
              { time: '11:00', bookings: 15 },
              { time: '14:00', bookings: 18 },
              { time: '15:00', bookings: 22 },
              { time: '16:00', bookings: 16 },
              { time: '17:00', bookings: 10 }
            ]
          }
        })
      }, 500)
    })
  }
}

// 创建单例实例
const mockApi = new MockApiService()

export default mockApi