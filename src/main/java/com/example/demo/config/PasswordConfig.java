package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration tạm thời để disable password encoding
 * CHỈ DÙNG CHO TESTING - KHÔNG DÙNG TRONG PRODUCTION!
 */
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Tạm thời sử dụng NoOpPasswordEncoder (không encode)
        // CHỈ DÙNG CHO TESTING!
        return NoOpPasswordEncoder.getInstance();
    }
}
