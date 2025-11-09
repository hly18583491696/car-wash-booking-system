<template>
  <div class="service-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>服务管理</h1>
        <p>管理洗车服务项目，设置价格和服务详情</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="showAddDialog">
          添加服务
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
              placeholder="搜索服务名称、描述"
              :prefix-icon="Search"
              clearable
              @input="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.category"
              placeholder="服务分类"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="基础洗车" value="basic" />
              <el-option label="精洗套餐" value="premium" />
              <el-option label="内饰清洁" value="interior" />
              <el-option label="美容服务" value="beauty" />
              <el-option label="保养维护" value="maintenance" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.status"
              placeholder="服务状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input-number
              v-model="searchForm.minPrice"
              placeholder="最低价格"
              :min="0"
              :precision="2"
              style="width: 48%"
            />
            <span style="margin: 0 2%">-</span>
            <el-input-number
              v-model="searchForm.maxPrice"
              placeholder="最高价格"
              :min="0"
              :precision="2"
              style="width: 48%"
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
          <el-card class="stats-card total">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Menu /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.total }}</div>
                <div class="stats-label">总服务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card active">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.active }}</div>
                <div class="stats-label">启用中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card premium">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.premium }}</div>
                <div class="stats-label">精品服务</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card revenue">
            <div class="stats-content">
              <div class="stats-icon">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">¥{{ stats.avgPrice }}</div>
                <div class="stats-label">平均价格</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 服务列表 -->
    <div class="table-section">
      <el-card shadow="never">
        <div class="table-header">
          <div class="table-title">服务列表</div>
          <div class="table-actions">
            <el-button-group>
              <el-button :icon="Grid" :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
                卡片视图
              </el-button>
              <el-button :icon="List" :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
                列表视图
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 卡片视图 -->
        <div v-if="viewMode === 'grid'" class="grid-view">
          <el-row :gutter="20">
            <el-col :span="8" v-for="service in services" :key="service.id">
              <el-card class="service-card" shadow="hover" @click="viewDetail(service)">
                <div class="service-header">
                  <div class="service-category">
                    <el-tag size="small" :type="getCategoryType(service.category)">
                      {{ getCategoryText(service.category) }}
                    </el-tag>
                  </div>
                  <div class="service-status">
                    <el-tag :type="service.status === 1 ? 'success' : 'danger'" size="small">
                      {{ service.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </div>
                </div>
                <div class="service-content">
                  <h3 class="service-name">{{ service.name }}</h3>
                  <p class="service-description">{{ service.description }}</p>
                  <div class="service-details">
                    <div class="service-price">¥{{ service.price }}</div>
                    <div class="service-duration">{{ service.duration }}分钟</div>
                  </div>
                </div>
                <div class="service-actions">
                  <el-button size="small" @click.stop="editService(service)">编辑</el-button>
                  <el-button
                    size="small"
                    :type="service.status === 1 ? 'warning' : 'success'"
                    @click.stop="toggleStatus(service)"
                  >
                    {{ service.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="danger" @click.stop="deleteService(service)">
                    删除
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 列表视图 -->
        <el-table
          v-else
          :data="services"
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="name" label="服务名称" min-width="150">
            <template #default="scope">
              <div class="service-name-cell">
                <strong>{{ scope.row.name }}</strong>
                <el-tag size="small" :type="getCategoryType(scope.row.category)" style="margin-left: 8px;">
                  {{ getCategoryText(scope.row.category) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="服务描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="price" label="价格" width="100" sortable>
            <template #default="scope">
              <span class="price-cell">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长" width="100" sortable>
            <template #default="scope">
              {{ scope.row.duration }}分钟
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="toggleStatus(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
                <el-button size="small" @click="editService(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteService(scope.row)">
                  删除
                </el-button>
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
            :page-sizes="[12, 24, 48, 96]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑服务对话框 -->
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
            <el-form-item label="服务名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入服务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="基础洗车" value="basic" />
                <el-option label="精洗套餐" value="premium" />
                <el-option label="内饰清洁" value="interior" />
                <el-option label="美容服务" value="beauty" />
                <el-option label="保养维护" value="maintenance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入价格"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务时长" prop="duration">
              <el-input-number
                v-model="form.duration"
                :min="15"
                :step="15"
                style="width: 100%"
                placeholder="请输入时长（分钟）"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入服务详细描述"
          />
        </el-form-item>
        <el-form-item label="服务特色" prop="features">
          <el-input
            v-model="form.features"
            type="textarea"
            :rows="3"
            placeholder="请输入服务特色和亮点"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="推荐服务" prop="recommended">
              <el-switch
                v-model="form.recommended"
                :active-value="1"
                :inactive-value="0"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 服务详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="服务详情"
      width="600px"
    >
      <el-descriptions v-if="currentService" :column="2" border>
        <el-descriptions-item label="服务名称">{{ currentService.name }}</el-descriptions-item>
        <el-descriptions-item label="服务分类">
          <el-tag :type="getCategoryType(currentService.category)">
            {{ getCategoryText(currentService.category) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务价格">¥{{ currentService.price }}</el-descriptions-item>
        <el-descriptions-item label="服务时长">{{ currentService.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="服务状态">
          <el-tag :type="currentService.status === 1 ? 'success' : 'danger'">
            {{ currentService.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="推荐服务">
          <el-tag :type="currentService.recommended === 1 ? 'warning' : ''">
            {{ currentService.recommended === 1 ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentService.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="服务描述" :span="2">
          {{ currentService.description || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务特色" :span="2">
          {{ currentService.features || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Refresh, Menu, Check, Star, Money,
  Grid, List
} from '@element-plus/icons-vue'
import serviceApi from '../../api/service.js'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const viewMode = ref('list') // 'grid' | 'list'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: '',
  minPrice: null,
  maxPrice: null
})

// 服务列表和分页
const services = ref([])
const selectedServices = ref([])
const currentService = ref(null)

const pagination = reactive({
  current: 1,
  size: 24,
  total: 0
})

// 统计数据
const stats = reactive({
  total: 0,
  active: 0,
  premium: 0,
  avgPrice: 0
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  category: '',
  description: '',
  features: '',
  price: 0,
  duration: 60,
  status: 1,
  recommended: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择服务分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入服务描述', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入服务时长', trigger: 'blur' },
    { type: 'number', min: 15, message: '时长不能小于15分钟', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑服务' : '添加服务')

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await serviceApi.getServiceList(params)
    if (response?.data) {
      services.value = response.data.records || response.data
      pagination.total = response.data.total || response.data.length
      
      // 计算统计数据
      calculateStats()
    }
  } catch (error) {
    console.error('加载服务数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  stats.total = services.value.length
  stats.active = services.value.filter(item => item.status === 1).length
  stats.premium = services.value.filter(item => item.category === 'premium').length
  
  const totalPrice = services.value.reduce((sum, item) => sum + (item.price || 0), 0)
  stats.avgPrice = stats.total > 0 ? Math.round(totalPrice / stats.total) : 0
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    status: '',
    minPrice: null,
    maxPrice: null
  })
  handleSearch()
}

const refreshData = () => {
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedServices.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadData()
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    category: '',
    description: '',
    features: '',
    price: 0,
    duration: 60,
    status: 1,
    recommended: 0
  })
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const apiMethod = isEdit.value ? serviceApi.updateService : serviceApi.createService
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

const editService = (service) => {
  isEdit.value = true
  Object.assign(form, service)
  dialogVisible.value = true
}

const toggleStatus = async (service) => {
  try {
    await serviceApi.updateServiceStatus(service.id, service.status)
    ElMessage.success(`服务已${service.status === 1 ? '启用' : '禁用'}`)
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
    // 恢复状态
    service.status = service.status === 1 ? 0 : 1
  }
}

const deleteService = async (service) => {
  try {
    await ElMessageBox.confirm('确定要删除这个服务吗？删除后不可恢复', '警告', {
      type: 'error'
    })
    
    await serviceApi.deleteService(service.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewDetail = (service) => {
  currentService.value = service
  detailVisible.value = true
}

// 工具方法
const getCategoryType = (category) => {
  const types = {
    basic: '',
    premium: 'warning',
    interior: 'success',
    beauty: 'danger',
    maintenance: 'info'
  }
  return types[category] || ''
}

const getCategoryText = (category) => {
  const texts = {
    basic: '基础洗车',
    premium: '精洗套餐',
    interior: '内饰清洁',
    beauty: '美容服务',
    maintenance: '保养维护'
  }
  return texts[category] || '未知'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.service-management {
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

.stats-card.total .stats-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-card.active .stats-icon {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.stats-card.premium .stats-icon {
  background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
}

.stats-card.revenue .stats-icon {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
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

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.grid-view {
  margin-bottom: 20px;
}

.service-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-card:hover {
  transform: translateY(-2px);
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.service-content {
  margin-bottom: 16px;
}

.service-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.service-description {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.service-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-price {
  font-size: 20px;
  font-weight: 700;
  color: #52c41a;
}

.service-duration {
  color: #6b7280;
  font-size: 14px;
}

.service-actions {
  display: flex;
  gap: 8px;
}

.service-name-cell strong {
  color: #1f2937;
}

.price-cell {
  font-weight: 600;
  color: #52c41a;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .service-management {
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
  
  .grid-view .el-col {
    margin-bottom: 16px;
  }
}
</style>