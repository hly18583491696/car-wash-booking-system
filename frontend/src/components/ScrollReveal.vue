<template>
  <div 
    class="scroll-reveal" 
    :class="[
      `reveal-${animation}`,
      { 'is-visible': isVisible, 'reveal-once': once }
    ]"
    ref="revealElement"
  >
    <slot />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'ScrollReveal',
  props: {
    animation: {
      type: String,
      default: 'fadeInUp',
      validator: (value) => [
        'fadeIn', 'fadeInUp', 'fadeInDown', 'fadeInLeft', 'fadeInRight',
        'slideInUp', 'slideInDown', 'slideInLeft', 'slideInRight',
        'zoomIn', 'zoomInUp', 'zoomInDown', 'rotateIn', 'flipInX', 'flipInY'
      ].includes(value)
    },
    delay: {
      type: Number,
      default: 0
    },
    duration: {
      type: Number,
      default: 600
    },
    offset: {
      type: Number,
      default: 100
    },
    once: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const revealElement = ref(null)
    const isVisible = ref(false)
    const observer = ref(null)
    
    const handleIntersection = (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          setTimeout(() => {
            isVisible.value = true
          }, props.delay)
          
          if (props.once && observer.value) {
            observer.value.unobserve(entry.target)
          }
        } else if (!props.once) {
          isVisible.value = false
        }
      })
    }
    
    onMounted(() => {
      if (revealElement.value) {
        observer.value = new IntersectionObserver(handleIntersection, {
          threshold: 0.1,
          rootMargin: `0px 0px -${props.offset}px 0px`
        })
        
        observer.value.observe(revealElement.value)
        
        // 设置动画持续时间
        revealElement.value.style.setProperty('--animation-duration', `${props.duration}ms`)
      }
    })
    
    onUnmounted(() => {
      if (observer.value) {
        observer.value.disconnect()
      }
    })
    
    return {
      revealElement,
      isVisible
    }
  }
}
</script>

<style scoped>
.scroll-reveal {
  --animation-duration: 600ms;
  opacity: 0;
  transition: all var(--animation-duration) cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.scroll-reveal.is-visible {
  opacity: 1;
}

/* Fade animations */
.reveal-fadeIn.is-visible {
  opacity: 1;
}

.reveal-fadeInUp {
  transform: translateY(30px);
}

.reveal-fadeInUp.is-visible {
  transform: translateY(0);
}

.reveal-fadeInDown {
  transform: translateY(-30px);
}

.reveal-fadeInDown.is-visible {
  transform: translateY(0);
}

.reveal-fadeInLeft {
  transform: translateX(-30px);
}

.reveal-fadeInLeft.is-visible {
  transform: translateX(0);
}

.reveal-fadeInRight {
  transform: translateX(30px);
}

.reveal-fadeInRight.is-visible {
  transform: translateX(0);
}

/* Slide animations */
.reveal-slideInUp {
  transform: translateY(50px);
}

.reveal-slideInUp.is-visible {
  transform: translateY(0);
}

.reveal-slideInDown {
  transform: translateY(-50px);
}

.reveal-slideInDown.is-visible {
  transform: translateY(0);
}

.reveal-slideInLeft {
  transform: translateX(-50px);
}

.reveal-slideInLeft.is-visible {
  transform: translateX(0);
}

.reveal-slideInRight {
  transform: translateX(50px);
}

.reveal-slideInRight.is-visible {
  transform: translateX(0);
}

/* Zoom animations */
.reveal-zoomIn {
  transform: scale(0.8);
}

.reveal-zoomIn.is-visible {
  transform: scale(1);
}

.reveal-zoomInUp {
  transform: scale(0.8) translateY(30px);
}

.reveal-zoomInUp.is-visible {
  transform: scale(1) translateY(0);
}

.reveal-zoomInDown {
  transform: scale(0.8) translateY(-30px);
}

.reveal-zoomInDown.is-visible {
  transform: scale(1) translateY(0);
}

/* Rotate animation */
.reveal-rotateIn {
  transform: rotate(-10deg) scale(0.9);
}

.reveal-rotateIn.is-visible {
  transform: rotate(0deg) scale(1);
}

/* Flip animations */
.reveal-flipInX {
  transform: perspective(400px) rotateX(-90deg);
}

.reveal-flipInX.is-visible {
  transform: perspective(400px) rotateX(0deg);
}

.reveal-flipInY {
  transform: perspective(400px) rotateY(-90deg);
}

.reveal-flipInY.is-visible {
  transform: perspective(400px) rotateY(0deg);
}

/* 减少动画效果（用户偏好） */
@media (prefers-reduced-motion: reduce) {
  .scroll-reveal {
    transition: opacity 0.3s ease;
    transform: none !important;
  }
}
</style>