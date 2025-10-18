import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref('blue')
  
  // 可用主题列表
  const themes = ref([
    { name: 'blue', label: '经典蓝', color: '#1890ff' },
    { name: 'green', label: '自然绿', color: '#52c41a' },
    { name: 'purple', label: '优雅紫', color: '#722ed1' },
    { name: 'orange', label: '活力橙', color: '#fa8c16' },
    { name: 'red', label: '热情红', color: '#f5222d' },
    { name: 'cyan', label: '清新青', color: '#13c2c2' },
    { name: 'pink', label: '温馨粉', color: '#eb2f96' },
    { name: 'gold', label: '尊贵金', color: '#faad14' }
  ])
  
  // 初始化主题
  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme && themes.value.find(t => t.name === savedTheme)) {
      currentTheme.value = savedTheme
    }
    applyTheme(currentTheme.value)
  }
  
  // 切换主题
  const setTheme = (themeName) => {
    if (themes.value.find(t => t.name === themeName)) {
      currentTheme.value = themeName
      localStorage.setItem('theme', themeName)
      applyTheme(themeName)
    }
  }
  
  // 应用主题到DOM
  const applyTheme = (themeName) => {
    document.documentElement.setAttribute('data-theme', themeName)
  }
  
  // 获取当前主题信息
  const getCurrentThemeInfo = () => {
    return themes.value.find(t => t.name === currentTheme.value)
  }
  
  return {
    currentTheme,
    themes,
    initTheme,
    setTheme,
    getCurrentThemeInfo
  }
})