<template>
  <div class="payment-container" role="main" :aria-busy="loading">
    <div class="payment-card">
      <!-- 页面级加载遮罩 -->
      <div
        v-if="loading && !showPaymentModal"
        class="loading-overlay"
        aria-live="polite"
      >
        <div class="loading-spinner"></div>
        <span class="sr-only">正在处理，请稍候</span>
      </div>
      <div class="payment-header">
        <h2 id="pageTitle">订单支付</h2>
        <p class="order-info">订单号：{{ orderInfo.orderNo }}</p>
      </div>

      <div class="order-details" aria-labelledby="orderDetailsTitle">
        <h3 id="orderDetailsTitle">订单详情</h3>
        <div v-if="isOrderLoaded">
          <div class="detail-item">
            <span>服务项目：</span>
            <span>{{ orderInfo.serviceName }}</span>
          </div>
          <div class="detail-item">
            <span>预约时间：</span>
            <span>{{ formatDateTime(orderInfo.appointmentTime) }}</span>
          </div>
          <div class="detail-item">
            <span>服务地点：</span>
            <span>{{ orderInfo.location }}</span>
          </div>
          <div class="detail-item total-amount">
            <span>支付金额：</span>
            <span class="amount">¥{{ orderInfo.totalAmount }}</span>
          </div>
        </div>
        <div v-else class="skeleton">
          <div class="skeleton-line"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line wide"></div>
        </div>
      </div>

      <div class="payment-methods">
        <h3 id="payMethodLabel">选择支付方式</h3>
        <div
          class="payment-options"
          role="radiogroup"
          :aria-labelledby="'payMethodLabel'"
        >
          <div 
            class="payment-option" 
            :class="{ active: selectedMethod === 'wechat' }"
            @click="selectPaymentMethod('wechat')"
            role="radio"
            :aria-checked="selectedMethod === 'wechat'"
            tabindex="0"
            @keydown="handleOptionKeydown('wechat', $event)"
          >
            <div class="payment-icon wechat-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path fill="#07C160" d="M8.5 6.5c-1.5 0-2.7 1.2-2.7 2.7s1.2 2.7 2.7 2.7 2.7-1.2 2.7-2.7-1.2-2.7-2.7-2.7zm7 0c-1.5 0-2.7 1.2-2.7 2.7s1.2 2.7 2.7 2.7 2.7-1.2 2.7-2.7-1.2-2.7-2.7-2.7z"/>
              </svg>
            </div>
            <span>微信支付</span>
            <div class="check-icon" v-if="selectedMethod === 'wechat'">✓</div>
          </div>
          
          <div 
            class="payment-option" 
            :class="{ active: selectedMethod === 'alipay' }"
            @click="selectPaymentMethod('alipay')"
            role="radio"
            :aria-checked="selectedMethod === 'alipay'"
            tabindex="0"
            @keydown="handleOptionKeydown('alipay', $event)"
          >
            <div class="payment-icon alipay-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path fill="#1677FF" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
              </svg>
            </div>
            <span>支付宝</span>
            <div class="check-icon" v-if="selectedMethod === 'alipay'">✓</div>
          </div>

          <div 
            class="payment-option" 
            :class="{ active: selectedMethod === 'credit_card' }"
            @click="selectPaymentMethod('credit_card')"
            role="radio"
            :aria-checked="selectedMethod === 'credit_card'"
            tabindex="0"
            @keydown="handleOptionKeydown('credit_card', $event)"
          >
            <div class="payment-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <rect x="3" y="5" width="18" height="14" rx="2" fill="#34495e"/>
                <rect x="3" y="9" width="18" height="3" fill="#2c3e50"/>
                <rect x="6" y="14" width="6" height="2" fill="#ecf0f1"/>
              </svg>
            </div>
            <span>信用卡</span>
            <div class="check-icon" v-if="selectedMethod === 'credit_card'">✓</div>
          </div>
        </div>
      </div>

      <!-- 信用卡支付表单 -->
      <div v-if="selectedMethod === 'credit_card'" class="card-form" aria-labelledby="cardFormTitle">
        <h3>填写信用卡信息</h3>
        <div class="form-row">
          <label>持卡人姓名</label>
          <input
            type="text"
            v-model="creditCard.cardHolder"
            placeholder="姓名"
            :aria-invalid="!!formErrors.cardHolder"
            aria-describedby="cardholder-error"
            :class="{ 'input-invalid': formErrors.cardHolder, 'input-valid': !formErrors.cardHolder && creditCard.cardHolder }"
          />
          <span id="cardholder-error" class="error-msg" role="alert" v-if="formErrors.cardHolder">{{ formErrors.cardHolder }}</span>
        </div>
        <div class="form-row">
          <label>卡号</label>
          <input
            type="text"
            v-model="creditCard.cardNumber"
            placeholder="1234 5678 9012 3456"
            inputmode="numeric"
            :aria-invalid="!!formErrors.cardNumber"
            aria-describedby="cardnumber-error"
            :class="{ 'input-invalid': formErrors.cardNumber, 'input-valid': !formErrors.cardNumber && creditCard.cardNumber }"
          />
          <span id="cardnumber-error" class="error-msg" role="alert" v-if="formErrors.cardNumber">{{ formErrors.cardNumber }}</span>
        </div>
        <div class="form-row two-cols">
          <div>
            <label>有效期 (MM/YY)</label>
            <input
              type="text"
              v-model="creditCard.expiry"
              placeholder="MM/YY"
              :aria-invalid="!!formErrors.expiry"
              aria-describedby="expiry-error"
              :class="{ 'input-invalid': formErrors.expiry, 'input-valid': !formErrors.expiry && creditCard.expiry }"
            />
            <span id="expiry-error" class="error-msg" role="alert" v-if="formErrors.expiry">{{ formErrors.expiry }}</span>
          </div>
          <div>
            <label>CVV</label>
            <input
              type="password"
              v-model="creditCard.cvv"
              placeholder="123"
              :aria-invalid="!!formErrors.cvv"
              aria-describedby="cvv-error"
              :class="{ 'input-invalid': formErrors.cvv, 'input-valid': !formErrors.cvv && creditCard.cvv }"
            />
            <span id="cvv-error" class="error-msg" role="alert" v-if="formErrors.cvv">{{ formErrors.cvv }}</span>
          </div>
        </div>
        <div class="form-row">
          <label>支付金额</label>
          <input
            type="number"
            v-model.number="payAmount"
            min="0"
            step="0.01"
            :aria-invalid="false"
          />
          <small class="hint">默认使用订单金额，可调整支付金额</small>
        </div>
        <div class="form-row">
          <label>短信验证码</label>
          <input
            type="text"
            v-model="creditCard.otp"
            maxlength="6"
            placeholder="6位验证码"
            inputmode="numeric"
            :aria-invalid="!!formErrors.otp"
            aria-describedby="otp-error"
            :class="{ 'input-invalid': formErrors.otp, 'input-valid': !formErrors.otp && creditCard.otp }"
          />
          <span id="otp-error" class="error-msg" role="alert" v-if="formErrors.otp">{{ formErrors.otp }}</span>
        </div>
      </div>

      <div class="payment-actions">
        <button class="btn-cancel" @click="cancelPayment">取消支付</button>
        <button 
          class="btn-pay" 
          @click="processPayment"
          :disabled="!selectedMethod || loading || (selectedMethod === 'credit_card' && !isCardFormValid)"
          :aria-disabled="!selectedMethod || loading || (selectedMethod === 'credit_card' && !isCardFormValid)"
        >
          <span v-if="loading">处理中...</span>
          <span v-else>立即支付 ¥{{ orderInfo.totalAmount }}</span>
        </button>
      </div>
    </div>

    <!-- 支付状态弹窗 -->
    <div v-if="showPaymentModal" class="payment-modal-overlay" @click="closePaymentModal">
      <div class="payment-modal" role="dialog" aria-modal="true" aria-labelledby="paymentDialogTitle" @click.stop>
        <div class="modal-content">
          <div v-if="paymentStatus === 'processing'" class="payment-processing">
            <div class="loading-spinner"></div>
            <h3 id="paymentDialogTitle">正在处理支付...</h3>
            <p>
              请在{{ selectedMethod === 'credit_card' ? '信用卡' : (selectedMethod === 'wechat' ? '微信' : '支付宝') }}中完成支付
            </p>
            <div v-if="selectedMethod !== 'credit_card'" class="qr-code-placeholder">
              <p>二维码区域</p>
              <small>请使用{{ selectedMethod === 'wechat' ? '微信' : '支付宝' }}扫码支付</small>
            </div>
          </div>
          
          <div v-else-if="paymentStatus === 'success'" class="payment-success">
            <div class="success-icon">✓</div>
            <h3 id="paymentDialogTitle">支付成功</h3>
            <p>您的订单已支付完成</p>
            <button class="btn-primary" @click="goToOrders">查看订单</button>
          </div>
          
          <div v-else-if="paymentStatus === 'failed'" class="payment-failed">
            <div class="error-icon">✗</div>
            <h3 id="paymentDialogTitle">支付失败</h3>
            <p>{{ paymentError || '支付过程中出现错误，请重试' }}</p>
            <button class="btn-primary" @click="retryPayment">重新支付</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import paymentApi from '@/api/payment'
