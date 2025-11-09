import realApi from './realApi.js'

// 使用真实API（真实数据替换虚拟数据）
const USE_REAL_API = true

// 认证相关API
export const authApi = {
  // 用户登录
  async login(username, password) {
    try {
      const response = await realApi.login(username, password)
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取用户信息
  async getUserInfo() {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        throw new Error('未找到登录令牌')
      }
      
      const response = await realApi.getUserInfo()
      return response
    } catch (error) {
      throw error
    }
  },

  // 用户登出
  async logout() {
    return new Promise((resolve) => {
      localStorage.removeItem('token')
      localStorage.removeItem('tokenType')
      localStorage.removeItem('user')
      resolve({
        code: 200,
        message: '登出成功'
      })
    })
  },

  // 检查用户名是否存在
  async checkUsername(username) {
    return await realApi.checkUsername(username)
  },

  // 检查手机号是否存在
  async checkPhone(phone) {
    return await realApi.checkPhone(phone)
  },

  // 检查邮箱是否存在
  async checkEmail(email) {
    return await realApi.checkEmail(email)
  }
}

export default authApi