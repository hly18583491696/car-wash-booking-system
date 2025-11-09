<template>
  <div class="guest-home">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载...</p>
    </div>
    
    <!-- 主要内容 -->
    <div v-else class="content-container">
      <!-- 粒子背景 - 懒加载 -->
      <ParticleBackground v-if="showParticles" />
      
      <!-- 主要展示区 -->
      <section class="hero-section">
        <div class="hero-container">
          <div class="hero-content">
            <h1 class="hero-title">
              专业洗车服务
              <span class="highlight">一键预约</span>
            </h1>
            <p class="hero-subtitle">
              高效便捷的汽车洗车服务预约平台，为您的爱车提供专业护理
            </p>
            
            <!-- 未登录用户引导 -->
            <div class="auth-guide">
              <div class="auth-prompt">
                <el-icon class="prompt-icon"><InfoFilled /></el-icon>
                <span>登录后即可享受完整的预约服务</span>
              </div>
              
              <div class="auth-actions">
                <router-link to="/login">
                  <el-button type="primary" size="large" class="login-btn">
                    <el-icon><User /></el-icon>
                    立即登录
                  </el-button>
                </router-link>
                <router-link to="/register">
                  <el-button size="large" class="register-btn">
                    <el-icon><UserFilled /></el-icon>
                    免费注册
                  </el-button>
                </router-link>
              </div>
              
              <div class="quick-register-guide">
                <p>新用户？<router-link to="/register" class="register-link">立即注册</router-link>，享受更多优惠</p>
              </div>
            </div>
          </div>
          
          <div class="hero-image">
            <div class="image-placeholder">
              <el-icon size="120" color="var(--primary-color)">
                <House />
              </el-icon>
            </div>
          </div>
        </div>
      </section>

    <!-- 访客模式功能展示 -->
    <section class="guest-features-section">
      <div class="container">
        <h2 class="section-title">访客模式 - 基础功能预览</h2>
        <p class="section-subtitle">登录后解锁完整功能</p>
        
        <div class="features-grid">
          <div class="feature-card" v-for="feature in guestFeatures" :key="feature.id">
            <div class="feature-icon" :style="{ background: feature.color }">
              <el-icon size="32">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
            <div class="feature-status">
              <el-tag v-if="feature.available" type="success" size="small">
                <el-icon><Check /></el-icon>
                可用
              </el-tag>
              <el-tag v-else type="info" size="small">
                <el-icon><Lock /></el-icon>
                需登录
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 服务项目预览 -->
    <section class="services-preview-section">
      <div class="container">
        <h2 class="section-title">热门服务项目</h2>
        <p class="section-subtitle">专业团队，品质保证</p>
        
        <div class="services-grid">
          <div class="service-card" v-for="service in previewServices" :key="service.id">
            <div class="service-header">
              <div class="service-icon">
                <el-icon size="24" :color="service.color">
                  <component :is="service.icon" />
                </el-icon>
              </div>
              <div class="service-price">¥{{ service.price }}</div>
            </div>
            <h3 class="service-name">{{ service.name }}</h3>
            <p class="service-description">{{ service.description }}</p>
            <div class="service-features">
              <el-tag v-for="feature in service.features" :key="feature" size="small" type="info">
                {{ feature }}
              </el-tag>
            </div>
            <div class="service-action">
              <router-link to="/login">
                <el-button type="primary" size="small" plain>
                  登录预约
                </el-button>
              </router-link>
            </div>
          </div>
        </div>
        
        <div class="services-more">
          <router-link to="/services">
            <el-button size="large" type="primary" plain>
              <el-icon><Grid /></el-icon>
              查看所有服务
            </el-button>
          </router-link>
        </div>
      </div>
    </section>

      <!-- 注册优惠提示 -->
      <section class="register-promotion-section">
        <div class="container">
          <div class="promotion-card">
            <div class="promotion-content">
              <h3 class="promotion-title">新用户专享优惠</h3>
              <p class="promotion-description">注册即送首次洗车8折优惠券，更有积分奖励等你来拿！</p>
              <div class="promotion-benefits">
                <div class="benefit-item">
                  <el-icon color="var(--success-color)"><CircleCheckFilled /></el-icon>
                  <span>首次洗车8折</span>
                </div>
                <div class="benefit-item">
                  <el-icon color="var(--success-color)"><CircleCheckFilled /></el-icon>
                  <span>积分奖励</span>
                </div>
                <div class="benefit-item">
                  <el-icon color="var(--success-color)"><CircleCheckFilled /></el-icon>
                  <span>生日特惠</span>
                </div>
              </div>
              <router-link to="/register">
                <el-button type="primary" size="large" class="promotion-btn">
                  立即注册领取优惠
                </el-button>
              </router-link>
            </div>
            <div class="promotion-image">
              <el-icon size="80" color="var(--primary-color)">
                <Present />
              </el-icon>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, defineAsyncComponent } from 'vue'

// 懒加载粒子背景组件
const ParticleBackground = defineAsyncComponent(() => 
  import('../components/ParticleBackground.vue')
)

