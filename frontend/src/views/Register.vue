<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>加入汽车洗车服务预约系统</p>
        </div>
        
        <el-form 
          ref="registerFormRef" 
          :model="registerForm" 
          :rules="registerRules" 
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              prefix-icon="UserFilled"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="smsCode">
            <div class="sms-code-container">
              <el-input
                v-model="registerForm.smsCode"
                placeholder="请输入验证码"
                prefix-icon="Message"
                size="large"
                class="sms-input"
              />
              <el-button 
                type="primary" 
                size="large"
                :disabled="smsCodeDisabled"
                :loading="smsCodeLoading"
                @click="sendSmsCode"
                class="sms-btn"
              >
                {{ smsCodeText }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <p>已有账号？ <router-link to="/login">立即登录</router-link></p>
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

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerFormRef = ref()
    const loading = ref(false)
    const smsCodeLoading = ref(false)
    const smsCodeDisabled = ref(false)
    const smsCodeText = ref('获取验证码')
    const countdown = ref(0)
    
    const registerForm = reactive({
      username: '',
      realName: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: '',
      smsCode: ''
    })
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    // 发送短信验证码
    const sendSmsCode = async () => {
      // 验证手机号
      if (!registerForm.phone) {
        ElMessage.error('请先输入手机号')
        return
      }
      
      if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
        ElMessage.error('请输入正确的手机号')
        return
      }

      try {
        smsCodeLoading.value = true
        
        const response = await fetch('http://localhost:8080/api/sms/send-register-code', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: `phone=${encodeURIComponent(registerForm.phone)}`
        })
        
        const result = await response.json()
        
        if (result.code === 200) {
          ElMessage.success('验证码发送成功')
          startCountdown()
        } else {
          ElMessage.error(result.message || '验证码发送失败')
        }
        
      } catch (error) {
        console.error('发送验证码失败:', error)
        ElMessage.error('发送验证码失败，请检查网络连接')
      } finally {
        smsCodeLoading.value = false
      }
    }

    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60
      smsCodeDisabled.value = true
      smsCodeText.value = `${countdown.value}s后重新获取`
      
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value > 0) {
          smsCodeText.value = `${countdown.value}s后重新获取`
        } else {
          clearInterval(timer)
          smsCodeDisabled.value = false
          smsCodeText.value = '获取验证码'
        }
      }, 1000)
    }
    
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      smsCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
      ]
    }
    
    const handleRegister = async () => {
      if (!registerFormRef.value) return
      
      try {
        await registerFormRef.value.validate()
        loading.value = true
        
        // 调用注册API
        const response = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: registerForm.username,
            realName: registerForm.realName,
            phone: registerForm.phone,
            email: registerForm.email,
            password: registerForm.password,
            confirmPassword: registerForm.confirmPassword,
            smsCode: registerForm.smsCode
          })
        })
        
        const result = await response.json()
        
        if (result.code === 200) {
          ElMessage.success('注册成功！请登录')
          router.push('/login')
        } else {
          ElMessage.error(result.message || '注册失败，请重试')
        }
        
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
    
    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      smsCodeLoading,
      smsCodeDisabled,
      smsCodeText,
      handleRegister,
      sendSmsCode
    }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.8rem;
}

.register-header p {
  color: #666;
  font-size: 0.9rem;
}

.register-form {
  margin-bottom: 20px;
}

.sms-code-container {
  display: flex;
  gap: 10px;
}

.sms-input {
  flex: 1;
}

.sms-btn {
  width: 120px;
  flex-shrink: 0;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.register-footer {
  text-align: center;
}

.register-footer p {
  color: #666;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
}

.back-home {
  position: absolute;
  top: 20px;
  left: 20px;
}

@media (max-width: 480px) {
  .register-card {
    padding: 30px 20px;
  }
  
  .register-header h2 {
    font-size: 1.5rem;
  }
}
</style>