package com.carwash.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carwash.common.BusinessException;
import com.carwash.common.result.ResultCode;
import com.carwash.dto.LoginRequest;
import com.carwash.dto.LoginResponse;
import com.carwash.dto.RegisterRequest;
import com.carwash.dto.UserInfoResponse;
import com.carwash.entity.User;
import com.carwash.mapper.UserMapper;
import com.carwash.service.SmsService;
import com.carwash.service.UserService;
import com.carwash.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SmsService smsService;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("用户注册开始，用户名: {}", request.getUsername());

        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 验证短信验证码
        if (!smsService.verifyCode(request.getPhone(), request.getSmsCode(), "register")) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查手机号是否已存在
        if (existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS, "手机号已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty() && existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS, "邮箱已存在");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 加密密码
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus(1);

        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "用户注册失败");
        }

        log.info("用户注册成功，用户ID: {}", user.getId());
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        log.info("用户登录开始，用户名: {}", request.getUsername());

        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 获取用户信息
            User user = findByUsernameOrPhone(request.getUsername());
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 更新最后登录信息 (暂时注释掉，因为表中没有相关字段)
            // userMapper.updateLastLoginInfo(user.getId(), LocalDateTime.now(), clientIp);

            // 构建响应
            LoginResponse response = new LoginResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar()
            );

            log.info("用户登录成功，用户ID: {}", user.getId());
            return response;

        } catch (Exception e) {
            log.error("用户登录失败，用户名: {}, 错误: {}", request.getUsername(), e.getMessage());
            throw new BusinessException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
        }
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("username", username)
                .eq("deleted", 0)
        );
    }

    @Override
    public User findByUsernameOrPhone(String identifier) {
        return userMapper.findByUsernameOrPhone(identifier);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserInfoResponse userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        user.setRealName(userInfo.getRealName());
        user.setPhone(userInfo.getPhone());
        user.setEmail(userInfo.getEmail());
        user.setAvatar(userInfo.getAvatar());

        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新用户信息失败");
        }

        log.info("用户信息更新成功，用户ID: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR, "原密码错误");
        }

        // 更新密码
        user.setPassword(newPassword); // 使用明文密码
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "修改密码失败");
        }

        log.info("用户密码修改成功，用户ID: {}", userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.countByPhone(phone) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }

    @Override
    public List<UserInfoResponse> getAllUsers() {
        List<User> users = userMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("deleted", 0)
                .orderByDesc("created_at")
        );

        return users.stream().map(user -> {
            UserInfoResponse response = new UserInfoResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        user.setStatus(status);
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "更新用户状态失败");
        }

        log.info("用户状态更新成功，用户ID: {}, 新状态: {}", userId, status);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 软删除
        user.setDeleted(1);
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "删除用户失败");
        }

        log.info("用户删除成功，用户ID: {}", userId);
    }

    @Override
    public long getUserCount() {
        try {
            return userMapper.selectCount(null);
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            return 0;
        }
    }

    @Override
    public long getActiveUserCount() {
        try {
            // 查询未删除的用户数量
            return userMapper.selectCount(
                new QueryWrapper<User>().eq("deleted", 0)
            );
        } catch (Exception e) {
            log.error("获取活跃用户数失败", e);
            return 0;
        }
    }

    @Override
    public long getAdminUserCount() {
        try {
            // 查询管理员用户数量
            return userMapper.selectCount(
                new QueryWrapper<User>()
                    .eq("role", "admin")
                    .eq("deleted", 0)
            );
        } catch (Exception e) {
            log.error("获取管理员用户数失败", e);
            return 0;
        }
    }
}