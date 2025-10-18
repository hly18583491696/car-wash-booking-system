<template>
  <teleport to="body">
    <div class="notification-container">
      <transition-group name="notification" tag="div">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification"
          :class="[
            `notification-${notification.type}`,
            { 'notification-closable': notification.closable }
          ]"
          @click="handleClick(notification)"
        >
          <div class="notification-icon">
            <el-icon>
              <component :is="getIcon(notification.type)" />
            </el-icon>
          </div>
          
          <div class="notification-content">
            <div v-if="notification.title" class="notification-title">
              {{ notification.title }}
            </div>
            <div class="notification-message">
              {{ notification.message }}
            </div>
          </div>
          
          <button
            v-if="notification.closable"
            class="notification-close"
            @click.stop="removeNotification(notification.id)"
          >
            <el-icon><Close /></el-icon>
          </button>
          
          <div
            v-if="notification.duration > 0"
            class="notification-progress"
            :style="{ animationDuration: `${notification.duration}ms` }"
          ></div>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'NotificationSystem',
  setup() {
    const notifications = ref([])
    let notificationId = 0
    
    const getIcon = (type) => {
      const icons = {
        success: 'CircleCheck',
        error: 'CircleClose',
        warning: 'Warning',
        info: 'InfoFilled'
      }
      return icons[type] || 'InfoFilled'
    }
    
    const addNotification = (options) => {
      const notification = {
        id: ++notificationId,
        type: options.type || 'info',
        title: options.title,
        message: options.message,
        duration: options.duration !== undefined ? options.duration : 4000,
        closable: options.closable !== false,
        onClick: options.onClick,
        ...options
      }
      
      notifications.value.push(notification)
      
      // 自动移除
      if (notification.duration > 0) {
        setTimeout(() => {
          removeNotification(notification.id)
        }, notification.duration)
      }
      
      return notification.id
    }
    
    const removeNotification = (id) => {
      const index = notifications.value.findIndex(n => n.id === id)
      if (index > -1) {
        notifications.value.splice(index, 1)
      }
    }
    
    const clearAll = () => {
      notifications.value = []
    }
    
    const handleClick = (notification) => {
      if (notification.onClick) {
        notification.onClick(notification)
      }
    }
    
    // 全局方法
    const showSuccess = (message, options = {}) => {
      return addNotification({ ...options, type: 'success', message })
    }
    
    const showError = (message, options = {}) => {
      return addNotification({ ...options, type: 'error', message })
    }
    
    const showWarning = (message, options = {}) => {
      return addNotification({ ...options, type: 'warning', message })
    }
    
    const showInfo = (message, options = {}) => {
      return addNotification({ ...options, type: 'info', message })
    }
    
    // 监听全局事件
    const handleGlobalNotification = (event) => {
      addNotification(event.detail)
    }
    
    onMounted(() => {
      window.addEventListener('show-notification', handleGlobalNotification)
      
      // 添加到全局对象
      window.$notify = {
        success: showSuccess,
        error: showError,
        warning: showWarning,
        info: showInfo,
        add: addNotification,
        remove: removeNotification,
        clear: clearAll
      }
    })
    
    onUnmounted(() => {
      window.removeEventListener('show-notification', handleGlobalNotification)
      delete window.$notify
    })
    
    return {
      notifications,
      getIcon,
      removeNotification,
      handleClick
    }
  }
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  pointer-events: none;
}

.notification {
  width: 320px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  padding: 16px;
  position: relative;
  overflow: hidden;
  pointer-events: auto;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification:hover {
  transform: translateX(-4px);
  box-shadow: var(--shadow-xl);
}

.notification-success {
  border-left: 4px solid var(--success-color);
}

.notification-error {
  border-left: 4px solid var(--error-color);
}

.notification-warning {
  border-left: 4px solid var(--warning-color);
}

.notification-info {
  border-left: 4px solid var(--info-color);
}

.notification-icon {
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-success .notification-icon {
  color: var(--success-color);
}

.notification-error .notification-icon {
  color: var(--error-color);
}

.notification-warning .notification-icon {
  color: var(--warning-color);
}

.notification-info .notification-icon {
  color: var(--info-color);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-size: 14px;
}

.notification-message {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.4;
  word-wrap: break-word;
}

.notification-close {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 0;
  margin-left: 8px;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.notification-close:hover {
  color: var(--text-primary);
}

.notification-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background: currentColor;
  animation: progress-bar linear forwards;
  opacity: 0.6;
}

.notification-success .notification-progress {
  background: var(--success-color);
}

.notification-error .notification-progress {
  background: var(--error-color);
}

.notification-warning .notification-progress {
  background: var(--warning-color);
}

.notification-info .notification-progress {
  background: var(--info-color);
}

/* 动画 */
.notification-enter-active {
  transition: all 0.3s ease;
}

.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

@keyframes progress-bar {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notification-container {
    top: 10px;
    right: 10px;
    left: 10px;
  }
  
  .notification {
    width: auto;
    margin-bottom: 8px;
  }
}
</style>