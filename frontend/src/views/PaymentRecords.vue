<template>
  <div class="payment-records-container">
    <div class="page-header">
      <h1>支付记录</h1>
      <p>查看您的所有支付和退款记录</p>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <div class="filter-group">
        <label>支付状态：</label>
        <select v-model="filters.status" @change="loadPaymentRecords">
          <option value="">全部</option>
          <option value="pending">待支付</option>
          <option value="paid">已支付</option>
          <option value="failed">支付失败</option>
          <option value="cancelled">已取消</option>
          <option value="refunded">已退款</option>
        </select>
      </div>

      <div class="filter-group">
        <label>支付方式：</label>
        <select v-model="filters.paymentMethod" @change="loadPaymentRecords">
          <option value="">全部</option>
          <option value="wechat">微信支付</option>
          <option value="alipay">支付宝</option>
        </select>
      </div>

      <div class="filter-group">
        <label>时间范围：</label>
        <input
          type="date"
          v-model="filters.startDate"
          @change="loadPaymentRecords"
        />
        <span>至</span>
        <input
          type="date"
          v-model="filters.endDate"
          @change="loadPaymentRecords"
        />
      </div>
    </div>

    <!-- 支付记录列表 -->
    <div class="records-list" v-loading="loading">
      <div v-if="paymentRecords.length === 0" class="empty-state">
        <div class="empty-icon">📄</div>
        <h3>暂无支付记录</h3>
        <p>您还没有任何支付记录</p>
      </div>

      <div v-else>
        <div
          v-for="record in paymentRecords"
          :key="record.paymentNo"
          class="record-card"
        >
          <div class="record-header">
            <div class="record-info">
              <h3>{{ record.orderNo }}</h3>
              <p class="payment-no">支付单号：{{ record.paymentNo }}</p>
            </div>
            <div class="record-status">
              <span :class="['status-badge', getStatusClass(record.status)]">
                {{ getStatusText(record.status) }}
              </span>
            </div>
          </div>

          <div class="record-details">
            <div class="detail-row">
              <span class="label">支付金额：</span>
              <span class="amount">¥{{ record.amount }}</span>
            </div>
            <div class="detail-row">
              <span class="label">支付方式：</span>
              <span>{{ getPaymentMethodText(record.paymentMethod) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">创建时间：</span>
              <span>{{ formatDateTime(record.createdAt) }}</span>
            </div>
            <div v-if="record.paidAt" class="detail-row">
              <span class="label">支付时间：</span>
              <span>{{ formatDateTime(record.paidAt) }}</span>
            </div>
            <div v-if="record.failureReason" class="detail-row">
              <span class="label">失败原因：</span>
              <span class="error-text">{{ record.failureReason }}</span>
            </div>
          </div>

          <div class="record-actions">
            <button
              v-if="record.status === 'pending'"
              class="btn-pay"
              @click="continuePay(record)"
            >
              继续支付
            </button>
            <button
              v-if="record.status === 'paid' && canRefund(record)"
              class="btn-refund"
              @click="requestRefund(record)"
            >
              申请退款
            </button>
            <!-- 取消支付功能暂时移除，等待后端API实现 -->
            <!--
            <button 
              v-if="record.status === 'pending'"
              class="btn-cancel"
              @click="cancelPayment(record)"
            >
              取消支付
            </button>
            -->
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination">
      <button
        :disabled="pagination.current === 1"
        @click="changePage(pagination.current - 1)"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ pagination.current }} 页，共 {{ pagination.pages }} 页
      </span>
      <button
        :disabled="pagination.current === pagination.pages"
        @click="changePage(pagination.current + 1)"
      >
        下一页
      </button>
    </div>

    <!-- 退款申请弹窗 -->
    <div v-if="showRefundModal" class="modal-overlay" @click="closeRefundModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>申请退款</h3>
          <button class="close-btn" @click="closeRefundModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>退款金额：</label>
            <input
              type="number"
              v-model="refundForm.amount"
              :max="selectedRecord.amount"
              step="0.01"
              placeholder="请输入退款金额"
            />
            <small>最大可退款金额：¥{{ selectedRecord.amount }}</small>
          </div>
          <div class="form-group">
            <label>退款原因：</label>
            <textarea
              v-model="refundForm.reason"
              placeholder="请说明退款原因"
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeRefundModal">取消</button>
          <button
            class="btn-primary"
            @click="submitRefund"
            :disabled="!refundForm.amount || !refundForm.reason"
          >
            提交申请
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import paymentApi from "@/api/payment";

