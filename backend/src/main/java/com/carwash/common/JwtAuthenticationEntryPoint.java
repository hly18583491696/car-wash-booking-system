package com.carwash.common;

import com.alibaba.fastjson2.JSON;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 处理未认证的请求
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", "未授权访问，请先登录");
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(JSON.toJSONString(result));
    }
}