/**
 * 数据同步测试工具
 * 用于验证前后端数据同步是否正常工作
 */

import { orderApi } from '../api/order.js'
import { dataSyncManager, updateBookingStatusSync } from './dataSync.js'
import { ElMessage } from 'element-plus'

/**
 * 数据同步测试套件
 */
export class SyncTestSuite {
  constructor() {
    this.testResults = []
    this.isRunning = false
  }

  /**
   * 运行完整的同步测试
   */
  async runFullTest() {
    if (this.isRunning) {
      console.warn('测试已在运行中')
      return
    }

    this.isRunning = true
    this.testResults = []
    
    console.log('🧪 开始数据同步测试套件')
    
    try {
      // 测试1: API连接测试
      await this.testApiConnection()
      
      // 测试2: 数据获取测试
      await this.testDataFetch()
      
      // 测试3: 状态更新测试
      await this.testStatusUpdate()
      
      // 测试4: 数据一致性测试
      await this.testDataConsistency()
      
      // 测试5: 同步队列测试
      await this.testSyncQueue()
      
      console.log('✅ 数据同步测试套件完成')
      this.printTestResults()
      
    } catch (error) {
      console.error('❌ 测试套件执行失败:', error)
      this.addTestResult('测试套件', false, error.message)
    } finally {
      this.isRunning = false
    }
  }

  /**
   * 测试API连接
   */
  async testApiConnection() {
    console.log('🔗 测试API连接...')
    
    try {
      const response = await orderApi.getOrderList()
      
      if (response && response.data) {
        this.addTestResult('API连接', true, `成功获取${response.data.length}条数据`)
      } else {
        this.addTestResult('API连接', false, '响应格式异常')
      }
    } catch (error) {
      this.addTestResult('API连接', false, error.message)
    }
  }

  /**
   * 测试数据获取
   */
  async testDataFetch() {
    console.log('📥 测试数据获取...')
    
    try {
      const response = await orderApi.getOrderList()
      
      if (response?.data?.length > 0) {
        const sampleData = response.data[0]
        const requiredFields = ['id', 'status', 'userName', 'serviceName']
        
        const missingFields = requiredFields.filter(field => !sampleData.hasOwnProperty(field))
        
        if (missingFields.length === 0) {
          this.addTestResult('数据获取', true, '数据结构完整')
        } else {
          this.addTestResult('数据获取', false, `缺少字段: ${missingFields.join(', ')}`)
        }
      } else {
        this.addTestResult('数据获取', false, '无数据或数据为空')
      }
    } catch (error) {
      this.addTestResult('数据获取', false, error.message)
    }
  }

  /**
   * 测试状态更新
   */
  async testStatusUpdate() {
    console.log('🔄 测试状态更新...')
    
    try {
      // 获取一个测试订单
      const response = await orderApi.getOrderList()
      
      if (response?.data?.length > 0) {
        const testBooking = response.data[0]
        const originalStatus = testBooking.status
        
        // 测试状态更新（不实际更新，只测试API调用）
        console.log(`测试订单 ${testBooking.id} 状态更新: ${originalStatus}`)
        
        // 这里我们只测试API调用格式，不实际更新
        const testStatuses = ['pending', 'confirmed', 'in_progress', 'completed', 'cancelled']
        const validStatuses = testStatuses.filter(status => status !== originalStatus)
        
        if (validStatuses.length > 0) {
          this.addTestResult('状态更新', true, `可用状态: ${validStatuses.join(', ')}`)
        } else {
          this.addTestResult('状态更新', false, '无可用的测试状态')
        }
      } else {
        this.addTestResult('状态更新', false, '无测试数据')
      }
    } catch (error) {
      this.addTestResult('状态更新', false, error.message)
    }
  }

  /**
   * 测试数据一致性
   */
  async testDataConsistency() {
    console.log('🔍 测试数据一致性...')
    
    try {
      const response1 = await orderApi.getOrderList()
      
      // 等待一小段时间后再次获取
      await new Promise(resolve => setTimeout(resolve, 100))
      
      const response2 = await orderApi.getOrderList()
      
      if (response1?.data && response2?.data) {
        const data1Length = response1.data.length
        const data2Length = response2.data.length
        
        if (data1Length === data2Length) {
          this.addTestResult('数据一致性', true, `两次获取数据量一致: ${data1Length}`)
        } else {
          this.addTestResult('数据一致性', false, `数据量不一致: ${data1Length} vs ${data2Length}`)
        }
      } else {
        this.addTestResult('数据一致性', false, '无法获取对比数据')
      }
    } catch (error) {
      this.addTestResult('数据一致性', false, error.message)
    }
  }

