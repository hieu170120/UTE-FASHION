package com.example.demo.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Messages")
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(name = "sender_type", nullable = false, length = 50)
	private String senderType;

	@Column(name = "message_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
	private String messageContent;

	@Column(name = "is_read", nullable = false)
	private boolean isRead = false;

	@Column(name = "created_at", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = new Date();
	}
}
