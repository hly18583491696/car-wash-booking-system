package com.carwash.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 */
@Data
public class RefundRequest {

    /**
     * 支付流水号
     */
    @NotBlank(message = "支付流水号不能为空")
    private String paymentNo;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal amount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    /**
     * 操作员ID
     */
    private Long operatorId;

    // Getter methods
    public String getPaymentNo() {
        return paymentNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    // Setter methods
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}