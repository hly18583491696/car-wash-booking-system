<template>
  <div class="services-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">服务项目</h1>
        <p class="page-subtitle">选择适合您的洗车服务套餐</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filters-section">
      <div class="container">
        <div class="filters-container">
          <div class="filter-group">
            <label class="filter-label">服务类型</label>
            <el-select v-model="filters.category" placeholder="全部类型" clearable>
              <el-option label="全部类型" value="" />
              <el-option label="基础洗车" value="basic" />
              <el-option label="精洗套餐" value="premium" />
              <el-option label="豪华套餐" value="luxury" />
              <el-option label="内饰清洁" value="interior" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">价格范围</label>
            <el-select v-model="filters.priceRange" placeholder="全部价格" clearable>
              <el-option label="全部价格" value="" />
              <el-option label="50元以下" value="0-50" />
              <el-option label="50-100元" value="50-100" />
              <el-option label="100-200元" value="100-200" />
              <el-option label="200元以上" value="200+" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">服务时长</label>
            <el-select v-model="filters.duration" placeholder="全部时长" clearable>
              <el-option label="全部时长" value="" />
              <el-option label="30分钟内" value="0-30" />
              <el-option label="30-60分钟" value="30-60" />
              <el-option label="60-90分钟" value="60-90" />
              <el-option label="90分钟以上" value="90+" />
            </el-select>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">排序方式</label>
            <el-select v-model="filters.sortBy" placeholder="默认排序">
              <el-option label="默认排序" value="default" />
              <el-option label="价格从低到高" value="price-asc" />
              <el-option label="价格从高到低" value="price-desc" />
              <el-option label="评分最高" value="rating-desc" />
              <el-option label="最受欢迎" value="popular" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务列表 -->
    <div class="services-section">
      <div class="container">
        <div class="services-grid">
          <div 
            class="service-card" 
            v-for="service in filteredServices" 
            :key="service.id"
            @click="viewServiceDetail(service)"
          >
            <div class="service-image">
              <div class="image-placeholder">
                <el-icon size="60" :color="service.color">
                  <component :is="service.icon" />
                </el-icon>
              </div>
              <div class="service-badges">
                <span v-if="service.popular" class="badge popular">热门</span>
                <span v-if="service.recommended" class="badge recommended">推荐</span>
                <span v-if="service.discount" class="badge discount">{{ service.discount }}折</span>
              </div>
            </div>
            
            <div class="service-content">
              <div class="service-header">
                <h3 class="service-title">{{ service.name }}</h3>
                <div class="service-rating">
                  <el-rate 
                    v-model="service.rating" 
                    disabled 
                    show-score 
                    text-color="#ff9900"
                    score-template="{value}"
                    size="small"
                  />
                </div>
              </div>
              
              <p class="service-description">{{ service.description }}</p>
              
              <div class="service-features">
                <div class="feature-item" v-for="feature in service.features" :key="feature">
                  <el-icon size="14" color="var(--success-color)"><Check /></el-icon>
                  <span>{{ feature }}</span>
                </div>
              </div>
              
              <div class="service-info">
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ service.duration }}</span>
                </div>
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>已服务 {{ service.serviceCount }}+</span>
                </div>
              </div>
              
              <div class="service-footer">
                <div class="service-price">
                  <span class="current-price">¥{{ service.price }}</span>
                  <span v-if="service.originalPrice" class="original-price">¥{{ service.originalPrice }}</span>
                </div>
                <el-button 
                  type="primary" 
                  size="default"
                  @click.stop="bookService(service)"
                >
                  立即预约
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="filteredServices.length === 0" class="empty-state">
          <el-empty description="没有找到符合条件的服务">
            <el-button type="primary" @click="clearFilters">清除筛选条件</el-button>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Services',
  setup() {
    const router = useRouter()
    
    // 筛选条件
    const filters = ref({
      category: '',
      priceRange: '',
      duration: '',
      sortBy: 'default'
    })
    
    // 服务数据
    const services = ref([
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗，轮胎清洁，玻璃清洁，适合日常维护',
        price: 30,
        originalPrice: null,
        duration: '30分钟',
        category: 'basic',
        rating: 4.2,
        serviceCount: 1200,
        features: ['外观清洗', '轮胎清洁', '玻璃清洁', '简单内饰'],
        icon: 'Car',
        color: 'var(--primary-color)',
        popular: false,
        recommended: false,
        discount: null
      },
      {
        id: 2,
        name: '精洗套餐',
        description: '深度清洁，内外兼修，包含基础洗车所有项目',
        price: 68,
        originalPrice: 88,
        duration: '60分钟',
        category: 'premium',
        rating: 4.6,
        serviceCount: 2800,
        features: ['深度清洗', '内饰清洁', '轮毂清洁', '玻璃镀膜', '轮胎护理'],
        icon: 'Star',
        color: 'var(--warning-color)',
        popular: true,
        recommended: true,
        discount: '7.7'
      },
      {
        id: 3,
        name: '豪华套餐',
        description: '全方位护理，焕然一新，适合重要场合前使用',
        price: 128,
        originalPrice: 168,
        duration: '90分钟',
        category: 'luxury',
        rating: 4.8,
        serviceCount: 950,
        features: ['精洗服务', '打蜡护理', '内饰深度清洁', '轮胎护理', '香氛服务', '细节处理'],
        icon: 'Trophy',
        color: 'var(--error-color)',
        popular: false,
        recommended: true,
        discount: '7.6'
      },
      {
        id: 4,
        name: '内饰深度清洁',
        description: '专业内饰清洁，除菌除味，营造健康车内环境',
        price: 88,
        originalPrice: null,
        duration: '45分钟',
        category: 'interior',
        rating: 4.4,
        serviceCount: 680,
        features: ['座椅清洁', '地毯清洁', '仪表台清洁', '除菌除味', '空调清洁'],
        icon: 'House',
        color: 'var(--info-color)',
        popular: false,
        recommended: false,
        discount: null
      },
      {
        id: 5,
        name: '快速洗车',
        description: '15分钟快速清洗，适合时间紧急的情况',
        price: 20,
        originalPrice: null,
        duration: '15分钟',
        category: 'basic',
        rating: 4.0,
        serviceCount: 1500,
        features: ['外观冲洗', '简单擦拭', '玻璃清洁'],
        icon: 'Timer',
        color: 'var(--success-color)',
        popular: false,
        recommended: false,
        discount: null
      },
      {
        id: 6,
        name: '打蜡护理',
        description: '专业打蜡服务，保护车漆，增加光泽度',
        price: 158,
        originalPrice: null,
        duration: '120分钟',
        category: 'luxury',
        rating: 4.7,
        serviceCount: 420,
        features: ['车身打蜡', '漆面护理', '光泽增强', '防护涂层'],
        icon: 'Sunny',
        color: 'var(--warning-color)',
        popular: false,
        recommended: true,
        discount: null
      }
    ])
    
    // 筛选后的服务
    const filteredServices = computed(() => {
      let result = [...services.value]
      
      // 按类型筛选
      if (filters.value.category) {
        result = result.filter(service => service.category === filters.value.category)
      }
      
      // 按价格筛选
      if (filters.value.priceRange) {
        const [min, max] = filters.value.priceRange.split('-').map(v => v === '+' ? Infinity : parseInt(v))
        result = result.filter(service => {
          if (max === undefined) return service.price >= min
          return service.price >= min && service.price <= max
        })
      }
      
      // 按时长筛选
      if (filters.value.duration) {
        const [min, max] = filters.value.duration.split('-').map(v => v === '+' ? Infinity : parseInt(v))
        result = result.filter(service => {
          const duration = parseInt(service.duration)
          if (max === undefined) return duration >= min
          return duration >= min && duration <= max
        })
      }
      
      // 排序
      switch (filters.value.sortBy) {
        case 'price-asc':
          result.sort((a, b) => a.price - b.price)
          break
        case 'price-desc':
          result.sort((a, b) => b.price - a.price)
          break
        case 'rating-desc':
          result.sort((a, b) => b.rating - a.rating)
          break
        case 'popular':
          result.sort((a, b) => b.serviceCount - a.serviceCount)
          break
        default:
          // 默认排序：推荐 > 热门 > 评分
          result.sort((a, b) => {
            if (a.recommended !== b.recommended) return b.recommended - a.recommended
            if (a.popular !== b.popular) return b.popular - a.popular
            return b.rating - a.rating
          })
      }
      
      return result
    })
    
    // 查看服务详情
    const viewServiceDetail = (service) => {
      router.push({
        path: '/service-detail',
        query: { id: service.id }
      })
    }
    
    // 预约服务
    const bookService = (service) => {
      router.push({
        path: '/appointment',
        query: { serviceId: service.id }
      })
    }
    
    // 清除筛选条件
    const clearFilters = () => {
      filters.value = {
        category: '',
        priceRange: '',
        duration: '',
        sortBy: 'default'
      }
    }
    
    onMounted(() => {
      // 可以在这里加载服务数据
    })
    
    return {
      filters,
      services,
      filteredServices,
      viewServiceDetail,
      bookService,
      clearFilters
    }
  }
}
</script>

<style scoped>
.services-page {
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

/* 筛选区域 */
.filters-section {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 24px 0;
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
  min-width: 150px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

/* 服务列表 */
.services-section {
  padding: 40px 0;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.service-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
  cursor: pointer;
}

.service-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.service-image {
  height: 140px;
  background: var(--bg-light);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.service-badges {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.badge {
  padding: 4px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-white);
}

.badge.popular {
  background: var(--error-color);
}

.badge.recommended {
  background: var(--warning-color);
}

.badge.discount {
  background: var(--success-color);
}

.service-content {
  padding: 20px;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.service-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.service-rating {
  flex-shrink: 0;
}

.service-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 16px;
  font-size: 14px;
}

.service-features {
  margin-bottom: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.service-info {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-light);
}

.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--primary-color);
}

.original-price {
  font-size: 1rem;
  color: var(--text-light);
  text-decoration: line-through;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 40px 0;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .filters-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .filter-group {
    min-width: 100%;
  }
  
  .services-grid {
    grid-template-columns: 1fr;
  }
  
  .service-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .service-content {
    padding: 16px;
  }
  
  .service-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .service-footer .el-button {
    width: 100%;
  }
}
</style>