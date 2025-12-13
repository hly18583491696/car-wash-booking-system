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
              <div class="image-placeholder">
                <el-icon size="120" :color="service.color">
                  <component :is="service.icon" />
                </el-icon>
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
                <span class="service-count">{{ service.serviceCount }}+ 次服务</span>
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

    // 服务数据（与 Services.vue 保持一致）
    const servicesData = [
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗，轮胎清洁，玻璃清洁，适合日常维护。我们使用专业清洗设备和环保清洗剂，确保您的爱车焕然一新。',
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
        description: '深度清洁，内外兼修，包含基础洗车所有项目。额外提供内饰深度清洁、轮毂精洗、玻璃镀膜等服务，让您的爱车由内而外焕发光彩。',
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
        description: '全方位护理，焕然一新，适合重要场合前使用。包含精洗套餐所有项目，额外提供车漆打蜡、真皮座椅护理、全车消毒等高端服务。',
        price: 128,
        originalPrice: 168,
        duration: '90分钟',
        category: 'luxury',
        rating: 4.8,
        serviceCount: 1500,
        features: ['精洗服务', '车漆打蜡', '真皮护理', '全车消毒', '发动机舱清洁', '轮胎上光'],
        icon: 'Trophy',
        color: 'var(--error-color)',
        popular: false,
        recommended: true,
        discount: '7.6'
      },
      {
        id: 4,
        name: '内饰精洗',
        description: '专注内饰深度清洁，去除异味，适合有宠物或经常载人的车辆。使用专业设备进行蒸汽清洗和臭氧消毒。',
        price: 88,
        originalPrice: 108,
        duration: '75分钟',
        category: 'interior',
        rating: 4.5,
        serviceCount: 980,
        features: ['座椅清洗', '地毯清洁', '空调消毒', '异味去除', '仪表盘护理'],
        icon: 'Brush',
        color: 'var(--success-color)',
        popular: false,
        recommended: false,
        discount: '8.1'
      },
      {
        id: 5,
        name: '车漆护理',
        description: '专业车漆养护，恢复光泽，延长漆面寿命。包含去污、抛光、打蜡等全套工序，让您的爱车漆面如新。',
        price: 198,
        originalPrice: 258,
        duration: '120分钟',
        category: 'luxury',
        rating: 4.9,
        serviceCount: 650,
        features: ['漆面去污', '划痕修复', '专业抛光', '高级打蜡', '镀膜保护'],
        icon: 'MagicStick',
        color: '#9b59b6',
        popular: true,
        recommended: true,
        discount: '7.7'
      },
      {
        id: 6,
        name: '会员月卡',
        description: '超值月卡套餐，每月4次基础洗车+1次精洗，省心省钱。适合经常用车的车主，享受VIP专属服务通道。',
        price: 199,
        originalPrice: 280,
        duration: '不限',
        category: 'premium',
        rating: 4.7,
        serviceCount: 420,
        features: ['4次基础洗车', '1次精洗服务', 'VIP专属通道', '免费停车', '生日特惠'],
        icon: 'Medal',
        color: '#e67e22',
        popular: true,
        recommended: true,
        discount: '7.1'
      }
    ]

    const categoryLabels = {
      basic: '基础洗车',
      premium: '精洗套餐',
      luxury: '豪华套餐',
      interior: '内饰清洁'
    }

    const getCategoryLabel = (category) => {
      return categoryLabels[category] || category
    }

    const loadService = () => {
      loading.value = true
      const serviceId = parseInt(route.query.id)
      
      // 模拟API请求延迟
      setTimeout(() => {
        const found = servicesData.find(s => s.id === serviceId)
        service.value = found || null
        loading.value = false
      }, 300)
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
  padding: 40px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.image-placeholder {
  margin-bottom: 20px;
}

.service-badges {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
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
