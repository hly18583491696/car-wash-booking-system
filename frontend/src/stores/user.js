import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
  }
  
  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
  }
  
  // 检查是否已登录
  const isLoggedIn = () => {
    return !!token.value && !!userInfo.value
  }
  
  return {
    userInfo,
    token,
    setUserInfo,
    setToken,
    clearUserInfo,
    isLoggedIn
  }
})