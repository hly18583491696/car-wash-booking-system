<template>
  <div class="time-demo">
    <div class="container">
      <h1>统一时间系统演示</h1>
      
      <div class="time-cards">
        <!-- 当前时间显示 -->
        <el-card class="time-card">
          <template #header>
            <h3>当前时间</h3>
          </template>
          <div class="time-info">
            <p><strong>本地时间:</strong> {{ localTime }}</p>
            <p><strong>格式化时间:</strong> {{ formattedTime }}</p>
            <p><strong>相对时间:</strong> {{ relativeTime }}</p>
          </div>
        </el-card>

        <!-- 服务器时间同步 -->
        <el-card class="time-card">
          <template #header>
            <h3>服务器时间同步</h3>
          </template>
          <div class="sync-info">
            <p><strong>同步状态:</strong> 
              <el-tag :type="syncStatus ? 'success' : 'warning'">
                {{ syncStatus ? '已同步' : '未同步' }}
              </el-tag>
            </p>
            <p><strong>最后同步:</strong> {{ lastSyncTime || '从未同步' }}</p>
            <p><strong>时间偏移:</strong> {{ timeOffset }}ms</p>
            <el-button @click="syncTime" :loading="syncing">手动同步</el-button>
          </div>
        </el-card>

        <!-- 时间格式化示例 -->
        <el-card class="time-card">
          <template #header>
            <h3>时间格式化示例</h3>
          </template>
          <div class="format-examples">
            <p><strong>标准格式:</strong> {{ TimeUtils.formatServerTime(currentTime, 'YYYY-MM-DD HH:mm:ss') }}</p>
            <p><strong>日期格式:</strong> {{ TimeUtils.formatDate(currentTime, 'YYYY年MM月DD日') }}</p>
            <p><strong>时间格式:</strong> {{ TimeUtils.formatServerTime(currentTime, 'HH:mm:ss') }}</p>
            <p><strong>ISO格式:</strong> {{ TimeUtils.formatToISO(currentTime) }}</p>
          </div>
        </el-card>
      </div>

      <!-- 订单时间示例 -->
      <el-card class="demo-orders">
        <template #header>
          <h3>订单时间统一显示示例</h3>
        </template>
        <el-table :data="demoOrders" style="width: 100%">
          <el-table-column prop="id" label="订单ID" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ TimeUtils.formatServerTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="180">
            <template #default="{ row }">
              {{ TimeUtils.formatServerTime(row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="relativeTime" label="相对时间">
            <template #default="{ row }">
              {{ TimeUtils.fromNow(row.updateTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { TimeUtils } from '@/utils/timeUtils'
import { timeSyncService } from '@/api/time'
import { ElMessage } from 'element-plus'

// 响应式数据
const localTime = ref('')
const formattedTime = ref('')
const relativeTime = ref('')
const currentTime = ref(TimeUtils.now())
const syncStatus = ref(false)
const syncing = ref(false)
const lastSyncTime = ref('')
const timeOffset = ref(0)

// 演示订单数据
const demoOrders = ref([
  {
    id: 1,
    status: '待确认',
    createTime: TimeUtils.now().subtract(2, 'hour'),
    updateTime: TimeUtils.now().subtract(30, 'minute')
  },
  {
    id: 2,
    status: '已确认',
    createTime: TimeUtils.now().subtract(1, 'day'),
    updateTime: TimeUtils.now().subtract(2, 'hour')
  },
  {
    id: 3,
    status: '已完成',
    createTime: TimeUtils.now().subtract(3, 'day'),
    updateTime: TimeUtils.now().subtract(1, 'day')
  }
])

let timeTimer = null

// 更新时间显示
const updateTimeDisplay = () => {
  const now = TimeUtils.now()
  currentTime.value = now
  localTime.value = now.format('YYYY-MM-DD HH:mm:ss')
  formattedTime.value = TimeUtils.formatServerTime(now)
  relativeTime.value = '当前时间'
}

// 手动同步时间
const syncTime = async () => {
  syncing.value = true
  try {
    const offset = await timeSyncService.syncServerTime()
    syncStatus.value = true
    timeOffset.value = offset
    lastSyncTime.value = TimeUtils.formatServerTime(TimeUtils.now())
    ElMessage.success('时间同步成功')
  } catch (error) {
    ElMessage.error('时间同步失败: ' + error.message)
  } finally {
    syncing.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '待确认': 'warning',
    '已确认': 'primary',
    '已完成': 'success'
  }
  return typeMap[status] || 'info'
}

// 组件挂载
onMounted(() => {
  updateTimeDisplay()
  
  // 每秒更新时间
  timeTimer = setInterval(updateTimeDisplay, 1000)
  
  // 尝试自动同步
  syncTime()
})

// 组件卸载
onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
.time-demo {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h1 {
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 40px;
  font-size: 2.5rem;
}

.time-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.time-card {
  height: fit-content;
}

.time-info, .sync-info, .format-examples {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-info p, .sync-info p, .format-examples p {
  margin: 0;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
}

.demo-orders {
  margin-top: 40px;
}

.el-button {
  margin-top: 16px;
}

.el-tag {
  font-weight: 500;
}

@media (max-width: 768px) {
  .time-cards {
    grid-template-columns: 1fr;
  }
  
  h1 {
    font-size: 2rem;
  }
}
</style>