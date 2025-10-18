package com.carwash.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功响应，自定义消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 失败响应，自定义消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 失败响应，自定义码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败响应，自定义码、消息和数据
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 根据结果码创建响应
     */
    public static <T> Result<T> result(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 根据结果码创建响应，带数据
     */
    public static <T> Result<T> result(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    // Getter and Setter methods
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}