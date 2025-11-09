package com.carwash.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付网关工厂类
 * 根据支付方式选择对应的支付网关
 */
@Component
public class PaymentGatewayFactory {
    
    private final Map<String, PaymentGateway> gateways = new HashMap<>();
    
    @Autowired
    public PaymentGatewayFactory(List<PaymentGateway> paymentGateways) {
        for (PaymentGateway gateway : paymentGateways) {
            gateways.put(gateway.getPaymentMethod(), gateway);
        }
    }
    
    /**
     * 根据支付方式获取支付网关
     * @param paymentMethod 支付方式
     * @return 支付网关
     */
    public PaymentGateway getGateway(String paymentMethod) {
        PaymentGateway gateway = gateways.get(paymentMethod);
        if (gateway == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod);
        }
        return gateway;
    }
    
    /**
     * 获取所有支持的支付方式
     * @return 支付方式列表
     */
    public String[] getSupportedPaymentMethods() {
        return gateways.keySet().toArray(new String[0]);
    }
    
    /**
     * 检查是否支持指定的支付方式
     * @param paymentMethod 支付方式
     * @return 是否支持
     */
    public boolean isSupported(String paymentMethod) {
        return gateways.containsKey(paymentMethod);
    }
}