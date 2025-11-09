package com.carwash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 支付二维码（用于扫码支付）
     */
    private String qrCode;

    /**
     * 支付链接（用于H5支付）
     */
    private String payUrl;

    /**
     * 支付过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 第三方支付平台交易号
     */
    private String transactionId;

    /**
     * 错误信息
     */
    private String errorMessage;

    public String getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setSuccess(boolean success) {
        // This method is for compatibility, maps to status
        this.status = success ? "SUCCESS" : "FAILED";
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.payUrl = paymentUrl;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.errorMessage = message;
    }

    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    public static class PaymentResponseBuilder {
        private String paymentNo;
        private String orderNo;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private String qrCode;
        private String payUrl;
        private LocalDateTime expireAt;
        private LocalDateTime paidAt;
        private String transactionId;
        private String errorMessage;

        public PaymentResponseBuilder paymentNo(String paymentNo) {
            this.paymentNo = paymentNo;
            return this;
        }

        public PaymentResponseBuilder orderNo(String orderNo) {
            this.orderNo = orderNo;
            return this;
        }

        public PaymentResponseBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public PaymentResponseBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentResponseBuilder qrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public PaymentResponseBuilder payUrl(String payUrl) {
            this.payUrl = payUrl;
            return this;
        }

        public PaymentResponseBuilder expireAt(LocalDateTime expireAt) {
            this.expireAt = expireAt;
            return this;
        }

        public PaymentResponseBuilder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public PaymentResponseBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PaymentResponse build() {
            PaymentResponse response = new PaymentResponse();
            response.paymentNo = this.paymentNo;
            response.orderNo = this.orderNo;
            response.amount = this.amount;
            response.paymentMethod = this.paymentMethod;
            response.status = this.status;
            response.qrCode = this.qrCode;
            response.payUrl = this.payUrl;
            response.expireAt = this.expireAt;
            response.paidAt = this.paidAt;
            response.transactionId = this.transactionId;
            response.errorMessage = this.errorMessage;
            return response;
        }
    }
}