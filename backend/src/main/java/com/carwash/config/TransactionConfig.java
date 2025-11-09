package com.carwash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 事务配置类
 * 确保事务管理正常工作
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    // 启用事务管理
}