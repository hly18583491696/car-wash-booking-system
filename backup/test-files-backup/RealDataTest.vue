<template>
  <div class="real-data-test">
    <div class="container">
      <h1>前后端真实数据连接测试</h1>
      
      <!-- 连接状态 -->
      <div class="status-section">
        <h2>连接状态</h2>
        <div class="status-grid">
          <div class="status-card" :class="{ success: backendStatus, error: !backendStatus }">
            <h3>后端服务</h3>
            <p>{{ backendStatus ? '已连接' : '未连接' }}</p>
            <el-button @click="testBackendConnection" :loading="testing">测试连接</el-button>
          </div>
          
          <div class="status-card" :class="{ success: databaseStatus, error: !databaseStatus }">
            <h3>数据库</h3>
            <p>{{ databaseStatus ? '已连接' : '未连接' }}</p>
            <el-button @click="testDatabaseConnection" :loading="testing">测试数据库</el-button>
          </div>
        </div>
      </div>

      <!-- 用户认证测试 -->
      <div class="test-section">
        <h2>用户认证测试</h2>
        <div class="test-form">
          <el-form :model="loginForm" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="admin 或 user001" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="admin123 或 user123" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="testLogin" :loading="loginTesting">测试登录</el-button>
              <el-button @click="testGetUserInfo" :loading="userInfoTesting">获取用户信息</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="test-result" v-if="loginResult">
          <h3>登录结果</h3>
          <pre>{{ JSON.stringify(loginResult, null, 2) }}</pre>
        </div>
      </div>

      <!-- 数据库数据测试 -->
      <div class="test-section">
        <h2>数据库数据测试</h2>
        <div class="data-buttons">
          <el-button @click="testUsers" :loading="loadingUsers">获取用户列表</el-button>
          <el-button @click="testServices" :loading="loadingServices">获取服务列表</el-button>
          <el-button @click="testBookings" :loading="loadingBookings">获取预约列表</el-button>
          <el-button @click="testStatistics" :loading="loadingStats">获取统计数据</el-button>
        </div>
        
        <!-- 用户数据 -->
        <div class="data-result" v-if="usersData">
          <h3>用户数据 ({{ usersData.length }} 条记录)</h3>
          <el-table :data="usersData" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </div>

        <!-- 服务数据 -->
        <div class="data-result" v-if="servicesData">
          <h3>服务数据 ({{ servicesData.length }} 条记录)</h3>
          <el-table :data="servicesData" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="服务名称" />
            <el-table-column prop="price" label="价格" />
            <el-table-column prop="duration" label="时长(分钟)" />
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </div>

        <!-- 预约数据 -->
        <div class="data-result" v-if="bookingsData">
          <h3>预约数据 ({{ bookingsData.length }} 条记录)</h3>
          <el-table :data="bookingsData" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column prop="bookingDate" label="预约日期" />
            <el-table-column prop="bookingTime" label="预约时间" />
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="totalPrice" label="总价" />
          </el-table>
        </div>

        <!-- 统计数据 -->
        <div class="data-result" v-if="statisticsData">
          <h3>统计数据</h3>
          <pre>{{ JSON.stringify(statisticsData, null, 2) }}</pre>
        </div>
      </div>

      <!-- API响应日志 -->
      <div class="test-section">
        <h2>API响应日志</h2>
        <div class="log-container">
          <div v-for="(log, index) in apiLogs" :key="index" class="log-item" :class="log.type">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-method">{{ log.method }}</span>
            <span class="log-url">{{ log.url }}</span>
            <span class="log-status">{{ log.status }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import authApi from '@/api/auth.js'
import realApi from '@/api/realApi.js'
import statisticsApi from '@/api/statistics.js'

// 状态数据
const backendStatus = ref(false)
const databaseStatus = ref(false)
const testing = ref(false)

// 登录测试
const loginTesting = ref(false)
const userInfoTesting = ref(false)
const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})
const loginResult = ref(null)

// 数据加载状态
const loadingUsers = ref(false)
const loadingServices = ref(false)
const loadingBookings = ref(false)
const loadingStats = ref(false)

// 数据结果
const usersData = ref(null)
const servicesData = ref(null)
const bookingsData = ref(null)
const statisticsData = ref(null)

// API日志
const apiLogs = ref([])

// 添加日志
const addLog = (method, url, status, type = 'info') => {
  apiLogs.value.unshift({
    time: new Date().toLocaleTimeString(),
    method,
    url,
    status,
    type
  })
  
  // 只保留最近20条日志
  if (apiLogs.value.length > 20) {
    apiLogs.value = apiLogs.value.slice(0, 20)
  }
}

