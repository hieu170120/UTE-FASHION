package com.example.demo.service;

import com.example.demo.entity.EmailVerification;
import com.example.demo.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Service xử lý gửi email OTP
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    
    @Value("${app.email.from:noreply@utefashion.com}")
    private String fromEmail;
    
    @Value("${app.email.max-attempts:5}")
    private int maxAttempts;
    
    /**
     * Tạo mã OTP 6 số
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    /**
     * Gửi mã OTP đến email
     */
    @Transactional
    public void sendOTP(String email) {
        // Kiểm tra số lần gửi trong 1 giờ qua
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentCount = emailVerificationRepository.countRecentVerifications(email, oneHourAgo);
        
        if (recentCount >= maxAttempts) {
            throw new RuntimeException("Bạn đã gửi quá nhiều mã OTP. Vui lòng thử lại sau 1 giờ.");
        }
        
        // Xóa verification cũ nếu có
        Optional<EmailVerification> existingVerification = emailVerificationRepository.findActiveByEmail(email, LocalDateTime.now());
        if (existingVerification.isPresent()) {
            emailVerificationRepository.delete(existingVerification.get());
        }
        
        // Tạo mã OTP mới
        String otpCode = generateOTP();
        
        // Lưu vào database
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setOtpCode(otpCode);
        verification.setIsVerified(false);
        emailVerificationRepository.save(verification);
        
        // Gửi email
        try {
            sendEmail(email, otpCode);
            log.info("OTP sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP to: {}", email, e);
            throw new RuntimeException("Không thể gửi email. Vui lòng kiểm tra lại địa chỉ email.");
        }
    }
    
    /**
     * Gửi email chứa mã OTP
     */
    private void sendEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã xác thực email - UTE Fashion");
        message.setText(buildEmailContent(otpCode));
        
        mailSender.send(message);
    }
    
    /**
     * Xây dựng nội dung email
     */
    private String buildEmailContent(String otpCode) {
        return String.format("""
            Chào bạn!
            
            Cảm ơn bạn đã đăng ký tài khoản tại UTE Fashion.
            
            Mã xác thực email của bạn là: %s
            
            Mã này có hiệu lực trong 15 phút.
            
            Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
            
            Trân trọng,
            Đội ngũ UTE Fashion
            """, otpCode);
    }
    
    /**
     * Xác thực mã OTP
     */
    @Transactional
    public boolean verifyOTP(String email, String otpCode) {
        Optional<EmailVerification> verification = emailVerificationRepository
            .findByEmailAndOtpCode(email, otpCode, LocalDateTime.now());
        
        if (verification.isPresent()) {
            EmailVerification ev = verification.get();
            ev.setIsVerified(true);
            ev.setVerifiedAt(LocalDateTime.now());
            emailVerificationRepository.save(ev);
            return true;
        }
        
        return false;
    }
    
    /**
     * Kiểm tra email đã được xác thực chưa
     */
    public boolean isEmailVerified(String email) {
        Optional<EmailVerification> verification = emailVerificationRepository
            .findActiveByEmail(email, LocalDateTime.now());
        
        return verification.isPresent() && verification.get().getIsVerified();
    }
    
    /**
     * Dọn dẹp các verification đã hết hạn
     */
    @Transactional
    public void cleanupExpiredVerifications() {
        emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        log.info("Cleaned up expired email verifications");
    }
}

