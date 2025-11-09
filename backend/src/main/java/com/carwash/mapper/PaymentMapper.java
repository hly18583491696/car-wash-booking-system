package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 根据订单号查询支付记录
     */
    Payment selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据支付流水号查询支付记录
     */
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据第三方交易号查询支付记录
     */
    Payment selectByTransactionId(@Param("transactionId") String transactionId);

    /**
     * 查询用户的支付记录
     */
    List<Payment> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询过期未支付的订单
     */
    List<Payment> selectExpiredPayments(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 统计用户支付总金额
     */
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * 统计指定时间范围内的支付金额
     */
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
}