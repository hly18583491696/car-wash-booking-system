package com.carwash.common.constants;

/**
 * 系统常量类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
public class Constants {

    /**
     * JWT相关常量
     */
    public static class JWT {
        public static final String TOKEN_HEADER = "Authorization";
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String TOKEN_TYPE = "JWT";
    }

    /**
     * 用户相关常量
     */
    public static class User {
        public static final String DEFAULT_AVATAR = "/images/default-avatar.png";
        public static final Integer STATUS_NORMAL = 1;
        public static final Integer STATUS_DISABLED = 0;
        public static final String ROLE_USER = "USER";
        public static final String ROLE_ADMIN = "ADMIN";
    }

    /**
     * 服务相关常量
     */
    public static class Service {
        public static final Integer STATUS_AVAILABLE = 1;
        public static final Integer STATUS_UNAVAILABLE = 0;
        public static final String TYPE_BASIC = "BASIC";
        public static final String TYPE_PREMIUM = "PREMIUM";
        public static final String TYPE_LUXURY = "LUXURY";
    }

    /**
     * 预约相关常量
     */
    public static class Appointment {
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_CONFIRMED = "CONFIRMED";
        public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
        public static final String STATUS_COMPLETED = "COMPLETED";
        public static final String STATUS_CANCELLED = "CANCELLED";
        
        public static final Integer MAX_ADVANCE_DAYS = 30; // 最多提前30天预约
        public static final Integer MIN_ADVANCE_HOURS = 2; // 最少提前2小时预约
    }

    /**
     * 订单相关常量
     */
    public static class Order {
        public static final String STATUS_UNPAID = "UNPAID";
        public static final String STATUS_PAID = "PAID";
        public static final String STATUS_REFUNDED = "REFUNDED";
        public static final String STATUS_CANCELLED = "CANCELLED";
    }

    /**
     * Redis缓存相关常量
     */
    public static class Cache {
        public static final String USER_PREFIX = "user:";
        public static final String SERVICE_PREFIX = "service:";
        public static final String APPOINTMENT_PREFIX = "appointment:";
        public static final String TIME_SLOT_PREFIX = "time_slot:";
        
        public static final int DEFAULT_EXPIRE_TIME = 3600; // 1小时
        public static final int USER_EXPIRE_TIME = 7200; // 2小时
        public static final int SERVICE_EXPIRE_TIME = 1800; // 30分钟
    }

    /**
     * 分页相关常量
     */
    public static class Page {
        public static final Integer DEFAULT_PAGE_NUM = 1;
        public static final Integer DEFAULT_PAGE_SIZE = 10;
        public static final Integer MAX_PAGE_SIZE = 100;
    }

    /**
     * 文件上传相关常量
     */
    public static class Upload {
        public static final String UPLOAD_PATH = "uploads/";
        public static final String AVATAR_PATH = "uploads/avatars/";
        public static final String SERVICE_IMAGE_PATH = "uploads/services/";
        public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
        public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
    }

    /**
     * 时间相关常量
     */
    public static class Time {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String TIME_FORMAT = "HH:mm:ss";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        
        public static final int WORK_START_HOUR = 8; // 营业开始时间
        public static final int WORK_END_HOUR = 20; // 营业结束时间
        public static final int TIME_SLOT_DURATION = 60; // 时间段长度（分钟）
    }
}