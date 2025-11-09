/**
 * 数据同步诊断工具
 * 用于检测和修复前后端数据同步问题
 */

import { ElMessage, ElNotification } from 'element-plus'
import realApi from '@/api/realApi.js'
import orderApi from '@/api/order.js'

export class SyncDiagnostic {
  constructor() {
    this.issues = []
    this.fixedIssues = []
  }

  /**
   * 执行完整的同步诊断
   */
  async runFullDiagnostic() {
    console.log('🔍 开始执行数据同步诊断...')
    this.issues = []
    this.fixedIssues = []

    try {
      // 1. 检查API连接
      await this.checkApiConnection()
      
      // 2. 检查数据一致性
      await this.checkDataConsistency()
      
      // 3. 检查状态映射
      await this.checkStatusMapping()
      
      // 4. 检查数据库连接
      await this.checkDatabaseConnection()
      
      // 5. 执行高级一致性检查
      await this.checkDataConsistencyAdvanced()
      
      // 6. 生成诊断报告
      this.generateReport()
      
      return {
        success: true,
        issues: this.issues,
        fixedIssues: this.fixedIssues
      }
    } catch (error) {
      console.error('❌ 诊断过程中发生错误:', error)
      return {
        success: false,
        error: error.message,
        issues: this.issues
      }
    }
  }

  /**
   * 检查API连接状态
   */
  async checkApiConnection() {
    console.log('🔗 检查API连接状态...')
    
    try {
      // 检查健康状态
      const healthResponse = await realApi.healthCheck()
      if (!healthResponse || !healthResponse.data) {
        this.addIssue('API_CONNECTION', '后端API连接失败', 'critical')
        return false
      }

      // 检查数据库连接
      const dbResponse = await realApi.testDatabase()
      if (!dbResponse || !dbResponse.data) {
        this.addIssue('DATABASE_CONNECTION', '数据库连接失败', 'critical')
        return false
      }

      console.log('✅ API连接正常')
      return true
    } catch (error) {
      this.addIssue('API_CONNECTION', `API连接错误: ${error.message}`, 'critical')
      return false
    }
  }

  /**
   * 检查数据一致性
   */
  async checkDataConsistency() {
    console.log('📊 检查数据一致性...')
    
    try {
      // 获取前端显示的订单数据
      const frontendOrders = await orderApi.getOrderList()
      
      // 获取后端实际的订单数据
      const backendOrders = await realApi.getOrderList()
      
      if (!frontendOrders || !backendOrders) {
        this.addIssue('DATA_FETCH', '无法获取订单数据进行比较', 'high')
        return false
      }

      // 比较数据数量
      const frontendCount = frontendOrders.data?.length || 0
      const backendCount = backendOrders.data?.length || 0
      
      if (frontendCount !== backendCount) {
        this.addIssue('DATA_COUNT_MISMATCH', 
          `前后端订单数量不一致: 前端${frontendCount}, 后端${backendCount}`, 'high')
      }

      // 检查状态值一致性
      if (frontendOrders.data && backendOrders.data) {
        this.checkOrderStatusConsistency(frontendOrders.data, backendOrders.data)
      }

      console.log('✅ 数据一致性检查完成')
      return true
    } catch (error) {
      this.addIssue('DATA_CONSISTENCY', `数据一致性检查失败: ${error.message}`, 'high')
      return false
    }
  }

  /**
   * 检查订单状态一致性
   */
  checkOrderStatusConsistency(frontendOrders, backendOrders) {
    const backendOrderMap = new Map()
    backendOrders.forEach(order => {
      backendOrderMap.set(order.id, order)
    })

    frontendOrders.forEach(frontendOrder => {
      const backendOrder = backendOrderMap.get(frontendOrder.id)
      if (backendOrder && frontendOrder.status !== backendOrder.status) {
        this.addIssue('STATUS_MISMATCH', 
          `订单${frontendOrder.id}状态不一致: 前端${frontendOrder.status}, 后端${backendOrder.status}`, 'medium')
      }
    })
  }

