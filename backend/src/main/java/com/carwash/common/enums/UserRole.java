package com.carwash.common.enums;

/**
 * 用户角色枚举
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public enum UserRole {
    
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static UserRole getByCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}