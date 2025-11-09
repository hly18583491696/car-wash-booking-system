<template>
  <div class="home-page">
    <!-- 粒子背景 -->
    <ParticleBackground />
    
    <!-- 顶部展示区 -->
    <section class="top-display-section">
      <div class="top-display-container">
        <div class="top-display-content">
          <h1 class="top-display-title">
            专业洗车服务
            <span class="highlight">一键预约</span>
          </h1>
          <p class="top-display-subtitle">
            高效便捷的汽车洗车服务预约平台，为您的爱车提供专业护理
          </p>
          <div class="top-display-actions">
            <router-link to="/appointment">
              <el-button type="primary" size="large" class="cta-button">
                <el-icon><Calendar /></el-icon>
                立即预约
              </el-button>
            </router-link>
            <router-link to="/services">
              <el-button size="large" class="secondary-button">
                <el-icon><Grid /></el-icon>
                查看服务
              </el-button>
            </router-link>
            <router-link to="/stores">
              <el-button size="large" class="secondary-button">
                <el-icon><Location /></el-icon>
                查找店铺
              </el-button>
            </router-link>
          </div>
        </div>
        <div class="top-display-image">
          <div class="image-placeholder">
            <el-icon size="120" color="var(--primary-color)">
              <House />
            </el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户快捷功能区域 -->
    <section class="user-shortcuts-section" v-if="isLoggedIn">
      <div class="container">
        <div class="shortcuts-header">
          <h2 class="section-title">快捷功能</h2>
          <p class="section-subtitle">常用功能，一键直达</p>
        </div>
        <div class="shortcuts-grid">
          <router-link to="/orders" class="shortcut-card">
            <div class="shortcut-icon orders-icon">
              <el-icon size="32"><List /></el-icon>
            </div>
            <h3 class="shortcut-title">我的订单</h3>
            <p class="shortcut-desc">查看订单状态和历史记录</p>
          </router-link>
          
          <router-link to="/payment-records" class="shortcut-card">
            <div class="shortcut-icon payment-icon">
              <el-icon size="32"><CreditCard /></el-icon>
            </div>
            <h3 class="shortcut-title">支付记录</h3>
            <p class="shortcut-desc">查看支付和退款记录</p>
          </router-link>
          
          <router-link to="/appointment" class="shortcut-card">
            <div class="shortcut-icon appointment-icon">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <h3 class="shortcut-title">快速预约</h3>
            <p class="shortcut-desc">预约洗车服务</p>
          </router-link>
          
          <router-link to="/profile" class="shortcut-card">
            <div class="shortcut-icon profile-icon">
              <el-icon size="32"><User /></el-icon>
            </div>
            <h3 class="shortcut-title">个人中心</h3>
            <p class="shortcut-desc">管理个人信息和设置</p>
          </router-link>
        </div>
      </div>
    </section>

    <!-- 特色服务 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为什么选择我们</h2>
          <p class="section-subtitle">专业、便捷、高效的洗车服务体验</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="feature in features" :key="feature.id">
            <div class="feature-icon" :style="{ background: feature.color }">
              <el-icon size="32">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 服务项目 -->
    <section class="services-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门服务项目</h2>
          <p class="section-subtitle">多种洗车套餐，满足不同需求</p>
        </div>
        <div class="services-grid">
          <div class="service-card" v-for="service in popularServices" :key="service.id">
            <div class="service-image">
              <div class="image-placeholder">
                <el-icon size="60" :color="service.color">
                  <component :is="service.icon" />
                </el-icon>
              </div>
              <div class="service-badge" v-if="service.popular">热门</div>
            </div>
            <div class="service-content">
              <h3 class="service-title">{{ service.name }}</h3>
              <p class="service-desc">{{ service.description }}</p>
              <div class="service-features">
                <span v-for="feature in service.features" :key="feature" class="feature-tag">
                  {{ feature }}
                </span>
              </div>
              <div class="service-footer">
                <div class="service-price">
                  <span class="price">¥{{ service.price }}</span>
                  <span class="duration">{{ service.duration }}</span>
                </div>
                <el-button type="primary" size="small" @click="bookService(service)">
                  立即预约
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="services-more">
          <router-link to="/services">
            <el-button type="primary" plain size="large">
              查看全部服务
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </router-link>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item" v-for="(stat, index) in stats" :key="stat.id">
            <div class="stat-number">
              <AnimatedCounter 
                :value="stat.numValue || 0" 
                :suffix="stat.suffix || ''"
                :duration="2000 + index * 300"
                :color="'#ffffff'"
              />
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户评价 -->
    <section class="testimonials-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">用户评价</h2>
          <p class="section-subtitle">听听用户怎么说</p>
        </div>
        <div class="testimonials-grid">
          <div class="testimonial-card" v-for="testimonial in testimonials" :key="testimonial.id">
            <div class="testimonial-content">
              <div class="rating">
                <el-icon v-for="i in 5" :key="i" :class="{ active: i <= testimonial.rating }">
                  <Star />
                </el-icon>
              </div>
              <p class="testimonial-text">"{{ testimonial.content }}"</p>
            </div>
            <div class="testimonial-author">
              <div class="author-avatar">
                <el-avatar :size="40" :src="testimonial.avatar">
                  {{ testimonial.name.charAt(0) }}
                </el-avatar>
              </div>
              <div class="author-info">
                <div class="author-name">{{ testimonial.name }}</div>
                <div class="author-title">{{ testimonial.title }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ParticleBackground from '@/components/ParticleBackground.vue'
import AnimatedCounter from '@/components/AnimatedCounter.vue'
import AuthManager from '../utils/auth.js'

export default {
  name: 'Home',
  components: {
    ParticleBackground,
    AnimatedCounter
  },
  setup() {
    const router = useRouter()
    
    // 使用 AuthManager 进行一致的登录状态检测
    const isLoggedIn = computed(() => AuthManager.isAuthenticated())
    const userInfo = computed(() => AuthManager.getCurrentUser())
    

    
    // 特色功能
    const features = ref([
      {
        id: 1,
        icon: 'Clock',
        title: '24小时预约',
        description: '随时随地在线预约，无需等待',
        color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      },
      {
        id: 2,
        icon: 'Star',
        title: '专业服务',
        description: '经验丰富的专业团队，品质保证',
        color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
      },
      {
        id: 3,
        icon: 'Location',
        title: '就近服务',
        description: '多个服务网点，就近选择',
        color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      },
      {
        id: 4,
        icon: 'Money',
        title: '透明定价',
        description: '明码标价，无隐藏费用',
        color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
      }
    ])
    
    // 热门服务
    const popularServices = ref([
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗，内饰简单清理',
        price: 30,
        duration: '30分钟',
        features: ['外观清洗', '轮胎清洁', '玻璃清洁'],
        icon: 'CarWashing',
        color: 'var(--primary-color)',
        popular: false
      },
      {
        id: 2,
        name: '精洗套餐',
        description: '深度清洁，内外兼修',
        price: 68,
        duration: '60分钟',
        features: ['深度清洗', '内饰清洁', '轮毂清洁', '玻璃镀膜'],
        icon: 'Star',
        color: 'var(--warning-color)',
        popular: true
      },
      {
        id: 3,
        name: '豪华套餐',
        description: '全方位护理，焕然一新',
        price: 128,
        duration: '90分钟',
        features: ['精洗服务', '打蜡护理', '内饰深度清洁', '轮胎护理'],
        icon: 'Crown',
        color: 'var(--error-color)',
        popular: false
      }
    ])
    
    // 统计数据
    const stats = ref([
      { id: 1, value: '10000+', numValue: 10000, suffix: '+', label: '服务用户' },
      { id: 2, value: '50000+', numValue: 50000, suffix: '+', label: '完成订单' },
      { id: 3, value: '98%', numValue: 98, suffix: '%', label: '满意度' },
      { id: 4, value: '24h', numValue: 24, suffix: 'h', label: '响应时间' }
    ])
    
    // 用户评价
    const testimonials = ref([
      {
        id: 1,
        name: '张先生',
        title: '奔驰车主',
        content: '服务非常专业，预约方便，洗车效果很满意！',
        rating: 5,
        avatar: ''
      },
      {
        id: 2,
        name: '李女士',
        title: '宝马车主',
        content: '价格透明，服务态度好，会继续使用这个平台。',
        rating: 5,
        avatar: ''
      },
      {
        id: 3,
        name: '王先生',
        title: '奥迪车主',
        content: '在线预约很方便，服务质量也很不错，推荐！',
        rating: 4,
        avatar: ''
      }
    ])
    
    // 预约服务
    const bookService = (service) => {
      router.push({
        path: '/appointment',
        query: { serviceId: service.id }
      })
    }
    
    onMounted(() => {
      // 页面加载时的初始化逻辑
      console.log('Home页面已加载，用户登录状态:', isLoggedIn.value)
    })
    
    return {
      features,
      popularServices,
      stats,
      testimonials,
      bookService,
      isLoggedIn,
      userInfo
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

/* 顶部展示区 */
.top-display-section {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 80px 0;
  position: relative;
  overflow: hidden;
}

.top-display-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="white" opacity="0.1"><polygon points="0,0 1000,0 1000,100 0,80"/></svg>');
  background-size: cover;
}

.top-display-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.top-display-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 20px;
}

.highlight {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.top-display-subtitle {
  font-size: 1.2rem;
  line-height: 1.6;
  margin-bottom: 40px;
  opacity: 0.9;
}

.top-display-actions {
  display: flex;
  gap: 20px;
}

.cta-button {
  background: var(--text-white);
  color: var(--primary-color);
  border: none;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
}

.cta-button:hover {
  background: var(--bg-light);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.secondary-button {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 2px solid rgba(255, 255, 255, 0.4) !important;
  color: #ffffff !important;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.secondary-button:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
  color: #ffffff !important;
  transform: translateY(-2px);
}

.secondary-button .el-icon {
  color: #ffffff !important;
}



.top-display-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-placeholder {
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

/* 用户快捷功能区域 */
.user-shortcuts-section {
  padding: 60px 0;
  background: var(--bg-primary);
}

.shortcuts-header {
  text-align: center;
  margin-bottom: 40px;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.shortcut-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid var(--border-light);
  text-decoration: none;
  color: inherit;
  display: block;
}

.shortcut-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-color);
}

.shortcut-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: white;
}

.orders-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.payment-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.appointment-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.profile-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.shortcut-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.shortcut-desc {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 通用容器和标题 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

/* 特色功能区域 */
.features-section {
  padding: 100px 0;
  background: var(--bg-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  border-radius: var(--radius-lg);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: var(--text-white);
}

.feature-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.feature-desc {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 服务项目区域 */
.services-section {
  padding: 100px 0;
  background: var(--bg-secondary);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 30px;
  margin-bottom: 60px;
}

.service-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.service-image {
  height: 160px;
  background: var(--bg-light);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.service-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--error-color);
  color: var(--text-white);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.service-content {
  padding: 24px;
}

.service-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.service-desc {
  color: var(--text-secondary);
  margin-bottom: 16px;
  line-height: 1.5;
}

.service-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.feature-tag {
  background: var(--primary-light);
  color: var(--primary-color);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.service-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-price {
  display: flex;
  flex-direction: column;
}

.price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

.duration {
  font-size: 12px;
  color: var(--text-light);
}

.services-more {
  text-align: center;
}

/* 统计数据区域 */
.stats-section {
  padding: 80px 0;
  background: var(--primary-gradient);
  color: var(--text-white);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 1.1rem;
  opacity: 0.9;
}

/* 用户评价区域 */
.testimonials-section {
  padding: 100px 0;
  background: var(--bg-primary);
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.testimonial-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 30px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.testimonial-card:hover {
  box-shadow: var(--shadow-md);
}

.rating {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
}

.rating .el-icon {
  color: var(--border-color);
  transition: color var(--transition-fast);
}

.rating .el-icon.active {
  color: #FFD700;
}

.testimonial-text {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
  font-style: italic;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name {
  font-weight: 600;
  color: var(--text-primary);
}

.author-title {
  font-size: 14px;
  color: var(--text-light);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-display-container {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .top-display-title {
    font-size: 2.5rem;
  }
  
  .top-display-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .features-grid,
  .services-grid,
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .top-display-section {
    padding: 60px 0;
  }
  
  .top-display-title {
    font-size: 2rem;
  }
  
  .features-section,
  .services-section,
  .testimonials-section {
    padding: 60px 0;
  }
  
  .stats-section {
    padding: 60px 0;
  }
  
  .stat-number {
    font-size: 2rem;
  }
}
</style>