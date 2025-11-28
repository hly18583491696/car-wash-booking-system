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
            >
              <template #suffix>
                <el-icon v-if="usernameValidating" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="usernameAvailable === true" color="#67c23a">
                  <CircleCheck />
                </el-icon>
                <el-icon v-else-if="usernameAvailable === false" color="#f56c6c">
                  <CircleClose />
                </el-icon>
              </template>
            </el-input>
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
            >
              <template #suffix>
                <el-icon v-if="phoneValidating" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="phoneAvailable === true" color="#67c23a">
                  <CircleCheck />
                </el-icon>
                <el-icon v-else-if="phoneAvailable === false" color="#f56c6c">
                  <CircleClose />
                </el-icon>
              </template>
            </el-input>
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
            >
              <template #suffix>
                <el-icon v-if="emailValidating" class="is-loading">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="emailAvailable === true" color="#67c23a">
                  <CircleCheck />
                </el-icon>
                <el-icon v-else-if="emailAvailable === false" color="#f56c6c">
                  <CircleClose />
                </el-icon>
              </template>
            </el-input>
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
              {{ loading ? "注册中..." : "注册" }}
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
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Loading, CircleCheck, CircleClose } from "@element-plus/icons-vue";
import authApi from "../api/auth.js";
import { useUserStore } from "../stores/user.js";

