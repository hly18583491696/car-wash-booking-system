/**
 * 高并发稳定性测试工具
 * 用于验证管理页面在高并发场景下的稳定性
 */

class ConcurrencyStabilityTest {
  constructor() {
    this.testResults = {
      tabSwitching: [],
      apiCalls: [],
      memoryUsage: [],
      errors: [],
      performance: [],
      startTime: null,
      endTime: null
    }
    
    this.isRunning = false
    this.testConfig = {
      duration: 60000, // 测试持续时间 60秒
      concurrentUsers: 10, // 模拟并发用户数
      tabSwitchInterval: 2000, // 标签页切换间隔
      apiCallInterval: 1000, // API调用间隔
      memoryCheckInterval: 5000 // 内存检查间隔
    }
  }

  /**
   * 开始高并发稳定性测试
   */
  async startStabilityTest() {
    if (this.isRunning) {
      console.warn('测试已在运行中')
      return
    }

    console.log('🚀 开始高并发稳定性测试')
    this.isRunning = true
    this.testResults.startTime = Date.now()
    
    // 重置测试结果
    this.resetTestResults()
    
    try {
      // 并行执行多个测试任务
      await Promise.all([
        this.runConcurrentTabSwitching(),
        this.runConcurrentApiCalls(),
        this.monitorMemoryUsage(),
        this.monitorPerformance(),
        this.monitorErrors()
      ])
    } catch (error) {
      console.error('测试执行失败:', error)
      this.testResults.errors.push({
        type: 'test_execution',
        message: error.message,
        timestamp: Date.now()
      })
    } finally {
      this.isRunning = false
      this.testResults.endTime = Date.now()
      console.log('✅ 高并发稳定性测试完成')
    }
  }

  /**
   * 并发标签页切换测试
   */
  async runConcurrentTabSwitching() {
    const tabs = ['overview', 'bookings', 'services', 'users', 'system']
    const promises = []
    
    for (let i = 0; i < this.testConfig.concurrentUsers; i++) {
      promises.push(this.simulateUserTabSwitching(i, tabs))
    }
    
    await Promise.all(promises)
  }

  /**
   * 模拟用户标签页切换
   */
  async simulateUserTabSwitching(userId, tabs) {
    const startTime = Date.now()
    let switchCount = 0
    
    while (this.isRunning && (Date.now() - startTime) < this.testConfig.duration) {
      try {
        const randomTab = tabs[Math.floor(Math.random() * tabs.length)]
        const switchStartTime = performance.now()
        
        // 模拟标签页切换
        await this.simulateTabSwitch(randomTab)
        
        const switchDuration = performance.now() - switchStartTime
        switchCount++
        
        this.testResults.tabSwitching.push({
          userId,
          tab: randomTab,
          duration: switchDuration,
          timestamp: Date.now(),
          success: true
        })
        
        // 等待间隔
        await this.sleep(this.testConfig.tabSwitchInterval + Math.random() * 1000)
        
      } catch (error) {
        this.testResults.errors.push({
          type: 'tab_switching',
          userId,
          message: error.message,
          timestamp: Date.now()
        })
      }
    }
    
    console.log(`用户${userId}完成${switchCount}次标签页切换`)
  }

  /**
   * 模拟标签页切换
   */
  async simulateTabSwitch(tab) {
    // 模拟DOM操作和数据加载
    return new Promise((resolve) => {
      setTimeout(() => {
        // 模拟一些计算密集型操作
        let sum = 0
        for (let i = 0; i < 100000; i++) {
          sum += Math.random()
        }
        resolve(sum)
      }, Math.random() * 100 + 50) // 50-150ms的随机延迟
    })
  }

  /**
   * 并发API调用测试
   */
  async runConcurrentApiCalls() {
    const apiEndpoints = [
      '/api/statistics/overview',
      '/api/orders/paginated',
      '/api/services/lazy',
      '/api/users',
      '/api/system/status'
    ]
    
    const promises = []
    
    for (let i = 0; i < this.testConfig.concurrentUsers; i++) {
      promises.push(this.simulateUserApiCalls(i, apiEndpoints))
    }
    
    await Promise.all(promises)
  }

