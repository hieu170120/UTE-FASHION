package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    
    Optional<Coupon> findByCouponCodeAndIsActiveTrue(String couponCode);
    
    // Tìm coupon còn hiệu lực
    Optional<Coupon> findByCouponCodeAndIsActiveTrueAndValidFromBeforeAndValidToAfter(
        String couponCode, 
        LocalDateTime now1, 
        LocalDateTime now2
    );
    
    // Tìm tất cả coupon của vendor
    List<Coupon> findByVendorUserIdOrderByCreatedAtDesc(Integer vendorId);
    
    // Tìm coupon của vendor theo code
    Optional<Coupon> findByCouponCodeAndVendorUserId(String couponCode, Integer vendorId);
}
