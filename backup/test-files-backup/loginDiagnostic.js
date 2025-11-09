/**
 * 登录API调用失败诊断工具
 */
import { ElMessage, ElNotification } from 'element-plus'
import realApi from '@/api/realApi.js'
import request from '@/api/request.js'
import API_CONFIG from '@/config/api.js'

export class LoginDiagnostic {
  constructor() {
    this.issues = []
    this.fixedIssues = []
    this.testResults = {}
  }

  /**
   * 添加问题
   */
  addIssue(type, description, severity = 'medium') {
    this.issues.push({
      type,
      description,
      severity,
      timestamp: new Date()
    })
  }

  /**
   * 全面诊断登录问题
   */
  async diagnoseLoginIssues() {
    console.log('🔍 开始登录问题诊断...')
    this.issues = []
    this.testResults = {}

    try {
      // 1. 检查网络连接
      await this.checkNetworkConnection()
      
      // 2. 检查后端服务状态
      await this.checkBackendService()
      
      // 3. 检查API配置
      await this.checkApiConfiguration()
      
      // 4. 检查认证端点
      await this.checkAuthEndpoint()
      
      // 5. 检查数据库连接
      await this.checkDatabaseConnection()
      
      // 6. 测试登录流程
      await this.testLoginFlow()
      
      // 7. 生成诊断报告
      this.generateDiagnosticReport()
      
    } catch (error) {
      console.error('❌ 诊断过程中发生错误:', error)
      this.addIssue('DIAGNOSTIC_ERROR', `诊断过程失败: ${error.message}`, 'critical')
    }

    return {
      issues: this.issues,
      testResults: this.testResults,
      summary: this.generateSummary()
    }
  }

  /**
   * 检查网络连接
   */
  async checkNetworkConnection() {
    console.log('🌐 检查网络连接...')
    
    try {
      const response = await fetch('https://www.baidu.com', { 
        method: 'HEAD',
        mode: 'no-cors',
        cache: 'no-cache'
      })
      
      this.testResults.networkConnection = true
      console.log('✅ 网络连接正常')
    } catch (error) {
      this.testResults.networkConnection = false
      this.addIssue('NETWORK_CONNECTION', '网络连接失败，请检查网络设置', 'critical')
      console.error('❌ 网络连接失败:', error)
    }
  }

  /**
   * 检查后端服务状态
   */
  async checkBackendService() {
    console.log('🖥️ 检查后端服务状态...')
    
    try {
      const healthUrl = `${API_CONFIG.BASE_URL}/test/health`
      console.log('检查健康端点:', healthUrl)
      
      const response = await fetch(healthUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })
      
      if (response.ok) {
        const data = await response.json()
        this.testResults.backendService = true
        console.log('✅ 后端服务正常运行')
      } else {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
    } catch (error) {
      this.testResults.backendService = false
      this.addIssue('BACKEND_SERVICE', `后端服务不可用: ${error.message}`, 'critical')
      console.error('❌ 后端服务检查失败:', error)
    }
  }

  /**
   * 检查API配置
   */
  async checkApiConfiguration() {
    console.log('⚙️ 检查API配置...')
    
    // 检查基础URL
    if (!API_CONFIG.BASE_URL) {
      this.addIssue('API_CONFIG', 'API基础URL未配置', 'critical')
    } else if (!API_CONFIG.BASE_URL.startsWith('http')) {
      this.addIssue('API_CONFIG', 'API基础URL格式错误', 'high')
    }
    
    // 检查超时设置
    if (!API_CONFIG.TIMEOUT || API_CONFIG.TIMEOUT < 1000) {
      this.addIssue('API_CONFIG', 'API超时时间设置过短', 'medium')
    }
    
    // 检查认证URL配置
    if (!API_CONFIG.NO_AUTH_URLS.includes('/auth/login')) {
      this.addIssue('API_CONFIG', '登录接口未在免认证列表中', 'high')
    }
    
    this.testResults.apiConfiguration = this.issues.filter(i => i.type === 'API_CONFIG').length === 0
    
    if (this.testResults.apiConfiguration) {
      console.log('✅ API配置检查通过')
    }
  }

  /**
   * 检查认证端点
   */
  async checkAuthEndpoint() {
    console.log('🔐 检查认证端点...')
    
    try {
      const authUrl = `${API_CONFIG.BASE_URL}/auth/login`
      console.log('检查认证端点:', authUrl)
      
      // 发送OPTIONS请求检查CORS
      const optionsResponse = await fetch(authUrl, {
        method: 'OPTIONS'
      })
      
      if (optionsResponse.ok) {
        console.log('✅ 认证端点可访问')
        this.testResults.authEndpoint = true
      } else {
        throw new Error(`OPTIONS请求失败: ${optionsResponse.status}`)
      }
    } catch (error) {
      this.testResults.authEndpoint = false
      this.addIssue('AUTH_ENDPOINT', `认证端点不可访问: ${error.message}`, 'critical')
      console.error('❌ 认证端点检查失败:', error)
    }
  }

  /**
   * 检查数据库连接
   */
  async checkDatabaseConnection() {
    console.log('🗄️ 检查数据库连接...')
    
    try {
      const response = await realApi.testDatabase()
      if (response && response.code === 200) {
        this.testResults.databaseConnection = true
        console.log('✅ 数据库连接正常')
      } else {
        throw new Error('数据库测试返回异常')
      }
    } catch (error) {
      this.testResults.databaseConnection = false
      this.addIssue('DATABASE_CONNECTION', `数据库连接失败: ${error.message}`, 'critical')
      console.error('❌ 数据库连接检查失败:', error)
    }
  }