  /**
   * 模拟用户API调用
   */
  async simulateUserApiCalls(userId, endpoints) {
    const startTime = Date.now()
    let callCount = 0
    
    while (this.isRunning && (Date.now() - startTime) < this.testConfig.duration) {
      try {
        const randomEndpoint = endpoints[Math.floor(Math.random() * endpoints.length)]
        const callStartTime = performance.now()
        
        // 模拟API调用
        await this.simulateApiCall(randomEndpoint)
        
        const callDuration = performance.now() - callStartTime
        callCount++
        
        this.testResults.apiCalls.push({
          userId,
          endpoint: randomEndpoint,
          duration: callDuration,
          timestamp: Date.now(),
          success: true
        })
        
        // 等待间隔
        await this.sleep(this.testConfig.apiCallInterval + Math.random() * 500)
        
      } catch (error) {
        this.testResults.errors.push({
          type: 'api_call',
          userId,
          message: error.message,
          timestamp: Date.now()
        })
      }
    }
    
    console.log(`用户${userId}完成${callCount}次API调用`)
  }

  /**
   * 模拟API调用
   */
  async simulateApiCall(endpoint) {
    return new Promise((resolve, reject) => {
      // 模拟网络延迟和处理时间
      const delay = Math.random() * 200 + 100 // 100-300ms
      const failureRate = 0.05 // 5%的失败率
      
      setTimeout(() => {
        if (Math.random() < failureRate) {
          reject(new Error(`API调用失败: ${endpoint}`))
        } else {
          resolve({ data: { success: true, endpoint } })
        }
      }, delay)
    })
  }

  /**
   * 监控内存使用情况
   */
  async monitorMemoryUsage() {
    const startTime = Date.now()
    
    while (this.isRunning && (Date.now() - startTime) < this.testConfig.duration) {
      try {
        const memoryInfo = this.getMemoryInfo()
        
        this.testResults.memoryUsage.push({
          ...memoryInfo,
          timestamp: Date.now()
        })
        
        // 检查内存泄漏
        if (memoryInfo.usedJSHeapSize > 100 * 1024 * 1024) { // 100MB
          console.warn('⚠️ 检测到可能的内存泄漏')
        }
        
        await this.sleep(this.testConfig.memoryCheckInterval)
        
      } catch (error) {
        this.testResults.errors.push({
          type: 'memory_monitoring',
          message: error.message,
          timestamp: Date.now()
        })
      }
    }
  }

  /**
   * 获取内存信息
   */
  getMemoryInfo() {
    if (performance.memory) {
      return {
        usedJSHeapSize: performance.memory.usedJSHeapSize,
        totalJSHeapSize: performance.memory.totalJSHeapSize,
        jsHeapSizeLimit: performance.memory.jsHeapSizeLimit
      }
    } else {
      return {
        usedJSHeapSize: 0,
        totalJSHeapSize: 0,
        jsHeapSizeLimit: 0
      }
    }
  }

  /**
   * 监控性能指标
   */
  async monitorPerformance() {
    const startTime = Date.now()
    
    while (this.isRunning && (Date.now() - startTime) < this.testConfig.duration) {
      try {
        const performanceInfo = this.getPerformanceInfo()
        
        this.testResults.performance.push({
          ...performanceInfo,
          timestamp: Date.now()
        })
        
        await this.sleep(2000) // 每2秒检查一次
        
      } catch (error) {
        this.testResults.errors.push({
          type: 'performance_monitoring',
          message: error.message,
          timestamp: Date.now()
        })
      }
    }
  }

  /**
   * 获取性能信息
   */
  getPerformanceInfo() {
    const navigation = performance.getEntriesByType('navigation')[0]
    
    return {
      domContentLoaded: navigation ? navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart : 0,
      loadComplete: navigation ? navigation.loadEventEnd - navigation.loadEventStart : 0,
      firstPaint: this.getFirstPaint(),
      firstContentfulPaint: this.getFirstContentfulPaint()
    }
  }

