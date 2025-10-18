<template>
  <div class="navbar">
    <div class="navbar-container">
      <!-- Logo区域 -->
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <div class="brand-icon">
            <el-icon size="28"><House /></el-icon>
          </div>
          <span class="brand-text">洗车预约</span>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <div class="navbar-menu" :class="{ 'is-active': mobileMenuOpen }">
        <nav class="nav-links">
          <router-link to="/" class="nav-link">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/services" class="nav-link">
            <el-icon><Grid /></el-icon>
            <span>服务项目</span>
          </router-link>
          <router-link to="/stores" class="nav-link">
            <el-icon><Location /></el-icon>
            <span>洗车店查询</span>
          </router-link>
          <router-link to="/appointment" class="nav-link">
            <el-icon><Calendar /></el-icon>
            <span>立即预约</span>
          </router-link>
          <router-link to="/orders" class="nav-link">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </router-link>

        </nav>
      </div>

      <!-- 右侧操作区 -->
      <div class="navbar-actions">
        <!-- 主题切换 -->
        <ThemeToggle />
        
        <!-- 用户菜单 -->
        <div class="user-menu" v-if="isLoggedIn">
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar">
              <el-avatar :size="36" :src="userInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ userInfo.name }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isAdmin" command="admin">
                  <el-icon><DataBoard /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  账户设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 登录按钮 -->
        <div class="auth-buttons" v-else>
          <router-link to="/login">
            <el-button type="primary" size="default">登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button type="default" size="default" plain>注册</el-button>
          </router-link>
        </div>

        <!-- 移动端菜单按钮 -->
        <div class="mobile-menu-btn" @click="toggleMobileMenu">
          <el-icon size="24">
            <Menu v-if="!mobileMenuOpen" />
            <Close v-else />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ThemeToggle from '../ThemeToggle.vue'

export default {
  name: 'Navbar',
  components: {
    ThemeToggle
  },
  setup() {
    const router = useRouter()
    const mobileMenuOpen = ref(false)
    
    // 用户信息
    const userInfo = ref({
      name: '用户名',
      avatar: ''
    })
    
    // 登录状态
    const isLoggedIn = computed(() => {
      return localStorage.getItem('token') !== null
    })

    // 管理员状态
    const isAdmin = computed(() => {
      return localStorage.getItem('userRole') === 'admin'
    })
    
    // 切换移动端菜单
    const toggleMobileMenu = () => {
      mobileMenuOpen.value = !mobileMenuOpen.value
    }
    
    // 处理用户菜单命令
    const handleUserCommand = async (command) => {
      switch (command) {
        case 'admin':
          router.push('/admin')
          break
        case 'profile':
          router.push('/profile')
          break
        case 'settings':
          router.push('/settings')
          break
        case 'logout':
          try {
            await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            })
            
            // 清除登录信息
            localStorage.removeItem('token')
            localStorage.removeItem('tokenType')
            localStorage.removeItem('userRole')
            localStorage.removeItem('userInfo')
            
            ElMessage.success('已退出登录')
            router.push('/login')
          } catch {
            // 用户取消
          }
          break
      }
    }
    
    // 初始化用户信息
    onMounted(() => {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (storedUserInfo) {
        try {
          const parsed = JSON.parse(storedUserInfo)
          userInfo.value = {
            name: parsed.username || parsed.name || '用户',
            avatar: parsed.avatar || ''
          }
        } catch (error) {
          console.error('解析用户信息失败:', error)
        }
      }
    })
    
    return {
      mobileMenuOpen,
      userInfo,
      isLoggedIn,
      isAdmin,
      toggleMobileMenu,
      handleUserCommand
    }
  }
}
</script>

<style scoped>
.navbar {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all var(--transition-normal);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

/* Logo区域 */
.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1.2rem;
  transition: all var(--transition-fast);
}

.brand-link:hover {
  color: var(--primary-color);
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  color: var(--text-white);
}

.brand-text {
  font-size: 1.3rem;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 导航菜单 */
.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
}

.nav-link:hover {
  color: var(--primary-color);
  background: var(--primary-light);
}

.nav-link.router-link-active {
  color: var(--primary-color);
  background: var(--primary-light);
  font-weight: 600;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: var(--primary-color);
  border-radius: 1px;
}

/* 右侧操作区 */
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* 用户菜单 */
.user-menu {
  cursor: pointer;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.user-avatar:hover {
  background: var(--bg-light);
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  color: var(--text-light);
  transition: transform var(--transition-fast);
}

.user-avatar:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 登录按钮 */
.auth-buttons {
  display: flex;
  gap: 8px;
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.mobile-menu-btn:hover {
  color: var(--primary-color);
  background: var(--primary-light);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 16px;
  }
  
  .brand-text {
    display: none;
  }
  
  .navbar-menu {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: var(--bg-primary);
    border-top: 1px solid var(--border-color);
    box-shadow: var(--shadow-md);
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all var(--transition-normal);
  }
  
  .navbar-menu.is-active {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }
  
  .nav-links {
    flex-direction: column;
    padding: 16px;
    gap: 4px;
  }
  
  .nav-link {
    width: 100%;
    justify-content: flex-start;
    padding: 12px 16px;
  }
  
  .user-name {
    display: none;
  }
  
  .auth-buttons {
    display: none;
  }
  
  .mobile-menu-btn {
    display: block;
  }
}

@media (max-width: 480px) {
  .navbar-container {
    height: 56px;
    padding: 0 12px;
  }
  
  .brand-icon {
    width: 36px;
    height: 36px;
  }
  
  .navbar-actions {
    gap: 12px;
  }
}
</style>