<template>
  <div class="not-found-page">
    <div class="container">
      <div class="not-found-content">
        <div class="error-illustration">
          <div class="error-code">404</div>
          <div class="error-icon">
            <el-icon size="120" color="var(--primary-color)">
              <QuestionFilled />
            </el-icon>
          </div>
        </div>
        
        <div class="error-info">
          <h1 class="error-title">页面未找到</h1>
          <p class="error-description">
            抱歉，您访问的页面不存在或已被移除。
            <br>
            请检查网址是否正确，或返回首页继续浏览。
          </p>
          
          <div class="error-actions">
            <router-link to="/">
              <el-button type="primary" size="large">
                <el-icon><House /></el-icon>
                返回首页
              </el-button>
            </router-link>
            
            <el-button size="large" @click="goBack">
              <el-icon><Back /></el-icon>
              返回上页
            </el-button>
            
            <router-link to="/services">
              <el-button size="large" plain>
                <el-icon><Star /></el-icon>
                浏览服务
              </el-button>
            </router-link>
          </div>
          
          <div class="help-links">
            <h3>您可能在寻找：</h3>
            <div class="links-grid">
              <router-link to="/appointment" class="help-link">
                <el-icon><Calendar /></el-icon>
                <span>预约洗车</span>
              </router-link>
              
              <router-link to="/orders" class="help-link">
                <el-icon><List /></el-icon>
                <span>我的订单</span>
              </router-link>
              
              <router-link to="/profile" class="help-link">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </router-link>
              
              <router-link to="/services" class="help-link">
                <el-icon><Setting /></el-icon>
                <span>服务项目</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'NotFound',
  setup() {
    const router = useRouter()
    
    const goBack = () => {
      if (window.history.length > 1) {
        router.go(-1)
      } else {
        router.push('/')
      }
    }
    
    return {
      goBack
    }
  }
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.not-found-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  max-width: 1000px;
  width: 100%;
}

/* 错误插图 */
.error-illustration {
  text-align: center;
  position: relative;
}

.error-code {
  font-size: 8rem;
  font-weight: 900;
  color: var(--primary-light);
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-icon {
  position: relative;
  z-index: 2;
}

/* 错误信息 */
.error-info {
  text-align: left;
}

.error-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.error-description {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 32px;
}

/* 操作按钮 */
.error-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.error-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 帮助链接 */
.help-links h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.help-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.help-link:hover {
  background: var(--primary-light);
  color: var(--primary-color);
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.help-link .el-icon {
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-content {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .error-info {
    text-align: center;
  }
  
  .error-code {
    font-size: 6rem;
  }
  
  .error-title {
    font-size: 2rem;
  }
  
  .error-actions {
    justify-content: center;
  }
  
  .links-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .not-found-page {
    padding: 20px 0;
  }
  
  .error-code {
    font-size: 4rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .error-actions {
    flex-direction: column;
  }
  
  .error-actions .el-button {
    width: 100%;
    justify-content: center;
  }
}
</style>