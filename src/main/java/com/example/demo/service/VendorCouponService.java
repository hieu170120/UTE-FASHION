package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.User;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorCouponService {
    
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    
    public List<CouponDTO> getAllVendorCoupons() {
        User currentVendor = getCurrentVendor();
        return couponRepository.findByVendorUserIdOrderByCreatedAtDesc(currentVendor.getUserId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CouponDTO getCouponById(Integer id) {
        User currentVendor = getCurrentVendor();
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon"));
        
        if (coupon.getVendor() == null || !coupon.getVendor().getUserId().equals(currentVendor.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập coupon này");
        }
        
        return convertToDTO(coupon);
    }
    
    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        User currentVendor = getCurrentVendor();
        
        // Kiểm tra mã coupon đã tồn tại chưa
        if (couponRepository.findByCouponCodeAndVendorUserId(dto.getCouponCode(), currentVendor.getUserId()).isPresent()) {
            throw new IllegalArgumentException("Mã coupon đã tồn tại");
        }
        
        // Validate dates
        if (dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Validate discount percentage
        if ("PERCENTAGE".equals(dto.getDiscountType()) && dto.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phần trăm không được vượt quá 100%");
        }
        
        Coupon coupon = new Coupon();
        coupon.setCouponCode(dto.getCouponCode().toUpperCase());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setUsageLimitPerUser(dto.getUsageLimitPerUser());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidTo(dto.getValidTo());
        coupon.setActive(dto.isActive());
        coupon.setShippingDiscount(dto.isShippingDiscount());
        coupon.setVendor(currentVendor);
        coupon.setUsageCount(0);
        
        Coupon saved = couponRepository.save(coupon);
        return convertToDTO(saved);
    }
    
    @Transactional
    public CouponDTO updateCoupon(Integer id, CouponDTO dto) {
        User currentVendor = getCurrentVendor();
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon"));
        
        if (coupon.getVendor() == null || !coupon.getVendor().getUserId().equals(currentVendor.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa coupon này");
        }
        
        // Check if code is changed and already exists
        if (!coupon.getCouponCode().equals(dto.getCouponCode())) {
            if (couponRepository.findByCouponCodeAndVendorUserId(dto.getCouponCode(), currentVendor.getUserId()).isPresent()) {
                throw new IllegalArgumentException("Mã coupon đã tồn tại");
            }
            coupon.setCouponCode(dto.getCouponCode().toUpperCase());
        }
        
        // Validate dates
        if (dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Validate discount percentage
        if ("PERCENTAGE".equals(dto.getDiscountType()) && dto.getDiscountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phần trăm không được vượt quá 100%");
        }
        
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setUsageLimitPerUser(dto.getUsageLimitPerUser());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidTo(dto.getValidTo());
        coupon.setActive(dto.isActive());
        coupon.setShippingDiscount(dto.isShippingDiscount());
        
        Coupon updated = couponRepository.save(coupon);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteCoupon(Integer id) {
        User currentVendor = getCurrentVendor();
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon"));
        
        if (coupon.getVendor() == null || !coupon.getVendor().getUserId().equals(currentVendor.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền xóa coupon này");
        }
        
        couponRepository.delete(coupon);
    }
    
    @Transactional
    public void toggleCouponStatus(Integer id) {
        User currentVendor = getCurrentVendor();
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon"));
        
        if (coupon.getVendor() == null || !coupon.getVendor().getUserId().equals(currentVendor.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền thay đổi trạng thái coupon này");
        }
        
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }
    
    private User getCurrentVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin người dùng"));
    }
    
    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setCouponCode(coupon.getCouponCode());
        dto.setDescription(coupon.getDescription());
        dto.setDiscountType(coupon.getDiscountType());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setMinOrderValue(coupon.getMinOrderValue());
        dto.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
        dto.setUsageLimit(coupon.getUsageLimit());
        dto.setUsageCount(coupon.getUsageCount());
        dto.setUsageLimitPerUser(coupon.getUsageLimitPerUser());
        dto.setValidFrom(coupon.getValidFrom());
        dto.setValidTo(coupon.getValidTo());
        dto.setActive(coupon.isActive());
        dto.setShippingDiscount(coupon.isShippingDiscount());
        dto.setCreatedAt(coupon.getCreatedAt());
        dto.setUpdatedAt(coupon.getUpdatedAt());
        return dto;
    }
}
