<template>
  <div class="appointment-test">
    <div class="container">
      <h1>预约功能测试</h1>
      
      <div class="test-section">
        <h2>认证状态</h2>
        <div class="auth-status">
          <p><strong>Token状态:</strong> {{ tokenStatus }}</p>
          <el-button @click="checkAuth" type="info">检查认证状态</el-button>
          <el-button @click="clearAuth" type="warning">清除认证</el-button>
          <el-button @click="testLogin" type="success" :loading="loginLoading">测试登录</el-button>
        </div>
      </div>
      
      <div class="test-section">
        <h2>快速预约测试</h2>
        <div class="quick-booking">
          <el-form :model="quickBooking" label-width="120px">
            <el-form-item label="服务类型">
              <el-select v-model="quickBooking.serviceId">
                <el-option label="基础洗车" :value="1" />
                <el-option label="精洗套餐" :value="2" />
                <el-option label="豪华套餐" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="预约日期">
              <el-date-picker v-model="quickBooking.date" type="date" />
            </el-form-item>
            <el-form-item label="预约时间">
              <el-time-picker v-model="quickBooking.time" format="HH:mm" />
            </el-form-item>
            <el-form-item label="车牌号">
              <el-input v-model="quickBooking.carNumber" placeholder="如：京A12345" />
            </el-form-item>
            <el-form-item label="车型">
              <el-input v-model="quickBooking.carModel" placeholder="如：奔驰 C200" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="quickBooking.phone" placeholder="如：13800138000" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="quickBooking.notes" type="textarea" />
            </el-form-item>
          </el-form>
          
          <div class="actions">
            <el-button type="primary" @click="submitQuickBooking" :loading="submitting">
              提交预约
            </el-button>
            <el-button @click="fillTestData">填充测试数据</el-button>
          </div>
        </div>
      </div>
      
      <div class="test-section">
        <h2>API测试结果</h2>
        <div class="test-results">
          <pre>{{ JSON.stringify(testResults, null, 2) }}</pre>
        </div>
      </div>
      
      <div class="test-section">
        <h2>快速跳转</h2>
        <div class="quick-links">
          <el-button @click="goToAppointment" type="primary">完整预约页面</el-button>
          <el-button @click="goToOrders" type="success">我的订单</el-button>
          <el-button @click="goToOrderFlowTest" type="info">订单流程测试</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { realApi } from '@/api/realApi'
import { TimeUtils } from '@/utils/timeUtils'

