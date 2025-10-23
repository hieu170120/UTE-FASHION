package com.example.demo.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		// Assuming senderType logic is handled by frontend or is fixed
		message.setSenderType("USER");
		message.setMessageContent(messageDTO.getContent()); // Use getContent() to match JS
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
		// Find the entities first
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

		// Use the repository method to find an existing conversation
		Conversation conversation = conversationRepository.findByUserIdAndShopId(user.getUserId(), shop.getId())
				.orElseGet(() -> {
					// Or create a new one if it doesn't exist
					Conversation newConversation = new Conversation();
					newConversation.setUser(user);
					newConversation.setShop(shop);
					newConversation.setCreatedAt(Date.from(Instant.now()));
					newConversation.setUpdatedAt(Date.from(Instant.now()));
					return conversationRepository.save(newConversation);
				});

		return convertToDTO(conversation);
	}

	/**
	 * Converts a Message entity to a MessageDTO. Ensures field names are consistent
	 * with the frontend (e.g., "content").
	 */
	private MessageDTO convertToDTO(Message message) {
		if (message == null)
			return null;
		return MessageDTO.builder().id(message.getId()).conversationId(message.getConversation().getId())
				.sender(convertToSimpleUserDTO(message.getSender())).content(message.getMessageContent())
				.isRead(message.isRead()).createdAt(message.getCreatedAt()).build();
	}

	/**
	 * Converts a Conversation entity to a ConversationDTO, including its messages.
	 */
	private ConversationDTO convertToDTO(Conversation conversation) {
		if (conversation == null)
			return null;

		List<MessageDTO> messages = getMessages(conversation.getId());

		return ConversationDTO.builder().id(conversation.getId()).user(convertToSimpleUserDTO(conversation.getUser()))
				.shop(convertToSimpleShopDTO(conversation.getShop())).messages(messages)
				.createdAt(conversation.getCreatedAt()).updatedAt(conversation.getUpdatedAt()).build();
	}

	// You would typically have these DTOs and mappers in a separate location

	private ConversationDTO.SimpleUserDTO convertToSimpleUserDTO(User user) {
		if (user == null)
			return null;
		return new ConversationDTO.SimpleUserDTO(user.getUserId(), user.getUsername());
	}

	private ConversationDTO.SimpleShopDTO convertToSimpleShopDTO(Shop shop) {
		if (shop == null)
			return null;
		return new ConversationDTO.SimpleShopDTO(shop.getId(), shop.getShopName());
	}
}
