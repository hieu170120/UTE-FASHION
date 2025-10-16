package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Products")
@Getter
@Setter
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Integer id;

	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;

	@Column(name = "slug", nullable = false, unique = true, length = 255)
	private String slug;

	@Column(name = "sku", unique = true, length = 100)
	private String sku;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id")
	private Brand brand;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", referencedColumnName = "shop_id")
	private Shop shop;

	@Lob
	@Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
	private String description;

	@Column(name = "short_description", length = 500)
	private String shortDescription;

	@Column(name = "price", nullable = false, precision = 18, scale = 2)
	private BigDecimal price;

	@Column(name = "sale_price", precision = 18, scale = 2)
	private BigDecimal salePrice;

	@Column(name = "cost_price", precision = 18, scale = 2)
	private BigDecimal costPrice;

	@Column(name = "stock_quantity")
	private Integer stockQuantity = 0;

	@Column(name = "low_stock_threshold")
	private Integer lowStockThreshold = 10;

	@Column(name = "weight", precision = 10, scale = 2)
	private BigDecimal weight;

	@Column(name = "dimensions", length = 50)
	private String dimensions;

	@Column(name = "material", length = 255)
	private String material;

	@Column(name = "is_featured")
	private boolean isFeatured = false;

	@Column(name = "is_new_arrival")
	private boolean isNewArrival = false;

	@Column(name = "is_best_seller")
	private boolean isBestSeller = false;

	@Column(name = "is_active")
	private boolean isActive = true;

	@Column(name = "view_count")
	private Integer viewCount = 0;

	@Column(name = "sold_count")
	private Integer soldCount = 0;

	@Column(name = "average_rating", precision = 3, scale = 2)
	private BigDecimal averageRating = BigDecimal.ZERO;

	@Column(name = "review_count")
	private Integer reviewCount = 0;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 20)
	private Set<ProductImage> images;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 20)
	private Set<ProductVariant> variants;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
