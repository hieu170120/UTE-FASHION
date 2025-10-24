package com.example.demo.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ConversationDTO {
	private int id;
	private SimpleUserDTO user;
	private SimpleShopDTO shop;
	private List<MessageDTO> messages;
	private Date createdAt;
	private Date updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SimpleUserDTO {
		private int id;
		private String username;
		private String fullName;
		private String avatarUrl;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SimpleShopDTO {
		private int id;
		private String name;
		private String logoUrl;
	}
}
