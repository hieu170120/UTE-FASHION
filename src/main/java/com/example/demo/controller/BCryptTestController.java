package com.example.demo.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để test BCrypt hash generation
 */
@RestController
@RequestMapping("/api/test")
public class BCryptTestController {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Tạo BCrypt hash cho password
     */
    @GetMapping("/hash/{password}")
    public Map<String, Object> generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("hashLength", hash.length());
        result.put("matches", matches);
        result.put("algorithm", "BCrypt");
        result.put("strength", 10);
        
        return result;
    }
    
    /**
     * Test hash với password cụ thể
     */
    @GetMapping("/test/user123")
    public Map<String, Object> testUser123() {
        String password = "user123";
        String hash = passwordEncoder.encode(password);
        
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("hashLength", hash.length());
        result.put("matches", passwordEncoder.matches(password, hash));
        result.put("algorithm", "BCrypt");
        result.put("strength", 10);
        
        // Tạo thêm một vài hash khác để demo
        Map<String, String> multipleHashes = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            String newHash = passwordEncoder.encode(password);
            multipleHashes.put("hash_" + i, newHash);
        }
        result.put("multipleHashes", multipleHashes);
        
        return result;
    }
}

