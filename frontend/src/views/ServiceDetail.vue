<template>
  <div class="service-detail-page">
    <!-- 返回导航 -->
    <div class="back-nav">
      <div class="container">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回服务列表
        </el-button>
      </div>
    </div>

    <!-- 服务详情内容 -->
    <div class="detail-content" v-if="service">
      <div class="container">
        <div class="detail-layout">
          <!-- 左侧：服务图片和基本信息 -->
          <div class="detail-left">
            <div class="service-image-section">
              <div class="image-container">
                <img 
                  :src="service.image" 
                  :alt="service.name"
                  class="service-img"
                  @error="(e) => e.target.src = 'https://images.unsplash.com/photo-1605164599901-db0b9283e705?w=400&h=300&fit=crop'"
                />
              </div>
              <div class="service-badges">
                <el-tag v-if="service.popular" type="danger">热门</el-tag>
                <el-tag v-if="service.recommended" type="warning">推荐</el-tag>
                <el-tag v-if="service.discount" type="success">{{ service.discount }}折</el-tag>
              </div>
            </div>

            <!-- 服务评价 -->
            <div class="rating-section">
              <h3>服务评价</h3>
              <div class="rating-info">
                <el-rate v-model="service.rating" disabled show-score text-color="#ff9900" />
                <span v-if="service.serviceCount" class="service-count">{{ service.serviceCount }}+ 次服务</span>
              </div>
            </div>
          </div>

          <!-- 右侧：详细信息 -->
          <div class="detail-right">
            <div class="service-header">
              <h1 class="service-name">{{ service.name }}</h1>
              <div class="service-category">
                <el-tag size="small" effect="plain">{{ getCategoryLabel(service.category) }}</el-tag>
              </div>
            </div>

            <p class="service-description">{{ service.description }}</p>

            <!-- 价格信息 -->
            <div class="price-section">
              <div class="price-info">
                <span class="current-price">¥{{ service.price }}</span>
                <span v-if="service.originalPrice" class="original-price">¥{{ service.originalPrice }}</span>
              </div>
              <div class="duration-info">
                <el-icon><Clock /></el-icon>
                <span>预计耗时 {{ service.duration }}</span>
              </div>
            </div>

            <!-- 服务特点 -->
            <div class="features-section">
              <h3>服务内容</h3>
              <div class="features-grid">
                <div class="feature-item" v-for="feature in service.features" :key="feature">
                  <el-icon color="var(--success-color)"><Check /></el-icon>
                  <span>{{ feature }}</span>
                </div>
              </div>
            </div>

            <!-- 预约按钮 -->
            <div class="action-section">
              <el-button type="primary" size="large" @click="bookService" class="book-btn">
                <el-icon><Calendar /></el-icon>
                立即预约
              </el-button>
              <el-button size="large" @click="goToServices" plain>
                查看其他服务
              </el-button>
            </div>

            <!-- 温馨提示 -->
            <div class="tips-section">
              <h3>温馨提示</h3>
              <ul class="tips-list">
                <li>预约成功后请按时到店，如需更改请提前联系</li>
                <li>服务过程中请配合工作人员，确保服务质量</li>
                <li>如有特殊需求请在预约时备注说明</li>
                <li>支持支付宝在线支付，安全便捷</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div class="loading-state" v-else-if="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 服务不存在 -->
    <div class="not-found" v-else>
      <el-empty description="服务不存在或已下架">
        <el-button type="primary" @click="goToServices">返回服务列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, Check, Calendar } from '@element-plus/icons-vue'
import { serviceApi } from '@/api/service.js'

