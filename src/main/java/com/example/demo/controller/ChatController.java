package com.example.demo.controller;

import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void sendMessage(@Payload MessageDTO messageDTO) {
        MessageDTO savedMessage = chatService.sendMessage(messageDTO);
        simpMessagingTemplate.convertAndSend("/topic/conversation/" + savedMessage.getConversationId(), savedMessage);
    }

    @GetMapping("/api/chat/messages/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable int conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }

    @PostMapping("/api/chat/conversation/find-or-create")
    public ResponseEntity<Conversation> findOrCreateConversation(@RequestParam int shopId) {
        Conversation conversation = chatService.findOrCreateConversation(shopId);
        return ResponseEntity.ok(conversation);
    }
}
