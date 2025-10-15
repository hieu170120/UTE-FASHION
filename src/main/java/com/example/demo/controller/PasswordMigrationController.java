package com.example.demo.controller;

import com.example.demo.util.PasswordHashUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để cập nhật password hash (CHỈ DÙNG CHO MIGRATION)
 * XÓA CONTROLLER NÀY SAU KHI ĐÃ CẬP NHẬT XONG!
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PasswordMigrationController {
    
    private final PasswordHashUpdater passwordHashUpdater;
    
    /**
     * Cập nhật password hash cho tất cả user
     * CHỈ CHẠY MỘT LẦN!
     */
    @PostMapping("/migrate-passwords")
    public Map<String, String> migrateAllPasswords() {
        try {
            passwordHashUpdater.updateAllPasswordHashes();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Password hash migration completed successfully!");
            return response;
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Migration failed: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Cập nhật password hash cho một user cụ thể
     */
    @PostMapping("/migrate-password/{username}")
    public Map<String, String> migrateUserPassword(
            @PathVariable String username,
            @RequestParam String password) {
        
        try {
            passwordHashUpdater.updatePasswordHashForUser(username, password);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Password hash updated for user: " + username);
            return response;
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Update failed: " + e.getMessage());
            return response;
        }
    }
}


