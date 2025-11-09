package com.carwash.service.payment;

import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import com.carwash.service.payment.virtual.VirtualPaymentSDK;
import com.carwash.service.payment.validation.PaymentDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 虚拟支付网关实现（开发/测试用）
 * - 统一遵循 PaymentGateway 接口
 * - 支持 qr/app/h5 支付渠道
 */
@Service
public class VirtualPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(VirtualPaymentGateway.class);

    @Autowired
    private VirtualPaymentSDK sdk;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            PaymentDataValidator.validate(request);
            String channel = request.getChannel() == null ? "qr" : request.getChannel();
            var r = sdk.create(request.getOrderNo(), channel);

            PaymentResponse resp = new PaymentResponse();
            resp.setPaymentNo(r.paymentNo);
            resp.setStatus(r.status);
            resp.setQrCode(r.qrCode);
            resp.setPaymentUrl(r.payUrl);
            resp.setMessage(r.message);
            return resp;
        } catch (Exception e) {
            log.error("创建虚拟支付失败", e);
            PaymentResponse resp = new PaymentResponse();
            resp.setStatus("failed");
            resp.setMessage("虚拟支付创建失败: " + e.getMessage());
            return resp;
        }
    }

    @Override
    public PaymentResponse queryPayment(String paymentNo) {
        var r = sdk.query(paymentNo);
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentNo(paymentNo);
        resp.setStatus(r.status);
        resp.setMessage(r.message);
        resp.setTransactionId(r.transactionId);
        return resp;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        var r = sdk.refund(request.getPaymentNo(), request.getAmount().toString(), request.getReason());
        RefundResponse resp = new RefundResponse();
        resp.setSuccess(r.success);
        resp.setRefundNo(r.refundNo);
        resp.setStatus(r.status);
        resp.setMessage(r.message);
        return resp;
    }

    @Override
    public RefundResponse queryRefund(String refundNo) {
        var r = sdk.queryRefund(refundNo);
        RefundResponse resp = new RefundResponse();
        resp.setSuccess(r.success);
        resp.setRefundNo(r.refundNo);
        resp.setStatus(r.status);
        resp.setMessage(r.message);
        return resp;
    }

    @Override
    public boolean handleCallback(String callbackData) {
        return sdk.handleCallback(callbackData);
    }

    @Override
    public String getPaymentMethod() {
        return "virtual";
    }
}