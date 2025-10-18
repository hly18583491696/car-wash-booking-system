<template>
  <div class="theme-toggle">
    <el-dropdown @command="handleThemeChange" placement="bottom-end">
      <el-button class="theme-button" circle>
        <el-icon><Brush /></el-icon>
      </el-button>
      
      <template #dropdown>
        <el-dropdown-menu class="theme-menu">
          <div class="theme-header">
            <h4>选择主题</h4>
          </div>
          <div class="theme-grid">
            <div 
              v-for="theme in themes" 
              :key="theme.name"
              class="theme-item"
              :class="{ active: currentTheme === theme.name }"
              @click="handleThemeChange(theme.name)"
            >
              <div 
                class="theme-color" 
                :style="{ backgroundColor: theme.color }"
              ></div>
              <span class="theme-label">{{ theme.label }}</span>
              <el-icon v-if="currentTheme === theme.name" class="theme-check">
                <Check />
              </el-icon>
            </div>
          </div>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useThemeStore } from '../stores/theme'

export default {
  name: 'ThemeToggle',
  setup() {
    const themeStore = useThemeStore()
    
    const currentTheme = computed(() => themeStore.currentTheme)
    const themes = computed(() => themeStore.themes)
    
    const handleThemeChange = (themeName) => {
      themeStore.setTheme(themeName)
    }
    
    return {
      currentTheme,
      themes,
      handleThemeChange
    }
  }
}
</script>

<style scoped>
.theme-toggle {
  display: inline-block;
}

.theme-button {
  background: var(--bg-light);
  border-color: var(--border-color);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.theme-button:hover {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.theme-menu {
  padding: 0;
  min-width: 280px;
}

.theme-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-light);
}

.theme-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.theme-grid {
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.theme-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.theme-item:hover {
  background: var(--bg-light);
}

.theme-item.active {
  background: var(--primary-light);
  color: var(--primary-color);
}

.theme-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex-shrink: 0;
}

.theme-label {
  font-size: 13px;
  flex: 1;
}

.theme-check {
  font-size: 14px;
  color: var(--primary-color);
}
</style>