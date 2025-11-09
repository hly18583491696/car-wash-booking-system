// 支付流程工具：用于统一支付按钮显示逻辑、辅助属性与导航构造

/**
 * 判断是否显示付款按钮
 * 显示条件：订单状态为 pending 或 confirmed，且未支付
 */
export function shouldShowPayButton(order) {
  if (!order) return false
  const statusOk = order.status === 'pending' || order.status === 'confirmed'
  const notPaid = order.paymentStatus !== 'paid'
  return statusOk && notPaid
}

/**
 * 构建无障碍 aria-label 文案
 */
export function buildPayAriaLabel(order) {
  const no = order?.orderNumber || order?.orderNo || '未知订单'
  return `为订单 ${no} 付款`
}

/**
 * 构建支付页路由对象
 */
export function resolvePaymentRoute(order) {
  const orderNo = order?.orderNumber || order?.orderNo
  return { name: 'Payment', params: { orderNo } }
}