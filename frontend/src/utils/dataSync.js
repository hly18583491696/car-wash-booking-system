/**
 * 数据同步工具 - 确保前后端数据一致性
 * 解决预约管理功能中的数据不同步问题
 */

import { ElMessage } from 'element-plus'

// 状态映射表 - 统一前后端状态值
export const STATUS_MAPPING = {
  // 后端状态 -> 前端显示状态
  'pending': 'pending',
  'confirmed': 'confirmed', 
  'in_progress': 'in_progress',
  'processing': 'in_progress', // 兼容旧状态
  'completed': 'completed',
  'cancelled': 'cancelled'
}

// 反向映射 - 前端状态 -> 后端状态
export const REVERSE_STATUS_MAPPING = {
  'pending': 'pending',
  'confirmed': 'confirmed',
  'in_progress': 'in_progress',
  'processing': 'in_progress', // 统一为 in_progress
  'completed': 'completed',
  'cancelled': 'cancelled'
}

/**
 * 标准化订单状态
 * @param {string} status - 原始状态
 * @returns {string} - 标准化后的状态
 */
export function normalizeStatus(status) {
  return STATUS_MAPPING[status] || status
}

/**
 * 获取后端API状态值
 * @param {string} frontendStatus - 前端状态
 * @returns {string} - 后端API状态值
 */
export function getBackendStatus(frontendStatus) {
  return REVERSE_STATUS_MAPPING[frontendStatus] || frontendStatus
}

/**
 * 数据同步管理器
 */
export class DataSyncManager {
  constructor() {
    this.syncQueue = []
    this.isProcessing = false
    this.retryCount = 0
    this.maxRetries = 3
  }

  /**
   * 添加同步任务到队列
   * @param {Object} task - 同步任务
   */
  addSyncTask(task) {
    this.syncQueue.push({
      ...task,
      timestamp: Date.now(),
      retries: 0
    })
    
    if (!this.isProcessing) {
      this.processSyncQueue()
    }
  }

  /**
   * 处理同步队列
   */
  async processSyncQueue() {
    if (this.isProcessing || this.syncQueue.length === 0) {
      return
    }

    this.isProcessing = true
    console.log('🔄 开始处理数据同步队列，任务数量:', this.syncQueue.length)

    while (this.syncQueue.length > 0) {
      const task = this.syncQueue.shift()
      
      try {
        await this.executeTask(task)
        console.log('✅ 同步任务执行成功:', task.type, task.id)
      } catch (error) {
        console.error('❌ 同步任务执行失败:', task.type, task.id, error)
        
        // 重试机制
        if (task.retries < this.maxRetries) {
          task.retries++
          this.syncQueue.push(task)
          console.log(`🔄 任务重试 ${task.retries}/${this.maxRetries}:`, task.type, task.id)
        } else {
          console.error('❌ 任务重试次数超限，放弃同步:', task.type, task.id)
          ElMessage.error(`数据同步失败: ${task.type} ${task.id}`)
        }
      }
    }

    this.isProcessing = false
    console.log('✅ 数据同步队列处理完成')
  }

  /**
   * 执行具体的同步任务
   * @param {Object} task - 同步任务
   */
  async executeTask(task) {
    switch (task.type) {
      case 'booking_status_update':
        return await this.syncBookingStatus(task)
      case 'booking_data_refresh':
        return await this.refreshBookingData(task)
      default:
        throw new Error(`未知的同步任务类型: ${task.type}`)
    }
  }

  /**
   * 同步预约状态
   * @param {Object} task - 状态更新任务
   */
  async syncBookingStatus(task) {
    const { id, status, api } = task
    
    // 确保使用正确的后端状态值
    const backendStatus = getBackendStatus(status)
    
    console.log(`🔄 同步预约状态: ${id} -> ${status} (后端: ${backendStatus})`)
    
    const response = await api.updateBookingStatus(id, backendStatus)
    
    if (!response || !response.success) {
      throw new Error('状态更新失败')
    }
    
    return response
  }

  /**
   * 刷新预约数据
   * @param {Object} task - 数据刷新任务
   */
  async refreshBookingData(task) {
    const { api, callback } = task
    
    console.log('🔄 刷新预约数据')
    
    const response = await api.getOrderList()
    
    if (response && response.data) {
      // 标准化状态值
      const normalizedData = response.data.map(booking => ({
        ...booking,
        status: normalizeStatus(booking.status)
      }))
      
      if (callback && typeof callback === 'function') {
        callback(normalizedData)
      }
      
      return normalizedData
    }
    
    throw new Error('获取数据失败')
  }

