package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopDTO {
    private Integer id;
    private Integer vendorId;
    private String shopName;
    private String slug;
    private String description;
    private String logoUrl;
    private boolean isActive;
    private BigDecimal commissionPercentage;
}
