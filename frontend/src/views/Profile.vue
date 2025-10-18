<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的个人信息和偏好设置</p>
      </div>
    </div>

    <div class="profile-container">
      <div class="container">
        <div class="profile-layout">
          <!-- 侧边栏导航 -->
          <div class="profile-sidebar">
            <div class="user-card">
              <div class="user-avatar">
                <el-avatar :size="80" :src="userInfo.avatar">
                  <el-icon size="40"><User /></el-icon>
                </el-avatar>
                <el-button class="avatar-upload" size="small" circle>
                  <el-icon><Camera /></el-icon>
                </el-button>
              </div>
              <div class="user-info">
                <h3 class="user-name">{{ userInfo.name }}</h3>
                <p class="user-level">{{ userInfo.level }}</p>
                <div class="user-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.totalOrders }}</span>
                    <span class="stat-label">总订单</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.totalSpent }}</span>
                    <span class="stat-label">总消费</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.points }}</span>
                    <span class="stat-label">积分</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="nav-menu">
              <div 
                class="nav-item" 
                v-for="item in navItems" 
                :key="item.key"
                :class="{ active: activeTab === item.key }"
                @click="activeTab = item.key"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

          <!-- 主要内容区域 -->
          <div class="profile-content">
            <!-- 个人信息 -->
            <div v-show="activeTab === 'info'" class="content-section">
              <div class="section-header">
                <h3>个人信息</h3>
                <el-button type="primary" @click="editMode = !editMode">
                  {{ editMode ? '取消编辑' : '编辑信息' }}
                </el-button>
              </div>
              
              <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="userForm.username" :disabled="!editMode" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="真实姓名" prop="realName">
                      <el-input v-model="userForm.realName" :disabled="!editMode" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="手机号码" prop="phone">
                      <el-input v-model="userForm.phone" :disabled="!editMode" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="邮箱地址" prop="email">
                      <el-input v-model="userForm.email" :disabled="!editMode" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="性别" prop="gender">
                      <el-select v-model="userForm.gender" :disabled="!editMode" style="width: 100%">
                        <el-option label="男" value="male" />
                        <el-option label="女" value="female" />
                        <el-option label="保密" value="secret" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="生日" prop="birthday">
                      <el-date-picker 
                        v-model="userForm.birthday" 
                        type="date" 
                        :disabled="!editMode"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="地址" prop="address">
                  <el-input v-model="userForm.address" :disabled="!editMode" />
                </el-form-item>
                
                <el-form-item v-if="editMode">
                  <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
                  <el-button @click="editMode = false">取消</el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 车辆管理 -->
            <div v-show="activeTab === 'vehicles'" class="content-section">
              <div class="section-header">
                <h3>车辆管理</h3>
                <el-button type="primary" @click="addVehicle">添加车辆</el-button>
              </div>
              
              <div class="vehicles-grid">
                <div class="vehicle-card" v-for="vehicle in vehicles" :key="vehicle.id">
                  <div class="vehicle-info">
                    <div class="vehicle-icon">
                      <el-icon size="32" color="var(--primary-color)"><Car /></el-icon>
                    </div>
                    <div class="vehicle-details">
                      <h4 class="plate-number">{{ vehicle.plateNumber }}</h4>
                      <p class="vehicle-model">{{ vehicle.brand }} {{ vehicle.model }}</p>
                      <p class="vehicle-color">{{ getColorName(vehicle.color) }}</p>
                    </div>
                  </div>
                  <div class="vehicle-actions">
                    <el-button size="small" @click="editVehicle(vehicle)">编辑</el-button>
                    <el-button size="small" type="danger" @click="deleteVehicle(vehicle)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 安全设置 -->
            <div v-show="activeTab === 'security'" class="content-section">
              <div class="section-header">
                <h3>安全设置</h3>
              </div>
              
              <div class="security-items">
                <div class="security-item">
                  <div class="item-info">
                    <h4>登录密码</h4>
                    <p>定期更换密码，保护账户安全</p>
                  </div>
                  <el-button @click="changePassword">修改密码</el-button>
                </div>
                
                <div class="security-item">
                  <div class="item-info">
                    <h4>手机绑定</h4>
                    <p>已绑定手机：{{ userForm.phone }}</p>
                  </div>
                  <el-button>更换手机</el-button>
                </div>
                
                <div class="security-item">
                  <div class="item-info">
                    <h4>邮箱绑定</h4>
                    <p>已绑定邮箱：{{ userForm.email }}</p>
                  </div>
                  <el-button>更换邮箱</el-button>
                </div>
              </div>
            </div>

            <!-- 偏好设置 -->
            <div v-show="activeTab === 'preferences'" class="content-section">
              <div class="section-header">
                <h3>偏好设置</h3>
              </div>
              
              <el-form :model="preferences" label-width="120px">
                <el-form-item label="主题设置">
                  <ThemeToggle />
                </el-form-item>
                
                <el-form-item label="消息通知">
                  <el-switch v-model="preferences.notifications" />
                  <span class="form-help">接收订单状态变更通知</span>
                </el-form-item>
                
                <el-form-item label="邮件通知">
                  <el-switch v-model="preferences.emailNotifications" />
                  <span class="form-help">接收优惠活动邮件</span>
                </el-form-item>
                
                <el-form-item label="短信通知">
                  <el-switch v-model="preferences.smsNotifications" />
                  <span class="form-help">接收预约提醒短信</span>
                </el-form-item>
                
                <el-form-item label="默认车辆">
                  <el-select v-model="preferences.defaultVehicle" placeholder="选择默认车辆">
                    <el-option 
                      v-for="vehicle in vehicles" 
                      :key="vehicle.id"
                      :label="vehicle.plateNumber"
                      :value="vehicle.id"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="savePreferences">保存设置</el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 积分记录 -->
            <div v-show="activeTab === 'points'" class="content-section">
              <div class="section-header">
                <h3>积分记录</h3>
                <div class="points-summary">
                  <span class="current-points">当前积分：{{ userStats.points }}</span>
                </div>
              </div>
              
              <div class="points-list">
                <div class="points-item" v-for="record in pointsRecords" :key="record.id">
                  <div class="points-info">
                    <h4>{{ record.description }}</h4>
                    <p class="points-date">{{ formatDate(record.date) }}</p>
                  </div>
                  <div class="points-change" :class="{ positive: record.change > 0, negative: record.change < 0 }">
                    {{ record.change > 0 ? '+' : '' }}{{ record.change }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 车辆编辑对话框 -->
    <el-dialog v-model="vehicleDialogVisible" :title="vehicleDialogTitle" width="500px">
      <el-form :model="vehicleForm" :rules="vehicleRules" ref="vehicleFormRef" label-width="80px">
        <el-form-item label="车牌号码" prop="plateNumber">
          <el-input v-model="vehicleForm.plateNumber" placeholder="请输入车牌号码" />
        </el-form-item>
        <el-form-item label="车辆品牌" prop="brand">
          <el-select v-model="vehicleForm.brand" placeholder="请选择车辆品牌" filterable>
            <el-option v-for="brand in carBrands" :key="brand" :label="brand" :value="brand" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆型号" prop="model">
          <el-input v-model="vehicleForm.model" placeholder="请输入车辆型号" />
        </el-form-item>
        <el-form-item label="车辆颜色" prop="color">
          <el-select v-model="vehicleForm.color" placeholder="请选择车辆颜色">
            <el-option label="白色" value="white" />
            <el-option label="黑色" value="black" />
            <el-option label="银色" value="silver" />
            <el-option label="红色" value="red" />
            <el-option label="蓝色" value="blue" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vehicleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVehicle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ThemeToggle from '../components/ThemeToggle.vue'

export default {
  name: 'Profile',
  components: {
    ThemeToggle
  },
  setup() {
    const activeTab = ref('info')
    const editMode = ref(false)
    const vehicleDialogVisible = ref(false)
    const currentVehicle = ref(null)
    const userFormRef = ref()
    const vehicleFormRef = ref()
    
    // 导航菜单
    const navItems = ref([
      { key: 'info', label: '个人信息', icon: 'User' },
      { key: 'vehicles', label: '车辆管理', icon: 'Car' },
      { key: 'security', label: '安全设置', icon: 'Lock' },
      { key: 'preferences', label: '偏好设置', icon: 'Setting' },
      { key: 'points', label: '积分记录', icon: 'Star' }
    ])
    
    // 用户信息
    const userInfo = ref({
      name: '张先生',
      level: 'VIP会员',
      avatar: ''
    })
    
    // 用户统计
    const userStats = ref({
      totalOrders: 15,
      totalSpent: '¥1,280',
      points: 2580
    })
    
    // 用户表单
    const userForm = ref({
      username: 'zhangsan',
      realName: '张三',
      phone: '13800138000',
      email: 'zhangsan@example.com',
      gender: 'male',
      birthday: new Date('1990-01-01'),
      address: '北京市朝阳区xxx街道xxx号'
    })
    
    // 用户表单验证规则
    const userRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }
    
    // 车辆列表
    const vehicles = ref([
      {
        id: 1,
        plateNumber: '京A12345',
        brand: '奥迪',
        model: 'A4L',
        color: 'white'
      },
      {
        id: 2,
        plateNumber: '京B67890',
        brand: '宝马',
        model: 'X3',
        color: 'black'
      }
    ])
    
    // 车辆表单
    const vehicleForm = ref({
      plateNumber: '',
      brand: '',
      model: '',
      color: ''
    })
    
    // 车辆表单验证规则
    const vehicleRules = {
      plateNumber: [
        { required: true, message: '请输入车牌号码', trigger: 'blur' }
      ],
      brand: [
        { required: true, message: '请选择车辆品牌', trigger: 'change' }
      ],
      model: [
        { required: true, message: '请输入车辆型号', trigger: 'blur' }
      ],
      color: [
        { required: true, message: '请选择车辆颜色', trigger: 'change' }
      ]
    }
    
    // 车辆品牌
    const carBrands = ref([
      '奥迪', '宝马', '奔驰', '大众', '丰田', '本田', '日产', '现代',
      '起亚', '福特', '雪佛兰', '别克', '凯迪拉克', '沃尔沃', '捷豹',
      '路虎', '保时捷', '特斯拉', '蔚来', '小鹏', '理想', '比亚迪'
    ])
    
    // 偏好设置
    const preferences = ref({
      notifications: true,
      emailNotifications: false,
      smsNotifications: true,
      defaultVehicle: 1
    })
    
    // 积分记录
    const pointsRecords = ref([
      {
        id: 1,
        description: '完成洗车服务',
        change: 50,
        date: new Date('2025-09-26')
      },
      {
        id: 2,
        description: '邀请好友注册',
        change: 100,
        date: new Date('2025-09-25')
      },
      {
        id: 3,
        description: '积分兑换优惠券',
        change: -200,
        date: new Date('2025-09-24')
      }
    ])
    
    // 车辆对话框标题
    const vehicleDialogTitle = computed(() => {
      return currentVehicle.value ? '编辑车辆' : '添加车辆'
    })
    
    // 获取颜色名称
    const getColorName = (color) => {
      const colorMap = {
        white: '白色',
        black: '黑色',
        silver: '银色',
        red: '红色',
        blue: '蓝色',
        other: '其他'
      }
      return colorMap[color] || color
    }
    
    // 格式化日期
    const formatDate = (date) => {
      return date.toLocaleDateString('zh-CN')
    }
    
    // 保存用户信息
    const saveUserInfo = async () => {
      try {
        await userFormRef.value.validate()
        ElMessage.success('个人信息保存成功')
        editMode.value = false
      } catch {
        // 验证失败
      }
    }
    
    // 添加车辆
    const addVehicle = () => {
      currentVehicle.value = null
      vehicleForm.value = {
        plateNumber: '',
        brand: '',
        model: '',
        color: ''
      }
      vehicleDialogVisible.value = true
    }
    
    // 编辑车辆
    const editVehicle = (vehicle) => {
      currentVehicle.value = vehicle
      vehicleForm.value = { ...vehicle }
      vehicleDialogVisible.value = true
    }
    
    // 保存车辆
    const saveVehicle = async () => {
      try {
        await vehicleFormRef.value.validate()
        
        if (currentVehicle.value) {
          const index = vehicles.value.findIndex(v => v.id === currentVehicle.value.id)
          if (index !== -1) {
            vehicles.value[index] = { ...vehicleForm.value, id: currentVehicle.value.id }
          }
          ElMessage.success('车辆信息更新成功')
        } else {
          const newVehicle = {
            ...vehicleForm.value,
            id: Date.now()
          }
          vehicles.value.push(newVehicle)
          ElMessage.success('车辆添加成功')
        }
        
        vehicleDialogVisible.value = false
      } catch {
        // 验证失败
      }
    }
    
    // 删除车辆
    const deleteVehicle = async (vehicle) => {
      try {
        await ElMessageBox.confirm('确定要删除这辆车吗？', '删除车辆', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const index = vehicles.value.findIndex(v => v.id === vehicle.id)
        if (index !== -1) {
          vehicles.value.splice(index, 1)
          ElMessage.success('车辆删除成功')
        }
      } catch {
        // 用户取消
      }
    }
    
    // 修改密码
    const changePassword = () => {
      ElMessage.info('修改密码功能开发中...')
    }
    
    // 保存偏好设置
    const savePreferences = () => {
      ElMessage.success('偏好设置保存成功')
    }
    
    // 初始化
    onMounted(() => {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (storedUserInfo) {
        try {
          const parsed = JSON.parse(storedUserInfo)
          userInfo.value.name = parsed.username || parsed.name || '用户'
        } catch (error) {
          console.error('解析用户信息失败:', error)
        }
      }
    })
    
    return {
      activeTab,
      editMode,
      vehicleDialogVisible,
      vehicleDialogTitle,
      userFormRef,
      vehicleFormRef,
      navItems,
      userInfo,
      userStats,
      userForm,
      userRules,
      vehicles,
      vehicleForm,
      vehicleRules,
      carBrands,
      preferences,
      pointsRecords,
      getColorName,
      formatDate,
      saveUserInfo,
      addVehicle,
      editVehicle,
      saveVehicle,
      deleteVehicle,
      changePassword,
      savePreferences
    }
  }
}
</script>