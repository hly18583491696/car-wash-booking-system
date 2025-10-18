import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入页面组件
const Home = () => import('../views/Home.vue')
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')

const Services = () => import('../views/Services.vue')
const Stores = () => import('../views/Stores.vue')

const Appointment = () => import('../views/Appointment.vue')
const Orders = () => import('../views/Orders.vue')
const Profile = () => import('../views/Profile.vue')
const NotFound = () => import('../views/NotFound.vue')
const Admin = () => import('../views/Admin.vue')

// 导入布局组件
const Layout = () => import('../components/Layout/Layout.vue')

const routes = [
  // API测试路由（独立页面）
  {
    path: '/api-test',
    name: 'ApiTest',
    component: () => import('../views/ApiTest.vue'),
    meta: {
      title: 'API测试',
      requiresAuth: false
    }
  },
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
          requiresAuth: false
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
        path: 'stores',
        name: 'Stores',
        component: Stores,
        meta: {
          title: '洗车店',
          requiresAuth: false
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

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 汽车洗车服务预约系统`
  }
  
  // 检查认证状态
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  const isAuthenticated = !!token
  
  // 如果页面需要认证但用户未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    ElMessage.warning('请先登录')
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 如果页面需要管理员权限但用户不是管理员
  if (to.meta.requiresAdmin && userRole !== 'admin') {
    ElMessage.error('您没有权限访问此页面')
    next('/')
    return
  }
  
  // 如果用户已登录但访问登录/注册页面
  if (to.meta.hideForAuth && isAuthenticated) {
    next('/')
    return
  }
  
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router