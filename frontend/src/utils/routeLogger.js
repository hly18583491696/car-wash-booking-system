/**
 * 路由跳转日志记录工具
 */
export class RouteLogger {
  static logs = []
  static maxLogs = 100 // 最大日志数量

  /**
   * 记录路由跳转日志
   * @param {Object} logData 日志数据
   */
  static log(logData) {
    const timestamp = new Date().toISOString()
    const logEntry = {
      timestamp,
      id: this.generateId(),
      ...logData
    }

    this.logs.unshift(logEntry)
    
    // 限制日志数量
    if (this.logs.length > this.maxLogs) {
      this.logs = this.logs.slice(0, this.maxLogs)
    }

    // 输出到控制台（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('🚀 路由跳转:', logEntry)
    }

    // 可选：发送到服务器进行分析
    this.sendToServer(logEntry)
  }

  /**
   * 记录认证相关的路由跳转
   * @param {string} from 来源路径
   * @param {string} to 目标路径
   * @param {string} reason 跳转原因
   * @param {Object} userInfo 用户信息
   */
  static logAuthRedirect(from, to, reason, userInfo = null) {
    this.log({
      type: 'auth_redirect',
      from,
      to,
      reason,
      userInfo: userInfo ? {
        id: userInfo.id,
        role: userInfo.role,
        username: userInfo.username
      } : null,
      isAuthenticated: !!userInfo
    })
  }

  /**
   * 记录权限检查失败
   * @param {string} path 访问路径
   * @param {string} requiredPermission 需要的权限
   * @param {Object} userInfo 用户信息
   */
  static logPermissionDenied(path, requiredPermission, userInfo = null) {
    this.log({
      type: 'permission_denied',
      path,
      requiredPermission,
      userInfo: userInfo ? {
        id: userInfo.id,
        role: userInfo.role,
        username: userInfo.username
      } : null,
      isAuthenticated: !!userInfo
    })
  }

  /**
   * 记录登录状态检测
   * @param {string} path 当前路径
   * @param {boolean} isAuthenticated 是否已认证
   * @param {Object} userInfo 用户信息
   */
  static logAuthCheck(path, isAuthenticated, userInfo = null) {
    this.log({
      type: 'auth_check',
      path,
      isAuthenticated,
      userInfo: userInfo ? {
        id: userInfo.id,
        role: userInfo.role,
        username: userInfo.username
      } : null
    })
  }

  /**
   * 记录路由守卫执行
   * @param {string} from 来源路径
   * @param {string} to 目标路径
   * @param {string} action 执行的动作
   * @param {Object} meta 路由元信息
   */
  static logGuardExecution(from, to, action, meta = {}) {
    this.log({
      type: 'guard_execution',
      from,
      to,
      action,
      meta: {
        requiresAuth: meta.requiresAuth,
        requiresAdmin: meta.requiresAdmin,
        hideForAuth: meta.hideForAuth,
        guestOnly: meta.guestOnly
      }
    })
  }

  /**
   * 获取所有日志
   * @returns {Array} 日志数组
   */
  static getLogs() {
    return [...this.logs]
  }

  /**
   * 获取特定类型的日志
   * @param {string} type 日志类型
   * @returns {Array} 过滤后的日志数组
   */
  static getLogsByType(type) {
    return this.logs.filter(log => log.type === type)
  }

  /**
   * 获取最近的日志
   * @param {number} count 数量
   * @returns {Array} 最近的日志
   */
  static getRecentLogs(count = 10) {
    return this.logs.slice(0, count)
  }

  /**
   * 清除所有日志
   */
  static clearLogs() {
    this.logs = []
    console.log('📝 路由日志已清除')
  }

  /**
   * 导出日志为JSON
   * @returns {string} JSON字符串
   */
  static exportLogs() {
    return JSON.stringify(this.logs, null, 2)
  }

  /**
   * 生成唯一ID
   * @returns {string} 唯一ID
   */
  static generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  /**
   * 发送日志到服务器（可选）
   * @param {Object} logEntry 日志条目
   */
  static async sendToServer(logEntry) {
    // 只在生产环境发送关键日志
    if (process.env.NODE_ENV !== 'production') {
      return
    }

    // 只发送重要的日志类型
    const importantTypes = ['auth_redirect', 'permission_denied']
    if (!importantTypes.includes(logEntry.type)) {
      return
    }

    try {
      // 这里可以实现发送到服务器的逻辑
      // await fetch('/api/logs/route', {
      //   method: 'POST',
      //   headers: {
      //     'Content-Type': 'application/json'
      //   },
      //   body: JSON.stringify(logEntry)
      // })
    } catch (error) {
      console.error('发送路由日志失败:', error)
    }
  }

  /**
   * 获取统计信息
   * @returns {Object} 统计信息
   */
  static getStatistics() {
    const stats = {
      total: this.logs.length,
      byType: {},
      recentActivity: this.logs.slice(0, 5),
      authRedirects: 0,
      permissionDenied: 0
    }

    this.logs.forEach(log => {
      // 按类型统计
      stats.byType[log.type] = (stats.byType[log.type] || 0) + 1
      
      // 特殊统计
      if (log.type === 'auth_redirect') {
        stats.authRedirects++
      } else if (log.type === 'permission_denied') {
        stats.permissionDenied++
      }
    })

    return stats
  }
}

// 在开发环境下暴露到全局，方便调试
if (process.env.NODE_ENV === 'development') {
  window.RouteLogger = RouteLogger
}

export default RouteLogger