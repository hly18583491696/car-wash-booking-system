package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.ChatRequest;
import com.carwash.dto.ChatResponse;
import com.carwash.entity.ChatMessage;
import com.carwash.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI客服控制器
 * 
 * 提供AI智能客服相关的API接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 发送消息并获取AI回复
     * 
     * @param request 聊天请求
     * @return AI回复
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // 尝试获取当前登录用户ID
            Long userId = getCurrentUserId();
            if (userId != null && request.getUserId() == null) {
                request.setUserId(userId);
            }
            
            ChatResponse response = aiChatService.chat(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("【AI客服】处理消息失败", e);
            return Result.error("AI客服暂时无法响应，请稍后再试");
        }
    }

    /**
     * 获取会话历史记录
     * 
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        try {
            List<ChatMessage> history = aiChatService.getSessionHistory(sessionId);
            return Result.success(history);
        } catch (Exception e) {
            log.error("【AI客服】获取历史记录失败", e);
            return Result.error("获取历史记录失败");
        }
    }

    /**
     * 提交用户评价反馈
     * 
     * @param sessionId 会话ID
     * @param feedback 评价内容
     * @return 操作结果
     */
    @PostMapping("/feedback/{sessionId}")
    public Result<Void> submitFeedback(@PathVariable String sessionId, 
                                       @RequestBody Map<String, Object> feedback) {
        try {
            Integer rating = (Integer) feedback.get("rating");
            Boolean resolved = (Boolean) feedback.get("resolved");
            
            aiChatService.submitFeedback(sessionId, rating, resolved);
            return Result.success(null);
        } catch (Exception e) {
            log.error("【AI客服】提交评价失败", e);
            return Result.error("提交评价失败");
        }
    }

    /**
     * 获取快捷问题列表
     * 
     * @return 快捷问题列表
     */
    @GetMapping("/quick-questions")
    public Result<List<String>> getQuickQuestions() {
        try {
            List<String> questions = aiChatService.getQuickQuestions();
            return Result.success(questions);
        } catch (Exception e) {
            log.error("【AI客服】获取快捷问题失败", e);
            return Result.error("获取快捷问题失败");
        }
    }

    /**
     * 清除会话记录
     * 
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        try {
            aiChatService.clearSession(sessionId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("【AI客服】清除会话失败", e);
            return Result.error("清除会话失败");
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.carwash.entity.User) {
                    return ((com.carwash.entity.User) principal).getId();
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败", e);
        }
        return null;
    }
}
