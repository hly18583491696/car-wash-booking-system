<template>
  <div class="order-status-progress">
    <!-- 状态头部 -->
    <div class="status-header">
      <div class="status-info">
        <el-tag :type="getStatusType(order.status)" size="large" class="status-tag">
          <el-icon class="status-icon">
            <component :is="getStatusIcon(order.status)" />
          </el-icon>
          {{ getStatusText(order.status) }}
        </el-tag>
        <span v-if="order.progress && order.status === 'in_progress'" class="progress-text">
          {{ order.progress }}%
        </span>
      </div>
      
      <div class="status-actions">
        <el-button 
          :icon="Refresh" 
          size="small" 
          circle 
          @click="refreshStatus"
          :loading="refreshing"
          title="刷新状态"
        />
        <span v-if="lastUpdateTime" class="last-update">
          {{ formatUpdateTime(lastUpdateTime) }}
        </span>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-section">
      <!-- 步骤进度条 -->
      <el-steps 
        :active="getProgressStep(order.status)" 
        :finish-status="getFinishStatus(order.status)"
        simple
        class="order-steps"
      >
        <el-step title="订单提交" :icon="DocumentAdd">
          <template #description>
            <span class="step-time">{{ formatStepTime(order.createTime) }}</span>
          </template>
        </el-step>
        <el-step title="确认预约" :icon="Select">
          <template #description>
            <span class="step-time">{{ formatStepTime(order.confirmTime) }}</span>
          </template>
        </el-step>
        <el-step title="开始服务" :icon="Tools">
          <template #description>
            <span class="step-time">{{ formatStepTime(order.startTime) }}</span>
          </template>
        </el-step>
        <el-step title="服务完成" :icon="CircleCheck">
          <template #description>
            <span class="step-time">{{ formatStepTime(order.completeTime) }}</span>
          </template>
        </el-step>
      </el-steps>

      <!-- 详细进度条（处理中状态） -->
      <div v-if="order.status === 'in_progress' && order.progress !== undefined" class="detailed-progress">
        <el-progress 
          :percentage="order.progress" 
          :status="getProgressStatus(order.progress)"
          :stroke-width="8"
          :show-text="true"
        >
          <template #default="{ percentage }">
            <span class="progress-label">{{ percentage }}%</span>
          </template>
        </el-progress>
        <div class="progress-description">
          {{ getProgressDescription(order.progress) }}
        </div>
      </div>
    </div>

    <!-- 状态详情 -->
    <div v-if="showDetails" class="status-details">
      <div class="detail-item" v-if="order.updateReason">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ order.updateReason }}</span>
      </div>
      <div class="detail-item" v-if="order.estimatedTime">
        <el-icon><Clock /></el-icon>
        <span>预计完成时间：{{ formatDateTime(order.estimatedTime) }}</span>
      </div>
      <div class="detail-item" v-if="order.serviceProvider">
        <el-icon><User /></el-icon>
        <span>服务人员：{{ order.serviceProvider }}</span>
      </div>
    </div>

    <!-- 错误状态显示 -->
    <div v-if="error" class="error-state">
      <el-alert
        :title="error.title || '状态更新失败'"
        :description="error.message"
        type="error"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-button size="small" type="primary" @click="retryUpdate">
            重试
          </el-button>
        </template>
      </el-alert>
    </div>

    <!-- 网络状态指示器 -->
    <div class="network-status" :class="{ 'offline': !isOnline }">
      <el-icon v-if="isOnline" class="online-icon"><Connection /></el-icon>
      <el-icon v-else class="offline-icon"><Close /></el-icon>
      <span class="status-text">{{ isOnline ? '实时同步' : '离线模式' }}</span>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, DocumentAdd, Select, Tools, CircleCheck, 
  InfoFilled, Clock, User, Connection, Close 
} from '@element-plus/icons-vue'
import { TimeUtils } from '@/utils/timeUtils'
import { useWebSocket } from '@/composables/useWebSocket'