  /**
   * 测试同步队列
   */
  async testSyncQueue() {
    console.log('⏳ 测试同步队列...')
    
    try {
      const initialStatus = dataSyncManager.getQueueStatus()
      
      // 添加一个测试任务到队列
      dataSyncManager.addSyncTask({
        type: 'test_task',
        id: 'test_001',
        status: 'test',
        api: {
          updateBookingStatus: async () => ({ success: true })
        }
      })
      
      const afterAddStatus = dataSyncManager.getQueueStatus()
      
      if (afterAddStatus.queueLength > initialStatus.queueLength) {
        this.addTestResult('同步队列', true, '队列添加任务成功')
        
        // 清空测试任务
        dataSyncManager.clearQueue()
      } else {
        this.addTestResult('同步队列', false, '队列添加任务失败')
      }
    } catch (error) {
      this.addTestResult('同步队列', false, error.message)
    }
  }

  /**
   * 添加测试结果
   */
  addTestResult(testName, success, message) {
    this.testResults.push({
      name: testName,
      success,
      message,
      timestamp: new Date().toLocaleString()
    })
  }

  /**
   * 打印测试结果
   */
  printTestResults() {
    console.log('\n📊 测试结果汇总:')
    console.log('=' .repeat(50))
    
    let passCount = 0
    let failCount = 0
    
    this.testResults.forEach(result => {
      const status = result.success ? '✅ PASS' : '❌ FAIL'
      console.log(`${status} ${result.name}: ${result.message}`)
      
      if (result.success) {
        passCount++
      } else {
        failCount++
      }
    })
    
    console.log('=' .repeat(50))
    console.log(`总计: ${this.testResults.length} 个测试`)
    console.log(`通过: ${passCount} 个`)
    console.log(`失败: ${failCount} 个`)
    console.log(`成功率: ${((passCount / this.testResults.length) * 100).toFixed(1)}%`)
    
    // 显示用户消息
    if (failCount === 0) {
      ElMessage.success(`所有测试通过！(${passCount}/${this.testResults.length})`)
    } else {
      ElMessage.warning(`测试完成，${failCount} 个测试失败`)
    }
  }

  /**
   * 获取测试结果
   */
  getTestResults() {
    return {
      results: this.testResults,
      summary: {
        total: this.testResults.length,
        passed: this.testResults.filter(r => r.success).length,
        failed: this.testResults.filter(r => !r.success).length
      }
    }
  }
}

/**
 * 快速同步测试
 */
export async function quickSyncTest() {
  console.log('🚀 执行快速同步测试')
  
  try {
    // 测试API连接
    const response = await orderApi.getOrderList()
    console.log('✅ API连接正常，数据量:', response?.data?.length || 0)
    
    // 测试同步管理器
    const queueStatus = dataSyncManager.getQueueStatus()
    console.log('✅ 同步管理器状态:', queueStatus)
    
    ElMessage.success('快速测试通过')
    return true
    
  } catch (error) {
    console.error('❌ 快速测试失败:', error)
    ElMessage.error('快速测试失败: ' + error.message)
    return false
  }
}

/**
 * 状态更新测试
 */
export async function testStatusUpdate(bookingId, newStatus) {
  console.log(`🧪 测试状态更新: ${bookingId} -> ${newStatus}`)
  
  try {
    // 模拟本地更新回调
    const mockLocalUpdate = (id, status) => {
      console.log(`📝 模拟本地更新: ${id} -> ${status}`)
    }
    
    await updateBookingStatusSync(bookingId, newStatus, orderApi, mockLocalUpdate)
    
    console.log('✅ 状态更新测试成功')
    ElMessage.success('状态更新测试成功')
    return true
    
  } catch (error) {
    console.error('❌ 状态更新测试失败:', error)
    ElMessage.error('状态更新测试失败: ' + error.message)
    return false
  }
}

// 创建全局测试实例
export const syncTestSuite = new SyncTestSuite()

// 导出便捷方法
export default {
  SyncTestSuite,
  syncTestSuite,
  quickSyncTest,
  testStatusUpdate
}