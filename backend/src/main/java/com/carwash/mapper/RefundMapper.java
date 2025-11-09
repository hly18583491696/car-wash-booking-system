package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款记录Mapper接口
 */
@Mapper
public interface RefundMapper extends BaseMapper<Refund> {

    /**
     * 根据退款单号查询退款记录
     */
    Refund selectByRefundNo(@Param("refundNo") String refundNo);

    /**
     * 根据支付记录ID查询退款记录
     */
    List<Refund> selectByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * 根据第三方退款单号查询退款记录
     */
    Refund selectByRefundId(@Param("refundId") String refundId);

    /**
     * 统计支付记录的退款总金额
     */
    BigDecimal sumRefundAmountByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * 统计指定时间范围内的退款金额
     */
    BigDecimal sumRefundAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}