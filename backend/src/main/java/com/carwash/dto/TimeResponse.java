package com.carwash.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 时间响应DTO
 * 用于前后端时间同步
 */
@Data
public class TimeResponse {
    
    /**
     * 服务器当前时间
     */
    private LocalDateTime serverTime;
    
    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 格式化的时间字符串
     */
    private String formatted;
    
    /**
     * ISO格式时间字符串
     */
    private String iso;
    
    /**
     * 时区信息
     */
    private String timezone;
    
    /**
     * 构造函数
     */
    public TimeResponse() {
    }
    
    /**
     * 构造函数
     * @param serverTime 服务器时间
     * @param timestamp 时间戳
     * @param formatted 格式化时间
     * @param iso ISO格式时间
     * @param timezone 时区
     */
    public TimeResponse(LocalDateTime serverTime, Long timestamp, String formatted, String iso, String timezone) {
        this.serverTime = serverTime;
        this.timestamp = timestamp;
        this.formatted = formatted;
        this.iso = iso;
        this.timezone = timezone;
    }
}