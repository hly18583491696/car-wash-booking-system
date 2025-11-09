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
import AuthManager from '../utils/auth.js'

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
        
        console.log('开始登录流程...')
        
        // 调用登录API
        try {
          const response = await authApi.login(loginForm.username, loginForm.password)
          
          console.log('登录API响应:', response)
          
          if (response.code === 200) {
            // 使用AuthManager处理登录
            const loginSuccess = AuthManager.login(
              response.data.user,
              response.data.token,
              response.data.tokenType
            )
            
            if (loginSuccess) {
              console.log('登录成功，用户信息:', {
                role: AuthManager.getUserRole(),
                name: AuthManager.getUserDisplayName(),
                isAdmin: AuthManager.isAdmin()
              })
              
              // 根据用户角色确定跳转路径
              const userRole = response.data.user.role
              let redirectPath = router.currentRoute.value.query.redirect
              
              console.log('用户角色:', userRole)
              
              // 显示登录成功消息
              if (userRole === 'admin') {
                console.log('检测到管理员角色，开始管理员登录流程')
                console.log('用户信息:', response.data.user)
                console.log('当前路由:', router.currentRoute.value)
                console.log('AuthManager状态:', {
                  isAuthenticated: AuthManager.isAuthenticated(),
                  isAdmin: AuthManager.isAdmin(),
                  currentUser: AuthManager.getCurrentUser()
                })
                
                ElMessage.success({
                  message: `登录成功！欢迎管理员，${response.data.user.realName || response.data.user.username}。正在跳转到管理后台...`,
                  duration: 3000
                })
                
                // 管理员跳转到前端Vue管理页面
                setTimeout(async () => {
                  try {
                    console.log('开始跳转到前端Vue管理后台...')
                    console.log('路由器状态:', router)
                    
                    // 确保跳转到前端Vue应用的管理页面，而不是后端页面
                    const targetUrl = window.location.origin + '/admin/dashboard'
                    console.log('目标URL:', targetUrl)
                    
                    // 检查路由是否存在
                    const route = router.resolve('/admin/dashboard')
                    console.log('路由解析结果:', route)
                    
                    // 使用Vue Router进行前端路由跳转
                    console.log('执行路由跳转...')
                    await router.push('/admin/dashboard')
                    console.log('管理员跳转成功到前端Vue页面')
                    
                    // 验证跳转后的状态
                    setTimeout(() => {
                      console.log('跳转后当前路由:', router.currentRoute.value)
                      console.log('跳转后页面URL:', window.location.href)
                    }, 500)
                    
                  } catch (routerError) {
                    console.error('管理员跳转失败:', routerError)
                    console.error('错误详情:', routerError.stack)
                    ElMessage.error('跳转到管理后台失败: ' + routerError.message)
                    
                    // 尝试备用跳转路径
                    try {
                      console.log('尝试备用路径 /admin')
                      await router.push('/admin')
                      console.log('备用路径跳转成功')
                    } catch (fallbackError) {
                      console.error('备用路径跳转也失败:', fallbackError)
                      console.error('备用错误详情:', fallbackError.stack)
                      ElMessage.error('路由跳转异常，请刷新页面后重试')
                      
                      // 最后尝试直接修改URL
                      console.log('尝试直接修改URL')
                      window.location.href = '/admin/dashboard'
                    }
                  }
                }, 1500)
                return
              }
              
              // 普通用户处理逻辑
              if (!redirectPath) {
                redirectPath = '/'
              }
              
              ElMessage.success(`登录成功！欢迎，${response.data.user.realName || response.data.user.username}`)
              
              console.log('准备跳转到:', redirectPath)
              
              // 延迟跳转确保权限信息已更新
              setTimeout(async () => {
                try {
                  console.log('开始跳转到:', redirectPath)
                  await router.push(redirectPath)
                  console.log('路由跳转成功')
                } catch (routerError) {
                  console.error('路由跳转失败:', routerError)
                  ElMessage.error(`跳转到 ${redirectPath} 失败，正在跳转到首页`)
                  // 如果跳转失败，尝试跳转到首页
                  try {
                    await router.push('/')
                    console.log('备用首页跳转成功')
                  } catch (fallbackError) {
                    console.error('备用首页跳转也失败:', fallbackError)
                    ElMessage.error('路由跳转异常，请刷新页面后重试')
                  }
                }
              }, 200)
              
            } else {
              ElMessage.error('登录信息保存失败，请重试')
            }
          } else {
            ElMessage.error(response.message || '登录失败')
          }
        } catch (error) {
          console.error('登录API调用失败:', error)
          ElMessage.error(error.message || '登录失败，请检查网络连接')
        }
        
      } catch (error) {
        console.error('表单验证失败:', error)
      } finally {
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