package com.carwash.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.carwash.service.UserService;

/**
 * 安全工具类
 * 用于获取当前登录用户信息
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Component
public class SecurityUtils {

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        SecurityUtils.userService = userService;
    }

    /**
     * 获取当前登录用户的用户名
     * 
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户的认证信息
     * 
     * @return Authentication对象，如果未登录则返回null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否已登录
     * 
     * @return true如果已登录，false如果未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 获取当前用户的UserDetails
     * 
     * @return UserDetails对象，如果未登录则返回null
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID，如果未登录则抛出异常
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // 根据用户名查询用户ID
            return userService.findByUsernameOrPhone(username).getId();
        }
        throw new RuntimeException("无法获取当前用户信息");
    }

    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称
     * @return true如果具有该角色，false如果没有
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }

    /**
     * 检查当前用户是否具有指定权限
     * 
     * @param permission 权限名称
     * @return true如果具有该权限，false如果没有
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = getCurrentAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));
        }
        return false;
    }
}