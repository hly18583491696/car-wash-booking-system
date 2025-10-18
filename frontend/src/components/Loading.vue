<template>
  <div class="loading-container" :class="{ fullscreen: fullscreen }">
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      
      <div class="loading-text">
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      
      <div v-if="showProgress" class="loading-progress">
        <el-progress :percentage="progress" :show-text="false" />
        <span class="progress-text">{{ progress }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Loading',
  props: {
    fullscreen: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '加载中...'
    },
    description: {
      type: String,
      default: ''
    },
    showProgress: {
      type: Boolean,
      default: false
    },
    progress: {
      type: Number,
      default: 0
    }
  }
}
</script>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  border-radius: 0;
}

.loading-content {
  text-align: center;
  max-width: 300px;
}

/* 加载动画 */
.loading-spinner {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1.2s linear infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: 0s;
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.4s;
  width: 60px;
  height: 60px;
  top: 10px;
  left: 10px;
  border-top-color: var(--success-color);
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.8s;
  width: 40px;
  height: 40px;
  top: 20px;
  left: 20px;
  border-top-color: var(--warning-color);
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 加载文本 */
.loading-text h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.loading-text p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 0;
}

/* 进度条 */
.loading-progress {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 14px;
  color: var(--text-secondary);
  min-width: 40px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .loading-container {
    padding: 20px;
  }
  
  .loading-spinner {
    width: 60px;
    height: 60px;
  }
  
  .spinner-ring:nth-child(2) {
    width: 45px;
    height: 45px;
    top: 7.5px;
    left: 7.5px;
  }
  
  .spinner-ring:nth-child(3) {
    width: 30px;
    height: 30px;
    top: 15px;
    left: 15px;
  }
}
</style>