import realApi from "./realApi.js";

// 使用真实API（真实数据替换虚拟数据）
const USE_REAL_API = true;

// 防抖函数工具
function debounce(fn, delay = 300) {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    return new Promise((resolve, reject) => {
      timer = setTimeout(async () => {
        try {
          const result = await fn.apply(this, args);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      }, delay);
    });
  };
}

// 认证相关API
export const authApi = {
  // 用户登录
  async login(username, password) {
    try {
      const response = await realApi.login(username, password);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 获取用户信息
  async getUserInfo() {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("未找到登录令牌");
      }

      const response = await realApi.getUserInfo();
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 用户登出
  async logout() {
    return new Promise((resolve) => {
      localStorage.removeItem("token");
      localStorage.removeItem("tokenType");
      localStorage.removeItem("user");
      resolve({
        code: 200,
        message: "登出成功",
      });
    });
  },

  // 用户注册
  async register(registerForm) {
    try {
      const response = await realApi.register(registerForm);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 检查用户名是否存在（带防抖）
  checkUsername: debounce(async (username) => {
    if (!username || username.length < 3) {
      return { data: true }; // 长度不足，暂不验证
    }
    return await realApi.checkUsername(username);
  }, 500),

  // 检查手机号是否存在（带防抖）
  checkPhone: debounce(async (phone) => {
    if (!phone || phone.length !== 11) {
      return { data: true }; // 长度不足，暂不验证
    }
    return await realApi.checkPhone(phone);
  }, 500),

  // 检查邮箱是否存在（带防抖）
  checkEmail: debounce(async (email) => {
    if (!email || !email.includes('@')) {
      return { data: true }; // 格式不符，暂不验证
    }
    return await realApi.checkEmail(email);
  }, 500),
};

export default authApi;
