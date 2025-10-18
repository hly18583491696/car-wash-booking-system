<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎回到汽车洗车服务预约系统</p>
        </div>
        
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名/手机号"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <el-link type="primary" @click="forgotPassword">忘记密码？</el-link>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>还没有账号？ <router-link to="/register">立即注册</router-link></p>
          <div class="quick-login">
            <p>快速登录</p>
            <div class="social-login">
              <el-button circle icon="ChatDotRound" @click="wechatLogin">微信</el-button>
              <el-button circle icon="Message" @click="smsLogin">短信</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 返回首页按钮 -->
    <div class="back-home">
      <router-link to="/">
        <el-button type="info" plain icon="ArrowLeft">返回首页</el-button>
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '../api/auth.js'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loginFormRef = ref()
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })
    
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      try {
        await loginFormRef.value.validate()
        loading.value = true
        
        // 调用登录API
        try {
          const response = await authApi.login(loginForm.username, loginForm.password)
          
          if (response.code === 200) {
            // 保存登录信息
            localStorage.setItem('token', response.data.token)
            localStorage.setItem('tokenType', response.data.tokenType || 'Bearer')
            localStorage.setItem('userRole', response.data.user.role)
            localStorage.setItem('userInfo', JSON.stringify(response.data.user))
            
            ElMessage.success(response.message)
            
            // 根据用户角色跳转
            if (response.data.user.role === 'admin') {
              router.push('/admin')
            } else {
              router.push('/')
            }
          } else {
            ElMessage.error(response.message)
          }
        } catch (error) {
          ElMessage.error(error.message || '登录失败，请重试')
        }
        
        loading.value = false
        
      } catch (error) {
        console.error('表单验证失败:', error)
        loading.value = false
      }
    }
    
    const forgotPassword = () => {
      ElMessage.info('忘记密码功能开发中...')
    }
    
    const wechatLogin = () => {
      ElMessage.info('微信登录功能开发中...')
    }
    
    const smsLogin = () => {
      ElMessage.info('短信登录功能开发中...')
    }
    
    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin,
      forgotPassword,
      wechatLogin,
      smsLogin
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: var(--text-primary);
  margin-bottom: 10px;
  font-size: 1.8rem;
}

.login-header p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.login-form {
  margin-bottom: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background: var(--primary-gradient);
  border: none;
}

.login-btn:hover {
  box-shadow: var(--shadow-primary);
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
}

.login-footer p {
  color: #666;
  margin-bottom: 20px;
}

.login-footer a {
  color: #409eff;
  text-decoration: none;
}

.quick-login {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.quick-login p {
  color: #999;
  font-size: 0.9rem;
  margin-bottom: 15px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.back-home {
  position: absolute;
  top: 20px;
  left: 20px;
}

@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 1.5rem;
  }
}
</style>