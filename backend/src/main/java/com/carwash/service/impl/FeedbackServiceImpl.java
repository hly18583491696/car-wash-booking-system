package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.entity.Feedback;
import com.carwash.mapper.FeedbackMapper;
import com.carwash.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户反馈服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFeedback(Feedback feedback) {
        log.info("创建反馈开始");
        
        int result = feedbackMapper.insert(feedback);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建反馈失败");
        }
        
        log.info("反馈创建成功，反馈ID: {}", feedback.getId());
        return feedback.getId();
    }

    @Override
    public IPage<Feedback> getUserFeedback(Long userId, Page<Feedback> page) {
        return feedbackMapper.selectUserFeedback(page, userId);
    }

    @Override
    public List<Feedback> getBookingFeedback(Long bookingId) {
        return feedbackMapper.selectByBookingId(bookingId);
    }

    @Override
    public IPage<Feedback> getPendingFeedback(Page<Feedback> page) {
        return feedbackMapper.selectPendingFeedback(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyFeedback(Long feedbackId, String reply) {
        log.info("回复反馈，反馈ID: {}", feedbackId);
        
        int result = feedbackMapper.updateReply(feedbackId, reply);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "回复反馈失败");
        }
        
        log.info("反馈回复成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFeedbackStatus(Long feedbackId, String status) {
        log.info("更新反馈状态，反馈ID: {}, 状态: {}", feedbackId, status);
        
        int result = feedbackMapper.updateStatus(feedbackId, status);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新反馈状态失败");
        }
        
        log.info("反馈状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long feedbackId) {
        log.info("删除反馈（软删除），反馈ID: {}", feedbackId);
        
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "反馈不存在");
        }
        
        // 软删除
        feedback.setDeleted(1);
        int result = feedbackMapper.updateById(feedback);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除反馈失败");
        }
        
        log.info("反馈删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteFeedback(Long feedbackId) {
        log.info("永久删除反馈（硬删除），反馈ID: {}", feedbackId);

        // 验证反馈ID
        if (feedbackId == null) {
            log.error("反馈ID为空");
            throw new BusinessException(ResultCode.PARAM_ERROR, "反馈ID不能为空");
        }

        // 检查反馈是否存在
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            log.error("反馈不存在，反馈ID: {}", feedbackId);
            throw new BusinessException(ResultCode.NOT_FOUND, "反馈不存在");
        }

        log.info("反馈删除前状态 - 反馈ID: {}, deleted: {}, 状态: {}", 
            feedbackId, feedback.getDeleted(), feedback.getStatus());

        // 执行硬删除，从数据库中完全移除记录
        log.info("执行数据库删除操作");
        int result = feedbackMapper.deleteById(feedbackId);
        log.info("数据库删除结果: {}", result);
        
        if (result <= 0) {
            log.error("硬删除反馈失败，数据库操作失败，反馈ID: {}, 影响行数: {}", feedbackId, result);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "硬删除反馈失败，数据库操作失败");
        }

        // 验证删除结果，确认记录不再存在
        log.info("验证数据库删除结果");
        Feedback deletedFeedback = feedbackMapper.selectById(feedbackId);
        
        if (deletedFeedback != null) {
            log.error("验证失败：删除后反馈仍然存在，反馈ID: {}", feedbackId);
            
            // 尝试重新删除一次
            log.info("尝试重新删除反馈");
            int retryResult = feedbackMapper.deleteById(feedbackId);
            
            if (retryResult <= 0) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "重试硬删除反馈失败");
            }
            
            // 再次验证
            Feedback finalFeedback = feedbackMapper.selectById(feedbackId);
            if (finalFeedback != null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, 
                    "数据库删除验证失败，记录仍然存在");
            }
        }

        log.info("反馈硬删除成功，反馈ID: {}, 记录已从数据库中完全移除", feedbackId);
    }
}
