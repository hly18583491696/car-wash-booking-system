package com.carwash;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 汽车洗车服务预约系统主启动类
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.carwash.mapper")
public class CarwashApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarwashApplication.class, args);
        System.out.println("=================================");
        System.out.println("汽车洗车服务预约系统启动成功！");
        System.out.println("=================================");
    }
}