package com.carwash.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 配置API文档生成
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建OpenAPI文档
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("汽车洗车服务预约系统 API文档")
                        .description("基于SpringBoot + Vue.js的汽车洗车服务预约系统后端API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CarWash Team")
                                .email("")
                                .url(""))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT token，格式为：Bearer {token}")));
    }
}