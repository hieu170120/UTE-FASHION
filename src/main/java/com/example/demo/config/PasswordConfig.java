package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration cho Password Encoding
 * Sử dụng BCrypt để mã hóa password an toàn
 */
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng BCrypt với strength = 10 (mặc định)
        return new BCryptPasswordEncoder();
    }
}



