package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity PasswordResetToken - Lưu trữ thông tin reset password với OTP
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;
    
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;
    
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_used")
    private Boolean isUsed = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    // Relationship với User (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Kiểm tra token có hết hạn không
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Kiểm tra token có hợp lệ không (chưa sử dụng và chưa hết hạn)
     */
    public boolean isValid() {
        return !isUsed && !isExpired();
    }
    
    /**
     * Đánh dấu token đã được sử dụng
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}
