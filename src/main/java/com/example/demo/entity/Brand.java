package com.example.demo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Brands")
@Getter
@Setter
public class Brand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "brand_id")
	private Integer id;

	@Column(name = "brand_name", nullable = false, unique = true, length = 100)
	private String brandName;

	@Column(name = "slug", nullable = false, unique = true, length = 100)
	private String slug;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	@Column(name = "website_url", length = 255)
	private String websiteUrl;

	@Column(name = "is_active")
	private boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