  /**
   * 清空同步队列
   */
  clearQueue() {
    this.syncQueue = []
    this.isProcessing = false
    console.log('🧹 同步队列已清空')
  }

  /**
   * 获取队列状态
   */
  getQueueStatus() {
    return {
      queueLength: this.syncQueue.length,
      isProcessing: this.isProcessing,
      retryCount: this.retryCount
    }
  }
}

// 创建全局同步管理器实例
export const dataSyncManager = new DataSyncManager()

/**
 * 预约状态更新辅助函数
 * @param {number} bookingId - 预约ID
 * @param {string} newStatus - 新状态
 * @param {Object} api - API对象
 * @param {Function} localUpdateCallback - 本地更新回调
 */
export async function updateBookingStatusSync(bookingId, newStatus, api, localUpdateCallback) {
  let originalStatus = null
  
  try {
    console.log(`🔄 开始同步更新预约状态: ${bookingId} -> ${newStatus}`)
    
    // 记录原始状态用于回滚
    if (localUpdateCallback) {
      // 假设可以从当前数据中获取原始状态
      originalStatus = getCurrentBookingStatus(bookingId)
    }
    
    // 立即更新本地状态（乐观更新）
    if (localUpdateCallback) {
      localUpdateCallback(bookingId, newStatus)
    }
    
    // 直接调用API更新状态，而不是使用队列
    const backendStatus = getBackendStatus(newStatus)
    const response = await api.updateBookingStatus(bookingId, backendStatus)
    
    if (!response || !response.success) {
      throw new Error('后端状态更新失败')
    }
    
    // 验证更新结果
    const updatedBooking = await api.getOrderById(bookingId)
    if (updatedBooking && updatedBooking.data) {
      const actualStatus = normalizeStatus(updatedBooking.data.status)
      if (actualStatus !== newStatus) {
        console.warn(`⚠️ 状态更新验证失败: 期望${newStatus}, 实际${actualStatus}`)
        // 使用实际状态更新本地显示
        if (localUpdateCallback) {
          localUpdateCallback(bookingId, actualStatus)
        }
      }
    }
    
    console.log('✅ 预约状态更新成功')
    return response
    
  } catch (error) {
    console.error('❌ 预约状态更新失败:', error)
    
    // 回滚本地状态
    if (localUpdateCallback && originalStatus) {
      console.log(`🔄 回滚状态: ${bookingId} -> ${originalStatus}`)
      localUpdateCallback(bookingId, originalStatus)
    }
    
    // 显示错误消息
    ElMessage.error(`状态更新失败: ${error.message}`)
    throw error
  }
}

/**
 * 获取当前预约状态（用于回滚）
 * @param {number} bookingId - 预约ID
 * @returns {string} - 当前状态
 */
function getCurrentBookingStatus(bookingId) {
  // 这里可以从全局状态或缓存中获取当前状态
  // 暂时返回默认值
  return 'pending'
}

/**
 * 数据刷新辅助函数
 * @param {Object} api - API对象
 * @param {Function} dataUpdateCallback - 数据更新回调
 */
export async function refreshBookingDataSync(api, dataUpdateCallback) {
  try {
    console.log('🔄 开始同步刷新预约数据')
    
    // 直接调用API获取数据，不使用队列
    const response = await api.getOrderList()
    
    if (response && response.data) {
      // 标准化状态值
      const normalizedData = response.data.map(booking => ({
        ...booking,
        status: normalizeStatus(booking.status)
      }))
      
      if (dataUpdateCallback && typeof dataUpdateCallback === 'function') {
        dataUpdateCallback(normalizedData)
      }
      
      console.log('✅ 数据刷新成功，数量:', normalizedData.length)
      return normalizedData
    }
    
    throw new Error('获取数据失败')
    
  } catch (error) {
    console.error('❌ 数据刷新失败:', error)
    throw error
  }
}

export default {
  DataSyncManager,
  dataSyncManager,
  normalizeStatus,
  getBackendStatus,
  updateBookingStatusSync,
  refreshBookingDataSync,
  STATUS_MAPPING,
  REVERSE_STATUS_MAPPING
}