package com.carwash.service.payment;

import com.carwash.dto.PaymentRequest;
import com.carwash.dto.PaymentResponse;
import com.carwash.entity.Booking;
import com.carwash.mapper.PaymentMapper;
import com.carwash.mapper.BookingMapper;
import com.carwash.service.impl.PaymentServiceImpl;
import com.carwash.service.payment.virtual.VirtualPaymentSDK;
import com.carwash.service.payment.PaymentGatewayFactory;
import com.carwash.service.payment.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 使用虚拟支付网关的 PaymentServiceImpl 服务级测试
 * 通过模拟 Mapper 层，验证创建支付与查询逻辑。
 */
public class PaymentServiceImplVirtualGatewayTest {

    private PaymentServiceImpl paymentService;
    private PaymentGatewayFactory gatewayFactory;
    private PaymentMapper paymentMapper;
    private BookingMapper bookingMapper;

    @BeforeEach
    void setup() {
        // 使用真实的虚拟网关实例
        VirtualPaymentSDK sdk = new VirtualPaymentSDK();
        VirtualPaymentGateway virtualGateway = new VirtualPaymentGateway();
        try {
            var f = VirtualPaymentGateway.class.getDeclaredField("sdk");
            f.setAccessible(true);
            f.set(virtualGateway, sdk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 将虚拟网关注册到工厂
        gatewayFactory = new PaymentGatewayFactory(java.util.List.of(virtualGateway));

        // 模拟 Mapper 层（数据库交互）
        paymentMapper = mock(PaymentMapper.class);
        bookingMapper = mock(BookingMapper.class);

        paymentService = new PaymentServiceImpl();
        try {
            var f1 = PaymentServiceImpl.class.getDeclaredField("paymentMapper");
            f1.setAccessible(true);
            f1.set(paymentService, paymentMapper);
            var f2 = PaymentServiceImpl.class.getDeclaredField("bookingMapper");
            f2.setAccessible(true);
            f2.set(paymentService, bookingMapper);
            var f3 = PaymentServiceImpl.class.getDeclaredField("paymentGatewayFactory");
            f3.setAccessible(true);
            f3.set(paymentService, gatewayFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreatePaymentAndQueryFlow() {
        // 构造 Booking
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(42L);
        booking.setStatus("confirmed");
        booking.setPaymentStatus("unpaid");

        when(bookingMapper.selectByOrderNo("ORDER-SERVICE-001")).thenReturn(booking);

        // 捕获插入的 Payment，以用于后续根据 paymentNo 查询
        final java.util.concurrent.atomic.AtomicReference<com.carwash.entity.Payment> insertedPayment = new java.util.concurrent.atomic.AtomicReference<>();
        when(paymentMapper.insert(any(com.carwash.entity.Payment.class))).thenAnswer(invocation -> {
            com.carwash.entity.Payment p = invocation.getArgument(0);
            insertedPayment.set(p);
            return 1; // 模拟插入成功
        });
        when(paymentMapper.selectByPaymentNo(anyString())).thenAnswer(invocation -> insertedPayment.get());

        // 创建支付请求
        PaymentRequest req = new PaymentRequest();
        req.setOrderNo("ORDER-SERVICE-001");
        req.setAmount(new BigDecimal("15.80"));
        req.setPaymentMethod("virtual");
        req.setChannel("h5");

        PaymentResponse created = paymentService.createPayment(req, 42L);
        assertNotNull(created.getPaymentNo(), "应生成支付单号");
        assertEquals("pending", created.getStatus(), "初始状态应为 pending");

        // 查询支付状态（虚拟实现会变为 paid）
        PaymentResponse queried = paymentService.queryPaymentStatus(created.getPaymentNo());
        assertEquals("paid", queried.getStatus(), "查询后应为 paid");
        assertNotNull(queried.getTransactionId(), "查询后应返回交易号");

        // 验证 Mapper 层交互基本发生（不强约束具体 SQL 行为）
        verify(bookingMapper, atLeastOnce()).selectByOrderNo("ORDER-SERVICE-001");
    }
}