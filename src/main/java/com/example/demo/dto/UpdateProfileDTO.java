package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO cho cập nhật thông tin profile
 */
public class UpdateProfileDTO {
    
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String email;
    
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;
    
    @Size(max = 500, message = "URL avatar không được quá 500 ký tự")
    private String avatarUrl;
    
    public UpdateProfileDTO() {}
    
    public UpdateProfileDTO(String fullName, String email, String phoneNumber, String avatarUrl) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
