<template>
  <div class="orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">我的订单</h1>
            <p class="page-subtitle">查看和管理您的洗车服务订单</p>
          </div>
          <div class="header-actions">
            <el-button
              type="primary"
              :icon="Refresh"
              @click="forceRefreshOrders"
              :loading="loading"
              circle
              title="刷新订单数据"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="orders-container">
      <div class="container">
        <!-- 订单筛选 -->
        <div class="orders-filters">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部订单" name="all">
              <template #label>
                <span class="tab-label">
                  全部订单
                  <el-badge
                    :value="orderCounts.all"
                    :hidden="orderCounts.all === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="待确认" name="pending">
              <template #label>
                <span class="tab-label">
                  待确认
                  <el-badge
                    :value="orderCounts.pending"
                    :hidden="orderCounts.pending === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已确认" name="confirmed">
              <template #label>
                <span class="tab-label">
                  已确认
                  <el-badge
                    :value="orderCounts.confirmed"
                    :hidden="orderCounts.confirmed === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="服务中" name="in-service">
              <template #label>
                <span class="tab-label">
                  服务中
                  <el-badge
                    :value="orderCounts.inService"
                    :hidden="orderCounts.inService === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已完成" name="completed">
              <template #label>
                <span class="tab-label">
                  已完成
                  <el-badge
                    :value="orderCounts.completed"
                    :hidden="orderCounts.completed === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已取消" name="cancelled">
              <template #label>
                <span class="tab-label">
                  已取消
                  <el-badge
                    :value="orderCounts.cancelled"
                    :hidden="orderCounts.cancelled === 0"
                  />
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 订单列表 -->
        <div class="orders-list">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>

          <div v-else-if="hasError" class="error-state">
            <el-result
              icon="warning"
              title="加载失败"
              sub-title="无法获取订单数据，请检查网络连接或重新登录"
            >
              <template #extra>
                <el-button type="primary" @click="fetchUserOrders"
                  >重新加载</el-button
                >
                <el-button @click="$router.push('/login')">重新登录</el-button>
              </template>
            </el-result>
          </div>

          <div v-else-if="filteredOrders.length === 0" class="empty-state">
            <el-empty :description="getEmptyDescription()">
              <router-link to="/appointment">
                <el-button type="primary">立即预约</el-button>
              </router-link>
            </el-empty>
          </div>

          <div v-else class="orders-grid">
            <div
              class="order-card"
              v-for="order in filteredOrders"
              :key="order.id"
            >
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-info">
                  <span class="order-number"
                    >订单号：{{ order.orderNumber }}</span
                  >
                  <span class="order-date">{{
                    formatDate(order.createTime)
                  }}</span>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusType(order.status)" size="large">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 订单内容 -->
              <div class="order-content">
                <div class="service-info">
                  <div class="service-icon">
                    <el-icon size="32" :color="order.service.color">
                      <component :is="order.service.icon" />
                    </el-icon>
                  </div>
                  <div class="service-details">
                    <h4 class="service-name">{{ order.service.name }}</h4>
                    <p class="service-desc">{{ order.service.description }}</p>
                    <div class="service-meta">
                      <span class="price">¥{{ order.service.price }}</span>
                      <span class="duration">{{ order.service.duration }}</span>
                    </div>
                  </div>
                </div>

                <!-- 订单状态进度 -->
                <div class="order-progress-section">
                  <OrderStatusProgress
                    :order="order"
                    :show-refresh="true"
                    @refresh="refreshOrderStatus"
                  />
                </div>

                <div class="appointment-info">
                  <div class="info-row">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDateTime(order.appointmentTime) }}</span>
                  </div>
                  <div class="info-row">
                    <el-icon><Service /></el-icon>
                    <span
                      >{{ order.vehicle.plateNumber }} ({{
                        order.vehicle.brand
                      }}
                      {{ order.vehicle.model }})</span
                    >
                  </div>
                  <div class="info-row">
                    <el-icon><Phone /></el-icon>
                    <span>{{ order.vehicle.phone }}</span>
                  </div>
                  <div v-if="order.vehicle.requirements" class="info-row">
                    <el-icon><Document /></el-icon>
                    <span>{{ order.vehicle.requirements }}</span>
                  </div>
                </div>
              </div>

              <!-- 订单操作 -->
              <div class="order-actions">
                <div class="action-buttons">
                  <el-button
                    v-if="
                      (order.status === 'confirmed' ||
                        order.status === 'pending') &&
                      order.paymentStatus !== 'paid'
                    "
                    type="primary"
                    size="small"
                    class="pay-button"
                    :aria-label="`为订单 ${order.orderNumber} 付款`"
                    title="付款"
                    @click="payOrder(order)"
                  >
                    付款
                  </el-button>
                  <el-button
                    v-if="order.status === 'pending'"
                    type="danger"
                    size="small"
                    plain
                    @click="cancelOrder(order)"
                  >
                    取消订单
                  </el-button>

                  <el-button
                    v-if="order.status === 'confirmed'"
                    type="warning"
                    size="small"
                    plain
                    @click="modifyOrder(order)"
                  >
                    修改预约
                  </el-button>

                  <el-button
                    v-if="order.status === 'completed' && !order.reviewed"
                    type="primary"
                    size="small"
                    @click="reviewOrder(order)"
                  >
                    评价服务
                  </el-button>

                  <el-button
                    v-if="order.status === 'completed' && order.reviewed"
                    size="small"
                    plain
                    @click="viewReview(order)"
                  >
                    查看评价
                  </el-button>

                  <el-button size="small" plain @click="viewOrderDetail(order)">
                    查看详情
                  </el-button>

                  <el-button
                    type="success"
                    size="small"
                    plain
                    @click="rebookOrder(order)"
                  >
                    再次预约
                  </el-button>
                </div>
              </div>

              <!-- 进度条 -->
              <div v-if="order.status !== 'cancelled'" class="order-progress">
                <el-steps
                  :active="getProgressStep(order.status)"
                  finish-status="success"
                  simple
                >
                  <el-step title="订单提交" />
                  <el-step title="确认预约" />
                  <el-step title="开始服务" />
                  <el-step title="服务完成" />
                </el-steps>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="filteredOrders.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="totalOrders"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="服务评价" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="服务评分">
          <el-rate v-model="reviewForm.rating" :max="5" show-text />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请分享您的服务体验..."
          />
        </el-form-item>
        <el-form-item label="服务标签">
          <el-checkbox-group v-model="reviewForm.tags">
            <el-checkbox label="服务态度好" />
            <el-checkbox label="清洗效果佳" />
            <el-checkbox label="速度快" />
            <el-checkbox label="价格合理" />
            <el-checkbox label="环境整洁" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  ref,
  computed,
  onMounted,
  onActivated,
  onUnmounted,
  watch,
  nextTick,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Refresh, Service, Calendar, Phone } from "@element-plus/icons-vue";
