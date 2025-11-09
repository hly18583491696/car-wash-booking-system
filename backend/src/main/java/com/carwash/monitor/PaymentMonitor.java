package com.carwash.monitor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 轻量支付监控组件：记录创建、查询、退款计数与失败计数。
 * 可后续扩展到 Micrometer/Prometheus。
 */
@Component
public class PaymentMonitor {
    private final AtomicLong createCount = new AtomicLong();
    private final AtomicLong queryCount = new AtomicLong();
    private final AtomicLong refundCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();

    public void markCreate() { createCount.incrementAndGet(); }
    public void markQuery() { queryCount.incrementAndGet(); }
    public void markRefund() { refundCount.incrementAndGet(); }
    public void markFailure() { failureCount.incrementAndGet(); }

    public long getCreateCount() { return createCount.get(); }
    public long getQueryCount() { return queryCount.get(); }
    public long getRefundCount() { return refundCount.get(); }
    public long getFailureCount() { return failureCount.get(); }
}