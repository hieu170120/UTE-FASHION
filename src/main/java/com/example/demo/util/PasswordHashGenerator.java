package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class để generate password hash cho testing
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        System.out.println("=== PASSWORD HASH GENERATOR ===");
        
        // Generate hash cho admin123
        String adminPassword = "admin123";
        String adminHash = passwordEncoder.encode(adminPassword);
        System.out.println("Password: " + adminPassword);
        System.out.println("Hash: " + adminHash);
        System.out.println("Verify: " + passwordEncoder.matches(adminPassword, adminHash));
        System.out.println();
        
        // Generate hash cho user123
        String userPassword = "user123";
        String userHash = passwordEncoder.encode(userPassword);
        System.out.println("Password: " + userPassword);
        System.out.println("Hash: " + userHash);
        System.out.println("Verify: " + passwordEncoder.matches(userPassword, userHash));
        System.out.println();
        
        // Test với hash cũ
        String oldHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("Old hash test:");
        System.out.println("Hash: " + oldHash);
        System.out.println("Matches admin123: " + passwordEncoder.matches(adminPassword, oldHash));
        System.out.println("Matches user123: " + passwordEncoder.matches(userPassword, oldHash));
        
        System.out.println("\n=== SQL INSERT STATEMENTS ===");
        System.out.println("-- Admin account");
        System.out.println("INSERT INTO Users (username, email, password_hash, full_name, phone_number, is_active, is_email_verified) VALUES");
        System.out.println("('admin', 'admin@utefashion.com', '" + adminHash + "', 'Administrator', '0123456789', 1, 1);");
        System.out.println();
        System.out.println("-- User account");
        System.out.println("INSERT INTO Users (username, email, password_hash, full_name, phone_number, is_active, is_email_verified) VALUES");
        System.out.println("('user1', 'user1@example.com', '" + userHash + "', 'User Test 1', '0987654321', 1, 1);");
    }
}






