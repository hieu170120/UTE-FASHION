package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Authentication Response (trả về JWT token)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    
    public AuthResponse(String token, Integer userId, String username, String email, String fullName) {
        this.token = token;
        this.type = "Bearer";
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }
}







