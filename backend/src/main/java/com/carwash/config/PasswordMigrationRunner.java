package com.carwash.config;

import com.carwash.entity.User;
import com.carwash.mapper.UserMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class PasswordMigrationRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner migratePlaintextPasswords() {
        return args -> {
            List<User> users = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                    .eq("deleted", 0)
            );
            for (User u : users) {
                String pwd = u.getPassword();
                if (pwd != null && !(pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
                    // 将数据库中的明文密码一次性迁移为BCrypt哈希，保持用户原有口令不变
                    u.setPassword(passwordEncoder.encode(pwd));
                    userMapper.updateById(u);
                }
            }
        };
    }
}