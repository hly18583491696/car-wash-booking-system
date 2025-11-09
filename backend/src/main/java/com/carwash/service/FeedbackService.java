package com.carwash.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.entity.Feedback;

import java.util.List;

/**
 * 用户反馈服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface FeedbackService {

    /**
     * 创建反馈
     */
    Long createFeedback(Feedback feedback);

    /**
     * 获取用户反馈列表
     */
    IPage<Feedback> getUserFeedback(Long userId, Page<Feedback> page);

    /**
     * 获取订单反馈列表
     */
    List<Feedback> getBookingFeedback(Long bookingId);

    /**
     * 获取待处理反馈
     */
    IPage<Feedback> getPendingFeedback(Page<Feedback> page);

    /**
     * 回复反馈
     */
    void replyFeedback(Long feedbackId, String reply);

    /**
     * 更新反馈状态
     */
    void updateFeedbackStatus(Long feedbackId, String status);

    /**
     * 删除反馈（软删除）
     */
    void deleteFeedback(Long feedbackId);

    /**
     * 永久删除反馈（硬删除）
     */
    void permanentlyDeleteFeedback(Long feedbackId);
}