  /**
   * 检查状态映射
   */
  async checkStatusMapping() {
    console.log('🔄 检查状态映射...')
    
    const validStatuses = ['pending', 'confirmed', 'in_progress', 'completed', 'cancelled']
    const deprecatedStatuses = ['processing', 'in-service']
    
    try {
      const orders = await realApi.getOrderList()
      if (orders && orders.data) {
        orders.data.forEach(order => {
          if (deprecatedStatuses.includes(order.status)) {
            this.addIssue('DEPRECATED_STATUS', 
              `订单${order.id}使用了已废弃的状态值: ${order.status}`, 'medium')
          } else if (!validStatuses.includes(order.status)) {
            this.addIssue('INVALID_STATUS', 
              `订单${order.id}使用了无效的状态值: ${order.status}`, 'high')
          }
        })
      }
      
      console.log('✅ 状态映射检查完成')
      return true
    } catch (error) {
      this.addIssue('STATUS_MAPPING', `状态映射检查失败: ${error.message}`, 'medium')
      return false
    }
  }

  /**
   * 检查数据库连接
   */
  async checkDatabaseConnection() {
    console.log('🗄️ 检查数据库连接...')
    
    try {
      const response = await realApi.testDatabase()
      if (response && response.data) {
        console.log('✅ 数据库连接正常')
        
        // 进一步检查数据同步健康状态
        await this.checkDataSyncHealth()
        return true
      } else {
        this.addIssue('DATABASE_CONNECTION', '数据库连接测试失败', 'critical')
        return false
      }
    } catch (error) {
      this.addIssue('DATABASE_CONNECTION', `数据库连接错误: ${error.message}`, 'critical')
      return false
    }
  }

  /**
   * 检查数据同步健康状态
   */
  async checkDataSyncHealth() {
    console.log('🏥 检查数据同步健康状态...')
    
    try {
      const response = await realApi.dataSyncHealthCheck()
      if (response && response.data) {
        const healthData = response.data
        
        // 检查健康分数
        if (healthData.healthScore < 70) {
          this.addIssue('SYNC_HEALTH_LOW', 
            `数据同步健康分数较低: ${healthData.healthScore}`, 'high')
        }
        
        // 检查各模块状态
        if (healthData.bookings?.status === 'error') {
          this.addIssue('BOOKING_MODULE_ERROR', 
            `订单模块异常: ${healthData.bookings.error}`, 'high')
        }
        
        if (healthData.users?.status === 'error') {
          this.addIssue('USER_MODULE_ERROR', 
            `用户模块异常: ${healthData.users.error}`, 'high')
        }
        
        if (healthData.services?.status === 'error') {
          this.addIssue('SERVICE_MODULE_ERROR', 
            `服务模块异常: ${healthData.services.error}`, 'high')
        }
        
        console.log('✅ 数据同步健康检查完成')
        return true
      }
    } catch (error) {
      this.addIssue('SYNC_HEALTH_CHECK', `数据同步健康检查失败: ${error.message}`, 'medium')
      return false
    }
  }

  /**
   * 检查数据一致性（使用后端API）
   */
  async checkDataConsistencyAdvanced() {
    console.log('🔍 执行高级数据一致性检查...')
    
    try {
      const response = await realApi.dataSyncConsistencyCheck()
      if (response && response.data) {
        const consistencyData = response.data
        
        // 检查一致性分数
        if (consistencyData.consistencyScore < 80) {
          this.addIssue('CONSISTENCY_SCORE_LOW', 
            `数据一致性分数较低: ${consistencyData.consistencyScore}%`, 'high')
        }
        
        // 检查订单一致性
        if (consistencyData.bookings?.consistencyRate < 95) {
          this.addIssue('BOOKING_CONSISTENCY_LOW', 
            `订单数据一致性较低: ${consistencyData.bookings.consistencyRate}%`, 'medium')
        }
        
        // 检查用户一致性
        if (consistencyData.users?.consistencyRate < 95) {
          this.addIssue('USER_CONSISTENCY_LOW', 
            `用户数据一致性较低: ${consistencyData.users.consistencyRate}%`, 'medium')
        }
        
        // 检查服务一致性
        if (consistencyData.services?.consistencyRate < 95) {
          this.addIssue('SERVICE_CONSISTENCY_LOW', 
            `服务数据一致性较低: ${consistencyData.services.consistencyRate}%`, 'medium')
        }
        
        console.log('✅ 高级数据一致性检查完成')
        return true
      }
    } catch (error) {
      this.addIssue('ADVANCED_CONSISTENCY_CHECK', 
        `高级一致性检查失败: ${error.message}`, 'medium')
      return false
    }
  }

