// API配置文件
export const API_CONFIG = {
  // 是否使用真实API（生产环境强制为true）
  USE_REAL_API: true,
  
  // 后端API基础地址
  BASE_URL: 'http://localhost:8080/api',
  
  // 请求超时时间
  TIMEOUT: 10000,
  
  // 不需要认证的接口列表
  NO_AUTH_URLS: [
    '/auth/login',
    '/auth/register', 
    '/auth/check-username',
    '/auth/check-phone',
    '/auth/check-email',
    '/test/health',
    '/test/database',
    '/test/info'
  ],
  
  // API端点映射
  ENDPOINTS: {
    // 认证相关
    AUTH: {
      LOGIN: '/auth/login',
      REGISTER: '/auth/register',
      CHECK_USERNAME: '/auth/check-username',
      CHECK_PHONE: '/auth/check-phone',
      CHECK_EMAIL: '/auth/check-email'
    },
    
    // 用户相关
    USER: {
      INFO: '/user/info',
      UPDATE: '/user/info',
      CHANGE_PASSWORD: '/user/change-password',
      LIST: '/user/admin/list',
      UPDATE_STATUS: '/user/admin/{id}/status',
      DELETE: '/user/admin/{id}'
    },
    
    // 服务相关
    SERVICE: {
      LIST: '/services/list',
      DETAIL: '/services/{id}',
      CATEGORY: '/services/category/{category}',
      SEARCH: '/services/search',
      CATEGORIES: '/services/categories',
      ADMIN_ALL: '/services/admin/all',
      CREATE: '/services',
      UPDATE: '/services/{id}',
      DELETE: '/services/{id}',
      UPDATE_STATUS: '/services/{id}/status'
    },
    
    // 预约相关
    BOOKING: {
      CREATE: '/bookings',
      LIST: '/bookings/admin/all',
      DETAIL: '/bookings/{id}',
      USER_ORDERS: '/bookings/user/{userId}',
      UPDATE_STATUS: '/bookings/{id}/status',
      CANCEL: '/bookings/{id}/cancel'
    },
    
    // 统计相关
    STATISTICS: {
      OVERVIEW: '/statistics/overview',
      TODAY: '/statistics/today',
      BOOKING_TREND: '/statistics/booking-trend',
      SERVICE_DISTRIBUTION: '/statistics/service-distribution',
      REVENUE: '/statistics/revenue',
      TIME_SLOT_HEATMAP: '/statistics/time-slot-heatmap',
      CUSTOMER_SATISFACTION: '/statistics/customer-satisfaction',
      USERS: '/statistics/users'
    },
    
    // 测试相关
    TEST: {
      HEALTH: '/test/health',
      DATABASE: '/test/database',
      REDIS: '/test/redis',
      INFO: '/test/info'
    }
  }
}

export default API_CONFIG