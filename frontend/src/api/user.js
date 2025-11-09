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

  // 获取用户列表（别名，兼容旧代码）
  async getUsers() {
    return this.getUserList()
  },

  // 分页获取用户列表
  async getUsersPaginated(params = {}) {
    try {
      const { page = 1, size = 10, keyword = '', search = '' } = params
      const response = await realApi.getUserList()
      
      // 如果后端支持分页，直接返回
      if (response.data && Array.isArray(response.data.records)) {
        return response
      }
      
      // 否则在前端进行分页处理
      let users = response.data || []
      
      // 关键词搜索（支持keyword或search参数）
      const searchTerm = keyword || search
      if (searchTerm) {
        const lowerKeyword = searchTerm.toLowerCase()
        users = users.filter(user => 
          user.username?.toLowerCase().includes(lowerKeyword) ||
          user.phone?.toLowerCase().includes(lowerKeyword) ||
          user.email?.toLowerCase().includes(lowerKeyword)
        )
      }
      
      // 计算分页
      const total = users.length
      const start = (page - 1) * size
      const end = start + size
      const content = users.slice(start, end)  // 使用 content 而不是 records
      
      return {
        ...response,
        data: {
          content,        // 主要数据字段（与订单API保持一致）
          records: content,  // 兼容字段
          total,
          current: page,
          size,
          pages: Math.ceil(total / size),
          totalPages: Math.ceil(total / size)
        }
      }
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