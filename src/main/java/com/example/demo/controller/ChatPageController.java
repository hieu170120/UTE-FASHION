package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.entity.User;
import com.example.demo.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatPageController {

	private final ChatService chatService;

	@Autowired
	public ChatPageController(ChatService chatService) {
		this.chatService = chatService;
	}

	@GetMapping
	public String chatPage() {
		return "chat/index";
	}

	@GetMapping("/api/conversations")
	@ResponseBody
	public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(401).build();
		}

		User currentUser = (User) authentication.getPrincipal();
		List<ConversationDTO> conversations = chatService.getConversationsForUser(currentUser.getUserId());
		return ResponseEntity.ok(conversations);
	}
}
