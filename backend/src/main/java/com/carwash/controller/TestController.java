package com.carwash.controller;

import com.carwash.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
@Tag(name = "测试接口", description = "系统测试相关接口")
public class TestController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public Result<String> healthCheck() {
        log.info("健康检查请求");
        return Result.success("服务运行正常 - " + System.currentTimeMillis());
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public Result<Map<String, Object>> getSystemInfo() {
        log.info("获取系统信息请求");
        
        Map<String, Object> info = new HashMap<>();
        info.put("applicationName", "洗车预约系统");
        info.put("version", "1.0.0");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("serverTime", System.currentTimeMillis());
        info.put("timeZone", System.getProperty("user.timezone"));
        
        return Result.success(info);
    }

    /**
     * Redis连接测试
     */
    @GetMapping("/redis")
    @Operation(summary = "Redis测试", description = "测试Redis连接是否正常")
    public Result<String> testRedis() {
        log.info("Redis连接测试请求");
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Redis connection test";
            
            // 写入测试数据
            stringRedisTemplate.opsForValue().set(testKey, testValue);
            
            // 读取测试数据
            String result = stringRedisTemplate.opsForValue().get(testKey);
            
            // 删除测试数据
            stringRedisTemplate.delete(testKey);
            
            if (testValue.equals(result)) {
                return Result.success("Redis连接正常");
            } else {
                return Result.error("Redis数据读写异常");
            }
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return Result.error("Redis连接异常: " + e.getMessage());
        }
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/database")
    @Operation(summary = "数据库测试", description = "测试数据库连接是否正常")
    public Result<String> testDatabase() {
        log.info("数据库连接测试请求");
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                return Result.success("数据库连接正常");
            } else {
                return Result.error("数据库连接异常");
            }
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return Result.error("数据库连接异常: " + e.getMessage());
        }
    }

    /**
     * CORS测试接口
     */
    @GetMapping("/cors")
    @Operation(summary = "CORS测试", description = "测试跨域请求是否正常")
    public Result<Map<String, Object>> testCors() {
        log.info("CORS测试请求");
        
        Map<String, Object> corsInfo = new HashMap<>();
        corsInfo.put("message", "CORS配置正常");
        corsInfo.put("timestamp", System.currentTimeMillis());
        corsInfo.put("origin", "允许所有来源");
        corsInfo.put("methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        return Result.success(corsInfo);
    }
}