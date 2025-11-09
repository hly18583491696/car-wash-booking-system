package com.carwash.controller;

import com.carwash.common.result.Result;
import com.carwash.config.PaymentConfig;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.entity.Payment;
import com.carwash.entity.PaymentAudit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.carwash.service.PaymentService;
import com.carwash.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentConfig paymentConfig;

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付订单")
    public Result<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 设置客户端IP
            request.setClientIp(getClientIp(httpRequest));
            
            PaymentResponse response = paymentService.createPayment(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return Result.error("创建支付订单失败：" + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{paymentNo}")
    @Operation(summary = "查询支付状态")
    public Result<PaymentResponse> queryPaymentStatus(@PathVariable String paymentNo) {
        try {
            PaymentResponse response = paymentService.queryPaymentStatus(paymentNo);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.error("查询支付状态失败：" + e.getMessage());
        }
    }

    /**
     * 根据订单号查询支付信息
     */
    @GetMapping("/order/{orderNo}")
    @Operation(summary = "根据订单号查询支付信息")
    public Result<Payment> getPaymentByOrderNo(@PathVariable String orderNo) {
        try {
            Payment payment = paymentService.getPaymentByOrderNo(orderNo);
            return Result.success(payment);
        } catch (Exception e) {
            log.error("查询支付信息失败", e);
            return Result.error("查询支付信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户支付记录
     */
    @GetMapping("/user/payments")
    @Operation(summary = "获取用户支付记录")
    public Result<List<Payment>> getUserPayments() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<Payment> payments = paymentService.getUserPayments(userId);
            return Result.success(payments);
        } catch (Exception e) {
            log.error("获取用户支付记录失败", e);
            return Result.error("获取用户支付记录失败：" + e.getMessage());
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund")
    @Operation(summary = "申请退款")
    public Result<PaymentResponse> processRefund(@Valid @RequestBody RefundRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            request.setOperatorId(userId);
            
            PaymentResponse response = paymentService.processRefund(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.error("申请退款失败：" + e.getMessage());
        }
    }

    /**
     * 微信支付回调
     */
    @PostMapping("/callback/wechat")
    @Operation(summary = "微信支付回调")
    public String wechatPayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = getRequestParams(request);
            boolean success = paymentService.handlePaymentCallback("wechat", params);
            
            if (success) {
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            } else {
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
            }
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统错误]]></return_msg></xml>";
        }
    }

    /**
     * 支付宝支付回调
     */
    @PostMapping("/callback/alipay")
    @Operation(summary = "支付宝支付回调")
    public String alipayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = getRequestParams(request);
            boolean success = paymentService.handlePaymentCallback("alipay", params);
            
            return success ? "success" : "fail";
        } catch (Exception e) {
            log.error("支付宝支付回调处理失败", e);
            return "fail";
        }
    }

    /**
     * 虚拟支付回调（开发测试用）
     */
    @PostMapping("/callback/virtual")
    @Operation(summary = "虚拟支付回调")
    public String virtualPayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = getRequestParams(request);
            boolean success = paymentService.handlePaymentCallback("virtual", params);
            return success ? "ok" : "fail";
        } catch (Exception e) {
            log.error("虚拟支付回调处理失败", e);
            return "fail";
        }
    }

    /**
     * 管理员退款操作
     */
    @PostMapping("/admin/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "管理员退款操作")
    public Result<PaymentResponse> adminRefund(@Valid @RequestBody RefundRequest request) {
        try {
            Long adminId = SecurityUtils.getCurrentUserId();
            request.setOperatorId(adminId);
            
            PaymentResponse response = paymentService.processRefund(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("管理员退款操作失败", e);
            return Result.error("退款操作失败：" + e.getMessage());
        }
    }

    /**
     * 取消过期支付订单（定时任务调用）
     */
    @PostMapping("/admin/cancel-expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取消过期支付订单")
    public Result<String> cancelExpiredPayments() {
        try {
            paymentService.cancelExpiredPayments();
            return Result.success("取消过期支付订单成功");
        } catch (Exception e) {
            log.error("取消过期支付订单失败", e);
            return Result.error("取消过期支付订单失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有支付记录（管理员）
     */
    @GetMapping("/admin/records")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有支付记录")
    public Result<List<Payment>> getAllPaymentRecords() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            return Result.success(payments);
        } catch (Exception e) {
            log.error("获取所有支付记录失败", e);
            return Result.error("获取所有支付记录失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询支付审计日志（管理员）
     */
    @GetMapping("/admin/audits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询支付审计日志")
    public Result<IPage<PaymentAudit>> getPaymentAudits(
            @RequestParam(required = false, defaultValue = "1") Long current,
            @RequestParam(required = false, defaultValue = "10") Long size,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status) {
        try {
            IPage<PaymentAudit> page = paymentService.getPaymentAudits(current, size, paymentNo, orderNo, eventType, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询支付审计日志失败", e);
            return Result.error("分页查询支付审计日志失败：" + e.getMessage());
        }
    }

    /**
     * 获取前端加密所需的RSA公钥
     */
    @GetMapping("/security/public-key")
    @Operation(summary = "获取RSA公钥")
    public Result<String> getSecurityPublicKey() {
        try {
            String publicKey = paymentConfig.getSecurity().getRsaPublicKey();
            return Result.success(publicKey);
        } catch (Exception e) {
            log.error("获取RSA公钥失败", e);
            return Result.error("获取RSA公钥失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        
        return params;
    }
}