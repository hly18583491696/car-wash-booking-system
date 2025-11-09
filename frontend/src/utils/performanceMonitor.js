/**
 * 性能监控工具
 * 用于实时监控管理页面的性能指标和稳定性
 */

class PerformanceMonitor {
  constructor() {
    this.metrics = {
      pageLoad: null,
      tabSwitches: [],
      apiCalls: [],
      memoryUsage: [],
      errors: [],
      renderTimes: []
    };
    
    this.observers = [];
    this.isMonitoring = false;
    this.startTime = null;
  }

  /**
   * 开始性能监控
   */
  startMonitoring() {
    if (this.isMonitoring) {
      console.warn('性能监控已在运行中');
      return;
    }

    this.isMonitoring = true;
    this.startTime = performance.now();
    
    console.log('🚀 开始性能监控...');
    
    // 监控页面加载性能
    this.monitorPageLoad();
    
    // 监控内存使用
    this.monitorMemoryUsage();
    
    // 监控渲染性能
    this.monitorRenderPerformance();
    
    // 监控错误
    this.monitorErrors();
    
    // 定期收集指标
    this.startMetricsCollection();
  }

  /**
   * 停止性能监控
   */
  stopMonitoring() {
    if (!this.isMonitoring) {
      return;
    }

    this.isMonitoring = false;
    
    // 清理观察者
    this.observers.forEach(observer => {
      if (observer.disconnect) {
        observer.disconnect();
      }
    });
    this.observers = [];
    
    console.log('⏹️ 性能监控已停止');
    
    return this.generateReport();
  }

  /**
   * 监控页面加载性能
   */
  monitorPageLoad() {
    if (performance.timing) {
      const timing = performance.timing;
      this.metrics.pageLoad = {
        domContentLoaded: timing.domContentLoadedEventEnd - timing.navigationStart,
        loadComplete: timing.loadEventEnd - timing.navigationStart,
        domReady: timing.domInteractive - timing.navigationStart,
        firstPaint: this.getFirstPaint(),
        timestamp: Date.now()
      };
    }

    // 监控资源加载
    if (performance.getEntriesByType) {
      const resources = performance.getEntriesByType('resource');
      this.metrics.resourceLoad = resources.map(resource => ({
        name: resource.name,
        duration: resource.duration,
        size: resource.transferSize || 0,
        type: resource.initiatorType
      }));
    }
  }

  /**
   * 获取首次绘制时间
   */
  getFirstPaint() {
    if (performance.getEntriesByType) {
      const paintEntries = performance.getEntriesByType('paint');
      const firstPaint = paintEntries.find(entry => entry.name === 'first-paint');
      return firstPaint ? firstPaint.startTime : null;
    }
    return null;
  }

  /**
   * 监控内存使用
   */
  monitorMemoryUsage() {
    const collectMemory = () => {
      if (performance.memory) {
        this.metrics.memoryUsage.push({
          used: performance.memory.usedJSHeapSize,
          total: performance.memory.totalJSHeapSize,
          limit: performance.memory.jsHeapSizeLimit,
          timestamp: Date.now()
        });
      }
    };

    // 立即收集一次
    collectMemory();
    
    // 每5秒收集一次
    const memoryInterval = setInterval(() => {
      if (this.isMonitoring) {
        collectMemory();
      } else {
        clearInterval(memoryInterval);
      }
    }, 5000);
  }

  /**
   * 监控渲染性能
   */
  monitorRenderPerformance() {
    // 监控长任务
    if ('PerformanceObserver' in window) {
      try {
        const longTaskObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.metrics.renderTimes.push({
              type: 'long-task',
              duration: entry.duration,
              startTime: entry.startTime,
              timestamp: Date.now()
            });
          }
        });
        
