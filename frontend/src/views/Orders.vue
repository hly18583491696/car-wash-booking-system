<template>
  <div class="orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">我的订单</h1>
        <p class="page-subtitle">查看和管理您的洗车服务订单</p>
      </div>
    </div>

    <div class="orders-container">
      <div class="container">
        <!-- 订单筛选 -->
        <div class="orders-filters">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部订单" name="all">
              <template #label>
                <span class="tab-label">
                  全部订单
                  <el-badge :value="orderCounts.all" :hidden="orderCounts.all === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="待确认" name="pending">
              <template #label>
                <span class="tab-label">
                  待确认
                  <el-badge :value="orderCounts.pending" :hidden="orderCounts.pending === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已确认" name="confirmed">
              <template #label>
                <span class="tab-label">
                  已确认
                  <el-badge :value="orderCounts.confirmed" :hidden="orderCounts.confirmed === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="服务中" name="in-service">
              <template #label>
                <span class="tab-label">
                  服务中
                  <el-badge :value="orderCounts.inService" :hidden="orderCounts.inService === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已完成" name="completed">
              <template #label>
                <span class="tab-label">
                  已完成
                  <el-badge :value="orderCounts.completed" :hidden="orderCounts.completed === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已取消" name="cancelled">
              <template #label>
                <span class="tab-label">
                  已取消
                  <el-badge :value="orderCounts.cancelled" :hidden="orderCounts.cancelled === 0" />
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 订单列表 -->
        <div class="orders-list">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          
          <div v-else-if="filteredOrders.length === 0" class="empty-state">
            <el-empty :description="getEmptyDescription()">
              <router-link to="/appointment">
                <el-button type="primary">立即预约</el-button>
              </router-link>
            </el-empty>
          </div>
          
          <div v-else class="orders-grid">
            <div class="order-card" v-for="order in filteredOrders" :key="order.id">
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-info">
                  <span class="order-number">订单号：{{ order.orderNumber }}</span>
                  <span class="order-date">{{ formatDate(order.createTime) }}</span>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusType(order.status)" size="large">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 订单内容 -->
              <div class="order-content">
                <div class="service-info">
                  <div class="service-icon">
                    <el-icon size="32" :color="order.service.color">
                      <component :is="order.service.icon" />
                    </el-icon>
                  </div>
                  <div class="service-details">
                    <h4 class="service-name">{{ order.service.name }}</h4>
                    <p class="service-desc">{{ order.service.description }}</p>
                    <div class="service-meta">
                      <span class="price">¥{{ order.service.price }}</span>
                      <span class="duration">{{ order.service.duration }}</span>
                    </div>
                  </div>
                </div>

                <div class="appointment-info">
                  <div class="info-row">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDateTime(order.appointmentTime) }}</span>
                  </div>
                  <div class="info-row">
                    <el-icon><Car /></el-icon>
                    <span>{{ order.vehicle.plateNumber }} ({{ order.vehicle.brand }} {{ order.vehicle.model }})</span>
                  </div>
                  <div class="info-row">
                    <el-icon><Phone /></el-icon>
                    <span>{{ order.vehicle.phone }}</span>
                  </div>
                  <div v-if="order.vehicle.requirements" class="info-row">
                    <el-icon><Document /></el-icon>
                    <span>{{ order.vehicle.requirements }}</span>
                  </div>
                </div>
              </div>

              <!-- 订单操作 -->
              <div class="order-actions">
                <div class="action-buttons">
                  <el-button 
                    v-if="order.status === 'pending'" 
                    type="danger" 
                    size="small" 
                    plain
                    @click="cancelOrder(order)"
                  >
                    取消订单
                  </el-button>
                  
                  <el-button 
                    v-if="order.status === 'confirmed'" 
                    type="warning" 
                    size="small" 
                    plain
                    @click="modifyOrder(order)"
                  >
                    修改预约
                  </el-button>
                  
                  <el-button 
                    v-if="order.status === 'completed' && !order.reviewed" 
                    type="primary" 
                    size="small"
                    @click="reviewOrder(order)"
                  >
                    评价服务
                  </el-button>
                  
                  <el-button 
                    v-if="order.status === 'completed' && order.reviewed" 
                    size="small" 
                    plain
                    @click="viewReview(order)"
                  >
                    查看评价
                  </el-button>
                  
                  <el-button 
                    size="small" 
                    plain
                    @click="viewOrderDetail(order)"
                  >
                    查看详情
                  </el-button>
                  
                  <el-button 
                    type="success" 
                    size="small" 
                    plain
                    @click="rebookOrder(order)"
                  >
                    再次预约
                  </el-button>
                </div>
              </div>

              <!-- 进度条 -->
              <div v-if="order.status !== 'cancelled'" class="order-progress">
                <el-steps :active="getProgressStep(order.status)" finish-status="success" simple>
                  <el-step title="订单提交" />
                  <el-step title="确认预约" />
                  <el-step title="开始服务" />
                  <el-step title="服务完成" />
                </el-steps>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="filteredOrders.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="totalOrders"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="服务评价" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="服务评分">
          <el-rate v-model="reviewForm.rating" :max="5" show-text />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请分享您的服务体验..."
          />
        </el-form-item>
        <el-form-item label="服务标签">
          <el-checkbox-group v-model="reviewForm.tags">
            <el-checkbox label="服务态度好" />
            <el-checkbox label="清洗效果佳" />
            <el-checkbox label="速度快" />
            <el-checkbox label="价格合理" />
            <el-checkbox label="环境整洁" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Orders',
  setup() {
    const router = useRouter()
    
    const loading = ref(false)
    const activeTab = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const reviewDialogVisible = ref(false)
    const currentReviewOrder = ref(null)
    
    // 评价表单
    const reviewForm = ref({
      rating: 5,
      content: '',
      tags: []
    })
    
    // 模拟订单数据
    const orders = ref([
      {
        id: 1,
        orderNumber: 'CW202509270001',
        status: 'pending',
        createTime: new Date('2025-09-27 09:30:00'),
        appointmentTime: new Date('2025-09-28 14:00:00'),
        service: {
          id: 2,
          name: '精洗套餐',
          description: '深度清洁，内外兼修',
          price: 68,
          duration: '60分钟',
          icon: 'Star',
          color: 'var(--warning-color)'
        },
        vehicle: {
          plateNumber: '京A12345',
          brand: '奥迪',
          model: 'A4L',
          color: 'white',
          phone: '13800138000',
          requirements: '请仔细清洗内饰'
        },
        reviewed: false
      },
      {
        id: 2,
        orderNumber: 'CW202509260001',
        status: 'confirmed',
        createTime: new Date('2025-09-26 15:20:00'),
        appointmentTime: new Date('2025-09-27 16:30:00'),
        service: {
          id: 1,
          name: '基础洗车',
          description: '外观清洗，轮胎清洁，玻璃清洁',
          price: 30,
          duration: '30分钟',
          icon: 'Car',
          color: 'var(--primary-color)'
        },
        vehicle: {
          plateNumber: '京B67890',
          brand: '宝马',
          model: 'X3',
          color: 'black',
          phone: '13900139000',
          requirements: ''
        },
        reviewed: false
      },
      {
        id: 3,
        orderNumber: 'CW202509250001',
        status: 'in-service',
        createTime: new Date('2025-09-25 10:15:00'),
        appointmentTime: new Date('2025-09-27 10:00:00'),
        service: {
          id: 3,
          name: '豪华套餐',
          description: '全方位护理，焕然一新',
          price: 128,
          duration: '90分钟',
          icon: 'Trophy',
          color: 'var(--error-color)'
        },
        vehicle: {
          plateNumber: '京C11111',
          brand: '奔驰',
          model: 'E300L',
          color: 'silver',
          phone: '13700137000',
          requirements: '需要打蜡服务'
        },
        reviewed: false
      },
      {
        id: 4,
        orderNumber: 'CW202509240001',
        status: 'completed',
        createTime: new Date('2025-09-24 14:30:00'),
        appointmentTime: new Date('2025-09-26 09:00:00'),
        service: {
          id: 2,
          name: '精洗套餐',
          description: '深度清洁，内外兼修',
          price: 68,
          duration: '60分钟',
          icon: 'Star',
          color: 'var(--warning-color)'
        },
        vehicle: {
          plateNumber: '京D22222',
          brand: '丰田',
          model: '凯美瑞',
          color: 'white',
          phone: '13600136000',
          requirements: ''
        },
        reviewed: false
      },
      {
        id: 5,
        orderNumber: 'CW202509230001',
        status: 'cancelled',
        createTime: new Date('2025-09-23 16:45:00'),
        appointmentTime: new Date('2025-09-25 15:30:00'),
        service: {
          id: 1,
          name: '基础洗车',
          description: '外观清洗，轮胎清洁，玻璃清洁',
          price: 30,
          duration: '30分钟',
          icon: 'CarWashing',
          color: 'var(--primary-color)'
        },
        vehicle: {
          plateNumber: '京E33333',
          brand: '本田',
          model: '雅阁',
          color: 'blue',
          phone: '13500135000',
          requirements: ''
        },
        reviewed: false
      }
    ])
    
    // 订单统计
    const orderCounts = computed(() => {
      const counts = {
        all: orders.value.length,
        pending: 0,
        confirmed: 0,
        inService: 0,
        completed: 0,
        cancelled: 0
      }
      
      orders.value.forEach(order => {
        switch (order.status) {
          case 'pending':
            counts.pending++
            break
          case 'confirmed':
            counts.confirmed++
            break
          case 'in-service':
            counts.inService++
            break
          case 'completed':
            counts.completed++
            break
          case 'cancelled':
            counts.cancelled++
            break
        }
      })
      
      return counts
    })
    
    // 筛选后的订单
    const filteredOrders = computed(() => {
      let filtered = orders.value
      
      if (activeTab.value !== 'all') {
        const statusMap = {
          'pending': 'pending',
          'confirmed': 'confirmed',
          'in-service': 'in-service',
          'completed': 'completed',
          'cancelled': 'cancelled'
        }
        filtered = filtered.filter(order => order.status === statusMap[activeTab.value])
      }
      
      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filtered.slice(start, end)
    })
    
    // 总订单数
    const totalOrders = computed(() => {
      if (activeTab.value === 'all') {
        return orders.value.length
      }
      
      const statusMap = {
        'pending': 'pending',
        'confirmed': 'confirmed',
        'in-service': 'in-service',
        'completed': 'completed',
        'cancelled': 'cancelled'
      }
      return orders.value.filter(order => order.status === statusMap[activeTab.value]).length
    })
    
    // 获取状态类型
    const getStatusType = (status) => {
      const typeMap = {
        'pending': 'warning',
        'confirmed': 'primary',
        'in-service': 'success',
        'completed': 'success',
        'cancelled': 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'pending': '待确认',
        'confirmed': '已确认',
        'in-service': '服务中',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return textMap[status] || '未知状态'
    }
    
    // 获取进度步骤
    const getProgressStep = (status) => {
      const stepMap = {
        'pending': 0,
        'confirmed': 1,
        'in-service': 2,
        'completed': 3
      }
      return stepMap[status] || 0
    }
    
    // 获取空状态描述
    const getEmptyDescription = () => {
      const descMap = {
        'all': '暂无订单记录',
        'pending': '暂无待确认订单',
        'confirmed': '暂无已确认订单',
        'in-service': '暂无服务中订单',
        'completed': '暂无已完成订单',
        'cancelled': '暂无已取消订单'
      }
      return descMap[activeTab.value] || '暂无数据'
    }
    
    // 格式化日期
    const formatDate = (date) => {
      return date.toLocaleDateString('zh-CN')
    }
    
    // 格式化日期时间
    const formatDateTime = (date) => {
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 切换标签
    const handleTabChange = (tab) => {
      currentPage.value = 1
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }
    
    // 取消订单
    const cancelOrder = async (order) => {
      try {
        await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 模拟取消订单
        order.status = 'cancelled'
        ElMessage.success('订单已取消')
        
      } catch {
        // 用户取消
      }
    }
    
    // 修改订单
    const modifyOrder = (order) => {
      router.push({
        path: '/appointment',
        query: { orderId: order.id }
      })
    }
    
    // 评价订单
    const reviewOrder = (order) => {
      currentReviewOrder.value = order
      reviewForm.value = {
        rating: 5,
        content: '',
        tags: []
      }
      reviewDialogVisible.value = true
    }
    
    // 提交评价
    const submitReview = () => {
      if (currentReviewOrder.value) {
        currentReviewOrder.value.reviewed = true
        ElMessage.success('评价提交成功')
        reviewDialogVisible.value = false
      }
    }
    
    // 查看评价
    const viewReview = (order) => {
      ElMessage.info('查看评价功能开发中...')
    }
    
    // 查看订单详情
    const viewOrderDetail = (order) => {
      router.push({
        path: '/order-detail',
        query: { id: order.id }
      })
    }
    
    // 再次预约
    const rebookOrder = (order) => {
      router.push({
        path: '/appointment',
        query: { serviceId: order.service.id }
      })
    }
    
    // 初始化
    onMounted(() => {
      loading.value = true
      // 模拟加载
      setTimeout(() => {
        loading.value = false
      }, 1000)
    })
    
    return {
      loading,
      activeTab,
      currentPage,
      pageSize,
      reviewDialogVisible,
      reviewForm,
      orders,
      orderCounts,
      filteredOrders,
      totalOrders,
      getStatusType,
      getStatusText,
      getProgressStep,
      getEmptyDescription,
      formatDate,
      formatDateTime,
      handleTabChange,
      handleSizeChange,
      handleCurrentChange,
      cancelOrder,
      modifyOrder,
      reviewOrder,
      submitReview,
      viewReview,
      viewOrderDetail,
      rebookOrder
    }
  }
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 页面头部 */
.page-header {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 60px 0;
  text-align: center;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

/* 订单容器 */
.orders-container {
  padding: 40px 0;
}

/* 订单筛选 */
.orders-filters {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 订单列表 */
.orders-list {
  margin-bottom: 32px;
}

.loading-container {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.empty-state {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 60px 20px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.orders-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 订单卡片 */
.order-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.order-card:hover {
  box-shadow: var(--shadow-lg);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-number {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.order-date {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 订单内容 */
.order-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 20px;
}

.service-info {
  display: flex;
  gap: 16px;
}

.service-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  background: var(--bg-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-details {
  flex: 1;
}

.service-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.service-desc {
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-size: 14px;
}

.service-meta {
  display: flex;
  gap: 16px;
}

.service-meta .price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-color);
}

.service-meta .duration {
  color: var(--text-light);
  font-size: 14px;
}

.appointment-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.info-row .el-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

/* 订单操作 */
.order-actions {
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 订单进度 */
.order-progress {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 40px 0;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .order-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .order-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .action-buttons {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .orders-container {
    padding: 20px 0;
  }
  
  .order-card {
    padding: 16px;
  }
  
  .service-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>