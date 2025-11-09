package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.config.NotificationConfig;
import com.carwash.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知测试控制器
 * 用于测试通知配置开关功能
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/notification-test")
@Tag(name = "通知测试", description = "通知配置开关测试接口")
public class NotificationTestController {

    private static final Logger log = LoggerFactory.getLogger(NotificationTestController.class);

    @Autowired
    private NotificationConfig notificationConfig;

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前通知配置状态
     */
    @GetMapping("/config")
    @Operation(summary = "获取通知配置", description = "获取当前通知配置开关状态")
    public Result<Map<String, Object>> getNotificationConfig() {
        log.info("获取通知配置状态");
        
        Map<String, Object> config = new HashMap<>();
        
        // SMS配置
        Map<String, Object> smsConfig = new HashMap<>();
        smsConfig.put("enabled", notificationConfig.getSms().isEnabled());
        smsConfig.put("orderStatusEnabled", notificationConfig.getSms().isOrderStatusEnabled());
        smsConfig.put("verificationEnabled", notificationConfig.getSms().isVerificationEnabled());
        config.put("sms", smsConfig);
        
        // 邮件配置
        Map<String, Object> emailConfig = new HashMap<>();
        emailConfig.put("enabled", notificationConfig.getEmail().isEnabled());
        emailConfig.put("orderStatusEnabled", notificationConfig.getEmail().isOrderStatusEnabled());
        config.put("email", emailConfig);
        
        // 微信配置
        Map<String, Object> wechatConfig = new HashMap<>();
        wechatConfig.put("enabled", notificationConfig.getWechat().isEnabled());
        wechatConfig.put("orderStatusEnabled", notificationConfig.getWechat().isOrderStatusEnabled());
        config.put("wechat", wechatConfig);
        
        // 站内消息配置
        Map<String, Object> inAppConfig = new HashMap<>();
        inAppConfig.put("enabled", notificationConfig.getInApp().isEnabled());
        inAppConfig.put("orderStatusEnabled", notificationConfig.getInApp().isOrderStatusEnabled());
        config.put("inApp", inAppConfig);
        
        log.info("通知配置状态: {}", config);
        return Result.success(config);
    }

