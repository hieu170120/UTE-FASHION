package com.example.demo.repository;

import com.example.demo.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository cho EmailVerification
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    
    /**
     * Tìm verification chưa hết hạn theo email
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.expiresAt > :now ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findActiveByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    /**
     * Tìm verification theo email và OTP code
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.otpCode = :otpCode AND ev.expiresAt > :now")
    Optional<EmailVerification> findByEmailAndOtpCode(@Param("email") String email, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);
    
    /**
     * Xóa các verification đã hết hạn
     */
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
    
    /**
     * Đếm số lần gửi OTP trong 1 giờ qua
     */
    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE ev.email = :email AND ev.createdAt > :oneHourAgo")
    long countRecentVerifications(@Param("email") String email, @Param("oneHourAgo") LocalDateTime oneHourAgo);
}