export default {
  name: 'GuestHome',
  components: {
    ParticleBackground
  },
  setup() {
    const loading = ref(true)
    const showParticles = ref(false)
    
    // 性能监控
    const startTime = performance.now()
    
    // 访客模式功能（预加载数据）
    const guestFeatures = ref([
      {
        id: 1,
        icon: 'Grid',
        title: '服务项目查看',
        description: '浏览所有洗车服务项目和价格',
        color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        available: true
      },
      {
        id: 2,
        icon: 'Location',
        title: '洗车店查询',
        description: '查找附近的洗车店位置信息',
        color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        available: true
      },
      {
        id: 3,
        icon: 'Calendar',
        title: '在线预约',
        description: '选择时间和服务进行预约',
        color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        available: false
      },
      {
        id: 4,
        icon: 'List',
        title: '订单管理',
        description: '查看和管理您的预约订单',
        color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        available: false
      }
    ])

    // 服务项目预览（预加载数据）
    const previewServices = ref([
      {
        id: 1,
        name: '基础洗车',
        description: '外观清洗，内饰简单清理',
        price: 30,
        features: ['外观清洗', '轮胎清洁', '玻璃清洁'],
        icon: 'CarWashing',
        color: 'var(--primary-color)'
      },
      {
        id: 2,
        name: '精致洗车',
        description: '深度清洁，内外兼顾',
        price: 50,
        features: ['深度清洗', '内饰护理', '轮毂清洁'],
        icon: 'Star',
        color: 'var(--warning-color)'
      },
      {
        id: 3,
        name: '豪华洗车',
        description: '全方位护理，专业打蜡',
        price: 80,
        features: ['全面清洗', '专业打蜡', '内饰深度清洁'],
        icon: 'Crown',
        color: 'var(--danger-color)'
      }
    ])

    // 页面初始化
    const initializePage = async () => {
      try {
        // 快速初始化核心内容
        await nextTick()
        
        // 记录加载时间
        const loadTime = performance.now() - startTime
        console.log(`GuestHome页面核心内容加载完成，耗时: ${Math.round(loadTime)}ms`)
        
        // 延迟加载粒子背景
        setTimeout(() => {
          showParticles.value = true
        }, 500)
        
      } catch (error) {
        console.error('页面初始化失败:', error)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      // 快速初始化页面
      initializePage()
    })

    return {
      loading,
      showParticles,
      guestFeatures,
      previewServices
    }
  }
}
</script>

<style scoped>
.guest-home {
  min-height: 100vh;
  position: relative;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 主要内容 */
.content-container {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 主要展示区 */
.hero-section {
  min-height: 100vh;
  display: flex;
  align-items: center;
  position: relative;
  padding: 2rem 0;
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.hero-content {
  z-index: 2;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.5rem;
  color: var(--text-primary);
}

.highlight {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

/* 认证引导区 */
.auth-guide {
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

.auth-prompt {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: var(--info-light);
  border-radius: 8px;
  color: var(--info-color);
}

.prompt-icon {
  font-size: 1.25rem;
}

.auth-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.login-btn, .register-btn {
  flex: 1;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
}

.quick-register-guide {
  text-align: center;
  color: var(--text-secondary);
}

.register-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.register-link:hover {
  text-decoration: underline;
}

/* 图片区域 */
.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-placeholder {
  width: 300px;
  height: 300px;
  background: var(--bg-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

/* 访客功能区 */
.guest-features-section {
  padding: 5rem 0;
  background: var(--bg-primary);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.section-subtitle {
  font-size: 1.125rem;
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 3rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  color: white;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.feature-description {
  color: var(--text-secondary);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.feature-status {
  display: flex;
  justify-content: center;
}

/* 服务预览区 */
.services-preview-section {
  padding: 5rem 0;
  background: var(--bg-secondary);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.service-card {
  background: var(--bg-primary);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.service-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.service-icon {
  width: 48px;
  height: 48px;
  background: var(--bg-secondary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

.service-name {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.service-description {
  color: var(--text-secondary);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.service-features {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.service-action {
  text-align: center;
}

.services-more {
  text-align: center;
}

/* 注册优惠区 */
.register-promotion-section {
  padding: 5rem 0;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
}

.promotion-card {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  align-items: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.promotion-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.promotion-description {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.promotion-benefits {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
  color: var(--text-primary);
}

.promotion-btn {
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
}

.promotion-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 性能优化 */
.feature-card,
.service-card {
  will-change: transform;
}

.feature-card:hover,
.service-card:hover {
  transform: translateY(-4px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .auth-actions {
    flex-direction: column;
  }
  
  .promotion-card {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .features-grid,
  .services-grid {
    grid-template-columns: 1fr;
  }
}

/* 预加载优化 */
@media (prefers-reduced-motion: reduce) {
  .content-container {
    animation: none;
  }
  
  .loading-spinner {
    animation: none;
  }
  
  .feature-card,
  .service-card {
    transition: none;
  }
}
</style>