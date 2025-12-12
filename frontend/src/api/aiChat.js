import request from './request'

/**
 * AI客服API模块
 * 
 * 提供AI智能客服相关的API调用
 */

/**
 * 发送消息给AI客服
 * @param {Object} data - 请求数据
 * @param {string} data.message - 用户消息
 * @param {string} [data.sessionId] - 会话ID（可选）
 * @param {string} [data.context] - 上下文信息（可选）
 * @returns {Promise} - AI回复
 */
export function sendMessage(data) {
  return request({
    url: '/ai-chat/chat',
    method: 'post',
    data
  })
}

/**
 * 获取会话历史记录
 * @param {string} sessionId - 会话ID
 * @returns {Promise} - 历史消息列表
 */
export function getSessionHistory(sessionId) {
  return request({
    url: `/ai-chat/history/${sessionId}`,
    method: 'get'
  })
}

/**
 * 提交用户评价反馈
 * @param {string} sessionId - 会话ID
 * @param {Object} feedback - 评价数据
 * @param {number} feedback.rating - 评分（1-5）
 * @param {boolean} feedback.resolved - 是否解决
 * @returns {Promise}
 */
export function submitFeedback(sessionId, feedback) {
  return request({
    url: `/ai-chat/feedback/${sessionId}`,
    method: 'post',
    data: feedback
  })
}

/**
 * 获取快捷问题列表
 * @returns {Promise} - 快捷问题列表
 */
export function getQuickQuestions() {
  return request({
    url: '/ai-chat/quick-questions',
    method: 'get'
  })
}

/**
 * 清除会话记录
 * @param {string} sessionId - 会话ID
 * @returns {Promise}
 */
export function clearSession(sessionId) {
  return request({
    url: `/ai-chat/session/${sessionId}`,
    method: 'delete'
  })
}

export default {
  sendMessage,
  getSessionHistory,
  submitFeedback,
  getQuickQuestions,
  clearSession
}
