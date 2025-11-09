<template>
  <div class="admin-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><Setting /></el-icon>
          <span>汽车洗车管理系统</span>
        </div>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <el-tooltip content="刷新数据" placement="bottom">
            <el-button circle :icon="Refresh" @click="refreshData" :loading="refreshing" />
          </el-tooltip>
          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="32">{{ userInfo.name?.[0] || 'A' }}</el-avatar>
              <span class="username">{{ userInfo.name || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <div class="dashboard-main">
      <!-- 侧边栏 -->
      <aside class="dashboard-sidebar">
        <el-menu
          :default-active="activeTab"
          :active-index="activeTab"
          class="sidebar-menu"
          @select="handleTabChange"
          :collapse="sidebarCollapsed"
        >
          <el-menu-item index="overview">
            <el-icon><DataBoard /></el-icon>
            <template #title>数据总览</template>
          </el-menu-item>
          <el-menu-item index="bookings">
            <el-icon><Calendar /></el-icon>
            <template #title>预约管理</template>
          </el-menu-item>
          <el-menu-item index="services">
            <el-icon><Tools /></el-icon>
            <template #title>服务管理</template>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="paymentAudit">
            <el-icon><Money /></el-icon>
            <template #title>支付审计</template>
          </el-menu-item>
          <el-menu-item index="system">
            <el-icon><Setting /></el-icon>
            <template #title>系统管理</template>
          </el-menu-item>
        </el-menu>
        
        <div class="sidebar-toggle" @click="toggleSidebar">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </div>
      </aside>

      <!-- 内容区域 -->
      <main class="dashboard-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <!-- 数据总览 -->
        <div v-if="activeTab === 'overview'" class="tab-content overview-tab">
          <div class="page-header">
            <h1>数据总览</h1>
            <p>实时业务数据监控与统计分析</p>
          </div>

          <!-- 关键指标卡片 -->
          <div class="metrics-grid">
            <div v-for="metric in metricsData" :key="metric.id" class="metric-card" :class="metric.type">
              <div class="metric-icon">
                <el-icon><component :is="metric.icon" /></el-icon>
              </div>
              <div class="metric-content">
                <div class="metric-value">{{ metric.value }}</div>
                <div class="metric-label">{{ metric.label }}</div>
                <div class="metric-change" :class="{ positive: metric.change > 0, negative: metric.change < 0 }">
                  <el-icon><ArrowUp v-if="metric.change > 0" /><ArrowDown v-else /></el-icon>
                  {{ Math.abs(metric.change) }}%
                </div>
              </div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts-section">
            <div class="chart-row">
              <div class="chart-container">
                <div class="chart-header">
                  <h3>预约趋势分析</h3>
                  <el-select v-model="trendPeriod" size="small" style="width: 120px">
                    <el-option label="最近7天" value="7days" />
                    <el-option label="最近30天" value="30days" />
                  </el-select>
                </div>
                <div ref="trendChart" class="chart"></div>
              </div>

              <div class="chart-container">
                <div class="chart-header">
                  <h3>服务类型分布</h3>
                </div>
                <div ref="serviceChart" class="chart"></div>
              </div>
            </div>

            <div class="chart-row">
              <div class="chart-container full-width">
                <div class="chart-header">
                  <h3>收入统计</h3>
                  <el-radio-group v-model="revenueType" size="small">
                    <el-radio-button label="daily">日收入</el-radio-button>
                    <el-radio-button label="monthly">月收入</el-radio-button>
                  </el-radio-group>
                </div>
                <div ref="revenueChart" class="chart large"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 预约管理 -->
        <div v-if="activeTab === 'bookings'" class="tab-content booking-management">
          <div class="page-header">
            <h1>预约管理</h1>
            <div class="header-actions">
              <el-input
                v-model="bookingSearch"
                placeholder="搜索预约记录..."
                :prefix-icon="Search"
                style="width: 300px; margin-right: 16px;"
                clearable
              />
              <el-select
                v-model="bookingStatusFilter"
                placeholder="状态筛选"
                style="width: 150px; margin-right: 16px;"
                clearable
              >
                <el-option label="全部" value="" />
                <el-option label="待确认" value="pending" />
                <el-option label="已确认" value="confirmed" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
              <el-button type="primary" :icon="Refresh" @click="refreshData">
                刷新
              </el-button>
            </div>
          </div>

          <div class="table-container">
            <el-table
              :data="filteredBookings"
              v-loading="bookingsLoading"
              stripe
              style="width: 100%"
              :default-sort="{ prop: 'createTime', order: 'descending' }"
            >
              <el-table-column prop="id" label="预约ID" width="100" />
              <el-table-column prop="serviceName" label="服务项目" width="150" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="customerPhone" label="联系电话" width="130" />
              <el-table-column prop="appointmentTime" label="预约时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getBookingStatusType(row.status)">
                    {{ getBookingStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'pending'"
                    type="success"
                    size="small"
                    @click="confirmBooking(row)"
                  >
                    确认
                  </el-button>
                  <el-button
                    v-if="row.status === 'confirmed'"
                    type="primary"
                    size="small"
                    @click="startService(row)"
                  >
                    开始服务
                  </el-button>
                  <el-button
                    v-if="row.status === 'in_progress'"
                    type="warning"
                    size="small"
                    @click="completeService(row)"
                  >
                    完成服务
                  </el-button>
                  <el-button
                    v-if="['pending', 'confirmed'].includes(row.status)"
                    type="danger"
                    size="small"
                    @click="cancelBooking(row)"
                  >
                    取消
                  </el-button>
                  <el-button
                    type="info"
                    size="small"
                    @click="viewBookingDetail(row)"
                  >
                    详情
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteBooking(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 服务管理 -->
        <div v-if="activeTab === 'services'" class="tab-content service-management">
          <div class="page-header">
            <h1>服务管理</h1>
            <div class="header-actions">
              <el-button type="primary" :icon="Plus" @click="showAddServiceDialog">
                添加服务
              </el-button>
              <el-button type="default" :icon="Refresh" @click="refreshData">
                刷新
              </el-button>
            </div>
          </div>

          <div class="table-container">
            <el-table
              :data="services"
              v-loading="servicesLoading"
              stripe
              style="width: 100%"
            >
              <el-table-column prop="id" label="服务ID" width="100" />
              <el-table-column prop="name" label="服务名称" width="200" />
              <el-table-column prop="description" label="服务描述" min-width="250" />
              <el-table-column prop="price" label="价格" width="120">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="时长" width="100">
                <template #default="{ row }">
                  {{ row.duration }}分钟
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                    {{ row.status === 'active' ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    size="small"
                    @click="editService(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    :type="row.status === 'active' ? 'warning' : 'success'"
                    size="small"
                    @click="toggleServiceStatus(row)"
                  >
                    {{ row.status === 'active' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteService(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="tab-content">
          <div class="page-header">
            <h1>用户管理</h1>
            <div class="header-actions">
              <el-input
                v-model="userSearch"
                placeholder="搜索用户..."
                :prefix-icon="Search"
                style="width: 200px;"
              />
            </div>
          </div>

          <div class="table-container">
            <el-table :data="filteredUsers" style="width: 100%" v-loading="usersLoading">
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="realName" label="真实姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="140" />
              <el-table-column prop="email" label="邮箱" min-width="180" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
                    {{ scope.row.role === 'admin' ? '管理员' : '用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="注册时间" width="180" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    :type="scope.row.status === 1 ? 'warning' : 'success'"
                    @click="toggleUserStatus(scope.row)"
                  >
                    {{ scope.row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" @click="viewUserDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 支付审计 -->
        <div v-if="activeTab === 'paymentAudit'" class="tab-content">
          <div class="page-header">
            <h1>支付审计</h1>
            <p>查看支付相关审计日志，支持筛选与分页</p>
            <div class="header-actions action-buttons">
              <el-button type="primary" @click="refreshData">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </div>
          </div>

          <div class="search-filters">
            <el-input
              v-model="auditQuery.paymentNo"
              placeholder="支付单号"
              style="width: 200px;"
            />
            <el-input
              v-model="auditQuery.orderNo"
              placeholder="订单号"
              style="width: 200px;"
            />
            <el-input
              v-model="auditQuery.eventType"
              placeholder="事件类型"
              style="width: 180px;"
            />
            <el-input
              v-model="auditQuery.status"
              placeholder="状态"
              style="width: 160px;"
            />
            <el-button type="primary" @click="onAuditFilterChange">
              <el-icon><Search /></el-icon>筛选
            </el-button>
          </div>

          <div class="table-container">
            <el-table :data="audits" style="width: 100%" v-loading="auditLoading">
              <el-table-column prop="paymentNo" label="支付单号" width="180" />
              <el-table-column prop="orderNo" label="订单号" width="180" />
              <el-table-column prop="eventType" label="事件类型" width="140" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : scope.row.status === 'FAILED' ? 'danger' : 'info'">
                    {{ scope.row.status || '未知' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="日志信息" min-width="240" />
              <el-table-column prop="createTime" label="时间" width="180" />
            </el-table>
          </div>

          <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
            <el-pagination
              v-model:current-page="auditPagination.current"
              v-model:page-size="auditPagination.size"
              :total="auditPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleAuditSizeChange"
              @current-change="handleAuditCurrentChange"
            />
          </div>
        </div>

        <!-- 系统管理 -->
        <div v-if="activeTab === 'system'" class="tab-content">
          <div class="page-header">
            <h1>系统管理</h1>
            <p>系统配置与维护工具</p>
          </div>

          <div class="system-section">
            <div class="system-cards">
              <div class="system-card">
                <div class="card-header">
                  <h3>系统信息</h3>
                </div>
                <div class="card-content">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="系统名称">汽车洗车服务预约系统</el-descriptions-item>
                    <el-descriptions-item label="版本">v2.0.0</el-descriptions-item>
                    <el-descriptions-item label="运行时间">{{ systemUptime }}</el-descriptions-item>
                    <el-descriptions-item label="数据库状态">
                      <el-tag type="success">正常连接</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="缓存状态">
                      <el-tag type="success">运行中</el-tag>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>

              <div class="system-card">
                <div class="card-header">
                  <h3>系统工具</h3>
                </div>
                <div class="card-content">
                  <div class="tool-buttons">
                    <el-button type="primary" @click="$router.push('/api-test')">
                      <el-icon><Link /></el-icon>API 测试
                    </el-button>
                    <el-button type="warning" @click="clearSystemCache">
                      <el-icon><Delete /></el-icon>清除缓存
                    </el-button>
                    <el-button type="info" @click="exportSystemLogs">
                      <el-icon><Download /></el-icon>导出日志
                    </el-button>
                    <el-button type="success" @click="backupDatabase">
                      <el-icon><DocumentCopy /></el-icon>数据备份
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 添加服务对话框 -->
    <el-dialog v-model="showServiceDialog" title="添加服务" width="600px">
      <el-form :model="serviceForm" :rules="serviceRules" ref="serviceFormRef" label-width="80px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入服务描述"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="serviceForm.price" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="时长" prop="duration">
          <el-input-number v-model="serviceForm.duration" :min="15" :step="15" />
          <span style="margin-left: 8px;">分钟</span>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="serviceForm.category" placeholder="请选择分类">
            <el-option label="基础洗车" value="basic" />
            <el-option label="精洗套餐" value="premium" />
            <el-option label="内饰清洁" value="interior" />
            <el-option label="美容服务" value="beauty" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showServiceDialog = false">取消</el-button>
        <el-button type="primary" @click="saveService" :loading="serviceSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import {
  Setting, Refresh, ArrowDown, User, SwitchButton,
  DataBoard, Calendar, Tools, Fold, Expand,
  Search, Plus, Link, Delete, Download, DocumentCopy,
  ArrowUp, Money, Star
} from '@element-plus/icons-vue'
import { useAdminDashboard } from './AdminScript.js'

const {
  // 响应式数据
  activeTab,
  sidebarCollapsed,
  refreshing,
  userInfo,
  metricsData,
  trendPeriod,
  revenueType,
  bookings,
  bookingsLoading,
  bookingSearch,
  bookingStatusFilter,
  services,
  servicesLoading,
  showServiceDialog,
  serviceSaving,
  serviceFormRef,
  serviceForm,
  serviceRules,
  users,
  usersLoading,
  userSearch,
  systemUptime,
  trendChart,
  serviceChart,
  revenueChart,
  audits,
  auditLoading,
  auditQuery,
  auditPagination,

  // 计算属性
  filteredBookings,
  filteredUsers,

  // 方法
  toggleSidebar,
  handleTabChange,
  handleUserCommand,
  refreshData,
  getBookingStatusType,
  getBookingStatusText,
  confirmBooking,
  startService,
  completeService,
  cancelBooking,
  viewBookingDetail,
  showAddServiceDialog,
  editService,
  toggleServiceStatus,
  deleteService,
  saveService,
  toggleUserStatus,
  viewUserDetail,
  clearSystemCache,
  exportSystemLogs,
  backupDatabase,
  onAuditFilterChange,
  handleAuditSizeChange,
  handleAuditCurrentChange,
  initialize
} = useAdminDashboard()

onMounted(() => {
  console.log('[Admin.vue] onMounted 被调用')
  // 确保在DOM完全渲染后再初始化
  setTimeout(() => {
    console.log('[Admin.vue] 开始执行initialize')
    initialize()
  }, 100)
})
</script>

<style scoped>
/* ==================== 绿色主题管理后台 ==================== */

/* 主色调定义 */
:root {
  --primary-green: #52c41a;
  --primary-green-light: #73d13d;
  --primary-green-dark: #389e0d;
  --primary-green-bg: #f6ffed;
  --secondary-white: #ffffff;
  --text-primary: #262626;
  --text-secondary: #595959;
  --text-light: #8c8c8c;
  --border-color: #d9d9d9;
  --bg-gray: #fafafa;
}

.admin-dashboard {
  height: 100vh;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
  display: flex;
  flex-direction: column;
}

/* ==================== 顶部导航栏 ==================== */
.dashboard-header {
  height: 64px;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
  border-bottom: 2px solid #389e0d;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.15);
  z-index: 1000;
}

.header-left .logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}

.logo-icon {
  margin-right: 12px;
  font-size: 28px;
  color: #ffffff;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions :deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

.header-actions :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: #ffffff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.15);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
}

.user-info :deep(.el-avatar) {
  background: #ffffff;
  color: #52c41a;
  font-weight: 600;
}

.username {
  font-size: 15px;
  color: #ffffff;
  font-weight: 500;
}

.user-info .el-icon {
  color: #ffffff;
}

/* ==================== 主体内容区 ==================== */
.dashboard-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ==================== 侧边栏 ==================== */
.dashboard-sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
  position: relative;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

.dashboard-sidebar :deep(.el-menu) {
  border-right: none;
  height: 100%;
  background: #ffffff;
}

.dashboard-sidebar :deep(.el-menu-item) {
  height: 56px;
  line-height: 56px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.dashboard-sidebar :deep(.el-menu-item:hover) {
  background: #f6ffed;
  color: #52c41a;
}

.dashboard-sidebar :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.25);
}

.dashboard-sidebar :deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff;
}

.sidebar-toggle {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  background: #f6ffed;
  border: 2px solid #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #52c41a;
}

.sidebar-toggle:hover {
  background: #52c41a;
  color: #ffffff;
  transform: translateX(-50%) scale(1.1);
}

/* ==================== 内容区域 ==================== */
.dashboard-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  transition: margin-left 0.3s ease;
  background: transparent;
}

.dashboard-content.sidebar-collapsed {
  margin-left: -176px;
}

.tab-content {
  max-width: 1600px;
  margin: 0 auto;
}

/* ==================== 页面头部 ==================== */
.page-header {
  margin-bottom: 32px;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #52c41a;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 8px 0;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  color: #8c8c8c;
  margin: 0;
  font-size: 14px;
}

.page-header .header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.page-header .header-actions :deep(.el-button--primary) {
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.page-header .header-actions :deep(.el-button--primary:hover) {
  background: linear-gradient(90deg, #73d13d 0%, #95de64 100%);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
}

/* ==================== 指标卡片 ==================== */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.metric-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.2);
  border-color: #52c41a;
}

.metric-card.primary .metric-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.metric-card.success .metric-icon {
  background: linear-gradient(135deg, #389e0d 0%, #52c41a 100%);
}

.metric-card.warning .metric-icon {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.metric-card.info .metric-icon {
  background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
}

.metric-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.metric-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
  font-weight: 500;
}

.metric-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
}

.metric-change.positive {
  color: #52c41a;
}

.metric-change.negative {
  color: #ff4d4f;
}

/* ==================== 图表区域 ==================== */
.charts-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 24px;
}

.chart-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.chart-container:hover {
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.15);
  border-color: #52c41a;
}

.chart-container.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  padding: 20px 28px;
  border-bottom: 2px solid #f6ffed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #f6ffed 0%, #ffffff 100%);
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.chart-header :deep(.el-select) {
  border-color: #52c41a;
}

.chart-header :deep(.el-radio-button__inner) {
  border-color: #52c41a;
  color: #52c41a;
}

.chart-header :deep(.el-radio-button__original-radio:checked+.el-radio-button__inner) {
  background: #52c41a;
  border-color: #52c41a;
  color: #ffffff;
}

.chart {
  width: 100%;
  height: 320px;
  min-height: 320px;
  padding: 16px;
}

.chart.large {
  height: 420px;
  min-height: 420px;
}

/* ==================== 表格容器 ==================== */
.table-container {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.table-container :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

.table-container :deep(.el-table thead th) {
  background: #f6ffed;
  color: #262626;
  font-weight: 600;
  border-bottom: 2px solid #52c41a;
}

.table-container :deep(.el-table tbody tr:hover) {
  background: #f6ffed;
}

.table-container :deep(.el-button--primary) {
  background: #52c41a;
  border-color: #52c41a;
}

.table-container :deep(.el-button--primary:hover) {
  background: #73d13d;
  border-color: #73d13d;
}

.table-container :deep(.el-button--success) {
  background: #52c41a;
  border-color: #52c41a;
}

.table-container :deep(.el-button--success:hover) {
  background: #389e0d;
  border-color: #389e0d;
}

.table-container :deep(.el-tag--success) {
  background: #f6ffed;
  color: #52c41a;
  border-color: #b7eb8f;
}

/* ==================== 卡片列表 ==================== */
.services-grid,
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.service-card,
.user-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.service-card::before,
.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
}

.service-card:hover,
.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.2);
  border-color: #52c41a;
}

/* ==================== 系统管理 ==================== */
.system-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.system-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.system-card:hover {
  border-color: #52c41a;
  box-shadow: 0 8px 24px rgba(82, 196, 26, 0.15);
}

.system-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #f6ffed;
}

.system-info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.system-info-item:last-child {
  border-bottom: none;
}

.system-info-label {
  color: #8c8c8c;
  font-weight: 500;
}

.system-info-value {
  color: #262626;
  font-weight: 600;
}

.system-actions {
  display: grid;
  gap: 12px;
  margin-top: 20px;
}

.system-actions :deep(.el-button) {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-weight: 500;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 768px) {
  .dashboard-header {
    padding: 0 16px;
  }
  
  .dashboard-content {
    padding: 16px;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-row {
    grid-template-columns: 1fr;
  }
}

/* ==================== 加载动画 ==================== */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tab-content {
  animation: fadeIn 0.4s ease-out;
}

/* ==================== 滚动条样式 ==================== */
.dashboard-content::-webkit-scrollbar {
  width: 8px;
}

.dashboard-content::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 4px;
}

.dashboard-content::-webkit-scrollbar-thumb {
  background: #52c41a;
  border-radius: 4px;
}

.dashboard-content::-webkit-scrollbar-thumb:hover {
  background: #389e0d;
}

/* ==================== 额外样式 ==================== */
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.price {
  font-weight: 600;
  color: #52c41a;
}

.system-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.system-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.system-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.card-content {
  padding: 20px;
}

.tool-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

/* 修复并发显示问题的关键样式 */
.booking-management {
  isolation: isolate;
  contain: layout style paint;
}

.service-management {
  isolation: isolate;
  contain: layout style paint;
}

/* 优化表格渲染性能 */
.el-table {
  contain: layout;
}

.el-table-column {
  contain: layout style;
}

/* 搜索和筛选区域样式 */
.search-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 加载状态优化 */
.el-loading-mask {
  border-radius: 8px;
}

/* 表格行悬停效果 */
.el-table tbody tr:hover > td {
  background-color: #f5f7fa !important;
}
</style>