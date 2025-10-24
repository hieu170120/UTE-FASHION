package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
	List<Message> findByConversationIdOrderByCreatedAtAsc(int conversationId);
}
