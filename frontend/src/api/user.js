import realApi from './realApi.js'

// 用户相关API
export const userApi = {
  // 获取用户列表
  async getUserList() {
    try {
      const response = await realApi.getUserList()
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取用户详情
  async getUserById(id) {
    try {
      const response = await realApi.getUserById(id)
      return response
    } catch (error) {
      throw error
    }
  },

  // 更新用户信息
  async updateUser(userId, userData) {
    try {
      const response = await realApi.updateUser(userId, userData)
      return response
    } catch (error) {
      throw error
    }
  },

  // 更新用户状态
  async updateUserStatus(userId, status) {
    try {
      const response = await realApi.updateUserStatus(userId, status)
      return response
    } catch (error) {
      throw error
    }
  },

  // 删除用户
  async deleteUser(userId) {
    try {
      const response = await realApi.deleteUser(userId)
      return response
    } catch (error) {
      throw error
    }
  },

  // 获取用户统计
  async getUserStats() {
    try {
      const response = await realApi.getUserStatistics()
      return response
    } catch (error) {
      throw error
    }
  }
}

export default userApi