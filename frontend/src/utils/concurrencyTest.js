/**
 * 高并发测试工具
 * 用于验证管理页面在高并发场景下的稳定性
 */

class ConcurrencyTest {
  constructor() {
    this.testResults = [];
    this.activeRequests = 0;
    this.maxConcurrentRequests = 0;
    this.errors = [];
    this.startTime = null;
    this.endTime = null;
  }

  /**
   * 模拟并发API请求
   */
  async simulateAPIRequest(url, options = {}) {
    this.activeRequests++;
    this.maxConcurrentRequests = Math.max(
      this.maxConcurrentRequests,
      this.activeRequests,
    );

    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const startTime = performance.now();

    try {
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          ...options.headers,
        },
        ...options,
      });

      const endTime = performance.now();
      const duration = endTime - startTime;

      const result = {
        requestId,
        url,
        status: response.status,
        duration,
        success: response.ok,
        timestamp: new Date().toISOString(),
      };

      if (!response.ok) {
        this.errors.push({
          ...result,
          error: `HTTP ${response.status}: ${response.statusText}`,
        });
      }

      this.testResults.push(result);
      return result;
    } catch (error) {
      const endTime = performance.now();
      const duration = endTime - startTime;

      const result = {
        requestId,
        url,
        status: 0,
        duration,
        success: false,
        error: error.message,
        timestamp: new Date().toISOString(),
      };

      this.errors.push(result);
      this.testResults.push(result);
      return result;
    } finally {
      this.activeRequests--;
    }
  }

  /**
   * 模拟并发标签切换
   */
  async simulateTabSwitching(tabCount = 5, switchCount = 20) {
    console.log(
      `🔄 开始并发标签切换测试: ${switchCount}次切换，${tabCount}个标签`,
    );

    const tabs = ["overview", "bookings", "services", "users", "system"];
    const promises = [];
    const results = [];

    for (let i = 0; i < switchCount; i++) {
      const promise = new Promise(async (resolve) => {
        const tabIndex = Math.floor(
          Math.random() * Math.min(tabCount, tabs.length),
        );
        const tabName = tabs[tabIndex];
        const startTime = performance.now();

        try {
          // 模拟标签切换操作
          const event = new CustomEvent("tabSwitch", {
            detail: { tab: tabName, index: tabIndex },
          });

          // 模拟DOM操作延迟
          await new Promise((resolve) =>
            setTimeout(resolve, Math.random() * 50),
          );

          const endTime = performance.now();
          const duration = endTime - startTime;

          const result = {
            operation: "tabSwitch",
            tab: tabName,
            duration,
            success: true,
            timestamp: new Date().toISOString(),
          };

          results.push(result);
          resolve(result);
        } catch (error) {
          const endTime = performance.now();
          const duration = endTime - startTime;

          const result = {
            operation: "tabSwitch",
            tab: tabName,
            duration,
            success: false,
            error: error.message,
            timestamp: new Date().toISOString(),
          };

          results.push(result);
          this.errors.push(result);
          resolve(result);
        }
      });

      promises.push(promise);

      // 随机延迟，模拟真实用户行为
      if (i < switchCount - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, Math.random() * 100),
        );
      }
    }

    await Promise.all(promises);

    const successCount = results.filter((r) => r.success).length;
    const avgDuration =
      results.reduce((sum, r) => sum + r.duration, 0) / results.length;

    console.log(`✅ 标签切换测试完成: ${successCount}/${switchCount} 成功`);
    console.log(`⏱️  平均响应时间: ${avgDuration.toFixed(2)}ms`);

    return {
      operation: "tabSwitching",
      total: switchCount,
      successful: successCount,
      failed: switchCount - successCount,
      averageDuration: avgDuration,
      results,
    };
  }

  /**
   * 模拟并发数据加载
   */
  async simulateDataLoading(concurrency = 10, requestsPerType = 5) {
    console.log(
      `📊 开始并发数据加载测试: ${concurrency}个并发，每类${requestsPerType}个请求`,
    );

    const baseUrl = "http://localhost:8080/api";
    const endpoints = [
      "/admin/statistics/overview",
      "/admin/orders/list",
      "/admin/services/list",
      "/admin/users/list",
    ];

    const promises = [];

    for (let i = 0; i < concurrency; i++) {
      for (const endpoint of endpoints) {
        for (let j = 0; j < requestsPerType; j++) {
          const promise = this.simulateAPIRequest(`${baseUrl}${endpoint}`, {
            headers: {
              Authorization: "Bearer test-token",
            },
          });
          promises.push(promise);

          // 小延迟避免瞬间大量请求
          await new Promise((resolve) =>
            setTimeout(resolve, Math.random() * 10),
          );
        }
      }
    }

    const results = await Promise.all(promises);

    const successCount = results.filter((r) => r.success).length;
    const avgDuration =
      results.reduce((sum, r) => sum + r.duration, 0) / results.length;
    const maxDuration = Math.max(...results.map((r) => r.duration));
    const minDuration = Math.min(...results.map((r) => r.duration));

    console.log(`✅ 数据加载测试完成: ${successCount}/${results.length} 成功`);
    console.log(
      `⏱️  响应时间 - 平均: ${avgDuration.toFixed(2)}ms, 最大: ${maxDuration.toFixed(2)}ms, 最小: ${minDuration.toFixed(2)}ms`,
    );
    console.log(`🔥 最大并发请求数: ${this.maxConcurrentRequests}`);

    return {
      operation: "dataLoading",
      total: results.length,
      successful: successCount,
      failed: results.length - successCount,
      averageDuration: avgDuration,
      maxDuration,
      minDuration,
      maxConcurrency: this.maxConcurrentRequests,
      results,
    };
  }

  /**
   * 模拟内存压力测试
   */
  async simulateMemoryPressure(iterations = 100) {
    console.log(`🧠 开始内存压力测试: ${iterations}次迭代`);

    const initialMemory = performance.memory
      ? performance.memory.usedJSHeapSize
      : 0;
    const memorySnapshots = [];
    const operations = [];

    for (let i = 0; i < iterations; i++) {
      const startTime = performance.now();

      try {
        // 创建大量数据模拟内存使用
        const largeArray = new Array(1000).fill(0).map((_, index) => ({
          id: index,
          data: `test_data_${index}_${Math.random().toString(36)}`,
          timestamp: Date.now(),
          nested: {
            value: Math.random(),
            array: new Array(100).fill(Math.random()),
          },
        }));

        // 模拟数据处理
        const processed = largeArray.map((item) => ({
          ...item,
          processed: true,
          hash: btoa(item.data).slice(0, 10),
        }));

        // 记录内存使用情况
        if (performance.memory) {
          memorySnapshots.push({
            iteration: i,
            usedJSHeapSize: performance.memory.usedJSHeapSize,
            totalJSHeapSize: performance.memory.totalJSHeapSize,
            jsHeapSizeLimit: performance.memory.jsHeapSizeLimit,
          });
        }

        const endTime = performance.now();
        const duration = endTime - startTime;

        operations.push({
          iteration: i,
          duration,
          success: true,
          dataSize: processed.length,
        });

        // 清理数据
        largeArray.length = 0;
        processed.length = 0;

        // 强制垃圾回收（如果支持）
        if (window.gc) {
          window.gc();
        }
      } catch (error) {
        const endTime = performance.now();
        const duration = endTime - startTime;

        operations.push({
          iteration: i,
          duration,
          success: false,
          error: error.message,
        });

        this.errors.push({
          operation: "memoryPressure",
          iteration: i,
          error: error.message,
        });
      }

      // 小延迟
      if (i % 10 === 0) {
        await new Promise((resolve) => setTimeout(resolve, 10));
      }
    }

    const finalMemory = performance.memory
      ? performance.memory.usedJSHeapSize
      : 0;
    const memoryIncrease = finalMemory - initialMemory;
    const successCount = operations.filter((op) => op.success).length;
    const avgDuration =
      operations.reduce((sum, op) => sum + op.duration, 0) / operations.length;

    console.log(`✅ 内存压力测试完成: ${successCount}/${iterations} 成功`);
    console.log(`🧠 内存变化: ${(memoryIncrease / 1024 / 1024).toFixed(2)}MB`);
    console.log(`⏱️  平均处理时间: ${avgDuration.toFixed(2)}ms`);

    return {
      operation: "memoryPressure",
      total: iterations,
      successful: successCount,
      failed: iterations - successCount,
      memoryIncrease,
      averageDuration: avgDuration,
      memorySnapshots,
      operations,
    };
  }

  /**
   * 运行完整的并发测试套件
   */
  async runFullConcurrencyTest() {
    console.log("🚀 开始高并发稳定性测试...");
    this.startTime = performance.now();

    const testSuite = [];

    try {
      // 1. 并发标签切换测试
      const tabTest = await this.simulateTabSwitching(5, 30);
      testSuite.push(tabTest);

      // 2. 并发数据加载测试
      const dataTest = await this.simulateDataLoading(8, 3);
      testSuite.push(dataTest);

      // 3. 内存压力测试
      const memoryTest = await this.simulateMemoryPressure(50);
      testSuite.push(memoryTest);

      this.endTime = performance.now();
      const totalDuration = this.endTime - this.startTime;

      // 生成综合报告
      const report = this.generateConcurrencyReport(testSuite, totalDuration);

      console.log("\n📊 高并发测试完成!");
      console.log(`⏱️  总耗时: ${(totalDuration / 1000).toFixed(2)}秒`);
      console.log(`❌ 总错误数: ${this.errors.length}`);

      return report;
    } catch (error) {
      console.error("❌ 并发测试失败:", error);
      this.errors.push({
        operation: "fullTest",
        error: error.message,
        timestamp: new Date().toISOString(),
      });

      return {
        success: false,
        error: error.message,
        partialResults: testSuite,
      };
    }
  }

  /**
   * 生成并发测试报告
   */
  generateConcurrencyReport(testSuite, totalDuration) {
    const totalOperations = testSuite.reduce(
      (sum, test) => sum + test.total,
      0,
    );
    const totalSuccessful = testSuite.reduce(
      (sum, test) => sum + test.successful,
      0,
    );
    const totalFailed = testSuite.reduce((sum, test) => sum + test.failed, 0);

    const report = {
      summary: {
        totalDuration: totalDuration,
        totalOperations,
        totalSuccessful,
        totalFailed,
        successRate: ((totalSuccessful / totalOperations) * 100).toFixed(2),
        maxConcurrency: this.maxConcurrentRequests,
        errorCount: this.errors.length,
      },
      testResults: testSuite,
      errors: this.errors,
      recommendations: this.generateRecommendations(testSuite),
    };

    // 输出详细报告
    console.log("\n📈 并发测试详细报告:");
    console.log(
      `✅ 成功率: ${report.summary.successRate}% (${totalSuccessful}/${totalOperations})`,
    );
    console.log(`🔥 最大并发: ${this.maxConcurrentRequests}`);
    console.log(`⚠️  错误数量: ${this.errors.length}`);

    if (this.errors.length > 0) {
      console.log("\n🚨 错误详情:");
      this.errors.slice(0, 5).forEach((error, index) => {
        console.log(
          `  ${index + 1}. ${error.operation}: ${error.error || error.message}`,
        );
      });
      if (this.errors.length > 5) {
        console.log(`  ... 还有 ${this.errors.length - 5} 个错误`);
      }
    }

    return report;
  }

  /**
   * 生成优化建议
   */
  generateRecommendations(testSuite) {
    const recommendations = [];

    // 分析响应时间
    const avgResponseTimes = testSuite.map((test) => test.averageDuration);
    const maxAvgResponseTime = Math.max(...avgResponseTimes);

    if (maxAvgResponseTime > 1000) {
      recommendations.push({
        type: "performance",
        priority: "high",
        message: "平均响应时间过长，建议优化API性能或增加缓存",
      });
    }

    // 分析错误率
    const errorRate =
      (this.errors.length /
        testSuite.reduce((sum, test) => sum + test.total, 0)) *
      100;

    if (errorRate > 5) {
      recommendations.push({
        type: "reliability",
        priority: "high",
        message: "错误率过高，需要改进错误处理和重试机制",
      });
    }

    // 分析并发性能
    if (this.maxConcurrentRequests > 20) {
      recommendations.push({
        type: "concurrency",
        priority: "medium",
        message: "高并发场景下建议实现请求队列和限流机制",
      });
    }

    return recommendations;
  }
}

// 导出测试类
export default ConcurrencyTest;

// 如果在浏览器环境中直接运行
if (typeof window !== "undefined") {
  window.ConcurrencyTest = ConcurrencyTest;
}
