<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1>管理员后台</h1>
      <div class="admin-user">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            {{ userInfo.name }}
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="admin-content">
      <el-container>
        <el-aside width="200px" class="admin-sidebar">
          <el-menu
            :default-active="activeMenu"
            class="admin-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><DataBoard /></el-icon>
              <span>数据看板</span>
            </el-menu-item>
            <el-menu-item index="services">
              <el-icon><Setting /></el-icon>
              <span>服务管理</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="system">
              <el-icon><Tools /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="admin-main">
          <div v-if="activeMenu === 'dashboard'" class="admin-section">
            <h2>数据统计分析</h2>
            
            <!-- 统计卡片 -->
            <div class="stats-grid">
              <div class="stat-card" v-for="stat in statsData" :key="stat.id">
                <div class="stat-icon" :style="{ background: stat.color }">
                  <el-icon><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-number">{{ stat.value }}</div>
                  <div class="stat-label">{{ stat.label }}</div>
                  <div class="stat-trend" :class="stat.trend > 0 ? 'positive' : 'negative'">
                    <el-icon><ArrowUp v-if="stat.trend > 0" /><ArrowDown v-else /></el-icon>
                    <span>{{ Math.abs(stat.trend) }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 图表区域 -->
            <div class="charts-grid">
              <!-- 预约趋势图 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>预约趋势分析</h3>
                  <el-select v-model="trendPeriod" size="small" style="width: 120px">
                    <el-option label="最近7天" value="7days" />
                    <el-option label="最近30天" value="30days" />
                    <el-option label="最近3个月" value="3months" />
                  </el-select>
                </div>
                <div class="chart-container">
                  <div ref="trendChart" class="chart"></div>
                </div>
              </div>

              <!-- 服务类型分布 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>服务类型分布</h3>
                </div>
                <div class="chart-container">
                  <div ref="serviceChart" class="chart"></div>
                </div>
              </div>

              <!-- 收入统计 -->
              <div class="chart-card full-width">
                <div class="chart-header">
                  <h3>收入统计分析</h3>
                  <div class="chart-controls">
                    <el-radio-group v-model="revenueType" size="small">
                      <el-radio-button label="daily">日收入</el-radio-button>
                      <el-radio-button label="monthly">月收入</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>
                <div class="chart-container">
                  <div ref="revenueChart" class="chart"></div>
                </div>
              </div>

              <!-- 时段热力图 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>预约时段热力图</h3>
                </div>
                <div class="chart-container">
                  <div ref="heatmapChart" class="chart"></div>
                </div>
              </div>

              <!-- 客户满意度 -->
              <div class="chart-card">
                <div class="chart-header">
                  <h3>客户满意度</h3>
                </div>
                <div class="chart-container">
                  <div ref="satisfactionChart" class="chart"></div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="activeMenu === 'services'" class="admin-section">
            <div class="section-header">
              <h2>服务管理</h2>
              <el-button type="primary" @click="showAddService = true">
                <el-icon><Plus /></el-icon>
                添加服务
              </el-button>
            </div>
            <el-table :data="services" style="width: 100%">
              <el-table-column prop="name" label="服务名称" />
              <el-table-column prop="price" label="价格">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="时长">
                <template #default="scope">
                  {{ scope.row.duration }}分钟
                </template>
              </el-table-column>
              <el-table-column prop="category" label="分类" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" @click="editService(scope.row)">
                    编辑
                  </el-button>
                  <el-button 
                    size="small" 
                    :type="scope.row.status === 1 ? 'warning' : 'success'"
                    @click="toggleServiceStatus(scope.row)"
                  >
                    {{ scope.row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else-if="activeMenu === 'orders'" class="admin-section">
            <h2>订单管理</h2>
            <el-table :data="orders" style="width: 100%">
              <el-table-column prop="id" label="订单号" />
              <el-table-column prop="serviceName" label="服务项目" />
              <el-table-column prop="userName" label="用户" />
              <el-table-column prop="price" label="金额">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" />
            </el-table>
          </div>

          <div v-else-if="activeMenu === 'users'" class="admin-section">
            <h2>用户管理</h2>
            <el-table :data="users" style="width: 100%">
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="realName" label="真实姓名" />
              <el-table-column prop="phone" label="手机号" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="role" label="角色">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
                    {{ scope.row.role === 'admin' ? '管理员' : '用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else-if="activeMenu === 'system'" class="admin-section">
            <h2>系统设置</h2>
            <div class="system-info">
              <el-descriptions title="系统信息" :column="2" border>
                <el-descriptions-item label="系统名称">汽车洗车服务预约系统</el-descriptions-item>
                <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
                <el-descriptions-item label="后端状态">
                  <el-tag type="success">运行中</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="数据库状态">
                  <el-tag type="success">正常</el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <div class="system-actions">
                <el-button type="primary" @click="$router.push('/api-test')">
                  API 测试
                </el-button>
                <el-button type="warning" @click="clearCache">
                  清除缓存
                </el-button>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, DataBoard, Setting, Document, User, Tools, 
  Plus, Money, Calendar, Star, ArrowUp
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()

// 用户信息
const userInfo = reactive({
  name: '管理员'
})

// 当前激活的菜单
const activeMenu = ref('dashboard')

// 图表相关引用
const trendChart = ref(null)
const serviceChart = ref(null)
const revenueChart = ref(null)
const heatmapChart = ref(null)
const satisfactionChart = ref(null)

const trendPeriod = ref('7days')
const revenueType = ref('daily')

// 统计数据
const statsData = reactive([
  {
    id: 1,
    icon: 'Calendar',
    label: '今日预约',
    value: '156',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: 12.5
  },
  {
    id: 2,
    icon: 'Money',
    label: '今日收入',
    value: '¥8,420',
    color: 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
    trend: 8.3
  },
  {
    id: 3,
    icon: 'User',
    label: '活跃用户',
    value: '2,341',
    color: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
    trend: -2.1
  },
  {
    id: 4,
    icon: 'Star',
    label: '平均评分',
    value: '4.8',
    color: 'linear-gradient(135deg, #EC4899 0%, #DB2777 100%)',
    trend: 5.2
  }
])

let trendChartInstance = null
let serviceChartInstance = null
let revenueChartInstance = null
let heatmapChartInstance = null
let satisfactionChartInstance = null

// 服务列表
const services = ref([
  { id: 1, name: '基础洗车', price: 30, duration: 30, category: '基础服务', status: 1 },
  { id: 2, name: '精致洗车', price: 50, duration: 45, category: '精致服务', status: 1 },
  { id: 3, name: '豪华洗车', price: 80, duration: 60, category: '豪华服务', status: 1 }
])

// 订单列表
const orders = ref([
  { id: 'ORD001', serviceName: '基础洗车', userName: '张三', price: 30, status: 'completed', createTime: '2025-10-18 10:30' },
  { id: 'ORD002', serviceName: '精致洗车', userName: '李四', price: 50, status: 'pending', createTime: '2025-10-18 11:15' },
  { id: 'ORD003', serviceName: '豪华洗车', userName: '王五', price: 80, status: 'processing', createTime: '2025-10-18 12:00' }
])

// 用户列表
const users = ref([
  { id: 1, username: 'admin', realName: '管理员', phone: '13800138000', email: 'admin@carwash.com', role: 'admin', status: 1 },
  { id: 2, username: 'user001', realName: '张三', phone: '13800138001', email: 'user001@example.com', role: 'user', status: 1 }
])

// 显示添加服务对话框
const showAddService = ref(false)

// 菜单选择处理
const handleMenuSelect = (key) => {
  activeMenu.value = key
}

// 用户操作处理
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('tokenType')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  }
}

// 编辑服务
const editService = (service) => {
  ElMessage.info(`编辑服务: ${service.name}`)
}

// 切换服务状态
const toggleServiceStatus = (service) => {
  service.status = service.status === 1 ? 0 : 1
  ElMessage.success(`${service.name} 已${service.status === 1 ? '启用' : '禁用'}`)
}

// 获取订单状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
}

// 获取订单状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || '未知'
}

// 初始化预约趋势图
const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['预约数量', '完成数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '预约数量',
        type: 'line',
        smooth: true,
        data: [45, 52, 38, 65, 72, 89, 94],
        itemStyle: {
          color: '#1890ff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: '#1890ff'
            }, {
              offset: 1, color: 'rgba(24, 144, 255, 0.2)'
            }]
          }
        }
      },
      {
        name: '完成数量',
        type: 'line',
        smooth: true,
        data: [42, 48, 35, 61, 68, 85, 90],
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

// 初始化服务类型分布图
const initServiceChart = () => {
  if (!serviceChart.value) return
  
  serviceChartInstance = echarts.init(serviceChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '服务类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 335, name: '基础洗车' },
          { value: 310, name: '精洗套餐' },
          { value: 234, name: '内饰清洁' },
          { value: 135, name: '打蜡服务' },
          { value: 154, name: '镀膜服务' }
        ]
      }
    ]
  }
  
  serviceChartInstance.setOption(option)
}

