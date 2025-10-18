<template>
  <transition name="fade">
    <div 
      v-show="visible" 
      class="back-to-top"
      @click="scrollToTop"
    >
      <el-icon size="20">
        <ArrowUp />
      </el-icon>
    </div>
  </transition>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'BackToTop',
  setup() {
    const visible = ref(false)
    
    const handleScroll = () => {
      visible.value = window.scrollY > 300
    }
    
    const scrollToTop = () => {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    }
    
    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
    })
    
    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })
    
    return {
      visible,
      scrollToTop
    }
  }
}
</script>

<style scoped>
.back-to-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  background: var(--primary-gradient);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-white);
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-normal);
  z-index: 999;
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-primary);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .back-to-top {
    bottom: 20px;
    right: 20px;
    width: 45px;
    height: 45px;
  }
}
</style>