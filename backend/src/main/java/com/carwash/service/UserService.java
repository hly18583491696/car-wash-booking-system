package com.carwash.service;

import com.carwash.dto.LoginRequest;
import com.carwash.dto.LoginResponse;
import com.carwash.dto.RegisterRequest;
import com.carwash.dto.UserInfoResponse;
import com.carwash.entity.User;

/**
 * 用户服务接口
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String clientIp);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据用户名或手机号查询用户
     */
    User findByUsernameOrPhone(String identifier);

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserInfoResponse userInfo);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}