// 初始化收入统计图
const initRevenueChart = () => {
  if (!revenueChart.value) return
  
  revenueChartInstance = echarts.init(revenueChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['收入', '成本', '利润']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: [45000, 52000, 38000, 65000, 72000, 89000, 94000, 87000, 76000, 82000, 91000, 98000],
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '成本',
        type: 'bar',
        data: [25000, 28000, 22000, 35000, 38000, 45000, 48000, 44000, 38000, 42000, 46000, 49000],
        itemStyle: {
          color: '#faad14'
        }
      },
      {
        name: '利润',
        type: 'line',
        data: [20000, 24000, 16000, 30000, 34000, 44000, 46000, 43000, 38000, 40000, 45000, 49000],
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  
  revenueChartInstance.setOption(option)
}

// 初始化热力图
const initHeatmapChart = () => {
  if (!heatmapChart.value) return
  
  heatmapChartInstance = echarts.init(heatmapChart.value)
  
  const hours = ['8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00']
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  const data = []
  for (let i = 0; i < days.length; i++) {
    for (let j = 0; j < hours.length; j++) {
      data.push([j, i, Math.floor(Math.random() * 20)])
    }
  }
  
  const option = {
    tooltip: {
      position: 'top',
      formatter: function (params) {
        return `${days[params.value[1]]} ${hours[params.value[0]]}<br/>预约数量: ${params.value[2]}`
      }
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: days,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 20,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%',
      inRange: {
        color: ['#e6f7ff', '#1890ff', '#0050b3']
      }
    },
    series: [{
      name: '预约热力',
      type: 'heatmap',
      data: data,
      label: {
        show: true
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  heatmapChartInstance.setOption(option)
}

// 初始化满意度图表
const initSatisfactionChart = () => {
  if (!satisfactionChart.value) return
  
  satisfactionChartInstance = echarts.init(satisfactionChart.value)
  
  const option = {
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%'
    },
    series: [
      {
        name: '满意度',
        type: 'gauge',
        progress: {
          show: true
        },
        detail: {
          valueAnimation: true,
          formatter: '{value}%'
        },
        data: [
          {
            value: 87,
            name: '客户满意度'
          }
        ],
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.3, '#fd666d'],
              [0.7, '#37a2da'],
              [1, '#1890ff']
            ]
          }
        }
      }
    ]
  }
  
  satisfactionChartInstance.setOption(option)
}

