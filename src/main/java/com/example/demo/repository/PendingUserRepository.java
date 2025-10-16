package com.example.demo.repository;

import com.example.demo.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository cho PendingUser
 */
@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Integer> {
    
    /**
     * Tìm pending user theo email
     */
    Optional<PendingUser> findByEmail(String email);
    
    /**
     * Tìm pending user theo username
     */
    Optional<PendingUser> findByUsername(String username);
    
    /**
     * Kiểm tra username đã tồn tại trong pending users
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra email đã tồn tại trong pending users
     */
    boolean existsByEmail(String email);
    
    /**
     * Tìm pending user chưa hết hạn theo email
     */
    @Query("SELECT pu FROM PendingUser pu WHERE pu.email = :email AND pu.expiresAt > :now")
    Optional<PendingUser> findActiveByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    /**
     * Xóa các pending user đã hết hạn
     */
    @Query("DELETE FROM PendingUser pu WHERE pu.expiresAt < :now")
    void deleteExpiredPendingUsers(@Param("now") LocalDateTime now);
}

