import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { nextTick } from 'vue'

// 懒加载组件
const Home = () => import('../views/Home.vue')
const GuestHome = () => import('../views/GuestHome.vue')
const Services = () => import('../views/Services.vue')
const Appointment = () => import('../views/Appointment.vue')
const Orders = () => import('../views/Orders.vue')
const Profile = () => import('../views/Profile.vue')
const Payment = () => import('../views/Payment.vue')
const PaymentRecords = () => import('../views/PaymentRecords.vue')
const Stores = () => import('../views/Stores.vue')
const Debug = () => import('../views/Debug.vue')
const AdminTest = () => import('../views/AdminTest.vue')
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Admin = () => import('../views/Admin.vue')
const NotFound = () => import('../views/NotFound.vue')

// 布局组件
const Layout = () => import('../components/Layout/Layout.vue')

const routes = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home,
        meta: {
          title: '首页',
          requiresAuth: true
        }
      },
      {
        path: 'services',
        name: 'Services',
        component: Services,
        meta: {
          title: '服务项目',
          requiresAuth: false
        }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: Appointment,
        meta: {
          title: '预约服务',
          requiresAuth: true
        }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: Orders,
        meta: {
          title: '我的订单',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'payment/:orderNo?',
        name: 'Payment',
        component: Payment,
        meta: {
          title: '支付',
          requiresAuth: true
        }
      },
      {
        path: 'payment-records',
        name: 'PaymentRecords',
        component: PaymentRecords,
        meta: {
          title: '支付记录',
          requiresAuth: true
        }
      },

      {
        path: 'stores',
        name: 'Stores',
        component: Stores,
        meta: {
          title: '洗车店',
          requiresAuth: false
        }
      },
      {
        path: 'debug',
        name: 'Debug',
        component: Debug,
        meta: {
          title: '调试页面',
          requiresAuth: false
        }
      },
      {
        path: 'admin-test',
        name: 'AdminTest',
        component: AdminTest,
        meta: {
          title: '管理员测试',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      // 访客首页 - 在Layout下
      {
        path: 'guest',
        name: 'GuestHome',
        component: GuestHome,
        meta: {
          title: '欢迎使用洗车预约系统',
          requiresAuth: false,
          guestOnly: true
        }
      },

    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      requiresAuth: false,
      hideForAuth: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册',
      requiresAuth: false,
      hideForAuth: true
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: {
      title: '管理员后台',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: Admin,
    meta: {
      title: '管理员后台',
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '页面未找到',
      requiresAuth: false
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 导入权限管理工具
import AuthManager from '../utils/auth.js'
import RouteLogger from '../utils/routeLogger.js'

// 增强的路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 汽车洗车服务预约系统`
  }
  
  const isAuthenticated = AuthManager.isAuthenticated()
  const userRole = AuthManager.getUserRole()
  const userInfo = AuthManager.getCurrentUser()
  
  // 记录认证状态检查
  RouteLogger.logAuthCheck(to.path, isAuthenticated, userInfo)
  
  console.log('路由导航:', {
    to: to.path,
    from: from.path,
    isAuth: isAuthenticated,
    role: userRole,
    requiresAuth: to.meta.requiresAuth,
    requiresAdmin: to.meta.requiresAdmin,
    hideForAuth: to.meta.hideForAuth,
    guestOnly: to.meta.guestOnly
  })
  
  try {
    // 1. 处理首页访问逻辑
    if (to.path === '/') {
      if (!isAuthenticated) {
        // 未登录用户跳转到访客首页
        RouteLogger.logAuthRedirect('/', 'guest', 'not_authenticated')
        next('guest')
        return
      } else if (userRole === 'admin') {
        // 管理员跳转到管理后台
        RouteLogger.logAuthRedirect('/', '/admin', 'admin_redirect')
        next('/admin')
        return
      }
      // 已登录的普通用户可以访问首页
    }

    // 2. 处理访客页面访问
    if (to.meta.guestOnly && isAuthenticated) {
      const redirectPath = userRole === 'admin' ? '/admin' : '/'
      RouteLogger.logAuthRedirect(to.path, redirectPath, 'already_authenticated')
      next(redirectPath)
      return
    }

    // 3. 处理需要认证的页面
    if (to.meta.requiresAuth && !isAuthenticated) {
      RouteLogger.logAuthRedirect(to.path, 'guest', 'authentication_required')
      next({
        path: 'guest',
        query: { redirect: to.fullPath, reason: 'login_required' }
      })
      return
    }

    // 4. 处理需要管理员权限的页面
    if (to.meta.requiresAdmin && (!isAuthenticated || !AuthManager.isAdmin())) {
      const redirectPath = isAuthenticated ? '/' : 'guest'
      const reason = !isAuthenticated ? 'not_authenticated' : 'insufficient_permission'
      
      RouteLogger.logPermissionDenied(to.path, 'admin', userInfo)
      RouteLogger.logAuthRedirect(to.path, redirectPath, reason)
      
      next({
        path: redirectPath,
        query: { redirect: to.fullPath, reason: 'admin_required' }
      })
      return
    }

    // 5. 处理登录/注册页面的访问
    if (to.meta.hideForAuth && isAuthenticated) {
      const redirectPath = userRole === 'admin' ? '/admin' : '/'
      RouteLogger.logAuthRedirect(to.path, redirectPath, 'already_authenticated')
      next(redirectPath)
      return
    }

    // 6. 检查Token是否即将过期
    if (isAuthenticated && AuthManager.isTokenExpiring()) {
      try {
        await AuthManager.refreshToken()
        RouteLogger.log({
          type: 'token_refresh',
          path: to.path,
          success: true
        })
      } catch (error) {
        console.error('Token刷新失败:', error)
        AuthManager.logout()
        RouteLogger.logAuthRedirect(to.path, 'guest', 'token_refresh_failed')
        next({
          path: 'guest',
          query: { redirect: to.fullPath, reason: 'session_expired' }
        })
        return
      }
    }

    // 记录守卫执行成功
    RouteLogger.logGuardExecution(from.path, to.path, 'allow', to.meta)
    next()
    
  } catch (error) {
    console.error('路由权限检查失败:', error)
    
    // 记录错误
    RouteLogger.log({
      type: 'guard_error',
      from: from.path,
      to: to.path,
      error: error.message
    })
    
    const redirectPath = isAuthenticated ? (userRole === 'admin' ? '/admin' : '/') : 'guest'
    
    RouteLogger.logAuthRedirect(to.path, redirectPath, 'guard_error')
    next({
      path: redirectPath,
      query: { redirect: to.fullPath, reason: 'system_error' }
    })
  }
})

// 路由后置守卫 - 用于页面加载完成后的处理
router.afterEach((to, from) => {
  // 记录页面访问日志
  if (AuthManager.isAuthenticated()) {
    console.log(`用户 ${AuthManager.getUserDisplayName()} 访问了页面: ${to.path}`)
  }
  
  // 滚动到顶部
  nextTick(() => {
    window.scrollTo(0, 0)
  })
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router