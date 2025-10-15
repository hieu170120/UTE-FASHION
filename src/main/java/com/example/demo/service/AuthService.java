package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordTestService passwordTestService;
    
    /**
     * Đăng ký user mới
     */
    @Transactional
    public User register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Kiểm tra password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        
        // Tạo user mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Mã hóa password bằng BCrypt
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        
        return userRepository.save(user);
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

