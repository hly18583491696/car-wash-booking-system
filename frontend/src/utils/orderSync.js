/**
 * 订单数据同步工具
 * 用于确保订单数据在不同页面间的实时同步
 */

import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 全局订单数据状态
const globalOrderState = reactive({
  orders: [],
  lastUpdate: 0
})

// 并发与一致性控制（仅应用最新一次请求结果）
let latestRequestId = 0
let lastAppliedRequestId = 0

// 订单数据变更监听器
const orderChangeListeners = new Set()

/**
 * 添加订单数据变更监听器
 * @param {Function} listener 监听器函数
 */
export function addOrderChangeListener(listener) {
  orderChangeListeners.add(listener)
  
  // 返回移除监听器的函数
  return () => {
    orderChangeListeners.delete(listener)
  }
}

/**
 * 通知所有监听器订单数据已变更
 * @param {Array} orders 新的订单数据
 */
function notifyOrderChange(orders) {
  globalOrderState.orders = orders
  globalOrderState.lastUpdate = Date.now()
  
  orderChangeListeners.forEach(listener => {
    try {
      listener(orders)
    } catch (error) {
      console.error('订单变更监听器执行失败:', error)
    }
  })
}

/**
 * 强制刷新订单数据
 * @param {Function} fetchFunction 获取订单数据的函数
 * @param {Object} options 选项
 */
