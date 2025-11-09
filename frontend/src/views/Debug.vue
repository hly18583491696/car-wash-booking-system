<template>
  <div class="debug-page">
    <h1>调试页面</h1>
    
    <div class="section">
      <h2>LocalStorage 状态</h2>
      <div class="info-box">
        <p><strong>Token:</strong> {{ tokenInfo.token }}</p>
        <p><strong>Token Type:</strong> {{ tokenInfo.tokenType }}</p>
        <p><strong>Token Length:</strong> {{ tokenInfo.tokenLength }}</p>
        <p><strong>User Info:</strong> {{ tokenInfo.userInfo }}</p>
      </div>
      <button @click="refreshTokenInfo">刷新Token信息</button>
    </div>

    <div class="section">
      <h2>API 测试</h2>
      <div class="api-test">
        <button @click="testLogin">测试登录</button>
        <button @click="testUserInfo">测试用户信息</button>
        <button @click="testUserInfoFetch">测试用户信息(Fetch)</button>
        <button @click="testOrdersAPI">测试订单API</button>
        <button @click="clearStorage">清空存储</button>
      </div>
      
      <div class="result-box" v-if="apiResult">
        <h3>API 结果:</h3>
        <pre>{{ apiResult }}</pre>
      </div>

      <div v-if="ordersResult" class="result-section">
        <h4>订单API 测试结果:</h4>
        <pre>{{ JSON.stringify(ordersResult, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { realApi } from '@/api/realApi'

const tokenInfo = ref({
  token: '',
  tokenType: '',
  tokenLength: 0,
  userInfo: ''
})

const apiResult = ref('')
const ordersResult = ref(null)

const refreshTokenInfo = () => {
  const token = localStorage.getItem('token') || ''
  const tokenType = localStorage.getItem('tokenType') || ''
  const userInfo = localStorage.getItem('userInfo') || ''
  
  tokenInfo.value = {
    token: token ? `${token.substring(0, 50)}...` : '无',
    tokenType,
    tokenLength: token.length,
    userInfo: userInfo ? JSON.parse(userInfo) : '无'
  }
}

const testLogin = async () => {
  try {
    console.log('🔍 开始测试登录...')
    const response = await realApi.login('admin', 'admin123')
    console.log('📥 登录响应:', response)
    apiResult.value = JSON.stringify(response, null, 2)
    refreshTokenInfo()
  } catch (error) {
    console.error('❌ 登录测试失败:', error)
    apiResult.value = `错误: ${error.message}`
  }
}

const testUserInfo = async () => {
  try {
    console.log('🔍 开始测试用户信息...')
    const response = await realApi.getUserInfo()
    console.log('📥 用户信息响应:', response)
    apiResult.value = JSON.stringify(response, null, 2)
  } catch (error) {
    console.error('❌ 用户信息测试失败:', error)
    apiResult.value = `错误: ${error.message}\n状态: ${error.response?.status}\n数据: ${JSON.stringify(error.response?.data, null, 2)}`
  }
}

const testUserInfoFetch = async () => {
  try {
    console.log('🔍 开始测试用户信息(Fetch)...')
    const token = localStorage.getItem('token')
    const tokenType = localStorage.getItem('tokenType') || 'Bearer'
    
    console.log('🔑 使用Token:', token ? `${token.substring(0, 20)}...` : '无')
    
    const response = await fetch('http://localhost:8080/api/user/info', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `${tokenType} ${token}`
      }
    })
    
    console.log('📥 Fetch响应状态:', response.status)
    const data = await response.json()
    console.log('📥 Fetch响应数据:', data)
    apiResult.value = `状态: ${response.status}\n数据: ${JSON.stringify(data, null, 2)}`
  } catch (error) {
    console.error('❌ Fetch测试失败:', error)
    apiResult.value = `Fetch错误: ${error.message}`
  }
}

const testOrdersAPI = async () => {
  try {
    const token = localStorage.getItem('token')
    const tokenType = localStorage.getItem('tokenType') || 'Bearer'
    
    console.log('🔍 测试订单API...')
    
    const response = await fetch('http://localhost:8080/api/orders', {
      method: 'GET',
      headers: {
        'Authorization': `${tokenType} ${token}`,
        'Content-Type': 'application/json'
      }
    })
    
    console.log('订单API响应状态:', response.status)
    const data = await response.json()
    console.log('订单数据:', data)
    
    ordersResult.value = {
       status: response.status,
       data: data
     }
   } catch (error) {
     console.error('❌ 订单API测试失败:', error)
     ordersResult.value = {
       error: error.message
     }
  }
}

const clearStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('tokenType')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userRole')
  refreshTokenInfo()
  apiResult.value = '存储已清空'
}

onMounted(() => {
  refreshTokenInfo()
})
</script>

<style scoped>
.debug-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.info-box {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.info-box p {
  margin: 5px 0;
  word-break: break-all;
}

.api-test {
  margin-bottom: 20px;
}

.api-test button {
  margin-right: 10px;
  margin-bottom: 10px;
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.api-test button:hover {
  background: #66b1ff;
}

.result-box {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.result-box pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>