import orderApi from '@/api/order'

export default {
  name: 'Payment',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(false)
    const selectedMethod = ref('')
    const showPaymentModal = ref(false)
    const paymentStatus = ref('') // processing, success, failed
    const paymentError = ref('')
    const paymentNo = ref('')
    const payAmount = ref(0)
    
    const orderInfo = reactive({
      orderNo: '',
      serviceName: '',
      appointmentTime: '',
      location: '',
      totalAmount: 0
    })

    const creditCard = reactive({
      cardHolder: '',
      cardNumber: '',
      expiry: '',
      cvv: '',
      otp: ''
    })

    // 表单即时校验与错误信息
    const formErrors = reactive({
      cardHolder: '',
      cardNumber: '',
      expiry: '',
      cvv: '',
      otp: ''
    })

    // Luhn 校验实现（避免对外部依赖的执行顺序问题）
    const isValidCardNumber = (num) => {
      const sanitized = String(num).replace(/\s+/g, '')
      if (!/^\d{12,19}$/.test(sanitized)) return false
      let sum = 0
      let shouldDouble = false
      for (let i = sanitized.length - 1; i >= 0; i--) {
        let digit = parseInt(sanitized[i], 10)
        if (shouldDouble) {
          digit *= 2
          if (digit > 9) digit -= 9
        }
        sum += digit
        shouldDouble = !shouldDouble
      }
      return sum % 10 === 0
    }

    const validateCardHolder = (val) => {
      formErrors.cardHolder = !val || String(val).trim() === '' ? '请输入持卡人姓名' : ''
    }
    const validateCardNumber = (val) => {
      formErrors.cardNumber = isValidCardNumber(val) ? '' : '信用卡号格式不正确'
    }
    const validateExpiry = (val) => {
      const ok = /^\d{2}\/\d{2}$/.test(val)
      if (!ok) {
        formErrors.expiry = '有效期格式应为MM/YY'
        return
      }
      const [mm, yy] = val.split('/')
      const m = parseInt(mm, 10)
      formErrors.expiry = m >= 1 && m <= 12 ? '' : '月份应在01-12之间'
    }
    const validateCvv = (val) => {
      formErrors.cvv = /^\d{3,4}$/.test(val) ? '' : 'CVV格式不正确'
    }
    const validateOtp = (val) => {
      formErrors.otp = val && String(val).length >= 4 ? '' : '请输入短信验证码'
    }

    watch(() => creditCard.cardHolder, validateCardHolder)
    watch(() => creditCard.cardNumber, validateCardNumber)
    watch(() => creditCard.expiry, validateExpiry)
    watch(() => creditCard.cvv, validateCvv)
    watch(() => creditCard.otp, validateOtp)

    const isCardFormValid = computed(() => {
      return (
        !formErrors.cardHolder &&
        !formErrors.cardNumber &&
        !formErrors.expiry &&
        !formErrors.cvv &&
        !formErrors.otp &&
        creditCard.cardHolder &&
        creditCard.cardNumber &&
        creditCard.expiry &&
        creditCard.cvv &&
        creditCard.otp
      )
    })

    // 订单加载状态（用于骨架屏）
    const isOrderLoaded = computed(() => !!orderInfo.orderNo)

    // 获取订单信息
    const getOrderInfo = async () => {
      console.log('开始获取订单信息...');
      try {
        const orderNo = route.params.orderNo || route.query.orderNo;
        console.log('订单号:', orderNo);
        if (!orderNo) {
          ElMessage.error('订单号不能为空');
          router.push('/orders');
          return;
        }

        console.log('调用 orderApi.getOrderByNo...');
        const response = await orderApi.getOrderByNo(orderNo);
        console.log('获取订单信息响应:', response);

        if (response.code === 200) {
          const order = response.data;
          console.log('成功获取订单信息:', order);
          Object.assign(orderInfo, {
            orderNo: order.orderNo,
            serviceName: order.serviceName,
            appointmentTime: order.appointmentTime,
            location: order.location || '上门服务',
            totalAmount: order.totalAmount
          });
          payAmount.value = order.totalAmount;
        } else {
          console.error('获取订单信息失败:', response.message);
          ElMessage.error(response.message || '获取订单信息失败');
          router.push('/orders');
        }
      } catch (error) {
        console.error('获取订单信息时发生异常:', error);
        ElMessage.error('获取订单信息时发生异常');
        router.push('/orders');
      }
    };

    // 选择支付方式
    const selectPaymentMethod = (method) => {
      selectedMethod.value = method
    }

    const handleOptionKeydown = (method, e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault()
        selectPaymentMethod(method)
      }
    }

    // 处理支付
    const processPayment = async () => {
      console.log('开始处理支付...');
      if (!selectedMethod.value) {
        ElMessage.warning('请选择支付方式');
        return;
      }

      // 信用卡前端校验
      if (selectedMethod.value === 'credit_card') {
        console.log('校验信用卡信息...');
        if (!creditCard.cardHolder || !creditCard.cardNumber || !creditCard.expiry || !creditCard.cvv) {
          ElMessage.error('请完整填写信用卡信息');
          return;
        }
        if (!luhnCheck(creditCard.cardNumber)) {
          ElMessage.error('信用卡号格式不正确');
          return;
        }
        if (!/^\d{2}\/\d{2}$/.test(creditCard.expiry)) {
          ElMessage.error('有效期格式应为MM/YY');
          return;
        }
        if (!/^\d{3,4}$/.test(creditCard.cvv)) {
          ElMessage.error('CVV格式不正确');
          return;
        }
        if (!creditCard.otp || creditCard.otp.length < 4) {
          ElMessage.error('请输入短信验证码');
          return;
        }
        console.log('信用卡信息校验通过。');
      }

      loading.value = true;
      try {
        const paymentData = {
          orderNo: orderInfo.orderNo,
          amount: payAmount.value,
          paymentMethod: selectedMethod.value
        };
        console.log('支付数据:', paymentData);

        // 信用卡加密载荷
        if (selectedMethod.value === 'credit_card') {
          console.log('获取公钥...');
          const pubResp = await paymentApi.getPublicKey();
          console.log('获取公钥响应:', pubResp);
          if (pubResp.code !== 200 || !pubResp.data) {
            throw new Error('获取公钥失败');
          }
          const publicKeyPem = pubResp.data;
          const payload = {
            cardHolder: creditCard.cardHolder.trim(),
            cardNumber: creditCard.cardNumber.replace(/\s+/g, ''),
            expiry: creditCard.expiry.trim(),
            cvv: creditCard.cvv.trim()
          };
          console.log('加密前的信用卡数据:', payload);
          const securePayload = await encryptWithRsaOaep(publicKeyPem, JSON.stringify(payload));
          console.log('加密后的信用卡数据:', securePayload);
          paymentData.securePayload = securePayload;
        }

        console.log('调用 paymentApi.createPayment...');
        const response = await paymentApi.createPayment(paymentData);
        console.log('创建支付响应:', response);

        if (response.code === 200) {
          paymentNo.value = response.data.paymentNo;
          showPaymentModal.value = true;
          paymentStatus.value = 'processing';
          
          // 模拟支付处理
          setTimeout(() => {
            checkPaymentStatus();
          }, 3000);
        } else {
          console.error('创建支付订单失败:', response.message);
          ElMessage.error(response.message || '创建支付订单失败');
        }
      } catch (error) {
        console.error('支付处理时发生异常:', error);
        ElMessage.error('支付处理时发生异常，请重试');
      } finally {
        loading.value = false;
      }
    };

    // 检查支付状态
    const checkPaymentStatus = async () => {
      try {
        const response = await paymentApi.getPaymentStatus(paymentNo.value)
        if (response.code === 200) {
          const status = response.data.status
          if (status === 'paid') {
            paymentStatus.value = 'success'
          } else if (status === 'failed' || status === 'cancelled') {
            paymentStatus.value = 'failed'
            paymentError.value = response.data.failureReason || '支付失败'
          } else {
            // 继续轮询
            setTimeout(() => {
              checkPaymentStatus()
            }, 2000)
          }
        }
      } catch (error) {
        console.error('查询支付状态失败:', error)
        paymentStatus.value = 'failed'
        paymentError.value = '查询支付状态失败'
      }
    }

    // 取消支付
    const cancelPayment = () => {
      router.push('/orders')
    }

    // 关闭支付弹窗
    const closePaymentModal = () => {
      if (paymentStatus.value === 'processing') {
        return // 支付处理中不允许关闭
      }
      showPaymentModal.value = false
    }

    // 重新支付
    const retryPayment = () => {
      showPaymentModal.value = false
      paymentStatus.value = ''
      paymentError.value = ''
      paymentNo.value = ''
    }

    // 跳转到订单页面
    const goToOrders = () => {
      router.push('/orders')
    }

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      getOrderInfo()
    })

    // === helpers ===
    const luhnCheck = (pan) => {
      const digits = (pan || '').replace(/[^0-9]/g, '')
      if (!digits || digits.length < 12) return false
      let sum = 0, alt = false
      for (let i = digits.length - 1; i >= 0; i--) {
        let n = digits.charCodeAt(i) - 48
        if (alt) {
          n *= 2
          if (n > 9) n -= 9
        }
        sum += n
        alt = !alt
      }
      return sum % 10 === 0
    }

    const encryptWithRsaOaep = async (pem, plaintext) => {
      const key = await importRsaPublicKey(pem)
      const enc = new TextEncoder()
      const data = enc.encode(plaintext)
      const cipher = await window.crypto.subtle.encrypt(
        { name: 'RSA-OAEP' },
        key,
        data
      )
      return arrayBufferToBase64(cipher)
    }

    const importRsaPublicKey = async (pem) => {
      const b64 = pem
        .replace('-----BEGIN PUBLIC KEY-----', '')
        .replace('-----END PUBLIC KEY-----', '')
        .replace(/\r/g, '')
        .replace(/\n/g, '')
        .trim()
      const raw = base64ToArrayBuffer(b64)
      return await window.crypto.subtle.importKey(
        'spki',
        raw,
        { name: 'RSA-OAEP', hash: 'SHA-256' },
        false,
        ['encrypt']
      )
    }

    const base64ToArrayBuffer = (base64) => {
      const binaryString = window.atob(base64)
      const len = binaryString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i)
      }
      return bytes.buffer
    }

    const arrayBufferToBase64 = (buffer) => {
      const bytes = new Uint8Array(buffer)
      let binary = ''
      const chunkSize = 0x8000
      for (let i = 0; i < bytes.length; i += chunkSize) {
        const chunk = bytes.subarray(i, i + chunkSize)
        binary += String.fromCharCode.apply(null, chunk)
      }
      return window.btoa(binary)
    }

      return {
        loading,
        selectedMethod,
        showPaymentModal,
        paymentStatus,
        paymentError,
        orderInfo,
        creditCard,
        payAmount,
        formErrors,
        isCardFormValid,
        isOrderLoaded,
        selectPaymentMethod,
        handleOptionKeydown,
        processPayment,
        cancelPayment,
        closePaymentModal,
        retryPayment,
        goToOrders,
        formatDateTime
      }
    }
  }
