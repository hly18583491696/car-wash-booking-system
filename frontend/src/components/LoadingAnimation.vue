<template>
  <div class="loading-container" :class="{ fullscreen: fullscreen }">
    <div class="loading-content">
      <!-- 主加载动画 -->
      <div class="loading-spinner" :class="type">
        <div v-if="type === 'dots'" class="dots-loader">
          <div class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.1}s` }"></div>
        </div>
        
        <div v-else-if="type === 'pulse'" class="pulse-loader">
          <div class="pulse-ring" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.4}s` }"></div>
        </div>
        
        <div v-else-if="type === 'wave'" class="wave-loader">
          <div class="wave-bar" v-for="i in 5" :key="i" :style="{ animationDelay: `${i * 0.1}s` }"></div>
        </div>
        
        <div v-else-if="type === 'car'" class="car-loader">
          <div class="car">
            <div class="car-body"></div>
            <div class="car-wheel wheel-front"></div>
            <div class="car-wheel wheel-rear"></div>
          </div>
          <div class="road"></div>
        </div>
        
        <div v-else class="circle-loader">
          <div class="circle"></div>
        </div>
      </div>
      
      <!-- 加载文本 -->
      <div class="loading-text" v-if="text">
        <span class="text-content">{{ currentText }}</span>
        <span class="loading-dots">
          <span v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.3}s` }">.</span>
        </span>
      </div>
      
      <!-- 进度条 -->
      <div class="progress-bar" v-if="showProgress">
        <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'

export default {
  name: 'LoadingAnimation',
  props: {
    type: {
      type: String,
      default: 'circle',
      validator: (value) => ['circle', 'dots', 'pulse', 'wave', 'car'].includes(value)
    },
    text: {
      type: String,
      default: ''
    },
    fullscreen: {
      type: Boolean,
      default: false
    },
    showProgress: {
      type: Boolean,
      default: false
    },
    progress: {
      type: Number,
      default: 0
    },
    textList: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const currentTextIndex = ref(0)
    const textInterval = ref(null)
    
    const currentText = computed(() => {
      if (props.textList.length > 0) {
        return props.textList[currentTextIndex.value]
      }
      return props.text
    })
    
    onMounted(() => {
      if (props.textList.length > 1) {
        textInterval.value = setInterval(() => {
          currentTextIndex.value = (currentTextIndex.value + 1) % props.textList.length
        }, 2000)
      }
    })
    
    onUnmounted(() => {
      if (textInterval.value) {
        clearInterval(textInterval.value)
      }
    })
    
    return {
      currentText
    }
  }
}
</script>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  z-index: 9999;
}

.loading-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 圆形加载器 */
.circle-loader .circle {
  width: 50px;
  height: 50px;
  border: 4px solid var(--border-light);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 点状加载器 */
.dots-loader {
  display: flex;
  gap: 8px;
}

.dots-loader .dot {
  width: 12px;
  height: 12px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite both;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 脉冲加载器 */
.pulse-loader {
  position: relative;
  width: 60px;
  height: 60px;
}

.pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: pulse 1.5s ease-out infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

/* 波浪加载器 */
.wave-loader {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 40px;
}

.wave-bar {
  width: 6px;
  height: 10px;
  background: var(--primary-color);
  border-radius: 3px;
  animation: wave 1.2s ease-in-out infinite;
}

@keyframes wave {
  0%, 40%, 100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

/* 汽车加载器 */
.car-loader {
  position: relative;
  width: 100px;
  height: 60px;
}

.car {
  position: absolute;
  top: 20px;
  left: 0;
  width: 60px;
  height: 30px;
  animation: drive 2s linear infinite;
}

.car-body {
  width: 100%;
  height: 20px;
  background: var(--primary-color);
  border-radius: 10px 10px 5px 5px;
  position: relative;
}

.car-body::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 15px;
  width: 30px;
  height: 12px;
  background: var(--primary-color);
  border-radius: 6px 6px 0 0;
}

.car-wheel {
  position: absolute;
  bottom: -8px;
  width: 12px;
  height: 12px;
  background: var(--text-dark);
  border-radius: 50%;
  animation: rotate 0.5s linear infinite;
}

.wheel-front {
  right: 8px;
}

.wheel-rear {
  left: 8px;
}

.road {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: repeating-linear-gradient(
    to right,
    var(--border-color) 0px,
    var(--border-color) 10px,
    transparent 10px,
    transparent 20px
  );
  animation: roadMove 1s linear infinite;
}

@keyframes drive {
  0% { transform: translateX(-20px); }
  100% { transform: translateX(60px); }
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes roadMove {
  0% { background-position: 0px 0px; }
  100% { background-position: 20px 0px; }
}

/* 加载文本 */
.loading-text {
  font-size: 16px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.text-content {
  font-weight: 500;
}

.loading-dots span {
  animation: dotBlink 1.4s ease-in-out infinite both;
}

@keyframes dotBlink {
  0%, 80%, 100% {
    opacity: 0;
  }
  40% {
    opacity: 1;
  }
}

/* 进度条 */
.progress-bar {
  width: 200px;
  height: 4px;
  background: var(--border-light);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
  border-radius: 2px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 深色主题适配 */
.dark .loading-container.fullscreen {
  background: rgba(0, 0, 0, 0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-content {
    gap: 15px;
  }
  
  .progress-bar {
    width: 150px;
  }
  
  .loading-text {
    font-size: 14px;
  }
}
</style>