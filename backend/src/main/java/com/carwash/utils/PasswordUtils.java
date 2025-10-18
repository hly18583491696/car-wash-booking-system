package com.carwash.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 密码工具类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public class PasswordUtils {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final SecureRandom random = new SecureRandom();

    // 密码强度正则表达式
    private static final Pattern WEAK_PASSWORD = Pattern.compile("^[0-9]+$|^[a-zA-Z]+$");
    private static final Pattern MEDIUM_PASSWORD = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$");

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&+=";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    /**
     * 检查密码强度
     * @param password 密码
     * @return 1-弱, 2-中等, 3-强
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 1; // 弱
        }
        
        if (STRONG_PASSWORD.matcher(password).matches()) {
            return 3; // 强
        } else if (MEDIUM_PASSWORD.matcher(password).matches()) {
            return 2; // 中等
        } else {
            return 1; // 弱
        }
    }

    /**
     * 验证密码格式
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            return false;
        }
        
        // 不能是纯数字或纯字母
        return !WEAK_PASSWORD.matcher(password).matches();
    }
}