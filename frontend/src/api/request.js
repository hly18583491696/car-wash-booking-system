import axios from 'axios'
import { ElMessage } from 'element-plus'
import API_CONFIG from '@/config/api.js'

// 创建axios实例
const request = axios.create({
  baseURL: API_CONFIG.BASE_URL, // 后端API基础地址
  timeout: API_CONFIG.TIMEOUT, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 不需要token的接口列表
    const noAuthUrls = API_CONFIG.NO_AUTH_URLS
    
    // 检查当前请求是否需要添加token
    const needAuth = !noAuthUrls.some(url => config.url.includes(url))
    
    // console.log('🔍 请求拦截器详情:', {
    //   url: config.url,
    //   noAuthUrls: noAuthUrls,
    //   needAuth: needAuth,
    //   matchedUrls: noAuthUrls.filter(url => config.url.includes(url))
    // })
    
    if (needAuth) {
      // 添加JWT token到请求头
      const token = localStorage.getItem('token')
      const tokenType = localStorage.getItem('tokenType') || 'Bearer'
      
      console.log('🔑 Token状态:', {
        hasToken: !!token,
        tokenLength: token?.length || 0,
        tokenType: tokenType,
        tokenPreview: token ? `${token.substring(0, 20)}...` : '无'
      })
      
      if (token && token.trim() !== '') {
        config.headers.Authorization = `${tokenType} ${token}`
        console.log('✅ 已添加Authorization头:', `${tokenType} ${token.substring(0, 20)}...`)
      } else {
        console.warn('⚠️ 需要认证但未找到有效token')
      }
    }
    
    console.log('📤 最终请求配置:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      headers: {
        ...config.headers,
        Authorization: config.headers.Authorization ? `${config.headers.Authorization.substring(0, 30)}...` : '无'
      },
      data: config.data,
      params: config.params
    })
    
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
    const method = response.config.method?.toUpperCase()
    const url = response.config.url
    
    console.log(`📥 [响应拦截器] ${method} ${url}`, {
      status: response.status,
      statusText: response.statusText,
      data: response.data
    })
    
    const { code, message, data } = response.data
    
    // 请求成功
    if (code === 200) {
      console.log(`✅ [响应成功] ${method} ${url}`, response.data)
      return response.data
    }
    
    // 请求失败，显示错误消息
    console.error(`❌ [响应失败] ${method} ${url}, code: ${code}, message: ${message}`)
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => {
    const method = error.config?.method?.toUpperCase()
    const url = error.config?.url
    
    console.error(`❌ [响应错误] ${method} ${url}`, {
      message: error.message,
      status: error.response?.status,
      data: error.response?.data
    })
    
    let message = '网络错误'
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          localStorage.removeItem('token')
          localStorage.removeItem('tokenType')
          localStorage.removeItem('user')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('userRole')
          const current = window.location.pathname + window.location.search
          if (!current.includes('/login')) {
            window.location.href = `/login?redirect=${encodeURIComponent(current)}`
          }
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