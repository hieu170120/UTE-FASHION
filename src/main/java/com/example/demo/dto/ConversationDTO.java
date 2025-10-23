package com.example.demo.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationDTO {
	private int id;
	private SimpleUserDTO user;
	private SimpleShopDTO shop;
	private List<MessageDTO> messages;
	private Date createdAt;
	private Date updatedAt;

	// --- Inner DTOs for simplified representations ---

	/**
	 * A simplified representation of a User for chat.
	 */
	@Data
	public static class SimpleUserDTO {
		private final int id;
		private final String username;
	}

	/**
	 * A simplified representation of a Shop for chat.
	 */
	@Data
	public static class SimpleShopDTO {
		private final int id;
		private final String name;
	}
}
