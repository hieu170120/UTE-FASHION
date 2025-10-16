package com.example.demo.service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý quên mật khẩu và gửi OTP qua email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    // OTP hết hạn sau 15 phút
    private static final int OTP_EXPIRATION_MINUTES = 15;
    
    // Giới hạn số lần gửi OTP trong 1 giờ
    private static final int MAX_OTP_ATTEMPTS_PER_HOUR = 3;
    
    /**
     * Gửi OTP để reset mật khẩu
     */
    @Transactional
    public Map<String, Object> sendResetPasswordOtp(String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống");
                return response;
            }
            
            // Normalize email
            email = email.trim().toLowerCase();
            
            log.info("Sending reset password OTP to email: {}", email);
            
            // Kiểm tra email có tồn tại không
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("Email not found: {}", email);
                response.put("success", false);
                response.put("message", "Email không tồn tại trong hệ thống");
                return response;
            }
            
            User user = userOpt.get();
            log.info("Found user: ID={}, Username={}", user.getUserId(), user.getUsername());
            
            // Kiểm tra số lần gửi OTP trong 1 giờ qua
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long otpCount = passwordResetTokenRepository.countUnusedTokensInLastHour(email, oneHourAgo);
            
            if (otpCount >= MAX_OTP_ATTEMPTS_PER_HOUR) {
                log.warn("Too many OTP requests for email: {}, count: {}", email, otpCount);
                response.put("success", false);
                response.put("message", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 1 giờ");
                return response;
            }
            
            // Xóa các token cũ của user này (chỉ xóa những token chưa sử dụng)
            passwordResetTokenRepository.deleteTokensByUserId(user.getUserId());
            log.info("Deleted old tokens for user: {}", user.getUserId());
            
            // Tạo OTP mới
            String otpCode = generateOTP();
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
            
            // Lưu token vào database
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setOtpCode(otpCode);
            passwordResetToken.setEmail(email);
            passwordResetToken.setExpiresAt(expiresAt);
            passwordResetToken.setUser(user);
            passwordResetTokenRepository.save(passwordResetToken);
            
            log.info("Created new token: ID={}, OTP={}, ExpiresAt={}", 
                passwordResetToken.getTokenId(), otpCode, expiresAt);
            
            // Gửi email
            sendOtpEmail(email, user.getFullName(), otpCode);
            
            log.info("OTP sent successfully to email: {}", email);
            
            response.put("success", true);
            response.put("message", "Mã OTP đã được gửi đến email của bạn");
            response.put("expiresInMinutes", OTP_EXPIRATION_MINUTES);
            
        } catch (Exception e) {
            log.error("Error sending OTP to email: {}", email, e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi gửi email. Vui lòng thử lại sau");
        }
        
        return response;
    }
    
    /**
     * Xác thực OTP và reset mật khẩu
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> resetPasswordWithOtp(String email, String otpCode, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không được để trống");
                return response;
            }
            
            if (otpCode == null || otpCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mã OTP không được để trống");
                return response;
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới không được để trống");
                return response;
            }
            
            // Normalize input
            email = email.trim().toLowerCase();
            otpCode = otpCode.trim();
            
            log.info("Attempting password reset for email: {}, OTP: {}", email, otpCode);
            
            // Tìm token theo OTP và email
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository
                .findByOtpCodeAndEmailAndIsUsedFalse(otpCode, email);
            
            if (tokenOpt.isEmpty()) {
                log.warn("No valid token found for email: {}, OTP: {}", email, otpCode);
                response.put("success", false);
                response.put("message", "Mã OTP không hợp lệ hoặc đã được sử dụng");
                return response;
            }
            
            PasswordResetToken token = tokenOpt.get();
            log.info("Found token: ID={}, ExpiresAt={}, IsUsed={}", 
                token.getTokenId(), token.getExpiresAt(), token.getIsUsed());
            
            // Kiểm tra token có hết hạn không
            if (token.isExpired()) {
                log.warn("Token expired for email: {}, ExpiresAt: {}", email, token.getExpiresAt());
                response.put("success", false);
                response.put("message", "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới");
                return response;
            }
            
            // Tìm user
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.error("User not found for email: {}", email);
                response.put("success", false);
                response.put("message", "Email không tồn tại trong hệ thống");
                return response;
            }
            
            User user = userOpt.get();
            log.info("Found user: ID={}, Username={}", user.getUserId(), user.getUsername());
            
            // Cập nhật mật khẩu mới
            String hashedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(newPassword, 
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
            
            log.info("Generated hashed password for user: {}", user.getUsername());
            log.info("Old password hash: {}", user.getPasswordHash());
            
            user.setPasswordHash(hashedPassword);
            
            log.info("New password hash: {}", user.getPasswordHash());
            
            // Save user với password mới
            User savedUser = userRepository.save(user);
            log.info("User saved successfully: ID={}, Password updated: {}", 
                savedUser.getUserId(), savedUser.getPasswordHash() != null);
            
            // Verify password was actually saved by querying database again
            Optional<User> verifyUser = userRepository.findByEmail(email);
            if (verifyUser.isPresent()) {
                User dbUser = verifyUser.get();
                log.info("Password verification - DB password hash: {}", dbUser.getPasswordHash());
                log.info("Password verification - Matches new hash: {}", 
                    dbUser.getPasswordHash().equals(hashedPassword));
            }
            
            // Đánh dấu token đã sử dụng
            token.markAsUsed();
            PasswordResetToken savedToken = passwordResetTokenRepository.save(token);
            log.info("Token marked as used: ID={}, IsUsed={}", 
                savedToken.getTokenId(), savedToken.getIsUsed());
            
            log.info("Password reset successfully for user: {} (ID: {})", user.getUsername(), user.getUserId());
            
            // Verify password was actually updated
            boolean passwordVerified = verifyPasswordUpdate(email, newPassword);
            if (!passwordVerified) {
                log.error("Password verification failed for user: {}", email);
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi cập nhật mật khẩu. Vui lòng thử lại");
                return response;
            }
            
            response.put("success", true);
            response.put("message", "Đặt lại mật khẩu thành công");
            
        } catch (Exception e) {
            log.error("Error resetting password for email: {}", email, e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại");
        }
        
        return response;
    }
    
    /**
     * Tạo mã OTP 6 chữ số
     */
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Tạo số từ 100000 đến 999999
        return String.valueOf(otp);
    }
    
    /**
     * Gửi email chứa mã OTP
     */
    private void sendOtpEmail(String toEmail, String fullName, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mã OTP đặt lại mật khẩu - UTE Fashion");
            
            String emailContent = String.format("""
                Xin chào %s,
                
                Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản UTE Fashion.
                
                Mã OTP của bạn là: %s
                
                Mã này có hiệu lực trong 15 phút.
                
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Đội ngũ UTE Fashion
                """, fullName, otpCode);
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }
    
    /**
     * Dọn dẹp các token đã hết hạn (có thể gọi định kỳ)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }
    
    /**
     * Kiểm tra password có được cập nhật đúng không
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyPasswordUpdate(String email, String newPassword) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.error("User not found for verification: {}", email);
                return false;
            }
            
            User user = userOpt.get();
            
            // Sử dụng BCrypt.checkpw để verify password với hash đã lưu
            boolean passwordMatches = org.springframework.security.crypto.bcrypt.BCrypt.checkpw(newPassword, user.getPasswordHash());
            log.info("Password verification for {}: matches={}", email, passwordMatches);
            log.info("Stored hash: {}", user.getPasswordHash());
            
            return passwordMatches;
            
        } catch (Exception e) {
            log.error("Error verifying password for email: {}", email, e);
            return false;
        }
    }
}