package com.carwash.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * 统一时间工具类
 * 解决前后端时间同步问题
 */
public class TimeUtils {
    
    /**
     * 系统默认时区 - 中国标准时间
     */
    public static final ZoneId SYSTEM_ZONE = ZoneId.of("Asia/Shanghai");
    
    /**
     * UTC时区
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    
    /**
     * 标准日期时间格式
     */
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 标准日期格式
     */
    public static final DateTimeFormatter DEFAULT_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * ISO格式（用于前后端传输）
     */
    public static final DateTimeFormatter ISO_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * 获取当前系统时间
     * @return 当前LocalDateTime（系统时区）
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(SYSTEM_ZONE);
    }
    
    /**
     * 获取当前UTC时间
     * @return 当前UTC时间
     */
    public static LocalDateTime utcNow() {
        return LocalDateTime.now(UTC_ZONE);
    }
    
    /**
     * 获取当前日期
     * @return 当前LocalDate（系统时区）
     */
    public static LocalDate today() {
        return LocalDate.now(SYSTEM_ZONE);
    }
    
    /**
     * 将LocalDateTime转换为时间戳（毫秒）
     * @param dateTime 本地时间
     * @return 时间戳
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) return 0L;
        return dateTime.atZone(SYSTEM_ZONE).toInstant().toEpochMilli();
    }
    
    /**
     * 将时间戳转换为LocalDateTime
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), 
            SYSTEM_ZONE
        );
    }
    
    /**
     * 格式化时间为标准字符串
     * @param dateTime 时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DEFAULT_DATETIME_FORMAT);
    }
    
    /**
     * 格式化日期为标准字符串
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 格式化时间为ISO字符串（用于前端传输）
     * @param dateTime 时间
     * @return ISO格式字符串
     */
    public static String formatToISO(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.atZone(SYSTEM_ZONE)
                      .withZoneSameInstant(UTC_ZONE)
                      .format(ISO_FORMAT);
    }
    
    /**
     * 从ISO字符串解析时间
     * @param isoString ISO格式字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseFromISO(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(isoString, ISO_FORMAT)
                               .atZone(UTC_ZONE)
                               .withZoneSameInstant(SYSTEM_ZONE)
                               .toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析日期字符串
     * @param dateString 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DEFAULT_DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析日期时间字符串
     * @param dateTimeString 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DEFAULT_DATETIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 判断是否为同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否同一天
     */
    public static boolean isSameDate(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) return false;
        return date1.equals(date2);
    }
    
    /**
     * 判断是否为今天
     * @param date 要检查的日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return isSameDate(date, today());
    }
    
    /**
     * 获取相对时间描述
     * @param dateTime 时间
     * @return 相对时间描述
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (seconds < 60) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 7) {
            return days + "天前";
        } else {
            return formatDateTime(dateTime);
        }
    }
    
    /**
     * 设置系统时区
     */
    public static void setSystemTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(SYSTEM_ZONE));
        System.setProperty("user.timezone", SYSTEM_ZONE.getId());
    }
}