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
public class MessageDTO {
    private long id;
    private int conversationId;
    private int senderId;
    private String senderType;
    private String messageContent;
    private boolean isRead;
    private Date createdAt;
}
