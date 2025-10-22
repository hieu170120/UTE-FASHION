package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationDTO {
    private int id;
    private int userId;
    private int shopId;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