export default {
  name: 'AppointmentTest',
  setup() {
    const router = useRouter()
    
    const tokenStatus = ref('未检查')
    const loginLoading = ref(false)
    const submitting = ref(false)
    const testResults = ref({})
    
    const quickBooking = ref({
      serviceId: 1,
      date: new Date(),
      time: new Date(),
      carNumber: '',
      carModel: '',
      phone: '',
      notes: ''
    })
    
    // 检查认证状态
    const checkAuth = () => {
      const token = localStorage.getItem('token')
      if (token && token.trim() !== '') {
        tokenStatus.value = '已登录'
        testResults.value.auth = {
          status: 'authenticated',
          token: token.substring(0, 20) + '...',
          timestamp: new Date().toISOString()
        }
      } else {
        tokenStatus.value = '未登录'
        testResults.value.auth = {
          status: 'not_authenticated',
          timestamp: new Date().toISOString()
        }
      }
    }
    
    // 清除认证
    const clearAuth = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('tokenType')
      tokenStatus.value = '已清除'
      ElMessage.info('认证信息已清除')
    }
    
    // 测试登录
    const testLogin = async () => {
      loginLoading.value = true
      try {
        console.log('🔐 测试登录...')
        const response = await realApi.login('admin', 'admin123')
        console.log('✅ 登录响应:', response)
        
        if (response && response.data && response.data.token) {
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('tokenType', 'Bearer')
          tokenStatus.value = '登录成功'
          testResults.value.login = {
            success: true,
            token: response.data.token.substring(0, 20) + '...',
            timestamp: new Date().toISOString()
          }
          ElMessage.success('登录成功')
        } else {
          throw new Error('登录响应无效')
        }
      } catch (error) {
        console.error('❌ 登录失败:', error)
        tokenStatus.value = '登录失败'
        testResults.value.login = {
          success: false,
          error: error.message,
          timestamp: new Date().toISOString()
        }
        ElMessage.error('登录失败: ' + error.message)
      } finally {
        loginLoading.value = false
      }
    }
    
    // 填充测试数据
    const fillTestData = () => {
      quickBooking.value = {
        serviceId: 1,
        date: new Date(),
        time: new Date(Date.now() + 2 * 60 * 60 * 1000), // 2小时后
        carNumber: '京A12345',
        carModel: '奔驰 C200',
        phone: '13800138000',
        notes: '测试预约，请及时处理'
      }
      ElMessage.success('测试数据已填充')
    }
    
    // 提交快速预约
    const submitQuickBooking = async () => {
      submitting.value = true
      try {
        // 确保用户已登录
        const token = localStorage.getItem('token')
        if (!token || token.trim() === '') {
          console.log('🔐 用户未登录，尝试自动登录...')
          await testLogin()
        }
        
        // 构建预约数据
        const bookingData = {
          userId: 2,
          serviceId: quickBooking.value.serviceId,
          bookingDate: TimeUtils.formatDate(quickBooking.value.date, 'YYYY-MM-DD'),
          bookingTime: TimeUtils.formatDate(quickBooking.value.time, 'HH:mm'),
          carNumber: quickBooking.value.carNumber,
          carModel: quickBooking.value.carModel,
          contactPhone: quickBooking.value.phone,
          notes: quickBooking.value.notes,
          totalPrice: 50.00,
          status: 'pending'
        }
        
        console.log('📤 提交预约数据:', bookingData)
        
        // 调用API创建预约
        const response = await realApi.createBooking(bookingData)
        console.log('✅ 预约创建响应:', response)
        
        if (response && response.data) {
          testResults.value.booking = {
            success: true,
            data: response.data,
            timestamp: new Date().toISOString()
          }
          ElMessage.success('预约提交成功！')
          
          // 询问是否跳转到订单页面
          setTimeout(() => {
            ElMessage({
              message: '预约成功！3秒后自动跳转到我的订单页面',
              type: 'success',
              duration: 3000,
              showClose: true
            })
            
            // 3秒后自动跳转
            setTimeout(() => {
              const timestamp = Date.now()
              router.push({
                path: '/orders',
                query: { refresh: timestamp }
              })
            }, 3000)
          }, 1000)
        } else {
          throw new Error('预约创建失败')
        }
        
      } catch (error) {
        console.error('❌ 预约提交失败:', error)
        testResults.value.booking = {
          success: false,
          error: error.message,
          timestamp: new Date().toISOString()
        }
        ElMessage.error('预约提交失败: ' + error.message)
      } finally {
        submitting.value = false
      }
    }
    
    // 跳转方法
    const goToAppointment = () => router.push('/appointment')
    const goToOrders = () => {
      const timestamp = Date.now()
      router.push({
        path: '/orders',
        query: { refresh: timestamp }
      })
    }
    const goToOrderFlowTest = () => router.push('/order-flow-test')
    
    // 初始化
    onMounted(() => {
      checkAuth()
      fillTestData()
    })
    
    return {
      tokenStatus,
      loginLoading,
      submitting,
      testResults,
      quickBooking,
      checkAuth,
      clearAuth,
      testLogin,
      fillTestData,
      submitQuickBooking,
      goToAppointment,
      goToOrders,
      goToOrderFlowTest
    }
  }
}
</script>

<style scoped>
.appointment-test {
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

.auth-status {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-booking {
  max-width: 600px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
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

.quick-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.el-form-item {
  margin-bottom: 16px;
}
</style>