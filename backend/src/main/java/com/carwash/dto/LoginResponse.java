package com.carwash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT访问令牌
     */
    private String token;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 用户信息
     */
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private String email;
        private String role;
        private String avatar;
    }

    public LoginResponse(String token, Long userId, String username, String realName, String phone, String email, String role, String avatar) {
        this.token = token;
        this.user = new UserInfo(userId, username, realName, phone, email, role, avatar);
    }

    // 兼容旧的构造函数
    public LoginResponse(String token, Long userId, String username, String realName, String role, String avatar) {
        this.token = token;
        this.user = new UserInfo(userId, username, realName, null, null, role, avatar);
    }
}