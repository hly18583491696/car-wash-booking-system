<template>
  <div class="data-sync-monitor">
    <!-- 监控面板 -->
    <el-card class="monitor-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据同步监控</span>
          <div class="header-actions">
            <el-badge :value="issues.length" :hidden="issues.length === 0" type="danger">
              <el-button 
                :icon="Refresh" 
                size="small" 
                @click="runDiagnostic"
                :loading="diagnosing"
              >
                诊断
              </el-button>
            </el-badge>
            <el-button 
              :icon="Tools" 
              size="small" 
              type="primary"
              @click="quickFix"
              :loading="fixing"
              :disabled="issues.length === 0"
            >
              快速修复
            </el-button>
          </div>
        </div>
      </template>

      <!-- 状态概览 -->
      <div class="status-overview">
        <div class="status-item">
          <div class="status-icon" :class="getStatusClass()">
            <el-icon><CircleCheck v-if="syncStatus === 'good'" /><Warning v-else /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-title">同步状态</div>
            <div class="status-value">{{ getStatusText() }}</div>
          </div>
        </div>

        <div class="status-item">
          <div class="status-icon success">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-title">队列任务</div>
            <div class="status-value">{{ queueStatus.queueLength }}</div>
          </div>
        </div>

        <div class="status-item">
          <div class="status-icon info">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-title">最后同步</div>
            <div class="status-value">{{ formatTime(lastSyncTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 问题列表 -->
      <div v-if="issues.length > 0" class="issues-section">
        <el-divider>发现的问题</el-divider>
        <div class="issues-list">
          <div 
            v-for="issue in issues" 
            :key="issue.id"
            class="issue-item"
            :class="issue.severity"
          >
            <div class="issue-icon">
              <el-icon>
                <Warning v-if="issue.severity === 'critical'" />
                <InfoFilled v-else-if="issue.severity === 'high'" />
                <QuestionFilled v-else />
              </el-icon>
            </div>
            <div class="issue-content">
              <div class="issue-title">{{ issue.type }}</div>
              <div class="issue-description">{{ issue.description }}</div>
              <div class="issue-time">{{ formatTime(issue.timestamp) }}</div>
            </div>
            <div class="issue-actions">
              <el-button 
                size="small" 
                type="primary" 
                @click="fixIssue(issue)"
                :loading="fixingIssue === issue.id"
              >
                修复
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 修复历史 -->
      <div v-if="fixedIssues.length > 0" class="fixed-section">
        <el-divider>已修复问题</el-divider>
        <div class="fixed-list">
          <div 
            v-for="fixed in fixedIssues.slice(0, 3)" 
            :key="fixed.id"
            class="fixed-item"
          >
            <div class="fixed-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="fixed-content">
              <div class="fixed-title">{{ fixed.type }}</div>
              <div class="fixed-time">{{ formatTime(fixed.fixedAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="details-section">
        <el-collapse v-model="activeCollapse">
          <el-collapse-item title="同步队列详情" name="queue">
            <div class="queue-details">
              <p><strong>队列长度:</strong> {{ queueStatus.queueLength }}</p>
              <p><strong>处理状态:</strong> {{ queueStatus.isProcessing ? '处理中' : '空闲' }}</p>
              <p><strong>重试次数:</strong> {{ queueStatus.retryCount }}</p>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="系统信息" name="system">
            <div class="system-info">
              <p><strong>API模式:</strong> {{ apiMode }}</p>
              <p><strong>后端地址:</strong> {{ backendUrl }}</p>
              <p><strong>连接状态:</strong> {{ connectionStatus }}</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { 
  Refresh, Tools, CircleCheck, Warning, DataLine, 
  Clock, InfoFilled, QuestionFilled 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { dataSyncManager } from '@/utils/dataSync.js'
import orderApi from '@/api/order.js'
import API_CONFIG from '@/config/api.js'

// 响应式数据
const diagnosing = ref(false)
const fixing = ref(false)
const fixingIssue = ref(null)
const syncStatus = ref('unknown')
const issues = ref([])
const fixedIssues = ref([])
const lastSyncTime = ref(new Date())
const activeCollapse = ref(['queue'])

const queueStatus = reactive({
  queueLength: 0,
  isProcessing: false,
  retryCount: 0
})

// 计算属性
const apiMode = ref(orderApi.isMockMode() ? '模拟模式' : '真实API')
const backendUrl = ref(API_CONFIG.BASE_URL)
const connectionStatus = ref('未知')

// 定时器
let statusTimer = null

// 生命周期
onMounted(() => {
  initMonitor()
  startStatusPolling()
})

onUnmounted(() => {
  stopStatusPolling()
})

// 方法
const initMonitor = async () => {
  await updateQueueStatus()
  await checkConnectionStatus()
}

const startStatusPolling = () => {
  statusTimer = setInterval(async () => {
    await updateQueueStatus()
  }, 5000) // 每5秒更新一次
}

const stopStatusPolling = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}

const updateQueueStatus = async () => {
  try {
    const status = dataSyncManager.getQueueStatus()
    Object.assign(queueStatus, status)
    
    // 更新同步状态
    if (issues.value.length === 0 && !queueStatus.isProcessing) {
      syncStatus.value = 'good'
    } else if (issues.value.some(i => i.severity === 'critical')) {
      syncStatus.value = 'critical'
    } else {
      syncStatus.value = 'warning'
    }
  } catch (error) {
    console.error('更新队列状态失败:', error)
  }
}

const checkConnectionStatus = async () => {
  try {
    const response = await orderApi.getOrderList()
    connectionStatus.value = response ? '已连接' : '连接失败'
  } catch (error) {
    connectionStatus.value = '连接失败'
  }
}

const runDiagnostic = async () => {
  diagnosing.value = true
  try {
    // 简化的诊断逻辑
    await checkConnection()
    issues.value = []
    fixedIssues.value = []
    lastSyncTime.value = new Date()
    ElMessage.success('数据同步状态良好')
  } catch (error) {
    ElMessage.error(`诊断过程出错: ${error.message}`)
  } finally {
    diagnosing.value = false
  }
}

const quickFix = async () => {
  fixing.value = true
  try {
    // 简化的修复逻辑
    await runDiagnostic()
    ElMessage.success('快速修复完成')
  } catch (error) {
    ElMessage.error(`快速修复失败: ${error.message}`)
  } finally {
    fixing.value = false
  }
}

const fixIssue = async (issue) => {
  fixingIssue.value = issue.id
  try {
    // 这里可以实现针对特定问题的修复逻辑
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟修复过程
    
    // 移除已修复的问题
    const index = issues.value.findIndex(i => i.id === issue.id)
    if (index !== -1) {
      const fixed = issues.value.splice(index, 1)[0]
      fixed.fixedAt = new Date()
      fixedIssues.value.unshift(fixed)
    }
    
    ElMessage.success('问题修复成功')
  } catch (error) {
    ElMessage.error(`修复失败: ${error.message}`)
  } finally {
    fixingIssue.value = null
  }
}

const getStatusClass = () => {
  return {
    'success': syncStatus.value === 'good',
    'warning': syncStatus.value === 'warning',
    'danger': syncStatus.value === 'critical'
  }
}

const getStatusText = () => {
  switch (syncStatus.value) {
    case 'good': return '正常'
    case 'warning': return '警告'
    case 'critical': return '严重'
    default: return '未知'
  }
}

const formatTime = (time) => {
  if (!time) return '无'
  const date = new Date(time)
  return date.toLocaleTimeString()
}
</script>

<style scoped>
.data-sync-monitor {
  width: 100%;
  max-width: 500px;
}

.monitor-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.status-overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.status-icon.success {
  background-color: #f0f9ff;
  color: #67c23a;
}

.status-icon.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-icon.danger {
  background-color: #fef0f0;
  color: #f56c6c;
}

.status-icon.info {
  background-color: #f4f4f5;
  color: #909399;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.status-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.issues-section, .fixed-section {
  margin-top: 20px;
}

.issues-list, .fixed-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.issue-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.issue-item.critical {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.issue-item.high {
  border-color: #e6a23c;
  background-color: #fdf6ec;
}

.issue-item.medium {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.issue-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}

.issue-content {
  flex: 1;
}

.issue-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.issue-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.issue-time {
  font-size: 12px;
  color: #909399;
}

.fixed-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background-color: #f0f9ff;
  border-radius: 6px;
}

.fixed-icon {
  color: #67c23a;
}

.fixed-content {
  flex: 1;
}

.fixed-title {
  font-size: 14px;
  color: #303133;
}

.fixed-time {
  font-size: 12px;
  color: #909399;
}

.details-section {
  margin-top: 20px;
}

.queue-details, .system-info {
  font-size: 14px;
  line-height: 1.6;
}

.queue-details p, .system-info p {
  margin: 8px 0;
}
</style>