import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, shallowMount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { resolvePaymentRoute, shouldShowPayButton, buildPayAriaLabel } from '@/utils/paymentFlow'

// 使用轻量宿主组件模拟按钮行为，避免引入整页依赖
const TestHost = defineComponent({
  name: 'TestHost',
  props: {
    order: { type: Object, required: true }
  },
  setup(props, { emit }) {
    const router = { push: vi.fn() }
    const payOrder = async (order) => {
      // 模拟已认证（真实页面会执行 ensureAuthenticated）
      const route = resolvePaymentRoute(order)
      router.push(route)
      emit('paid', route)
    }
    return () =>
      h('div', { class: 'action-buttons' }, [
        shouldShowPayButton(props.order)
          ? h(
              'button',
              {
                class: 'pay-button',
                'aria-label': buildPayAriaLabel(props.order),
                onClick: () => payOrder(props.order)
              },
              '付款'
            )
          : null
      ])
  }
})

describe('Pay button integration (lightweight host)', () => {
  beforeEach(() => {
    localStorage.setItem('token', 'stub-token')
  })

  it('renders button when order needs payment', () => {
    const wrapper = shallowMount(TestHost, {
      props: { order: { orderNumber: 'X001', status: 'pending', paymentStatus: 'pending' } }
    })
    const btn = wrapper.find('.pay-button')
    expect(btn.exists()).toBe(true)
    expect(btn.attributes('aria-label')).toContain('X001')
  })

  it('triggers payment navigation on click', async () => {
    const wrapper = mount(TestHost, {
      props: { order: { orderNumber: 'X002', status: 'confirmed', paymentStatus: 'failed' } }
    })
    const btn = wrapper.find('.pay-button')
    expect(btn.exists()).toBe(true)
    await btn.trigger('click')
    const emitted = wrapper.emitted('paid')
    expect(emitted).toBeTruthy()
    expect(emitted[0][0]).toEqual({ name: 'Payment', params: { orderNo: 'X002' } })
  })

  it('does not render button when already paid', () => {
    const wrapper = shallowMount(TestHost, {
      props: { order: { orderNumber: 'X003', status: 'completed', paymentStatus: 'paid' } }
    })
    expect(wrapper.find('.pay-button').exists()).toBe(false)
  })
})