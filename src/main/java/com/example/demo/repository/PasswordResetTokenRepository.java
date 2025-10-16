package com.example.demo.repository;

import com.example.demo.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository cho PasswordResetToken
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Tìm token theo token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Tìm token theo email và chưa sử dụng
     */
    Optional<PasswordResetToken> findByEmailAndIsUsedFalse(String email);
    
    /**
     * Tìm token theo OTP code và email
     */
    Optional<PasswordResetToken> findByOtpCodeAndEmailAndIsUsedFalse(String otpCode, String email);
    
    /**
     * Xóa các token đã hết hạn
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Xóa các token của user cụ thể
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.user.userId = :userId")
    void deleteTokensByUserId(@Param("userId") Integer userId);
    
    /**
     * Đếm số lượng token chưa sử dụng của một email trong 1 giờ qua
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.email = :email AND p.isUsed = false AND p.createdAt > :oneHourAgo")
    long countUnusedTokensInLastHour(@Param("email") String email, @Param("oneHourAgo") LocalDateTime oneHourAgo);
}