        longTaskObserver.observe({ entryTypes: ['longtask'] });
        this.observers.push(longTaskObserver);
      } catch (e) {
        console.warn('长任务监控不支持:', e.message);
      }

      // 监控布局偏移
      try {
        const clsObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (entry.hadRecentInput) continue;
            
            this.metrics.renderTimes.push({
              type: 'layout-shift',
              value: entry.value,
              startTime: entry.startTime,
              timestamp: Date.now()
            });
          }
        });
        
        clsObserver.observe({ entryTypes: ['layout-shift'] });
        this.observers.push(clsObserver);
      } catch (e) {
        console.warn('布局偏移监控不支持:', e.message);
      }
    }
  }

  /**
   * 监控错误
   */
  monitorErrors() {
    // JavaScript错误
    const errorHandler = (event) => {
      this.metrics.errors.push({
        type: 'javascript',
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        stack: event.error ? event.error.stack : null,
        timestamp: Date.now()
      });
    };

    window.addEventListener('error', errorHandler);
    
    // Promise拒绝
    const rejectionHandler = (event) => {
      this.metrics.errors.push({
        type: 'promise-rejection',
        reason: event.reason,
        timestamp: Date.now()
      });
    };

    window.addEventListener('unhandledrejection', rejectionHandler);
    
    // 清理函数
    this.observers.push({
      disconnect: () => {
        window.removeEventListener('error', errorHandler);
        window.removeEventListener('unhandledrejection', rejectionHandler);
      }
    });
  }

  /**
   * 开始计时
   */
  start(label = 'default') {
    const startTime = performance.now();
    return {
      label,
      startTime,
      id: `${label}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    };
  }

  /**
   * 结束计时
   */
  end(timer, label = null) {
    if (!timer || !timer.startTime) {
      console.warn('无效的计时器对象');
      return 0;
    }
    
    const endTime = performance.now();
    const duration = endTime - timer.startTime;
    const finalLabel = label || timer.label || 'unknown';
    
    // 记录到渲染时间数组
    this.metrics.renderTimes.push({
      label: finalLabel,
      duration,
      timestamp: Date.now()
    });
    
    console.log(`⏱️ ${finalLabel}: ${duration.toFixed(2)}ms`);
    return duration;
  }

  /**
   * 记录标签切换性能
   */
  recordTabSwitch(fromTab, toTab, duration) {
    this.metrics.tabSwitches.push({
      from: fromTab,
      to: toTab,
      duration,
      timestamp: Date.now()
    });
  }

  /**
   * 记录API调用性能
   */
  recordAPICall(url, method, duration, success, error = null) {
    this.metrics.apiCalls.push({
      url,
      method,
      duration,
      success,
      error,
      timestamp: Date.now()
    });
  }

  /**
   * 开始指标收集
   */
  startMetricsCollection() {
    const collectInterval = setInterval(() => {
      if (!this.isMonitoring) {
        clearInterval(collectInterval);
        return;
      }

      // 收集当前性能指标
      this.collectCurrentMetrics();
    }, 10000); // 每10秒收集一次
  }

  /**
   * 收集当前性能指标
   */
  collectCurrentMetrics() {
    const now = Date.now();
    
    // 计算平均响应时间
    const recentAPICalls = this.metrics.apiCalls.filter(
      call => now - call.timestamp < 60000 // 最近1分钟
    );
    
    const avgAPIResponse = recentAPICalls.length > 0 
      ? recentAPICalls.reduce((sum, call) => sum + call.duration, 0) / recentAPICalls.length
      : 0;

    // 计算平均标签切换时间
    const recentTabSwitches = this.metrics.tabSwitches.filter(
      tab => now - tab.timestamp < 60000 // 最近1分钟
    );
    
    const avgTabSwitch = recentTabSwitches.length > 0
      ? recentTabSwitches.reduce((sum, tab) => sum + tab.duration, 0) / recentTabSwitches.length
      : 0;

    // 输出实时指标
    console.log(`📊 实时性能指标 (${new Date().toLocaleTimeString()}):`, {
      avgAPIResponse: `${avgAPIResponse.toFixed(2)}ms`,
      avgTabSwitch: `${avgTabSwitch.toFixed(2)}ms`,
      errorCount: this.metrics.errors.length,
      memoryUsed: performance.memory ? `${(performance.memory.usedJSHeapSize / 1024 / 1024).toFixed(2)}MB` : 'N/A'
    });
  }

  /**
   * 生成性能报告
   */
  generateReport() {
    const endTime = performance.now();
    const totalDuration = endTime - this.startTime;
    
    const report = {
      summary: {
        monitoringDuration: totalDuration,
        totalErrors: this.metrics.errors.length,
        totalAPIcalls: this.metrics.apiCalls.length,
        totalTabSwitches: this.metrics.tabSwitches.length
      },
      performance: {
        pageLoad: this.metrics.pageLoad,
        averageAPIResponse: this.calculateAverageAPIResponse(),
        averageTabSwitch: this.calculateAverageTabSwitch(),
        memoryTrend: this.analyzeMemoryTrend(),
        renderingIssues: this.analyzeRenderingIssues()
      },
      stability: {
        errorRate: this.calculateErrorRate(),
        errors: this.metrics.errors,
        criticalIssues: this.identifyCriticalIssues()
      },
      recommendations: this.generateRecommendations()
    };

    console.log('📈 性能监控报告:', report);
    return report;
  }

  /**
   * 计算平均API响应时间
   */
  calculateAverageAPIResponse() {
    if (this.metrics.apiCalls.length === 0) return 0;
    
    const totalDuration = this.metrics.apiCalls.reduce((sum, call) => sum + call.duration, 0);
    return totalDuration / this.metrics.apiCalls.length;
  }

  /**
   * 计算平均标签切换时间
   */
  calculateAverageTabSwitch() {
    if (this.metrics.tabSwitches.length === 0) return 0;
    
    const totalDuration = this.metrics.tabSwitches.reduce((sum, tab) => sum + tab.duration, 0);
    return totalDuration / this.metrics.tabSwitches.length;
  }

  /**
   * 分析内存趋势
   */
  analyzeMemoryTrend() {
    if (this.metrics.memoryUsage.length < 2) return null;
    
    const first = this.metrics.memoryUsage[0];
    const last = this.metrics.memoryUsage[this.metrics.memoryUsage.length - 1];
    
    return {
      initial: first.used,
      final: last.used,
      increase: last.used - first.used,
      trend: last.used > first.used ? 'increasing' : 'stable'
    };
  }

  /**
   * 分析渲染问题
   */
  analyzeRenderingIssues() {
    const longTasks = this.metrics.renderTimes.filter(entry => entry.type === 'long-task');
    const layoutShifts = this.metrics.renderTimes.filter(entry => entry.type === 'layout-shift');
    
    return {
      longTaskCount: longTasks.length,
      averageLongTaskDuration: longTasks.length > 0 
        ? longTasks.reduce((sum, task) => sum + task.duration, 0) / longTasks.length 
        : 0,
      layoutShiftCount: layoutShifts.length,
      cumulativeLayoutShift: layoutShifts.reduce((sum, shift) => sum + shift.value, 0)
    };
  }

  /**
   * 计算错误率
   */
  calculateErrorRate() {
    const totalOperations = this.metrics.apiCalls.length + this.metrics.tabSwitches.length;
    if (totalOperations === 0) return 0;
    
    return (this.metrics.errors.length / totalOperations) * 100;
  }

  /**
   * 识别关键问题
   */
  identifyCriticalIssues() {
    const issues = [];
    
    // 检查高错误率
    const errorRate = this.calculateErrorRate();
    if (errorRate > 5) {
      issues.push({
        type: 'high-error-rate',
        severity: 'critical',
        message: `错误率过高: ${errorRate.toFixed(2)}%`
      });
    }
    
    // 检查慢API响应
    const avgAPIResponse = this.calculateAverageAPIResponse();
    if (avgAPIResponse > 2000) {
      issues.push({
        type: 'slow-api-response',
        severity: 'warning',
        message: `API响应时间过慢: ${avgAPIResponse.toFixed(2)}ms`
      });
    }
    
    // 检查内存泄漏
    const memoryTrend = this.analyzeMemoryTrend();
    if (memoryTrend && memoryTrend.increase > 50 * 1024 * 1024) { // 50MB
      issues.push({
        type: 'memory-leak',
        severity: 'warning',
        message: `可能存在内存泄漏: 增加${(memoryTrend.increase / 1024 / 1024).toFixed(2)}MB`
      });
    }
    
    return issues;
  }

  /**
   * 生成优化建议
   */
  generateRecommendations() {
    const recommendations = [];
    const criticalIssues = this.identifyCriticalIssues();
    
    criticalIssues.forEach(issue => {
      switch (issue.type) {
        case 'high-error-rate':
          recommendations.push('改进错误处理机制，增加重试逻辑');
          break;
        case 'slow-api-response':
          recommendations.push('优化API性能，考虑增加缓存或分页');
          break;
        case 'memory-leak':
          recommendations.push('检查内存泄漏，确保正确清理事件监听器和定时器');
          break;
      }
    });
    
    // 基于指标的通用建议
    const avgTabSwitch = this.calculateAverageTabSwitch();
    if (avgTabSwitch > 500) {
      recommendations.push('优化标签切换性能，考虑使用虚拟滚动或懒加载');
    }
    
    return recommendations;
  }
}

// 创建全局实例
const performanceMonitor = new PerformanceMonitor();

// 导出
export default PerformanceMonitor;
export { performanceMonitor };

// 如果在浏览器环境中
if (typeof window !== 'undefined') {
  window.PerformanceMonitor = PerformanceMonitor;
  window.performanceMonitor = performanceMonitor;
  
  // 自动开始监控（如果URL参数指定）
  if (window.location.search.includes('startPerformanceMonitor=true')) {
    performanceMonitor.startMonitoring();
    
    // 页面卸载时生成报告
    window.addEventListener('beforeunload', () => {
      performanceMonitor.stopMonitoring();
    });
  }
}