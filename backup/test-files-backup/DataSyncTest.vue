<template>
  <div class="data-sync-test">
    <div class="page-header">
      <h1>数据同步测试与诊断</h1>
      <p>全面测试前后端数据同步功能，诊断和修复同步问题</p>
    </div>

    <!-- 快速操作面板 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="6">
        <el-card shadow="hover" class="action-card">
          <div class="action-content">
            <el-icon class="action-icon"><Tools /></el-icon>
            <h3>快速诊断</h3>
            <p>检测数据同步问题</p>
            <el-button 
              type="primary" 
              @click="runQuickDiagnostic"
              :loading="diagnosing"
              block
            >
              开始诊断
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="action-card">
          <div class="action-content">
            <el-icon class="action-icon"><Refresh /></el-icon>
            <h3>快速修复</h3>
            <p>自动修复常见问题</p>
            <el-button 
              type="success" 
              @click="runQuickFix"
              :loading="fixing"
              :disabled="!hasIssues"
              block
            >
              快速修复
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="action-card">
          <div class="action-content">
            <el-icon class="action-icon"><DataLine /></el-icon>
            <h3>数据测试</h3>
            <p>测试CRUD操作</p>
            <el-button 
              type="warning" 
              @click="runDataTest"
              :loading="testing"
              block
            >
              数据测试
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="action-card">
          <div class="action-content">
            <el-icon class="action-icon"><Monitor /></el-icon>
            <h3>实时监控</h3>
            <p>查看同步状态</p>
            <el-button 
              type="info" 
              @click="showMonitor = !showMonitor"
              block
            >
              {{ showMonitor ? '隐藏' : '显示' }}监控
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 监控面板 -->
    <div v-if="showMonitor" class="monitor-section">
      <DataSyncMonitor />
    </div>

    <!-- 测试结果 -->
    <el-row :gutter="20" class="test-results">
      <!-- 诊断结果 -->
      <el-col :span="12">
        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>诊断结果</span>
              <el-badge :value="diagnosticResult.issues?.length || 0" type="danger">
                <el-tag :type="getDiagnosticType()">
                  {{ getDiagnosticStatus() }}
                </el-tag>
              </el-badge>
            </div>
          </template>

          <div v-if="diagnosticResult.timestamp" class="result-content">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="检测时间">
                {{ formatTime(diagnosticResult.timestamp) }}
              </el-descriptions-item>
              <el-descriptions-item label="总问题数">
                {{ diagnosticResult.totalIssues || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="严重问题">
                {{ diagnosticResult.criticalIssues || 0 }}
              </el-descriptions-item>
              <el-descriptions-item label="重要问题">
                {{ diagnosticResult.highIssues || 0 }}
              </el-descriptions-item>
            </el-descriptions>

            <!-- 问题列表 -->
            <div v-if="diagnosticResult.issues?.length > 0" class="issues-list">
              <h4>发现的问题：</h4>
              <el-alert
                v-for="issue in diagnosticResult.issues"
                :key="issue.id"
                :title="issue.type"
                :description="issue.description"
                :type="getIssueType(issue.severity)"
                :closable="false"
                class="issue-alert"
              />
            </div>

            <div v-else class="no-issues">
              <el-result
                icon="success"
                title="数据同步正常"
                sub-title="未发现任何同步问题"
              />
            </div>
          </div>

          <div v-else class="no-result">
            <el-empty description="暂无诊断结果，请点击"开始诊断"" />
          </div>
        </el-card>
      </el-col>

      <!-- 测试结果 -->
      <el-col :span="12">
        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>数据测试结果</span>
              <el-tag :type="getTestResultType()">
                {{ getTestResultStatus() }}
              </el-tag>
            </div>
          </template>

          <div v-if="testResults.length > 0" class="test-content">
            <el-timeline>
              <el-timeline-item
                v-for="(result, index) in testResults"
                :key="index"
                :type="result.success ? 'success' : 'danger'"
                :timestamp="formatTime(result.timestamp)"
              >
                <div class="test-item">
                  <div class="test-title">{{ result.name }}</div>
                  <div class="test-description">{{ result.description }}</div>
                  <div v-if="result.error" class="test-error">
                    错误: {{ result.error }}
                  </div>
                  <div v-if="result.data" class="test-data">
                    <el-tag size="small">{{ result.data }}</el-tag>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>

          <div v-else class="no-test">
            <el-empty description="暂无测试结果，请点击"数据测试"" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细操作面板 -->
    <el-card class="operations-card">
      <template #header>
        <span>详细操作</span>
      </template>

      <el-tabs v-model="activeTab" type="card">
        <!-- API测试 -->
        <el-tab-pane label="API测试" name="api">
          <div class="api-test-section">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-button @click="testApiConnection" :loading="apiTesting" block>
                  测试API连接
                </el-button>
              </el-col>
              <el-col :span="8">
                <el-button @click="testDatabaseConnection" :loading="dbTesting" block>
                  测试数据库连接
                </el-button>
              </el-col>
              <el-col :span="8">
                <el-button @click="testOrderOperations" :loading="orderTesting" block>
                  测试订单操作
                </el-button>
              </el-col>
            </el-row>

            <div v-if="apiTestResults.length > 0" class="api-results">
              <h4>API测试结果：</h4>
              <el-table :data="apiTestResults" size="small" border>
                <el-table-column prop="name" label="测试项" width="200" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.success ? 'success' : 'danger'">
                      {{ scope.row.success ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="message" label="结果" />
                <el-table-column prop="duration" label="耗时(ms)" width="100" />
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 数据同步测试 -->
        <el-tab-pane label="同步测试" name="sync">
          <div class="sync-test-section">
            <el-form :model="syncTestForm" label-width="120px">
              <el-form-item label="测试订单ID">
                <el-input v-model="syncTestForm.orderId" placeholder="输入订单ID" />
              </el-form-item>
              <el-form-item label="目标状态">
                <el-select v-model="syncTestForm.status" placeholder="选择状态">
                  <el-option label="待确认" value="pending" />
                  <el-option label="已确认" value="confirmed" />
                  <el-option label="进行中" value="in_progress" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已取消" value="cancelled" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testStatusUpdate" :loading="statusTesting">
                  测试状态更新
                </el-button>
                <el-button @click="testDataRefresh" :loading="refreshTesting">
                  测试数据刷新
                </el-button>
              </el-form-item>
            </el-form>

            <div v-if="syncTestResults.length > 0" class="sync-results">
              <h4>同步测试结果：</h4>
              <div class="sync-result-list">
                <div 
                  v-for="(result, index) in syncTestResults"
                  :key="index"
                  class="sync-result-item"
                  :class="result.success ? 'success' : 'error'"
                >
                  <div class="result-header">
                    <span class="result-title">{{ result.operation }}</span>
                    <span class="result-time">{{ formatTime(result.timestamp) }}</span>
                  </div>
                  <div class="result-content">
                    <p><strong>订单ID:</strong> {{ result.orderId }}</p>
                    <p><strong>状态变更:</strong> {{ result.oldStatus }} → {{ result.newStatus }}</p>
                    <p><strong>结果:</strong> {{ result.success ? '成功' : '失败' }}</p>
                    <p v-if="result.error"><strong>错误:</strong> {{ result.error }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 系统信息 -->
        <el-tab-pane label="系统信息" name="system">
          <div class="system-info-section">
            <el-descriptions title="系统配置" :column="2" border>
              <el-descriptions-item label="前端地址">
                {{ window.location.origin }}
              </el-descriptions-item>
              <el-descriptions-item label="后端地址">
                {{ backendUrl }}
              </el-descriptions-item>
              <el-descriptions-item label="API模式">
                {{ apiMode }}
              </el-descriptions-item>
              <el-descriptions-item label="浏览器">
                {{ navigator.userAgent.split(' ')[0] }}
              </el-descriptions-item>
            </el-descriptions>

            <el-button @click="getSystemInfo" :loading="systemInfoLoading" style="margin-top: 20px;">
              获取系统详细信息
            </el-button>

            <div v-if="systemInfo" class="system-details">
              <h4>系统详细信息：</h4>
              <pre>{{ JSON.stringify(systemInfo, null, 2) }}</pre>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  Tools, Refresh, DataLine, Monitor 
} from '@element-plus/icons-vue'
import DataSyncMonitor from '@/components/DataSyncMonitor.vue'
import { syncDiagnostic, runDiagnostic, quickFix } from '@/utils/syncDiagnostic.js'
import { updateBookingStatusSync, refreshBookingDataSync } from '@/utils/dataSync.js'
import realApi from '@/api/realApi.js'
import orderApi from '@/api/order.js'
import API_CONFIG from '@/config/api.js'

// 响应式数据
const diagnosing = ref(false)
const fixing = ref(false)
const testing = ref(false)
const showMonitor = ref(false)
const activeTab = ref('api')

// 测试状态
const apiTesting = ref(false)
const dbTesting = ref(false)
const orderTesting = ref(false)
const statusTesting = ref(false)
const refreshTesting = ref(false)
const systemInfoLoading = ref(false)

// 结果数据
const diagnosticResult = ref({})
const testResults = ref([])
const apiTestResults = ref([])
const syncTestResults = ref([])
const systemInfo = ref(null)

// 表单数据
const syncTestForm = reactive({
  orderId: '',
  status: ''
})

// 计算属性
const hasIssues = computed(() => {
  return diagnosticResult.value.issues && diagnosticResult.value.issues.length > 0
})

const backendUrl = computed(() => API_CONFIG.BASE_URL)
const apiMode = computed(() => orderApi.isMockMode() ? '模拟模式' : '真实API')

// 生命周期
onMounted(() => {
  // 初始化一些默认值
  syncTestForm.orderId = '1'
  syncTestForm.status = 'confirmed'
})

// 方法
const runQuickDiagnostic = async () => {
  diagnosing.value = true
  try {
    ElMessage.info('开始执行数据同步诊断...')
    const result = await runDiagnostic()
    diagnosticResult.value = result
    
    if (result.success) {
      if (result.issues && result.issues.length > 0) {
        ElNotification({
          title: '诊断完成',
          message: `发现 ${result.issues.length} 个问题`,
          type: 'warning',
          duration: 5000
        })
      } else {
        ElMessage.success('诊断完成，数据同步状态良好')
      }
    } else {
      ElMessage.error(`诊断失败: ${result.error}`)
    }
  } catch (error) {
    ElMessage.error(`诊断过程出错: ${error.message}`)
  } finally {
    diagnosing.value = false
  }
}

const runQuickFix = async () => {
  fixing.value = true
  try {
    ElMessage.info('开始执行快速修复...')
    const success = await quickFix()
    
    if (success) {
      ElMessage.success('快速修复完成')
      // 重新运行诊断
      await runQuickDiagnostic()
    }
  } catch (error) {
    ElMessage.error(`快速修复失败: ${error.message}`)
  } finally {
    fixing.value = false
  }
}

const runDataTest = async () => {
  testing.value = true
  testResults.value = []
  
  try {
    ElMessage.info('开始执行数据测试...')
    
    // 测试1: 获取订单列表
    await addTestResult('获取订单列表', '测试获取所有订单数据', async () => {
      const response = await orderApi.getOrderList()
      return `获取到 ${response.data?.length || 0} 条订单`
    })
    
    // 测试2: 创建测试订单
    await addTestResult('创建测试订单', '测试订单创建功能', async () => {
      const bookingData = {
        userId: 2,
        serviceId: 1,
        bookingDate: new Date().toISOString().split('T')[0],
        bookingTime: '10:00-11:00',
        carNumber: 'TEST001',
        carModel: '测试车型',
        contactPhone: '13800138000',
        notes: '数据同步测试订单',
        totalPrice: 100,
        status: 'pending'
      }
      
      const response = await realApi.createBooking(bookingData)
      return `订单创建成功，ID: ${response.data?.id}`
    })
    
    // 测试3: 状态更新测试
    await addTestResult('状态更新测试', '测试订单状态更新', async () => {
      // 先获取一个订单
      const orders = await orderApi.getOrderList()
      if (orders.data && orders.data.length > 0) {
        const orderId = orders.data[0].id
        const oldStatus = orders.data[0].status
        const newStatus = oldStatus === 'pending' ? 'confirmed' : 'pending'
        
        await orderApi.updateOrderStatus(orderId, newStatus)
        return `订单 ${orderId} 状态更新: ${oldStatus} → ${newStatus}`
      } else {
        throw new Error('没有可用的订单进行测试')
      }
    })
    
    ElMessage.success('数据测试完成')
    
  } catch (error) {
    ElMessage.error(`数据测试失败: ${error.message}`)
  } finally {
    testing.value = false
  }
}

const addTestResult = async (name, description, testFn) => {
  const startTime = Date.now()
  try {
    const result = await testFn()
    testResults.value.push({
      name,
      description,
      success: true,
      data: result,
      timestamp: new Date(),
      duration: Date.now() - startTime
    })
  } catch (error) {
    testResults.value.push({
      name,
      description,
      success: false,
      error: error.message,
      timestamp: new Date(),
      duration: Date.now() - startTime
    })
  }
}

const testApiConnection = async () => {
  apiTesting.value = true
  try {
    const startTime = Date.now()
    const response = await realApi.healthCheck()
    const duration = Date.now() - startTime
    
    apiTestResults.value.push({
      name: 'API连接测试',
      success: !!response,
      message: response ? 'API连接正常' : 'API连接失败',
      duration
    })
    
    ElMessage.success('API连接测试完成')
  } catch (error) {
    apiTestResults.value.push({
      name: 'API连接测试',
      success: false,
      message: error.message,
      duration: 0
    })
    ElMessage.error('API连接测试失败')
  } finally {
    apiTesting.value = false
  }
}

const testDatabaseConnection = async () => {
  dbTesting.value = true
  try {
    const startTime = Date.now()
    const response = await realApi.testDatabase()
    const duration = Date.now() - startTime
    
    apiTestResults.value.push({
      name: '数据库连接测试',
      success: !!response,
      message: response ? '数据库连接正常' : '数据库连接失败',
      duration
    })
    
    ElMessage.success('数据库连接测试完成')
  } catch (error) {
    apiTestResults.value.push({
      name: '数据库连接测试',
      success: false,
      message: error.message,
      duration: 0
    })
    ElMessage.error('数据库连接测试失败')
  } finally {
    dbTesting.value = false
  }
}

const testOrderOperations = async () => {
  orderTesting.value = true
  try {
    // 测试获取订单列表
    const startTime1 = Date.now()
    const orders = await orderApi.getOrderList()
    apiTestResults.value.push({
      name: '获取订单列表',
      success: !!orders,
      message: `获取到 ${orders.data?.length || 0} 条订单`,
      duration: Date.now() - startTime1
    })
    
    // 如果有订单，测试获取订单详情
    if (orders.data && orders.data.length > 0) {
      const startTime2 = Date.now()
      const orderId = orders.data[0].id
      const orderDetail = await orderApi.getOrderById(orderId)
      apiTestResults.value.push({
        name: '获取订单详情',
        success: !!orderDetail,
        message: `订单 ${orderId} 详情获取成功`,
        duration: Date.now() - startTime2
      })
    }
    
    ElMessage.success('订单操作测试完成')
  } catch (error) {
    apiTestResults.value.push({
      name: '订单操作测试',
      success: false,
      message: error.message,
      duration: 0
    })
    ElMessage.error('订单操作测试失败')
  } finally {
    orderTesting.value = false
  }
}

const testStatusUpdate = async () => {
  if (!syncTestForm.orderId || !syncTestForm.status) {
    ElMessage.warning('请填写订单ID和目标状态')
    return
  }
  
  statusTesting.value = true
  try {
    // 先获取当前状态
    const orderDetail = await orderApi.getOrderById(syncTestForm.orderId)
    const oldStatus = orderDetail.data?.status || 'unknown'
    
    // 执行状态更新
    await updateBookingStatusSync(
      syncTestForm.orderId,
      syncTestForm.status,
      orderApi,
      (id, status) => {
        console.log(`本地状态更新: ${id} -> ${status}`)
      }
    )
    
    syncTestResults.value.unshift({
      operation: '状态更新测试',
      orderId: syncTestForm.orderId,
      oldStatus,
      newStatus: syncTestForm.status,
      success: true,
      timestamp: new Date()
    })
    
    ElMessage.success('状态更新测试成功')
  } catch (error) {
    syncTestResults.value.unshift({
      operation: '状态更新测试',
      orderId: syncTestForm.orderId,
      oldStatus: 'unknown',
      newStatus: syncTestForm.status,
      success: false,
      error: error.message,
      timestamp: new Date()
    })
    ElMessage.error(`状态更新测试失败: ${error.message}`)
  } finally {
    statusTesting.value = false
  }
}

const testDataRefresh = async () => {
  refreshTesting.value = true
  try {
    await refreshBookingDataSync(orderApi, (data) => {
      console.log(`数据刷新成功，获取到 ${data.length} 条订单`)
    })
    
    syncTestResults.value.unshift({
      operation: '数据刷新测试',
      orderId: 'N/A',
      oldStatus: 'N/A',
      newStatus: 'N/A',
      success: true,
      timestamp: new Date()
    })
    
    ElMessage.success('数据刷新测试成功')
  } catch (error) {
    syncTestResults.value.unshift({
      operation: '数据刷新测试',
      orderId: 'N/A',
      oldStatus: 'N/A',
      newStatus: 'N/A',
      success: false,
      error: error.message,
      timestamp: new Date()
    })
    ElMessage.error(`数据刷新测试失败: ${error.message}`)
  } finally {
    refreshTesting.value = false
  }
}

const getSystemInfo = async () => {
  systemInfoLoading.value = true
  try {
    const response = await realApi.getSystemInfo()
    systemInfo.value = response.data
    ElMessage.success('系统信息获取成功')
  } catch (error) {
    ElMessage.error(`获取系统信息失败: ${error.message}`)
  } finally {
    systemInfoLoading.value = false
  }
}

// 辅助方法
const getDiagnosticType = () => {
  if (!diagnosticResult.value.timestamp) return 'info'
  if (diagnosticResult.value.criticalIssues > 0) return 'danger'
  if (diagnosticResult.value.highIssues > 0) return 'warning'
  return 'success'
}

const getDiagnosticStatus = () => {
  if (!diagnosticResult.value.timestamp) return '未检测'
  if (diagnosticResult.value.totalIssues === 0) return '正常'
  if (diagnosticResult.value.criticalIssues > 0) return '严重'
  return '警告'
}

const getTestResultType = () => {
  if (testResults.value.length === 0) return 'info'
  const hasError = testResults.value.some(r => !r.success)
  return hasError ? 'danger' : 'success'
}

const getTestResultStatus = () => {
  if (testResults.value.length === 0) return '未测试'
  const hasError = testResults.value.some(r => !r.success)
  return hasError ? '有失败' : '全部成功'
}

const getIssueType = (severity) => {
  const types = {
    'critical': 'error',
    'high': 'warning',
    'medium': 'info'
  }
  return types[severity] || 'info'
}

const formatTime = (time) => {
  if (!time) return '无'
  return new Date(time).toLocaleString()
}
</script>

<style scoped>
.data-sync-test {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.quick-actions {
  margin-bottom: 30px;
}

.action-card {
  height: 200px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.action-content {
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.action-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 15px;
}

.action-content h3 {
  margin: 10px 0;
  color: #303133;
}

.action-content p {
  color: #909399;
  margin-bottom: 20px;
}

.monitor-section {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.test-results {
  margin-bottom: 30px;
}

.result-card {
  height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-content {
  height: 400px;
  overflow-y: auto;
}

.issues-list {
  margin-top: 20px;
}

.issue-alert {
  margin-bottom: 10px;
}

.no-issues, .no-result, .no-test {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.test-content {
  height: 400px;
  overflow-y: auto;
}

.test-item {
  padding: 10px 0;
}

.test-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.test-description {
  color: #606266;
  margin-bottom: 5px;
}

.test-error {
  color: #f56c6c;
  font-size: 12px;
}

.test-data {
  margin-top: 5px;
}

.operations-card {
  margin-bottom: 30px;
}

.api-test-section {
  padding: 20px 0;
}

.api-results {
  margin-top: 20px;
}

.sync-test-section {
  padding: 20px 0;
}

.sync-results {
  margin-top: 20px;
}

.sync-result-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.sync-result-item {
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.sync-result-item.success {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.sync-result-item.error {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-title {
  font-weight: bold;
  color: #303133;
}

.result-time {
  font-size: 12px;
  color: #909399;
}

.result-content p {
  margin: 5px 0;
  font-size: 14px;
}

.system-info-section {
  padding: 20px 0;
}

.system-details {
  margin-top: 20px;
}

.system-details pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
  font-size: 12px;
  overflow-x: auto;
}
</style>