</script>

<style scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.payment-container {
  min-height: 100vh;
  /* 可根据品牌色调整 */
  --brand-primary: #3498db;
  --brand-primary-dark: #1f5f8b;
  --brand-accent: #2980b9;
  --surface: #ffffff;
  --surface-alt: #f7f9fb;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --border-color: #ecf0f1;
  --error-color: #e74c3c;
  --success-color: #27ae60;
  background: linear-gradient(135deg, #e3f2fd 0%, #f0f5ff 100%);
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.payment-card {
  background: var(--surface);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 32px;
  max-width: 500px;
  width: 100%;
  position: relative;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.payment-header {
  text-align: center;
  margin-bottom: 32px;
}

.payment-header h2 {
  color: var(--text-primary);
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 600;
}

.order-info {
  color: var(--text-secondary);
  font-size: 14px;
}

.order-details {
  margin-bottom: 32px;
}

.order-details h3 {
  color: var(--text-primary);
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-item:last-child {
  border-bottom: none;
}

.total-amount {
  font-weight: 600;
  font-size: 16px;
}

.amount {
  color: var(--error-color);
  font-size: 20px;
  font-weight: 700;
}

.payment-methods {
  margin-bottom: 32px;
}

.payment-methods h3 {
  color: var(--text-primary);
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-form {
  background: var(--surface-alt);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}
.card-form h3 {
  margin-bottom: 12px;
  color: var(--text-primary);
}
.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}
.form-row.two-cols {
  flex-direction: row;
  gap: 12px;
}
.form-row label { font-weight: 600; color: var(--text-primary); }
.form-row input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}
.hint { color: #7f8c8d; font-size: 12px; }

.error-msg {
  color: var(--error-color);
  font-size: 12px;
}
.input-invalid {
  border-color: var(--error-color) !important;
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.15);
}
.input-valid {
  border-color: var(--success-color) !important;
  box-shadow: 0 0 0 2px rgba(39, 174, 96, 0.12);
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.payment-option:hover {
  border-color: var(--brand-primary);
  background-color: #f8f9fa;
}

.payment-option.active {
  border-color: var(--brand-primary);
  background-color: #e3f2fd;
}

.payment-icon {
  margin-right: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  position: absolute;
  right: 16px;
  color: var(--brand-primary);
  font-weight: bold;
  font-size: 18px;
}

.payment-actions {
  display: flex;
  gap: 16px;
}

.btn-cancel {
  flex: 1;
  padding: 14px;
  border: 2px solid #95a5a6;
  background: white;
  color: #95a5a6;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #95a5a6;
  color: white;
}

.btn-pay {
  flex: 2;
  padding: 14px;
  border: none;
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-accent));
  color: white;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-pay:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--brand-accent), var(--brand-primary-dark));
  transform: translateY(-2px);
}

.btn-pay:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 支付弹窗样式 */
.payment-modal-overlay {
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

.payment-modal {
  background: white;
  border-radius: 16px;
  padding: 32px;
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #ecf0f1;
  border-top: 4px solid var(--brand-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.success-icon {
  width: 60px;
  height: 60px;
  background: var(--success-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin: 0 auto 16px;
}

.error-icon {
  width: 60px;
  height: 60px;
  background: var(--error-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin: 0 auto 16px;
}

.qr-code-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 20px auto;
  color: #7f8c8d;
}

.btn-primary {
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-accent));
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 16px;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: linear-gradient(135deg, var(--brand-accent), var(--brand-primary-dark));
  transform: translateY(-2px);
}

/* 骨架屏 */
.skeleton {
  display: grid;
  gap: 12px;
}
.skeleton-line {
  height: 14px;
  background: linear-gradient(90deg, #eee, #f5f5f5, #eee);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: 6px;
}
.skeleton-line.wide { height: 20px; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .payment-container {
    padding: 10px;
  }
  
  .payment-card {
    padding: 20px;
  }
  
  .payment-actions {
    flex-direction: column;
  }
}
</style>