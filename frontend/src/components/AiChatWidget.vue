<template>
  <div class="ai-chat-widget">
    <!-- 聊天按钮 -->
    <transition name="bounce">
      <div 
        v-if="!isOpen" 
        class="chat-trigger"
        @click="openChat"
      >
        <div class="trigger-icon">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="trigger-pulse"></div>
        <span class="trigger-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
      </div>
    </transition>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-info">
            <div class="avatar">
              <el-icon :size="24"><Robot /></el-icon>
            </div>
            <div class="info-text">
              <span class="title">AI智能客服</span>
              <span class="status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>
          <div class="header-actions">
            <el-tooltip content="清除对话">
              <el-button :icon="Delete" circle size="small" @click="clearChat" />
            </el-tooltip>
            <el-tooltip content="关闭">
              <el-button :icon="Close" circle size="small" @click="closeChat" />
            </el-tooltip>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-section">
            <div class="welcome-icon">
              <el-icon :size="48"><Service /></el-icon>
            </div>
            <h3>您好！我是AI智能助手</h3>
            <p>有什么可以帮您的吗？</p>
            
            <!-- 快捷问题 -->
            <div class="quick-questions">
              <div 
                v-for="(question, index) in quickQuestions" 
                :key="index"
                class="quick-question"
                @click="sendQuickQuestion(question)"
              >
                {{ question }}
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              <el-icon v-if="msg.role === 'assistant'"><Robot /></el-icon>
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
              
              <!-- 建议操作按钮 -->
              <div v-if="msg.suggestedActions && msg.suggestedActions.length" class="suggested-actions">
                <el-button
                  v-for="(action, i) in msg.suggestedActions"
                  :key="i"
                  size="small"
                  :type="action.type === 'navigation' ? 'primary' : 'default'"
                  @click="handleAction(action)"
                >
                  {{ action.label }}
                </el-button>
              </div>
              
              <!-- 服务推荐 -->
              <div v-if="msg.recommendations && msg.recommendations.length" class="recommendations">
                <div 
                  v-for="rec in msg.recommendations" 
                  :key="rec.serviceId"
                  class="recommendation-card"
                  @click="goToService(rec.serviceId)"
                >
                  <span class="rec-name">{{ rec.serviceName }}</span>
                  <span class="rec-price">¥{{ rec.price }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="isLoading" class="message assistant">
            <div class="message-avatar">
              <el-icon><Robot /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble typing">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="输入您的问题..."
            :disabled="isLoading"
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button 
                type="primary" 
                :icon="Promotion"
                :loading="isLoading"
                @click="sendMessage"
              >
                发送
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- 底部提示 -->
        <div class="chat-footer">
          <span>AI助手为您服务 · 如需人工请说"转人工"</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, 
  Close, 
  Delete, 
  Promotion,
  Robot,
  User,
  Service
} from '@element-plus/icons-vue'
import { sendMessage as apiSendMessage, getQuickQuestions } from '@/api/aiChat'

const router = useRouter()

// 状态
const isOpen = ref(false)
const isLoading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const sessionId = ref(null)
const unreadCount = ref(0)
const quickQuestions = ref([])
const messagesContainer = ref(null)

// 初始化
onMounted(async () => {
  try {
    const res = await getQuickQuestions()
    if (res.code === 200 && res.data) {
      quickQuestions.value = res.data.slice(0, 6)
    }
  } catch (e) {
    console.error('获取快捷问题失败', e)
    // 使用默认快捷问题
    quickQuestions.value = [
      '你们有哪些洗车服务？',
      '洗车多少钱？',
      '如何预约洗车？',
      '营业时间是什么时候？'
    ]
  }
})

// 打开聊天窗口
const openChat = () => {
  isOpen.value = true
  unreadCount.value = 0
}

// 关闭聊天窗口
const closeChat = () => {
  isOpen.value = false
}

// 清除聊天记录
const clearChat = () => {
  messages.value = []
  sessionId.value = null
  ElMessage.success('对话已清除')
}

