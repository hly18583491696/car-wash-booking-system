<template>
  <div id="app" :class="themeClass">
    <router-view />
    <BackToTop />
    
    <!-- 浮动操作按钮 -->
    <FloatingActionButton />
    
    <!-- 通知系统 -->
    <NotificationSystem ref="notificationSystem" />
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import BackToTop from './components/BackToTop.vue'
import FloatingActionButton from './components/FloatingActionButton.vue'
import NotificationSystem from './components/NotificationSystem.vue'

export default {
  name: 'App',
  components: {
    BackToTop,
    FloatingActionButton,
    NotificationSystem
  },
  setup() {
    // 简化的主题类名，暂时使用默认主题
    const themeClass = computed(() => {
      return 'theme-blue'
    })
    
    // 初始化应用
    onMounted(() => {
      // 设置全局错误处理
      window.addEventListener('unhandledrejection', (event) => {
        console.error('未处理的Promise拒绝:', event.reason)
      })
      
      window.addEventListener('error', (event) => {
        console.error('全局错误:', event.error)
      })
    })
    
    return {
      themeClass
    }
  }
}
</script>

<style>
/* 导入全局样式 */
@import './assets/css/themes.css';
@import './assets/css/profile.css';

/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
  line-height: 1.5;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: var(--bg-secondary);
  color: var(--text-primary);
  transition: all var(--transition-normal);
}

#app {
  min-height: 100vh;
}

/* 容器样式 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式容器 */
@media (max-width: 1240px) {
  .container {
    max-width: 100%;
    padding: 0 16px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-light);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
  transition: background var(--transition-fast);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-light);
}

/* Element Plus 组件样式覆盖 */
.el-button {
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: all var(--transition-fast);
}

.el-button:hover {
  transform: translateY(-1px);
}

.el-card {
  border-radius: var(--radius-lg);
  border-color: var(--border-color);
  box-shadow: var(--shadow-sm);
}

.el-input__wrapper {
  border-radius: var(--radius-md);
  box-shadow: none;
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.el-input__wrapper:hover {
  border-color: var(--primary-light);
}

.el-input__wrapper.is-focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.el-select .el-input__wrapper {
  cursor: pointer;
}

.el-textarea__inner {
  border-radius: var(--radius-md);
  border-color: var(--border-color);
  transition: all var(--transition-fast);
}

.el-textarea__inner:hover {
  border-color: var(--primary-light);
}

.el-textarea__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.el-dialog {
  border-radius: var(--radius-lg);
}

.el-message {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.el-notification {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.el-table {
  border-radius: var(--radius-lg);
}

.el-pagination {
  justify-content: center;
}

.el-steps {
  margin: 20px 0;
}

.el-step__title {
  font-weight: 500;
}

.el-badge__content {
  border-radius: var(--radius-sm);
}

.el-tag {
  border-radius: var(--radius-sm);
}

.el-progress-bar__outer {
  border-radius: var(--radius-sm);
}

.el-progress-bar__inner {
  border-radius: var(--radius-sm);
}

.el-skeleton__item {
  border-radius: var(--radius-md);
}

.el-empty {
  padding: 40px 20px;
}

.el-calendar-table .el-calendar-day {
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.el-calendar-table .el-calendar-day:hover {
  background: var(--primary-light);
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.scale-enter-active,
.scale-leave-active {
  transition: all var(--transition-normal);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* 工具类 */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.grid {
  display: grid;
}

.hidden {
  display: none;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* 响应式工具类 */
@media (max-width: 768px) {
  .hidden-mobile {
    display: none;
  }
}

@media (min-width: 769px) {
  .hidden-desktop {
    display: none;
  }
}

/* 打印样式 */
@media print {
  .no-print {
    display: none !important;
  }
  
  body {
    background: white !important;
    color: black !important;
  }
  
  .container {
    max-width: none;
    padding: 0;
  }
}
</style>