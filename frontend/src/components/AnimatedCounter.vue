<template>
  <span class="animated-counter">{{ displayValue }}</span>
</template>

<script>
import { ref, watch, onMounted } from 'vue'

export default {
  name: 'AnimatedCounter',
  props: {
    value: {
      type: [Number, String],
      required: true
    },
    duration: {
      type: Number,
      default: 2000
    },
    prefix: {
      type: String,
      default: ''
    },
    suffix: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const displayValue = ref('0')
    let animationId = null
    
    const animateValue = (start, end, duration) => {
      const startTime = performance.now()
      const isNumber = !isNaN(parseFloat(end))
      
      const animate = (currentTime) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)
        
        // 使用缓动函数
        const easeOutQuart = 1 - Math.pow(1 - progress, 4)
        
        if (isNumber) {
          const current = start + (parseFloat(end) - start) * easeOutQuart
          displayValue.value = props.prefix + Math.floor(current).toLocaleString() + props.suffix
        } else {
          displayValue.value = props.prefix + end + props.suffix
        }
        
        if (progress < 1) {
          animationId = requestAnimationFrame(animate)
        }
      }
      
      animationId = requestAnimationFrame(animate)
    }
    
    const startAnimation = () => {
      if (animationId) {
        cancelAnimationFrame(animationId)
      }
      
      const currentValue = displayValue.value.replace(props.prefix, '').replace(props.suffix, '').replace(/,/g, '')
      const startValue = isNaN(parseFloat(currentValue)) ? 0 : parseFloat(currentValue)
      
      animateValue(startValue, props.value, props.duration)
    }
    
    watch(() => props.value, startAnimation)
    
    onMounted(() => {
      startAnimation()
    })
    
    return {
      displayValue
    }
  }
}
</script>

<style scoped>
.animated-counter {
  display: inline-block;
  font-variant-numeric: tabular-nums;
}
</style>