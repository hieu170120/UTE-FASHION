package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.User;
import com.example.demo.service.ChatService;

@RestController
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	public ChatController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate) {
		this.chatService = chatService;
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	@MessageMapping("/chat.sendMessage")
	public void sendMessage(@Payload MessageDTO messageDTO, Authentication authentication) {
		// Secure the sendMessage endpoint
		User currentUser = (User) authentication.getPrincipal();
		MessageDTO savedMessage = chatService.sendMessage(messageDTO, currentUser.getUserId());
		simpMessagingTemplate.convertAndSend("/topic/conversation/" + savedMessage.getConversationId(), savedMessage);
	}

	@GetMapping("/api/chat/messages/{conversationId}")
	public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable int conversationId) {
		return ResponseEntity.ok(chatService.getMessages(conversationId));
	}

	@PostMapping("/api/chat/conversation/find-or-create")
	public ResponseEntity<ConversationDTO> findOrCreateConversation(@RequestParam int shopId,
			Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof User)) {
			return ResponseEntity.status(403).build();
		}

		User currentUser = (User) principal;
		ConversationDTO conversation = chatService.findOrCreateConversation(shopId, currentUser.getUserId());
		return ResponseEntity.ok(conversation);
	}
}
