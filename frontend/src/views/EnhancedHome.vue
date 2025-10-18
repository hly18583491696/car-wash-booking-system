<template>
  <div class="enhanced-home">
    <!-- 粒子背景 -->
    <ParticleBackground />
    
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <h1 class="hero-title">
            <span class="title-line">专业洗车服务</span>
            <span class="title-highlight">一键预约</span>
          </h1>
          <p class="hero-subtitle">
            高效便捷的汽车洗车服务预约平台，为您的爱车提供专业护理
          </p>
          <div class="hero-actions">
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
            <router-link to="/ui-demo">
              <el-button size="large" class="demo-button">
                <el-icon><Star /></el-icon>
                UI演示
              </el-button>
            </router-link>
          </div>
        </div>
        <div class="hero-image">
          <div class="floating-card">
            <div class="card-content">
              <el-icon size="80" color="var(--primary-color)">
                <House />
              </el-icon>
              <h3>智能预约</h3>
              <p>24小时在线服务</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色功能 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为什么选择我们</h2>
          <p class="section-subtitle">专业、便捷、高效的洗车服务体验</p>
        </div>
        <div class="features-grid">
          <div 
            class="feature-card" 
            v-for="(feature, index) in features" 
            :key="feature.id"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
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

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div 
            class="stat-item" 
            v-for="(stat, index) in stats" 
            :key="stat.id"
            :style="{ animationDelay: `${index * 0.2}s` }"
          >
            <div class="stat-icon">
              <el-icon size="40">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-number">
              <AnimatedCounter 
                :value="stat.numValue" 
                :suffix="stat.suffix"
                :duration="2000 + index * 300"
                :color="'#ffffff'"
              />
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 服务展示 -->
    <section class="services-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门服务项目</h2>
          <p class="section-subtitle">多种洗车套餐，满足不同需求</p>
        </div>
        <div class="services-grid">
          <div 
            class="service-card" 
            v-for="(service, index) in services" 
            :key="service.id"
            :style="{ animationDelay: `${index * 0.15}s` }"
          >
            <div class="service-header">
              <div class="service-icon">
                <el-icon size="48" :color="service.color">
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
                <el-button type="primary" @click="bookService(service)">
                  立即预约
                </el-button>
              </div>
            </div>
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
          <div 
            class="testimonial-card" 
            v-for="(testimonial, index) in testimonials" 
            :key="testimonial.id"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="testimonial-content">
              <div class="rating">
                <el-icon v-for="i in 5" :key="i" :class="{ active: i <= testimonial.rating }">
                  <Star />
                </el-icon>
              </div>
              <p class="testimonial-text">"{{ testimonial.content }}"</p>
            </div>
            <div class="testimonial-author">
              <el-avatar :size="50" :src="testimonial.avatar">
                {{ testimonial.name.charAt(0) }}
              </el-avatar>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ParticleBackground from '@/components/ParticleBackground.vue'
import AnimatedCounter from '@/components/AnimatedCounter.vue'

export default {
  name: 'EnhancedHome',
  components: {
    ParticleBackground,
    AnimatedCounter
  },
  setup() {
    const router = useRouter()
    
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
    
    // 统计数据
    const stats = ref([
      { id: 1, icon: 'User', numValue: 10000, suffix: '+', label: '服务用户' },
      { id: 2, icon: 'ShoppingCart', numValue: 50000, suffix: '+', label: '完成订单' },
      { id: 3, icon: 'Star', numValue: 98, suffix: '%', label: '满意度' },
      { id: 4, icon: 'Clock', numValue: 24, suffix: 'h', label: '响应时间' }
    ])
    
    // 服务项目
    const services = ref([
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗，内饰简单清理',
        price: 30,
        duration: '30分钟',
        features: ['外观清洗', '轮胎清洁', '玻璃清洁'],
        icon: 'House',
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
        icon: 'Trophy',
        color: 'var(--error-color)',
        popular: false
      }
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
    
    return {
      features,
      stats,
      services,
      testimonials,
      bookService
    }
  }
}
</script>

<style scoped>
.enhanced-home {
  min-height: 100vh;
  position: relative;
}

/* 英雄区域 */
.hero-section {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 120px 0;
  position: relative;
  overflow: hidden;
  min-height: 80vh;
  display: flex;
  align-items: center;
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
  position: relative;
  z-index: 10;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-line {
  display: block;
}

.title-highlight {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { filter: drop-shadow(0 0 10px rgba(255, 215, 0, 0.5)); }
  to { filter: drop-shadow(0 0 20px rgba(255, 215, 0, 0.8)); }
}

.hero-subtitle {
  font-size: 1.3rem;
  line-height: 1.6;
  margin-bottom: 40px;
  opacity: 0.9;
  max-width: 500px;
}

.hero-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.cta-button {
  background: var(--text-white);
  color: var(--primary-color);
  border: none;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.secondary-button, .demo-button {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 2px solid rgba(255, 255, 255, 0.4) !important;
  color: #ffffff !important;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.demo-button {
  background: rgba(255, 215, 0, 0.2) !important;
  border-color: rgba(255, 215, 0, 0.6) !important;
}

.secondary-button:hover, .demo-button:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
  color: #ffffff !important;
  transform: translateY(-2px);
}

.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.floating-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  padding: 40px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.card-content h3 {
  margin: 20px 0 10px;
  font-size: 1.5rem;
  font-weight: 600;
}

.card-content p {
  opacity: 0.8;
  font-size: 1rem;
}

/* 通用样式 */
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
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 40px;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  border-radius: var(--radius-xl);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
  animation: slideInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(30px);
}

@keyframes slideInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-xl);
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

/* 统计数据区域 */
.stats-section {
  padding: 100px 0;
  background: var(--primary-gradient);
  color: var(--text-white);
  position: relative;
  overflow: hidden;
}

.stats-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="white" opacity="0.05"><polygon points="0,0 1000,0 1000,100 0,80"/></svg>');
  background-size: cover;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  position: relative;
  z-index: 1;
}

.stat-item {
  text-align: center;
  padding: 30px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: slideInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(30px);
}

.stat-icon {
  margin-bottom: 16px;
  opacity: 0.8;
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

/* 服务项目区域 */
.services-section {
  padding: 100px 0;
  background: var(--bg-secondary);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 40px;
}

.service-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
  animation: slideInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(30px);
}

.service-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.service-header {
  padding: 30px;
  text-align: center;
  background: var(--bg-light);
  position: relative;
}

.service-icon {
  margin-bottom: 20px;
}

.service-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: var(--error-color);
  color: var(--text-white);
  padding: 6px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
}

.service-content {
  padding: 30px;
}

.service-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.service-desc {
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.5;
}

.service-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.feature-tag {
  background: var(--primary-light);
  color: var(--primary-color);
  padding: 6px 12px;
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
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--primary-color);
}

.duration {
  font-size: 12px;
  color: var(--text-light);
}

/* 用户评价区域 */
.testimonials-section {
  padding: 100px 0;
  background: var(--bg-primary);
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.testimonial-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 30px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  animation: slideInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(30px);
}

.testimonial-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-5px);
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
  margin-bottom: 24px;
  font-style: italic;
  font-size: 1.1rem;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-info {
  flex: 1;
}

.author-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.author-title {
  font-size: 14px;
  color: var(--text-light);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    justify-content: center;
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
  .hero-section {
    padding: 80px 0;
  }
  
  .hero-title {
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