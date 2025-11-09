<template>
  <div class="booking-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>预约管理</h1>
        <p>管理客户预约信息，处理预约状态变更</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="showAddBookingDialog">
          新增预约
        </el-button>
        <el-button :icon="Refresh" @click="refreshData" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-card shadow="never">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索客户姓名、手机号、订单号"
              :prefix-icon="Search"
              clearable
              @input="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.status"
              placeholder="预约状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="待确认" value="pending" />
              <el-option label="已确认" value="confirmed" />
              <el-option label="进行中" value="processing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.serviceId"
              placeholder="服务类型"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option
                v-for="service in services"
                :key="service.id"
                :label="service.name"
                :value="service.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card pending">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.pending }}</div>
                <div class="stats-label">待确认</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card confirmed">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.confirmed }}</div>
                <div class="stats-label">已确认</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card processing">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Loading /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.processing }}</div>
                <div class="stats-label">进行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card completed">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.completed }}</div>
                <div class="stats-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预约列表 -->
    <div class="table-section">
      <el-card shadow="never">
        <el-table
          :data="bookings"
          style="width: 100%"
          v-loading="loading"
          row-key="id"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="id" label="订单号" width="120" />
          <el-table-column prop="customerName" label="客户信息" width="160">
            <template #default="scope">
              <div class="customer-info">
                <div class="customer-name">{{ scope.row.customerName }}</div>
                <div class="customer-phone">{{ scope.row.customerPhone }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="serviceName" label="服务项目" min-width="150">
            <template #default="scope">
              <div class="service-info">
                <div class="service-name">{{ scope.row.serviceName }}</div>
                <div class="service-price">¥{{ scope.row.price }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appointmentTime" label="预约时间" width="160">
            <template #default="scope">
              <div class="time-info">
                <div>{{ formatDate(scope.row.appointmentTime) }}</div>
                <div class="time-detail">{{ formatTime(scope.row.appointmentTime) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button
                  v-if="scope.row.status === 'pending'"
                  size="small"
                  type="primary"
                  @click="confirmBooking(scope.row)"
                >
                  确认
                </el-button>
                <el-button
                  v-if="scope.row.status === 'confirmed'"
                  size="small"
                  type="success"
                  @click="startService(scope.row)"
                >
                  开始服务
                </el-button>
                <el-button
                  v-if="scope.row.status === 'in_progress' || scope.row.status === 'processing'"
                  size="small"
                  type="warning"
                  @click="completeService(scope.row)"
                >
                  完成服务
                </el-button>
                <el-button size="small" @click="viewDetail(scope.row)">
                  详情
                </el-button>
                <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)">
                  <el-button size="small">
                    更多<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="cancel" :disabled="scope.row.status === 'completed'">
                        取消预约
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑预约对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户姓名" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="customerPhone">
              <el-input v-model="form.customerPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务项目" prop="serviceId">
              <el-select v-model="form.serviceId" placeholder="请选择服务项目" style="width: 100%">
                <el-option
                  v-for="service in services"
                  :key="service.id"
                  :label="`${service.name} - ¥${service.price}`"
                  :value="service.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预约时间" prop="appointmentTime">
              <el-date-picker
                v-model="form.appointmentTime"
                type="datetime"
                placeholder="选择预约时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注信息" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="预约详情"
      width="600px"
    >
      <el-descriptions v-if="currentBooking" :column="2" border>
        <el-descriptions-item label="订单号">{{ currentBooking.id }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentBooking.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentBooking.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ currentBooking.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="服务价格">¥{{ currentBooking.price }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDateTime(currentBooking.appointmentTime) }}</el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <el-tag :type="getStatusType(currentBooking.status)">
            {{ getStatusText(currentBooking.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentBooking.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注信息" :span="2">
          {{ currentBooking.remark || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Refresh, Clock, Check, Loading, CircleCheck,
  ArrowDown
} from '@element-plus/icons-vue'
import orderApi from '../../api/order.js'
import serviceApi from '../../api/service.js'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  serviceId: '',
  dateRange: null
})

// 预约列表和分页
const bookings = ref([])
const services = ref([])
const selectedBookings = ref([])
const currentBooking = ref(null)

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = reactive({
  pending: 0,
  confirmed: 0,
  processing: 0,
  completed: 0
})

// 表单数据
const form = reactive({
  id: null,
  customerName: '',
  customerPhone: '',
  serviceId: '',
  appointmentTime: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  customerPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  serviceId: [
    { required: true, message: '请选择服务项目', trigger: 'change' }
  ],
  appointmentTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑预约' : '新增预约')

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await orderApi.getOrderList(params)
    if (response?.data) {
      bookings.value = response.data.records || response.data
      pagination.total = response.data.total || response.data.length
      
      // 计算统计数据
      calculateStats()
    }
  } catch (error) {
    console.error('加载预约数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadServices = async () => {
  try {
    const response = await serviceApi.getServiceList()
    if (response?.data) {
      services.value = response.data
    }
  } catch (error) {
    console.error('加载服务数据失败:', error)
  }
}

const calculateStats = () => {
  stats.pending = bookings.value.filter(item => item.status === 'pending').length
  stats.confirmed = bookings.value.filter(item => item.status === 'confirmed').length
  stats.processing = bookings.value.filter(item => item.status === 'processing').length
  stats.completed = bookings.value.filter(item => item.status === 'completed').length
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    serviceId: '',
    dateRange: null
  })
  handleSearch()
}

const refreshData = () => {
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedBookings.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadData()
}

const showAddBookingDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    customerName: '',
    customerPhone: '',
    serviceId: '',
    appointmentTime: '',
    remark: ''
  })
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const apiMethod = isEdit.value ? orderApi.updateOrder : orderApi.createOrder
    await apiMethod(form)
    
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const confirmBooking = async (booking) => {
  try {
    console.log('确认预约，订单ID:', booking.id)
    await orderApi.updateBookingStatus(booking.id, 'confirmed')
    ElMessage.success('预约已确认')
    loadData()
  } catch (error) {
    console.error('确认预约失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const startService = async (booking) => {
  try {
    console.log('开始服务，订单ID:', booking.id)
    await orderApi.updateBookingStatus(booking.id, 'in_progress')
    ElMessage.success('服务已开始')
    loadData()
  } catch (error) {
    console.error('开始服务失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const completeService = async (booking) => {
  try {
    console.log('完成服务，订单ID:', booking.id)
    await orderApi.updateBookingStatus(booking.id, 'completed')
    ElMessage.success('服务已完成')
    loadData()
  } catch (error) {
    console.error('完成服务失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const viewDetail = (booking) => {
  currentBooking.value = booking
  detailVisible.value = true
}

const handleCommand = (command, booking) => {
  switch (command) {
    case 'edit':
      editBooking(booking)
      break
    case 'cancel':
      cancelBooking(booking)
      break
    case 'delete':
      deleteBooking(booking)
      break
  }
}

const editBooking = (booking) => {
  isEdit.value = true
  Object.assign(form, booking)
  dialogVisible.value = true
}

const cancelBooking = async (booking) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      type: 'warning'
    })
    
    console.log('取消预约，订单ID:', booking.id)
    await orderApi.updateBookingStatus(booking.id, 'cancelled')
    ElMessage.success('预约已取消')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const deleteBooking = async (booking) => {
  try {
    await ElMessageBox.confirm('确定要删除这个预约吗？删除后不可恢复', '警告', {
      type: 'error'
    })
    
    await orderApi.deleteOrder(booking.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 工具方法
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'primary',
    processing: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待确认',
    confirmed: '已确认',
    processing: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

const formatDate = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleDateString()
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleTimeString()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

// 生命周期
onMounted(() => {
  loadData()
  loadServices()
})
</script>

<style scoped>
.booking-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #1f2937;
}

.header-left p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-section {
  margin-bottom: 20px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stats-card.pending .stats-icon {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.stats-card.confirmed .stats-icon {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.stats-card.processing .stats-icon {
  background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
}

.stats-card.completed .stats-icon {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.stats-number {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.table-section {
  margin-bottom: 20px;
}

.customer-info .customer-name {
  font-weight: 500;
  color: #1f2937;
}

.customer-info .customer-phone {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.service-info .service-name {
  color: #1f2937;
}

.service-info .service-price {
  font-size: 12px;
  color: #52c41a;
  font-weight: 600;
  margin-top: 2px;
}

.time-info .time-detail {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .booking-management {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>