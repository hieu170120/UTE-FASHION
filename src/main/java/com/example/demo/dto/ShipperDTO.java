package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipperDTO {
    private Integer id;

    @NotNull(message = "Nhà vận chuyển không được để trống")
    private Integer carrierId;

    private Integer userId; // Nếu liên kết với User

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String vehicleType;

    private boolean active = true;

    private int cancelCount = 0; // Để theo dõi số lần hủy đơn
}