package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service để load user từ database cho Spring Security.
 * Service này sẽ trả về một CustomUserDetails chứa đầy đủ thông tin user.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Tìm kiếm User từ database dựa trên username (hoặc email) và trả về
     * một đối tượng CustomUserDetails mà Spring Security có thể sử dụng.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Logic tìm kiếm user vẫn giữ nguyên.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        // Thay vì tự build một đối tượng UserDetails chung, ta tạo CustomUserDetails.
        // Mọi logic về kiểm tra trạng thái active và roles đã được đóng gói trong CustomUserDetails,
        // giúp cho service này gọn gàng và tuân thủ đúng nguyên tắc Single Responsibility.
        return new CustomUserDetails(user);
    }
}
