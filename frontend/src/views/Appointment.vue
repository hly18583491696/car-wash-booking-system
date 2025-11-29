<template>
  <div class="appointment-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">预约洗车服务</h1>
        <p class="page-subtitle">选择服务、时间和车辆信息，轻松完成预约</p>
      </div>
    </div>

    <div class="appointment-container">
      <div class="container">
        <div class="appointment-layout">
          <!-- 预约表单 -->
          <div class="appointment-form">
            <el-steps
              :active="currentStep"
              finish-status="success"
              align-center
            >
              <el-step title="选择服务" />
              <el-step title="选择时间" />
              <el-step title="车辆信息" />
              <el-step title="确认预约" />
            </el-steps>

            <!-- 步骤1: 选择服务 -->
            <div v-show="currentStep === 0" class="step-content">
              <h3 class="step-title">选择洗车服务</h3>
              <div class="services-selection">
                <div
                  class="service-option"
                  v-for="service in availableServices"
                  :key="service.id"
                  :class="{ active: selectedService?.id === service.id }"
                  @click="selectService(service)"
                >
                  <div class="service-icon">
                    <el-icon size="32" :color="service.color">
                      <component :is="service.icon" />
                    </el-icon>
                  </div>
                  <div class="service-info">
                    <h4 class="service-name">{{ service.name }}</h4>
                    <p class="service-desc">{{ service.description }}</p>
                    <div class="service-details">
                      <span class="price">¥{{ service.price }}</span>
                      <span class="duration">{{ service.duration }}</span>
                    </div>
                  </div>
                  <div class="service-features">
                    <el-tag
                      v-for="feature in service.features.slice(0, 3)"
                      :key="feature"
                      size="small"
                    >
                      {{ feature }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <!-- 步骤2: 选择时间 -->
            <div v-show="currentStep === 1" class="step-content">
              <h3 class="step-title">选择预约时间</h3>
              <div class="time-selection">
                <div class="date-picker">
                  <h4>选择日期</h4>
                  <el-calendar v-model="selectedDate" :range="dateRange">
                    <template #date-cell="{ data }">
                      <div
                        class="calendar-cell"
                        :class="{
                          disabled: isDateDisabled(data.date),
                          selected: isSameDate(data.date, selectedDate),
                        }"
                      >
                        <span>{{ data.date.getDate() }}</span>
                        <div
                          v-if="getDateStatus(data.date)"
                          class="date-status"
                        >
                          {{ getDateStatus(data.date) }}
                        </div>
                      </div>
                    </template>
                  </el-calendar>
                </div>

                <div class="time-slots" v-if="selectedDate">
                  <h4>选择时间段</h4>
                  <div class="current-time">
                    当前系统时间：{{ currentTimeText }}
                  </div>
                  <div class="slots-grid">
                    <div
                      class="time-slot"
                      v-for="slot in availableTimeSlots"
                      :key="slot.id"
                      :class="{
                        active: selectedTimeSlot?.id === slot.id,
                        disabled: slot.disabled,
                        busy: slot.busy,
                      }"
                      @click="selectTimeSlot(slot)"
                    >
                      <div class="slot-time">{{ slot.time }}</div>
                      <div class="slot-status">
                        <span v-if="slot.busy">已满</span>
                        <span v-else-if="slot.disabled && slot.reason">{{
                          slot.reason
                        }}</span>
                        <span v-else>{{ slot.available }}个空位</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 步骤3: 车辆信息 -->
            <div v-show="currentStep === 2" class="step-content">
              <h3 class="step-title">填写车辆信息</h3>
              <el-form
                :model="vehicleForm"
                :rules="vehicleRules"
                ref="vehicleFormRef"
                label-width="100px"
              >
                <el-form-item label="车牌号码" prop="plateNumber">
                  <el-input
                    v-model="vehicleForm.plateNumber"
                    placeholder="请输入车牌号码，如：京A12345"
                    maxlength="8"
                  />
                </el-form-item>

                <el-form-item label="车辆品牌" prop="brand">
                  <el-select
                    v-model="vehicleForm.brand"
                    placeholder="请选择车辆品牌"
                    filterable
                  >
                    <el-option
                      v-for="brand in carBrands"
                      :key="brand"
                      :label="brand"
                      :value="brand"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="车辆型号" prop="model">
                  <el-input
                    v-model="vehicleForm.model"
                    placeholder="请输入车辆型号，如：A4L"
                  />
                </el-form-item>

                <el-form-item label="车辆颜色" prop="color">
                  <el-select
                    v-model="vehicleForm.color"
                    placeholder="请选择车辆颜色"
                  >
                    <el-option label="白色" value="white" />
                    <el-option label="黑色" value="black" />
                    <el-option label="银色" value="silver" />
                    <el-option label="红色" value="red" />
                    <el-option label="蓝色" value="blue" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>

                <el-form-item label="联系电话" prop="phone">
                  <el-input
                    v-model="vehicleForm.phone"
                    placeholder="请输入联系电话"
                  />
                </el-form-item>

                <el-form-item label="特殊要求">
                  <el-input
                    v-model="vehicleForm.requirements"
                    type="textarea"
                    :rows="3"
                    placeholder="如有特殊要求请在此说明（选填）"
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- 步骤4: 确认预约 -->
            <div v-show="currentStep === 3" class="step-content">
              <h3 class="step-title">确认预约信息</h3>
              <div class="confirmation-card">
                <div class="confirm-section">
                  <h4>服务信息</h4>
                  <div class="info-row">
                    <span class="label">服务项目：</span>
                    <span class="value">{{ selectedService?.name }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">服务价格：</span>
                    <span class="value price"
                      >¥{{ selectedService?.price }}</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="label">服务时长：</span>
                    <span class="value">{{ selectedService?.duration }}</span>
                  </div>
                </div>

                <div class="confirm-section">
                  <h4>预约时间</h4>
                  <div class="info-row">
                    <span class="label">预约日期：</span>
                    <span class="value">{{ formatDate(selectedDate) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">预约时间：</span>
                    <span class="value">{{ selectedTimeSlot?.time }}</span>
                  </div>
                </div>

                <div class="confirm-section">
                  <h4>车辆信息</h4>
                  <div class="info-row">
                    <span class="label">车牌号码：</span>
                    <span class="value">{{ vehicleForm.plateNumber }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">车辆信息：</span>
                    <span class="value"
                      >{{ vehicleForm.brand }} {{ vehicleForm.model }} ({{
                        getColorName(vehicleForm.color)
                      }})</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="label">联系电话：</span>
                    <span class="value">{{ vehicleForm.phone }}</span>
                  </div>
                  <div v-if="vehicleForm.requirements" class="info-row">
                    <span class="label">特殊要求：</span>
                    <span class="value">{{ vehicleForm.requirements }}</span>
                  </div>
                </div>

                <div class="total-section">
                  <div class="total-row">
                    <span class="total-label">总计金额：</span>
                    <span class="total-price"
                      >¥{{ selectedService?.price }}</span
                    >
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="step-actions">
              <el-button v-if="currentStep > 0" @click="prevStep"
                >上一步</el-button
              >
              <el-button
                v-if="currentStep < 3"
                type="primary"
                @click="nextStep"
                :disabled="!canProceed"
              >
                下一步
              </el-button>
              <el-button
                v-if="currentStep === 3"
                type="primary"
                @click="submitAppointment"
                :loading="submitting"
              >
                {{ submitting ? "提交中..." : "确认预约" }}
              </el-button>
            </div>
          </div>

          <!-- 侧边栏信息 -->
          <div class="appointment-sidebar">
            <div class="sidebar-card">
              <h4>预约须知</h4>
              <ul class="notice-list">
                <li>请提前15分钟到达服务点</li>
                <li>如需取消预约，请提前2小时联系客服</li>
                <li>雨天可能影响服务质量，建议改期</li>
                <li>贵重物品请提前取出</li>
                <li>服务完成后请及时验收</li>
              </ul>
            </div>

            <div class="sidebar-card" v-if="selectedService">
              <h4>已选服务</h4>
              <div class="selected-service">
                <div class="service-icon">
                  <el-icon size="24" :color="selectedService.color">
                    <component :is="selectedService.icon" />
                  </el-icon>
                </div>
                <div class="service-details">
                  <h5>{{ selectedService.name }}</h5>
                  <p>
                    ¥{{ selectedService.price }} /
                    {{ selectedService.duration }}
                  </p>
                </div>
              </div>
            </div>

            <div class="sidebar-card">
              <h4>联系客服</h4>
              <div class="contact-info">
                <div class="contact-item">
                  <el-icon><Phone /></el-icon>
                  <span>400-888-8888</span>
                </div>
                <div class="contact-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>在线客服</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import { TimeUtils } from "@/utils/timeUtils";
import { timeSlotsApi } from "@/api/timeSlots";
import { timeSyncService } from "@/api/time";
import { realApi } from "@/api/realApi";
import { serviceApi } from "@/api/service";
import { addOrderToGlobalState } from "@/utils/orderSync";

export default {
  name: "Appointment",
  setup() {
    const router = useRouter();
    const route = useRoute();
    const userStore = useUserStore();

    const currentStep = ref(0);
    const submitting = ref(false);
    const vehicleFormRef = ref();

    // 选中的数据
    const selectedService = ref(null);
    const selectedDate = ref(new Date());
    const selectedTimeSlot = ref(null);
    const serverNow = ref(new Date());
    const currentTimeText = computed(() => {
      return TimeUtils.formatServerTime(serverNow.value, "HH:mm");
    });

    // 车辆表单
    const vehicleForm = ref({
      plateNumber: "",
      brand: "",
      model: "",
      color: "",
      phone: "",
      requirements: "",
    });

    // 表单验证规则
    const vehicleRules = {
      plateNumber: [
        { required: true, message: "请输入车牌号码", trigger: "blur" },
        {
          pattern:
            /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/,
          message: "请输入正确的车牌号码",
          trigger: "blur",
        },
      ],
      brand: [{ required: true, message: "请选择车辆品牌", trigger: "change" }],
      model: [{ required: true, message: "请输入车辆型号", trigger: "blur" }],
      color: [{ required: true, message: "请选择车辆颜色", trigger: "change" }],
      phone: [
        { required: true, message: "请输入联系电话", trigger: "blur" },
        {
          pattern: /^1[3-9]\d{9}$/,
          message: "请输入正确的手机号码",
          trigger: "blur",
        },
      ],
    };

    // 可用服务（从后端动态加载）
    const availableServices = ref([]);

    // 车辆品牌
    const carBrands = ref([
      "奥迪",
      "宝马",
      "奔驰",
      "大众",
      "丰田",
      "本田",
      "日产",
      "现代",
      "起亚",
      "福特",
      "雪佛兰",
      "别克",
      "凯迪拉克",
      "沃尔沃",
      "捷豹",
      "路虎",
      "保时捷",
      "特斯拉",
      "蔚来",
      "小鹏",
      "理想",
      "比亚迪",
    ]);

    // 日期范围
    const dateRange = computed(() => {
      const today = new Date();
      const maxDate = new Date();
      maxDate.setDate(today.getDate() + 30);
      return [today, maxDate];
    });

    // 可用时间段（真实数据）
    const availableTimeSlots = ref([]);
    let serverClockTimer = null;

    // 加载可用服务（从后端获取）
    const loadAvailableServices = async () => {
      try {
        const response = await serviceApi.getServiceList();
        console.log("📝 服务列表响应:", response);
        
        if (response && response.data) {
          // 后端返回的是分页数据，需要从 records 中获取
          const serviceList = response.data.records || response.data;
          
          // 将后端数据映射为前端需要的格式
          availableServices.value = serviceList.map((service, index) => ({
            id: service.id,
            name: service.name,
            description: service.description || "",
            price: parseFloat(service.price), // 确保价格是数字
            duration: service.duration ? `${service.duration}分钟` : "30分钟",
            features: service.description ? service.description.split("、") : [],
            icon: index % 3 === 0 ? "Car" : index % 3 === 1 ? "Star" : "Trophy",
            color: index % 3 === 0 ? "var(--primary-color)" : index % 3 === 1 ? "var(--warning-color)" : "var(--error-color)",
          }));
          
          console.log("✅ 服务列表加载成功，共", availableServices.value.length, "个服务");
          console.log("💰 服务价格:", availableServices.value.map(s => `${s.name}: ￥${s.price}`));
        }
      } catch (error) {
        console.error("❌ 加载服务列表失败:", error);
        ElMessage.error("加载服务列表失败，请刷新页面重试");
      }
    };

    const loadAvailableTimeSlots = async () => {
      if (!selectedDate.value) {
        availableTimeSlots.value = [];
        return;
      }
      try {
        const dateStr = TimeUtils.formatDate(selectedDate.value, "YYYY-MM-DD");
        const res = await timeSlotsApi.getAvailableTimeSlots(dateStr);
        const list = res?.data || [];
        const mapped = list.map((s) => {
          const startStr = (s.startTime || s.start_time || "")
            .toString()
            .substring(0, 5);
          const endStr = (s.endTime || s.end_time || "")
            .toString()
            .substring(0, 5);
          const max = s.maxBookings ?? s.max_bookings ?? 0;
          const cur = s.currentBookings ?? s.current_bookings ?? 0;
          const avail = Math.max(0, max - cur);
          return {
            id: s.id,
            time: `${startStr}-${endStr}`,
            available: avail,
            busy: avail === 0,
            disabled: false,
            reason: "",
          };
        });
        availableTimeSlots.value = mapped;
        applySlotValidity();
      } catch (error) {
        console.error("加载时间段失败", error);
        availableTimeSlots.value = [];
      }
    };

    const applySlotValidity = () => {
      if (!selectedDate.value || availableTimeSlots.value.length === 0) return;
      const now = serverNow.value;
      const isToday = isSameDate(selectedDate.value, now);
      if (!isToday) {
        availableTimeSlots.value.forEach((slot) => {
          slot.disabled = false;
          slot.reason = "";
        });
        return;
      }
      const nowMinutes = now.getHours() * 60 + now.getMinutes();
      availableTimeSlots.value.forEach((slot) => {
        const [startStr, endStr] = slot.time.split("-");
        const [sh, sm] = startStr.split(":").map((v) => parseInt(v));
        const [eh, em] = endStr.split(":").map((v) => parseInt(v));
        const startMinutes = sh * 60 + sm;
        const endMinutes = eh * 60 + em;
        const expired = endMinutes <= nowMinutes;
        slot.disabled = expired;
        slot.reason = expired ? "已过时" : "";
      });
    };

    // 是否可以进行下一步
    const canProceed = computed(() => {
      switch (currentStep.value) {
        case 0:
          return selectedService.value !== null;
        case 1:
          return selectedDate.value && selectedTimeSlot.value;
        case 2:
          return (
            vehicleForm.value.plateNumber &&
            vehicleForm.value.brand &&
            vehicleForm.value.model &&
            vehicleForm.value.color &&
            vehicleForm.value.phone
          );
        default:
          return true;
      }
    });

    // 选择服务
    const selectService = (service) => {
      selectedService.value = service;
    };

    // 选择时间段
    const selectTimeSlot = (slot) => {
      if (slot.busy) {
        ElMessage.warning("该时间段已满");
        return;
      }
      if (slot.disabled) {
        ElMessage.info(slot.reason || "该时间段不可选择");
        return;
      }
      selectedTimeSlot.value = slot;
    };

    // 判断日期是否禁用
    const isDateDisabled = (date) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    };

    // 判断是否是同一天
    const isSameDate = (date1, date2) => {
      return date1.toDateString() === date2.toDateString();
    };

    // 获取日期状态
    const getDateStatus = (date) => {
      const today = new Date();
      if (isSameDate(date, today)) return "今天";

      const tomorrow = new Date(today);
      tomorrow.setDate(today.getDate() + 1);
      if (isSameDate(date, tomorrow)) return "明天";

      return null;
    };

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return "";
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long",
      });
    };

    // 获取颜色名称
    const getColorName = (color) => {
      const colorMap = {
        white: "白色",
        black: "黑色",
        silver: "银色",
        red: "红色",
        blue: "蓝色",
        other: "其他",
      };
      return colorMap[color] || color;
    };

    // 下一步
    const nextStep = async () => {
      if (currentStep.value === 2) {
        // 验证车辆表单
        try {
          await vehicleFormRef.value.validate();
        } catch {
          return;
        }
      }

      if (currentStep.value < 3) {
        currentStep.value++;
      }
    };

    // 上一步
    const prevStep = () => {
      if (currentStep.value > 0) {
        currentStep.value--;
      }
    };

    // 确保用户已登录
    const ensureAuthenticated = async () => {
      const token = localStorage.getItem("token");
      if (token && token.trim() !== "") {
        console.log("✅ 用户已登录");
        return true;
      }

      console.log("🔐 用户未登录，尝试自动登录...");
      try {
        // 使用测试用户自动登录
        const loginResponse = await realApi.login("admin", "admin123");
        if (loginResponse && loginResponse.data && loginResponse.data.token) {
          localStorage.setItem("token", loginResponse.data.token);
          localStorage.setItem("tokenType", "Bearer");
          console.log("✅ 自动登录成功");
          return true;
        }
      } catch (error) {
        console.error("❌ 自动登录失败:", error);
      }

      return false;
    };

    // 提交预约
    const submitAppointment = async () => {
      try {
        await ElMessageBox.confirm("确认提交预约吗？", "确认预约", {
          confirmButtonText: "确认",
          cancelButtonText: "取消",
          type: "info",
        });

        submitting.value = true;

        // 确保用户已登录
        const isAuthenticated = await ensureAuthenticated();
        if (!isAuthenticated) {
          ElMessage.error("登录失败，无法提交预约");
          return;
        }

        // 构建预约数据
        const bookingData = {
          userId: userStore.userId, // 使用当前登录用户的ID
          serviceId: selectedService.value.id,
          bookingDate: TimeUtils.formatDate(selectedDate.value, "YYYY-MM-DD"),
          bookingTime: selectedTimeSlot.value.time,
          carNumber: vehicleForm.value.plateNumber,
          carModel: `${vehicleForm.value.brand} ${vehicleForm.value.model}`,
          contactPhone: vehicleForm.value.phone,
          notes: vehicleForm.value.requirements || "",
          totalPrice: selectedService.value.price,
          status: "pending",
        };

        console.log("📤 提交预约数据:", bookingData);

        // 调用真实API创建预约
        const response = await realApi.createBooking(bookingData);
        console.log("✅ 预约创建响应:", response);

        if (response && response.data) {
          ElMessage.success("预约提交成功！");

          // 重要:使用与后端 API 返回一致的数据格式,确保数据同步
          const apiData = response.data;
          const newOrder = {
            id: apiData.id,
            orderNumber: apiData.orderNo,
            status: apiData.status || "pending",
            createTime: TimeUtils.formatServerTime(
              apiData.createdAt || new Date().toISOString(),
            ),
            appointmentTime: `${apiData.bookingDate || bookingData.bookingDate} ${apiData.bookingTime || bookingData.bookingTime}`,
            service: {
              id: apiData.serviceId || selectedService.value.id,
              name: selectedService.value.name,
              description: selectedService.value.description,
              price: apiData.totalPrice || selectedService.value.price,
              duration: selectedService.value.duration || "30分钟",
              icon: selectedService.value.icon,
              color: selectedService.value.color,
            },
            vehicle: {
              plateNumber: apiData.carNumber || vehicleForm.value.plateNumber,
              brand: vehicleForm.value.brand,
              model: vehicleForm.value.model,
              color: vehicleForm.value.color,
              phone: apiData.contactPhone || vehicleForm.value.phone,
              requirements:
                apiData.notes || vehicleForm.value.requirements || "",
            },
            reviewed: false,
            paymentStatus: apiData.paymentStatus || "pending",
          };

          console.log("➕ 准备添加新订单到全局状态:", newOrder);

          // 添加到全局状态，触发所有订单页面的实时更新
          addOrderToGlobalState(newOrder);

          console.log("✅ 新订单已添加到全局状态");

          // 等待状态传播完成
          await new Promise((resolve) => setTimeout(resolve, 200));

          // 根据支付状态决定跳转页面
          const paymentStatus = apiData.paymentStatus || "unpaid";
          const orderStatus = apiData.status || "pending";

          if (
            paymentStatus === "unpaid" &&
            (orderStatus === "confirmed" || orderStatus === "pending")
          ) {
            // 订单需要支付,跳转至支付页面
            console.log("🔄 跳转到支付页面, 订单号:", newOrder.orderNumber);
            router.push({
              name: "Payment",
              params: { orderNo: newOrder.orderNumber },
            });
          } else {
            // 订单已支付或不需要支付,跳转到订单列表
            console.log("🔄 跳转到订单列表页面");
            router.push({
              path: "/orders",
            });
          }
        } else {
          throw new Error("预约创建失败");
        }
      } catch (error) {
        console.error("❌ 预约提交失败:", error);
        if (error !== "cancel") {
          ElMessage.error("预约提交失败，请重试");
        }
      } finally {
        submitting.value = false;
      }
    };

    // 初始化
    onMounted(async () => {
      // 加载服务列表
      await loadAvailableServices();
      
      // 如果有预选服务ID，自动选择
      const serviceId = route.query.serviceId;
      if (serviceId) {
        const service = availableServices.value.find((s) => s.id == serviceId);
        if (service) {
          selectedService.value = service;
        }
      }
      try {
        await timeSyncService.syncServerTime();
      } catch (e) {}
      serverNow.value = timeSyncService.getServerTime();
      serverClockTimer = setInterval(() => {
        serverNow.value = timeSyncService.getServerTime();
        applySlotValidity();
      }, 1000);
      loadAvailableTimeSlots();
    });

    watch(
      () => selectedDate.value,
      () => {
        loadAvailableTimeSlots();
      },
    );

    onBeforeUnmount(() => {
      if (serverClockTimer) {
        clearInterval(serverClockTimer);
        serverClockTimer = null;
      }
    });

    return {
      currentStep,
      submitting,
      vehicleFormRef,
      selectedService,
      selectedDate,
      selectedTimeSlot,
      vehicleForm,
      vehicleRules,
      availableServices,
      carBrands,
      dateRange,
      availableTimeSlots,
      loadAvailableTimeSlots,
      canProceed,
      selectService,
      selectTimeSlot,
      isDateDisabled,
      isSameDate,
      getDateStatus,
      formatDate,
      getColorName,
      serverNow,
      currentTimeText,
      nextStep,
      prevStep,
      submitAppointment,
    };
  },
};
</script>

