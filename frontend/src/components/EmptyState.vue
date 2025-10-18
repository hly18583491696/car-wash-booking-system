<template>
  <div class="empty-state">
    <div class="empty-illustration">
      <el-icon :size="iconSize" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    
    <div class="empty-content">
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-description">{{ description }}</p>
      
      <div v-if="showAction" class="empty-actions">
        <slot name="actions">
          <el-button type="primary" @click="handleAction">
            {{ actionText }}
          </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: {
      type: String,
      default: 'Box'
    },
    iconSize: {
      type: Number,
      default: 80
    },
    iconColor: {
      type: String,
      default: 'var(--text-light)'
    },
    title: {
      type: String,
      default: '暂无数据'
    },
    description: {
      type: String,
      default: '当前没有可显示的内容'
    },
    showAction: {
      type: Boolean,
      default: false
    },
    actionText: {
      type: String,
      default: '刷新'
    }
  },
  emits: ['action'],
  setup(props, { emit }) {
    const handleAction = () => {
      emit('action')
    }
    
    return {
      handleAction
    }
  }
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  min-height: 300px;
}

.empty-illustration {
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-content {
  max-width: 400px;
}

.empty-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.empty-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 24px;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .empty-state {
    padding: 40px 16px;
  }
  
  .empty-title {
    font-size: 1.1rem;
  }
  
  .empty-actions .el-button {
    width: 100%;
  }
}
</style>