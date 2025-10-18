<template>
  <div class="stores-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">洗车店查询</h1>
        <p class="page-subtitle">找到您附近的专业洗车服务店</p>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <div class="container">
        <div class="search-container">
          <div class="search-input-group">
            <el-input
              v-model="searchQuery"
              placeholder="搜索洗车店名称或地址"
              size="large"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="large" @click="getCurrentLocation">
              <el-icon><Location /></el-icon>
              定位附近
            </el-button>
          </div>
          
          <div class="filters-container">
            <div class="filter-group">
              <label class="filter-label">营业状态</label>
              <el-select v-model="filters.status" placeholder="全部状态" clearable>
                <el-option label="全部状态" value="" />
                <el-option label="营业中" value="open" />
                <el-option label="即将关闭" value="closing" />
                <el-option label="已关闭" value="closed" />
              </el-select>
            </div>
            
            <div class="filter-group">
              <label class="filter-label">服务类型</label>
              <el-select v-model="filters.serviceType" placeholder="全部服务" clearable>
                <el-option label="全部服务" value="" />
                <el-option label="基础洗车" value="basic" />
                <el-option label="精洗服务" value="premium" />
                <el-option label="豪华套餐" value="luxury" />
                <el-option label="内饰清洁" value="interior" />
              </el-select>
            </div>
            
            <div class="filter-group">
              <label class="filter-label">距离范围</label>
              <el-select v-model="filters.distance" placeholder="全部距离" clearable>
                <el-option label="全部距离" value="" />
                <el-option label="1公里内" value="1" />
                <el-option label="3公里内" value="3" />
                <el-option label="5公里内" value="5" />
                <el-option label="10公里内" value="10" />
              </el-select>
            </div>
            
            <div class="filter-group">
              <label class="filter-label">排序方式</label>
              <el-select v-model="filters.sortBy" placeholder="默认排序">
                <el-option label="距离最近" value="distance" />
                <el-option label="评分最高" value="rating" />
                <el-option label="价格最低" value="price" />
                <el-option label="最新开业" value="newest" />
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="container">
        <div class="content-layout">
          <!-- 地图区域 -->
          <div class="map-section">
            <div class="map-container">
              <div class="map-placeholder">
                <el-icon size="60" color="var(--text-light)"><Location /></el-icon>
                <p>地图加载中...</p>
                <p class="map-note">实际项目中可集成百度地图、高德地图等</p>
              </div>
              
              <!-- 地图控制按钮 -->
              <div class="map-controls">
                <el-button circle @click="zoomIn">
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button circle @click="zoomOut">
                  <el-icon><Minus /></el-icon>
                </el-button>
                <el-button circle @click="resetView">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 店铺列表区域 -->
          <div class="stores-section">
            <div class="section-header">
              <h3>找到 {{ filteredStores.length }} 家洗车店</h3>
              <div class="view-toggle">
                <el-button-group>
                  <el-button 
                    :type="viewMode === 'list' ? 'primary' : ''" 
                    @click="viewMode = 'list'"
                  >
                    <el-icon><List /></el-icon>
                    列表
                  </el-button>
                  <el-button 
                    :type="viewMode === 'grid' ? 'primary' : ''" 
                    @click="viewMode = 'grid'"
                  >
                    <el-icon><Grid /></el-icon>
                    网格
                  </el-button>
                </el-button-group>
              </div>
            </div>

            <!-- 店铺列表 -->
            <div class="stores-list" :class="viewMode">
              <div 
                class="store-card" 
                v-for="store in filteredStores" 
                :key="store.id"
                @click="viewStoreDetail(store)"
              >
                <!-- 店铺图片 -->
                <div class="store-image">
                  <img :src="store.image" :alt="store.name" />
                  <div class="store-status" :class="store.status">
                    {{ getStatusText(store.status) }}
                  </div>
                  <div class="store-badges">
                    <span v-if="store.recommended" class="badge recommended">推荐</span>
                    <span v-if="store.discount" class="badge discount">{{ store.discount }}折</span>
                  </div>
                </div>

                <!-- 店铺信息 -->
                <div class="store-content">
                  <div class="store-header">
                    <h3 class="store-name">{{ store.name }}</h3>
                    <div class="store-rating">
                      <el-rate 
                        v-model="store.rating" 
                        disabled 
                        show-score 
                        text-color="#ff9900"
                        score-template="{value}"
                        size="small"
                      />
                      <span class="review-count">({{ store.reviewCount }}条评价)</span>
                    </div>
                  </div>

                  <div class="store-info">
                    <div class="info-item">
                      <el-icon><Location /></el-icon>
                      <span>{{ store.address }}</span>
                      <span class="distance">{{ store.distance }}km</span>
                    </div>
                    <div class="info-item">
                      <el-icon><Clock /></el-icon>
                      <span>{{ store.businessHours }}</span>
                    </div>
                    <div class="info-item">
                      <el-icon><Phone /></el-icon>
                      <span>{{ store.phone }}</span>
                    </div>
                  </div>

                  <div class="store-services">
                    <span 
                      v-for="service in store.services" 
                      :key="service"
                      class="service-tag"
                    >
                      {{ service }}
                    </span>
                  </div>

                  <div class="store-footer">
                    <div class="price-range">
                      <span class="price-label">价格区间：</span>
                      <span class="price-value">¥{{ store.priceRange.min }}-{{ store.priceRange.max }}</span>
                    </div>
                    <div class="store-actions">
                      <el-button size="small" @click.stop="callStore(store)">
                        <el-icon><Phone /></el-icon>
                        电话
                      </el-button>
                      <el-button size="small" @click.stop="navigateToStore(store)">
                        <el-icon><Guide /></el-icon>
                        导航
                      </el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click.stop="bookAtStore(store)"
                      >
                        <el-icon><Calendar /></el-icon>
                        预约
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredStores.length === 0" class="empty-state">
              <el-empty description="没有找到符合条件的洗车店">
                <el-button type="primary" @click="clearFilters">清除筛选条件</el-button>
              </el-empty>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 店铺详情弹窗 -->
    <el-dialog
      v-model="showStoreDetail"
      :title="selectedStore?.name"
      width="800px"
      class="store-detail-dialog"
    >
      <div v-if="selectedStore" class="store-detail">
        <div class="detail-images">
          <img :src="selectedStore.image" :alt="selectedStore.name" />
        </div>
        
        <div class="detail-info">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">地址：</span>
                <span>{{ selectedStore.address }}</span>
              </div>
              <div class="info-item">
                <span class="label">电话：</span>
                <span>{{ selectedStore.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">营业时间：</span>
                <span>{{ selectedStore.businessHours }}</span>
              </div>
              <div class="info-item">
                <span class="label">距离：</span>
                <span>{{ selectedStore.distance }}km</span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h4>服务项目</h4>
            <div class="services-grid">
              <div 
                v-for="service in selectedStore.serviceDetails" 
                :key="service.name"
                class="service-item"
              >
                <span class="service-name">{{ service.name }}</span>
                <span class="service-price">¥{{ service.price }}</span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h4>店铺介绍</h4>
            <p class="store-description">{{ selectedStore.description }}</p>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showStoreDetail = false">关闭</el-button>
          <el-button @click="callStore(selectedStore)">
            <el-icon><Phone /></el-icon>
            拨打电话
          </el-button>
          <el-button @click="navigateToStore(selectedStore)">
            <el-icon><Guide /></el-icon>
            地图导航
          </el-button>
          <el-button type="primary" @click="bookAtStore(selectedStore)">
            <el-icon><Calendar /></el-icon>
            立即预约
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Stores',
  setup() {
    const router = useRouter()
    
    // 搜索和筛选
    const searchQuery = ref('')
    const filters = ref({
      status: '',
      serviceType: '',
      distance: '',
      sortBy: 'distance'
    })
    
    // 视图模式
    const viewMode = ref('list')
    
    // 店铺详情弹窗
    const showStoreDetail = ref(false)
    const selectedStore = ref(null)
    
    // 模拟店铺数据
    const stores = ref([
      {
        id: 1,
        name: '星洁汽车美容中心',
        address: '北京市朝阳区建国路88号',
        phone: '010-12345678',
        businessHours: '08:00-20:00',
        distance: 0.8,
        rating: 4.8,
        reviewCount: 256,
        status: 'open',
        image: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400',
        services: ['基础洗车', '精洗服务', '内饰清洁'],
        priceRange: { min: 30, max: 150 },
        recommended: true,
        discount: null,
        description: '专业的汽车美容服务，拥有10年经验的技师团队，使用进口清洁用品，为您的爱车提供最优质的服务。',
        serviceDetails: [
          { name: '基础洗车', price: 30 },
          { name: '精洗服务', price: 68 },
          { name: '内饰清洁', price: 88 },
          { name: '打蜡护理', price: 128 }
        ]
      },
      {
        id: 2,
        name: '快洁洗车店',
        address: '北京市海淀区中关村大街123号',
        phone: '010-87654321',
        businessHours: '07:00-22:00',
        distance: 1.2,
        rating: 4.5,
        reviewCount: 189,
        status: 'open',
        image: 'https://images.unsplash.com/photo-1486754735734-325b5831c3ad?w=400',
        services: ['快速洗车', '基础洗车', '轮胎护理'],
        priceRange: { min: 20, max: 80 },
        recommended: false,
        discount: '8.5',
        description: '快速便捷的洗车服务，15分钟快洗，适合忙碌的都市人群。',
        serviceDetails: [
          { name: '快速洗车', price: 20 },
          { name: '基础洗车', price: 35 },
          { name: '轮胎护理', price: 50 }
        ]
      },
      {
        id: 3,
        name: '豪车专业护理',
        address: '北京市西城区金融街45号',
        phone: '010-11223344',
        businessHours: '09:00-18:00',
        distance: 2.1,
        rating: 4.9,
        reviewCount: 98,
        status: 'closing',
        image: 'https://images.unsplash.com/photo-1520340356584-f9917d1eea6f?w=400',
        services: ['豪华套餐', '漆面护理', '内饰深度清洁'],
        priceRange: { min: 100, max: 500 },
        recommended: true,
        discount: null,
        description: '专注高端车型护理，提供个性化定制服务，让您的爱车焕然一新。',
        serviceDetails: [
          { name: '豪华套餐', price: 288 },
          { name: '漆面护理', price: 388 },
          { name: '内饰深度清洁', price: 198 }
        ]
      },
      {
        id: 4,
        name: '24小时自助洗车',
        address: '北京市丰台区南三环西路66号',
        phone: '010-99887766',
        businessHours: '24小时营业',
        distance: 3.5,
        rating: 4.2,
        reviewCount: 445,
        status: 'open',
        image: 'https://images.unsplash.com/photo-1607860108855-64acf2078ed9?w=400',
        services: ['自助洗车', '基础洗车', '吸尘服务'],
        priceRange: { min: 15, max: 60 },
        recommended: false,
        discount: '7.5',
        description: '24小时自助洗车服务，方便快捷，价格实惠。',
        serviceDetails: [
          { name: '自助洗车', price: 15 },
          { name: '基础洗车', price: 25 },
          { name: '吸尘服务', price: 10 }
        ]
      },
      {
        id: 5,
        name: '绿色环保洗车',
        address: '北京市昌平区回龙观东大街88号',
        phone: '010-55443322',
        businessHours: '08:30-19:30',
        distance: 4.8,
        rating: 4.6,
        reviewCount: 167,
        status: 'closed',
        image: 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=400',
        services: ['环保洗车', '蒸汽清洁', '无水洗车'],
        priceRange: { min: 40, max: 120 },
        recommended: false,
        discount: null,
        description: '采用环保清洁技术，无污染排放，保护环境的同时为您的爱车提供优质服务。',
        serviceDetails: [
          { name: '环保洗车', price: 45 },
          { name: '蒸汽清洁', price: 78 },
          { name: '无水洗车', price: 55 }
        ]
      }
    ])
    
    // 筛选后的店铺
    const filteredStores = computed(() => {
      let result = [...stores.value]
      
      // 搜索筛选
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(store => 
          store.name.toLowerCase().includes(query) ||
          store.address.toLowerCase().includes(query)
        )
      }
      
      // 营业状态筛选
      if (filters.value.status) {
        result = result.filter(store => store.status === filters.value.status)
      }
      
      // 服务类型筛选
      if (filters.value.serviceType) {
        const serviceMap = {
          'basic': '基础洗车',
          'premium': '精洗服务',
          'luxury': '豪华套餐',
          'interior': '内饰清洁'
        }
        const serviceName = serviceMap[filters.value.serviceType]
        result = result.filter(store => 
          store.services.includes(serviceName)
        )
      }
      
      // 距离筛选
      if (filters.value.distance) {
        const maxDistance = parseInt(filters.value.distance)
        result = result.filter(store => store.distance <= maxDistance)
      }
      
      // 排序
      switch (filters.value.sortBy) {
        case 'distance':
          result.sort((a, b) => a.distance - b.distance)
          break
        case 'rating':
          result.sort((a, b) => b.rating - a.rating)
          break
        case 'price':
          result.sort((a, b) => a.priceRange.min - b.priceRange.min)
          break
        case 'newest':
          // 模拟按开业时间排序
          result.sort((a, b) => b.id - a.id)
          break
        default:
          result.sort((a, b) => a.distance - b.distance)
      }
      
      return result
    })
    
    // 搜索处理
    const handleSearch = () => {
      // 实际项目中可以添加防抖处理
    }
    
    // 获取当前位置
    const getCurrentLocation = () => {
      ElMessage.info('正在获取您的位置...')
      // 实际项目中集成地理定位API
      setTimeout(() => {
        ElMessage.success('定位成功！已为您显示附近的洗车店')
      }, 1000)
    }
    
    // 地图控制
    const zoomIn = () => {
      ElMessage.info('地图放大')
    }
    
    const zoomOut = () => {
      ElMessage.info('地图缩小')
    }
    
    const resetView = () => {
      ElMessage.info('重置地图视图')
    }
    
    // 获取营业状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'open': '营业中',
        'closing': '即将关闭',
        'closed': '已关闭'
      }
      return statusMap[status] || '未知'
    }
    
    // 查看店铺详情
    const viewStoreDetail = (store) => {
      selectedStore.value = store
      showStoreDetail.value = true
    }
    
    // 拨打电话
    const callStore = (store) => {
      ElMessageBox.confirm(
        `确定要拨打 ${store.name} 的电话 ${store.phone} 吗？`,
        '拨打电话',
        {
          confirmButtonText: '拨打',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        ElMessage.success('正在为您拨打电话...')
        // 实际项目中可以调用系统拨号功能
      }).catch(() => {
        // 用户取消
      })
    }
    
    // 导航到店铺
    const navigateToStore = (store) => {
      ElMessage.info(`正在为您导航到 ${store.name}`)
      // 实际项目中集成地图导航功能
    }
    
    // 在店铺预约
    const bookAtStore = (store) => {
      router.push({
        path: '/appointment',
        query: { storeId: store.id }
      })
    }
    
    // 清除筛选条件
    const clearFilters = () => {
      searchQuery.value = ''
      filters.value = {
        status: '',
        serviceType: '',
        distance: '',
        sortBy: 'distance'
      }
    }
    
    onMounted(() => {
      // 页面加载时可以获取用户位置
    })
    
    return {
      searchQuery,
      filters,
      viewMode,
      showStoreDetail,
      selectedStore,
      stores,
      filteredStores,
      handleSearch,
      getCurrentLocation,
      zoomIn,
      zoomOut,
      resetView,
      getStatusText,
      viewStoreDetail,
      callStore,
      navigateToStore,
      bookAtStore,
      clearFilters
    }
  }
}
</script>

