package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    
    private Integer id;
    
    @NotBlank(message = "Mã coupon không được để trống")
    @Size(max = 50, message = "Mã coupon không quá 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã coupon chỉ chứa chữ cái, số, gạch ngang và gạch dưới")
    private String couponCode;
    
    @Size(max = 500, message = "Mô tả không quá 500 ký tự")
    private String description;
    
    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENTAGE, FIXED
    
    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0", message = "Giá trị đơn hàng tối thiểu phải >= 0")
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    
    @DecimalMin(value = "0", message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal maxDiscountAmount;
    
    @Min(value = 1, message = "Giới hạn sử dụng phải >= 1")
    private Integer usageLimit;
    
    private Integer usageCount = 0;
    
    @Min(value = 1, message = "Giới hạn sử dụng/người phải >= 1")
    private Integer usageLimitPerUser = 1;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime validTo;
    
    private boolean active = true;
    
    private boolean shippingDiscount = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
