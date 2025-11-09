/**
 * 时间同步组合函数
 * 用于前后端数据同步和实时更新
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { TimeUtils } from '@/utils/timeUtils'

/**
 * 使用时间同步功能
 * @param {Function} updateCallback 数据更新回调函数
 * @param {number} interval 同步间隔（毫秒）
 * @returns {Object} 同步控制对象
 */
export function useTimeSync(updateCallback, interval = 30000) {
  const isSync = ref(false)
  const lastSyncTime = ref(null)
  let syncTimer = null

  /**
   * 开始同步
   */
  const startSync = () => {
    if (syncTimer) return

    isSync.value = true
    syncTimer = setInterval(async () => {
      try {
        if (updateCallback && typeof updateCallback === 'function') {
          await updateCallback()
          lastSyncTime.value = TimeUtils.now().format('HH:mm:ss')
        }
      } catch (error) {
        console.error('数据同步失败:', error)
      }
    }, interval)
  }

  /**
   * 停止同步
   */
  const stopSync = () => {
    if (syncTimer) {
      clearInterval(syncTimer)
      syncTimer = null
      isSync.value = false
    }
  }

  /**
   * 手动同步一次
   */
  const syncOnce = async () => {
    try {
      if (updateCallback && typeof updateCallback === 'function') {
        await updateCallback()
        lastSyncTime.value = TimeUtils.now().format('HH:mm:ss')
      }
    } catch (error) {
      console.error('手动同步失败:', error)
      throw error
    }
  }

  // 组件挂载时自动开始同步
  onMounted(() => {
    startSync()
  })

  // 组件卸载时停止同步
  onUnmounted(() => {
    stopSync()
  })

  return {
    isSync,
    lastSyncTime,
    startSync,
    stopSync,
    syncOnce
  }
}

/**
 * 使用订单数据同步
 * @param {Function} fetchOrders 获取订单数据的函数
 * @returns {Object} 订单同步对象
 */
export function useOrderSync(fetchOrders) {
  const orders = ref([])
  const loading = ref(false)
  const error = ref(null)

  /**
   * 更新订单数据
   */
  const updateOrders = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await fetchOrders()
      const serverOrders = response.data || response

      // 比较并更新有变化的订单
      updateChangedOrders(serverOrders)
      
    } catch (err) {
      error.value = err.message || '获取订单数据失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新有变化的订单
   * @param {Array} serverOrders 服务器订单数据
   */
  const updateChangedOrders = (serverOrders) => {
    serverOrders.forEach(serverOrder => {
      const localOrder = orders.value.find(o => o.id === serverOrder.id)
      
      // 比较更新时间判断是否有变化
      if (!localOrder || 
          TimeUtils.toTimestamp(localOrder.updateTime) !== TimeUtils.toTimestamp(serverOrder.updateTime)) {
        
        // 数据有变化，更新本地数据
        const index = orders.value.findIndex(o => o.id === serverOrder.id)
        if (index !== -1) {
          orders.value[index] = {
            ...serverOrder,
            // 格式化时间字段
            createTime: TimeUtils.formatServerTime(serverOrder.createTime),
            updateTime: TimeUtils.formatServerTime(serverOrder.updateTime),
            appointmentTime: TimeUtils.formatServerTime(serverOrder.appointmentTime)
          }
        } else {
          orders.value.push({
            ...serverOrder,
            createTime: TimeUtils.formatServerTime(serverOrder.createTime),
            updateTime: TimeUtils.formatServerTime(serverOrder.updateTime),
            appointmentTime: TimeUtils.formatServerTime(serverOrder.appointmentTime)
          })
        }
      }
    })

    // 移除服务器端不存在的订单
    orders.value = orders.value.filter(localOrder => 
      serverOrders.some(serverOrder => serverOrder.id === localOrder.id)
    )
  }

  // 使用时间同步
  const syncControl = useTimeSync(updateOrders)

  return {
    orders,
    loading,
    error,
    updateOrders,
    ...syncControl
  }
}

/**
 * 使用实时时间显示
 * @returns {Object} 实时时间对象
 */
export function useRealTime() {
  const currentTime = ref(TimeUtils.now())
  const formattedTime = ref(TimeUtils.formatServerTime(TimeUtils.now()))
  let timeTimer = null

  /**
   * 开始实时时间更新
   */
  const startRealTime = () => {
    if (timeTimer) return

    timeTimer = setInterval(() => {
      currentTime.value = TimeUtils.now()
      formattedTime.value = TimeUtils.formatServerTime(TimeUtils.now())
    }, 1000)
  }

  /**
   * 停止实时时间更新
   */
  const stopRealTime = () => {
    if (timeTimer) {
      clearInterval(timeTimer)
      timeTimer = null
    }
  }

  onMounted(() => {
    startRealTime()
  })

  onUnmounted(() => {
    stopRealTime()
  })

  return {
    currentTime,
    formattedTime,
    startRealTime,
    stopRealTime
  }
}