package com.carwash.service.payment;

import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.dto.RefundRequest;
import com.carwash.dto.RefundResponse;
import com.carwash.service.payment.virtual.VirtualPaymentSDK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VirtualPaymentGateway 单元测试
 * - 验证 create/query/refund/handleCallback 四个核心行为
 */
public class VirtualPaymentGatewayTest {

    private VirtualPaymentGateway gateway;
    private VirtualPaymentSDK sdk;

    @BeforeEach
    void setup() throws Exception {
        sdk = new VirtualPaymentSDK();
        gateway = new VirtualPaymentGateway();
        // 通过反射注入 SDK，避免引入 Spring 上下文
        var field = VirtualPaymentGateway.class.getDeclaredField("sdk");
        field.setAccessible(true);
        field.set(gateway, sdk);
    }

    @Test
    void testCreatePaymentQrChannel() {
        PaymentRequest req = new PaymentRequest();
        req.setOrderNo("ORDER-UNIT-001");
        req.setAmount(new java.math.BigDecimal("19.90"));
        req.setPaymentMethod("virtual");
        req.setChannel("qr");

        PaymentResponse resp = gateway.createPayment(req);
        assertNotNull(resp.getPaymentNo(), "支付单号应生成");
        assertEquals("pending", resp.getStatus(), "初始状态应为 pending");
        assertNotNull(resp.getQrCode(), "QR 场景应返回二维码");
        assertNull(resp.getPayUrl(), "QR 场景不返回支付 URL");
    }

    @Test
    void testQueryPaymentTransitionsToPaid() {
        PaymentRequest req = new PaymentRequest();
        req.setOrderNo("ORDER-UNIT-002");
        req.setAmount(new java.math.BigDecimal("9.90"));
        req.setPaymentMethod("virtual");
        req.setChannel("h5");

        PaymentResponse created = gateway.createPayment(req);
        assertEquals("pending", created.getStatus());

        PaymentResponse queried = gateway.queryPayment(created.getPaymentNo());
        assertEquals("paid", queried.getStatus(), "查询后应模拟为已支付");
        assertNotNull(queried.getTransactionId(), "查询后应返回交易号");
    }

    @Test
    void testRefundSuccess() {
        PaymentRequest req = new PaymentRequest();
        req.setOrderNo("ORDER-UNIT-003");
        req.setAmount(new java.math.BigDecimal("29.90"));
        req.setPaymentMethod("virtual");
        req.setChannel("app");

        PaymentResponse created = gateway.createPayment(req);

        RefundRequest refund = new RefundRequest();
        refund.setPaymentNo(created.getPaymentNo());
        refund.setAmount(new java.math.BigDecimal("5.00"));
        refund.setReason("单元测试退款");
        refund.setOperatorId(100L);

        RefundResponse refundResp = gateway.refund(refund);
        assertTrue(refundResp.isSuccess(), "退款应成功");
        assertEquals("success", refundResp.getStatus(), "退款状态应为 success");
        assertNotNull(refundResp.getRefundNo(), "应返回退款单号");
    }

    @Test
    void testHandleCallbackAlwaysTrue() {
        boolean ok = gateway.handleCallback("{\"mock\":true}");
        assertTrue(ok, "虚拟回调应始终返回 true");
    }
}