package com.carwash.service;

import com.carwash.dto.ChatRequest;
import com.carwash.dto.ChatResponse;
import com.carwash.entity.ChatMessage;

import java.util.List;

/**
 * AI客服服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface AiChatService {

    /**
     * 处理用户消息并生成AI回复
     * 
     * @param request 聊天请求
     * @return AI回复响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 获取会话历史记录
     * 
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    List<ChatMessage> getSessionHistory(String sessionId);

    /**
     * 提交用户评价
     * 
     * @param sessionId 会话ID
     * @param rating 评分（1-5）
     * @param resolved 是否解决
     */
    void submitFeedback(String sessionId, Integer rating, Boolean resolved);

    /**
     * 获取快捷问题列表
     * 
     * @return 常见问题列表
     */
    List<String> getQuickQuestions();

    /**
     * 清除会话
     * 
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);
}