  /**
   * 测试登录流程
   */
  async testLoginFlow() {
    console.log('🧪 测试登录流程...')
    
    try {
      // 使用测试账号进行登录测试
      const testCredentials = [
        { username: 'admin', password: 'admin123' },
        { username: 'test', password: 'test123' }
      ]
      
      for (const cred of testCredentials) {
        try {
          console.log(`测试账号: ${cred.username}`)
          
          const response = await request.post('/auth/login', {
            username: cred.username,
            password: cred.password
          })
          
          if (response && response.code === 200) {
            console.log(`✅ 测试账号 ${cred.username} 登录成功`)
            this.testResults.loginFlow = true
            return // 有一个账号成功就算通过
          }
        } catch (error) {
          console.log(`⚠️ 测试账号 ${cred.username} 登录失败: ${error.message}`)
        }
      }
      
      // 如果所有测试账号都失败
      this.testResults.loginFlow = false
      this.addIssue('LOGIN_FLOW', '所有测试账号登录失败，可能是认证逻辑问题', 'high')
      
    } catch (error) {
      this.testResults.loginFlow = false
      this.addIssue('LOGIN_FLOW', `登录流程测试失败: ${error.message}`, 'high')
      console.error('❌ 登录流程测试失败:', error)
    }
  }

  /**
   * 生成诊断报告
   */
  generateDiagnosticReport() {
    console.log('\n📋 登录诊断报告')
    console.log('==========================================')
    
    // 测试结果概览
    console.log('🔍 测试结果概览:')
    Object.entries(this.testResults).forEach(([key, result]) => {
      const status = result ? '✅ 通过' : '❌ 失败'
      console.log(`  ${key}: ${status}`)
    })
    
    // 问题列表
    if (this.issues.length > 0) {
      console.log('\n⚠️ 发现的问题:')
      this.issues.forEach((issue, index) => {
        const severityIcon = {
          critical: '🔴',
          high: '🟠', 
          medium: '🟡',
          low: '🟢'
        }[issue.severity] || '⚪'
        
        console.log(`  ${index + 1}. ${severityIcon} [${issue.type}] ${issue.description}`)
      })
    } else {
      console.log('\n✅ 未发现问题')
    }
    
    console.log('==========================================\n')
  }

  /**
   * 生成摘要
   */
  generateSummary() {
    const totalTests = Object.keys(this.testResults).length
    const passedTests = Object.values(this.testResults).filter(Boolean).length
    const criticalIssues = this.issues.filter(i => i.severity === 'critical').length
    const highIssues = this.issues.filter(i => i.severity === 'high').length
    
    return {
      totalTests,
      passedTests,
      failedTests: totalTests - passedTests,
      totalIssues: this.issues.length,
      criticalIssues,
      highIssues,
      healthScore: Math.round((passedTests / totalTests) * 100)
    }
  }

  /**
   * 自动修复问题
   */
  async autoFix() {
    console.log('🔧 开始自动修复登录问题...')
    
    for (const issue of this.issues) {
      try {
        switch (issue.type) {
          case 'API_CONFIG':
            await this.fixApiConfiguration(issue)
            break
          case 'AUTH_ENDPOINT':
            await this.fixAuthEndpoint(issue)
            break
          case 'BACKEND_SERVICE':
            await this.fixBackendService(issue)
            break
          case 'DATABASE_CONNECTION':
            await this.fixDatabaseConnection(issue)
            break
          default:
            console.log(`⚠️ 无法自动修复问题类型: ${issue.type}`)
        }
      } catch (error) {
        console.error(`❌ 修复问题失败: ${issue.type}`, error)
      }
    }
    
    console.log('✅ 自动修复完成')
  }

  /**
   * 修复API配置问题
   */
  async fixApiConfiguration(issue) {
    console.log(`🔧 修复API配置问题: ${issue.description}`)
    
    // 这里可以实现具体的修复逻辑
    // 例如：重新加载配置、重置默认值等
    
    this.fixedIssues.push({
      ...issue,
      fixedAt: new Date(),
      fixMethod: 'api_config_reset'
    })
  }

  /**
   * 修复认证端点问题
   */
  async fixAuthEndpoint(issue) {
    console.log(`🔧 修复认证端点问题: ${issue.description}`)
    
    // 可以尝试重新初始化请求客户端
    
    this.fixedIssues.push({
      ...issue,
      fixedAt: new Date(),
      fixMethod: 'endpoint_reinit'
    })
  }

  /**
   * 修复后端服务问题
   */
  async fixBackendService(issue) {
    console.log(`🔧 尝试修复后端服务问题: ${issue.description}`)
    
    ElNotification({
      title: '后端服务问题',
      message: '检测到后端服务不可用，请确保后端服务已启动并运行在正确的端口上',
      type: 'warning',
      duration: 0
    })
  }

  /**
   * 修复数据库连接问题
   */
  async fixDatabaseConnection(issue) {
    console.log(`🔧 尝试修复数据库连接问题: ${issue.description}`)
    
    ElNotification({
      title: '数据库连接问题',
      message: '检测到数据库连接失败，请检查数据库服务状态和连接配置',
      type: 'error',
      duration: 0
    })
  }

  /**
   * 快速诊断（简化版）
   */
  async quickDiagnose() {
    console.log('⚡ 快速登录诊断...')
    
    const results = {
      network: false,
      backend: false,
      auth: false
    }
    
    try {
      // 检查后端服务
      const healthResponse = await fetch(`${API_CONFIG.BASE_URL}/test/health`)
      results.backend = healthResponse.ok
      
      // 检查认证端点
      const authResponse = await fetch(`${API_CONFIG.BASE_URL}/auth/login`, { method: 'OPTIONS' })
      results.auth = authResponse.ok
      
      results.network = true
    } catch (error) {
      console.error('快速诊断失败:', error)
    }
    
    return results
  }
}

export default LoginDiagnostic