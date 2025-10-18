package com.carwash.common;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 创建业务异常
     */
    public static BusinessException create(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 创建业务异常
     */
    public static BusinessException create(Integer code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 根据ResultCode创建业务异常
     */
    public BusinessException(com.carwash.common.result.ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 根据ResultCode和自定义消息创建业务异常
     */
    public BusinessException(com.carwash.common.result.ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
    }
}