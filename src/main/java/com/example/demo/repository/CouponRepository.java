package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}
