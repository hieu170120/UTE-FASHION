package com.example.demo.service.impl;

import com.example.demo.dto.ConversationDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Autowired
    public ChatServiceImpl(MessageRepository messageRepository, ConversationRepository conversationRepository,
                         UserRepository userRepository, ShopRepository shopRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDTO, int senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + senderId));

        Conversation conversation = conversationRepository.findById(messageDTO.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with id: " + messageDTO.getConversationId()));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setSenderType("USER");
        message.setMessageContent(messageDTO.getContent());
        message.setRead(false);
        message.setCreatedAt(Date.from(Instant.now()));

        Message savedMessage = messageRepository.save(message);

        return convertToDTO(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(int conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationDTO findOrCreateConversation(int shopId, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Use the new repository method to fetch the Shop with its Vendor
        Shop shop = shopRepository.findByIdWithVendor(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        Optional<Conversation> existingConversationOpt = conversationRepository.findByUserIdAndShopId(user.getUserId(), shop.getId());

        if (existingConversationOpt.isPresent()) {
            return convertToDTO(existingConversationOpt.get());
        } else {
            Conversation newConversation = new Conversation();
            newConversation.setUser(user);
            newConversation.setShop(shop);
            newConversation.setCreatedAt(Date.from(Instant.now()));
            newConversation.setUpdatedAt(Date.from(Instant.now()));
            Conversation savedConversation = conversationRepository.save(newConversation);

            return convertToDTO(savedConversation); 
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForUser(int userId) {
        List<Conversation> userConversations = conversationRepository.findByUser_UserId(userId);

        Optional<Shop> ownedShopOpt = shopRepository.findByVendorUserId(userId);

        if (ownedShopOpt.isPresent()) {
            Shop ownedShop = ownedShopOpt.get();
            List<Conversation> shopConversations = conversationRepository.findByShopId(ownedShop.getId());
            for (Conversation sc : shopConversations) {
                if (userConversations.stream().noneMatch(c -> Objects.equals(c.getId(), sc.getId()))) {
                    userConversations.add(sc);
                }
            }
        }

        return userConversations.stream().map(this::convertToSummaryDTO).collect(Collectors.toList());
    }

    private ConversationDTO convertToSummaryDTO(Conversation conversation) {
        if (conversation == null) return null;

        return ConversationDTO.builder()
                .id(conversation.getId())
                .user(convertToSimpleUserDTO(conversation.getUser()))
                .shop(convertToSimpleShopDTO(conversation.getShop()))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private MessageDTO convertToDTO(Message message) {
        if (message == null) return null;
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(convertToSimpleUserDTO(message.getSender()))
                .content(message.getMessageContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        if (conversation == null) return null;

        // Fetch messages separately to avoid circular dependency issues and keep it light
        List<MessageDTO> messages = getMessages(conversation.getId()); 

        return ConversationDTO.builder()
                .id(conversation.getId())
                .user(convertToSimpleUserDTO(conversation.getUser()))
                .shop(convertToSimpleShopDTO(conversation.getShop()))
                .messages(messages)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ConversationDTO.SimpleUserDTO convertToSimpleUserDTO(User user) {
        if (user == null) return null;
        return ConversationDTO.SimpleUserDTO.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private ConversationDTO.SimpleShopDTO convertToSimpleShopDTO(Shop shop) {
        if (shop == null) return null;
        return ConversationDTO.SimpleShopDTO.builder()
                .id(shop.getId())
                .name(shop.getShopName())
                .logoUrl(shop.getLogoUrl())
                .build();
    }
}
