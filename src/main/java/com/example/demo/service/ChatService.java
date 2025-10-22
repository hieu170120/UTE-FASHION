package com.example.demo.service;

import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.Conversation;

import java.util.List;

public interface ChatService {
    MessageDTO sendMessage(MessageDTO messageDTO);
    List<MessageDTO> getMessages(int conversationId);
    Conversation findOrCreateConversation(int shopId);
}
