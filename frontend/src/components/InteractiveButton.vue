<template>
  <button 
    class="interactive-button"
    :class="[
      `btn-${type}`,
      `btn-${size}`,
      {
        'btn-loading': loading,
        'btn-disabled': disabled,
        'btn-block': block,
        'btn-rounded': rounded,
        'btn-gradient': gradient,
        'btn-glow': glow,
        'btn-ripple': ripple
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
    @mousedown="handleMouseDown"
    ref="buttonRef"
  >
    <!-- 涟漪效果 -->
    <span 
      v-if="ripple && rippleStyle" 
      class="ripple-effect" 
      :style="rippleStyle"
    ></span>
    
    <!-- 加载动画 -->
    <span v-if="loading" class="loading-spinner">
      <svg viewBox="0 0 24 24" class="spinner">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-dasharray="31.416" stroke-dashoffset="31.416">
          <animate attributeName="stroke-dasharray" dur="2s" values="0 31.416;15.708 15.708;0 31.416" repeatCount="indefinite"/>
          <animate attributeName="stroke-dashoffset" dur="2s" values="0;-15.708;-31.416" repeatCount="indefinite"/>
        </circle>
      </svg>
    </span>
    
    <!-- 图标 -->
    <span v-if="icon && !loading" class="btn-icon" :class="{ 'icon-right': iconPosition === 'right' }">
      <el-icon :size="iconSize">
        <component :is="icon" />
      </el-icon>
    </span>
    
    <!-- 按钮文本 -->
    <span class="btn-text" v-if="$slots.default">
      <slot />
    </span>
    
    <!-- 徽章 -->
    <span v-if="badge" class="btn-badge">{{ badge }}</span>
    
    <!-- 悬浮提示 -->
    <div v-if="tooltip" class="btn-tooltip">{{ tooltip }}</div>
  </button>
</template>

<script>
import { ref, computed } from 'vue'

export default {
  name: 'InteractiveButton',
  props: {
    type: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'success', 'warning', 'danger', 'info', 'text'].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    loading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    block: {
      type: Boolean,
      default: false
    },
    rounded: {
      type: Boolean,
      default: false
    },
    gradient: {
      type: Boolean,
      default: false
    },
    glow: {
      type: Boolean,
      default: false
    },
    ripple: {
      type: Boolean,
      default: true
    },
    icon: {
      type: String,
      default: ''
    },
    iconPosition: {
      type: String,
      default: 'left',
      validator: (value) => ['left', 'right'].includes(value)
    },
    iconSize: {
      type: Number,
      default: 16
    },
    badge: {
      type: [String, Number],
      default: ''
    },
    tooltip: {
      type: String,
      default: ''
    }
  },
  emits: ['click'],
  setup(props, { emit }) {
    const buttonRef = ref(null)
    const rippleStyle = ref(null)
    
    const handleClick = (event) => {
      if (props.disabled || props.loading) return
      emit('click', event)
    }
    
    const handleMouseDown = (event) => {
      if (!props.ripple || props.disabled || props.loading) return
      
      const button = buttonRef.value
      const rect = button.getBoundingClientRect()
      const size = Math.max(rect.width, rect.height)
      const x = event.clientX - rect.left - size / 2
      const y = event.clientY - rect.top - size / 2
      
      rippleStyle.value = {
        width: `${size}px`,
        height: `${size}px`,
        left: `${x}px`,
        top: `${y}px`
      }
      
      setTimeout(() => {
        rippleStyle.value = null
      }, 600)
    }
    
    return {
      buttonRef,
      rippleStyle,
      handleClick,
      handleMouseDown
    }
  }
}
</script>

<style scoped>
.interactive-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  user-select: none;
  outline: none;
  text-decoration: none;
  white-space: nowrap;
}

/* 尺寸变体 */
.btn-small {
  padding: 8px 16px;
  font-size: 12px;
  gap: 6px;
}

.btn-medium {
  padding: 12px 24px;
  font-size: 14px;
  gap: 8px;
}

