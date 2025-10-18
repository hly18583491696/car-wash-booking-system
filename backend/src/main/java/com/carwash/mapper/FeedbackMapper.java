package com.carwash.mapper;

import com.carwash.entity.Feedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户反馈数据访问层
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 分页查询用户反馈
     */
    @Select("SELECT * FROM feedback WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    IPage<Feedback> selectUserFeedback(Page<Feedback> page, @Param("userId") Long userId);

    /**
     * 查询订单相关反馈
     */
    @Select("SELECT * FROM feedback WHERE booking_id = #{bookingId} AND deleted = 0")
    List<Feedback> selectByBookingId(@Param("bookingId") Long bookingId);

    /**
     * 分页查询待处理反馈
     */
    @Select("SELECT * FROM feedback WHERE status = 'pending' AND deleted = 0 ORDER BY created_at DESC")
    IPage<Feedback> selectPendingFeedback(Page<Feedback> page);

    /**
     * 更新反馈回复
     */
    @Update("UPDATE feedback SET reply = #{reply}, status = 'replied' WHERE id = #{feedbackId}")
    int updateReply(@Param("feedbackId") Long feedbackId, @Param("reply") String reply);

    /**
     * 更新反馈状态
     */
    @Update("UPDATE feedback SET status = #{status} WHERE id = #{feedbackId}")
    int updateStatus(@Param("feedbackId") Long feedbackId, @Param("status") String status);
}