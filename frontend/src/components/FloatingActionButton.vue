<template>
  <div class="fab-container" :class="{ 'fab-open': isOpen }">
    <!-- 主按钮 -->
    <button 
      class="fab-main" 
      @click="toggleFab"
      :style="{ background: mainColor }"
    >
      <el-icon class="fab-icon" :class="{ 'fab-rotate': isOpen }">
        <component :is="isOpen ? 'Close' : mainIcon" />
      </el-icon>
    </button>
    
    <!-- 子按钮 -->
    <transition-group name="fab-item" tag="div" class="fab-items">
      <button
        v-for="(item, index) in items"
        :key="item.id"
        class="fab-item"
        :style="{ 
          background: item.color || '#ffffff',
          transitionDelay: `${index * 50}ms`,
          transform: isOpen ? `translateY(-${(index + 1) * 60}px)` : 'translateY(0)'
        }"
        @click="handleItemClick(item)"
        v-show="isOpen"
      >
        <el-icon>
          <component :is="item.icon" />
        </el-icon>
        <span class="fab-tooltip">{{ item.label }}</span>
      </button>
    </transition-group>
    
    <!-- 背景遮罩 -->
    <div 
      v-if="isOpen" 
      class="fab-backdrop" 
      @click="closeFab"
    ></div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'FloatingActionButton',
  props: {
    mainIcon: {
      type: String,
      default: 'Plus'
    },
    mainColor: {
      type: String,
      default: 'var(--primary-color)'
    },
    items: {
      type: Array,
      default: () => []
    }
  },
  emits: ['item-click'],
  setup(props, { emit }) {
    const isOpen = ref(false)
    
    const toggleFab = () => {
      isOpen.value = !isOpen.value
    }
    
    const closeFab = () => {
      isOpen.value = false
    }
    
    const handleItemClick = (item) => {
      emit('item-click', item)
      closeFab()
    }
    
    return {
      isOpen,
      toggleFab,
      closeFab,
      handleItemClick
    }
  }
}
</script>

<style scoped>
.fab-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.fab-main {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1001;
}

.fab-main:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.fab-main:active {
  transform: scale(0.95);
}

.fab-icon {
  font-size: 24px;
  color: white;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fab-rotate {
  transform: rotate(45deg);
}

.fab-items {
  position: absolute;
  bottom: 0;
  right: 0;
}

.fab-item {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  bottom: 4px;
  right: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: translateY(0) scale(0);
}

.fab-open .fab-item {
  opacity: 1;
  transform: scale(1);
}

.fab-item:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.fab-item .el-icon {
  font-size: 20px;
  color: var(--text-primary);
}

.fab-tooltip {
  position: absolute;
  right: 60px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.fab-item:hover .fab-tooltip {
  opacity: 1;
}

.fab-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.3);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

/* 动画 */
.fab-item-enter-active,
.fab-item-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fab-item-enter-from {
  opacity: 0;
  transform: translateY(0) scale(0);
}

.fab-item-leave-to {
  opacity: 0;
  transform: translateY(0) scale(0);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .fab-container {
    bottom: 20px;
    right: 20px;
  }
  
  .fab-main {
    width: 48px;
    height: 48px;
  }
  
  .fab-icon {
    font-size: 20px;
  }
  
  .fab-item {
    width: 40px;
    height: 40px;
  }
  
  .fab-item .el-icon {
    font-size: 16px;
  }
}
</style>