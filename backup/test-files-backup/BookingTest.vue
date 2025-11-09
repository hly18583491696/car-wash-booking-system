<template>
  <div class="booking-test">
    <div class="container">
      <h1>预约订单测试页面</h1>
      
      <!-- 数据库连接测试 -->
      <el-card class="test-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>数据库连接测试</span>
            <el-button type="primary" @click="testDatabaseConnection" :loading="dbTesting">
              测试连接
            </el-button>
          </div>
        </template>
        
        <div v-if="dbResult">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="状态">
              <el-tag :type="dbResult.status === 'success' ? 'success' : 'danger'">
                {{ dbResult.status === 'success' ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户数量">{{ dbResult.users_count || 0 }}</el-descriptions-item>
            <el-descriptions-item label="服务数量">{{ dbResult.services_count || 0 }}</el-descriptions-item>
            <el-descriptions-item label="订单数量">{{ dbResult.bookings_count || 0 }}</el-descriptions-item>
            <el-descriptions-item label="时间段数量">{{ dbResult.time_slots_count || 0 }}</el-descriptions-item>
            <el-descriptions-item label="测试时间">{{ dbResult.timestamp }}</el-descriptions-item>
          </el-descriptions>
          
          <div v-if="dbResult.message" class="result-message">
            <el-alert :title="dbResult.message" :type="dbResult.status === 'success' ? 'success' : 'error'" show-icon />
          </div>
        </div>
      </el-card>

      <!-- 简单订单创建测试 -->
      <el-card class="test-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>简单订单创建测试</span>
            <el-button type="success" @click="createSimpleBooking" :loading="simpleBookingTesting">
              创建测试订单
            </el-button>
          </div>
        </template>
        
        <div v-if="simpleBookingResult">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="状态">
              <el-tag :type="simpleBookingResult.status === 'success' ? 'success' : 'danger'">
                {{ simpleBookingResult.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单ID">{{ simpleBookingResult.booking_id }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ simpleBookingResult.user_id }}</el-descriptions-item>
            <el-descriptions-item label="服务ID">{{ simpleBookingResult.service_id }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ simpleBookingResult.timestamp }}</el-descriptions-item>
            <el-descriptions-item label="消息">{{ simpleBookingResult.message }}</el-descriptions-item>
          </el-descriptions>
          
          <div v-if="simpleBookingResult.booking" class="booking-details">
            <h4>订单详情</h4>
            <pre>{{ JSON.stringify(simpleBookingResult.booking, null, 2) }}</pre>
          </div>
        </div>
      </el-card>

      <!-- 用户预约创建测试 -->
      <el-card class="test-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>用户预约创建测试</span>
            <el-button type="warning" @click="createUserBooking" :loading="userBookingTesting">
              创建用户预约
            </el-button>
          </div>
        </template>
        
        <el-form :model="userBookingForm" label-width="120px" class="booking-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户ID">
                <el-input-number v-model="userBookingForm.userId" :min="1" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务ID">
                <el-input-number v-model="userBookingForm.serviceId" :min="1" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="车牌号">
                <el-input v-model="userBookingForm.carNumber" placeholder="如：京A12345" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="车型">
                <el-input v-model="userBookingForm.carModel" placeholder="如：丰田卡罗拉" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话">
                <el-input v-model="userBookingForm.contactPhone" placeholder="如：13800138000" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备注">
                <el-input v-model="userBookingForm.notes" placeholder="预约备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        
        <div v-if="userBookingResult">
          <el-divider>测试结果</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="状态">
              <el-tag :type="userBookingResult.status === 'success' ? 'success' : 'danger'">
                {{ userBookingResult.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单ID">{{ userBookingResult.booking_id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userBookingResult.user_name }}</el-descriptions-item>
            <el-descriptions-item label="服务名">{{ userBookingResult.service_name }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ userBookingResult.timestamp }}</el-descriptions-item>
            <el-descriptions-item label="消息">{{ userBookingResult.message }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 所有订单查看 -->
      <el-card class="test-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>所有订单查看</span>
            <el-button type="info" @click="getAllBookings" :loading="allBookingsLoading">
              刷新订单
            </el-button>
          </div>
        </template>
        
        <el-table :data="allBookings" stripe style="width: 100%" max-height="400">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单号" width="200" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="serviceId" label="服务ID" width="100" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="金额" width="100" />
          <el-table-column prop="carNumber" label="车牌号" width="120" />
          <el-table-column prop="contactPhone" label="电话" width="140" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column prop="notes" label="备注" min-width="150" />
        </el-table>
      </el-card>

      <!-- 清理测试数据 -->
      <el-card class="test-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>清理测试数据</span>
            <el-button type="danger" @click="cleanupTestData" :loading="cleanupLoading">
              清理数据
            </el-button>
          </div>
        </template>
        
        <el-alert
          title="注意"
          description="此操作将删除所有包含'测试'关键词的订单数据，请谨慎操作。"
          type="warning"
          show-icon
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request.js'

// 响应式数据
const dbTesting = ref(false)
const dbResult = ref(null)

const simpleBookingTesting = ref(false)
const simpleBookingResult = ref(null)

const userBookingTesting = ref(false)
const userBookingResult = ref(null)
const userBookingForm = reactive({
  userId: 2,
  serviceId: 1,
  carNumber: '测试A12345',
  carModel: '测试车型',
  contactPhone: '13800138000',
  notes: '前端测试预约'
})

const allBookingsLoading = ref(false)
const allBookings = ref([])

const cleanupLoading = ref(false)

// 方法
const testDatabaseConnection = async () => {
  dbTesting.value = true
  try {
    console.log('🔗 测试数据库连接...')
    const response = await request.get('/debug/db-connection')
    dbResult.value = response.data
    console.log('✅ 数据库连接测试结果:', response.data)
    
    if (response.data.status === 'success') {
      ElMessage.success('数据库连接正常')
    } else {
      ElMessage.error('数据库连接异常')
    }
  } catch (error) {
    console.error('❌ 数据库连接测试失败:', error)
    dbResult.value = {
      status: 'error',
      message: error.message || '连接失败'
    }
    ElMessage.error('数据库连接测试失败')
  } finally {
    dbTesting.value = false
  }
}

const createSimpleBooking = async () => {
  simpleBookingTesting.value = true
  try {
    console.log('📝 创建简单测试订单...')
    const response = await request.post('/debug/create-simple-booking')
    simpleBookingResult.value = response.data
    console.log('✅ 简单订单创建结果:', response.data)
    
    if (response.data.status === 'success') {
      ElMessage.success('测试订单创建成功')
      // 刷新订单列表
      getAllBookings()
    } else {
      ElMessage.error('测试订单创建失败')
    }
  } catch (error) {
    console.error('❌ 简单订单创建失败:', error)
    simpleBookingResult.value = {
      status: 'error',
      message: error.message || '创建失败'
    }
    ElMessage.error('简单订单创建失败')
  } finally {
    simpleBookingTesting.value = false
  }
}

const createUserBooking = async () => {
  userBookingTesting.value = true
  try {
    console.log('📝 创建用户预约订单...', userBookingForm)
    const response = await request.post('/debug/test-user-booking', userBookingForm)
    userBookingResult.value = response.data
    console.log('✅ 用户预约创建结果:', response.data)
    
    if (response.data.status === 'success') {
      ElMessage.success('用户预约创建成功')
      // 刷新订单列表
      getAllBookings()
    } else {
      ElMessage.error('用户预约创建失败')
    }
  } catch (error) {
    console.error('❌ 用户预约创建失败:', error)
    userBookingResult.value = {
      status: 'error',
      message: error.message || '创建失败'
    }
    ElMessage.error('用户预约创建失败')
  } finally {
    userBookingTesting.value = false
  }
}

const getAllBookings = async () => {
  allBookingsLoading.value = true
  try {
    console.log('📋 获取所有订单...')
    const response = await request.get('/debug/all-bookings')
    allBookings.value = response.data || []
    console.log('✅ 获取订单成功，数量:', allBookings.value.length)
    ElMessage.success(`获取到 ${allBookings.value.length} 条订单`)
  } catch (error) {
    console.error('❌ 获取订单失败:', error)
    ElMessage.error('获取订单失败')
  } finally {
    allBookingsLoading.value = false
  }
}

const cleanupTestData = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有测试数据吗？', '确认清理', {
      type: 'warning'
    })
    
    cleanupLoading.value = true
    console.log('🧹 清理测试数据...')
    const response = await request.delete('/debug/cleanup-test-data')
    console.log('✅ 清理结果:', response.data)
    
    ElMessage.success(response.data || '清理完成')
    // 刷新订单列表
    getAllBookings()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('❌ 清理失败:', error)
      ElMessage.error('清理失败')
    }
  } finally {
    cleanupLoading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    'pending': 'warning',
    'confirmed': 'primary',
    'in_progress': 'info',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return types[status] || 'info'
}

// 生命周期
onMounted(() => {
  // 自动测试数据库连接
  testDatabaseConnection()
  // 获取所有订单
  getAllBookings()
})
</script>

<style scoped>
.booking-test {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

h1 {
  text-align: center;
  color: white;
  margin-bottom: 30px;
  font-size: 2.5rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.test-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.result-message {
  margin-top: 16px;
}

.booking-details {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.booking-details pre {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
  max-height: 200px;
  overflow-y: auto;
}

.booking-form {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
}

:deep(.el-table) {
  font-size: 12px;
}
</style>