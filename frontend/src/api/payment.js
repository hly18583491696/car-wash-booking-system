import request from './request'

/**
 * 支付相关API
 */
export const paymentApi = {
  /**
   * 创建支付订单
   * @param {Object} paymentData - 支付数据
   * @param {string} paymentData.orderNo - 订单号
   * @param {number} paymentData.amount - 支付金额
   * @param {string} paymentData.paymentMethod - 支付方式 (wechat/alipay/credit_card)
   * @param {string} [paymentData.securePayload] - 加密敏感信息载荷（信用卡支付）
   * @returns {Promise} 支付订单信息
   */
  createPayment(paymentData) {
    return request.post('/payment/create', paymentData)
  },

  /**
   * 获取RSA公钥（用于前端加密敏感支付数据）
   * @returns {Promise} 公钥PEM字符串
   */
  getPublicKey() {
    return request.get('/payment/security/public-key')
  },

  /**
   * 查询支付状态
   * @param {string} paymentNo - 支付单号
   * @returns {Promise} 支付状态信息
   */
  getPaymentStatus(paymentNo) {
    return request.get(`/payment/status/${paymentNo}`)
  },

  /**
   * 根据订单号查询支付信息
   * @param {string} orderNo - 订单号
   * @returns {Promise} 支付信息
   */
  getPaymentByOrderNo(orderNo) {
    return request.get(`/payment/order/${orderNo}`)
  },

  /**
   * 获取用户支付记录
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页大小
   * @returns {Promise} 支付记录列表
   */
  getUserPaymentRecords(params = {}) {
    return request.get('/payment/user/payments', { params })
  },

  /**
   * 申请退款
   * @param {Object} refundData - 退款数据
   * @param {string} refundData.paymentNo - 支付单号
   * @param {number} refundData.amount - 退款金额
   * @param {string} refundData.reason - 退款原因
   * @returns {Promise} 退款申请结果
   */
  requestRefund(refundData) {
    return request.post('/payment/refund', refundData)
  },

  /**
   * 查询退款状态
   * @param {string} refundNo - 退款单号
   * @returns {Promise} 退款状态信息
   */
  getRefundStatus(refundNo) {
    return request.get(`/payment/refund/status/${refundNo}`)
  },

  /**
   * 取消过期支付订单
   * @param {string} paymentNo - 支付单号
   * @returns {Promise} 取消结果
   */
  cancelExpiredPayment(paymentNo) {
    return request.post(`/payment/cancel/${paymentNo}`)
  },

  // 管理员专用API
  admin: {
    /**
     * 处理退款申请（管理员）
     * @param {Object} refundData - 退款处理数据
     * @param {string} refundData.refundNo - 退款单号
     * @param {string} refundData.status - 处理状态 (approved/rejected)
     * @param {string} refundData.remark - 处理备注
     * @returns {Promise} 处理结果
     */
    processRefund(refundData) {
      return request.post('/payment/admin/refund/process', refundData)
    },

    /**
     * 取消过期支付订单（管理员）
     * @returns {Promise} 取消结果
     */
    cancelExpiredPayments() {
      return request.post('/payment/admin/cancel-expired')
    },

    /**
     * 获取所有支付记录（管理员）
     * @param {Object} params - 查询参数
     * @returns {Promise} 支付记录列表
     */
    getAllPaymentRecords(params = {}) {
      return request.get('/payment/admin/records', { params })
    },

    /**
     * 分页查询支付审计日志（管理员）
     * @param {Object} params - 查询参数，如 { current, size, paymentNo, orderNo, eventType, status }
     * @returns {Promise} 审计日志分页结果
     */
    getPaymentAudits(params = {}) {
      return request.get('/payment/admin/audits', { params })
    },

    /**
     * 获取所有退款记录（管理员）
     * @param {Object} params - 查询参数
     * @returns {Promise} 退款记录列表
     */
    getAllRefundRecords(params = {}) {
      return request.get('/payment/admin/refunds', { params })
    }
  }
}

export default paymentApi