package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopRegistrationDTO {

    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên cửa hàng phải có từ 3 đến 100 ký tự")
    private String shopName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private String logoUrl;

}