import { TimeUtils } from "@/utils/timeUtils";
import { realApi } from "@/api/realApi";
import { useUserStore } from "@/stores/user";
import { useOrderSync, forceRefreshOrders } from "@/utils/orderSync";
import { AuthManager } from "@/utils/auth";
import { useWebSocket } from "@/composables/useWebSocket";
import { useEnhancedWebSocket } from "@/utils/enhancedWebSocket";
import OrderStatusProgress from "@/components/OrderStatusProgress.vue";
import paymentApi from "@/api/payment";

export default {
  name: "Orders",
  components: {
    OrderStatusProgress,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();
    const userStore = useUserStore();
    const { onOrderStatusUpdate } = useWebSocket();

    // 使用增强的WebSocket管理器
    const enhancedWS = useEnhancedWebSocket();

    const activeTab = ref("all");
    const currentPage = ref(1);
    const pageSize = ref(10);
    const reviewDialogVisible = ref(false);
    const currentReviewOrder = ref(null);

    // 评价表单
    const reviewForm = ref({
      rating: 5,
      content: "",
      tags: [],
    });

    let unsubscribe = null;

    onMounted(() => {
      console.log("Orders.vue onMounted");
      // 订阅订单状态更新
      unsubscribe = onOrderStatusUpdate((data) => {
        console.log("WebSocket 收到订单状态更新:", data);
        // 强制刷新订单列表
        forceRefreshOrders();
      });
    });

    onUnmounted(() => {
      console.log("Orders.vue onUnmounted");
      // 取消订阅
      if (unsubscribe) {
        unsubscribe();
      }
    });

    onActivated(() => {
      console.log("Orders.vue onActivated");
      // 页面被激活时，强制刷新订单数据
      forceRefreshOrders();

      // 检查路由参数，确定是否需要显示特定标签页
      if (route.query.status) {
        activeTab.value = route.query.status;
      }
    });

    // 服务图标映射
    const serviceIconMap = {
      1: "Service",
      2: "Star",
      3: "Trophy",
      4: "Setting",
      5: "Tools",
    };

    // 服务颜色映射
    const serviceColorMap = {
      1: "var(--primary-color)",
      2: "var(--warning-color)",
      3: "var(--error-color)",
      4: "var(--success-color)",
      5: "var(--info-color)",
    };

    // 获取当前用户ID
    const getCurrentUserId = () => {
      console.log("🔍 开始获取用户ID...");

      const user = AuthManager.getCurrentUser();
      console.log("👤 AuthManager.getCurrentUser():", user);
      if (user && user.id) {
        console.log("✅ 从AuthManager获取到用户ID:", user.id);
        return user.id;
      }

      // 如果没有用户信息，尝试从localStorage获取
      const userInfo = localStorage.getItem("userInfo");
      console.log("💾 localStorage userInfo:", userInfo);
      if (userInfo) {
        try {
          const parsed = JSON.parse(userInfo);
          console.log("📋 解析后的用户信息:", parsed);
          const userId = parsed.id || parsed.userId;
          if (userId) {
            console.log("✅ 从localStorage获取到用户ID:", userId);
            return userId;
          }
        } catch (error) {
          console.error("❌ 解析用户信息失败:", error);
        }
      }

      // 如果无法获取用户ID，返回null
      console.error("❌ 无法获取用户ID，用户可能未登录");
      return null;
    };

    // 防重复调用标志
    let isAuthenticating = false;

    // 确保用户已登录
    const ensureAuthenticated = async () => {
      if (isAuthenticating) {
        console.log("⏳ 认证正在进行中，跳过重复调用");
        return false;
      }

      isAuthenticating = true;

      try {
        const token = localStorage.getItem("token");
        console.log(
          "🔍 检查认证状态，当前token:",
          token ? `${token.substring(0, 20)}...` : "无",
        );

        if (token && token.trim() !== "") {
          console.log("✅ 用户已登录，token长度:", token.length);

          // 验证token是否有效
          try {
            console.log("🔍 验证token有效性...");
            const testResponse = await realApi.getUserInfo();
            console.log("✅ Token验证成功:", testResponse);
            return true;
          } catch (error) {
            console.warn("⚠️ Token验证失败，尝试重新登录:", error.message);
            // Token无效，响应拦截器已经处理了清除逻辑，这里不需要重复清除
            // 直接进入自动登录流程
          }
        }

        console.log("🔐 用户未登录，尝试自动登录...");
        try {
          // 使用测试用户自动登录
          console.log("📡 发送登录请求...");
          const loginResponse = await realApi.login("admin", "admin123");
          console.log("📥 登录响应:", loginResponse);

          if (loginResponse && loginResponse.data && loginResponse.data.token) {
            // 保存token
            const newToken = loginResponse.data.token;
            console.log("💾 保存新token:", `${newToken.substring(0, 20)}...`);
            localStorage.setItem("token", newToken);
            localStorage.setItem("tokenType", "Bearer");

            // 更新userStore的token
            userStore.setToken(newToken);

            // 保存用户信息
            if (loginResponse.data.user) {
              localStorage.setItem(
                "userInfo",
                JSON.stringify(loginResponse.data.user),
              );
              localStorage.setItem("userRole", loginResponse.data.user.role);
              console.log("👤 保存用户信息:", loginResponse.data.user);

              // 更新userStore的用户信息
              userStore.setUserInfo(loginResponse.data.user);
            }

            console.log(
              "✅ 自动登录成功，用户ID:",
              loginResponse.data.user?.id,
            );
            return true;
          } else {
            console.error("❌ 登录响应格式异常:", loginResponse);
            return false;
          }
        } catch (error) {
          console.error("❌ 自动登录失败:", error);
          console.error("❌ 错误详情:", {
            message: error.message,
            status: error.response?.status,
            data: error.response?.data,
          });
          return false;
        }
      } catch (error) {
        console.error("❌ 认证过程发生异常:", error);
        return false;
      } finally {
        isAuthenticating = false;
      }
    };

    // 获取服务名称
    const getServiceName = (serviceId) => {
      const serviceMap = {
        1: "基础洗车",
        2: "精洗服务",
        3: "豪华套餐",
        4: "内饰清洁",
        5: "打蜡抛光",
      };
      return serviceMap[serviceId] || "未知服务";
    };

    // 获取服务描述
    const getServiceDescription = (serviceId) => {
      const descriptionMap = {
        1: "外观清洗，快速便捷",
        2: "深度清洁，焕然一新",
        3: "全方位护理，尊享体验",
        4: "内饰深度清洁",
        5: "车漆保护，持久光亮",
      };
      return descriptionMap[serviceId] || "专业洗车服务";
    };

    // 使用订单同步Hook
    const orderSyncResult = useOrderSync(async (userId) => {
      // 确保用户已登录
      const isAuthenticated = await ensureAuthenticated();
      if (!isAuthenticated) {
        throw new Error("认证失败");
      }

      console.log("📥 开始获取用户订单，用户ID:", userId);
      const response = await realApi.getUserOrders(userId);
      console.log("📋 用户订单响应:", response);

      if (response && response.data) {
        // 转换后端数据格式为前端需要的格式
        const processedOrders = response.data.map((order) => ({
          id: order.id,
          orderNumber: order.orderNo,
          status: order.status,
          createTime: TimeUtils.formatServerTime(order.createdAt),
          appointmentTime: order.bookingDate + " " + order.bookingTime,
          service: {
            id: order.serviceId,
            name: getServiceName(order.serviceId),
            description: getServiceDescription(order.serviceId),
            price: order.totalPrice,
            duration: "30分钟", // 默认时长
            icon: serviceIconMap[order.serviceId] || "Service",
            color: serviceColorMap[order.serviceId] || "var(--primary-color)",
          },
          vehicle: {
            plateNumber: order.carNumber,
            brand: order.carModel?.split(" ")[0] || "未知品牌",
            model: order.carModel?.split(" ")[1] || "未知型号",
            color: "unknown",
            phone: order.contactPhone,
            requirements: order.notes || "",
          },
          reviewed: false,
          paymentStatus: order.paymentStatus,
        }));

        return processedOrders;
      }

      return [];
    });

    // 从同步结果中解构需要的数据
    const {
      orders,
      loading: syncLoading,
      error: syncError,
      refreshOrders: syncRefreshOrders,
      removeListener,
    } = orderSyncResult;

    // 添加认证状态管理
    const authError = ref(false);
    const authLoading = ref(false);

    // 综合加载状态管理
    const loading = computed(() => {
      return syncLoading.value || authLoading.value;
    });

    // 综合错误状态管理
    const hasError = computed(() => {
      return authError.value || !!syncError.value;
    });

    // 获取用户订单数据（使用同步机制）
    const fetchUserOrders = async () => {
      try {
        console.log("🚀 开始获取用户订单数据...");
        authLoading.value = true;
        authError.value = false;

        // 确保用户已认证
        const isAuthenticated = await ensureAuthenticated();
        if (!isAuthenticated) {
          console.error("❌ 用户认证失败，无法获取订单数据");
          authError.value = true;
          ElMessage.error("用户认证失败，请刷新页面重试");
          return;
        }
        console.log("✅ 用户认证成功");

        const userId = getCurrentUserId();
        console.log("🆔 获取到的用户ID:", userId);
        if (!userId) {
          console.error("❌ 无法获取用户ID，无法获取订单数据");
          authError.value = true;
          ElMessage.error("无法获取用户信息，请重新登录");
          return;
        }

        console.log("📥 准备调用syncRefreshOrders，用户ID:", userId);
        await syncRefreshOrders({ userId, showMessage: false });
        console.log("✅ syncRefreshOrders调用完成");
        authError.value = false;
      } catch (error) {
        console.error("❌ 获取订单列表失败:", error);
        authError.value = true;
        ElMessage.error("获取订单列表失败");
      } finally {
        authLoading.value = false;
      }
    };

    // WebSocket订单状态更新监听
    let unsubscribeWebSocket = null;

    onMounted(() => {
      // 监听WebSocket订单状态更新
      unsubscribeWebSocket = onOrderStatusUpdate((data) => {
        console.log("📡 收到订单状态更新:", data);
        const { orderId, newStatus, oldStatus } = data;

        // 查找并更新对应的订单
        const orderIndex = orders.value.findIndex(
          (order) => order.id === orderId,
        );
        if (orderIndex !== -1) {
          console.log(
            `🔄 更新订单 ${orderId} 状态: ${oldStatus} -> ${newStatus}`,
          );
          orders.value[orderIndex].status = newStatus;

          // 显示状态更新通知
          const statusText = getStatusText(newStatus);
          ElMessage({
            type: "success",
            message: `订单状态已更新为：${statusText}`,
            duration: 3000,
          });
        } else {
          console.log(`⚠️ 未找到订单 ${orderId}，可能需要刷新订单列表`);
          // 如果找不到订单，刷新整个订单列表
          fetchUserOrders();
        }
      });

      // 监听全局订单状态更新事件（作为备用）
      const handleGlobalOrderUpdate = (event) => {
        const data = event.detail;
        console.log("🌐 收到全局订单状态更新事件:", data);
        // 这里可以添加额外的处理逻辑
      };

      window.addEventListener("orderStatusUpdate", handleGlobalOrderUpdate);

      // 清理函数
      onUnmounted(() => {
        if (unsubscribeWebSocket) {
          unsubscribeWebSocket();
        }
        window.removeEventListener(
          "orderStatusUpdate",
          handleGlobalOrderUpdate,
        );
      });
    });

    // 订单统计
    const orderCounts = computed(() => {
      const counts = {
        all: orders.value.length,
        pending: 0,
        confirmed: 0,
        inService: 0,
        completed: 0,
        cancelled: 0,
      };

      orders.value.forEach((order) => {
        switch (order.status) {
          case "pending":
            counts.pending++;
            break;
          case "confirmed":
            counts.confirmed++;
            break;
          case "in_progress":
          case "in-service":
            counts.inService++;
            break;
          case "completed":
            counts.completed++;
            break;
          case "cancelled":
            counts.cancelled++;
            break;
        }
      });

      return counts;
    });

    // 筛选后的订单
    const filteredOrders = computed(() => {
      let filtered = orders.value;

      if (activeTab.value !== "all") {
        const statusMap = {
          pending: "pending",
          confirmed: "confirmed",
          "in-service": "in_progress",
          completed: "completed",
          cancelled: "cancelled",
        };
        filtered = filtered.filter(
          (order) => order.status === statusMap[activeTab.value],
        );
      }

      // 分页
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filtered.slice(start, end);
    });

    // 总订单数
    const totalOrders = computed(() => {
      if (activeTab.value === "all") {
        return orders.value.length;
      }

      const statusMap = {
        pending: "pending",
        confirmed: "confirmed",
        "in-service": "in_progress",
        completed: "completed",
        cancelled: "cancelled",
      };
      return orders.value.filter(
        (order) => order.status === statusMap[activeTab.value],
      ).length;
    });

    // 获取状态类型
    const getStatusType = (status) => {
      const typeMap = {
        pending: "warning",
        confirmed: "primary",
        in_progress: "success",
        "in-service": "success",
        completed: "success",
        cancelled: "danger",
      };
      return typeMap[status] || "info";
    };

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        pending: "待确认",
        confirmed: "已确认",
        in_progress: "服务中",
        "in-service": "服务中",
        completed: "已完成",
        cancelled: "已取消",
      };
      return textMap[status] || "未知状态";
    };

    // 获取进度步骤
    const getProgressStep = (status) => {
      const stepMap = {
        pending: 0,
        confirmed: 1,
        in_progress: 2,
        "in-service": 2,
        completed: 3,
      };
      return stepMap[status] || 0;
    };

    // 获取空状态描述
    const getEmptyDescription = () => {
      const descMap = {
        all: "暂无订单记录",
        pending: "暂无待确认订单",
        confirmed: "暂无已确认订单",
        "in-service": "暂无服务中订单",
        completed: "暂无已完成订单",
        cancelled: "暂无已取消订单",
      };
      return descMap[activeTab.value] || "暂无数据";
    };

    // 格式化日期 - 使用统一时间工具
    const formatDate = (date) => {
      return TimeUtils.formatDate(date);
    };

    // 格式化日期时间 - 使用统一时间工具
    const formatDateTime = (date) => {
      return TimeUtils.formatServerTime(date, "YYYY-MM-DD HH:mm");
    };

    // 获取相对时间
    const getRelativeTime = (date) => {
      return TimeUtils.fromNow(date);
    };

    // 切换标签
    const handleTabChange = (tab) => {
      currentPage.value = 1;
    };

    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size;
      currentPage.value = 1;
    };

    const handleCurrentChange = (page) => {
      currentPage.value = page;
    };

    // 取消订单
    const cancelOrder = async (order) => {
      try {
        await ElMessageBox.confirm("确定要取消这个订单吗？", "取消订单", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        // 调用API取消订单
        await realApi.cancelOrder(order.id, "用户主动取消");

        // 更新本地状态
        order.status = "cancelled";
        ElMessage.success("订单已取消");

        // 重新获取订单列表
        await fetchUserOrders();
      } catch (error) {
        if (error !== "cancel") {
          console.error("取消订单失败:", error);
          ElMessage.error("取消订单失败");
        }
      }
    };

    // 修改订单
    const modifyOrder = (order) => {
      router.push({
        path: "/appointment",
        query: { orderId: order.id },
      });
    };

    // 评价订单
    const reviewOrder = (order) => {
      currentReviewOrder.value = order;
      reviewForm.value = {
        rating: 5,
        content: "",
        tags: [],
      };
      reviewDialogVisible.value = true;
    };

    // 提交评价
    const submitReview = () => {
      if (currentReviewOrder.value) {
        currentReviewOrder.value.reviewed = true;
        ElMessage.success("评价提交成功");
        reviewDialogVisible.value = false;
      }
    };

    // 查看评价
    const viewReview = (order) => {
      ElMessage.info("查看评价功能开发中...");
    };

    // 查看订单详情
    const viewOrderDetail = (order) => {
      router.push({
        path: "/order-detail",
        query: { id: order.id },
      });
    };

    // 再次预约
    const rebookOrder = (order) => {
      router.push({
        path: "/appointment",
        query: { serviceId: order.service.id },
      });
    };

    // 触发支付流程（安全校验 + 跳转支付页）
    const payOrder = async (order) => {
      try {
        const isAuthenticated = await ensureAuthenticated();
        if (!isAuthenticated) {
          ElMessage.error("用户未认证，无法发起支付");
          return;
        }

        // 已支付直接提示
        if (order.paymentStatus === "paid") {
          ElMessage.success("该订单已完成支付");
          return;
        }

        // 查询是否已有支付记录且已支付
        try {
          const resp = await paymentApi.getPaymentByOrderNo(order.orderNumber);
          const status = resp?.data?.status || resp?.status;
          if (status === "paid") {
            ElMessage.success("该订单已完成支付");
            return;
          }
        } catch (e) {
          console.warn("查询订单支付记录失败，继续进入支付流程:", e?.message);
        }

        // 跳转到支付页面
        router.push({
          name: "Payment",
          params: { orderNo: order.orderNumber },
        });
      } catch (error) {
        console.error("发起支付流程失败:", error);
        ElMessage.error("发起支付失败，请稍后重试");
      }
    };

    // 刷新单个订单状态
    const refreshOrderStatus = async (orderId) => {
      console.log("🔄 刷新订单状态:", orderId);
      try {
        // 使用增强WebSocket发送刷新请求
        enhancedWS.sendMessage({
          type: "refresh_order_status",
          data: { orderId },
        });

        // 同时调用API刷新
        await forceRefreshOrdersLocal();

        ElMessage.success("订单状态已刷新");
      } catch (error) {
        console.error("刷新订单状态失败:", error);
        ElMessage.error("刷新失败，请稍后重试");
      }
    };

    // 强制刷新数据的方法
    const forceRefreshOrdersLocal = async () => {
      console.log("🔄 强制刷新订单数据");
      const userId = getCurrentUserId();
      console.log("📥 强制刷新用户订单，用户ID:", userId);
      try {
        await syncRefreshOrders({
          userId,
          showMessage: true,
          showLoading: true,
        });
      } catch (error) {
        console.error("强制刷新失败:", error);
        ElMessage.error("刷新订单数据失败");
      }
    };

    // 初始化
    onMounted(async () => {
      console.log("📱 Orders页面挂载，初始化数据");

      try {
        // 首先确保用户已认证
        console.log("🔐 检查用户认证状态...");
        const isAuthenticated = await ensureAuthenticated();

        if (!isAuthenticated) {
          console.error("❌ 用户认证失败");
          authError.value = true;
          ElMessage.error("用户认证失败，请重新登录");
          return;
        }

        // 获取用户ID
        const userId = getCurrentUserId();
        console.log("📥 初始化用户订单，用户ID:", userId);

        if (!userId) {
          console.error("❌ 无法获取用户ID");
          authError.value = true;
          ElMessage.error("无法获取用户信息，请重新登录");
          return;
        }

        // 初始化增强WebSocket连接
        if (userStore.isLoggedIn()) {
          await enhancedWS.connect();
          console.log("✅ 增强WebSocket连接成功");
        }

        // 监听订单状态更新
        const unsubscribeOrderUpdate = enhancedWS.onOrderStatusUpdate(
          (data) => {
            console.log("📨 收到订单状态更新:", data);
            // 自动刷新订单列表
            forceRefreshOrdersLocal();
          },
        );

        // 加载订单数据
        console.log("📋 开始加载订单数据...");
        await syncRefreshOrders({
          userId,
          showMessage: false,
          showLoading: true,
        });

        console.log("✅ 订单数据初始化完成");

        // 注册取消订阅到组件卸载钩子，避免对函数使用 push 导致类型错误
        onUnmounted(() => {
          if (typeof unsubscribeOrderUpdate === "function") {
            unsubscribeOrderUpdate();
          }
        });
      } catch (error) {
        console.error("❌ 初始化失败:", error);
        authError.value = true;
        ElMessage.error(`初始化失败: ${error.message}`);
      }
    });

    // 页面激活时刷新数据（从其他页面跳转回来时）
    onActivated(() => {
      console.log("🔄 订单页面激活，刷新数据");
      forceRefreshOrdersLocal();
    });

    // 监听路由变化，确保每次进入订单页面都刷新数据
    watch(
      () => route.path,
      (newPath, oldPath) => {
        console.log("🛣️ 路由变化:", oldPath, "->", newPath);
        if (newPath === "/orders" && oldPath !== "/orders") {
          console.log("🔄 检测到跳转到订单页面，强制刷新数据");
          // 使用setTimeout确保组件完全渲染后再刷新
          setTimeout(() => {
            forceRefreshOrdersLocal();
          }, 100);
        }
      },
      { immediate: false },
    );

    // 监听查询参数变化（用于强制刷新）
    watch(
      () => route.query.refresh,
      (newRefresh) => {
        if (newRefresh && route.path === "/orders") {
          console.log("🔄 检测到刷新参数，强制刷新订单数据");
          forceRefreshOrdersLocal();
        }
      },
    );

    // 监听页面可见性变化
    const handleVisibilityChange = () => {
      if (!document.hidden && route.path === "/orders") {
        console.log("🔄 页面重新可见，刷新订单数据");
        forceRefreshOrdersLocal();
      }
    };

    // 添加页面可见性监听
    onMounted(() => {
      document.addEventListener("visibilitychange", handleVisibilityChange);
    });

    // 清理监听器
    const cleanup = () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
      removeListener(); // 移除订单同步监听器
    };

    // 组件卸载时清理
    onUnmounted(() => {
      cleanup();
    });

    return {
      loading,
      hasError,
      authError,
      activeTab,
      currentPage,
      pageSize,
      reviewDialogVisible,
      reviewForm,
      orders,
      orderCounts,
      filteredOrders,
      totalOrders,
      getStatusType,
      getStatusText,
      getProgressStep,
      getEmptyDescription,
      formatDate,
      formatDateTime,
      handleTabChange,
      handleSizeChange,
      handleCurrentChange,
      cancelOrder,
      modifyOrder,
      reviewOrder,
      submitReview,
      viewReview,
      viewOrderDetail,
      payOrder,
      rebookOrder,
      fetchUserOrders,
      forceRefreshOrders: forceRefreshOrdersLocal,
      forceRefreshOrdersLocal,
      refreshOrderStatus,
      cleanup,
      Refresh,
      Service,
      Calendar,
      Phone,
    };
  },
};
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 页面头部 */
.page-header {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 60px 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  text-align: left;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

/* 订单容器 */
.orders-container {
  padding: 40px 0;
}

/* 订单筛选 */
.orders-filters {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 订单列表 */
.orders-list {
  min-height: 400px;
}

.loading-container {
  padding: 40px 20px;
}

.error-state {
  padding: 40px 20px;
  text-align: center;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.orders-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 订单卡片 */
.order-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.order-card:hover {
  box-shadow: var(--shadow-lg);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-number {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 16px;
}

.order-date {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 订单内容 */
.order-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 20px;
}

.service-info {
  display: flex;
  gap: 16px;
}

.service-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  background: var(--bg-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-details {
  flex: 1;
}

.service-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.service-desc {
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-size: 14px;
}

.service-meta {
  display: flex;
  gap: 16px;
}

.service-meta .price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-color);
}

.service-meta .duration {
  color: var(--text-light);
  font-size: 14px;
}

.appointment-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.info-row .el-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

/* 订单进度部分 */
.order-progress-section {
  margin: 16px 0;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

/* 订单操作 */
.order-actions {
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
/* 付款按钮交互增强 */
.pay-button {
  transition:
    background-color 0.2s ease,
    transform 0.05s ease,
    box-shadow 0.2s ease;
}
.pay-button:hover {
  filter: brightness(1.04);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
.pay-button:active {
  transform: scale(0.98);
}
.pay-button:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}

/* 订单进度 */
.order-progress {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 40px 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .order-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .order-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .action-buttons {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .orders-container {
    padding: 20px 0;
  }

  .order-card {
    padding: 16px;
  }

  .service-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
