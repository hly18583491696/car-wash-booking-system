package com.carwash.dto;

import lombok.Data;

/**
 * 退款响应DTO
 */
@Data
public class RefundResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 退款状态
     */
    private String status;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    // Setter methods
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    // Getter methods
    public boolean isSuccess() {
        return success;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}