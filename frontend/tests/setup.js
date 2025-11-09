import { vi } from 'vitest'

// 为测试环境提供必要的全局/浏览器 API 补丁
if (typeof window !== 'undefined') {
  // matchMedia 补丁以防某些组件查询媒体特性
  if (!window.matchMedia) {
    window.matchMedia = () => ({ matches: false, addListener: () => {}, removeListener: () => {} })
  }
}

// 简单 localStorage 模拟（Node 环境下）
if (typeof window === 'undefined') {
  const store = {}
  global.localStorage = {
    getItem: (k) => store[k] || null,
    setItem: (k, v) => { store[k] = String(v) },
    removeItem: (k) => { delete store[k] },
    clear: () => { Object.keys(store).forEach((k) => delete store[k]) }
  }
}

// 默认将 token 设置为空，测试中可按需覆盖
localStorage.setItem('token', '')

// mock 路由 push，测试中可替换
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({ push: vi.fn() })
  }
})