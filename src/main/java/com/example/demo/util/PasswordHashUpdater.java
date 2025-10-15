package com.example.demo.util;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class để cập nhật password hash cho các user hiện tại
 * CHỈ CHẠY MỘT LẦN để migrate từ plain text sang BCrypt
 */
@Component
@RequiredArgsConstructor
public class PasswordHashUpdater {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Cập nhật password hash cho tất cả user có password dạng plain text
     * CHỈ CHẠY MỘT LẦN!
     */
    public void updateAllPasswordHashes() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String currentPasswordHash = user.getPasswordHash();
            
            // Kiểm tra nếu password chưa được mã hóa (không có BCrypt prefix)
            if (!currentPasswordHash.startsWith("$2a$") && !currentPasswordHash.startsWith("$2b$")) {
                System.out.println("Updating password hash for user: " + user.getUsername());
                
                // Mã hóa password hiện tại bằng BCrypt
                String newPasswordHash = passwordEncoder.encode(currentPasswordHash);
                user.setPasswordHash(newPasswordHash);
                userRepository.save(user);
                
                System.out.println("Updated password hash for user: " + user.getUsername());
            }
        }
        
        System.out.println("Password hash update completed!");
    }
    
    /**
     * Tạo password hash mới cho một user cụ thể
     */
    public void updatePasswordHashForUser(String username, String plainPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        String newPasswordHash = passwordEncoder.encode(plainPassword);
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
        
        System.out.println("Updated password hash for user: " + username);
    }
}


