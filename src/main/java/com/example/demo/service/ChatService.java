package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.dto.MessageDTO;

public interface ChatService {

	MessageDTO sendMessage(MessageDTO messageDTO, int senderId);

	List<MessageDTO> getMessages(int conversationId);

	ConversationDTO findOrCreateConversation(int shopId, int userId);

	List<ConversationDTO> getConversationsForUser(int userId);
}
