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

  // 永久删除用户（硬删除）
  async permanentlyDeleteUser(userId) {
    try {
      console.log('🗑️ 永久删除用户，用户ID:', userId)
      const response = await realApi.permanentlyDeleteUser(userId)
      console.log('✅ 用户永久删除成功')
      return response
    } catch (error) {
      console.error('❌ 永久删除用户失败:', error)
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