package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service để test BCrypt password encoding
 */
@Service
public class PasswordTestService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Test BCrypt hash cho password admin123
     */
    public void testPasswordHash() {
        String password = "admin123";
        
        // Tạo hash mới
        String newHash = passwordEncoder.encode(password);
        
        // Test với hash cũ
        String oldHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        // Test với hash từ log
        String logHash = "$2a$10$KTXSVi1.Xeq0uDRApemZ1.ovyd4kS.jfWon.hs413DPVhqTLSBAh6";
        
        System.out.println("=== PASSWORD HASH TEST ===");
        System.out.println("Original password: " + password);
        System.out.println("Generated hash: " + newHash);
        System.out.println("Matches test: " + passwordEncoder.matches(password, newHash));
        System.out.println("Old hash matches: " + passwordEncoder.matches(password, oldHash));
        System.out.println("Log hash matches: " + passwordEncoder.matches(password, logHash));
        System.out.println("=========================");
    }
}