.btn-large {
  padding: 16px 32px;
  font-size: 16px;
  gap: 10px;
}

/* 类型变体 */
.btn-primary {
  background: var(--primary-color);
  color: var(--text-white);
  box-shadow: 0 2px 8px rgba(var(--primary-rgb), 0.3);
}

.btn-primary:hover:not(.btn-disabled) {
  background: var(--primary-dark);
  box-shadow: 0 4px 16px rgba(var(--primary-rgb), 0.4);
  transform: translateY(-2px);
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover:not(.btn-disabled) {
  background: var(--bg-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.btn-success {
  background: var(--success-color);
  color: var(--text-white);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.btn-success:hover:not(.btn-disabled) {
  background: #16a34a;
  transform: translateY(-2px);
}

.btn-warning {
  background: var(--warning-color);
  color: var(--text-white);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.btn-warning:hover:not(.btn-disabled) {
  background: #d97706;
  transform: translateY(-2px);
}

.btn-danger {
  background: var(--error-color);
  color: var(--text-white);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.btn-danger:hover:not(.btn-disabled) {
  background: #dc2626;
  transform: translateY(-2px);
}

.btn-info {
  background: #3b82f6;
  color: var(--text-white);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.btn-info:hover:not(.btn-disabled) {
  background: #2563eb;
  transform: translateY(-2px);
}

.btn-text {
  background: transparent;
  color: var(--primary-color);
  box-shadow: none;
}

.btn-text:hover:not(.btn-disabled) {
  background: var(--primary-light);
  color: var(--primary-dark);
}

/* 修饰符 */
.btn-block {
  width: 100%;
}

.btn-rounded {
  border-radius: var(--radius-full);
}

.btn-gradient.btn-primary {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
}

.btn-gradient.btn-success {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.btn-gradient.btn-warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.btn-gradient.btn-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.btn-glow {
  box-shadow: 0 0 20px rgba(var(--primary-rgb), 0.5);
}

.btn-glow:hover:not(.btn-disabled) {
  box-shadow: 0 0 30px rgba(var(--primary-rgb), 0.7);
}

/* 状态 */
.btn-loading {
  pointer-events: none;
}

.btn-disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 图标 */
.btn-icon {
  display: flex;
  align-items: center;
}

.btn-icon.icon-right {
  order: 1;
}

.btn-text {
  flex: 1;
}

/* 加载动画 */
.loading-spinner {
  display: flex;
  align-items: center;
}

.spinner {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 涟漪效果 */
.ripple-effect {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 徽章 */
.btn-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--error-color);
  color: var(--text-white);
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: var(--radius-full);
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 提示框 */
.btn-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: var(--text-dark);
  color: var(--text-white);
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  pointer-events: none;
  margin-bottom: 8px;
}

.btn-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 4px solid transparent;
  border-top-color: var(--text-dark);
}

.interactive-button:hover .btn-tooltip {
  opacity: 1;
  visibility: visible;
}

/* 焦点状态 */
.interactive-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* 激活状态 */
.interactive-button:active:not(.btn-disabled) {
  transform: scale(0.98);
}

/* 深色主题适配 */
.dark .btn-secondary {
  background: var(--bg-dark);
  border-color: var(--border-dark);
  color: var(--text-light);
}

.dark .btn-secondary:hover:not(.btn-disabled) {
  background: var(--bg-secondary);
}

.dark .btn-text {
  color: var(--primary-light);
}

.dark .btn-text:hover:not(.btn-disabled) {
  background: rgba(var(--primary-rgb), 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .btn-large {
    padding: 14px 28px;
    font-size: 15px;
  }
  
  .btn-medium {
    padding: 10px 20px;
    font-size: 13px;
  }
  
  .btn-small {
    padding: 6px 12px;
    font-size: 11px;
  }
}

/* 减少动画效果（用户偏好） */
@media (prefers-reduced-motion: reduce) {
  .interactive-button {
    transition: none;
  }
  
  .interactive-button:hover:not(.btn-disabled) {
    transform: none;
  }
  
  .ripple-effect {
    display: none;
  }
}
</style>