export async function forceRefreshOrders(fetchFunction, options = {}) {
  const {
    showLoading = true,
    showMessage = true,
    userId,
    fastMode = true, // 快速响应模式：如有缓存，先返回缓存并后台刷新
    maxCacheAge = 30000 // 缓存有效期（毫秒）
  } = options

  console.log('🚀 forceRefreshOrders开始执行，参数:', { showLoading, showMessage, userId, fastMode, maxCacheAge })
  console.log('📋 传入的fetchFunction:', fetchFunction)

  // 参数校验
  const normalizedUserId = userId != null ? String(userId).trim() : ''
  if (!normalizedUserId) {
    const err = new Error('INVALID_PARAMETERS: userId 缺失或无效')
    console.error('❌ 参数校验失败:', err.message)
    if (showMessage) ElMessage.error('参数错误：缺少用户ID')
    throw err
  }

  // 快速模式：缓存有效则快速返回，保证<500ms响应
  const age = Date.now() - globalOrderState.lastUpdate
  if (fastMode && globalOrderState.orders.length > 0 && age <= maxCacheAge) {
    console.log('⚡ 使用缓存返回订单数据，年龄(ms):', age)
    // 后台刷新最新数据但不阻塞当前返回
    const bgRequestId = ++latestRequestId
    ;(async () => {
      try {
        if (showLoading) ElMessage.info('后台刷新订单数据...')
        const start = performance.now()
        const fresh = await fetchFunction(normalizedUserId)
        const duration = performance.now() - start
        console.log(`🕒 后台刷新完成，用时 ${Math.round(duration)}ms，数量:`, fresh?.length || 0)
        if (!Array.isArray(fresh)) {
          console.warn('⚠️ 后台刷新返回非数组数据，忽略更新')
          return
        }
        // 仅应用最新一次请求结果
        if (bgRequestId >= lastAppliedRequestId) {
          lastAppliedRequestId = bgRequestId
          notifyOrderChange(fresh)
          if (showMessage) ElMessage.success(`订单已刷新，共 ${fresh.length} 条`)
        } else {
          console.log('🔕 有更新结果更晚的请求已应用，跳过本次结果')
        }
      } catch (error) {
        console.error('❌ 后台刷新失败:', error)
      }
    })()
    return [...globalOrderState.orders]
  }

  // 正常刷新流程（并发安全：仅应用最新请求结果）
  const requestId = ++latestRequestId
  try {
    console.log('🔄 开始强制刷新订单数据...')
    if (showLoading) ElMessage.info('正在刷新订单数据...')

    const start = performance.now()
    const orders = await fetchFunction(normalizedUserId)
    const duration = performance.now() - start
    console.log(`🕒 刷新完成，用时 ${Math.round(duration)}ms`)

    if (!Array.isArray(orders)) {
      const err = new Error('INVALID_RESPONSE: 订单数据格式应为数组')
      console.error('❌ 数据格式错误:', err.message)
      if (showMessage) ElMessage.error('返回数据格式错误')
      throw err
    }

    // 并发控制：仅应用最新一次请求结果
    if (requestId >= lastAppliedRequestId) {
      lastAppliedRequestId = requestId
      notifyOrderChange(orders)
      if (showMessage) ElMessage.success(`订单数据已更新，共 ${orders.length} 条记录`)
      console.log('✅ 订单数据刷新成功，数量:', orders.length)
    } else {
      console.log('🔕 有更新结果更晚的请求已应用，跳过本次结果')
    }

    // 记录更新时间
    globalOrderState.lastUpdate = Date.now()
    return orders

  } catch (error) {
    console.error('❌ 刷新订单数据失败:', error)
    console.error('❌ 错误详情:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    })
    if (showMessage) ElMessage.error(error.message || '刷新订单数据失败')
    throw error
  }
}

/**
 * 获取全局订单状态
 */
export function getGlobalOrderState() {
  return globalOrderState
}

/**
 * 检查订单数据是否需要刷新
 * @param {number} maxAge 最大缓存时间（毫秒）
 */
export function shouldRefreshOrders(maxAge = 30000) {
  const age = Date.now() - globalOrderState.lastUpdate
  return age > maxAge || globalOrderState.orders.length === 0
}

/**
 * 添加新订单到全局状态
 * @param {Object} newOrder 新订单数据
 */
export function addOrderToGlobalState(newOrder) {
  console.log('➕ 添加新订单到全局状态:', newOrder)
  
  const orders = [...globalOrderState.orders]
  
  // 检查是否已存在相同ID的订单
  const existingIndex = orders.findIndex(order => order.id === newOrder.id)
  
  if (existingIndex >= 0) {
    // 更新现有订单
    orders[existingIndex] = newOrder
    console.log('🔄 更新现有订单:', newOrder.id)
  } else {
    // 添加新订单到开头
    orders.unshift(newOrder)
    console.log('✅ 添加新订单:', newOrder.id)
  }
  
  // 通知变更
  notifyOrderChange(orders)
}

/**
 * 更新订单状态
 * @param {number} orderId 订单ID
 * @param {string} newStatus 新状态
 */
export function updateOrderStatus(orderId, newStatus) {
  console.log('🔄 更新订单状态:', orderId, '->', newStatus)
  
  const orders = [...globalOrderState.orders]
  const orderIndex = orders.findIndex(order => order.id === orderId)
  
  if (orderIndex >= 0) {
    orders[orderIndex] = {
      ...orders[orderIndex],
      status: newStatus
    }
    
    // 通知变更
    notifyOrderChange(orders)
    
    console.log('✅ 订单状态更新成功')
  } else {
    console.warn('⚠️ 未找到要更新的订单:', orderId)
  }
}

/**
 * 创建订单数据同步Hook
 * @param {Function} fetchFunction 获取订单数据的函数
 */
export function useOrderSync(fetchFunction) {
  const orders = ref([])
  const loading = ref(false)
  const error = ref(null)
  const lastUpdate = ref(0)
  
  // 刷新订单数据
  const refreshOrders = async (options = {}) => {
    console.log('🔄 refreshOrders开始执行，参数:', options)
    console.log('📋 传入的fetchFunction:', fetchFunction)
    
    loading.value = true
    error.value = null
    try {
      console.log('📡 准备调用forceRefreshOrders...')
      const result = await forceRefreshOrders(fetchFunction, {
        ...options,
        showLoading: false
      })
      console.log('✅ forceRefreshOrders返回结果:', result)
      
      orders.value = result
      lastUpdate.value = Date.now()
      console.log('🔄 refreshOrders执行完成，订单数量:', result?.length || 0)
      return result
    } catch (err) {
      console.error('❌ refreshOrders执行失败:', err)
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 监听全局订单变更
  const removeListener = addOrderChangeListener((newOrders) => {
    orders.value = newOrders
    lastUpdate.value = Date.now()
  })
  
  // 初始化时同步全局状态
  if (globalOrderState.orders.length > 0) {
    orders.value = globalOrderState.orders
    lastUpdate.value = globalOrderState.lastUpdate
  }
  
  return {
    orders,
    loading,
    error,
    lastUpdate,
    refreshOrders,
    removeListener,
    globalState: globalOrderState
  }
}

export default {
  forceRefreshOrders,
  addOrderChangeListener,
  getGlobalOrderState,
  shouldRefreshOrders,
  addOrderToGlobalState,
  updateOrderStatus,
  useOrderSync
}