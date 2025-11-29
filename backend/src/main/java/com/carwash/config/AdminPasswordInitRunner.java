package com.carwash.config;

import com.carwash.entity.User;
import com.carwash.mapper.UserMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AdminPasswordInitRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordInitRunner(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner initAdminPassword() {
        return args -> {
            List<User> admins = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                    .eq("role", "admin")
                    .eq("deleted", 0)
            );
            for (User admin : admins) {
                String pwd = admin.getPassword();
                if (pwd != null && !(pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
                    admin.setPassword(passwordEncoder.encode(pwd));
                    userMapper.updateById(admin);
                }
            }
        };
    }
}