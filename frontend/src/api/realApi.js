import request from './request.js'

// 真实API服务 - 连接后端
export const realApi = {
  // ==================== 认证相关 ====================
  
  // 用户登录
  async login(username, password) {
    return request.post('/auth/login', {
      username,
      password
    })
  },

  // 用户注册
  async register(userInfo) {
    return request.post('/auth/register', userInfo)
  },

  // 检查用户名是否存在
  async checkUsername(username) {
    return request.get('/auth/check-username', {
      params: { username }
    })
  },

  // 检查手机号是否存在
  async checkPhone(phone) {
    return request.get('/auth/check-phone', {
      params: { phone }
    })
  },

  // 检查邮箱是否存在
  async checkEmail(email) {
    return request.get('/auth/check-email', {
      params: { email }
    })
  },

  // ==================== 用户相关 ====================
  
  // 获取用户信息
  async getUserInfo() {
    return request.get('/user/info')
  },

  // 更新用户信息
  async updateUserInfo(userInfo) {
    return request.put('/user/info', userInfo)
  },

  // 修改密码
  async changePassword(oldPassword, newPassword) {
    return request.post('/user/change-password', null, {
      params: { oldPassword, newPassword }
    })
  },

  // ==================== 服务相关 ====================
  
  // 获取服务列表
  async getServices(params = {}) {
    return request.get('/services/list', { params })
  },

  // 获取服务详情
  async getServiceDetail(id) {
    return request.get(`/services/${id}`)
  },

  // 按分类获取服务
  async getServicesByCategory(category) {
    return request.get(`/services/category/${category}`)
  },

  // 搜索服务
  async searchServices(keyword) {
    return request.get('/services/search', {
      params: { keyword }
    })
  },

  // 获取服务分类
  async getServiceCategories() {
    return request.get('/services/categories')
  },

  // ==================== 管理员相关 ====================
  
  // 获取所有服务（管理员）
  async getAllServices(params = {}) {
    return request.get('/services/admin/all', { params })
  },

  // 创建服务
  async createService(serviceData) {
    return request.post('/services', serviceData)
  },

  // 更新服务
  async updateService(id, serviceData) {
    return request.put(`/services/${id}`, serviceData)
  },

  // 删除服务
  async deleteService(id) {
    return request.delete(`/services/${id}`)
  },

  // 更新服务状态
  async updateServiceStatus(id, status) {
    return request.put(`/services/${id}/status`, null, {
      params: { status }
    })
  },

  // ==================== 测试相关 ====================
  
  // 健康检查
  async healthCheck() {
    return request.get('/test/health')
  },

  // 系统信息
  async getSystemInfo() {
    return request.get('/test/info')
  },

  // Redis测试
  async testRedis() {
    return request.get('/test/redis')
  },

  // 数据库测试
  async testDatabase() {
    return request.get('/test/database')
  }
}

export default realApi