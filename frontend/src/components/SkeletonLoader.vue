<template>
  <div class="skeleton-loader" :class="{ 'dark-mode': isDark }">
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-title"></div>
        <div class="skeleton-text"></div>
        <div class="skeleton-text short"></div>
      </div>
    </div>
    
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in count" :key="i" class="skeleton-list-item">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-text"></div>
        </div>
      </div>
    </div>
    
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-header">
        <div v-for="i in columns" :key="i" class="skeleton-table-cell"></div>
      </div>
      <div v-for="i in rows" :key="i" class="skeleton-table-row">
        <div v-for="j in columns" :key="j" class="skeleton-table-cell"></div>
      </div>
    </div>
    
    <div v-else class="skeleton-text-block">
      <div v-for="i in count" :key="i" class="skeleton-text" :class="{ short: i === count }"></div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'SkeletonLoader',
  props: {
    type: {
      type: String,
      default: 'text',
      validator: value => ['text', 'card', 'list', 'table'].includes(value)
    },
    count: {
      type: Number,
      default: 3
    },
    rows: {
      type: Number,
      default: 5
    },
    columns: {
      type: Number,
      default: 4
    },
    dark: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const isDark = computed(() => {
      return props.dark || document.documentElement.getAttribute('data-theme') === 'dark'
    })
    
    return {
      isDark
    }
  }
}
</script>

<style scoped>
.skeleton-loader {
  --skeleton-color: #f0f0f0;
  --skeleton-highlight: #e0e0e0;
}

.skeleton-loader.dark-mode {
  --skeleton-color: #2a2a2a;
  --skeleton-highlight: #3a3a3a;
}

.skeleton-base {
  background: linear-gradient(90deg, var(--skeleton-color) 25%, var(--skeleton-highlight) 50%, var(--skeleton-color) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-card {
  border: 1px solid var(--skeleton-color);
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-image {
  @extend .skeleton-base;
  height: 200px;
  width: 100%;
}

.skeleton-content {
  padding: 16px;
}

.skeleton-title {
  @extend .skeleton-base;
  height: 20px;
  width: 70%;
  margin-bottom: 12px;
}

.skeleton-text {
  @extend .skeleton-base;
  height: 16px;
  width: 100%;
  margin-bottom: 8px;
}

.skeleton-text.short {
  width: 60%;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--skeleton-color);
}

.skeleton-list-item:last-child {
  border-bottom: none;
}

.skeleton-avatar {
  @extend .skeleton-base;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  flex-shrink: 0;
}

.skeleton-list-item .skeleton-content {
  flex: 1;
  padding: 0;
}

.skeleton-list-item .skeleton-title {
  width: 40%;
  margin-bottom: 8px;
}

.skeleton-list-item .skeleton-text {
  width: 80%;
  margin-bottom: 0;
}

.skeleton-table {
  border: 1px solid var(--skeleton-color);
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-table-header {
  display: flex;
  background: var(--skeleton-highlight);
  padding: 12px;
}

.skeleton-table-row {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid var(--skeleton-color);
}

.skeleton-table-row:last-child {
  border-bottom: none;
}

.skeleton-table-cell {
  @extend .skeleton-base;
  height: 16px;
  flex: 1;
  margin-right: 12px;
}

.skeleton-table-cell:last-child {
  margin-right: 0;
}

.skeleton-text-block .skeleton-text {
  @extend .skeleton-base;
}

/* 扩展skeleton-base样式 */
.skeleton-image,
.skeleton-title,
.skeleton-text,
.skeleton-avatar,
.skeleton-table-cell {
  background: linear-gradient(90deg, var(--skeleton-color) 25%, var(--skeleton-highlight) 50%, var(--skeleton-color) 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>