    /**
     * 测试SMS通知
     */
    @PostMapping("/test-sms")
    @Operation(summary = "测试SMS通知", description = "测试SMS通知功能和配置开关")
    public Result<String> testSmsNotification(@RequestParam String phone, @RequestParam String message) {
        log.info("测试SMS通知: phone={}, message={}", phone, message);
        
        // 检查SMS配置
        if (!notificationConfig.getSms().isEnabled()) {
            log.warn("SMS通知已禁用，跳过发送");
            return Result.success("SMS通知已禁用，测试跳过");
        }
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("message", message);
            
            boolean success = notificationService.sendSms(phone, message, params);
            
            if (success) {
                log.info("SMS通知测试成功");
                return Result.success("SMS通知发送成功");
            } else {
                log.warn("SMS通知测试失败");
                return Result.error("SMS通知发送失败");
            }
        } catch (Exception e) {
            log.error("SMS通知测试异常", e);
            return Result.error("SMS通知测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试邮件通知
     */
    @PostMapping("/test-email")
    @Operation(summary = "测试邮件通知", description = "测试邮件通知功能和配置开关")
    public Result<String> testEmailNotification(@RequestParam String email, @RequestParam String subject, @RequestParam String content) {
        log.info("测试邮件通知: email={}, subject={}", email, subject);
        
        // 检查邮件配置
        if (!notificationConfig.getEmail().isEnabled()) {
            log.warn("邮件通知已禁用，跳过发送");
            return Result.success("邮件通知已禁用，测试跳过");
        }
        
        try {
            boolean success = notificationService.sendEmail(email, subject, content);
            
            if (success) {
                log.info("邮件通知测试成功");
                return Result.success("邮件通知发送成功");
            } else {
                log.warn("邮件通知测试失败");
                return Result.error("邮件通知发送失败");
            }
        } catch (Exception e) {
            log.error("邮件通知测试异常", e);
            return Result.error("邮件通知测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试微信通知
     */
    @PostMapping("/test-wechat")
    @Operation(summary = "测试微信通知", description = "测试微信通知功能和配置开关")
    public Result<String> testWechatNotification(@RequestParam String openId, @RequestParam String templateId, @RequestParam String message) {
        log.info("测试微信通知: openId={}, templateId={}", openId, templateId);
        
        // 检查微信配置
        if (!notificationConfig.getWechat().isEnabled()) {
            log.warn("微信通知已禁用，跳过发送");
            return Result.success("微信通知已禁用，测试跳过");
        }
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("message", message);
            
            boolean success = notificationService.sendWechatMessage(openId, templateId, data);
            
            if (success) {
                log.info("微信通知测试成功");
                return Result.success("微信通知发送成功");
            } else {
                log.warn("微信通知测试失败");
                return Result.error("微信通知发送失败");
            }
        } catch (Exception e) {
            log.error("微信通知测试异常", e);
            return Result.error("微信通知测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试站内消息
     */
    @PostMapping("/test-in-app")
    @Operation(summary = "测试站内消息", description = "测试站内消息功能和配置开关")
    public Result<String> testInAppNotification(@RequestParam Long userId, @RequestParam String title, @RequestParam String content) {
        log.info("测试站内消息: userId={}, title={}", userId, title);
        
        // 检查站内消息配置
        if (!notificationConfig.getInApp().isEnabled()) {
            log.warn("站内消息已禁用，跳过发送");
            return Result.success("站内消息已禁用，测试跳过");
        }
        
        try {
            boolean success = notificationService.sendInAppMessage(userId, title, content);
            
            if (success) {
                log.info("站内消息测试成功");
                return Result.success("站内消息发送成功");
            } else {
                log.warn("站内消息测试失败");
                return Result.error("站内消息发送失败");
            }
        } catch (Exception e) {
            log.error("站内消息测试异常", e);
            return Result.error("站内消息测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试订单状态通知
     */
    @PostMapping("/test-order-status")
    @Operation(summary = "测试订单状态通知", description = "测试订单状态变更通知功能和配置开关")
    public Result<String> testOrderStatusNotification(
            @RequestParam String phone,
            @RequestParam String orderNo,
            @RequestParam String oldStatus,
            @RequestParam String newStatus,
            @RequestParam String customerName) {
        
        log.info("测试订单状态通知: phone={}, orderNo={}, oldStatus={}, newStatus={}, customerName={}", 
                phone, orderNo, oldStatus, newStatus, customerName);
        
        try {
            boolean success = notificationService.sendOrderStatusSms(phone, orderNo, oldStatus, newStatus, customerName);
            
            if (success) {
                log.info("订单状态通知测试成功");
                return Result.success("订单状态通知发送成功");
            } else {
                log.warn("订单状态通知测试失败");
                return Result.error("订单状态通知发送失败");
            }
        } catch (Exception e) {
            log.error("订单状态通知测试异常", e);
            return Result.error("订单状态通知测试异常: " + e.getMessage());
        }
    }

    /**
     * 获取配置开关测试报告
     */
    @GetMapping("/config-test-report")
    @Operation(summary = "获取配置测试报告", description = "获取通知配置开关测试报告")
    public Result<Map<String, Object>> getConfigTestReport() {
        log.info("生成配置测试报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 测试各个配置开关
        Map<String, Object> testResults = new HashMap<>();
        
        // SMS测试
        Map<String, Object> smsTest = new HashMap<>();
        smsTest.put("enabled", notificationConfig.getSms().isEnabled());
        smsTest.put("orderStatusEnabled", notificationConfig.getSms().isOrderStatusEnabled());
        smsTest.put("verificationEnabled", notificationConfig.getSms().isVerificationEnabled());
        smsTest.put("status", notificationConfig.getSms().isEnabled() ? "可用" : "已禁用");
        testResults.put("sms", smsTest);
        
        // 邮件测试
        Map<String, Object> emailTest = new HashMap<>();
        emailTest.put("enabled", notificationConfig.getEmail().isEnabled());
        emailTest.put("orderStatusEnabled", notificationConfig.getEmail().isOrderStatusEnabled());
        emailTest.put("status", notificationConfig.getEmail().isEnabled() ? "可用" : "已禁用");
        testResults.put("email", emailTest);
        
        // 微信测试
        Map<String, Object> wechatTest = new HashMap<>();
        wechatTest.put("enabled", notificationConfig.getWechat().isEnabled());
        wechatTest.put("orderStatusEnabled", notificationConfig.getWechat().isOrderStatusEnabled());
        wechatTest.put("status", notificationConfig.getWechat().isEnabled() ? "可用" : "已禁用");
        testResults.put("wechat", wechatTest);
        
        // 站内消息测试
        Map<String, Object> inAppTest = new HashMap<>();
        inAppTest.put("enabled", notificationConfig.getInApp().isEnabled());
        inAppTest.put("orderStatusEnabled", notificationConfig.getInApp().isOrderStatusEnabled());
        inAppTest.put("status", notificationConfig.getInApp().isEnabled() ? "可用" : "已禁用");
        testResults.put("inApp", inAppTest);
        
        report.put("testResults", testResults);
        report.put("timestamp", System.currentTimeMillis());
        report.put("summary", generateTestSummary(testResults));
        
        log.info("配置测试报告生成完成: {}", report);
        return Result.success(report);
    }

    /**
     * 生成测试摘要
     */
    private Map<String, Object> generateTestSummary(Map<String, Object> testResults) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalConfigs = 0;
        int enabledConfigs = 0;
        
        for (Map.Entry<String, Object> entry : testResults.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) entry.getValue();
            totalConfigs++;
            if ((Boolean) config.get("enabled")) {
                enabledConfigs++;
            }
        }
        
        summary.put("totalConfigs", totalConfigs);
        summary.put("enabledConfigs", enabledConfigs);
        summary.put("disabledConfigs", totalConfigs - enabledConfigs);
        summary.put("enabledPercentage", totalConfigs > 0 ? (enabledConfigs * 100.0 / totalConfigs) : 0);
        
        return summary;
    }
}