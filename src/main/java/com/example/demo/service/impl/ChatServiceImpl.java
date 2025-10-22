package com.example.demo.service.impl;

import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Override
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        // Lấy thông tin user đang đăng nhập
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        User sender = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Conversation conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setSenderType(messageDTO.getSenderType());
        message.setMessageContent(messageDTO.getMessageContent());
        message.setRead(false); // Tin nhắn mới chưa được đọc
        message.setCreatedAt(new Date());

        message = messageRepository.save(message);

        return convertToDTO(message);
    }

    @Override
    public List<MessageDTO> getMessages(int conversationId) {
        return messageRepository.findByConversationId(conversationId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Conversation findOrCreateConversation(int shopId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return conversationRepository.findByUserIdAndShopId(user.getId(), shopId)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setUser(user);
                    newConversation.setShop(shop);
                    return conversationRepository.save(newConversation);
                });
    }

    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderType(message.getSenderType())
                .messageContent(message.getMessageContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
