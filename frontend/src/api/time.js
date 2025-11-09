/**
 * 时间同步API
 * 用于前后端时间同步
 */

import request from './request'

/**
 * 时间API服务
 */
export const timeApi = {
  /**
   * 获取服务器当前时间
   * @returns {Promise} 服务器时间信息
   */
  async getCurrentTime() {
    return request.get('/time/current')
  },

  /**
   * 获取服务器时区信息
   * @returns {Promise} 时区信息
   */
  async getTimezone() {
    return request.get('/time/timezone')
  },

  /**
   * 时间服务健康检查
   * @returns {Promise} 健康状态
   */
  async healthCheck() {
    return request.get('/time/health')
  }
}

/**
 * 时间同步服务
 */
export class TimeSyncService {
  constructor() {
    this.serverTimeOffset = 0 // 服务器时间偏移量
    this.lastSyncTime = 0
    this.syncInterval = 5 * 60 * 1000 // 5分钟同步一次
  }

  /**
   * 同步服务器时间
   * @returns {Promise<number>} 时间偏移量
   */
  async syncServerTime() {
    try {
      const startTime = Date.now()
      const response = await timeApi.getCurrentTime()
      const endTime = Date.now()
      
      if (response.code === 200) {
        const serverTime = new Date(response.data.timestamp).getTime()
        const networkDelay = (endTime - startTime) / 2
        const clientTime = startTime + networkDelay
        
        // 计算时间偏移量
        this.serverTimeOffset = serverTime - clientTime
        this.lastSyncTime = Date.now()
        
        console.log('时间同步成功，偏移量:', this.serverTimeOffset, 'ms')
        return this.serverTimeOffset
      }
    } catch (error) {
      console.error('时间同步失败:', error)
      throw error
    }
  }

  /**
   * 获取同步后的服务器时间
   * @returns {Date} 服务器时间
   */
  getServerTime() {
    return new Date(Date.now() + this.serverTimeOffset)
  }

  /**
   * 检查是否需要重新同步
   * @returns {boolean} 是否需要同步
   */
  needsSync() {
    return Date.now() - this.lastSyncTime > this.syncInterval
  }

  /**
   * 自动同步时间（如果需要）
   * @returns {Promise<void>}
   */
  async autoSync() {
    if (this.needsSync()) {
      try {
        await this.syncServerTime()
      } catch (error) {
        console.warn('自动时间同步失败:', error)
      }
    }
  }
}

// 创建全局时间同步实例
export const timeSyncService = new TimeSyncService()

// 页面加载时自动同步
if (typeof window !== 'undefined') {
  timeSyncService.syncServerTime().catch(console.error)
  
  // 定期自动同步
  setInterval(() => {
    timeSyncService.autoSync()
  }, 60 * 1000) // 每分钟检查一次
}

export default timeApi