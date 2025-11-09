import { describe, it, expect, vi, beforeEach } from 'vitest'
import { forceRefreshOrders, getGlobalOrderState } from '@/utils/orderSync'

describe('orderSync.forceRefreshOrders', () => {
  beforeEach(() => {
    // 重置全局状态
    const state = getGlobalOrderState()
    state.orders = []
    state.lastUpdate = 0
  })

  it('throws on invalid parameters (missing userId)', async () => {
    const fetchFn = vi.fn()
    await expect(forceRefreshOrders(fetchFn, { userId: null })).rejects.toThrow(/INVALID_PARAMETERS/i)
  })

  it('returns cached orders under fastMode within ~500ms', async () => {
    const state = getGlobalOrderState()
    state.orders = [{ id: 1 }, { id: 2 }]
    state.lastUpdate = Date.now()

    const slowFetch = vi.fn(async () => {
      await new Promise(r => setTimeout(r, 800))
      return [{ id: 3 }]
    })

    const start = performance.now()
    const result = await forceRefreshOrders(slowFetch, { userId: 'u1', fastMode: true, showMessage: false })
    const duration = performance.now() - start

    expect(result).toEqual([{ id: 1 }, { id: 2 }])
    expect(duration).toBeLessThan(500) // 快速返回缓存
  })

  it('applies only latest request results under concurrency (latest-only)', async () => {
    const fastFirst = vi.fn(async () => {
      await new Promise(r => setTimeout(r, 100))
      return [{ id: 'A' }]
    })
    const slowSecond = vi.fn(async () => {
      await new Promise(r => setTimeout(r, 300))
      return [{ id: 'B' }]
    })

    // 并发触发两次刷新
    const p1 = forceRefreshOrders(fastFirst, { userId: 'u2', showMessage: false, fastMode: false })
    const p2 = forceRefreshOrders(slowSecond, { userId: 'u2', showMessage: false, fastMode: false })

    const r1 = await p1
    const r2 = await p2
    const state = getGlobalOrderState()

    // 两次结果都返回，但最终应用的是最新一次（第二次慢请求）
    expect(r1).toEqual([{ id: 'A' }])
    expect(r2).toEqual([{ id: 'B' }])
    expect(state.orders).toEqual([{ id: 'B' }])
  })

  it('propagates errors with clear messages', async () => {
    const failingFetch = vi.fn(async () => {
      const error = new Error('网络错误')
      error.response = { status: 500, data: { message: '服务器内部错误' } }
      throw error
    })

    await expect(forceRefreshOrders(failingFetch, { userId: 'u3' })).rejects.toThrow()
  })
})