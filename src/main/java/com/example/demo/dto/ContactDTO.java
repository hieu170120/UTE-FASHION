package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {
    private Integer contactId;
    private Integer shopId;
    private Integer userId;
    private String userFullName; // Tên người gửi
    private String userAvatar;   // Avatar người gửi để hiển thị cho đẹp
    private String fullName;     // Tên nhập trong form (nếu là guest)
    private String email;
    private String phoneNumber;
    private String subject;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}
