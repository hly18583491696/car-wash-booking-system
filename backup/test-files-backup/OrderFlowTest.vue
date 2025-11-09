<template>
  <div class="order-flow-test">
    <div class="container">
      <h1>订单流程测试</h1>
      
      <div class="test-section">
        <h2>1. 创建测试订单</h2>
        <el-button type="primary" @click="createTestOrder" :loading="creating">
          创建测试订单
        </el-button>
        <p v-if="lastCreatedOrder">
          最后创建的订单ID: {{ lastCreatedOrder.id }}
        </p>
      </div>
      
      <div class="test-section">
        <h2>2. 获取用户订单</h2>
        <el-button type="success" @click="fetchUserOrders" :loading="fetching">
          获取用户订单
        </el-button>
        <div v-if="userOrders.length > 0" class="orders-list">
          <h3>用户订单列表 ({{ userOrders.length }}条):</h3>
          <div v-for="order in userOrders" :key="order.id" class="order-item">
            <p><strong>订单ID:</strong> {{ order.id }}</p>
            <p><strong>订单号:</strong> {{ order.orderNo }}</p>
            <p><strong>服务ID:</strong> {{ order.serviceId }}</p>
            <p><strong>状态:</strong> {{ order.status }}</p>
            <p><strong>车牌号:</strong> {{ order.carNumber }}</p>
            <p><strong>预约时间:</strong> {{ order.bookingDate }} {{ order.bookingTime }}</p>
            <p><strong>创建时间:</strong> {{ order.createdAt }}</p>
            <hr>
          </div>
        </div>
        <p v-else-if="!fetching">暂无订单数据</p>
      </div>
      
      <div class="test-section">
        <h2>3. 模拟预约流程</h2>
        <el-button type="warning" @click="simulateBookingFlow" :loading="simulating">
          模拟完整预约流程
        </el-button>
        <p>这将创建一个订单，然后跳转到我的订单页面</p>
      </div>
      
      <div class="test-section">
        <h2>4. 跳转到我的订单页面</h2>
        <el-button type="info" @click="goToOrders">
          跳转到我的订单
        </el-button>
      </div>
      
      <div class="test-section">
        <h2>5. API测试结果</h2>
        <div class="test-results">
          <pre>{{ JSON.stringify(testResults, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { realApi } from '@/api/realApi'
import { TimeUtils } from '@/utils/timeUtils'

export default {
  name: 'OrderFlowTest',
  setup() {
    const router = useRouter()
    
    const creating = ref(false)
    const fetching = ref(false)
    const simulating = ref(false)
    const lastCreatedOrder = ref(null)
    const userOrders = ref([])
    const testResults = ref({})
    
    // 确保用户已登录
    const ensureAuthenticated = async () => {
      const token = localStorage.getItem('token')
      if (token && token.trim() !== '') {
        console.log('✅ 用户已登录')
        return true
      }
      
      console.log('🔐 用户未登录，尝试自动登录...')
      try {
        // 使用测试用户自动登录
        const loginResponse = await realApi.login('admin', 'admin123')
        if (loginResponse && loginResponse.data && loginResponse.data.token) {
          localStorage.setItem('token', loginResponse.data.token)
          localStorage.setItem('tokenType', 'Bearer')
          console.log('✅ 自动登录成功')
          return true
        }
      } catch (error) {
        console.error('❌ 自动登录失败:', error)
      }
      
      return false
    }
    
    // 创建测试订单
    const createTestOrder = async () => {
      creating.value = true
      try {
        // 确保用户已登录
        const isAuthenticated = await ensureAuthenticated()
        if (!isAuthenticated) {
          ElMessage.error('登录失败，无法创建订单')
          return
        }
        
        const bookingData = {
          userId: 2,
          serviceId: 1,
          bookingDate: TimeUtils.formatDate(new Date(), 'YYYY-MM-DD'),
          bookingTime: '10:00',
          carNumber: '测试A12345',
          carModel: '测试品牌 测试型号',
          contactPhone: '13800138000',
          notes: '测试订单',
          totalPrice: 50.00,
          status: 'pending'
        }
        
        console.log('📤 创建测试订单:', bookingData)
        const response = await realApi.createBooking(bookingData)
        console.log('✅ 订单创建响应:', response)
        
        if (response && response.data) {
          lastCreatedOrder.value = response.data
          testResults.value.createOrder = {
            success: true,
            data: response.data,
            timestamp: new Date().toISOString()
          }
          ElMessage.success('测试订单创建成功')
        }
      } catch (error) {
        console.error('❌ 创建订单失败:', error)
        testResults.value.createOrder = {
          success: false,
          error: error.message,
          timestamp: new Date().toISOString()
        }
        ElMessage.error('创建订单失败: ' + error.message)
      } finally {
        creating.value = false
      }
    }
    
    // 获取用户订单
    const fetchUserOrders = async () => {
      fetching.value = true
      try {
        // 确保用户已登录
        const isAuthenticated = await ensureAuthenticated()
        if (!isAuthenticated) {
          ElMessage.error('登录失败，无法获取订单')
          return
        }
        
        const userId = 2
        console.log('📥 获取用户订单，用户ID:', userId)
        const response = await realApi.getUserOrders(userId)
        console.log('📋 用户订单响应:', response)
        
        if (response && response.data) {
          userOrders.value = response.data
          testResults.value.getUserOrders = {
            success: true,
            count: response.data.length,
            data: response.data,
            timestamp: new Date().toISOString()
          }
          ElMessage.success(`获取到 ${response.data.length} 条订单`)
        }
      } catch (error) {
        console.error('❌ 获取订单失败:', error)
        testResults.value.getUserOrders = {
          success: false,
          error: error.message,
          timestamp: new Date().toISOString()
        }
        ElMessage.error('获取订单失败: ' + error.message)
      } finally {
        fetching.value = false
      }
    }
    
    // 模拟完整预约流程
    const simulateBookingFlow = async () => {
      simulating.value = true
      try {
        // 1. 创建订单
        await createTestOrder()
        
        // 2. 等待一下
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 3. 跳转到订单页面
        ElMessage.success('预约提交成功！即将跳转到我的订单页面')
        setTimeout(() => {
          const timestamp = Date.now()
          router.push({
            path: '/orders',
            query: { refresh: timestamp }
          })
        }, 1500)
        
      } catch (error) {
        console.error('❌ 模拟流程失败:', error)
        ElMessage.error('模拟流程失败')
      } finally {
        simulating.value = false
      }
    }
    
    // 跳转到我的订单页面
    const goToOrders = () => {
      const timestamp = Date.now()
      router.push({
        path: '/orders',
        query: { refresh: timestamp }
      })
    }
    
    return {
      creating,
      fetching,
      simulating,
      lastCreatedOrder,
      userOrders,
      testResults,
      createTestOrder,
      fetchUserOrders,
      simulateBookingFlow,
      goToOrders
    }
  }
}
</script>

<style scoped>
.order-flow-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.container {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.test-section {
  margin-bottom: 32px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.test-section h2 {
  color: #333;
  margin-bottom: 16px;
}

.orders-list {
  margin-top: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.order-item {
  background: #f5f5f5;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
}

.order-item p {
  margin: 4px 0;
  font-size: 14px;
}

.test-results {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.test-results pre {
  font-size: 12px;
  line-height: 1.4;
  margin: 0;
}

hr {
  margin: 8px 0;
  border: none;
  border-top: 1px solid #ddd;
}
</style>