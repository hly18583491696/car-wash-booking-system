import request from '@/utils/request'

/**
 * 短信验证码相关API
 */

// 发送注册验证码
export function sendRegisterCode(phone) {
  return request({
    url: '/api/sms/send-register-code',
    method: 'post',
    params: { phone }
  })
}

// 发送登录验证码
export function sendLoginCode(phone) {
  return request({
    url: '/api/sms/send-login-code',
    method: 'post',
    params: { phone }
  })
}

// 发送重置密码验证码
export function sendResetPasswordCode(phone) {
  return request({
    url: '/api/sms/send-reset-code',
    method: 'post',
    params: { phone }
  })
}

// 验证验证码
export function verifyCode(phone, code, type) {
  return request({
    url: '/api/sms/verify-code',
    method: 'post',
    params: { phone, code, type }
  })
}