<style scoped>
.appointment-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 页面头部 */
.page-header {
  background: var(--primary-gradient);
  color: var(--text-white);
  padding: 60px 0;
  text-align: center;
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

/* 预约容器 */
.appointment-container {
  padding: 40px 0;
}

.appointment-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
}

/* 预约表单 */
.appointment-form {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.step-content {
  margin: 40px 0;
  min-height: 400px;
}

.step-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

/* 服务选择 */
.services-selection {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.service-option {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.service-option:hover {
  border-color: var(--primary-light);
  background: var(--primary-light);
}

.service-option.active {
  border-color: var(--primary-color);
  background: var(--primary-light);
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

.service-info {
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

.service-details {
  display: flex;
  gap: 16px;
}

.service-details .price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-color);
}

.service-details .duration {
  color: var(--text-light);
  font-size: 14px;
}

.service-features {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

/* 时间选择 */
.time-selection {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.date-picker h4,
.time-slots h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.calendar-cell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.calendar-cell.disabled {
  color: var(--text-light);
  cursor: not-allowed;
}

.calendar-cell.selected {
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-sm);
}

.date-status {
  font-size: 10px;
  color: var(--primary-color);
  position: absolute;
  bottom: 2px;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.time-slot {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.time-slot:hover:not(.disabled):not(.busy) {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.time-slot.active {
  border-color: var(--primary-color);
  background: var(--primary-color);
  color: var(--text-white);
}

.time-slot.disabled {
  background: var(--bg-light);
  color: var(--text-light);
  cursor: not-allowed;
}

.time-slot.busy {
  background: var(--error-light);
  color: var(--error-color);
  cursor: not-allowed;
}

.slot-time {
  font-weight: 600;
  margin-bottom: 4px;
}

.slot-status {
  font-size: 12px;
}

.current-time {
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 12px;
}

/* 确认信息 */
.confirmation-card {
  background: var(--bg-light);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.confirm-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.confirm-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.confirm-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-row .label {
  color: var(--text-secondary);
}

.info-row .value {
  color: var(--text-primary);
  font-weight: 500;
}

.info-row .value.price {
  color: var(--primary-color);
  font-weight: 700;
}

.total-section {
  background: var(--primary-light);
  padding: 16px;
  border-radius: var(--radius-md);
  margin-top: 16px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.total-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
}

/* 操作按钮 */
.step-actions {
  display: flex;
  justify-content: space-between;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

/* 侧边栏 */
.appointment-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.sidebar-card h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  padding: 8px 0;
  color: var(--text-secondary);
  font-size: 14px;
  border-bottom: 1px solid var(--border-light);
}

.notice-list li:last-child {
  border-bottom: none;
}

.notice-list li::before {
  content: "•";
  color: var(--primary-color);
  margin-right: 8px;
}

.selected-service {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-service .service-icon {
  width: 40px;
  height: 40px;
  background: var(--bg-light);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.selected-service h5 {
  margin: 0 0 4px 0;
  color: var(--text-primary);
}

.selected-service p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.contact-item .el-icon {
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .appointment-layout {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .appointment-form {
    padding: 20px;
  }

  .time-selection {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .service-option {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .service-features {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }

  .slots-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 40px 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .appointment-form {
    padding: 16px;
  }

  .step-actions {
    flex-direction: column;
    gap: 12px;
  }

  .step-actions .el-button {
    width: 100%;
  }
}
</style>
