<template>
  <div class="admin-test">
    <h1>管理员测试页面</h1>
    <p>当前用户: {{ userInfo.username }}</p>
    <p>用户角色: {{ userInfo.role }}</p>
    <p>认证状态: {{ isAuthenticated ? '已认证' : '未认证' }}</p>
    <p>管理员权限: {{ isAdmin ? '是' : '否' }}</p>
    
    <el-button @click="testAdminScript" type="primary">测试AdminScript</el-button>
    <el-button @click="goToAdmin" type="success">跳转到Admin页面</el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AuthManager from '../utils/auth.js'

const router = useRouter()
const userInfo = ref({})
const isAuthenticated = ref(false)
const isAdmin = ref(false)

onMounted(() => {
  try {
    userInfo.value = AuthManager.getCurrentUser() || {}
    isAuthenticated.value = AuthManager.isAuthenticated()
    isAdmin.value = AuthManager.isAdmin()
    
    console.log('用户信息:', userInfo.value)
    console.log('认证状态:', isAuthenticated.value)
    console.log('管理员权限:', isAdmin.value)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
})

const testAdminScript = async () => {
  try {
    const { useAdminDashboard } = await import('./AdminScript.js')
    const adminDashboard = useAdminDashboard()
    console.log('AdminScript加载成功:', adminDashboard)
    ElMessage.success('AdminScript加载成功')
  } catch (error) {
    console.error('AdminScript加载失败:', error)
    ElMessage.error('AdminScript加载失败: ' + error.message)
  }
}

const goToAdmin = () => {
  router.push('/admin/dashboard')
}
</script>

<style scoped>
.admin-test {
  padding: 20px;
}
</style>