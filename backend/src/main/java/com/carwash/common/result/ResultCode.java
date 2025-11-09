package com.carwash.common.result;

/**
 * 响应状态码枚举
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public enum ResultCode {

    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 参数相关
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400, "缺少必要参数"),
    PARAM_INVALID(400, "参数格式不正确"),
    
    // 认证相关
    UNAUTHORIZED(401, "未授权访问"),
    TOKEN_INVALID(401, "Token无效"),
    TOKEN_EXPIRED(401, "Token已过期"),
    LOGIN_REQUIRED(401, "请先登录"),
    
    // 权限相关
    FORBIDDEN(403, "权限不足"),
    ACCESS_DENIED(403, "访问被拒绝"),
    
    // 资源相关
    NOT_FOUND(404, "资源不存在"),
    RESOURCE_NOT_FOUND(404, "请求的资源不存在"),
    
    // 业务相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PHONE_ALREADY_EXISTS(1003, "手机号已存在"),
    EMAIL_ALREADY_EXISTS(1004, "邮箱已存在"),
    PASSWORD_ERROR(1005, "密码错误"),
    OLD_PASSWORD_ERROR(1006, "原密码错误"),
    LOGIN_FAILED(1007, "登录失败"),
    ACCOUNT_DISABLED(1008, "账户已被禁用"),
    ACCOUNT_LOCKED(1009, "账户已被锁定"),
    
    SERVICE_NOT_FOUND(2001, "服务项目不存在"),
    SERVICE_UNAVAILABLE(2002, "服务项目不可用"),
    
    APPOINTMENT_NOT_FOUND(3001, "预约不存在"),
    APPOINTMENT_CONFLICT(3002, "预约时间冲突"),
    APPOINTMENT_EXPIRED(3003, "预约已过期"),
    APPOINTMENT_CANCELLED(3004, "预约已取消"),
    
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(4003, "订单无法取消"),
    INVALID_STATUS_TRANSITION(4004, "无效的状态转换"),
    ORDER_ALREADY_PAID(4005, "订单已支付"),
    
    // 支付相关
    PAYMENT_NOT_FOUND(5001, "支付记录不存在"),
    PAYMENT_STATUS_ERROR(5002, "支付状态错误"),
    PAYMENT_AMOUNT_ERROR(5003, "支付金额错误"),
    PAYMENT_FAILED(5004, "支付失败"),
    REFUND_AMOUNT_ERROR(5005, "退款金额错误"),
    REFUND_FAILED(5006, "退款失败"),
    PERMISSION_DENIED(5007, "权限不足"),
    
    // 系统相关
    SYSTEM_ERROR(6000, "系统内部错误"),
    DATABASE_ERROR(6001, "数据库操作失败"),
    NETWORK_ERROR(6002, "网络连接异常"),
    FILE_UPLOAD_ERROR(6003, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(6004, "文件下载失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}