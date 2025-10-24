package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;

@RestController
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final UserRepository userRepository;

	@Autowired
	public ChatController(ChatService chatService, SimpMessagingTemplate simpMessagingTemplate,
			UserRepository userRepository) {
		this.chatService = chatService;
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.userRepository = userRepository;
	}

	@MessageMapping("/chat.sendMessage")
	public void sendMessage(@Payload MessageDTO messageDTO, Authentication authentication) {
		Integer senderId = getUserIdFromAuthentication(authentication);
		if (senderId == null) {
			return;
		}

		MessageDTO savedMessage = chatService.sendMessage(messageDTO, senderId);
		simpMessagingTemplate.convertAndSendToUser(savedMessage.getSender().getUsername(), "/queue/messages",
				savedMessage);

	}

	@GetMapping("/api/chat/messages/{conversationId}")
	public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable int conversationId) {
		return ResponseEntity.ok(chatService.getMessages(conversationId));
	}

	@PostMapping("/api/chat/conversation/find-or-create")
	public ResponseEntity<ConversationDTO> findOrCreateConversation(@RequestParam int shopId,
			Authentication authentication) {
		Integer currentUserId = getUserIdFromAuthentication(authentication);

		if (currentUserId == null) {
			return ResponseEntity.status(403).build();
		}

		ConversationDTO conversation = chatService.findOrCreateConversation(shopId, currentUserId);
		return ResponseEntity.ok(conversation);
	}

	private Integer getUserIdFromAuthentication(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		String username = null;

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else if (principal instanceof User) {
			username = ((User) principal).getUsername();
		} else if (principal instanceof String) {
			username = (String) principal;
		}

		if (username != null) {
			Optional<User> userOpt = userRepository.findByUsername(username);
			return userOpt.map(User::getUserId).orElse(null);
		}

		return null;
	}
}