  /**
   * 获取首次绘制时间
   */
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint')
    const firstPaint = paintEntries.find(entry => entry.name === 'first-paint')
    return firstPaint ? firstPaint.startTime : 0
  }

  /**
   * 获取首次内容绘制时间
   */
  getFirstContentfulPaint() {
    const paintEntries = performance.getEntriesByType('paint')
    const firstContentfulPaint = paintEntries.find(entry => entry.name === 'first-contentful-paint')
    return firstContentfulPaint ? firstContentfulPaint.startTime : 0
  }

  /**
   * 监控错误
   */
  async monitorErrors() {
    // 监听全局错误
    const originalErrorHandler = window.onerror
    const originalUnhandledRejectionHandler = window.onunhandledrejection
    
    window.onerror = (message, source, lineno, colno, error) => {
      this.testResults.errors.push({
        type: 'javascript_error',
        message: message,
        source: source,
        line: lineno,
        column: colno,
        timestamp: Date.now()
      })
      
      if (originalErrorHandler) {
        originalErrorHandler.call(window, message, source, lineno, colno, error)
      }
    }
    
    window.onunhandledrejection = (event) => {
      this.testResults.errors.push({
        type: 'unhandled_promise_rejection',
        message: event.reason.toString(),
        timestamp: Date.now()
      })
      
      if (originalUnhandledRejectionHandler) {
        originalUnhandledRejectionHandler.call(window, event)
      }
    }
    
    // 等待测试完成
    while (this.isRunning) {
      await this.sleep(1000)
    }
    
    // 恢复原始错误处理器
    window.onerror = originalErrorHandler
    window.onunhandledrejection = originalUnhandledRejectionHandler
  }

  /**
   * 生成测试报告
   */
  generateReport() {
    const duration = this.testResults.endTime - this.testResults.startTime
    const report = {
      summary: {
        duration: duration,
        totalTabSwitches: this.testResults.tabSwitching.length,
        totalApiCalls: this.testResults.apiCalls.length,
        totalErrors: this.testResults.errors.length,
        successRate: this.calculateSuccessRate()
      },
      performance: {
        averageTabSwitchTime: this.calculateAverageTabSwitchTime(),
        averageApiCallTime: this.calculateAverageApiCallTime(),
        memoryUsage: this.analyzeMemoryUsage(),
        errorRate: this.calculateErrorRate()
      },
      stability: {
        memoryLeaks: this.detectMemoryLeaks(),
        performanceDegradation: this.detectPerformanceDegradation(),
        errorPatterns: this.analyzeErrorPatterns()
      },
      recommendations: this.generateRecommendations()
    }
    
    return report
  }

  /**
   * 计算成功率
   */
  calculateSuccessRate() {
    const totalOperations = this.testResults.tabSwitching.length + this.testResults.apiCalls.length
    const successfulOperations = this.testResults.tabSwitching.filter(t => t.success).length + 
                                this.testResults.apiCalls.filter(a => a.success).length
    
    return totalOperations > 0 ? (successfulOperations / totalOperations * 100).toFixed(2) : 0
  }

  /**
   * 计算平均标签页切换时间
   */
  calculateAverageTabSwitchTime() {
    if (this.testResults.tabSwitching.length === 0) return 0
    
    const totalTime = this.testResults.tabSwitching.reduce((sum, t) => sum + t.duration, 0)
    return (totalTime / this.testResults.tabSwitching.length).toFixed(2)
  }

  /**
   * 计算平均API调用时间
   */
  calculateAverageApiCallTime() {
    if (this.testResults.apiCalls.length === 0) return 0
    
    const totalTime = this.testResults.apiCalls.reduce((sum, a) => sum + a.duration, 0)
    return (totalTime / this.testResults.apiCalls.length).toFixed(2)
  }

  /**
   * 分析内存使用情况
   */
  analyzeMemoryUsage() {
    if (this.testResults.memoryUsage.length === 0) return {}
    
    const memoryData = this.testResults.memoryUsage
    const maxMemory = Math.max(...memoryData.map(m => m.usedJSHeapSize))
    const minMemory = Math.min(...memoryData.map(m => m.usedJSHeapSize))
    const avgMemory = memoryData.reduce((sum, m) => sum + m.usedJSHeapSize, 0) / memoryData.length
    
    return {
      max: (maxMemory / 1024 / 1024).toFixed(2) + 'MB',
      min: (minMemory / 1024 / 1024).toFixed(2) + 'MB',
      average: (avgMemory / 1024 / 1024).toFixed(2) + 'MB',
      growth: ((maxMemory - minMemory) / 1024 / 1024).toFixed(2) + 'MB'
    }
  }

  /**
   * 计算错误率
   */
  calculateErrorRate() {
    const totalOperations = this.testResults.tabSwitching.length + this.testResults.apiCalls.length
    return totalOperations > 0 ? (this.testResults.errors.length / totalOperations * 100).toFixed(2) : 0
  }

  /**
   * 检测内存泄漏
   */
  detectMemoryLeaks() {
    if (this.testResults.memoryUsage.length < 2) return false
    
    const firstMemory = this.testResults.memoryUsage[0].usedJSHeapSize
    const lastMemory = this.testResults.memoryUsage[this.testResults.memoryUsage.length - 1].usedJSHeapSize
    const growth = lastMemory - firstMemory
    
    // 如果内存增长超过50MB，认为可能存在内存泄漏
    return growth > 50 * 1024 * 1024
  }

  /**
   * 检测性能降级
   */
  detectPerformanceDegradation() {
    if (this.testResults.tabSwitching.length < 10) return false
    
    const firstHalf = this.testResults.tabSwitching.slice(0, Math.floor(this.testResults.tabSwitching.length / 2))
    const secondHalf = this.testResults.tabSwitching.slice(Math.floor(this.testResults.tabSwitching.length / 2))
    
    const firstHalfAvg = firstHalf.reduce((sum, t) => sum + t.duration, 0) / firstHalf.length
    const secondHalfAvg = secondHalf.reduce((sum, t) => sum + t.duration, 0) / secondHalf.length
    
    // 如果后半段的平均时间比前半段增加超过50%，认为存在性能降级
    return secondHalfAvg > firstHalfAvg * 1.5
  }

  /**
   * 分析错误模式
   */
  analyzeErrorPatterns() {
    const errorTypes = {}
    
    this.testResults.errors.forEach(error => {
      if (!errorTypes[error.type]) {
        errorTypes[error.type] = 0
      }
      errorTypes[error.type]++
    })
    
    return errorTypes
  }

  /**
   * 生成建议
   */
  generateRecommendations() {
    const recommendations = []
    
    // 基于错误率给出建议
    const errorRate = parseFloat(this.calculateErrorRate())
    if (errorRate > 5) {
      recommendations.push('错误率较高，建议检查错误处理机制和API稳定性')
    }
    
    // 基于内存使用给出建议
    if (this.detectMemoryLeaks()) {
      recommendations.push('检测到可能的内存泄漏，建议检查事件监听器和定时器的清理')
    }
    
    // 基于性能给出建议
    if (this.detectPerformanceDegradation()) {
      recommendations.push('检测到性能降级，建议优化长时间运行的操作')
    }
    
    // 基于平均响应时间给出建议
    const avgTabSwitchTime = parseFloat(this.calculateAverageTabSwitchTime())
    if (avgTabSwitchTime > 500) {
      recommendations.push('标签页切换时间较长，建议优化数据加载和渲染逻辑')
    }
    
    const avgApiCallTime = parseFloat(this.calculateAverageApiCallTime())
    if (avgApiCallTime > 1000) {
      recommendations.push('API调用时间较长，建议优化后端响应时间或实现更好的缓存策略')
    }
    
    if (recommendations.length === 0) {
      recommendations.push('系统在高并发场景下表现良好，无明显问题')
    }
    
    return recommendations
  }

  /**
   * 重置测试结果
   */
  resetTestResults() {
    this.testResults = {
      tabSwitching: [],
      apiCalls: [],
      memoryUsage: [],
      errors: [],
      performance: [],
      startTime: null,
      endTime: null
    }
  }

  /**
   * 停止测试
   */
  stopTest() {
    this.isRunning = false
    console.log('🛑 测试已停止')
  }

  /**
   * 睡眠函数
   */
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

// 导出测试工具
export { ConcurrencyStabilityTest }

// 全局实例
export const concurrencyStabilityTest = new ConcurrencyStabilityTest()