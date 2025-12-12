package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI客服聊天消息Mapper
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据会话ID查询历史消息
     */
    @Select("SELECT * FROM chat_messages WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY created_at ASC")
    List<ChatMessage> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计用户对话次数
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE user_id = #{userId} AND deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 查询最近N条消息用于上下文
     */
    @Select("SELECT * FROM chat_messages WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<ChatMessage> findRecentMessages(@Param("sessionId") String sessionId, @Param("limit") int limit);
}
