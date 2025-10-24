package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRequest {
    private BigDecimal commissionPercentage;
    private String reason; // Lý do thay đổi chiết khấu (tùy chọn)
}
