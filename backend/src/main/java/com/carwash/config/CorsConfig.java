package com.carwash.config;

import org.springframework.context.annotation.Configuration;

/**
 * 跨域配置类
 * 解决前后端分离开发中的跨域问题
 * 注意：CORS配置已移至SecurityConfig中统一管理
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {
    // CORS配置已移至SecurityConfig中统一管理，避免Bean重复定义
}