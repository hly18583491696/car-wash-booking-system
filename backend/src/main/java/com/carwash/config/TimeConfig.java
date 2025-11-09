package com.carwash.config;

import com.carwash.utils.TimeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

/**
 * 时间配置类
 * 统一系统时区设置
 */
@Configuration
public class TimeConfig {

    /**
     * 初始化系统时区
     * 在应用启动时设置统一时区
     */
    @Bean
    public ApplicationRunner timeZoneInitializer() {
        return args -> {
            // 设置系统时区为中国标准时间
            TimeUtils.setSystemTimeZone();
            
            System.out.println("系统时区已设置为: " + TimeUtils.SYSTEM_ZONE.getId());
            System.out.println("当前系统时间: " + TimeUtils.formatDateTime(TimeUtils.now()));
        };
    }
}