package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO cho Login Request
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    private Boolean rememberMe = false;
}
