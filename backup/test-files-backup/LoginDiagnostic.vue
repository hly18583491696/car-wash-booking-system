<template>
  <div class="login-diagnostic-page">
    <div class="diagnostic-container">
      <div class="diagnostic-header">
        <h2>🔍 登录问题诊断工具</h2>
        <p>自动检测和修复登录API调用失败问题</p>
      </div>

      <!-- 快速操作区 -->
      <div class="quick-actions">
        <el-button 
          type="primary" 
          size="large"
          :loading="diagnosing"
          @click="startDiagnosis"
        >
          {{ diagnosing ? '诊断中...' : '🔍 开始诊断' }}
        </el-button>
        
        <el-button 
          type="success" 
          size="large"
          :disabled="!diagnosticResult || issues.length === 0"
          :loading="fixing"
          @click="autoFix"
        >
          {{ fixing ? '修复中...' : '🔧 自动修复' }}
        </el-button>
        
        <el-button 
          type="info" 
          size="large"
          @click="testLogin"
        >
          🧪 测试登录
        </el-button>
      </div>

      <!-- 诊断结果概览 -->
      <div v-if="diagnosticResult" class="diagnostic-overview">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📊 诊断结果概览</span>
              <el-tag :type="getHealthScoreType(summary.healthScore)">
                健康分数: {{ summary.healthScore }}%
              </el-tag>
            </div>
          </template>
          
          <div class="overview-stats">
            <div class="stat-item">
              <div class="stat-value">{{ summary.passedTests }}</div>
              <div class="stat-label">通过测试</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ summary.failedTests }}</div>
              <div class="stat-label">失败测试</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ summary.totalIssues }}</div>
              <div class="stat-label">发现问题</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ summary.criticalIssues }}</div>
              <div class="stat-label">严重问题</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 测试结果详情 -->
      <div v-if="testResults && Object.keys(testResults).length > 0" class="test-results">
        <el-card>
          <template #header>
            <span>🧪 测试结果详情</span>
          </template>
          
          <div class="test-items">
            <div 
              v-for="(result, key) in testResults" 
              :key="key"
              class="test-item"
            >
              <div class="test-name">{{ getTestName(key) }}</div>
              <div class="test-status">
                <el-tag :type="result ? 'success' : 'danger'">
                  {{ result ? '✅ 通过' : '❌ 失败' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 问题列表 -->
      <div v-if="issues.length > 0" class="issues-list">
        <el-card>
          <template #header>
            <span>⚠️ 发现的问题 ({{ issues.length }})</span>
          </template>
          
          <div class="issues">
            <div 
              v-for="(issue, index) in issues" 
              :key="index"
              class="issue-item"
            >
              <div class="issue-severity">
                <el-tag :type="getSeverityType(issue.severity)">
                  {{ getSeverityIcon(issue.severity) }} {{ getSeverityText(issue.severity) }}
                </el-tag>
              </div>
              <div class="issue-content">
                <div class="issue-type">{{ issue.type }}</div>
                <div class="issue-description">{{ issue.description }}</div>
                <div class="issue-time">{{ formatTime(issue.timestamp) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 修复历史 -->
      <div v-if="fixedIssues.length > 0" class="fixed-issues">
        <el-card>
          <template #header>
            <span>✅ 修复历史 ({{ fixedIssues.length }})</span>
          </template>
          
          <div class="fixed-items">
            <div 
              v-for="(fixed, index) in fixedIssues" 
              :key="index"
              class="fixed-item"
            >
              <div class="fixed-content">
                <div class="fixed-type">{{ fixed.type }}</div>
                <div class="fixed-description">{{ fixed.description }}</div>
                <div class="fixed-method">修复方法: {{ fixed.fixMethod }}</div>
                <div class="fixed-time">修复时间: {{ formatTime(fixed.fixedAt) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 登录测试区 -->
      <div class="login-test">
        <el-card>
          <template #header>
            <span>🔐 登录功能测试</span>
          </template>
          
          <el-form :model="testLoginForm" label-width="100px">
            <el-form-item label="用户名">
              <el-input 
                v-model="testLoginForm.username" 
                placeholder="请输入用户名"
                style="width: 300px;"
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input 
                v-model="testLoginForm.password" 
                type="password"
                placeholder="请输入密码"
                style="width: 300px;"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                :loading="testingLogin"
                @click="performLoginTest"
              >
                {{ testingLogin ? '测试中...' : '测试登录' }}
              </el-button>
              <el-button @click="resetTestForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <div v-if="loginTestResult" class="login-test-result">
            <el-alert
              :title="loginTestResult.success ? '登录测试成功' : '登录测试失败'"
              :type="loginTestResult.success ? 'success' : 'error'"
              :description="loginTestResult.message"
              show-icon
            />
          </div>
        </el-card>
      </div>

      <!-- 操作日志 -->
      <div v-if="logs.length > 0" class="operation-logs">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📝 操作日志</span>
              <el-button size="small" @click="clearLogs">清空日志</el-button>
            </div>
          </template>
          
          <div class="logs">
            <div 
              v-for="(log, index) in logs" 
              :key="index"
              class="log-item"
            >
              <span class="log-time">{{ formatTime(log.timestamp) }}</span>
              <span class="log-level" :class="`log-${log.level}`">{{ log.level.toUpperCase() }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 返回按钮 -->
      <div class="back-actions">
        <router-link to="/login">
          <el-button type="info" plain>返回登录页</el-button>
        </router-link>
        <router-link to="/">
          <el-button type="default" plain>返回首页</el-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import LoginDiagnostic from '@/utils/loginDiagnostic.js'
import authApi from '@/api/auth.js'

export default {
  name: 'LoginDiagnostic',
  setup() {
    const diagnosing = ref(false)
    const fixing = ref(false)
    const testingLogin = ref(false)
    
    const diagnosticResult = ref(null)
    const testResults = ref({})
    const issues = ref([])
    const fixedIssues = ref([])
    const summary = ref({})
    const logs = ref([])
    const loginTestResult = ref(null)
    
    const testLoginForm = reactive({
      username: 'admin',
      password: 'admin123'
    })
    
    const diagnostic = new LoginDiagnostic()
    
    // 添加日志
    const addLog = (level, message) => {
      logs.value.unshift({
        level,
        message,
        timestamp: new Date()
      })
      
      // 限制日志数量
      if (logs.value.length > 100) {
        logs.value = logs.value.slice(0, 100)
      }
    }
    
    // 开始诊断
    const startDiagnosis = async () => {
      diagnosing.value = true
      addLog('info', '开始登录问题诊断...')
      
      try {
        const result = await diagnostic.diagnoseLoginIssues()
        
        diagnosticResult.value = result
        testResults.value = result.testResults
        issues.value = result.issues
        summary.value = result.summary
        
        addLog('success', `诊断完成，发现 ${result.issues.length} 个问题`)
        
        if (result.issues.length === 0) {
          ElMessage.success('🎉 未发现问题，登录功能正常！')
        } else {
          ElNotification({
            title: '诊断完成',
            message: `发现 ${result.issues.length} 个问题，其中 ${result.summary.criticalIssues} 个严重问题`,
            type: 'warning'
          })
        }
        
      } catch (error) {
        addLog('error', `诊断失败: ${error.message}`)
        ElMessage.error('诊断过程中发生错误')
      } finally {
        diagnosing.value = false
      }
    }
    
    // 自动修复
    const autoFix = async () => {
      fixing.value = true
      addLog('info', '开始自动修复问题...')
      
      try {
        await diagnostic.autoFix()
        fixedIssues.value = diagnostic.fixedIssues
        
        addLog('success', `修复完成，已修复 ${fixedIssues.value.length} 个问题`)
        ElMessage.success('自动修复完成！')
        
        // 重新诊断验证修复效果
        setTimeout(() => {
          startDiagnosis()
        }, 1000)
        
      } catch (error) {
        addLog('error', `修复失败: ${error.message}`)
        ElMessage.error('自动修复过程中发生错误')
      } finally {
        fixing.value = false
      }
    }
    
    // 测试登录
    const testLogin = async () => {
      addLog('info', '执行快速登录测试...')
      
      try {
        const result = await diagnostic.quickDiagnose()
        
        let message = '快速测试结果: '
        if (result.network) message += '网络✅ '
        if (result.backend) message += '后端✅ '
        if (result.auth) message += '认证✅'
        
        addLog('info', message)
        
        if (result.network && result.backend && result.auth) {
          ElMessage.success('快速测试通过，登录功能可用！')
        } else {
          ElMessage.warning('快速测试发现问题，建议进行完整诊断')
        }
        
      } catch (error) {
        addLog('error', `快速测试失败: ${error.message}`)
        ElMessage.error('快速测试失败')
      }
    }
    
    // 执行登录测试
    const performLoginTest = async () => {
      if (!testLoginForm.username || !testLoginForm.password) {
        ElMessage.warning('请输入用户名和密码')
        return
      }
      
      testingLogin.value = true
      loginTestResult.value = null
      addLog('info', `测试登录: ${testLoginForm.username}`)
      
      try {
        const response = await authApi.login(testLoginForm.username, testLoginForm.password)
        
        if (response && response.code === 200) {
          loginTestResult.value = {
            success: true,
            message: `登录成功！用户: ${response.data.user.realName || response.data.user.username}`
          }
          addLog('success', '登录测试成功')
          ElMessage.success('登录测试成功！')
        } else {
          loginTestResult.value = {
            success: false,
            message: response.message || '登录失败'
          }
          addLog('error', `登录测试失败: ${response.message}`)
        }
        
      } catch (error) {
        loginTestResult.value = {
          success: false,
          message: error.message || '登录请求失败'
        }
        addLog('error', `登录测试异常: ${error.message}`)
      } finally {
        testingLogin.value = false
      }
    }
    
    // 重置测试表单
    const resetTestForm = () => {
      testLoginForm.username = 'admin'
      testLoginForm.password = 'admin123'
      loginTestResult.value = null
    }
    
    // 清空日志
    const clearLogs = () => {
      logs.value = []
      addLog('info', '日志已清空')
    }
    
    // 获取健康分数类型
    const getHealthScoreType = (score) => {
      if (score >= 80) return 'success'
      if (score >= 60) return 'warning'
      return 'danger'
    }
    
    // 获取测试名称
    const getTestName = (key) => {
      const names = {
        networkConnection: '网络连接',
        backendService: '后端服务',
        apiConfiguration: 'API配置',
        authEndpoint: '认证端点',
        databaseConnection: '数据库连接',
        loginFlow: '登录流程'
      }
      return names[key] || key
    }
    
    // 获取严重程度类型
    const getSeverityType = (severity) => {
      const types = {
        critical: 'danger',
        high: 'warning',
        medium: 'info',
        low: 'success'
      }
      return types[severity] || 'info'
    }
    
    // 获取严重程度图标
    const getSeverityIcon = (severity) => {
      const icons = {
        critical: '🔴',
        high: '🟠',
        medium: '🟡',
        low: '🟢'
      }
      return icons[severity] || '⚪'
    }
    
    // 获取严重程度文本
    const getSeverityText = (severity) => {
      const texts = {
        critical: '严重',
        high: '高',
        medium: '中',
        low: '低'
      }
      return texts[severity] || severity
    }
    
    // 格式化时间
    const formatTime = (date) => {
      return new Date(date).toLocaleString()
    }
    
    return {
      diagnosing,
      fixing,
      testingLogin,
      diagnosticResult,
      testResults,
      issues,
      fixedIssues,
      summary,
      logs,
      loginTestResult,
      testLoginForm,
      startDiagnosis,
      autoFix,
      testLogin,
      performLoginTest,
      resetTestForm,
      clearLogs,
      getHealthScoreType,
      getTestName,
      getSeverityType,
      getSeverityIcon,
      getSeverityText,
      formatTime
    }
  }
}
</script>

<style scoped>
.login-diagnostic-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.diagnostic-container {
  max-width: 1200px;
  margin: 0 auto;
}

.diagnostic-header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.diagnostic-header h2 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.diagnostic-header p {
  font-size: 1.2em;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.diagnostic-overview,
.test-results,
.issues-list,
.fixed-issues,
.login-test,
.operation-logs {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  color: #666;
  margin-top: 5px;
}

.test-items {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.test-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.test-name {
  font-weight: 500;
}

.issues {
  space-y: 15px;
}

.issue-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.issue-content {
  flex: 1;
}

.issue-type {
  font-weight: bold;
  color: #333;
}

.issue-description {
  margin: 5px 0;
  color: #666;
}

.issue-time {
  font-size: 0.9em;
  color: #999;
}

.fixed-items {
  space-y: 15px;
}

.fixed-item {
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #10b981;
  margin-bottom: 10px;
}

.fixed-content {
  color: #333;
}

.fixed-type {
  font-weight: bold;
}

.fixed-description,
.fixed-method,
.fixed-time {
  margin: 5px 0;
  font-size: 0.9em;
  color: #666;
}

.login-test-result {
  margin-top: 20px;
}

.logs {
  max-height: 300px;
  overflow-y: auto;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 15px;
}

.log-item {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.log-time {
  color: #888;
  min-width: 150px;
}

.log-level {
  min-width: 60px;
  font-weight: bold;
}

.log-info { color: #409eff; }
.log-success { color: #67c23a; }
.log-warning { color: #e6a23c; }
.log-error { color: #f56c6c; }

.log-message {
  color: #fff;
}

.back-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}
</style>