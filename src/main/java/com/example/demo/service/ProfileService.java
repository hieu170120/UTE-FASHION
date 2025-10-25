package com.example.demo.service;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic nghiệp vụ cho Profile
 */
@Service
public class ProfileService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Lấy thông tin profile của user
     */
    public ProfileDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return convertToProfileDTO(user);
    }
    
    /**
     * Cập nhật thông tin profile
     */
    @Transactional
    public ProfileDTO updateProfile(String username, UpdateProfileDTO updateProfileDTO) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Kiểm tra và cập nhật từng trường chỉ khi có giá trị
        if (updateProfileDTO.getFullName() != null && !updateProfileDTO.getFullName().trim().isEmpty()) {
            user.setFullName(updateProfileDTO.getFullName().trim());
        }
        
        if (updateProfileDTO.getEmail() != null && !updateProfileDTO.getEmail().trim().isEmpty()) {
            String newEmail = updateProfileDTO.getEmail().trim();
            // Kiểm tra email có bị trùng với user khác không
            if (!user.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
                }
                user.setEmail(newEmail);
            }
        }
        
        if (updateProfileDTO.getPhoneNumber() != null) {
            // Cho phép xóa số điện thoại (để trống)
            user.setPhoneNumber(updateProfileDTO.getPhoneNumber().trim().isEmpty() ? null : updateProfileDTO.getPhoneNumber().trim());
        }
        
        // Luôn cập nhật avatar URL nếu có giá trị
        System.out.println("DEBUG: updateProfileDTO.getAvatarUrl() = '" + updateProfileDTO.getAvatarUrl() + "'");
        if (updateProfileDTO.getAvatarUrl() != null && !updateProfileDTO.getAvatarUrl().trim().isEmpty()) {
            String newAvatarUrl = updateProfileDTO.getAvatarUrl().trim();
            System.out.println("DEBUG: Updating avatar URL from '" + user.getAvatarUrl() + "' to '" + newAvatarUrl + "'");
            user.setAvatarUrl(newAvatarUrl);
        } else {
            System.out.println("DEBUG: Avatar URL is null or empty in updateProfileDTO");
        }
        
        System.out.println("DEBUG: Saving user to database...");
        User savedUser = userRepository.save(user);
        System.out.println("DEBUG: User saved successfully. Avatar URL in DB: " + savedUser.getAvatarUrl());
        return convertToProfileDTO(savedUser);
    }
    
    /**
     * Đổi mật khẩu
     */
    @Transactional
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }
        
        // Kiểm tra mật khẩu mới và xác nhận
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }
        
        // Kiểm tra mật khẩu mới khác mật khẩu hiện tại
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        
        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * Chuyển đổi User entity thành ProfileDTO
     */
    private ProfileDTO convertToProfileDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getUserId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setFullName(user.getFullName());
        profileDTO.setPhoneNumber(user.getPhoneNumber());
        profileDTO.setAvatarUrl(user.getAvatarUrl());
        profileDTO.setIsActive(user.getIsActive());
        profileDTO.setIsEmailVerified(user.getIsEmailVerified());
        profileDTO.setLastLogin(user.getLastLogin());
        profileDTO.setCreatedAt(user.getCreatedAt());
        profileDTO.setUpdatedAt(user.getUpdatedAt());
        return profileDTO;
    }
}
