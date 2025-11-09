import { ref, onMounted, onUnmounted } from 'vue'
import webSocketManager from '@/utils/websocket'
import { useUserStore } from '@/stores/user'

export function useWebSocket() {
  const isConnected = ref(false)
  const connectionStatus = ref('disconnected')
  
  let connectionUnsubscribe = null
  let messageUnsubscribers = []

  // 初始化WebSocket连接
  const initWebSocket = () => {
    const userStore = useUserStore()
    
    // 只有在用户已登录时才连接WebSocket
    if (userStore.isLoggedIn) {
      webSocketManager.connect()
      
      // 监听连接状态变化
      connectionUnsubscribe = webSocketManager.onConnectionChange((connected) => {
        isConnected.value = connected
        connectionStatus.value = webSocketManager.getConnectionStatus()
      })
      
      // 更新初始状态
      isConnected.value = webSocketManager.isConnected()
      connectionStatus.value = webSocketManager.getConnectionStatus()
    }
  }

  // 断开WebSocket连接
  const disconnectWebSocket = () => {
    webSocketManager.disconnect()
    isConnected.value = false
    connectionStatus.value = 'disconnected'
  }

  // 发送消息
  const sendMessage = (message) => {
    return webSocketManager.send(message)
  }

  // 监听特定类型的消息
  const onMessage = (type, handler) => {
    const unsubscribe = webSocketManager.onMessage(type, handler)
    messageUnsubscribers.push(unsubscribe)
    return unsubscribe
  }

  // 监听订单状态更新
  const onOrderStatusUpdate = (handler) => {
    return onMessage('order_status_update', handler)
  }

  // 清理资源
  const cleanup = () => {
    // 取消消息监听
    messageUnsubscribers.forEach(unsubscribe => unsubscribe())
    messageUnsubscribers = []
    
    // 取消连接状态监听
    if (connectionUnsubscribe) {
      connectionUnsubscribe()
      connectionUnsubscribe = null
    }
  }

  return {
    isConnected,
    connectionStatus,
    initWebSocket,
    disconnectWebSocket,
    sendMessage,
    onMessage,
    onOrderStatusUpdate,
    cleanup
  }
}

// 全局WebSocket管理
export function useGlobalWebSocket() {
  const { initWebSocket, disconnectWebSocket, cleanup } = useWebSocket()
  
  onMounted(() => {
    initWebSocket()
  })
  
  onUnmounted(() => {
    cleanup()
  })

  return {
    initWebSocket,
    disconnectWebSocket
  }
}