// 发送消息
const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text,
    timestamp: new Date()
  })
  inputMessage.value = ''
  
  // 滚动到底部
  await scrollToBottom()
  
  // 调用API
  isLoading.value = true
  try {
    const res = await apiSendMessage({
      message: text,
      sessionId: sessionId.value
    })
    
    if (res.code === 200 && res.data) {
      const data = res.data
      sessionId.value = data.sessionId
      
      // 添加AI回复
      messages.value.push({
        role: 'assistant',
        content: data.message,
        timestamp: new Date(data.timestamp) || new Date(),
        suggestedActions: data.suggestedActions,
        recommendations: data.recommendations,
        needHumanSupport: data.needHumanSupport
      })
      
      // 如果需要人工支持
      if (data.needHumanSupport) {
        ElMessage.info('如需人工客服，请拨打 400-888-8888')
      }
    } else {
      throw new Error(res.message || '请求失败')
    }
  } catch (e) {
    console.error('发送消息失败', e)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我遇到了一些问题，请稍后再试。',
      timestamp: new Date()
    })
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

// 发送快捷问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 处理建议操作
const handleAction = (action) => {
  if (action.url) {
    router.push(action.url)
    closeChat()
  } else if (action.action === 'transfer_human') {
    ElMessage.info('人工客服热线：400-888-8888')
  } else if (action.action === 'booking_guide') {
    inputMessage.value = '预约流程是怎样的？'
    sendMessage()
  }
}

// 跳转到服务详情
const goToService = (serviceId) => {
  router.push(`/appointment?serviceId=${serviceId}`)
  closeChat()
}

// 格式化消息（支持markdown简单格式）
const formatMessage = (text) => {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
    .replace(/•/g, '&bull;')
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 触发按钮 */
.chat-trigger {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  position: relative;
  transition: transform 0.3s, box-shadow 0.3s;
}

.chat-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

.trigger-icon {
  color: white;
}

.trigger-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}

.trigger-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background: #f56c6c;
  border-radius: 50%;
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 聊天窗口 */
.chat-window {
  width: 380px;
  height: 560px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-text {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.status {
  font-size: 12px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  animation: blink 2s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-actions :deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.header-actions :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f8f9fa;
}

/* 欢迎区域 */
.welcome-section {
  text-align: center;
  padding: 20px 0;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.welcome-section h3 {
  margin: 0 0 8px;
  color: #303133;
}

.welcome-section p {
  margin: 0 0 16px;
  color: #909399;
}

/* 快捷问题 */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-question {
  padding: 8px 12px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-question:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 消息样式 */
.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.user .message-avatar {
  background: #409eff;
  color: white;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message.assistant .message-bubble {
  background: white;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.message.user .message-time {
  text-align: right;
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 16px !important;
}

.typing .dot {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

/* 建议操作 */
.suggested-actions {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 服务推荐 */
.recommendations {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommendation-card {
  padding: 10px 12px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-card:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.rec-name {
  font-size: 13px;
  color: #303133;
}

.rec-price {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}

/* 输入区域 */
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: white;
}

.chat-input :deep(.el-input-group__append) {
  padding: 0;
}

.chat-input :deep(.el-input-group__append .el-button) {
  border-radius: 0 4px 4px 0;
}

/* 底部 */
.chat-footer {
  padding: 8px;
  text-align: center;
  font-size: 11px;
  color: #909399;
  background: #f8f9fa;
}

/* 动画 */
.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.3s reverse;
}
@keyframes bounce-in {
  0% { transform: scale(0); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.slide-up-enter-active {
  animation: slide-up 0.3s ease-out;
}
.slide-up-leave-active {
  animation: slide-up 0.2s ease-in reverse;
}
@keyframes slide-up {
  0% { opacity: 0; transform: translateY(20px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

/* 响应式 */
@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 100px);
    position: fixed;
    bottom: 80px;
    right: 16px;
  }
}
</style>
