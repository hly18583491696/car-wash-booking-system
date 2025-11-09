package com.carwash.service.payment.validation;

import com.carwash.dto.PaymentRequest;

/**
 * 支付数据校验与转换层
 * - 针对不同支付方式/渠道进行前置校验
 * - 可扩展转换为网关所需的专用参数结构
 */
public class PaymentDataValidator {

    public static void validate(PaymentRequest req) {
        if (req.getOrderNo() == null || req.getOrderNo().trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (req.getAmount() == null || req.getAmount().doubleValue() <= 0d) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        if (req.getPaymentMethod() == null || req.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("支付方式不能为空");
        }

        String method = req.getPaymentMethod();
        String channel = req.getChannel() == null ? "qr" : req.getChannel();

        // 信用卡必需安全载荷
        if ("credit_card".equals(method)) {
            if (req.getSecurePayload() == null || req.getSecurePayload().isEmpty()) {
                throw new IllegalArgumentException("信用卡支付缺少安全载荷");
            }
        }

        // 非信用卡的渠道校验
        if (!"credit_card".equals(method)) {
            if (!("qr".equals(channel) || "app".equals(channel) || "h5".equals(channel))) {
                throw new IllegalArgumentException("非法支付渠道: " + channel);
            }
        }
    }
}