export default {
  name: 'ServiceDetail',
  components: {
    ArrowLeft,
    Clock,
    Check,
    Calendar
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const service = ref(null)

    // 服务名称关键词到专属图片的映射
    const serviceNameImageMap = {
      '快速': 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop',
      '基础': 'https://images.unsplash.com/photo-1605164599901-db0b9283e705?w=400&h=300&fit=crop',
      '标准': 'https://images.unsplash.com/photo-1552930294-6b595f4c4dc0?w=400&h=300&fit=crop',
      '精洗': 'https://images.unsplash.com/photo-1601362840469-51e4d8d58785?w=400&h=300&fit=crop',
      '豪华': 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=400&h=300&fit=crop',
      '内饰': 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400&h=300&fit=crop',
      '打蜡': 'https://images.unsplash.com/photo-1619405399517-d7fce0f13302?w=400&h=300&fit=crop',
      '美容': 'https://images.unsplash.com/photo-1619405399517-d7fce0f13302?w=400&h=300&fit=crop',
      '养护': 'https://images.unsplash.com/photo-1487754180451-c456f719a1fc?w=400&h=300&fit=crop',
      '保养': 'https://images.unsplash.com/photo-1487754180451-c456f719a1fc?w=400&h=300&fit=crop',
      '清洁': 'https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=400&h=300&fit=crop',
    }

    // 默认图片
    const defaultImage = 'https://images.unsplash.com/photo-1605164599901-db0b9283e705?w=400&h=300&fit=crop'

    // 根据服务名称获取匹配的图片
    const getServiceImage = (serviceName) => {
      if (!serviceName) return defaultImage
      for (const [keyword, imageUrl] of Object.entries(serviceNameImageMap)) {
        if (serviceName.includes(keyword)) {
          return imageUrl
        }
      }
      return defaultImage
    }

    // 分类样式映射
    const categoryStyleMap = {
      basic: { icon: 'Car', color: 'var(--primary-color)' },
      premium: { icon: 'Star', color: 'var(--warning-color)' },
      luxury: { icon: 'Trophy', color: 'var(--error-color)' },
      interior: { icon: 'Brush', color: 'var(--success-color)' },
    }

    const categoryLabels = {
      basic: '基础洗车',
      premium: '精洗套餐',
      luxury: '豪华套餐',
      interior: '内饰清洁'
    }

    const getCategoryLabel = (category) => {
      return categoryLabels[category] || category
    }

    // 从后端API加载服务详情
    const loadService = async () => {
      loading.value = true
      const serviceId = route.query.id
      
      if (!serviceId) {
        service.value = null
        loading.value = false
        return
      }

      try {
        const response = await serviceApi.getServiceById(serviceId)
        console.log('✅ 服务详情加载成功:', response)
        
        if (response && response.data) {
          const data = response.data
          const categoryStyle = categoryStyleMap[data.category] || categoryStyleMap.basic
          
          // 将后端数据转换为前端显示格式
          service.value = {
            id: data.id,
            name: data.name,
            description: data.description || '',
            price: parseFloat(data.price) || 0,
            originalPrice: null,
            duration: data.duration ? `${data.duration}分钟` : '30分钟',
            category: data.category || 'basic',
            rating: 4.5, // 默认评分
            serviceCount: null, // 不显示虚假数据
            features: data.description ? data.description.split('、').slice(0, 6) : [],
            icon: categoryStyle.icon,
            color: categoryStyle.color,
            image: data.imageUrl || getServiceImage(data.name),
            popular: false,
            recommended: data.status === 1,
            discount: null
          }
        } else {
          service.value = null
        }
      } catch (error) {
        console.error('❌ 加载服务详情失败:', error)
        ElMessage.error('加载服务详情失败，请稍后重试')
        service.value = null
      } finally {
        loading.value = false
      }
    }

    const goBack = () => {
      router.back()
    }

    const goToServices = () => {
      router.push('/services')
    }

    const bookService = () => {
      if (!service.value) return
      router.push({
        path: '/appointment',
        query: { serviceId: service.value.id }
      })
    }

    onMounted(() => {
      loadService()
    })

    return {
      loading,
      service,
      getCategoryLabel,
      goBack,
      goToServices,
      bookService
    }
  }
}
</script>

<style scoped>
.service-detail-page {
  min-height: 100vh;
  background: var(--bg-color);
}

.back-nav {
  background: var(--card-bg);
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.detail-content {
  padding: 40px 0;
}

.detail-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 40px;
}

.detail-left {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.service-image-section {
  background: var(--card-bg);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.image-container {
  width: 100%;
  height: 280px;
  overflow: hidden;
}

.service-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.service-image-section:hover .service-img {
  transform: scale(1.05);
}

.service-badges {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 16px;
}

.rating-section {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
  margin-top: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.rating-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: var(--text-primary);
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.service-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.detail-right {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.service-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.service-name {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.service-description {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.price-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #667eea 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}

.current-price {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
}

.original-price {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: line-through;
  margin-left: 12px;
}

.duration-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
}

.features-section {
  margin-bottom: 24px;
}

.features-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-color);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.action-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.book-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

.tips-section {
  padding: 20px;
  background: #fef6e6;
  border-radius: 12px;
  border-left: 4px solid var(--warning-color);
}

.tips-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: var(--text-primary);
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.loading-state,
.not-found {
  padding: 80px 20px;
  text-align: center;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-left {
    position: static;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .action-section {
    flex-direction: column;
  }
}
</style>