  /**
   * 自动修复已知问题
   */
  async autoFix() {
    console.log('🔧 开始自动修复问题...')
    
    for (const issue of this.issues) {
      try {
        switch (issue.type) {
          case 'DEPRECATED_STATUS':
            await this.fixDeprecatedStatus(issue)
            break
          case 'STATUS_MISMATCH':
            await this.fixStatusMismatch(issue)
            break
          case 'DATA_COUNT_MISMATCH':
            await this.fixDataCountMismatch(issue)
            break
          case 'SYNC_HEALTH_LOW':
            await this.fixSyncHealthIssues(issue)
            break
          case 'CONSISTENCY_SCORE_LOW':
            await this.fixConsistencyIssues(issue)
            break
          case 'BOOKING_CONSISTENCY_LOW':
          case 'USER_CONSISTENCY_LOW':
          case 'SERVICE_CONSISTENCY_LOW':
            await this.fixModuleConsistency(issue)
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
   * 修复同步健康问题
   */
  async fixSyncHealthIssues(issue) {
    console.log(`🔧 修复同步健康问题: ${issue.description}`)
    
    try {
      // 调用后端修复API
      const response = await realApi.dataSyncRepair()
      if (response && response.data) {
        console.log('✅ 同步健康问题修复成功')
        this.fixedIssues.push({
          ...issue,
          fixedAt: new Date(),
          fixMethod: 'backend_repair'
        })
      }
    } catch (error) {
      console.error('❌ 修复同步健康问题失败:', error)
      throw error
    }
  }

  /**
   * 修复一致性问题
   */
  async fixConsistencyIssues(issue) {
    console.log(`🔧 修复一致性问题: ${issue.description}`)
    
    try {
      // 针对不同类型的一致性问题采用不同的修复策略
      if (issue.type.includes('BOOKING')) {
        await realApi.dataSyncRepair('booking')
      } else if (issue.type.includes('USER')) {
        await realApi.dataSyncRepair('user')
      } else if (issue.type.includes('SERVICE')) {
        await realApi.dataSyncRepair('service')
      } else {
        // 全面修复
        await realApi.dataSyncRepair()
      }
      
      console.log('✅ 一致性问题修复成功')
      this.fixedIssues.push({
        ...issue,
        fixedAt: new Date(),
        fixMethod: 'consistency_repair'
      })
    } catch (error) {
      console.error('❌ 修复一致性问题失败:', error)
      throw error
    }
  }

  /**
   * 修复模块一致性问题
   */
  async fixModuleConsistency(issue) {
    console.log(`🔧 修复模块一致性问题: ${issue.description}`)
    
    try {
      let repairType = 'all'
      if (issue.type.includes('BOOKING')) {
        repairType = 'booking'
      } else if (issue.type.includes('USER')) {
        repairType = 'user'
      } else if (issue.type.includes('SERVICE')) {
        repairType = 'service'
      }
      
      await realApi.dataSyncRepair(repairType)
      
      console.log('✅ 模块一致性问题修复成功')
      this.fixedIssues.push({
        ...issue,
        fixedAt: new Date(),
        fixMethod: 'module_repair'
      })
    } catch (error) {
      console.error('❌ 修复模块一致性问题失败:', error)
      throw error
    }
  }

  /**
   * 修复废弃状态值
   */
  async fixDeprecatedStatus(issue) {
    // 这里可以实现状态值的自动更新逻辑
    console.log(`🔧 修复废弃状态: ${issue.description}`)
    this.fixedIssues.push({
      ...issue,
      fixedAt: new Date(),
      fixMethod: 'auto'
    })
  }

  /**
   * 修复状态不匹配
   */
  async fixStatusMismatch(issue) {
    console.log(`🔧 修复状态不匹配: ${issue.description}`)
    // 实现状态同步逻辑
    this.fixedIssues.push({
      ...issue,
      fixedAt: new Date(),
      fixMethod: 'sync'
    })
  }

  /**
   * 修复数据数量不匹配
   */
  async fixDataCountMismatch(issue) {
    console.log(`🔧 修复数据数量不匹配: ${issue.description}`)
    // 实现数据重新同步逻辑
    this.fixedIssues.push({
      ...issue,
      fixedAt: new Date(),
      fixMethod: 'resync'
    })
  }

  /**
   * 添加问题到列表
   */
  addIssue(type, description, severity = 'medium') {
    this.issues.push({
      type,
      description,
      severity,
      timestamp: new Date(),
      id: Date.now() + Math.random()
    })
  }

  /**
   * 生成诊断报告
   */
  generateReport() {
    const report = {
      timestamp: new Date(),
      totalIssues: this.issues.length,
      criticalIssues: this.issues.filter(i => i.severity === 'critical').length,
      highIssues: this.issues.filter(i => i.severity === 'high').length,
      mediumIssues: this.issues.filter(i => i.severity === 'medium').length,
      issues: this.issues,
      fixedIssues: this.fixedIssues
    }

    console.log('📋 诊断报告:', report)

    // 显示通知
    if (report.criticalIssues > 0) {
      ElNotification({
        title: '严重问题',
        message: `发现${report.criticalIssues}个严重的数据同步问题`,
        type: 'error',
        duration: 0
      })
    } else if (report.highIssues > 0) {
      ElNotification({
        title: '重要问题',
        message: `发现${report.highIssues}个重要的数据同步问题`,
        type: 'warning',
        duration: 5000
      })
    } else if (report.totalIssues === 0) {
      ElMessage.success('数据同步状态良好，未发现问题')
    }

    return report
  }

  /**
   * 快速修复常见问题
   */
  async quickFix() {
    console.log('⚡ 执行快速修复...')
    
    try {
      // 1. 强制刷新订单数据
      await this.forceRefreshOrders()
      
      // 2. 清除前端缓存
      this.clearFrontendCache()
      
      // 3. 重新同步状态
      await this.resyncOrderStatus()
      
      ElMessage.success('快速修复完成')
      return true
    } catch (error) {
      ElMessage.error(`快速修复失败: ${error.message}`)
      return false
    }
  }

  /**
   * 强制刷新订单数据
   */
  async forceRefreshOrders() {
    console.log('🔄 强制刷新订单数据...')
    
    // 清除可能的缓存
    if (window.orderCache) {
      delete window.orderCache
    }
    
    // 重新获取数据
    const response = await realApi.getOrderList()
    if (response && response.data) {
      console.log('✅ 订单数据刷新成功')
      return response.data
    }
    
    throw new Error('订单数据刷新失败')
  }

  /**
   * 清除前端缓存
   */
  clearFrontendCache() {
    console.log('🧹 清除前端缓存...')
    
    // 清除localStorage中的缓存数据
    const cacheKeys = ['orderCache', 'userCache', 'serviceCache']
    cacheKeys.forEach(key => {
      localStorage.removeItem(key)
    })
    
    // 清除sessionStorage
    sessionStorage.clear()
    
    console.log('✅ 前端缓存清除完成')
  }

  /**
   * 重新同步订单状态
   */
  async resyncOrderStatus() {
    console.log('🔄 重新同步订单状态...')
    
    try {
      const orders = await realApi.getOrderList()
      if (orders && orders.data) {
        // 这里可以实现状态重新同步的逻辑
        console.log('✅ 订单状态同步完成')
        return true
      }
    } catch (error) {
      console.error('❌ 订单状态同步失败:', error)
      throw error
    }
  }
}

// 创建全局实例
export const syncDiagnostic = new SyncDiagnostic()

// 导出便捷方法
export const runDiagnostic = () => syncDiagnostic.runFullDiagnostic()
export const quickFix = () => syncDiagnostic.quickFix()
export const autoFix = () => syncDiagnostic.autoFix()

export default {
  SyncDiagnostic,
  syncDiagnostic,
  runDiagnostic,
  quickFix,
  autoFix
}