package com.example.demo.repository;

import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository cho Role
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Tìm role theo tên
     */
    Optional<Role> findByRoleName(String roleName);
    
    /**
     * Kiểm tra role name đã tồn tại chưa
     */
    boolean existsByRoleName(String roleName);
}



