package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.carwash.dto.ChatRequest;
import com.carwash.dto.ChatResponse;
import com.carwash.entity.ChatMessage;
import com.carwash.entity.KnowledgeBase;
import com.carwash.entity.Service;
import com.carwash.mapper.ChatMessageMapper;
import com.carwash.mapper.KnowledgeBaseMapper;
import com.carwash.mapper.ServiceMapper;
import com.carwash.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI客服服务实现类
 * 
 * 功能特性：
 * 1. 意图识别 - 基于关键词和规则的NLP处理
 * 2. 知识库匹配 - 智能匹配用户问题
 * 3. 上下文管理 - 维护对话历史
 * 4. 服务推荐 - 根据用户意图推荐相关服务
 * 5. 学习优化 - 记录对话用于后续分析
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ServiceMapper serviceMapper;

    // 意图关键词映射
    private static final Map<String, List<String>> INTENT_KEYWORDS = new HashMap<>();
    
    // 问候语模式
    private static final List<String> GREETING_PATTERNS = Arrays.asList(
        "你好", "您好", "hi", "hello", "嗨", "在吗", "在不在", "有人吗"
    );
    
    // 快捷问题列表
    private static final List<String> QUICK_QUESTIONS = Arrays.asList(
        "你们有哪些洗车服务？",
        "洗车多少钱？",
        "如何预约洗车？",
        "我的订单在哪里查看？",
        "可以取消预约吗？",
        "营业时间是什么时候？",
        "支持哪些支付方式？",
        "如何联系客服？"
    );

    static {
        // 初始化意图关键词
        INTENT_KEYWORDS.put("greeting", Arrays.asList("你好", "您好", "hi", "hello", "嗨", "在吗"));
        INTENT_KEYWORDS.put("service_inquiry", Arrays.asList("服务", "项目", "洗车", "打蜡", "抛光", "美容", "有什么", "哪些"));
        INTENT_KEYWORDS.put("price_inquiry", Arrays.asList("价格", "多少钱", "费用", "收费", "贵", "便宜", "优惠"));
        INTENT_KEYWORDS.put("booking_help", Arrays.asList("预约", "预订", "怎么约", "如何预约", "订", "约"));
        INTENT_KEYWORDS.put("order_status", Arrays.asList("订单", "进度", "状态", "查询", "我的订单", "查看"));
        INTENT_KEYWORDS.put("payment", Arrays.asList("支付", "付款", "付钱", "微信", "支付宝", "退款"));
        INTENT_KEYWORDS.put("time_inquiry", Arrays.asList("时间", "营业", "几点", "什么时候", "开门", "关门"));
        INTENT_KEYWORDS.put("complaint", Arrays.asList("投诉", "建议", "不满意", "问题", "差评", "反馈"));
        INTENT_KEYWORDS.put("contact", Arrays.asList("联系", "电话", "客服", "人工", "转人工"));
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("【AI客服】收到用户消息: {}", request.getMessage());
        
        // 生成或获取会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }
        
        // 保存用户消息
        saveMessage(sessionId, request.getUserId(), "user", request.getMessage(), null, null);
        
        // 识别用户意图
        IntentResult intentResult = recognizeIntent(request.getMessage());
        log.info("【AI客服】识别意图: {} (置信度: {})", intentResult.intent, intentResult.confidence);
        
        // 生成AI回复
        ChatResponse response = generateResponse(intentResult, request, sessionId);
        
        // 保存AI回复
        saveMessage(sessionId, request.getUserId(), "assistant", response.getMessage(), 
                    intentResult.intent, intentResult.confidence);
        
        return response;
    }

    @Override
    public List<ChatMessage> getSessionHistory(String sessionId) {
        return chatMessageMapper.findBySessionId(sessionId);
    }

    @Override
    public void submitFeedback(String sessionId, Integer rating, Boolean resolved) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .set(ChatMessage::getRating, rating)
               .set(ChatMessage::getResolved, resolved ? 1 : 0);
        chatMessageMapper.update(null, wrapper);
        log.info("【AI客服】用户评价已提交: sessionId={}, rating={}, resolved={}", sessionId, rating, resolved);
    }

    @Override
    public List<String> getQuickQuestions() {
        return QUICK_QUESTIONS;
    }

    @Override
    public void clearSession(String sessionId) {
        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .set(ChatMessage::getDeleted, 1);
        chatMessageMapper.update(null, wrapper);
        log.info("【AI客服】会话已清除: {}", sessionId);
    }

    /**
     * 意图识别结果
     */
    private static class IntentResult {
        String intent;
        Double confidence;
        List<String> matchedKeywords;
        
        IntentResult(String intent, Double confidence, List<String> matchedKeywords) {
            this.intent = intent;
            this.confidence = confidence;
            this.matchedKeywords = matchedKeywords;
        }
    }

    /**
     * 识别用户意图
     */
    private IntentResult recognizeIntent(String message) {
        String normalizedMsg = message.toLowerCase().trim();
        
        // 检查问候语
        for (String pattern : GREETING_PATTERNS) {
            if (normalizedMsg.contains(pattern)) {
                return new IntentResult("greeting", 0.95, Collections.singletonList(pattern));
            }
        }
        
        // 关键词匹配
        Map<String, List<String>> intentMatches = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : INTENT_KEYWORDS.entrySet()) {
            List<String> matched = new ArrayList<>();
            for (String keyword : entry.getValue()) {
                if (normalizedMsg.contains(keyword.toLowerCase())) {
                    matched.add(keyword);
                }
            }
            if (!matched.isEmpty()) {
                intentMatches.put(entry.getKey(), matched);
            }
        }
        
        if (intentMatches.isEmpty()) {
            // 尝试从知识库匹配
            List<KnowledgeBase> kbMatches = searchKnowledgeBase(normalizedMsg);
            if (!kbMatches.isEmpty()) {
                return new IntentResult(kbMatches.get(0).getCategory(), 0.7, Collections.emptyList());
            }
            return new IntentResult("other", 0.3, Collections.emptyList());
        }
        
        // 选择匹配度最高的意图
        String bestIntent = intentMatches.entrySet().stream()
            .max(Comparator.comparingInt(e -> e.getValue().size()))
            .map(Map.Entry::getKey)
            .orElse("other");
        
        List<String> matchedKeywords = intentMatches.get(bestIntent);
        double confidence = Math.min(0.5 + matchedKeywords.size() * 0.15, 0.95);
        
        return new IntentResult(bestIntent, confidence, matchedKeywords);
    }

    /**
     * 生成AI回复
     */
    private ChatResponse generateResponse(IntentResult intentResult, ChatRequest request, String sessionId) {
        ChatResponse.ChatResponseBuilder builder = ChatResponse.builder()
            .sessionId(sessionId)
            .intent(intentResult.intent)
            .confidence(intentResult.confidence)
            .needHumanSupport(false)
            .timestamp(LocalDateTime.now());
        
        String message;
        List<ChatResponse.SuggestedAction> actions = new ArrayList<>();
        List<ChatResponse.ServiceRecommendation> recommendations = new ArrayList<>();
        
        switch (intentResult.intent) {
            case "greeting":
                message = generateGreeting();
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("quick_reply")
                    .label("查看服务")
                    .action("view_services")
                    .build());
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("quick_reply")
                    .label("立即预约")
                    .action("make_booking")
                    .url("/appointment")
                    .build());
                break;
                
            case "service_inquiry":
                message = generateServiceResponse();
                recommendations = getServiceRecommendations();
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("navigation")
                    .label("查看全部服务")
                    .action("view_all_services")
                    .url("/services")
                    .build());
                break;
                
            case "price_inquiry":
                message = generatePriceResponse();
                recommendations = getServiceRecommendations();
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("navigation")
                    .label("查看价目表")
                    .action("view_prices")
                    .url("/services")
                    .build());
                break;
                
            case "booking_help":
                message = generateBookingHelpResponse();
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("navigation")
                    .label("立即预约")
                    .action("make_booking")
                    .url("/appointment")
                    .build());
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("quick_reply")
                    .label("预约流程说明")
                    .action("booking_guide")
                    .build());
                break;
                
            case "order_status":
                message = generateOrderStatusResponse(request.getUserId());
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("navigation")
                    .label("查看我的订单")
                    .action("view_orders")
                    .url("/orders")
                    .build());
                break;
                
            case "payment":
                message = generatePaymentResponse();
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("navigation")
                    .label("查看支付记录")
                    .action("view_payments")
                    .url("/payment-records")
                    .build());
                break;
                
            case "time_inquiry":
                message = generateTimeResponse();
                break;
                
            case "complaint":
                message = "非常抱歉给您带来了不好的体验！我们非常重视您的反馈。\n\n" +
                         "您可以：\n" +
                         "1. 详细描述您遇到的问题，我会尽力帮您解决\n" +
                         "2. 联系人工客服获取更专业的帮助\n\n" +
                         "我们一定会认真对待您的每一条建议！";
                builder.needHumanSupport(true);
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("action")
                    .label("转人工客服")
                    .action("transfer_human")
                    .build());
                break;
                
            case "contact":
                message = "您可以通过以下方式联系我们：\n\n" +
                         "📞 客服热线：400-888-8888\n" +
                         "📱 微信客服：carwash_service\n" +
                         "📧 邮箱：service@carwash.com\n" +
                         "🕐 服务时间：8:00-22:00\n\n" +
                         "如需人工客服，请点击下方按钮。";
                builder.needHumanSupport(true);
                actions.add(ChatResponse.SuggestedAction.builder()
                    .type("action")
                    .label("转人工客服")
                    .action("transfer_human")
                    .build());
                break;
                
            default:
                // 尝试从知识库查找答案
                message = searchAndGenerateResponse(request.getMessage());
                if (message == null) {
                    message = "抱歉，我没有完全理解您的问题。您可以：\n\n" +
                             "1. 换一种方式描述您的问题\n" +
                             "2. 选择下方的快捷问题\n" +
                             "3. 联系人工客服获取帮助\n\n" +
                             "我会不断学习，努力为您提供更好的服务！";
                }
                break;
        }
        
        return builder
            .message(message)
            .suggestedActions(actions)
            .recommendations(recommendations)
            .build();
    }

    /**
     * 生成问候语
     */
    private String generateGreeting() {
        int hour = LocalDateTime.now().getHour();
        String timeGreeting;
        if (hour < 12) {
            timeGreeting = "早上好";
        } else if (hour < 18) {
            timeGreeting = "下午好";
        } else {
            timeGreeting = "晚上好";
        }
        
        return timeGreeting + "！欢迎来到汽车洗车服务预约系统！🚗\n\n" +
               "我是您的AI智能助手，可以帮您：\n" +
               "• 了解我们的洗车服务\n" +
               "• 查询服务价格\n" +
               "• 预约洗车服务\n" +
               "• 查看订单状态\n\n" +
               "请问有什么可以帮您的吗？";
    }

    /**
     * 生成服务介绍回复
     */
    private String generateServiceResponse() {
        List<Service> services = getActiveServices();
        StringBuilder sb = new StringBuilder();
        sb.append("我们提供以下专业洗车服务：\n\n");
        
        for (Service service : services) {
            sb.append("🚙 **").append(service.getName()).append("**\n");
            sb.append("   价格：¥").append(service.getPrice()).append("\n");
            sb.append("   时长：约").append(service.getDuration()).append("分钟\n");
            if (service.getDescription() != null) {
                sb.append("   ").append(service.getDescription()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("点击下方按钮可查看全部服务详情，或直接预约~");
        return sb.toString();
    }

    /**
     * 生成价格咨询回复
     */
    private String generatePriceResponse() {
        List<Service> services = getActiveServices();
        StringBuilder sb = new StringBuilder();
        sb.append("以下是我们的服务价格表：\n\n");
        
        for (Service service : services) {
            sb.append("• ").append(service.getName())
              .append("：¥").append(service.getPrice())
              .append("（约").append(service.getDuration()).append("分钟）\n");
        }
        
        sb.append("\n💡 温馨提示：\n");
        sb.append("- 会员用户享受9折优惠\n");
        sb.append("- 首次预约可获得新人礼包\n");
        sb.append("- 支持微信、支付宝等多种支付方式");
        
        return sb.toString();
    }

    /**
     * 生成预约帮助回复
     */
    private String generateBookingHelpResponse() {
        return "预约洗车非常简单！只需3步：\n\n" +
               "**第1步：选择服务** 🚗\n" +
               "在服务列表中选择您需要的洗车项目\n\n" +
               "**第2步：选择时间** 📅\n" +
               "选择方便的日期和时间段\n\n" +
               "**第3步：确认预约** ✅\n" +
               "填写车辆信息，完成预约\n\n" +
               "预约成功后，您会收到确认通知。\n" +
               "按时到店即可享受服务！\n\n" +
               "点击下方【立即预约】开始吧~";
    }

    /**
     * 生成订单状态回复
     */
    private String generateOrderStatusResponse(Long userId) {
        if (userId == null) {
            return "请先登录后再查看订单状态哦~\n\n" +
                   "登录后您可以：\n" +
                   "• 查看所有历史订单\n" +
                   "• 跟踪订单实时状态\n" +
                   "• 管理预约信息";
        }
        
        return "您可以在【我的订单】页面查看所有订单详情。\n\n" +
               "订单状态说明：\n" +
               "• 🟡 待确认 - 订单已提交，等待确认\n" +
               "• 🔵 已确认 - 订单已确认，请按时到店\n" +
               "• 🟢 进行中 - 服务正在进行\n" +
               "• ✅ 已完成 - 服务已完成\n" +
               "• ❌ 已取消 - 订单已取消\n\n" +
               "点击下方按钮查看您的订单~";
    }

    /**
     * 生成支付相关回复
     */
    private String generatePaymentResponse() {
        return "我们支持以下支付方式：\n\n" +
               "💳 **在线支付**\n" +
               "• 支付宝支付\n" +
               "• 微信支付\n" +
               "• 银行卡支付\n\n" +
               "💰 **其他方式**\n" +
               "• 到店现金支付\n" +
               "• 会员余额支付\n\n" +
               "⚡ 在线支付即时到账，安全便捷！\n\n" +
               "如需退款，请在订单详情页申请，我们会在1-3个工作日内处理。";
    }

    /**
     * 生成营业时间回复
     */
    private String generateTimeResponse() {
        return "我们的营业时间如下：\n\n" +
               "🕐 **工作日**：8:00 - 22:00\n" +
               "🕐 **周末/节假日**：8:00 - 22:00\n\n" +
               "⏰ **建议预约时间**：\n" +
               "• 工作日上午 - 人少不排队\n" +
               "• 避开周末下午高峰期\n\n" +
               "💡 提前预约可确保您到店即可享受服务，无需等待！";
    }

    /**
     * 从知识库搜索并生成回复
     */
    private String searchAndGenerateResponse(String query) {
        List<KnowledgeBase> results = searchKnowledgeBase(query);
        if (!results.isEmpty()) {
            KnowledgeBase best = results.get(0);
            knowledgeBaseMapper.incrementHitCount(best.getId());
            return best.getAnswer();
        }
        return null;
    }

    /**
     * 搜索知识库
     */
    private List<KnowledgeBase> searchKnowledgeBase(String query) {
        // 提取关键词
        String[] words = query.split("[\\s,，。？！]+");
        List<KnowledgeBase> allResults = new ArrayList<>();
        
        for (String word : words) {
            if (word.length() >= 2) {
                List<KnowledgeBase> results = knowledgeBaseMapper.searchByKeyword(word, 5);
                allResults.addAll(results);
            }
        }
        
        // 去重并按优先级排序
        return allResults.stream()
            .distinct()
            .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
            .limit(3)
            .collect(Collectors.toList());
    }

    /**
     * 获取服务推荐
     */
    private List<ChatResponse.ServiceRecommendation> getServiceRecommendations() {
        List<Service> services = getActiveServices();
        return services.stream()
            .limit(3)
            .map(s -> ChatResponse.ServiceRecommendation.builder()
                .serviceId(s.getId())
                .serviceName(s.getName())
                .description(s.getDescription())
                .price(s.getPrice().doubleValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 获取活跃服务列表
     */
    private List<Service> getActiveServices() {
        LambdaQueryWrapper<Service> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Service::getStatus, 1)
               .eq(Service::getDeleted, 0)
               .orderByAsc(Service::getSortOrder);
        return serviceMapper.selectList(wrapper);
    }

    /**
     * 保存消息
     */
    private void saveMessage(String sessionId, Long userId, String role, String content, 
                            String intent, Double confidence) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setIntent(intent);
        message.setConfidence(confidence);
        message.setCreatedAt(LocalDateTime.now());
        message.setDeleted(0);
        message.setResolved(0);
        
        chatMessageMapper.insert(message);
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "chat_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
