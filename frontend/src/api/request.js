import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API基础地址
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 不需要token的接口列表
    const noAuthUrls = ['/auth/login', '/auth/register', '/auth/check-username', '/auth/check-phone', '/auth/check-email', '/test/health']
    
    // 检查当前请求是否需要添加token
    const needAuth = !noAuthUrls.some(url => config.url.includes(url))
    
    if (needAuth) {
      // 添加JWT token到请求头
      const token = localStorage.getItem('token')
      const tokenType = localStorage.getItem('tokenType') || 'Bearer'
      if (token && token.trim() !== '') {
        config.headers.Authorization = `${tokenType} ${token}`
      }
    }
    
    console.log('Request:', config.method?.toUpperCase(), config.url, config.data || config.params)
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('Response:', response.config.url, response.data)
    
    const { code, message, data } = response.data
    
    // 请求成功
    if (code === 200) {
      return response.data
    }
    
    // 请求失败，显示错误消息
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => {
    console.error('Response Error:', error)
    
    let message = '网络错误'
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          // 清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('tokenType')
          localStorage.removeItem('user')
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message === 'Network Error') {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request