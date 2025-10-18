import mockApi from './mock.js'
import realApi from './realApi.js'

// 是否使用真实API（可以通过环境变量控制）
const USE_REAL_API = import.meta.env.VITE_USE_REAL_API === 'true' || false

// 认证相关API
export const authApi = {
  // 用户登录
  async login(username, password) {
    try {
      if (USE_REAL_API) {
        const response = await realApi.login(username, password)
        return response
      } else {
        const response = await mockApi.login(username, password)
        return response
      }
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
      
      if (USE_REAL_API) {
        const response = await realApi.getUserInfo()
        return response
      } else {
        const response = await mockApi.getUserInfo(token)
        return response
      }
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
    if (USE_REAL_API) {
      return await realApi.checkUsername(username)
    } else {
      return new Promise((resolve) => {
        setTimeout(() => {
          const exists = username === 'admin' || username === 'user001'
          resolve({
            code: 200,
            message: exists ? '用户名已存在' : '用户名可用',
            data: exists
          })
        }, 300)
      })
    }
  },

  // 检查手机号是否存在
  async checkPhone(phone) {
    if (USE_REAL_API) {
      return await realApi.checkPhone(phone)
    } else {
      return new Promise((resolve) => {
        setTimeout(() => {
          const exists = phone === '13800138000' || phone === '13800138001'
          resolve({
            code: 200,
            message: exists ? '手机号已存在' : '手机号可用',
            data: exists
          })
        }, 300)
      })
    }
  },

  // 检查邮箱是否存在
  async checkEmail(email) {
    if (USE_REAL_API) {
      return await realApi.checkEmail(email)
    } else {
      return new Promise((resolve) => {
        setTimeout(() => {
          const exists = email === 'admin@carwash.com' || email === 'user001@example.com'
          resolve({
            code: 200,
            message: exists ? '邮箱已存在' : '邮箱可用',
            data: exists
          })
        }, 300)
      })
    }
  }
}

export default authApi