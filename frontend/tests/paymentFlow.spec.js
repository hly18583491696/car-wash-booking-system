import { describe, it, expect } from 'vitest'
import { shouldShowPayButton, buildPayAriaLabel, resolvePaymentRoute } from '@/utils/paymentFlow'

describe('paymentFlow utils', () => {
  it('should show pay button for pending/confirmed and not paid', () => {
    expect(shouldShowPayButton({ status: 'pending', paymentStatus: 'pending' })).toBe(true)
    expect(shouldShowPayButton({ status: 'confirmed', paymentStatus: 'failed' })).toBe(true)
  })

  it('should hide pay button when paid or inappropriate status', () => {
    expect(shouldShowPayButton({ status: 'completed', paymentStatus: 'paid' })).toBe(false)
    expect(shouldShowPayButton({ status: 'cancelled', paymentStatus: 'pending' })).toBe(false)
    expect(shouldShowPayButton(null)).toBe(false)
  })

  it('builds aria-label containing order number', () => {
    expect(buildPayAriaLabel({ orderNumber: 'A123' })).toBe('为订单 A123 付款')
  })

  it('resolves payment route with order number', () => {
    const route = resolvePaymentRoute({ orderNumber: 'B456' })
    expect(route).toEqual({ name: 'Payment', params: { orderNo: 'B456' } })
  })
})