export default {
  name: "Register",
  components: {
    Loading,
    CircleCheck,
    CircleClose,
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const registerFormRef = ref();
    const loading = ref(false);
    const smsCodeLoading = ref(false);
    const smsCodeDisabled = ref(false);
    const smsCodeText = ref("获取验证码");
    const countdown = ref(0);

    // 实时验证状态
    const usernameValidating = ref(false);
    const usernameAvailable = ref(null); // null: 未验证, true: 可用, false: 不可用
    const phoneValidating = ref(false);
    const phoneAvailable = ref(null);
    const emailValidating = ref(false);
    const emailAvailable = ref(null);

    const registerForm = reactive({
      username: "",
      realName: "",
      phone: "",
      email: "",
      password: "",
      confirmPassword: "",
      smsCode: "",
    });

    // 用户名唯一性验证
    const validateUsername = async (rule, value, callback) => {
      if (!value || value.length < 3) {
        usernameAvailable.value = null;
        return callback();
      }
      
      usernameValidating.value = true;
      try {
        const response = await authApi.checkUsername(value);
        usernameAvailable.value = response.data === true;
        if (!usernameAvailable.value) {
          callback(new Error("用户名已被使用"));
        } else {
          callback();
        }
      } catch (error) {
        console.error("用户名验证失败:", error);
        usernameAvailable.value = null;
        callback();
      } finally {
        usernameValidating.value = false;
      }
    };

    // 手机号唯一性验证
    const validatePhone = async (rule, value, callback) => {
      if (!value || value.length !== 11) {
        phoneAvailable.value = null;
        return callback();
      }
      
      phoneValidating.value = true;
      try {
        const response = await authApi.checkPhone(value);
        phoneAvailable.value = response.data === true;
        if (!phoneAvailable.value) {
          callback(new Error("手机号已被注册"));
        } else {
          callback();
        }
      } catch (error) {
        console.error("手机号验证失败:", error);
        phoneAvailable.value = null;
        callback();
      } finally {
        phoneValidating.value = false;
      }
    };

    // 邮箱唯一性验证
    const validateEmail = async (rule, value, callback) => {
      if (!value || !value.includes('@')) {
        emailAvailable.value = null;
        return callback();
      }
      
      emailValidating.value = true;
      try {
        const response = await authApi.checkEmail(value);
        emailAvailable.value = response.data === true;
        if (!emailAvailable.value) {
          callback(new Error("邮箱已被注册"));
        } else {
          callback();
        }
      } catch (error) {
        console.error("邮箱验证失败:", error);
        emailAvailable.value = null;
        callback();
      } finally {
        emailValidating.value = false;
      }
    };

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error("两次输入密码不一致"));
      } else {
        callback();
      }
    };

    // 发送短信验证码（占位，暂缓实现）
    const sendSmsCode = async () => {
      ElMessage.info("验证码功能暂缓开发");
    };

    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60;
      smsCodeDisabled.value = true;
      smsCodeText.value = `${countdown.value}s后重新获取`;

      const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value > 0) {
          smsCodeText.value = `${countdown.value}s后重新获取`;
        } else {
          clearInterval(timer);
          smsCodeDisabled.value = false;
          smsCodeText.value = "获取验证码";
        }
      }, 1000);
    };

    const registerRules = {
      username: [
        { required: true, message: "请输入用户名", trigger: "blur" },
        { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: "用户名为3-20位字母/数字/下划线", trigger: "blur" },
        { validator: validateUsername, trigger: "blur" },
      ],
      realName: [
        { max: 50, message: "真实姓名长度不能超过50个字符", trigger: "blur" },
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" },
        { validator: validatePhone, trigger: "blur" },
      ],
      email: [
        { required: true, message: "请输入邮箱", trigger: "blur" },
        { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
        { validator: validateEmail, trigger: "blur" },
      ],
      password: [
        { required: true, message: "请输入密码", trigger: "blur" },
        { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=?]{8,20}$/,
          message: "密码需包含字母和数字，可含特殊字符，长度8-20",
          trigger: "blur" },
      ],
      confirmPassword: [
        { required: true, message: "请确认密码", trigger: "blur" },
        { validator: validateConfirmPassword, trigger: "blur" },
      ],
      smsCode: [],
    };

    const handleRegister = async () => {
      if (!registerFormRef.value) return;

      try {
        await registerFormRef.value.validate();
        loading.value = true;

        // 构造注册请求数据
        const payload = {
          username: registerForm.username,
          email: registerForm.email,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
        };
        if (registerForm.realName) payload.realName = registerForm.realName;
        if (registerForm.phone) payload.phone = registerForm.phone;

        // 调用注册API
        const response = await authApi.register(payload);

        if (response.code === 200) {
          ElMessage.success("注册成功！正在自动登录...");
          
          // 注册成功后自动登录
          await performAutoLogin(registerForm.username, registerForm.password);
        } else {
          ElMessage.error(response.message || "注册失败，请重试");
        }
      } catch (error) {
        console.error("注册失败:", error);
        if (error.response?.data?.message) {
          ElMessage.error(error.response.data.message);
        } else {
          ElMessage.error("注册失败，请检查网络连接");
        }
      } finally {
        loading.value = false;
      }
    };

    // 自动登录函数
    const performAutoLogin = async (username, password) => {
      try {
        const loginResponse = await authApi.login(username, password);
        
        if (loginResponse.code === 200 && loginResponse.data) {
          // 保存token到localStorage
          localStorage.setItem("token", loginResponse.data.token);
          localStorage.setItem("user", JSON.stringify(loginResponse.data));
          
          // 更新Vuex store
          userStore.login(loginResponse.data);
          
          ElMessage.success("登录成功，欢迎回来！");
          
          // 根据用户角色跳转
          if (loginResponse.data.role === "admin") {
            router.push("/admin");
          } else {
            router.push("/dashboard");
          }
        } else {
          ElMessage.warning("注册成功，但自动登录失败，请手动登录");
          router.push("/login");
        }
      } catch (error) {
        console.error("自动登录失败:", error);
        ElMessage.warning("注册成功，但自动登录失败，请手动登录");
        router.push("/login");
      }
    };

    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      smsCodeLoading,
      smsCodeDisabled,
      smsCodeText,
      handleRegister,
      sendSmsCode,
      usernameValidating,
      usernameAvailable,
      phoneValidating,
      phoneAvailable,
      emailValidating,
      emailAvailable,
    };
  },
};
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
