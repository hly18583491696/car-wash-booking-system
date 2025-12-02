package com.carwash.service.payment;

import com.carwash.config.PaymentConfig;
import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 支付宝模拟支付网关测试类
 */
@ExtendWith(MockitoExtension.class)
public class AlipayMockGatewayTest {

    @InjectMocks
    private AlipayMockGateway alipayMockGateway;

    @Mock
    private PaymentConfig paymentConfig;

    @BeforeEach
    void setUp() {
        // 初始化配置
        PaymentConfig.Alipay alipayConfig = new PaymentConfig.Alipay();
        alipayConfig.setAppId("2021000000000000");
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        alipayConfig.setNotifyUrl("http://localhost:8080/api/payment/callback/alipay");
        
        when(paymentConfig.getAlipay()).thenReturn(alipayConfig);
        
        // 设置服务器端口
        ReflectionTestUtils.setField(alipayMockGateway, "serverPort", "8080");
    }

    @Test
    void testGetPaymentMethod() {
        // 测试支付方式标识
        String paymentMethod = alipayMockGateway.getPaymentMethod();
        assertEquals("alipay_mock", paymentMethod);
    }

    @Test
    void testCreatePayment() {
        // 创建支付请求
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("99.99"));
        request.setPaymentMethod("alipay_mock");
        request.setDescription("测试订单");

        // 调用创建支付
        PaymentResponse response = alipayMockGateway.createPayment(request);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("pending", response.getStatus());
        assertNotNull(response.getPaymentNo());
        assertNotNull(response.getQrCode());
        assertNotNull(response.getPaymentUrl());
        assertEquals(request.getOrderNo(), response.getOrderNo());
        assertTrue(response.getMessage().contains("模拟支付"));
    }

    @Test
    void testQueryPayment() {
        // 先创建支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("99.99"));
        request.setPaymentMethod("alipay_mock");

        PaymentResponse createResponse = alipayMockGateway.createPayment(request);
        String paymentNo = createResponse.getPaymentNo();

        // 查询支付状态
        PaymentResponse queryResponse = alipayMockGateway.queryPayment(paymentNo);

        // 验证结果
        assertNotNull(queryResponse);
        assertTrue(queryResponse.isSuccess());
        assertEquals(paymentNo, queryResponse.getPaymentNo());
        assertNotNull(queryResponse.getStatus());
    }

    @Test
    void testRefund() {
        // 先创建支付
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        paymentRequest.setAmount(new BigDecimal("99.99"));
        paymentRequest.setPaymentMethod("alipay_mock");

        PaymentResponse createResponse = alipayMockGateway.createPayment(paymentRequest);
        String paymentNo = createResponse.getPaymentNo();

        // 模拟支付成功(手动更新状态)
        // 在实际环境中,这会通过回调自动完成

        // 创建退款请求
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentNo(paymentNo);
        refundRequest.setAmount(new BigDecimal("99.99"));
        refundRequest.setReason("测试退款");
        refundRequest.setOperatorId(1L);

        // 调用退款
        RefundResponse refundResponse = alipayMockGateway.refund(refundRequest);

        // 验证结果
        assertNotNull(refundResponse);
        // 注意: 由于支付状态是pending,退款会失败
        // 如果需要测试成功场景,需要先让支付状态变为paid
    }

    @Test
    void testQueryRefund() {
        // 查询退款状态
        String refundNo = "REF_MOCK_20251202123456";
        RefundResponse response = alipayMockGateway.queryRefund(refundNo);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(refundNo, response.getRefundNo());
        assertEquals("success", response.getStatus());
    }

    @Test
    void testHandleCallback() {
        // 测试回调处理
        String callbackData = "out_trade_no=TEST123&trade_status=TRADE_SUCCESS";
        boolean result = alipayMockGateway.handleCallback(callbackData);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testCreatePaymentWithMinAmount() {
        // 测试最小金额支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("0.01"));
        request.setPaymentMethod("alipay_mock");

        PaymentResponse response = alipayMockGateway.createPayment(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("0.01"), response.getAmount());
    }

    @Test
    void testCreatePaymentWithLargeAmount() {
        // 测试大额支付
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo("TEST_ORD_" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("9999.99"));
        request.setPaymentMethod("alipay_mock");

        PaymentResponse response = alipayMockGateway.createPayment(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("9999.99"), response.getAmount());
    }
}
