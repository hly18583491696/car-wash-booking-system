package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.service.SmsService;
import com.carwash.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * 短信验证码控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "短信验证码", description = "短信验证码发送和验证接口")
public class SmsController {

    private static final Logger log = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-register-code")
    @Operation(summary = "发送注册验证码", description = "向指定手机号发送注册验证码")
    public Result<Void> sendRegisterCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam String phone) {
        
        log.info("发送注册验证码请求，手机号: {}", phone);

        // 验证手机号格式
        if (!isValidPhone(phone)) {
            return Result.error("手机号格式不正确");
        }

        // 检查手机号是否已注册
        if (userService.existsByPhone(phone)) {
            return Result.error("该手机号已注册");
        }

        // 发送验证码
        boolean success = smsService.sendRegisterCode(phone);
        if (success) {
            return Result.success("验证码发送成功");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 发送登录验证码
     */
    @PostMapping("/send-login-code")
    @Operation(summary = "发送登录验证码", description = "向指定手机号发送登录验证码")
    public Result<Void> sendLoginCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam String phone) {
        
        log.info("发送登录验证码请求，手机号: {}", phone);

        // 验证手机号格式
        if (!isValidPhone(phone)) {
            return Result.error("手机号格式不正确");
        }

        // 检查手机号是否已注册
        if (!userService.existsByPhone(phone)) {
            return Result.error("该手机号未注册");
        }

        // 发送验证码
        boolean success = smsService.sendLoginCode(phone);
        if (success) {
            return Result.success("验证码发送成功");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 发送重置密码验证码
     */
    @PostMapping("/send-reset-code")
    @Operation(summary = "发送重置密码验证码", description = "向指定手机号发送重置密码验证码")
    public Result<Void> sendResetPasswordCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam String phone) {
        
        log.info("发送重置密码验证码请求，手机号: {}", phone);

        // 验证手机号格式
        if (!isValidPhone(phone)) {
            return Result.error("手机号格式不正确");
        }

        // 检查手机号是否已注册
        if (!userService.existsByPhone(phone)) {
            return Result.error("该手机号未注册");
        }

        // 发送验证码
        boolean success = smsService.sendResetPasswordCode(phone);
        if (success) {
            return Result.success("验证码发送成功");
        } else {
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码", description = "验证手机验证码是否正确")
    public Result<Void> verifyCode(
            @Parameter(description = "手机号", required = true)
            @RequestParam String phone,
            @Parameter(description = "验证码", required = true)
            @RequestParam String code,
            @Parameter(description = "验证码类型", required = true)
            @RequestParam String type) {
        
        log.info("验证验证码请求，手机号: {}, 类型: {}", phone, type);

        // 验证手机号格式
        if (!isValidPhone(phone)) {
            return Result.error("手机号格式不正确");
        }

        // 验证验证码格式
        if (code == null || code.length() != 6 || !code.matches("\\d{6}")) {
            return Result.error("验证码格式不正确");
        }

        // 验证类型
        if (!isValidType(type)) {
            return Result.error("验证码类型不正确");
        }

        // 验证验证码
        boolean valid = smsService.verifyCode(phone, code, type);
        if (valid) {
            return Result.success("验证码验证成功");
        } else {
            return Result.error("验证码错误或已过期");
        }
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证验证码类型
     */
    private boolean isValidType(String type) {
        return "register".equals(type) || "login".equals(type) || "reset_password".equals(type);
    }
}