<style scoped>
.stores-page {
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

/* 搜索区域 */
.search-section {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 24px 0;
}

.search-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input-group .el-input {
  flex: 1;
  max-width: 400px;
}

.filters-container {
  display: flex;
  gap: 24px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

/* 主要内容 */
.main-content {
  padding: 30px 0;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  min-height: 600px;
}

/* 地图区域 */
.map-section {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.map-container {
  position: relative;
  height: 500px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.map-placeholder {
  height: 100%;
  background: var(--bg-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-light);
}

.map-note {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.7;
}

.map-controls {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 店铺列表区域 */
.stores-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

/* 店铺列表 */
.stores-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stores-list.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.store-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
  cursor: pointer;
  display: flex;
  gap: 16px;
}

.stores-list.grid .store-card {
  flex-direction: column;
  gap: 0;
}

.store-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.store-image {
  width: 200px;
  height: 150px;
  position: relative;
  flex-shrink: 0;
}

.stores-list.grid .store-image {
  width: 100%;
  height: 180px;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-status {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-white);
}

.store-status.open {
  background: var(--success-color);
}

.store-status.closing {
  background: var(--warning-color);
}

.store-status.closed {
  background: var(--error-color);
}

.store-badges {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.badge {
  padding: 4px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-white);
}

.badge.recommended {
  background: var(--primary-color);
}

.badge.discount {
  background: var(--error-color);
}

.store-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stores-list.grid .store-content {
  padding: 20px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.store-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.store-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.review-count {
  font-size: 12px;
  color: var(--text-light);
}

.store-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.distance {
  margin-left: auto;
  color: var(--primary-color);
  font-weight: 600;
}

.store-services {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.service-tag {
  background: var(--primary-light);
  color: var(--primary-color);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
}

.store-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

.price-range {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-label {
  font-size: 12px;
  color: var(--text-light);
}

.price-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

.store-actions {
  display: flex;
  gap: 8px;
}

/* 店铺详情弹窗 */
.store-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-images img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.info-section {
  margin-bottom: 20px;
}

.info-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-grid .info-item {
  display: flex;
  gap: 8px;
}

.info-grid .label {
  font-weight: 600;
  color: var(--text-secondary);
  min-width: 80px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--bg-light);
  border-radius: var(--radius-sm);
}

.service-name {
  font-weight: 500;
  color: var(--text-primary);
}

.service-price {
  font-weight: 600;
  color: var(--primary-color);
}

.store-description {
  line-height: 1.6;
  color: var(--text-secondary);
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .map-section {
    position: static;
  }
  
  .map-container {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 40px 0;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .search-input-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filters-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-group {
    min-width: 100%;
  }
  
  .store-card {
    flex-direction: column;
    gap: 0;
  }
  
  .store-image {
    width: 100%;
    height: 160px;
  }
  
  .store-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .store-actions {
    justify-content: space-between;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .store-content {
    padding: 12px;
  }
  
  .store-actions .el-button {
    flex: 1;
  }
}
</style>