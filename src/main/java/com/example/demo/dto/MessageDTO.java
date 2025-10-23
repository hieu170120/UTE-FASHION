package com.example.demo.dto;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDTO {
	private int id;
	private int conversationId;
	private ConversationDTO.SimpleUserDTO sender;
	private String content;
	private boolean isRead;
	private Date createdAt;
	private String senderType; // "USER" or "SHOP"
}
