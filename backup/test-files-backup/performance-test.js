// 性能测试工具
export class PerformanceTest {
  constructor() {
    this.measurements = {}
  }

  // 开始测量
  start(name) {
    this.measurements[name] = {
      startTime: performance.now(),
      endTime: null,
      duration: null
    }
    return this.measurements[name].startTime
  }

  // 结束测量
  end(name) {
    if (!this.measurements[name]) {
      console.warn(`性能测量 "${name}" 未找到`)
      return null
    }

    this.measurements[name].endTime = performance.now()
    this.measurements[name].duration = this.measurements[name].endTime - this.measurements[name].startTime
    
    console.log(`🚀 性能测量 - ${name}: ${this.measurements[name].duration.toFixed(2)}ms`)
    return this.measurements[name].duration
  }

  // 获取所有测量结果
  getResults() {
    return this.measurements
  }

  // 生成性能报告
  generateReport() {
    console.group('📊 性能测试报告')
    
    Object.entries(this.measurements).forEach(([name, data]) => {
      if (data.duration !== null) {
        const status = data.duration < 100 ? '✅ 优秀' : 
                      data.duration < 300 ? '⚠️ 良好' : 
                      '❌ 需要优化'
        console.log(`${name}: ${data.duration.toFixed(2)}ms ${status}`)
      }
    })
    
    console.groupEnd()
  }

  // 清理测量数据
  clear() {
    this.measurements = {}
  }
}

// 导出单例实例
export const performanceTest = new PerformanceTest()