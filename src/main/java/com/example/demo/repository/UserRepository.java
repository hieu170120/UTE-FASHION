package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository cho User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * Tìm user theo username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Kiểm tra username đã tồn tại chưa
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Tìm kiếm user theo username, email hoặc fullName
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        @Param("keyword") String keyword1, 
        @Param("keyword") String keyword2, 
        @Param("keyword") String keyword3, 
        Pageable pageable);
    
    /**
     * Lọc user theo trạng thái active
     */
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    /**
     * Đếm user theo trạng thái active
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Đếm user được tạo sau thời điểm nhất định
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
}
