package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatPageController {

	private final ChatService chatService;
	private final UserRepository userRepository;

	@Autowired
	public ChatPageController(ChatService chatService, UserRepository userRepository) {
		this.chatService = chatService;
		this.userRepository = userRepository;
	}

	@GetMapping
	// view
	public String chatPage(Authentication authentication, Model model) {
		Integer currentUserId = getUserIdFromAuthentication(authentication);
		if (currentUserId != null) {
			model.addAttribute("currentUserId", currentUserId);
		}
		return "chat/index";
	}

	@GetMapping("/api/conversations")
	@ResponseBody
	public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(401).build();
		}

		Integer currentUserId = getUserIdFromAuthentication(authentication);
		if (currentUserId == null) {
			return ResponseEntity.status(403).build();
		}
		List<ConversationDTO> conversations = chatService.getConversationsForUser(currentUserId);
		return ResponseEntity.ok(conversations);
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
