// 权限管理工具
import { ElMessage, ElNotification } from 'element-plus'

/**
 * 权限管理类
 */
export class AuthManager {
  // 获取当前用户信息
  static getCurrentUser() {
    try {
      const userInfo = localStorage.getItem('userInfo')
      if (!userInfo || userInfo === 'undefined' || userInfo === 'null') {
        return null
      }
      return JSON.parse(userInfo)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      // 清除无效的用户信息
      localStorage.removeItem('userInfo')
      return null
    }
  }

  // 获取用户角色
  static getUserRole() {
    const userRole = localStorage.getItem('userRole')
    const user = this.getCurrentUser()
    
    // 优先使用localStorage中的角色，其次使用用户信息中的角色
    return userRole || user?.role || null
  }

  // 检查是否已登录
  static isAuthenticated() {
    const token = localStorage.getItem('token')
    const user = this.getCurrentUser()
    return !!(token && user)
  }

  // 检查是否为管理员
  static isAdmin() {
    const role = this.getUserRole()
    return role === 'admin'
  }

  // 检查是否为普通用户
  static isUser() {
    const role = this.getUserRole()
    return role === 'user'
  }

  // 检查是否有指定权限
  static hasPermission(permission) {
    const role = this.getUserRole()
    
    // 如果是管理员，允许所有权限
    if (role === 'admin') {
      return true
    }
    
    const permissions = {
      admin: [
        'overview',
        'bookings',
        'services', 
        'users',
        'system'
      ],
      user: [
        'profile.view',
        'bookings.create',
        'bookings.view',
        'services.view'
      ]
    }

    return permissions[role]?.includes(permission) || false
  }

  // 获取用户信息（兼容性方法）
  static getUserInfo() {
    return this.getCurrentUser()
  }

  // 获取用户显示名称
  static getUserDisplayName() {
    const user = this.getCurrentUser()
    return user?.realName || user?.username || '未知用户'
  }

  // 登录
  static login(userInfo, token, tokenType = 'Bearer') {
    try {
      localStorage.setItem('token', token)
      localStorage.setItem('tokenType', tokenType)
      localStorage.setItem('userRole', userInfo.role)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      ElNotification({
        title: '登录成功',
        message: `欢迎回来，${userInfo.realName || userInfo.username}！`,
        type: 'success',
        duration: 3000
      })
      
      return true
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请重试')
      return false
    }
  }

  // 退出登录
  static logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('tokenType')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userInfo')
    
    ElMessage.success('已退出登录')
  }

  // 权限检查装饰器
  static requireAuth() {
    if (!this.isAuthenticated()) {
      throw new Error('REQUIRE_AUTH')
    }
    return true
  }

  // 管理员权限检查
  static requireAdmin() {
    if (!this.isAuthenticated()) {
      throw new Error('REQUIRE_AUTH')
    }
    if (!this.isAdmin()) {
      throw new Error('REQUIRE_ADMIN')
    }
    return true
  }

  // 检查Token是否即将过期
  static isTokenExpiring() {
    // 这里可以添加JWT Token过期检查逻辑
    return false
  }

  // 刷新Token
  static async refreshToken() {
    // 这里可以添加Token刷新逻辑
    return true
  }

  // 权限错误处理
  static handleAuthError(error) {
    switch (error.message) {
      case 'REQUIRE_AUTH':
        ElMessage.warning('请先登录')
        return '/login'
      case 'REQUIRE_ADMIN':
        ElMessage.error('您没有管理员权限')
        return '/'
      default:
        ElMessage.error('权限验证失败')
        return '/'
    }
  }
}

// 权限指令 - 用于Vue模板中的权限控制
export const authDirective = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value && !AuthManager.hasPermission(value)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const { value } = binding
    
    if (value && !AuthManager.hasPermission(value)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

export default AuthManager