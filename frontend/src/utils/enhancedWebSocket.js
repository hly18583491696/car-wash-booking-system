/**
 * 增强的WebSocket管理器
 * 支持自动重连、错误处理、状态监控等功能
 */

import { ref, reactive } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

class EnhancedWebSocketManager {
  constructor() {
    this.ws = null
    this.url = ''
    this.userId = null
    this.isConnecting = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.reconnectTimer = null
    
    // 响应式状态
    this.state = reactive({
      connected: false,
      connecting: false,
      error: null,
      lastHeartbeat: null,
      connectionCount: 0,
      messageCount: 0
    })
    
    // 事件监听器
    this.listeners = {
      connection: new Set(),
      message: new Map(),
      error: new Set(),
      close: new Set()
    }
    
    // 消息队列（离线时缓存）
    this.messageQueue = []
    this.maxQueueSize = 100
  }

  /**
   * 连接WebSocket
   */
  connect(userId) {
    if (this.isConnecting || this.state.connected) {
      console.log('WebSocket已连接或正在连接中')
      return Promise.resolve()
    }

    this.userId = userId
    this.url = `ws://localhost:8080/ws/order-status?userId=${userId}`
    this.isConnecting = true
    this.state.connecting = true
    this.state.error = null

    return new Promise((resolve, reject) => {
      try {
        console.log(`🔌 连接WebSocket: ${this.url}`)
        
        this.ws = new WebSocket(this.url)
        
        // 连接超时处理
        const connectTimeout = setTimeout(() => {
          if (this.ws.readyState === WebSocket.CONNECTING) {
            this.ws.close()
            reject(new Error('连接超时'))
          }
        }, 10000)

        this.ws.onopen = (event) => {
          clearTimeout(connectTimeout)
          this.isConnecting = false
          this.state.connecting = false
          this.state.connected = true
          this.state.connectionCount++
          this.reconnectAttempts = 0
          
          console.log('✅ WebSocket连接成功')
          
          // 发送连接确认消息
          this.sendMessage({
            type: 'connection',
            data: { userId: this.userId, timestamp: Date.now() }
          })
          
          // 启动心跳
          this.startHeartbeat()
          
          // 处理离线期间的消息队列
          this.processMessageQueue()
          
          // 通知监听器
          this.notifyListeners('connection', { connected: true, event })
          
          // 显示连接成功通知
          ElNotification({
            title: 'WebSocket连接成功',
            message: '实时状态同步已启用',
            type: 'success',
            duration: 2000,
            position: 'bottom-right'
          })
          
          resolve()
        }

        this.ws.onmessage = (event) => {
          this.state.messageCount++
          
          try {
            const message = JSON.parse(event.data)
            console.log('📨 收到WebSocket消息:', message)
            
            // 处理心跳响应
            if (message.type === 'pong') {
              this.state.lastHeartbeat = new Date()
              return
            }
            
            // 处理订单状态更新
            if (message.type === 'order_status_update') {
              this.handleOrderStatusUpdate(message.data)
            }
            
            // 通知消息监听器
            this.notifyMessageListeners(message.type, message.data)
            
          } catch (error) {
            console.error('❌ 解析WebSocket消息失败:', error, event.data)
          }
        }

        this.ws.onclose = (event) => {
          clearTimeout(connectTimeout)
          this.isConnecting = false
          this.state.connecting = false
          this.state.connected = false
          this.stopHeartbeat()
          
          console.log('🔌 WebSocket连接关闭:', event.code, event.reason)
          
          // 通知监听器
          this.notifyListeners('close', { event })
          
          // 如果不是主动关闭，尝试重连
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.scheduleReconnect()
          } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            this.state.error = '连接失败次数过多，请刷新页面重试'
            ElMessage.error('WebSocket连接失败，请刷新页面重试')
          }
        }

        this.ws.onerror = (error) => {
          clearTimeout(connectTimeout)
          console.error('❌ WebSocket错误:', error)
          
          this.state.error = '连接错误'
          this.notifyListeners('error', { error })
          
          if (this.isConnecting) {
            reject(error)
          }
        }

      } catch (error) {
        this.isConnecting = false
        this.state.connecting = false
        this.state.error = error.message
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    console.log('🔌 主动断开WebSocket连接')
    
    this.stopHeartbeat()
    this.clearReconnectTimer()
    
    if (this.ws) {
      this.ws.close(1000, '主动断开')
      this.ws = null
    }
    
    this.state.connected = false
    this.state.connecting = false
    this.isConnecting = false
  }

  /**
   * 发送消息
   */
  sendMessage(message) {
    if (!this.state.connected || !this.ws) {
      console.warn('⚠️ WebSocket未连接，消息已加入队列:', message)
      
      // 添加到消息队列
      if (this.messageQueue.length < this.maxQueueSize) {
        this.messageQueue.push({
          message,
          timestamp: Date.now()
        })
      }
      return false
    }

    try {
      const messageStr = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(messageStr)
      console.log('📤 发送WebSocket消息:', message)
      return true
    } catch (error) {
      console.error('❌ 发送WebSocket消息失败:', error)
      return false
    }
  }

  /**
   * 处理消息队列
   */
  processMessageQueue() {
    if (this.messageQueue.length === 0) return
    
    console.log(`📦 处理离线消息队列，共 ${this.messageQueue.length} 条消息`)
    
    const messages = [...this.messageQueue]
    this.messageQueue = []
    
    messages.forEach(({ message }) => {
      this.sendMessage(message)
    })
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    
    this.heartbeatTimer = setInterval(() => {
      if (this.state.connected) {
        this.sendMessage('ping')
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 安排重连
   */
  scheduleReconnect() {
    this.clearReconnectTimer()
    
    this.reconnectAttempts++
    const delay = Math.min(this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1), 30000)
    
    console.log(`🔄 ${delay/1000}秒后尝试第${this.reconnectAttempts}次重连...`)
    
    ElMessage({
      type: 'warning',
      message: `连接断开，${delay/1000}秒后自动重连...`,
      duration: 3000
    })
    
    this.reconnectTimer = setTimeout(() => {
      if (this.userId) {
        this.connect(this.userId).catch(error => {
          console.error('❌ 重连失败:', error)
        })
      }
    }, delay)
  }

  /**
   * 清除重连定时器
   */
  clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  /**
   * 处理订单状态更新
   */
  handleOrderStatusUpdate(data) {
    const { orderId, orderNo, oldStatus, newStatus, updateTime, updateReason } = data
    
    console.log(`🔄 订单状态更新: ${orderNo} (${orderId}) ${oldStatus} → ${newStatus}`)
    
    // 显示状态更新通知
    const statusText = this.getStatusText(newStatus)
    ElNotification({
      title: '订单状态更新',
      message: `订单 ${orderNo} 状态已更新为：${statusText}`,
      type: 'info',
      duration: 4000,
      position: 'bottom-right'
    })

    // 触发全局事件
    window.dispatchEvent(new CustomEvent('orderStatusUpdate', {
      detail: data
    }))
  }

  /**
   * 获取状态文本
   */
  getStatusText(status) {
    const textMap = {
      'pending': '待处理',
      'confirmed': '已确认',
      'in_progress': '处理中',
      'completed': '已完成',
      'cancelled': '已取消',
      'failed': '处理失败'
    }
    return textMap[status] || status
  }

  /**
   * 添加连接状态监听器
   */
  onConnectionChange(callback) {
    this.listeners.connection.add(callback)
    
    // 返回取消监听的函数
    return () => {
      this.listeners.connection.delete(callback)
    }
  }

  /**
   * 添加消息监听器
   */
  onMessage(type, callback) {
    if (!this.listeners.message.has(type)) {
      this.listeners.message.set(type, new Set())
    }
    
    this.listeners.message.get(type).add(callback)
    
    // 返回取消监听的函数
    return () => {
      const typeListeners = this.listeners.message.get(type)
      if (typeListeners) {
        typeListeners.delete(callback)
        if (typeListeners.size === 0) {
          this.listeners.message.delete(type)
        }
      }
    }
  }

  /**
   * 添加错误监听器
   */
  onError(callback) {
    this.listeners.error.add(callback)
    
    return () => {
      this.listeners.error.delete(callback)
    }
  }

  /**
   * 通知连接监听器
   */
  notifyListeners(type, data) {
    const listeners = this.listeners[type]
    if (listeners) {
      listeners.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`❌ 监听器回调错误 (${type}):`, error)
        }
      })
    }
  }

