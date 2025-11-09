<template>
  <div class="api-test-container">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <h2>前后端连接测试</h2>
          <el-tag :type="connectionStatus === 'success' ? 'success' : 'danger'">
            {{ connectionStatus === 'success' ? '连接正常' : '连接异常' }}
          </el-tag>
        </div>
      </template>

      <div class="test-section">
        <h3>API 配置信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="API 模式">
            <el-tag :type="useRealApi ? 'success' : 'warning'">
              {{ useRealApi ? '真实API' : 'Mock API' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="基础URL">{{ baseURL }}</el-descriptions-item>
          <el-descriptions-item label="超时时间">10秒</el-descriptions-item>
          <el-descriptions-item label="认证方式">JWT Bearer Token</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="test-section">
        <h3>连接测试</h3>
        <div class="test-buttons">
          <el-button type="primary" @click="testHealth" :loading="loading.health">
            健康检查
          </el-button>
          <el-button type="info" @click="testSystemInfo" :loading="loading.info">
            系统信息
          </el-button>
          <el-button type="warning" @click="testCors" :loading="loading.cors">
            CORS测试
          </el-button>
          <el-button type="success" @click="testRedis" :loading="loading.redis">
            Redis测试
          </el-button>
          <el-button type="danger" @click="testDatabase" :loading="loading.database">
            数据库测试
          </el-button>
        </div>
      </div>

      <div class="test-section" v-if="testResults.length > 0">
        <h3>测试结果</h3>
        <div class="results-container">
          <el-timeline>
            <el-timeline-item
              v-for="(result, index) in testResults"
              :key="index"
              :timestamp="result.timestamp"
              :type="result.success ? 'success' : 'danger'"
            >
              <el-card class="result-card">
                <div class="result-header">
                  <strong>{{ result.title }}</strong>
                  <el-tag :type="result.success ? 'success' : 'danger'" size="small">
                    {{ result.success ? '成功' : '失败' }}
                  </el-tag>
                </div>
                <div class="result-content">
                  <p><strong>状态码:</strong> {{ result.code }}</p>
                  <p><strong>消息:</strong> {{ result.message }}</p>
                  <div v-if="result.data" class="result-data">
                    <p><strong>数据:</strong></p>
                    <pre>{{ JSON.stringify(result.data, null, 2) }}</pre>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import realApi from '@/api/realApi.js'

// 响应式数据
const connectionStatus = ref('unknown')
const testResults = ref([])
const loading = reactive({
  health: false,
  info: false,
  cors: false,
  redis: false,
  database: false
})

// 计算属性
const useRealApi = computed(() => {
  return import.meta.env.VITE_USE_REAL_API === 'true'
})

const baseURL = computed(() => {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
})

// 添加测试结果
const addTestResult = (title, success, code, message, data = null) => {
  const result = {
    title,
    success,
    code,
    message,
    data,
    timestamp: new Date().toLocaleString()
  }
  testResults.value.unshift(result)
  
  // 更新连接状态
  if (success && title === '健康检查') {
    connectionStatus.value = 'success'
  } else if (!success && title === '健康检查') {
    connectionStatus.value = 'error'
  }
}

// 健康检查
const testHealth = async () => {
  loading.health = true
  try {
    const response = await realApi.healthCheck()
    addTestResult('健康检查', true, response.code, response.message, response.data)
    ElMessage.success('健康检查通过')
  } catch (error) {
    addTestResult('健康检查', false, error.response?.status || 0, error.message)
    ElMessage.error('健康检查失败: ' + error.message)
  } finally {
    loading.health = false
  }
}

// 系统信息测试
const testSystemInfo = async () => {
  loading.info = true
  try {
    const response = await realApi.getSystemInfo()
    addTestResult('系统信息', true, response.code, response.message, response.data)
    ElMessage.success('获取系统信息成功')
  } catch (error) {
    addTestResult('系统信息', false, error.response?.status || 0, error.message)
    ElMessage.error('获取系统信息失败: ' + error.message)
  } finally {
    loading.info = false
  }
}

// CORS测试
const testCors = async () => {
  loading.cors = true
  try {
    // 这里可以添加CORS特定的测试逻辑
    const response = await realApi.healthCheck()
    addTestResult('CORS测试', true, response.code, 'CORS配置正常，跨域请求成功')
    ElMessage.success('CORS测试通过')
  } catch (error) {
    addTestResult('CORS测试', false, error.response?.status || 0, 'CORS配置异常: ' + error.message)
    ElMessage.error('CORS测试失败: ' + error.message)
  } finally {
    loading.cors = false
  }
}

// Redis测试
const testRedis = async () => {
  loading.redis = true
  try {
    const response = await realApi.testRedis()
    addTestResult('Redis测试', true, response.code, response.message, response.data)
    ElMessage.success('Redis连接正常')
  } catch (error) {
    addTestResult('Redis测试', false, error.response?.status || 0, error.message)
    ElMessage.error('Redis测试失败: ' + error.message)
  } finally {
    loading.redis = false
  }
}

// 数据库测试
const testDatabase = async () => {
  loading.database = true
  try {
    const response = await realApi.testDatabase()
    addTestResult('数据库测试', true, response.code, response.message, response.data)
    ElMessage.success('数据库连接正常')
  } catch (error) {
    addTestResult('数据库测试', false, error.response?.status || 0, error.message)
    ElMessage.error('数据库测试失败: ' + error.message)
  } finally {
    loading.database = false
  }
}

// 页面加载时自动进行健康检查
onMounted(() => {
  setTimeout(() => {
    testHealth()
  }, 1000)
})
</script>

<style scoped>
.api-test-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.test-section {
  margin: 20px 0;
}

.test-section h3 {
  color: #606266;
  margin-bottom: 15px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
}

.test-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.results-container {
  max-height: 600px;
  overflow-y: auto;
}

.result-card {
  margin-bottom: 10px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-content p {
  margin: 5px 0;
  color: #606266;
}

.result-data {
  margin-top: 10px;
}

.result-data pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .test-buttons {
    flex-direction: column;
  }
  
  .test-buttons .el-button {
    width: 100%;
  }
}
</style>