package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.PendingUser;
import com.example.demo.entity.User;
import com.example.demo.repository.PendingUserRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý Authentication với JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PendingUserRepository pendingUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordTestService passwordTestService;
    private final EmailService emailService;
    
    /**
     * Đăng ký user mới và gửi OTP xác thực email (lưu tạm thời vào PendingUser)
     */
    @Transactional
    public PendingUser register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại trong User hoặc PendingUser
        if (userRepository.existsByUsername(request.getUsername()) || 
            pendingUserRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại trong User hoặc PendingUser
        if (userRepository.existsByEmail(request.getEmail()) || 
            pendingUserRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Kiểm tra password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        
        // Xóa pending user cũ nếu có (trường hợp đăng ký lại)
        pendingUserRepository.findByEmail(request.getEmail()).ifPresent(pendingUserRepository::delete);
        
        // Tạo pending user mới
        PendingUser pendingUser = new PendingUser();
        pendingUser.setUsername(request.getUsername());
        pendingUser.setEmail(request.getEmail());
        pendingUser.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Mã hóa password bằng BCrypt
        pendingUser.setFullName(request.getFullName());
        pendingUser.setPhoneNumber(request.getPhoneNumber());
        pendingUser.setIsOtpSent(false);
        
        PendingUser savedPendingUser = pendingUserRepository.save(pendingUser);
        
        // Gửi OTP xác thực email
        try {
            emailService.sendOTP(request.getEmail());
            savedPendingUser.setIsOtpSent(true);
            pendingUserRepository.save(savedPendingUser);
        } catch (Exception e) {
            // Nếu gửi email thất bại, xóa pending user đã tạo
            pendingUserRepository.delete(savedPendingUser);
            throw new RuntimeException("Không thể gửi mã xác thực email: " + e.getMessage());
        }
        
        return savedPendingUser;
    }
    
    /**
     * Xác thực email và chuyển từ PendingUser sang User
     */
    @Transactional
    public User verifyEmail(String email, String otpCode) {
        // Xác thực OTP
        boolean isValidOTP = emailService.verifyOTP(email, otpCode);
        
        if (isValidOTP) {
            // Tìm pending user
            PendingUser pendingUser = pendingUserRepository.findActiveByEmail(email, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký với email: " + email));
            
            // Tạo user mới từ pending user
            User user = new User();
            user.setUsername(pendingUser.getUsername());
            user.setEmail(pendingUser.getEmail());
            user.setPasswordHash(pendingUser.getPasswordHash());
            user.setFullName(pendingUser.getFullName());
            user.setPhoneNumber(pendingUser.getPhoneNumber());
            user.setIsActive(true);
            user.setIsEmailVerified(true); // Đã xác thực email
            
            // Lưu user mới
            User savedUser = userRepository.save(user);
            
            // Xóa pending user
            pendingUserRepository.delete(pendingUser);
            
            return savedUser;
        }
        
        throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn");
    }
    
    /**
     * Đăng nhập và tạo JWT token
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Xác thực username và password
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // Tìm user
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra account active
        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        
        // Cập nhật last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Tạo JWT token
        String token = jwtUtil.generateToken(user.getUsername());
        
        // Trả về response với token
        return new AuthResponse(
            token,
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName()
        );
    }
    
    /**
     * Đăng nhập (phiên bản cũ, trả về User)
     */
    @Transactional
    public User loginUser(LoginRequest request) {
        // Test password hash
        passwordTestService.testPasswordHash();
        
        // Tìm user theo username
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        User user = userOpt.get();
        
        // Kiểm tra account active
        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        
        // Kiểm tra password bằng BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        // Cập nhật last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        return user;
    }
    
    /**
     * Tìm user theo username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

