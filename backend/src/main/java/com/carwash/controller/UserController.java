package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.UserInfoResponse;
import com.carwash.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<UserInfoResponse> getUserInfo() {
        Long userId = getCurrentUserId();
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Void> updateUserInfo(@Valid @RequestBody UserInfoResponse userInfo) {
        Long userId = getCurrentUserId();
        userService.updateUserInfo(userId, userInfo);
        return Result.success("用户信息更新成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Result<Void> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Long userId = getCurrentUserId();
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 根据用户名查询用户ID
            return userService.findByUsernameOrPhone(username).getId();
        }
        throw new RuntimeException("无法获取当前用户信息");
    }
}