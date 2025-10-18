package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.LoginRequest;
import com.carwash.dto.LoginResponse;
import com.carwash.dto.RegisterRequest;
import com.carwash.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户注册、登录等认证相关接口")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求，用户名: {}", request.getUsername());
        userService.register(request);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("用户登录请求，用户名: {}", request.getUsername());
        
        // 获取客户端IP
        String clientIp = getClientIp(httpRequest);
        
        LoginResponse response = userService.login(request, clientIp);
        return Result.success("登录成功", response);
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(!exists);
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已被使用")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return Result.success(!exists);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被使用")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(!exists);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xfor = request.getHeader("X-Forwarded-For");
        
        if (xfor != null && !xfor.isEmpty() && !"unknown".equalsIgnoreCase(xfor)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        
        xfor = xip;
        if (xfor != null && !xfor.isEmpty() && !"unknown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("Proxy-Client-IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (xfor == null || xfor.isEmpty() || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getRemoteAddr();
        }
        
        return xfor;
    }
}