<template>
  <div class="particle-background" ref="particleContainer">
    <canvas ref="canvas" class="particle-canvas"></canvas>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'ParticleBackground',
  setup() {
    const canvas = ref(null)
    const particleContainer = ref(null)
    let animationId = null
    let particles = []
    let ctx = null
    
    class Particle {
      constructor(x, y) {
        this.x = x
        this.y = y
        this.size = Math.random() * 3 + 1
        this.speedX = Math.random() * 3 - 1.5
        this.speedY = Math.random() * 3 - 1.5
        this.opacity = Math.random() * 0.5 + 0.2
        this.life = Math.random() * 100 + 50
        this.maxLife = this.life
      }
      
      update() {
        this.x += this.speedX
        this.y += this.speedY
        this.life--
        this.opacity = (this.life / this.maxLife) * 0.7
        
        // 边界检测
        if (this.x < 0 || this.x > canvas.value.width) this.speedX *= -1
        if (this.y < 0 || this.y > canvas.value.height) this.speedY *= -1
      }
      
      draw() {
        ctx.save()
        ctx.globalAlpha = this.opacity
        ctx.beginPath()
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
        ctx.fillStyle = '#ffffff'
        ctx.fill()
        ctx.restore()
      }
    }
    
    const initCanvas = () => {
      if (!canvas.value || !particleContainer.value) return
      
      const container = particleContainer.value
      canvas.value.width = container.offsetWidth
      canvas.value.height = container.offsetHeight
      ctx = canvas.value.getContext('2d')
      
      // 创建初始粒子
      particles = []
      for (let i = 0; i < 50; i++) {
        particles.push(new Particle(
          Math.random() * canvas.value.width,
          Math.random() * canvas.value.height
        ))
      }
    }
    
    const animate = () => {
      if (!ctx || !canvas.value) return
      
      ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
      
      // 更新和绘制粒子
      particles.forEach((particle, index) => {
        particle.update()
        particle.draw()
        
        // 移除生命周期结束的粒子
        if (particle.life <= 0) {
          particles.splice(index, 1)
          // 添加新粒子
          particles.push(new Particle(
            Math.random() * canvas.value.width,
            Math.random() * canvas.value.height
          ))
        }
      })
      
      // 绘制连接线
      particles.forEach((particle, i) => {
        particles.slice(i + 1).forEach(otherParticle => {
          const distance = Math.sqrt(
            Math.pow(particle.x - otherParticle.x, 2) +
            Math.pow(particle.y - otherParticle.y, 2)
          )
          
          if (distance < 100) {
            ctx.save()
            ctx.globalAlpha = (100 - distance) / 100 * 0.2
            ctx.beginPath()
            ctx.moveTo(particle.x, particle.y)
            ctx.lineTo(otherParticle.x, otherParticle.y)
            ctx.strokeStyle = '#ffffff'
            ctx.lineWidth = 1
            ctx.stroke()
            ctx.restore()
          }
        })
      })
      
      animationId = requestAnimationFrame(animate)
    }
    
    const handleResize = () => {
      initCanvas()
    }
    
    onMounted(() => {
      initCanvas()
      animate()
      window.addEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      if (animationId) {
        cancelAnimationFrame(animationId)
      }
      window.removeEventListener('resize', handleResize)
    })
    
    return {
      canvas,
      particleContainer
    }
  }
}
</script>

<style scoped>
.particle-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
</style>