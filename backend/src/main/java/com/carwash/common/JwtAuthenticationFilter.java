package com.carwash.common;

import com.carwash.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT token并验证
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        logger.debug("JWT过滤器处理请求: {}", requestURI);
        
        try {
            // 从请求头获取JWT token
            String jwt = getJwtFromRequest(request);
            logger.debug("从请求头提取的JWT token: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 50)) + "..." : "null");
            
            if (StringUtils.hasText(jwt)) {
                logger.debug("JWT token不为空，开始验证");
                boolean isValid = jwtUtils.validateToken(jwt);
                logger.debug("JWT token验证结果: {}", isValid);
                
                if (isValid) {
                    // 从token中获取用户名
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    logger.debug("从JWT token中提取的用户名: {}", username);
                    
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("加载用户详情成功，用户: {}, 权限: {}", username, userDetails.getAuthorities());
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("JWT认证成功，已设置安全上下文，用户: {}", username);
                } else {
                    logger.warn("JWT token验证失败，请求URI: {}", requestURI);
                }
            } else {
                logger.debug("请求头中没有JWT token，请求URI: {}", requestURI);
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证，请求URI: {}, 错误: {}", requestURI, ex.getMessage(), ex);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头或URL参数中提取JWT token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 首先尝试从请求头获取token
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 如果请求头中没有token，尝试从URL参数获取（用于管理后台页面跳转）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            logger.debug("从URL参数中提取到JWT token");
            return tokenParam;
        }
        
        return null;
    }
}