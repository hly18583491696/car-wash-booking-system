package com.carwash.service.payment.virtual;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虚拟支付SDK（开发/测试用）
 * - 支持扫码（qr）、APP（app）、H5（h5）三种支付场景
 * - 提供创建、查询、退款、查询退款等模拟能力
 * - 内部维护简单的内存状态，便于测试端到端流程
 */
@Component
public class VirtualPaymentSDK {

    public static class CreateResult {
        public String paymentNo;
        public String channel; // qr/app/h5
        public String qrCode;  // qr 场景
        public String payUrl;  // h5或app 场景
        public String status;  // 初始: pending
        public String message;
    }

    public static class QueryResult {
        public String paymentNo;
        public String status; // pending/paid/cancelled
        public String transactionId;
        public String message;
    }

    public static class RefundResult {
        public boolean success;
        public String refundNo;
        public String status; // success/failed/pending
        public String message;
    }

    private final Map<String, String> payments = new ConcurrentHashMap<>(); // paymentNo -> status

    /**
     * 创建支付，返回渠道对应的模拟参数
     */
    public CreateResult create(String orderNo, String channel) {
        CreateResult r = new CreateResult();
        r.paymentNo = generatePaymentNo(orderNo);
        r.channel = channel;
        r.status = "pending";
        switch (channel) {
            case "qr":
                r.qrCode = "virtual://qrcode/" + UUID.randomUUID();
                r.payUrl = null;
                r.message = "虚拟扫码支付创建成功";
                break;
            case "app":
                // 使用深链或Scheme模拟APP拉起
                r.payUrl = "carwashpay://pay?order=" + urlEncode(orderNo) + "&ts=" + System.currentTimeMillis();
                r.qrCode = null;
                r.message = "虚拟APP支付创建成功";
                break;
            case "h5":
            default:
                r.payUrl = "https://virtual-pay.example/h5/checkout?order=" + urlEncode(orderNo);
                r.qrCode = null;
                r.message = "虚拟H5支付创建成功";
                break;
        }
        payments.put(r.paymentNo, r.status);
        return r;
    }

    /**
     * 查询支付状态（简单模拟：创建后查询即视为已支付）
     */
    public QueryResult query(String paymentNo) {
        QueryResult r = new QueryResult();
        r.paymentNo = paymentNo;
        String current = payments.getOrDefault(paymentNo, "pending");
        if ("pending".equals(current)) {
            // 模拟支付完成
            current = "paid";
            payments.put(paymentNo, current);
        }
        r.status = current;
        r.transactionId = "VTX" + System.currentTimeMillis();
        r.message = "查询成功";
        return r;
    }

    /**
     * 申请退款（总是成功）
     */
    public RefundResult refund(String paymentNo, String amount, String reason) {
        RefundResult r = new RefundResult();
        r.success = true;
        r.status = "success";
        r.refundNo = generateRefundNo();
        r.message = "虚拟退款受理成功";
        return r;
    }

    /**
     * 查询退款结果（总是成功）
     */
    public RefundResult queryRefund(String refundNo) {
        RefundResult r = new RefundResult();
        r.success = true;
        r.status = "success";
        r.refundNo = refundNo;
        r.message = "查询成功";
        return r;
    }

    /**
     * 验证并解析虚拟回调（演示返回true）
     */
    public boolean handleCallback(String payload) {
        return true;
    }

    private String generatePaymentNo(String orderNo) {
        return "VP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + orderNo.hashCode();
    }

    private String generateRefundNo() {
        return "VRF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", new Random().nextInt(1_000_000));
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}