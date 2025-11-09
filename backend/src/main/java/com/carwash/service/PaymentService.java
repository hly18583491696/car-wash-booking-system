package com.carwash.service;

import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.entity.Payment;
import com.carwash.entity.PaymentAudit;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付订单
     */
    PaymentResponse createPayment(PaymentRequest request, Long userId);

    /**
     * 查询支付状态
     */
    PaymentResponse queryPaymentStatus(String paymentNo);

    /**
     * 处理支付回调
     */
    boolean handlePaymentCallback(String paymentMethod, Map<String, String> params);

    /**
     * 处理退款
     */
    PaymentResponse processRefund(RefundRequest request);

    /**
     * 根据订单号查询支付记录
     */
    Payment getPaymentByOrderNo(String orderNo);

    /**
     * 根据支付流水号查询支付记录
     */
    Payment getPaymentByPaymentNo(String paymentNo);

    /**
     * 查询用户的支付记录
     */
    List<Payment> getUserPayments(Long userId);

    /**
     * 取消过期支付订单
     */
    void cancelExpiredPayments();

    /**
     * 验证支付回调签名
     */
    boolean verifyPaymentCallback(String paymentMethod, Map<String, String> params);

    /**
     * 获取所有支付记录（管理员）
     */
    List<Payment> getAllPayments();

    /**
     * 分页查询支付审计日志（管理员）
     * @param current 当前页，默认1
     * @param size 每页大小，默认10
     * @param paymentNo 支付流水号（可选）
     * @param orderNo 订单号（可选）
     * @param eventType 事件类型（可选）
     * @param status 状态（可选）
     * @return 审计日志分页结果
     */
    IPage<PaymentAudit> getPaymentAudits(Long current, Long size,
                                         String paymentNo,
                                         String orderNo,
                                         String eventType,
                                         String status);
}