package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Test class để kiểm tra password hash
 */
@Component
public class PasswordTestService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void testPasswordHash() {
        String password = "admin123";
        String hash = passwordEncoder.encode(password);
        
        System.out.println("=== PASSWORD HASH TEST ===");
        System.out.println("Original password: " + password);
        System.out.println("Generated hash: " + hash);
        System.out.println("Matches test: " + passwordEncoder.matches(password, hash));
        
        // Test với hash trong database
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("DB hash matches: " + passwordEncoder.matches(password, dbHash));
        System.out.println("=========================");
    }
}