export default {
  name: "PaymentRecords",
  setup() {
    const router = useRouter();

    const loading = ref(false);
    const paymentRecords = ref([]);
    const showRefundModal = ref(false);
    const selectedRecord = ref({});

    const filters = reactive({
      status: "",
      paymentMethod: "",
      startDate: "",
      endDate: "",
    });

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0,
      pages: 0,
    });

    const refundForm = reactive({
      amount: "",
      reason: "",
    });

    // 加载支付记录
    const loadPaymentRecords = async () => {
      loading.value = true;
      try {
        const params = {
          page: pagination.current,
          size: pagination.size,
          ...filters,
        };

        const response = await paymentApi.getUserPaymentRecords(params);
        console.log("[支付记录] 响应摘要:", {
          code: response.code,
          dataType: Array.isArray(response.data)
            ? "array"
            : typeof response.data,
          sample: Array.isArray(response.data)
            ? response.data[0]
            : response.data,
        });
        if (response.code === 200) {
          const data = response.data;
          const isArray = Array.isArray(data);
          paymentRecords.value = isArray ? data : data.records || [];
          pagination.total = isArray
            ? data.length
            : data.total || paymentRecords.value.length || 0;
          pagination.pages = Math.ceil(
            (pagination.total || 0) / (pagination.size || 1),
          );
        }
      } catch (error) {
        console.error("加载支付记录失败:", error);
        ElMessage.error("加载支付记录失败");
      } finally {
        loading.value = false;
      }
    };

    // 获取状态样式类
    const getStatusClass = (status) => {
      const statusMap = {
        pending: "warning",
        paid: "success",
        failed: "danger",
        cancelled: "info",
        refunded: "info",
      };
      return statusMap[status] || "info";
    };

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        pending: "待支付",
        paid: "已支付",
        failed: "支付失败",
        cancelled: "已取消",
        refunded: "已退款",
      };
      return statusMap[status] || status;
    };

    // 获取支付方式文本
    const getPaymentMethodText = (method) => {
      const methodMap = {
        wechat: "微信支付",
        alipay: "支付宝",
        credit_card: "信用卡支付",
        virtual: "虚拟支付(测试)",
      };
      return methodMap[method] || method;
    };

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return "";
      const date = new Date(dateTime);
      return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // 判断是否可以退款
    const canRefund = (record) => {
      if (!record.paidAt) return false;
      const paidTime = new Date(record.paidAt);
      const now = new Date();
      const diffDays = (now - paidTime) / (1000 * 60 * 60 * 24);
      return diffDays <= 7; // 7天内可以退款
    };

    // 继续支付
    const continuePay = (record) => {
      router.push({
        name: "Payment",
        params: { orderNo: record.orderNo },
      });
    };

    // 申请退款
    const requestRefund = (record) => {
      selectedRecord.value = record;
      refundForm.amount = record.amount;
      refundForm.reason = "";
      showRefundModal.value = true;
    };

    // 取消支付 - 暂时注释，等待后端API实现
    /*
    const cancelPayment = async (record) => {
      try {
        await ElMessageBox.confirm(
          '确定要取消这笔支付吗？',
          '确认取消',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await paymentApi.cancelExpiredPayment(record.paymentNo)
        if (response.code === 200) {
          ElMessage.success('支付已取消')
          loadPaymentRecords()
        } else {
          ElMessage.error(response.message || '取消支付失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消支付失败:', error)
          ElMessage.error('取消支付失败')
        }
      }
    }
    */

    // 提交退款申请
    const submitRefund = async () => {
      try {
        const refundData = {
          paymentNo: selectedRecord.value.paymentNo,
          amount: parseFloat(refundForm.amount),
          reason: refundForm.reason,
        };

        const response = await paymentApi.requestRefund(refundData);
        if (response.code === 200) {
          ElMessage.success("退款申请已提交，请等待审核");
          closeRefundModal();
          loadPaymentRecords();
        } else {
          ElMessage.error(response.message || "提交退款申请失败");
        }
      } catch (error) {
        console.error("提交退款申请失败:", error);
        ElMessage.error("提交退款申请失败");
      }
    };

    // 关闭退款弹窗
    const closeRefundModal = () => {
      showRefundModal.value = false;
      selectedRecord.value = {};
      refundForm.amount = "";
      refundForm.reason = "";
    };

    // 切换页面
    const changePage = (page) => {
      pagination.current = page;
      loadPaymentRecords();
    };

    onMounted(() => {
      loadPaymentRecords();
    });

    return {
      loading,
      paymentRecords,
      showRefundModal,
      selectedRecord,
      filters,
      pagination,
      refundForm,
      loadPaymentRecords,
      getStatusClass,
      getStatusText,
      getPaymentMethodText,
      formatDateTime,
      canRefund,
      continuePay,
      requestRefund,
      // cancelPayment, // 暂时移除，等待后端API实现
      submitRefund,
      closeRefundModal,
      changePage,
    };
  },
};
</script>

<style scoped>
.payment-records-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-header p {
  color: #7f8c8d;
}

.filters {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.records-list {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.record-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.record-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
}

.record-info h3 {
  color: #2c3e50;
  margin-bottom: 4px;
  font-size: 16px;
}

.payment-no {
  color: #7f8c8d;
  font-size: 14px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.success {
  background: #d4edda;
  color: #155724;
}

.status-badge.warning {
  background: #fff3cd;
  color: #856404;
}

.status-badge.danger {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.info {
  background: #d1ecf1;
  color: #0c5460;
}

.record-details {
  padding: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #7f8c8d;
  font-size: 14px;
}

.amount {
  color: #e74c3c;
  font-weight: 600;
  font-size: 16px;
}

.error-text {
  color: #e74c3c;
}

.record-actions {
  padding: 20px;
  border-top: 1px solid #ecf0f1;
  display: flex;
  gap: 12px;
}

.btn-pay {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-pay:hover {
  background: linear-gradient(135deg, #2980b9, #1f5f8b);
}

.btn-refund {
  background: linear-gradient(135deg, #f39c12, #e67e22);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-refund:hover {
  background: linear-gradient(135deg, #e67e22, #d35400);
}

.btn-cancel {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination button:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #7f8c8d;
  font-size: 14px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
}

.modal-header h3 {
  color: #2c3e50;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #7f8c8d;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group small {
  color: #7f8c8d;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #ecf0f1;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-primary {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #2980b9, #1f5f8b);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }

  .record-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .record-actions {
    flex-direction: column;
  }

  .pagination {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
