package com.carwash.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
public class UserInfoResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}