// 测试后端连接
const testBackendConnection = async () => {
  testing.value = true
  try {
    const response = await realApi.healthCheck()
    backendStatus.value = true
    addLog('GET', '/test/health', '200 OK', 'success')
    ElMessage.success('后端连接成功')
  } catch (error) {
    backendStatus.value = false
    addLog('GET', '/test/health', 'ERROR', 'error')
    ElMessage.error('后端连接失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

// 测试数据库连接
const testDatabaseConnection = async () => {
  testing.value = true
  try {
    const response = await realApi.testDatabase()
    databaseStatus.value = true
    addLog('GET', '/test/database', '200 OK', 'success')
    ElMessage.success('数据库连接成功')
  } catch (error) {
    databaseStatus.value = false
    addLog('GET', '/test/database', 'ERROR', 'error')
    ElMessage.error('数据库连接失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

// 测试登录
const testLogin = async () => {
  loginTesting.value = true
  try {
    const response = await authApi.login(loginForm.username, loginForm.password)
    loginResult.value = response
    addLog('POST', '/auth/login', '200 OK', 'success')
    ElMessage.success('登录成功')
  } catch (error) {
    loginResult.value = { error: error.message }
    addLog('POST', '/auth/login', 'ERROR', 'error')
    ElMessage.error('登录失败: ' + error.message)
  } finally {
    loginTesting.value = false
  }
}

// 测试获取用户信息
const testGetUserInfo = async () => {
  userInfoTesting.value = true
  try {
    const response = await authApi.getUserInfo()
    loginResult.value = response
    addLog('GET', '/user/info', '200 OK', 'success')
    ElMessage.success('获取用户信息成功')
  } catch (error) {
    loginResult.value = { error: error.message }
    addLog('GET', '/user/info', 'ERROR', 'error')
    ElMessage.error('获取用户信息失败: ' + error.message)
  } finally {
    userInfoTesting.value = false
  }
}

// 测试用户数据
const testUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await realApi.getUserList()
    usersData.value = response.data || []
    addLog('GET', '/user/admin/list', '200 OK', 'success')
    ElMessage.success(`获取到 ${usersData.value.length} 条用户数据`)
  } catch (error) {
    addLog('GET', '/user/admin/list', 'ERROR', 'error')
    ElMessage.error('获取用户数据失败: ' + error.message)
  } finally {
    loadingUsers.value = false
  }
}

// 测试服务数据
const testServices = async () => {
  loadingServices.value = true
  try {
    const response = await realApi.getServices()
    servicesData.value = response.data || []
    addLog('GET', '/services/list', '200 OK', 'success')
    ElMessage.success(`获取到 ${servicesData.value.length} 条服务数据`)
  } catch (error) {
    addLog('GET', '/services/list', 'ERROR', 'error')
    ElMessage.error('获取服务数据失败: ' + error.message)
  } finally {
    loadingServices.value = false
  }
}

// 测试预约数据
const testBookings = async () => {
  loadingBookings.value = true
  try {
    const response = await realApi.getOrderList()
    bookingsData.value = response.data || []
    addLog('GET', '/bookings/admin/all', '200 OK', 'success')
    ElMessage.success(`获取到 ${bookingsData.value.length} 条预约数据`)
  } catch (error) {
    addLog('GET', '/bookings/admin/all', 'ERROR', 'error')
    ElMessage.error('获取预约数据失败: ' + error.message)
  } finally {
    loadingBookings.value = false
  }
}

// 测试统计数据
const testStatistics = async () => {
  loadingStats.value = true
  try {
    const response = await statisticsApi.getOverview()
    statisticsData.value = response.data || {}
    addLog('GET', '/statistics/overview', '200 OK', 'success')
    ElMessage.success('获取统计数据成功')
  } catch (error) {
    addLog('GET', '/statistics/overview', 'ERROR', 'error')
    ElMessage.error('获取统计数据失败: ' + error.message)
  } finally {
    loadingStats.value = false
  }
}
</script>

<style scoped>
.real-data-test {
  padding: 20px;
  background: var(--bg-secondary);
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 30px;
}

.status-section, .test-section {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-md);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.status-card {
  padding: 20px;
  border-radius: var(--radius-md);
  text-align: center;
  border: 2px solid var(--border-color);
}

.status-card.success {
  border-color: var(--success-color);
  background: var(--success-bg);
}

.status-card.error {
  border-color: var(--error-color);
  background: var(--error-bg);
}

.test-form {
  margin-bottom: 20px;
}

.data-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.data-result {
  margin-top: 20px;
}

.test-result, .data-result {
  background: var(--bg-secondary);
  padding: 15px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.test-result pre, .data-result pre {
  background: var(--bg-primary);
  padding: 10px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  font-size: 12px;
}

.log-container {
  max-height: 400px;
  overflow-y: auto;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 10px;
}

.log-item {
  display: flex;
  gap: 10px;
  padding: 5px 10px;
  border-radius: var(--radius-sm);
  margin-bottom: 5px;
  font-family: monospace;
  font-size: 12px;
}

.log-item.success {
  background: var(--success-bg);
  color: var(--success-color);
}

.log-item.error {
  background: var(--error-bg);
  color: var(--error-color);
}

.log-item.info {
  background: var(--info-bg);
  color: var(--info-color);
}

.log-time {
  min-width: 80px;
}

.log-method {
  min-width: 60px;
  font-weight: bold;
}

.log-url {
  flex: 1;
}

.log-status {
  min-width: 80px;
  text-align: right;
}
</style>