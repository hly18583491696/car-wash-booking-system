package com.carwash.service.payment;

import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;

/**
 * 支付网关接口
 * 定义统一的支付方法，支持多种支付方式
 */
public interface PaymentGateway {
    
    /**
     * 创建支付订单
     * @param request 支付请求
     * @return 支付响应
     */
    PaymentResponse createPayment(PaymentRequest request);
    
    /**
     * 查询支付状态
     * @param paymentNo 支付单号
     * @return 支付响应
     */
    PaymentResponse queryPayment(String paymentNo);
    
    /**
     * 申请退款
     * @param request 退款请求
     * @return 退款响应
     */
    RefundResponse refund(RefundRequest request);
    
    /**
     * 查询退款状态
     * @param refundNo 退款单号
     * @return 退款响应
     */
    RefundResponse queryRefund(String refundNo);
    
    /**
     * 处理支付回调
     * @param callbackData 回调数据
     * @return 处理结果
     */
    boolean handleCallback(String callbackData);
    
    /**
     * 获取支付方式
     * @return 支付方式标识
     */
    String getPaymentMethod();
}