package com.carwash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源处理等
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 静态资源
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-resources/");
        
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/v3/api-docs/");
        
        // 上传文件静态资源
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}