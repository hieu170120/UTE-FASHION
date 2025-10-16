package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarrierDTO {
    private Integer id;

    @NotBlank(message = "Tên nhà vận chuyển không được để trống")
    private String carrierName;

    private String description;

    @PositiveOrZero(message = "Phí vận chuyển phải >= 0")
    private BigDecimal defaultShippingFee = BigDecimal.ZERO;

    private String contactPhone;
    private String websiteUrl;
    private boolean active = true;
}