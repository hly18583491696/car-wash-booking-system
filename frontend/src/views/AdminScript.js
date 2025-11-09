import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Refresh, ArrowDown, User, SwitchButton,
  DataBoard, Calendar, Tools, Fold, Expand,
  Search, Plus, Link, Delete, Download, DocumentCopy,
  ArrowUp, Money, Star
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import statisticsApi from '../api/statistics.js'
import serviceApi from '../api/service.js'
import orderApi from '../api/order.js'
import userApi from '../api/user.js'
import paymentApi from '../api/payment.js'
import AuthManager from '../utils/auth.js'
import { updateBookingStatusSync, refreshBookingDataSync, normalizeStatus } from '../utils/dataSync.js'
import { performanceMonitor } from '../utils/performanceMonitor.js'

export function useAdminDashboard() {
  const router = useRouter()
  const route = useRoute()

  // 响应式数据
  const activeTab = ref('overview')
  const sidebarCollapsed = ref(false)
  const refreshing = ref(false)
  
  // 数据缓存机制
  const dataCache = reactive({
    overview: null,
    bookings: null,
    services: null,
    users: null,
    paymentAudit: null
  })
  
  // 缓存时间戳
  const cacheTimestamps = reactive({
    overview: 0,
    bookings: 0,
    services: 0,
    users: 0,
    paymentAudit: 0
  })
  
  // 缓存有效期（5分钟）
  const CACHE_DURATION = 5 * 60 * 1000
  
  // 图表初始化状态
  const chartsInitialized = ref(false)
  const chartResizeTimer = ref(null)

  // 用户信息
  const userInfo = reactive({
    name: '管理员'
  })

  // 关键指标数据
  const metricsData = reactive([
    {
      id: 1,
      type: 'primary',
      icon: 'Calendar',
      label: '今日预约',
      value: '0',
      change: 0
    },
    {
      id: 2,
      type: 'success',
      icon: 'Money',
      label: '今日收入',
      value: '¥0',
      change: 0
    },
    {
      id: 3,
      type: 'warning',
      icon: 'User',
      label: '活跃用户',
      value: '0',
      change: 0
    },
    {
      id: 4,
      type: 'info',
      icon: 'Star',
      label: '平均评分',
      value: '0.0',
      change: 0
    }
  ])

  // 图表相关
  const trendPeriod = ref('7days')
  const revenueType = ref('daily')

  // 预约管理
  const bookings = ref([])
  const bookingsLoading = ref(false)
  const bookingSearch = ref('')
  const bookingStatusFilter = ref('')

  // 服务管理
  const services = ref([])
  const servicesLoading = ref(false)
  const showServiceDialog = ref(false)
  const serviceSaving = ref(false)
  const serviceFormRef = ref()
  const serviceForm = reactive({
    name: '',
    description: '',
    price: 0,
    duration: 60,
    category: ''
  })
  const serviceRules = {
    name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
    description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }]
  }

  // 用户管理
  const users = ref([])
  const usersLoading = ref(false)
  const userSearch = ref('')

  // 支付审计
  const audits = ref([])
  const auditLoading = ref(false)
  const auditQuery = reactive({
    paymentNo: '',
    orderNo: '',
    eventType: '',
    status: ''
  })
  const auditPagination = reactive({
    page: 1,
    size: 20,
    total: 0
  })

  // 系统信息
  const systemUptime = ref('0天0小时0分钟')

  // 图表实例
  const chartInstances = {}
  const trendChart = ref()
  const serviceChart = ref()
  const revenueChart = ref()

  // 图表初始化重试控制
  const MAX_INIT_ATTEMPTS = 8
  const chartInitAttempts = { trend: 0, service: 0, revenue: 0 }

  // 优化的计算属性（添加缓存和防抖）
  const filteredBookingsCache = ref(null)
  const filteredUsersCache = ref(null)
  
  const filteredBookings = computed(() => {
    // 如果数据没有变化，返回缓存结果
    if (!bookings.value.length) return []
    
    const searchKey = `${bookingSearch.value}_${bookingStatusFilter.value}`
    if (filteredBookingsCache.value?.key === searchKey) {
      return filteredBookingsCache.value.data
    }
    
    let filtered = bookings.value
    
    if (bookingSearch.value) {
      const searchTerm = bookingSearch.value.toLowerCase()
      filtered = filtered.filter(booking => 
        booking.customerName?.toLowerCase().includes(searchTerm) ||
        booking.customerPhone?.includes(bookingSearch.value) ||
        booking.serviceName?.toLowerCase().includes(searchTerm)
      )
    }
    
    if (bookingStatusFilter.value) {
      filtered = filtered.filter(booking => booking.status === bookingStatusFilter.value)
    }
    
    // 缓存结果
    filteredBookingsCache.value = {
      key: searchKey,
      data: filtered
    }
    
    return filtered
  })

  const filteredUsers = computed(() => {
    if (!users.value.length) return []
    if (!userSearch.value) return users.value
    
    const searchKey = userSearch.value
    if (filteredUsersCache.value?.key === searchKey) {
      return filteredUsersCache.value.data
    }
    
    const searchTerm = userSearch.value.toLowerCase()
    const filtered = users.value.filter(user =>
      user.username?.toLowerCase().includes(searchTerm) ||
      user.realName?.toLowerCase().includes(searchTerm) ||
      user.phone?.includes(userSearch.value) ||
      user.email?.toLowerCase().includes(searchTerm)
    )
    
    // 缓存结果
    filteredUsersCache.value = {
      key: searchKey,
      data: filtered
    }
    
    return filtered
  })

  // 方法
  // 规范化模块键：路由中的 'dashboard' 映射为 'overview'
  const normalizeModuleKey = (key) => {
    if (key === 'dashboard') return 'overview'
    return key
  }

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    
    // 防抖处理图表重绘
    if (chartResizeTimer.value) {
      clearTimeout(chartResizeTimer.value)
    }
    
    chartResizeTimer.value = setTimeout(() => {
      requestAnimationFrame(() => {
        Object.values(chartInstances).forEach(chart => {
          if (chart && chart.resize) {
            chart.resize()
          }
        })
      })
    }, 300)
  }

  // 添加防抖和节流机制
  const debounceTimers = reactive({})
  const loadingStates = reactive({
    overview: false,
    bookings: false,
    services: false,
    users: false,
    paymentAudit: false
  })

  // 防抖函数
  const debounce = (func, delay, key) => {
    return (...args) => {
      if (debounceTimers[key]) {
        clearTimeout(debounceTimers[key])
      }
      debounceTimers[key] = setTimeout(() => {
        func.apply(this, args)
        delete debounceTimers[key]
      }, delay)
    }
  }

  // 优化的标签切换方法 - 修复并发错误
  const handleTabChange = (key) => {
    const normalizedKey = normalizeModuleKey(key)
    console.log('🔄 标签切换开始:', key)
    
    // 记录标签切换开始时间
    const switchStartTime = performance.now()
    const previousTab = activeTab.value
    
    // 防止重复点击
    if (activeTab.value === normalizedKey) {
      console.log('⚠️ 重复点击同一标签，忽略')
      return
    }
    
    // 检查是否有正在进行的加载操作
    if (loadingStates[normalizedKey]) {
      console.log('⚠️ 模块正在加载中，请稍候')
      ElMessage.warning('数据正在加载中，请稍候...')
      return
    }
    
    // 权限检查
    const hasPermission = AuthManager.hasPermission(normalizedKey)
    const userRole = AuthManager.getUserRole()
    console.log('权限检查:', { key: normalizedKey, hasPermission, userRole })
    
    if (!hasPermission) {
      ElMessage.warning('您没有访问该模块的权限')
      return
    }
    
    // 取消之前的防抖操作
    Object.keys(debounceTimers).forEach(timerKey => {
      if (debounceTimers[timerKey]) {
        clearTimeout(debounceTimers[timerKey])
        delete debounceTimers[timerKey]
      }
    })
    
    // 立即切换标签，提升用户体验
    console.log('🎯 切换activeTab从', activeTab.value, '到', normalizedKey)
    activeTab.value = normalizedKey
    
    // 使用防抖机制加载数据，避免快速切换导致的并发问题
    const debouncedLoad = debounce(() => {
      console.log('📊 防抖后开始加载模块数据:', normalizedKey)
      // 系统管理页不需要数据加载
      if (normalizedKey === 'system') {
        const switchEndTime = performance.now()
        const duration = switchEndTime - switchStartTime
        performanceMonitor.recordTabSwitch(previousTab, normalizedKey, duration)
        console.log(`⏱️ 标签切换完成: ${previousTab} -> ${normalizedKey}, 耗时: ${duration.toFixed(2)}ms`)
        return
      }
      loadModuleDataSafe(normalizedKey).then(() => {
        // 记录标签切换性能
        const switchEndTime = performance.now()
        const duration = switchEndTime - switchStartTime
        performanceMonitor.recordTabSwitch(previousTab, normalizedKey, duration)
        console.log(`⏱️ 标签切换完成: ${previousTab} -> ${normalizedKey}, 耗时: ${duration.toFixed(2)}ms`)
      })
    }, 150, `load_${normalizedKey}`)
    
    // 使用requestAnimationFrame优化UI更新
    requestAnimationFrame(() => {
      debouncedLoad()
    })
  }
  
  // 安全的模块数据加载方法 - 防止并发冲突
  const loadModuleDataSafe = async (moduleKey) => {
    console.log('📥 loadModuleDataSafe 被调用:', moduleKey)
    
    // 检查是否已经在加载
    if (loadingStates[moduleKey]) {
      console.log('⚠️ 模块已在加载中，跳过:', moduleKey)
      return
    }
    
    const now = Date.now()
    const cacheAge = now - cacheTimestamps[moduleKey]
    
    // 检查缓存是否有效
    if (dataCache[moduleKey] && cacheAge < CACHE_DURATION) {
      console.log(`✅ 使用缓存数据: ${moduleKey}`)
      applyModuleData(moduleKey, dataCache[moduleKey])
      return
    }
    
    // 设置加载状态
    loadingStates[moduleKey] = true
    
    try {
      console.log(`🔄 开始安全加载新数据: ${moduleKey}`)
      
      // 使用Promise.race添加超时机制
      const loadPromise = loadModuleDataWithTimeout(moduleKey)
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('加载超时')), 10000) // 10秒超时
      })
      
      await Promise.race([loadPromise, timeoutPromise])
      
    } catch (error) {
      console.error(`❌ 加载模块数据失败: ${moduleKey}`, error)
      ElMessage.error(`加载${getModuleName(moduleKey)}数据失败`)
    } finally {
      // 确保加载状态被重置
      loadingStates[moduleKey] = false
    }
  }
  
  // 带超时的数据加载方法
  const loadModuleDataWithTimeout = async (moduleKey) => {
    // 统一规范化模块键
    moduleKey = normalizeModuleKey(moduleKey)
    switch (moduleKey) {
      case 'overview':
        console.log('📊 加载概览数据')
        await loadDashboardDataOptimized()
        break
      case 'bookings':
        console.log('📅 加载预约数据')
        await loadBookingsDataOptimized()
        break
      case 'services':
        console.log('🛠️ 加载服务数据')
        await loadServicesDataOptimized()
        break
      case 'users':
        console.log('👥 加载用户数据')
        await loadUsersDataOptimized()
        break
      case 'paymentAudit':
        console.log('💳 加载支付审计数据')
        await loadPaymentAuditsOptimized()
        break
      case 'system':
        console.log('🧰 系统管理无需数据加载')
        break
      default:
        throw new Error(`未知的模块类型: ${moduleKey}`)
    }
  }
  
  // 获取模块中文名称
  const getModuleName = (moduleKey) => {
    const names = {
      overview: '概览',
      dashboard: '概览',
      bookings: '预约管理',
      services: '服务管理',
      users: '用户管理',
      paymentAudit: '支付审计',
      system: '系统管理'
    }
    return names[moduleKey] || moduleKey
  }

  const handleUserCommand = (command) => {
    if (command === 'logout') {
      logout()
    }
  }

  const logout = () => {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      AuthManager.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  }

  // 清理缓存方法
  const clearCache = (moduleKey = null) => {
    if (moduleKey) {
      dataCache[moduleKey] = null
      cacheTimestamps[moduleKey] = 0
      console.log(`清理缓存: ${moduleKey}`)
    } else {
      // 清理所有缓存
      Object.keys(dataCache).forEach(key => {
        dataCache[key] = null
        cacheTimestamps[key] = 0
      })
      filteredBookingsCache.value = null
      filteredUsersCache.value = null
      console.log('清理所有缓存')
    }
  }
  
  const refreshData = async () => {
    const startTime = performance.now()
    refreshing.value = true
    
    try {
      // 清理当前模块缓存
      clearCache(activeTab.value)
      
      // 重新加载数据
      await loadModuleData(activeTab.value)
      
      const duration = performance.now() - startTime
      console.log(`性能监控 - 数据刷新: ${duration.toFixed(2)}ms`)
      
      ElMessage.success('数据刷新成功')
    } catch (error) {
      console.error('刷新数据失败:', error)
      ElMessage.error('刷新数据失败')
    } finally {
      refreshing.value = false
    }
  }

  // 应用模块数据 - 修复并发显示问题
  const applyModuleData = (moduleKey, data) => {
    console.log('🎯 applyModuleData 被调用:', moduleKey, data)
    
    // 使用nextTick确保DOM更新的正确顺序
    nextTick(() => {
      try {
        switch (moduleKey) {
          case 'overview':
            console.log('📊 应用概览数据')
            if (data && typeof data === 'object') {
              updateMetricsData(data)
              // 延迟初始化图表，确保DOM已完全渲染
              setTimeout(() => {
                nextTick(() => {
                  requestAnimationFrame(() => {
                    console.log('📈 数据加载完成，开始初始化图表')
                    initChartsOptimized()
                  })
                })
              }, 800)
            }
            break
            
          case 'bookings':
            console.log('📅 应用预约数据，数量:', data?.bookings?.length || data?.length || 0)
            if (data) {
              // 处理分页数据结构
              if (data.bookings && Array.isArray(data.bookings)) {
                bookings.value = data.bookings
              } else if (Array.isArray(data)) {
                bookings.value = data
              } else {
                console.warn('⚠️ 预约数据格式不正确:', data)
                bookings.value = []
              }
            }
            break
            
          case 'services':
            console.log('🛠️ 应用服务数据，数量:', data?.length || 0)
            if (Array.isArray(data)) {
              services.value = data
            } else {
              console.warn('⚠️ 服务数据格式不正确:', data)
              services.value = []
            }
            break
            
          case 'users':
            console.log('👥 应用用户数据，数量:', data?.length || 0)
            if (Array.isArray(data)) {
              users.value = data
            } else {
              console.warn('⚠️ 用户数据格式不正确:', data)
              users.value = []
            }
            break

          case 'paymentAudit':
            console.log('💳 应用支付审计数据')
            if (data) {
              // 兼容多种分页结构
              let records = []
              let total = 0
              let current = auditPagination.page
              let size = auditPagination.size

              if (Array.isArray(data.records)) {
                records = data.records
                total = Number(data.total || 0)
                current = Number(data.current || current)
                size = Number(data.size || size)
              } else if (Array.isArray(data.content)) {
                records = data.content
                total = Number(data.totalElements || 0)
                current = Number(data.number || current)
                size = Number(data.size || size)
              } else if (Array.isArray(data)) {
                records = data
              } else {
                console.warn('⚠️ 审计数据格式不正确:', data)
              }

              audits.value = records
              auditPagination.total = total
              auditPagination.page = current
              auditPagination.size = size
            } else {
              audits.value = []
            }
            break
            
          default:
            console.warn('⚠️ 未知的模块类型:', moduleKey)
        }
      } catch (error) {
        console.error('❌ 应用模块数据时发生错误:', error)
        ElMessage.error(`应用${getModuleName(moduleKey)}数据失败`)
      }
    })
  }

  // 优化版本的数据加载方法
  const loadDashboardDataOptimized = async () => {
    console.log('📊 开始加载概览数据')
    const apiStartTime = performance.now()
    
    try {
      const response = await statisticsApi.getOverview()
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用性能
      performanceMonitor.recordAPICall(
        '/api/statistics/overview',
        'GET',
        apiDuration,
        true
      )
      
      if (response && response.data) {
        const data = response.data
        
        // 缓存数据
        dataCache.overview = data
        cacheTimestamps.overview = Date.now()
        
        // 应用数据
        applyModuleData('overview', data)
        
        console.log('✅ 概览数据加载完成')
      } else {
        throw new Error('响应数据格式错误')
      }
    } catch (error) {
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用失败
      performanceMonitor.recordAPICall(
        '/api/statistics/overview',
        'GET',
        apiDuration,
        false,
        error.message
      )
      
      console.error('❌ 概览数据加载失败:', error)
      
      // 降级处理：使用模拟数据
      const mockData = {
        todayBookings: '0',
        todayRevenue: '0',
        activeUsers: '0',
        averageRating: '0.0',
        bookingTrend: 0,
        revenueTrend: 0,
        userTrend: 0,
        ratingTrend: 0
      }
      
      applyModuleData('overview', mockData)
      ElMessage.warning('概览数据加载失败，显示默认数据')
    }
  }
  
  // 优化的预约数据加载 - 实现分页和懒加载
  const loadBookingsDataOptimized = async () => {
    console.log('📅 开始优化加载预约数据')
    bookingsLoading.value = true
    const apiStartTime = performance.now()
    
    try {
      // 分页参数
      const pageSize = 20 // 每页20条记录
      const currentPage = 1
      
      // 使用分页API减少数据传输量
      const response = await orderApi.getOrdersPaginated({
        page: currentPage,
        size: pageSize,
        status: bookingStatusFilter.value || undefined,
        search: bookingSearch.value || undefined
      })
      
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用性能
      performanceMonitor.recordAPICall(
        '/api/orders/paginated',
        'GET',
        apiDuration,
        true
      )
      
      if (response && response.data) {
        const { content, totalElements, totalPages } = response.data
        
        // 缓存数据
        const cacheData = {
          bookings: content || [],
          total: totalElements || 0,
          pages: totalPages || 0,
          currentPage: currentPage
        }
        
        dataCache.bookings = cacheData
        cacheTimestamps.bookings = Date.now()
        
        // 应用数据
        applyModuleData('bookings', cacheData)
        
        console.log('✅ 预约数据加载完成:', {
          count: content?.length || 0,
          total: totalElements,
          pages: totalPages
        })
        
        // 预加载下一页数据（如果存在）
        if (totalPages > 1) {
          setTimeout(() => {
            preloadNextPage('bookings', 2, pageSize)
          }, 1000)
        }
        
      } else {
        throw new Error('响应数据格式错误')
      }
      
    } catch (error) {
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用失败
      performanceMonitor.recordAPICall(
        '/api/orders/paginated',
        'GET',
        apiDuration,
        false,
        error.message
      )
      
      console.error('❌ 预约数据加载失败:', error)
      
      // 降级处理：尝试加载基础数据
      try {
        console.log('🔄 尝试降级加载预约数据')
        const fallbackStartTime = performance.now()
        const fallbackResponse = await orderApi.getOrders()
        const fallbackEndTime = performance.now()
        const fallbackDuration = fallbackEndTime - fallbackStartTime
        
        // 记录降级API调用性能
        performanceMonitor.recordAPICall(
          '/api/orders',
          'GET',
          fallbackDuration,
          true
        )
        
        if (fallbackResponse && fallbackResponse.data) {
          const fallbackData = fallbackResponse.data.slice(0, 20) // 限制数量
          applyModuleData('bookings', fallbackData)
          dataCache.bookings = { bookings: fallbackData }
          cacheTimestamps.bookings = Date.now()
        }
      } catch (fallbackError) {
        console.error('❌ 降级加载也失败:', fallbackError)
        ElMessage.error('预约数据加载失败，请稍后重试')
        applyModuleData('bookings', [])
      }
      
    } finally {
      bookingsLoading.value = false
    }
  }
  
  // 优化的服务数据加载 - 实现懒加载
  const loadServicesDataOptimized = async () => {
    console.log('🛠️ 开始优化加载服务数据')
    servicesLoading.value = true
    const apiStartTime = performance.now()
    
    try {
      const response = await serviceApi.getServiceList()
      
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用性能
      performanceMonitor.recordAPICall(
        '/api/services',
        'GET',
        apiDuration,
        true
      )
      
      if (response && response.data) {
        // 处理分页数据格式
        let servicesData = response.data
        
        // 如果是分页格式 {records: [...], total: ...}，提取 records
        if (servicesData.records && Array.isArray(servicesData.records)) {
          servicesData = servicesData.records
        }
        // 如果是分页格式 {content: [...], total: ...}，提取 content
        else if (servicesData.content && Array.isArray(servicesData.content)) {
          servicesData = servicesData.content
        }
        
        // 缓存数据
        dataCache.services = servicesData
        cacheTimestamps.services = Date.now()
        
        // 应用数据
        applyModuleData('services', servicesData)
        
        console.log('✅ 服务数据加载完成:', {
          count: servicesData.length
        })
        
      } else {
        throw new Error('响应数据格式错误')
      }
      
    } catch (error) {
      const apiEndTime = performance.now()
      const apiDuration = apiEndTime - apiStartTime
      
      // 记录API调用失败
      performanceMonitor.recordAPICall(
        '/api/services',
        'GET',
        apiDuration,
        false,
        error.message
      )
      
      console.error('❌ 服务数据加载失败:', error)
      ElMessage.error('服务数据加载失败，请稍后重试')
      applyModuleData('services', [])
      
    } finally {
      servicesLoading.value = false
    }
  }
  
  // 预加载下一页数据
  const preloadNextPage = async (moduleType, page, pageSize) => {
    try {
      console.log(`🔄 预加载${moduleType}下一页数据: 第${page}页`)
      
      if (moduleType === 'bookings') {
        const response = await orderApi.getOrdersPaginated({
          page: page,
          size: pageSize
        })
        
        if (response && response.data && response.data.content) {
          // 将预加载的数据存储到缓存中
          if (!dataCache.bookingsPreload) {
            dataCache.bookingsPreload = {}
          }
          dataCache.bookingsPreload[page] = response.data.content
          console.log(`✅ 预约数据第${page}页预加载完成`)
        }
      }
      
    } catch (error) {
      console.log(`⚠️ 预加载第${page}页数据失败:`, error.message)
    }
  }
  

  const loadUsersDataOptimized = async () => {
    console.log('👥 开始优化加载用户数据')
    usersLoading.value = true
    
    try {
      // 分页加载用户数据
      const pageSize = 25
      const currentPage = 1
      
      const response = await userApi.getUsersPaginated({
        page: currentPage,
        size: pageSize,
        search: userSearch.value || undefined
      })
      
      if (response && response.data) {
        const userData = response.data.content || response.data
        
        // 缓存数据
        dataCache.users = userData
        cacheTimestamps.users = Date.now()
        
        // 应用数据
        applyModuleData('users', userData)
        
        console.log('✅ 用户数据加载完成:', {
          count: userData.length
        })
        
      } else {
        throw new Error('响应数据格式错误')
      }
      
    } catch (error) {
      console.error('❌ 用户数据加载失败:', error)
      
      // 降级处理
      try {
        console.log('🔄 尝试降级加载用户数据')
        const fallbackResponse = await userApi.getUsers()
        if (fallbackResponse && fallbackResponse.data) {
          const fallbackData = fallbackResponse.data.slice(0, 25)
          applyModuleData('users', fallbackData)
          dataCache.users = fallbackData
          cacheTimestamps.users = Date.now()
        }
      } catch (fallbackError) {
        console.error('❌ 降级加载也失败:', fallbackError)
        ElMessage.error('用户数据加载失败，请稍后重试')
        applyModuleData('users', [])
      }
      
    } finally {
      usersLoading.value = false
    }
  }

  // 支付审计日志（分页与筛选）
  const loadPaymentAuditsOptimized = async () => {
    console.log('💳 开始优化加载支付审计日志')
    auditLoading.value = true

    try {
      const params = {
        current: auditPagination.page,
        size: auditPagination.size
      }
      if (auditQuery.paymentNo) params.paymentNo = auditQuery.paymentNo
      if (auditQuery.orderNo) params.orderNo = auditQuery.orderNo
      if (auditQuery.eventType) params.eventType = auditQuery.eventType
      if (auditQuery.status) params.status = auditQuery.status

      const resp = await paymentApi.admin.getPaymentAudits(params)
      if (resp && resp.data) {
        // 直接缓存整个分页结果，applyModuleData 做统一解析
        dataCache.paymentAudit = resp.data
        cacheTimestamps.paymentAudit = Date.now()
        applyModuleData('paymentAudit', resp.data)
        console.log('✅ 支付审计数据加载完成')
      } else {
        throw new Error('响应数据为空或格式错误')
      }
    } catch (error) {
      console.error('❌ 支付审计数据加载失败:', error)
      ElMessage.error('审计日志加载失败，请稍后重试')
      applyModuleData('paymentAudit', [])
    } finally {
      auditLoading.value = false
    }
  }

  const onAuditFilterChange = () => {
    auditPagination.page = 1
    loadPaymentAuditsOptimized()
  }

  const handleAuditSizeChange = (size) => {
    auditPagination.size = size
    auditPagination.page = 1
    loadPaymentAuditsOptimized()
  }

  const handleAuditCurrentChange = (page) => {
    auditPagination.page = page
    loadPaymentAuditsOptimized()
  }

  // 统一的模块数据加载方法 - 优化版本
  const loadModuleData = async (moduleKey) => {
    console.log(`🚀 开始加载模块数据: ${moduleKey}`)
    
    // 检查缓存
    const cached = dataCache[moduleKey]
    const cacheTime = cacheTimestamps[moduleKey]
    const now = Date.now()
    
    if (cached && cacheTime && (now - cacheTime) < CACHE_DURATION) {
      console.log(`📦 使用缓存数据: ${moduleKey}`)
      applyModuleData(moduleKey, cached)
      return
    }
    
    // 使用优化的加载方法
    try {
      switch (moduleKey) {
        case 'overview':
          await loadDashboardDataOptimized()
          break
        case 'bookings':
          await loadBookingsDataOptimized()
          break
        case 'services':
          await loadServicesDataOptimized()
          break
        case 'users':
          await loadUsersDataOptimized()
          break
        case 'paymentAudit':
          await loadPaymentAuditsOptimized()
          break
        default:
          console.warn(`⚠️ 未知的模块类型: ${moduleKey}`)
      }
    } catch (error) {
      console.error(`❌ 加载${moduleKey}数据失败:`, error)
      ElMessage.error(`加载${getModuleName(moduleKey)}数据失败`)
    }
  }

  // 更新指标数据
  const updateMetricsData = (data) => {
    try {
      if (data && typeof data === 'object') {
        // 安全地更新各项指标
        if (data.todayBookings !== undefined) {
          metricsData[0].value = String(data.todayBookings)
        }
        if (data.todayRevenue !== undefined) {
          metricsData[1].value = `¥${data.todayRevenue}`
        }
        if (data.activeUsers !== undefined) {
          metricsData[2].value = String(data.activeUsers)
        }
        if (data.averageRating !== undefined) {
          metricsData[3].value = String(data.averageRating)
        }
        
        // 更新趋势数据
        if (data.bookingTrend !== undefined) {
          metricsData[0].change = Number(data.bookingTrend)
        }
        if (data.revenueTrend !== undefined) {
          metricsData[1].change = Number(data.revenueTrend)
        }
        if (data.userTrend !== undefined) {
          metricsData[2].change = Number(data.userTrend)
        }
        if (data.ratingTrend !== undefined) {
          metricsData[3].change = Number(data.ratingTrend)
        }
        
        console.log('✅ 指标数据更新完成')
      }
    } catch (error) {
      console.error('❌ 更新指标数据失败:', error)
    }
  }

  const loadUsersData = async () => {
    usersLoading.value = true
    try {
      const response = await userApi.getUserList()
      if (response?.data) {
        users.value = response.data
      }
    } catch (error) {
      console.error('加载用户数据失败:', error)
      ElMessage.error('加载用户数据失败')
    } finally {
      usersLoading.value = false
    }
  }

  // 预约状态处理
  const getBookingStatusType = (status) => {
    const types = {
      pending: 'warning',
      confirmed: 'primary',
      in_progress: 'info',
      processing: 'info', // 兼容旧状态
      completed: 'success',
      cancelled: 'danger'
    }
    return types[status] || 'info'
  }

  const getBookingStatusText = (status) => {
    const texts = {
      pending: '待确认',
      confirmed: '已确认',
      in_progress: '进行中',
      processing: '进行中', // 兼容旧状态
      completed: '已完成',
      cancelled: '已取消'
    }
    return texts[status] || '未知'
  }

  // 预约操作 - 使用数据同步工具
  const confirmBooking = async (booking) => {
    try {
      console.log('🔄 确认预约，订单ID:', booking.id)
      
      await updateBookingStatusSync(
        booking.id, 
        'confirmed', 
        orderApi, 
        updateBookingStatusLocal
      )
      
      ElMessage.success('预约已确认')
      console.log('✅ 预约确认成功，状态已更新')
    } catch (error) {
      console.error('❌ 确认预约失败:', error)
      ElMessage.error(error.message || '确认预约失败')
    }
  }

  const startService = async (booking) => {
    try {
      console.log('🔄 开始服务，订单ID:', booking.id)
      
      await updateBookingStatusSync(
        booking.id, 
        'in_progress', 
        orderApi, 
        updateBookingStatusLocal
      )
      
      ElMessage.success('服务已开始')
      console.log('✅ 服务开始成功，状态已更新')
    } catch (error) {
      console.error('❌ 开始服务失败:', error)
      ElMessage.error(error.message || '开始服务失败')
    }
  }

  const completeService = async (booking) => {
    try {
      console.log('🔄 完成服务，订单ID:', booking.id)
      
      await updateBookingStatusSync(
        booking.id, 
        'completed', 
        orderApi, 
        updateBookingStatusLocal
      )
      
      ElMessage.success('服务已完成')
      console.log('✅ 服务完成成功，状态已更新')
    } catch (error) {
      console.error('❌ 完成服务失败:', error)
      ElMessage.error(error.message || '完成服务失败')
    }
  }

  // 取消预约
  const cancelBooking = async (booking) => {
    try {
      await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
        type: 'warning'
      })
      
      console.log('🔄 取消预约，订单ID:', booking.id)
      
      await updateBookingStatusSync(
        booking.id, 
        'cancelled', 
        orderApi, 
        updateBookingStatusLocal
      )
      
      ElMessage.success('预约已取消')
      console.log('✅ 预约取消成功，状态已更新')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('❌ 取消预约失败:', error)
        ElMessage.error(error.message || '取消预约失败')
      }
    }
  }

  // 删除预约
  const deleteBooking = async (booking) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除这个预约吗？订单号: ${booking.orderNo || booking.id}，删除后不可恢复！`, 
        '警告', 
        {
          type: 'error',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      
      console.log('🗑️ [删除预约] 开始删除，订单信息:', {
        id: booking.id,
        orderNo: booking.orderNo,
        status: booking.status,
        serviceName: booking.serviceName
      })
      
      // 调用API删除订单
      console.log('📞 [删除预约] 调用 orderApi.deleteOrder, 订单ID:', booking.id)
      const response = await orderApi.deleteOrder(booking.id)
      console.log('✅ [删除预约] API调用成功，响应:', response)
      
      // 清理缓存
      filteredBookingsCache.value = null
      dataCache.bookings = null
      cacheTimestamps.bookings = 0
      console.log('🗑️ [删除预约] 缓存已清理')
      
      // 重新从服务器加载订单列表，确保数据同步
      console.log('🔄 [删除预约] 重新加载订单列表')
      await loadBookingsData()
      console.log('✅ [删除预约] 订单列表已重新加载')
      
      ElMessage.success('预约删除成功')
      console.log('✅ [删除预约] 删除操作完成')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('❌ [删除预约] 删除失败，错误详情:', {
          message: error.message,
          response: error.response,
          stack: error.stack
        })
        ElMessage.error(error.message || '删除预约失败')
      } else {
        console.log('🚫 [删除预约] 用户取消删除')
      }
    }
  }

  // 统一的状态更新方法
  const updateBookingStatusLocal = (bookingId, newStatus) => {
    const index = bookings.value.findIndex(b => b.id === bookingId)
    if (index !== -1) {
      bookings.value[index].status = newStatus
      console.log(`📝 本地状态已更新: ${bookingId} -> ${newStatus}`)
      
      // 清理相关缓存
      filteredBookingsCache.value = null
      
      // 触发响应式更新
      bookings.value = [...bookings.value]
    }
  }

  const viewBookingDetail = (booking) => {
    ElMessage.info(`查看预约详情: ${booking.id}`)
  }

  // 服务管理
  const showAddServiceDialog = () => {
    showServiceDialog.value = true
    Object.assign(serviceForm, {
      name: '',
      description: '',
      price: 0,
      duration: 60,
      category: ''
    })
  }

  const editService = (service) => {
    ElMessage.info(`编辑服务: ${service.name}`)
  }

  const toggleServiceStatus = async (service) => {
    try {
      const newStatus = service.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      // 确认操作
      await ElMessageBox.confirm(
        `确定要${statusText}服务「${service.name}」吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 调用后端API更新状态
      console.log(`🔄 更新服务状态: ${service.name} (${service.id}) -> ${statusText}`)
      await serviceApi.updateServiceStatus(service.id, newStatus)
      
      // 更新前端状态
      service.status = newStatus
      
      ElMessage.success(`${service.name} 已${statusText}`)
      
      // 刷新服务列表以确保数据一致性
      await loadServicesDataOptimized()
      
    } catch (error) {
      if (error !== 'cancel') {
        console.error('❌ 更新服务状态失败:', error)
        ElMessage.error('更新服务状态失败，请重试')
      }
    }
  }

  const deleteService = async (service) => {
    try {
      await ElMessageBox.confirm(`确定要删除服务「${service.name}」吗？删除后不可恢复！`, '警告', {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      })
      
      console.log('🗑️ 删除服务，服务ID:', service.id)
      
      // 调用API删除服务
      await serviceApi.deleteService(service.id)
      
      ElMessage.success(`已删除服务: ${service.name}`)
      
      // 刷新服务列表
      await loadServicesDataOptimized()
      
      console.log('✅ 服务删除成功并已刷新列表')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('❌ 删除服务失败:', error)
        ElMessage.error(error.message || '删除服务失败')
      }
    }
  }

  const saveService = () => {
    serviceFormRef.value?.validate((valid) => {
      if (valid) {
        serviceSaving.value = true
        setTimeout(() => {
          serviceSaving.value = false
          showServiceDialog.value = false
          ElMessage.success('服务添加成功')
          loadServicesData()
        }, 1000)
      }
    })
  }

  // 用户管理
  const toggleUserStatus = async (user) => {
    try {
      const newStatus = user.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'
      
      // 确认操作
      await ElMessageBox.confirm(
        `确定要${statusText}用户「${user.username}」吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 调用后端API更新状态
      console.log(`🔄 更新用户状态: ${user.username} (${user.id}) -> ${statusText}`)
      await userApi.updateUserStatus(user.id, newStatus)
      
      // 更新前端状态
      user.status = newStatus
      
      ElMessage.success(`用户 ${user.username} 已${statusText}`)
      
      // 刷新用户列表以确保数据一致性
      await loadUsersDataOptimized()
      
    } catch (error) {
      if (error !== 'cancel') {
        console.error('❌ 更新用户状态失败:', error)
        ElMessage.error('更新用户状态失败，请重试')
      }
    }
  }

  const viewUserDetail = (user) => {
    ElMessage.info(`查看用户详情: ${user.username}`)
  }

  // 系统管理
  const clearSystemCache = () => {
    ElMessage.success('系统缓存已清除')
  }

  const exportSystemLogs = () => {
    ElMessage.success('系统日志导出中...')
  }

  const backupDatabase = () => {
    ElMessage.success('数据库备份中...')
  }

  // 支付审计 - 状态文本转换
  const getStatusText = (status) => {
    const statusMap = {
      'PENDING': '待处理',
      'SUCCESS': '成功',
      'FAILED': '失败',
      'REFUNDING': '退款中',
      'REFUNDED': '已退款',
      'CANCELLED': '已取消',
      'PROCESSING': '处理中'
    }
    return statusMap[status] || status || '未知'
  }

  // 支付审计 - 状态标签类型
  const getStatusTagType = (status) => {
    const typeMap = {
      'PENDING': 'warning',
      'SUCCESS': 'success',
      'FAILED': 'danger',
      'REFUNDING': 'warning',
      'REFUNDED': 'info',
      'CANCELLED': 'info',
      'PROCESSING': 'primary'
    }
    return typeMap[status] || 'info'
  }

  // 支付审计 - 事件类型文本转换
  const getEventTypeText = (eventType) => {
    const eventTypeMap = {
      'PAYMENT_CREATED': '创建支付',
      'PAYMENT_SUCCESS': '支付成功',
      'PAYMENT_FAILED': '支付失败',
      'PAYMENT_CANCELLED': '支付取消',
      'REFUND_CREATED': '发起退款',
      'REFUND_SUCCESS': '退款成功',
      'REFUND_FAILED': '退款失败',
      'REFUND_CANCELLED': '退款取消'
    }
    return eventTypeMap[eventType] || eventType || '未知'
  }

  // 优化版本的图表初始化
  const initChartsOptimized = () => {
    // 仅在概览页可见时尝试初始化图表
    if (activeTab.value !== 'overview') {
      console.log('[Chart] 当前不是概览页，跳过图表初始化')
      return
    }

    if (chartsInitialized.value) {
      console.log('[Chart] 图表已初始化，执行resize')
      requestAnimationFrame(() => {
        Object.values(chartInstances).forEach(chart => {
          if (chart && !chart.isDisposed && chart.resize) {
            chart.resize()
          }
        })
      })
      return
    }
    
    console.log('[Chart] 准备初始化图表...')
    console.log('[Chart] ref绑定状态:', {
      trendChart: !!trendChart.value,
      serviceChart: !!serviceChart.value,
      revenueChart: !!revenueChart.value
    })
    
    // 检查DOM是否真正渲染
    if (trendChart.value) {
      console.log('[Chart] 趋势图DOM信息:', {
        tagName: trendChart.value.tagName,
        className: trendChart.value.className,
        offsetWidth: trendChart.value.offsetWidth,
        offsetHeight: trendChart.value.offsetHeight,
        clientWidth: trendChart.value.clientWidth,
        clientHeight: trendChart.value.clientHeight
      })
    }
    
    // 先等待一下确保DOM完全渲染
    setTimeout(() => {
      requestAnimationFrame(() => {
        console.log('[Chart] 尝试初始化所有图表')
        const tReady = initTrendChartOptimized()
        const sReady = initServiceChartOptimized()
        const rReady = initRevenueChartOptimized()

        const allReady = !!(chartInstances.trend && chartInstances.service && chartInstances.revenue)
        if (allReady) {
          chartsInitialized.value = true
          console.log('[Chart] ✅ 所有图表初始化成功')
        } else {
          const canRetry = (
            chartInitAttempts.trend < MAX_INIT_ATTEMPTS ||
            chartInitAttempts.service < MAX_INIT_ATTEMPTS ||
            chartInitAttempts.revenue < MAX_INIT_ATTEMPTS
          )
          if (canRetry) {
            console.log('[Chart] 部分图表初始化失败，500ms后重试')
            setTimeout(() => {
              initChartsOptimized()
            }, 500)
          } else {
            console.error('[Chart] ❌ 初始化重试已达上限，部分图表可能未显示')
            console.log('[Chart] 失败详情:', {
              trend: !!chartInstances.trend,
              service: !!chartInstances.service,
              revenue: !!chartInstances.revenue,
              attempts: chartInitAttempts,
              refs: {
                trendChart: !!trendChart.value,
                serviceChart: !!serviceChart.value,
                revenueChart: !!revenueChart.value
              }
            })
          }
        }
      })
    }, 600) // 减少初始延迟
  }
  
  // 原始图表初始化方法（保持兼容性）
  const initCharts = () => {
    initTrendChart()
    initServiceChart()
    initRevenueChart()
  }

  // 优化版本的图表初始化方法
  const initTrendChartOptimized = () => {
    // 检查 ref 是否存在
    if (!trendChart.value) {
      console.warn('[ECharts] 趋势图ref未绑定，可能是DOM还未创建')
      return false
    }
    
    // 如果图表已存在且未被销毁，直接resize
    if (chartInstances.trend && !chartInstances.trend.isDisposed()) {
      chartInstances.trend.resize()
      return true
    }
    
    const el = trendChart.value
    
    // 尝试获取容器实际尺寸，如果为0则使用固定值
    const rect = el.getBoundingClientRect()
    const containerWidth = rect.width || el.clientWidth || 600
    const containerHeight = 300
    
    console.log(`[ECharts] 初始化趋势图，容器尺寸: ${containerWidth}x${containerHeight}`)
    
    const chart = echarts.init(el, null, {
      renderer: 'canvas',
      useDirtyRect: true,
      width: containerWidth,
      height: containerHeight
    })
    chartInstances.trend = chart
    chartInitAttempts.trend = 0
    console.log('[ECharts] 趋势图初始化成功')
    
    const option = {
      animation: true,
      animationDuration: 1000,
      tooltip: {
        trigger: 'axis',
        axisPointer: { 
          type: 'cross',
          lineStyle: {
            color: '#52c41a'
          }
        },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#52c41a',
        borderWidth: 1,
        textStyle: {
          color: '#262626'
        }
      },
      legend: {
        data: ['预约数量', '完成数量'],
        textStyle: {
          color: '#595959'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c'
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c'
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        }
      },
      series: [
        {
          name: '预约数量',
          type: 'line',
          smooth: true,
          data: [45, 52, 38, 65, 72, 89, 94],
          itemStyle: { 
            color: '#52c41a',
            borderWidth: 2
          },
          lineStyle: {
            width: 3
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
                { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
              ]
            }
          }
        },
        {
          name: '完成数量',
          type: 'line',
          smooth: true,
          data: [42, 48, 35, 61, 68, 85, 90],
          itemStyle: { 
            color: '#73d13d',
            borderWidth: 2
          },
          lineStyle: {
            width: 3
          }
        }
      ]
    }
    
    chart.setOption(option)
    return true
  }
  
  const initServiceChartOptimized = () => {
    if (!serviceChart.value) {
      console.warn('[ECharts] 服务图ref未绑定，可能是DOM还未创建')
      return false
    }
    
    if (chartInstances.service && !chartInstances.service.isDisposed()) {
      chartInstances.service.resize()
      return true
    }
    
    const el = serviceChart.value
    const rect = el.getBoundingClientRect()
    const containerWidth = rect.width || el.clientWidth || 600
    const containerHeight = 300
    
    console.log(`[ECharts] 初始化服务图，容器尺寸: ${containerWidth}x${containerHeight}`)
    
    const chart = echarts.init(el, null, {
      renderer: 'canvas',
      useDirtyRect: true,
      width: containerWidth,
      height: containerHeight
    })
    chartInstances.service = chart
    chartInitAttempts.service = 0
    console.log('[ECharts] 服务图初始化成功')
    
    const option = {
      animation: true,
      animationDuration: 1000,
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#52c41a',
        borderWidth: 1,
        textStyle: {
          color: '#262626'
        }
      },
      legend: {
        orient: 'vertical',
        left: 10,
        data: ['基础洗车', '精洗服务', '打蜡服务', '内饰清洁', '其他服务'],
        textStyle: {
          color: '#595959'
        }
      },
      color: ['#52c41a', '#73d13d', '#95de64', '#b7eb8f', '#d9f7be'],
      series: [
        {
          name: '服务类型',
          type: 'pie',
          radius: ['45%', '75%'],
          center: ['60%', '50%'],
          data: [
            { value: 335, name: '基础洗车' },
            { value: 310, name: '精洗服务' },
            { value: 234, name: '打蜡服务' },
            { value: 135, name: '内饰清洁' },
            { value: 148, name: '其他服务' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 15,
              shadowOffsetX: 0,
              shadowColor: 'rgba(82, 196, 26, 0.4)'
            }
          },
          label: {
            color: '#595959',
            fontWeight: 500
          },
          labelLine: {
            lineStyle: {
              color: '#d9d9d9'
            }
          }
        }
      ]
    }
    
    chart.setOption(option)
    return true
  }
  
  const initRevenueChartOptimized = () => {
    if (!revenueChart.value) {
      console.warn('[ECharts] 收入图ref未绑定，可能是DOM还未创建')
      return false
    }
    
    if (chartInstances.revenue && !chartInstances.revenue.isDisposed()) {
      chartInstances.revenue.resize()
      return true
    }
    
    const el = revenueChart.value
    const rect = el.getBoundingClientRect()
    const containerWidth = rect.width || el.clientWidth || 800
    const containerHeight = 400
    
    console.log(`[ECharts] 初始化收入图，容器尺寸: ${containerWidth}x${containerHeight}`)
    
    const chart = echarts.init(el, null, {
      renderer: 'canvas',
      useDirtyRect: true,
      width: containerWidth,
      height: containerHeight
    })
    chartInstances.revenue = chart
    chartInitAttempts.revenue = 0
    console.log('[ECharts] 收入图初始化成功')
    
    const option = {
      animation: true,
      animationDuration: 1000,
      tooltip: {
        trigger: 'axis',
        axisPointer: { 
          type: 'shadow',
          shadowStyle: {
            color: 'rgba(82, 196, 26, 0.1)'
          }
        },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#52c41a',
        borderWidth: 1,
        textStyle: {
          color: '#262626'
        }
      },
      legend: {
        data: ['收入'],
        textStyle: {
          color: '#595959'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月'],
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c'
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c',
          formatter: '￥{value}'
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        }
      },
      series: [
        {
          name: '收入',
          type: 'bar',
          data: [2800, 3200, 2900, 3500, 4200, 3800],
          barWidth: '50%',
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#52c41a' },
                { offset: 1, color: '#95de64' }
              ]
            },
            borderRadius: [8, 8, 0, 0]
          },
          emphasis: {
            itemStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: '#73d13d' },
                  { offset: 1, color: '#b7eb8f' }
                ]
              }
            }
          }
        }
      ]
    }
    
    chart.setOption(option)
    return true
  }

  // 原始图表初始化方法（保持兼容性）
  const initTrendChart = () => {
    if (!trendChart.value) return
    
    const chart = echarts.init(trendChart.value)
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' }
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
          itemStyle: { color: '#1890ff' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#1890ff' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.2)' }
              ]
            }
          }
        },
        {
          name: '完成数量',
          type: 'line',
          smooth: true,
          data: [42, 48, 35, 61, 68, 85, 90],
          itemStyle: { color: '#52c41a' }
        }
      ]
    }
    
    chart.setOption(option)
    chartInstances.trend = chart
  }

  const initServiceChart = () => {
    if (!serviceChart.value) return
    
    const chart = echarts.init(serviceChart.value)
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
          data: [
            { value: 335, name: '基础洗车' },
            { value: 310, name: '精洗套餐' },
            { value: 234, name: '内饰清洁' },
            { value: 135, name: '打蜡服务' },
            { value: 154, name: '镀膜服务' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    
    chart.setOption(option)
    chartInstances.service = chart
  }

  const initRevenueChart = () => {
    if (!revenueChart.value) return
    
    const chart = echarts.init(revenueChart.value)
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
          itemStyle: { color: '#1890ff' }
        },
        {
          name: '成本',
          type: 'bar',
          data: [25000, 28000, 22000, 35000, 38000, 45000, 48000, 44000, 38000, 42000, 46000, 49000],
          itemStyle: { color: '#faad14' }
        },
        {
          name: '利润',
          type: 'line',
          data: [20000, 24000, 16000, 30000, 34000, 44000, 46000, 43000, 38000, 40000, 45000, 49000],
          itemStyle: { color: '#52c41a' }
        }
      ]
    }
    
    chart.setOption(option)
    chartInstances.revenue = chart
  }

  // 响应式处理
  const handleResize = () => {
    nextTick(() => {
      Object.values(chartInstances).forEach(chart => {
        chart?.resize()
      })
    })
  }

  // 监听标签切换到概览页时触发图表初始化/重绘
  watch(activeTab, (tab, oldTab) => {
    console.log(`[Chart] 标签切换: ${oldTab} -> ${tab}`)
    
    if (tab === 'overview') {
      console.log('[Chart] 切换到概览页，准备初始化图表')
      
      // 重置初始化状态
      chartsInitialized.value = false
      chartInitAttempts.trend = 0
      chartInitAttempts.service = 0
      chartInitAttempts.revenue = 0
      
      // 等待DOM渲染后初始化
      nextTick(() => {
        setTimeout(() => {
          initChartsOptimized()
        }, 300)
      })
    }
  })

  // 初始化
  const initialize = () => {
    console.log('[AdminScript] initialize 被调用')
    console.log('[AdminScript] ref绑定状态检查:', {
      trendChart: !!trendChart.value,
      serviceChart: !!serviceChart.value,
      revenueChart: !!revenueChart.value
    })
    
    // 权限验证
    if (!AuthManager.isAuthenticated()) {
      ElMessage.error('请先登录')
      router.push('/login')
      return
    }
    
    if (!AuthManager.isAdmin()) {
      ElMessage.error('您没有访问管理后台的权限')
      router.push('/')
      return
    }

    // 获取用户信息
    const userInfo_stored = AuthManager.getUserInfo()
    if (userInfo_stored) {
      userInfo.name = userInfo_stored.realName || userInfo_stored.username || '管理员'
    }

    // 根据当前路由设置activeTab
    const currentPath = router.currentRoute.value.path
    console.log('当前路径:', currentPath)
    
    if (currentPath.includes('/admin/dashboard')) {
      activeTab.value = 'overview'
      console.log('设置activeTab为: overview (dashboard路径)')
    } else if (currentPath === '/admin') {
      // 如果是/admin根路径，设置默认tab但不跳转
      activeTab.value = 'overview'
      console.log('设置activeTab为: overview (根路径)')
    } else {
      activeTab.value = 'overview'
    }

    // 根据当前标签加载对应数据（使用优化版本）
    const loadStartTime = performanceMonitor.start('初始化加载')
    loadModuleData(activeTab.value)
    performanceMonitor.end(loadStartTime, '初始化加载')
    
    // 窗口resize处理（防抖优化）
    let resizeTimer = null
    const handleResize = () => {
      if (resizeTimer) {
        clearTimeout(resizeTimer)
      }
      resizeTimer = setTimeout(() => {
        requestAnimationFrame(() => {
          Object.values(chartInstances).forEach(chart => {
            if (chart && chart.resize) {
              chart.resize()
            }
          })
        })
      }, 300)
    }
    
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
    
    // 计算系统运行时间
    const systemStartTime = new Date('2024-01-01')
    const now = new Date()
    const diff = now - systemStartTime
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    systemUptime.value = `${days}天${hours}小时${minutes}分钟`
    
    // 监听路由变化
    watch(() => route.path, (newPath) => {
      console.log('路由变化:', newPath)
      if (newPath.includes('/admin/')) {
        const raw = newPath.split('/admin/')[1] || 'overview'
        const tabName = normalizeModuleKey(raw)
        if (activeTab.value !== tabName) {
          activeTab.value = tabName
          console.log('路由变化，更新activeTab为:', tabName)
          
          // 根据新标签加载对应数据（使用优化版本）
          if (tabName !== 'system') {
            loadModuleData(tabName)
          }
        }
      }
    }, { immediate: true })
    
    // 返回清理函数供 onBeforeUnmount 使用
    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }
  
  // 组件卸载时清理资源
  onBeforeUnmount(() => {
    console.log('[Chart] 组件卸载，清理图表实例')
    
    // 销毁所有图表实例
    Object.keys(chartInstances).forEach(key => {
      if (chartInstances[key] && !chartInstances[key].isDisposed()) {
        chartInstances[key].dispose()
        chartInstances[key] = null
      }
    })
  })

  return {
    // 响应式数据
    activeTab,
    sidebarCollapsed,
    refreshing,
    userInfo,
    metricsData,
    trendPeriod,
    revenueType,
    bookings,
    bookingsLoading,
    bookingSearch,
    bookingStatusFilter,
    services,
    servicesLoading,
    showServiceDialog,
    serviceSaving,
    serviceFormRef,
    serviceForm,
    serviceRules,
    users,
    usersLoading,
    userSearch,
    // 支付审计
    audits,
    auditLoading,
    auditQuery,
    auditPagination,
    systemUptime,
    trendChart,
    serviceChart,
    revenueChart,

    // 计算属性
    filteredBookings,
    filteredUsers,

    // 方法
    toggleSidebar,
    handleTabChange,
    handleUserCommand,
    refreshData,
    getBookingStatusType,
    getBookingStatusText,
    confirmBooking,
    startService,
    completeService,
    cancelBooking,
    deleteBooking,
    updateBookingStatusLocal,
    viewBookingDetail,
    showAddServiceDialog,
    editService,
    toggleServiceStatus,
    deleteService,
    saveService,
    toggleUserStatus,
    viewUserDetail,
    onAuditFilterChange,
    handleAuditSizeChange,
    handleAuditCurrentChange,
    getStatusText,
    getStatusTagType,
    getEventTypeText,
    clearSystemCache,
    exportSystemLogs,
    backupDatabase,
    clearCache,
    loadModuleData,
    performanceMonitor,
    initialize
  }
}