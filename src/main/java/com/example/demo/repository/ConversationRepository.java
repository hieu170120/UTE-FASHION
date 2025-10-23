package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
	@Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.shop.id = :shopId")
	Optional<Conversation> findByUserIdAndShopId(@Param("userId") int userId, @Param("shopId") int shopId);

	List<Conversation> findByUserId(int userId);

	List<Conversation> findByShopId(int shopId);
}
