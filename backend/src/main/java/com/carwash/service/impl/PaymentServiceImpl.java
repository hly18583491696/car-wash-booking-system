package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import com.carwash.entity.Booking;
import com.carwash.entity.Payment;
import com.carwash.entity.Refund;
import com.carwash.entity.PaymentAudit;
import com.carwash.mapper.BookingMapper;
import com.carwash.mapper.PaymentMapper;
import com.carwash.mapper.RefundMapper;
import com.carwash.mapper.PaymentAuditMapper;
import com.carwash.service.PaymentService;
import com.carwash.service.payment.PaymentGateway;
import com.carwash.service.payment.PaymentGatewayFactory;
import com.carwash.service.payment.security.CallbackSignatureVerifier;
import com.carwash.utils.TimeUtils;
import com.carwash.monitor.PaymentMonitor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    @Autowired(required = false)
    private PaymentMonitor paymentMonitor;

    @Autowired
    private CallbackSignatureVerifier signatureVerifier;

    @Autowired(required = false)
    private PaymentAuditMapper paymentAuditMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse createPayment(PaymentRequest request, Long userId) {
        log.info("创建支付订单开始，订单号: {}, 用户ID: {}", request.getOrderNo(), userId);

        // 验证订单是否存在
        Booking booking = bookingMapper.selectByOrderNo(request.getOrderNo());
        if (booking == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "无权限操作此订单");
        }

        // 验证订单状态
        if (!"confirmed".equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单状态不允许支付");
        }

        // 验证支付状态
        if ("paid".equals(booking.getPaymentStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID, "订单已支付");
        }

        // 检查是否已有待支付的支付记录
        Payment existingPayment = paymentMapper.selectByOrderNo(request.getOrderNo());
        if (existingPayment != null && "pending".equals(existingPayment.getStatus())) {
            // 返回已存在的支付信息
            return buildPaymentResponse(existingPayment);
        }

        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderNo(request.getOrderNo());
        payment.setPaymentNo(generatePaymentNo());
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("pending");
        payment.setDescription(request.getDescription());
        payment.setExpireAt(TimeUtils.now().plusMinutes(30)); // 30分钟过期
        payment.setNotifyCount(0);

        int result = paymentMapper.insert(payment);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建支付订单失败");
        }

        // 审计：创建支付记录
        savePaymentAudit(payment, "CREATE", payment.getStatus(), "创建支付记录", null, userId, request.getAmount());

        // 根据支付方式调用相应的支付接口
        PaymentResponse gatewayResp = callPaymentApi(payment, request);

        // 审计：调用网关
        String raw = gatewayResp != null ? ("payUrl=" + gatewayResp.getPayUrl() + ", qrCode=" + gatewayResp.getQrCode()) : null;
        savePaymentAudit(payment, "CALL_GATEWAY", gatewayResp != null ? gatewayResp.getStatus() : null, "调用支付网关", raw, userId, request.getAmount());

        // 构建统一响应（确保返回真实的paymentNo等信息）
        PaymentResponse response = buildPaymentResponse(payment);
        if (gatewayResp != null) {
            response.setQrCode(gatewayResp.getQrCode());
            response.setPaymentUrl(gatewayResp.getPayUrl());
            response.setStatus(gatewayResp.getStatus() != null ? gatewayResp.getStatus() : response.getStatus());
            response.setMessage(gatewayResp.getErrorMessage());
        }

        log.info("创建支付订单成功，支付流水号: {}", payment.getPaymentNo());
        if (paymentMonitor != null) {
            paymentMonitor.markCreate();
        }
        return response;
    }

    @Override
    public PaymentResponse queryPaymentStatus(String paymentNo) {
        log.info("查询支付状态，支付流水号: {}", paymentNo);

        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND, "支付记录不存在");
        }

        // 如果是待支付状态，查询第三方支付平台状态
        if ("pending".equals(payment.getStatus())) {
            // 审计：查询第三方状态
            savePaymentAudit(payment, "QUERY_STATUS", payment.getStatus(), "查询第三方支付状态", null, null, null);
            queryThirdPartyPaymentStatus(payment);
        }

        PaymentResponse resp = buildPaymentResponse(payment);
        if (paymentMonitor != null) {
            paymentMonitor.markQuery();
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String paymentMethod, Map<String, String> params) {
        log.info("处理支付回调，支付方式: {}", paymentMethod);

        // 验证回调签名
        if (!verifyPaymentCallback(paymentMethod, params)) {
            log.error("支付回调签名验证失败");
            return false;
        }

        // 审计：回调验签通过
        String verifyRaw = params != null ? params.toString() : null;
        PaymentAudit verifyAudit = new PaymentAudit();
        verifyAudit.setEventType("CALLBACK_VERIFIED");
        verifyAudit.setStatus("verified");
        verifyAudit.setPaymentMethod(paymentMethod);
        verifyAudit.setRawData(verifyRaw);

        String transactionId = params.get("transaction_id");
        String paymentNo = params.get("out_trade_no");

        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            log.error("支付记录不存在，支付流水号: {}", paymentNo);
            return false;
        }

        // 审计：记录验签审计的上下文（补充支付/订单号）
        if (paymentAuditMapper != null) {
            verifyAudit.setPaymentNo(payment.getPaymentNo());
            verifyAudit.setOrderNo(payment.getOrderNo());
            verifyAudit.setCreatedAt(TimeUtils.now());
            paymentAuditMapper.insert(verifyAudit);
        }

        // 更新支付状态
        payment.setStatus("paid");
        payment.setTransactionId(transactionId);
        payment.setPaidAt(TimeUtils.now());
        payment.setRawData(params.toString());

        paymentMapper.updateById(payment);

        // 审计：回调更新成功
        savePaymentAudit(payment, "CALLBACK_UPDATE", payment.getStatus(), "回调更新支付状态为paid", params != null ? params.toString() : null, null, payment.getAmount());

        // 更新订单支付状态
        Booking booking = bookingMapper.selectByOrderNo(payment.getOrderNo());
        if (booking != null) {
            booking.setPaymentStatus("paid");
            booking.setPaymentMethod(payment.getPaymentMethod());
            booking.setPaidAt(payment.getPaidAt());
            bookingMapper.updateById(booking);
        }

        log.info("支付回调处理成功，支付流水号: {}", paymentNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse processRefund(RefundRequest request) {
        log.info("处理退款请求，支付流水号: {}", request.getPaymentNo());

        Payment payment = paymentMapper.selectByPaymentNo(request.getPaymentNo());
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND, "支付记录不存在");
        }

        if (!"paid".equals(payment.getStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_ERROR, "支付状态不允许退款");
        }

        // 检查退款金额
        BigDecimal totalRefunded = refundMapper.sumRefundAmountByPaymentId(payment.getId());
        if (totalRefunded == null) {
            totalRefunded = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = payment.getAmount().subtract(totalRefunded);
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException(ResultCode.REFUND_AMOUNT_ERROR, "退款金额超过可退款金额");
        }

        // 创建退款记录
        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setRefundNo(generateRefundNo());
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setStatus("pending");
        refund.setOperatorId(request.getOperatorId());

        refundMapper.insert(refund);

        // 审计：发起退款请求
        savePaymentAudit(payment, "REFUND_REQUEST", refund.getStatus(), "发起退款请求", null, request.getOperatorId(), request.getAmount());

        // 调用第三方退款接口
        boolean refundSuccess = callRefundApi(payment, refund);

        if (refundSuccess) {
            refund.setStatus("success");
            refund.setRefundedAt(TimeUtils.now());
            refundMapper.updateById(refund);

            // 审计：退款成功
            savePaymentAudit(payment, "REFUND_SUCCESS", refund.getStatus(), "退款成功", null, request.getOperatorId(), request.getAmount());

            // 如果全额退款，更新订单状态
            if (request.getAmount().compareTo(payment.getAmount()) == 0) {
                Booking booking = bookingMapper.selectByOrderNo(payment.getOrderNo());
                if (booking != null) {
                    booking.setPaymentStatus("refunded");
                    bookingMapper.updateById(booking);
                }
            }
        } else {
            refund.setStatus("failed");
            refundMapper.updateById(refund);

            // 审计：退款失败
            savePaymentAudit(payment, "REFUND_FAILED", refund.getStatus(), "退款失败", null, request.getOperatorId(), request.getAmount());
        }

        log.info("退款处理完成，退款单号: {}, 状态: {}", refund.getRefundNo(), refund.getStatus());
        if (paymentMonitor != null) {
            paymentMonitor.markRefund();
            if (!"success".equals(refund.getStatus())) {
                paymentMonitor.markFailure();
            }
        }

        return PaymentResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .status(refund.getStatus())
                .build();
    }

    @Override
    public Payment getPaymentByOrderNo(String orderNo) {
        return paymentMapper.selectByOrderNo(orderNo);
    }

    @Override
    public Payment getPaymentByPaymentNo(String paymentNo) {
        return paymentMapper.selectByPaymentNo(paymentNo);
    }

    @Override
    public List<Payment> getUserPayments(Long userId) {
        return paymentMapper.selectByUserId(userId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredPayments() {
        log.info("取消过期支付订单");

        List<Payment> expiredPayments = paymentMapper.selectExpiredPayments(TimeUtils.now());
        for (Payment payment : expiredPayments) {
            payment.setStatus("cancelled");
            paymentMapper.updateById(payment);

            // 审计：取消过期支付
            savePaymentAudit(payment, "CANCEL_EXPIRED", payment.getStatus(), "支付超时自动取消", null, null, payment.getAmount());

            // 更新订单状态
            Booking booking = bookingMapper.selectByOrderNo(payment.getOrderNo());
            if (booking != null && "unpaid".equals(booking.getPaymentStatus())) {
                booking.setStatus("cancelled");
                booking.setCancelReason("支付超时自动取消");
                booking.setCancelledAt(TimeUtils.now());
                bookingMapper.updateById(booking);
            }
        }

        log.info("取消过期支付订单完成，共取消 {} 个订单", expiredPayments.size());
    }

    @Override
    public boolean verifyPaymentCallback(String paymentMethod, Map<String, String> params) {
        log.info("验证支付回调签名，支付方式: {}", paymentMethod);

        try {
            // 优先使用统一验签组件
            if (signatureVerifier != null) {
                boolean verified = signatureVerifier.verify(paymentMethod, params);
                if (!verified) {
                    log.warn("统一验签失败，paymentMethod={}, params={}", paymentMethod, params);
                    return false;
                }
                return true;
            }

            // 兜底：回退到网关内部处理（不建议长期使用）
            PaymentGateway gateway = paymentGatewayFactory.getGateway(paymentMethod);
            String callbackData = params.toString();
            return gateway.handleCallback(callbackData);
        } catch (Exception e) {
            log.error("验证回调签名失败", e);
            return false;
        }
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 调用第三方支付接口
     */
    private PaymentResponse callPaymentApi(Payment payment, PaymentRequest request) {
        log.info("调用第三方支付接口，支付方式: {}", payment.getPaymentMethod());

        try {
            PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
            return gateway.createPayment(request);
        } catch (Exception e) {
            log.error("调用支付接口失败", e);
            if (paymentMonitor != null) {
                paymentMonitor.markFailure();
            }
            throw new BusinessException(ResultCode.PAYMENT_FAILED, "支付接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 查询第三方支付平台状态
     */
    private void queryThirdPartyPaymentStatus(Payment payment) {
        log.info("查询第三方支付平台状态，支付流水号: {}", payment.getPaymentNo());

        try {
            PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
            PaymentResponse response = gateway.queryPayment(payment.getPaymentNo());
            
            // 如果状态发生变化，更新数据库
            if (!payment.getStatus().equals(response.getStatus())) {
                payment.setStatus(response.getStatus());
                if ("paid".equals(response.getStatus())) {
                    payment.setPaidAt(TimeUtils.now());
                    payment.setTransactionId(response.getTransactionId());
                    
                    // 更新订单支付状态
                    Booking booking = bookingMapper.selectByOrderNo(payment.getOrderNo());
                    if (booking != null) {
                        booking.setPaymentStatus("paid");
                        booking.setPaymentMethod(payment.getPaymentMethod());
                        booking.setPaidAt(payment.getPaidAt());
                        bookingMapper.updateById(booking);
                    }
                }
                paymentMapper.updateById(payment);

                // 审计：状态变化
                savePaymentAudit(payment, "STATUS_CHANGE", payment.getStatus(), "第三方查询导致状态变化", "transactionId=" + response.getTransactionId(), null, payment.getAmount());
            }
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            if (paymentMonitor != null) {
                paymentMonitor.markFailure();
            }
        }
    }

    /**
     * 保存支付审计记录（可选依赖，Mapper缺失时忽略）
     */
    private void savePaymentAudit(Payment payment, String eventType, String status, String message, String rawData, Long operatorId, BigDecimal amount) {
        try {
            if (paymentAuditMapper == null || payment == null) {
                return;
            }
            PaymentAudit audit = new PaymentAudit();
            audit.setPaymentNo(payment.getPaymentNo());
            audit.setOrderNo(payment.getOrderNo());
            audit.setEventType(eventType);
            audit.setStatus(status);
            audit.setPaymentMethod(payment.getPaymentMethod());
            audit.setAmount(amount);
            audit.setOperatorId(operatorId);
            audit.setMessage(message);
            audit.setRawData(rawData);
            audit.setCreatedAt(TimeUtils.now());
            paymentAuditMapper.insert(audit);
        } catch (Exception ex) {
            log.warn("保存支付审计失败: {}", ex.getMessage());
        }
    }

    /**
     * 调用第三方退款接口
     */
    private boolean callRefundApi(Payment payment, Refund refund) {
        log.info("调用第三方退款接口，退款单号: {}", refund.getRefundNo());

        try {
            PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
            
            // 构建退款请求对象
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPaymentNo(payment.getPaymentNo());
            refundRequest.setAmount(refund.getAmount());
            refundRequest.setReason(refund.getReason());
            refundRequest.setOperatorId(refund.getOperatorId());
            
            RefundResponse response = gateway.refund(refundRequest);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("调用退款接口失败", e);
            if (paymentMonitor != null) {
                paymentMonitor.markFailure();
            }
            return false;
        }
    }

    /**
     * 构建支付响应对象
     */
    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(payment.getOrderNo())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .expireAt(payment.getExpireAt())
                .paidAt(payment.getPaidAt())
                .transactionId(payment.getTransactionId())
                .build();
    }

    @Override
    public IPage<PaymentAudit> getPaymentAudits(Long current, Long size,
                                                String paymentNo,
                                                String orderNo,
                                                String eventType,
                                                String status) {
        if (current == null || current <= 0) current = 1L;
        if (size == null || size <= 0) size = 10L;

        Page<PaymentAudit> page = new Page<>(current, size);

        QueryWrapper<PaymentAudit> wrapper = new QueryWrapper<>();
        if (paymentNo != null && !paymentNo.trim().isEmpty()) {
            wrapper.like("payment_no", paymentNo.trim());
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like("order_no", orderNo.trim());
        }
        if (eventType != null && !eventType.trim().isEmpty()) {
            wrapper.eq("event_type", eventType.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status.trim());
        }
        wrapper.orderByDesc("created_at");

        return paymentAuditMapper.selectPage(page, wrapper);
    }
}