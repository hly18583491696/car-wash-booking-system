package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.dto.UserInfoResponse;
import com.carwash.service.UserService;
import com.carwash.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
     * 获取所有用户列表（管理员专用）
     */
    @GetMapping("/admin/list")
    @Operation(summary = "获取用户列表", description = "管理员获取所有用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserInfoResponse>> getAllUsers() {
        List<UserInfoResponse> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 更新用户状态（管理员专用）
     */
    @PutMapping("/admin/{userId}/status")
    @Operation(summary = "更新用户状态", description = "管理员更新用户状态")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success("用户状态更新成功");
    }

    /**
     * 删除用户（管理员专用）
     */
    @DeleteMapping("/admin/{userId}")
    @Operation(summary = "删除用户", description = "管理员软删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("用户删除成功");
    }

    /**
     * 永久删除用户（管理员专用）
     */
    @DeleteMapping("/admin/{userId}/permanent")
    @Operation(summary = "永久删除用户", description = "管理员硬删除用户，完全从数据库移除，此操作不可恢复")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> permanentlyDeleteUser(@PathVariable Long userId) {
        log.info("硬删除用户请求，用户ID: {}", userId);
        
        try {
            userService.permanentlyDeleteUser(userId);
            log.info("用户硬删除成功，用户ID: {}", userId);
            return Result.success("用户已永久删除");
        } catch (Exception e) {
            log.error("硬删除用户失败，用户ID: {}", userId, e);
            return Result.error("硬删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}