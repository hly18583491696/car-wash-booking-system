package com.carwash.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "reset-db", havingValue = "true")
public class DbResetRunner {
    private final DataSource dataSource;

    public DbResetRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public ApplicationRunner dbReset() {
        return args -> {
            ClassPathResource resource = new ClassPathResource("sql/reset_seed.sql");
            try (InputStream in = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                String sql = reader.lines().collect(Collectors.joining("\n"));
                String cleaned = sql.replaceAll("(?s)/\\*.*?\\*/", "");
                String[] statements = cleaned.replace("\r\n", "\n").lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n")).split(";\n?");
                for (String s : statements) {
                    String q = s.trim();
                    if (!q.isEmpty()) {
                        stmt.execute(q);
                    }
                }
                conn.commit();
                System.out.println("=== 数据库重置与初始化完成 ===");
                String[] tables = new String[]{
                        "users","services","time_slots","bookings","feedback","system_config","payments","refunds","payment_audit"
                };
                for (String t : tables) {
                    try {
                        var rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM " + t);
                        if (rs.next()) {
                            System.out.println(t + " 行数=" + rs.getLong("c"));
                        }
                        rs.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        };
    }
}