export default {
  name: 'OrderStatusProgress',
  components: {
    Refresh, DocumentAdd, Select, Tools, CircleCheck,
    InfoFilled, Clock, User, Connection, Close
  },
  props: {
    order: {
      type: Object,
      required: true
    },
    showDetails: {
      type: Boolean,
      default: false
    },
    autoRefresh: {
      type: Boolean,
      default: true
    },
    refreshInterval: {
      type: Number,
      default: 8000 // 8秒
    }
  },
  emits: ['status-updated', 'refresh-requested'],
  setup(props, { emit }) {
    const refreshing = ref(false)
    const lastUpdateTime = ref(null)
    const error = ref(null)
    const isOnline = ref(navigator.onLine)
    const autoRefreshTimer = ref(null)

    const { onOrderStatusUpdate, isConnected } = useWebSocket()

    // 监听WebSocket订单状态更新
    let unsubscribeWebSocket = null

    // 获取状态类型
    const getStatusType = (status) => {
      const typeMap = {
        'pending': 'warning',
        'confirmed': 'primary',
        'in_progress': 'success',
        'completed': 'success',
        'cancelled': 'danger',
        'failed': 'danger'
      }
      return typeMap[status] || 'info'
    }

    // 获取状态图标
    const getStatusIcon = (status) => {
      const iconMap = {
        'pending': 'Clock',
        'confirmed': 'Select',
        'in_progress': 'Tools',
        'completed': 'CircleCheck',
        'cancelled': 'Close',
        'failed': 'Close'
      }
      return iconMap[status] || 'InfoFilled'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'pending': '待处理',
        'confirmed': '已确认',
        'in_progress': '处理中',
        'completed': '已完成',
        'cancelled': '已取消',
        'failed': '处理失败'
      }
      return textMap[status] || '未知状态'
    }

    // 获取进度步骤
    const getProgressStep = (status) => {
      const stepMap = {
        'pending': 0,
        'confirmed': 1,
        'in_progress': 2,
        'completed': 3,
        'cancelled': 0,
        'failed': 2
      }
      return stepMap[status] || 0
    }

    // 获取完成状态
    const getFinishStatus = (status) => {
      if (status === 'completed') return 'success'
      if (status === 'cancelled' || status === 'failed') return 'error'
      return 'process'
    }

    // 获取进度状态
    const getProgressStatus = (progress) => {
      if (progress >= 100) return 'success'
      if (progress >= 80) return 'warning'
      return undefined
    }

    // 获取进度描述
    const getProgressDescription = (progress) => {
      if (progress < 25) return '准备阶段'
      if (progress < 50) return '清洗中'
      if (progress < 75) return '冲洗中'
      if (progress < 100) return '整理中'
      return '即将完成'
    }

    // 格式化步骤时间
    const formatStepTime = (time) => {
      if (!time) return ''
      return TimeUtils.formatServerTime(time, 'MM-DD HH:mm')
    }

    // 格式化更新时间
    const formatUpdateTime = (time) => {
      if (!time) return ''
      const now = new Date()
      const updateTime = new Date(time)
      const diff = Math.floor((now - updateTime) / 1000)
      
      if (diff < 60) return '刚刚更新'
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
      if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
      return TimeUtils.formatServerTime(time, 'MM-DD HH:mm')
    }

    // 格式化日期时间
    const formatDateTime = (time) => {
      if (!time) return ''
      return TimeUtils.formatServerTime(time, 'YYYY-MM-DD HH:mm')
    }

    // 刷新状态
    const refreshStatus = async () => {
      refreshing.value = true
      error.value = null
      
      try {
        emit('refresh-requested', props.order.id)
        lastUpdateTime.value = new Date()
        
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 500))
        
      } catch (err) {
        error.value = {
          title: '刷新失败',
          message: err.message || '无法获取最新状态，请检查网络连接'
        }
      } finally {
        refreshing.value = false
      }
    }

    // 重试更新
    const retryUpdate = () => {
      error.value = null
      refreshStatus()
    }

    // 启动自动刷新
    const startAutoRefresh = () => {
      if (!props.autoRefresh) return
      
      stopAutoRefresh()
      autoRefreshTimer.value = setInterval(() => {
        if (props.order.status === 'in_progress' && isOnline.value) {
          refreshStatus()
        }
      }, props.refreshInterval)
    }

    // 停止自动刷新
    const stopAutoRefresh = () => {
      if (autoRefreshTimer.value) {
        clearInterval(autoRefreshTimer.value)
        autoRefreshTimer.value = null
      }
    }

    // 处理网络状态变化
    const handleOnline = () => {
      isOnline.value = true
      error.value = null
      if (props.autoRefresh) {
        startAutoRefresh()
      }
    }

    const handleOffline = () => {
      isOnline.value = false
      stopAutoRefresh()
    }

    // 监听订单状态变化
    watch(() => props.order.status, (newStatus, oldStatus) => {
      if (newStatus !== oldStatus) {
        lastUpdateTime.value = new Date()
        emit('status-updated', {
          orderId: props.order.id,
          oldStatus,
          newStatus
        })
        
        // 根据状态调整自动刷新
        if (newStatus === 'in_progress') {
          startAutoRefresh()
        } else {
          stopAutoRefresh()
        }
      }
    })

    onMounted(() => {
      // 监听WebSocket订单状态更新
      unsubscribeWebSocket = onOrderStatusUpdate((data) => {
        if (data.orderId === props.order.id) {
          lastUpdateTime.value = new Date()
          
          ElMessage({
            type: 'success',
            message: `订单状态已更新为：${getStatusText(data.newStatus)}`,
            duration: 3000
          })
        }
      })

      // 监听网络状态
      window.addEventListener('online', handleOnline)
      window.addEventListener('offline', handleOffline)

      // 启动自动刷新
      startAutoRefresh()
    })

    onUnmounted(() => {
      // 清理WebSocket监听
      if (unsubscribeWebSocket) {
        unsubscribeWebSocket()
      }

      // 清理网络状态监听
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)

      // 停止自动刷新
      stopAutoRefresh()
    })

    return {
      refreshing,
      lastUpdateTime,
      error,
      isOnline,
      getStatusType,
      getStatusIcon,
      getStatusText,
      getProgressStep,
      getFinishStatus,
      getProgressStatus,
      getProgressDescription,
      formatStepTime,
      formatUpdateTime,
      formatDateTime,
      refreshStatus,
      retryUpdate,
      // Expose icons to the template
      Refresh, DocumentAdd, Select, Tools, CircleCheck,
      InfoFilled, Clock, User, Connection, Close
    }
  }
}
</script>

<style scoped>
.order-status-progress {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  position: relative;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-tag {
  font-size: 14px;
  padding: 8px 12px;
}

.status-icon {
  margin-right: 4px;
}

.progress-text {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.status-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.last-update {
  font-size: 12px;
  color: #909399;
}

.progress-section {
  margin-bottom: 16px;
}

.order-steps {
  margin-bottom: 16px;
}

.step-time {
  font-size: 12px;
  color: #909399;
}

.detailed-progress {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  margin-top: 16px;
}

.progress-label {
  font-weight: 600;
  color: #409eff;
}

.progress-description {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.status-details {
  background: #f9f9f9;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.error-state {
  margin-bottom: 12px;
}

.network-status {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  transition: all 0.3s;
}

.network-status.offline {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.online-icon {
  color: #67c23a;
}

.offline-icon {
  color: #f56c6c;
}

.status-text {
  font-size: 11px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .status-actions {
    align-self: flex-end;
  }
  
  .network-status {
    position: static;
    align-self: flex-end;
    margin-top: 8px;
  }
}
</style>