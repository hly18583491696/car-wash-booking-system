import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './assets/css/themes.css'
import './assets/css/global.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())

// 初始化 userStore，从 localStorage 恢复用户信息
const userStore = useUserStore()
const storedUserInfo = localStorage.getItem('userInfo')
if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
  try {
    userStore.setUserInfo(JSON.parse(storedUserInfo))
  } catch (e) {
    console.error('恢复用户信息失败:', e)
  }
}

app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')