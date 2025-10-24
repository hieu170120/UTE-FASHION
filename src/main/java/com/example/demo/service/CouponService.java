package com.example.demo.service;

import com.example.demo.entity.Coupon;
import com.example.demo.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * Validate coupon code
     * @return Coupon nếu hợp lệ, throw Exception nếu không
     */
    public Coupon validateCoupon(String couponCode, BigDecimal orderTotal) {
        LocalDateTime now = LocalDateTime.now();
        
        Coupon coupon = couponRepository.findByCouponCodeAndIsActiveTrueAndValidFromBeforeAndValidToAfter(
                couponCode, now, now)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại hoặc đã hết hạn"));

        // Kiểm tra giá trị đơn hàng tối thiểu
        if (orderTotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu " + 
                formatMoney(coupon.getMinOrderValue()) + " để áp dụng mã này");
        }

        // Kiểm tra usage limit
        if (coupon.getUsageLimit() != null && 
            coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        return coupon;
    }

    /**
     * Tính số tiền giảm giá
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        BigDecimal discount = BigDecimal.ZERO;
        
        String discountType = coupon.getDiscountType();
        if (discountType == null) {
            return BigDecimal.ZERO;
        }

        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            // Giảm theo %
            discount = orderTotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // Áp dụng max discount nếu có
            if (coupon.getMaxDiscountAmount() != null && 
                discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else if ("FIXED".equalsIgnoreCase(discountType)) {
            // Giảm số tiền cố định
            discount = coupon.getDiscountValue();
        }

        // Không giảm quá tổng tiền
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }

    /**
     * Format tiền
     */
    private String formatMoney(BigDecimal amount) {
        return String.format("%,d₫", amount.longValue());
    }

    /**
     * Lấy coupon theo code (không validate)
     */
    public Coupon getCouponByCode(String couponCode) {
        return couponRepository.findByCouponCodeAndIsActiveTrue(couponCode)
                .orElse(null);
    }
    
    /**
     * Lấy danh sách coupons đang active và còn hiệu lực
     */
    public java.util.List<Coupon> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAll().stream()
                .filter(c -> c.isActive() && 
                           c.getValidFrom().isBefore(now) && 
                           c.getValidTo().isAfter(now) &&
                           (c.getUsageLimit() == null || c.getUsageCount() < c.getUsageLimit()))
                .collect(java.util.stream.Collectors.toList());
    }
}