// 响应式处理
const handleResize = () => {
  nextTick(() => {
    trendChartInstance?.resize()
    serviceChartInstance?.resize()
    revenueChartInstance?.resize()
    heatmapChartInstance?.resize()
    satisfactionChartInstance?.resize()
  })
}

// 清除缓存
const clearCache = () => {
  ElMessage.success('缓存已清除')
}

// 初始化
onMounted(() => {
  // 获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      const parsed = JSON.parse(storedUserInfo)
      userInfo.name = parsed.realName || parsed.username || '管理员'
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }

  // 初始化图表
  nextTick(() => {
    initTrendChart()
    initServiceChart()
    initRevenueChart()
    initHeatmapChart()
    initSatisfactionChart()
    
    window.addEventListener('resize', handleResize)
  })
})

// 监听筛选条件变化
watch([trendPeriod, revenueType], () => {
  console.log('筛选条件变化:', trendPeriod.value, revenueType.value)
})

// 监听菜单切换，重新初始化图表
watch(activeMenu, (newValue) => {
  if (newValue === 'dashboard') {
    nextTick(() => {
      initTrendChart()
      initServiceChart()
      initRevenueChart()
      initHeatmapChart()
      initSatisfactionChart()
    })
  }
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.admin-header {
  background: white;
  padding: 0 24px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.admin-header h1 {
  margin: 0;
  color: #1890ff;
  font-size: 20px;
}

.admin-user {
  cursor: pointer;
}

.admin-content {
  height: calc(100vh - 64px);
}

.admin-sidebar {
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.admin-menu {
  border-right: none;
  height: 100%;
}

.admin-main {
  padding: 24px;
  background: #f5f5f5;
}

.admin-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  color: white;
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.9rem;
  font-weight: 500;
}

.stat-trend.positive {
  color: #a7f3d0;
}

.stat-trend.negative {
  color: #fecaca;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  color: #262626;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
}

.chart-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chart-container {
  padding: 20px;
}

.chart {
  width: 100%;
  height: 300px;
}

.system-info {
  max-width: 800px;
}

.system-actions {
  margin-top: 24px;
}

.system-actions .el-button {
  margin-right: 12px;
}
</style>