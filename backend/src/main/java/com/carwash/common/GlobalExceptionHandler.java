package com.carwash.common;

import com.carwash.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.warn("认证异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(401, "认证失败：" + e.getMessage());
    }

    /**
     * 处理凭证错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        logger.warn("凭证错误: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("权限不足: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(403, "权限不足，无法访问该资源");
    }

    /**
     * 处理参数校验异常 - @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.warn("参数校验异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
        }
        
        return Result.error(400, "参数校验失败：" + errorMsg.toString());
    }

    /**
     * 处理参数校验异常 - @ModelAttribute
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e, HttpServletRequest request) {
        logger.warn("参数绑定异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
        }
        
        return Result.error(400, "参数绑定失败：" + errorMsg.toString());
    }

    /**
     * 处理参数校验异常 - @RequestParam
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        logger.warn("约束违反异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        StringBuilder errorMsg = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
        }
        
        return Result.error(400, "参数校验失败：" + errorMsg.toString());
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "参数错误：" + e.getMessage());
    }

    /**
     * 处理NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}