  /**
   * 通知消息监听器
   */
  notifyMessageListeners(type, data) {
    const typeListeners = this.listeners.message.get(type)
    if (typeListeners) {
      typeListeners.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`❌ 消息监听器回调错误 (${type}):`, error)
        }
      })
    }
  }

  /**
   * 获取连接状态
   */
  getConnectionStatus() {
    if (this.state.connected) return 'connected'
    if (this.state.connecting) return 'connecting'
    if (this.state.error) return 'error'
    return 'disconnected'
  }

  /**
   * 获取统计信息
   */
  getStats() {
    return {
      connected: this.state.connected,
      connectionCount: this.state.connectionCount,
      messageCount: this.state.messageCount,
      reconnectAttempts: this.reconnectAttempts,
      queueSize: this.messageQueue.length,
      lastHeartbeat: this.state.lastHeartbeat
    }
  }

  /**
   * 手动重连
   */
  reconnect() {
    if (this.state.connected) {
      this.disconnect()
    }
    
    this.reconnectAttempts = 0
    this.clearReconnectTimer()
    
    if (this.userId) {
      return this.connect(this.userId)
    }
    
    return Promise.reject(new Error('用户ID未设置'))
  }
}

// 创建全局实例
const enhancedWebSocketManager = new EnhancedWebSocketManager()

// Vue组合式API
export function useEnhancedWebSocket() {
  const userStore = useUserStore()

  const connect = () => {
    // 修复：isLoggedIn是函数，需要调用；user应该是userInfo
    if (userStore.isLoggedIn() && userStore.userInfo?.id) {
      return enhancedWebSocketManager.connect(userStore.userInfo.id)
    }
    return Promise.reject(new Error('用户未登录'))
  }

  const disconnect = () => {
    enhancedWebSocketManager.disconnect()
  }

  const sendMessage = (message) => {
    return enhancedWebSocketManager.sendMessage(message)
  }

  const onOrderStatusUpdate = (callback) => {
    return enhancedWebSocketManager.onMessage('order_status_update', callback)
  }

  const onConnectionChange = (callback) => {
    return enhancedWebSocketManager.onConnectionChange(callback)
  }

  const getState = () => enhancedWebSocketManager.state
  const getStats = () => enhancedWebSocketManager.getStats()
  const reconnect = () => enhancedWebSocketManager.reconnect()

  return {
    connect,
    disconnect,
    sendMessage,
    onOrderStatusUpdate,
    onConnectionChange,
    getState,
    getStats,
    reconnect,
    isConnected: () => enhancedWebSocketManager.state.connected,
    getConnectionStatus: () => enhancedWebSocketManager.getConnectionStatus()
  }
}

export default enhancedWebSocketManager