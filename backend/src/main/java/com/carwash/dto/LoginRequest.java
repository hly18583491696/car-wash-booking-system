package com.carwash.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class LoginRequest {

    /**
     * 用户名或手机号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;
}