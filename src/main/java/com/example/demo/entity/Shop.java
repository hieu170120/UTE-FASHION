package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Shops")
@Getter
@Setter
public class Shop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shop_id")
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", referencedColumnName = "user_id", nullable = false)
	private User vendor;

	@Column(name = "shop_name", nullable = false, unique = true, length = 100)
	private String shopName;

	@Column(name = "slug", nullable = false, unique = true, length = 100)
	private String slug;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	@Column(name = "is_active")
	private boolean isActive = false;

	@Column(name = "commission_percentage", precision = 5, scale = 2)
	private BigDecimal commissionPercentage = new BigDecimal("0.00");

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Product> products;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
