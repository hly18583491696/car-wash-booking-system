<template>
  <div class="app-layout">
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
    
    <!-- 页脚 -->
    <Footer />
    
    <!-- 回到顶部按钮 -->
    <BackToTop />
    
    <!-- 全局加载遮罩 -->
    <div v-if="globalLoading" class="global-loading">
      <div class="loading-content">
        <el-icon class="loading-icon" size="40">
          <Loading />
        </el-icon>
        <p class="loading-text">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import Navbar from './Navbar.vue'
import Footer from './Footer.vue'
import BackToTop from '../BackToTop.vue'

export default {
  name: 'Layout',
  components: {
    Navbar,
    Footer,
    BackToTop
  },
  setup() {
    const globalLoading = ref(false)
    
    // 监听全局加载状态
    const handleLoadingStart = () => {
      globalLoading.value = true
    }
    
    const handleLoadingEnd = () => {
      globalLoading.value = false
    }
    
    onMounted(() => {
      // 监听路由变化时的加载状态
      window.addEventListener('loading-start', handleLoadingStart)
      window.addEventListener('loading-end', handleLoadingEnd)
    })
    
    onUnmounted(() => {
      window.removeEventListener('loading-start', handleLoadingStart)
      window.removeEventListener('loading-end', handleLoadingEnd)
    })
    
    return {
      globalLoading
    }
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 64px - 80px); /* 减去导航栏和页脚高度 */
}

/* 页面切换动画 */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 全局加载遮罩 */
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
}

.loading-icon {
  color: var(--primary-color);
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-text {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 深色主题适配 */
[data-theme="dark"] .global-loading {
  background: rgba(31, 41, 55, 0.9);
}
</style>