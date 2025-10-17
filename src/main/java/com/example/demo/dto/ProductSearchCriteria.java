package com.example.demo.dto;

// We are replacing @Data with explicit getters and setters to ensure Spring's data binding works reliably.
public class ProductSearchCriteria {
	private String keyword;
	private String categorySlug;
	private String brandSlug;
	private String sort;

	// Getters
	public String getKeyword() {
		return keyword;
	}

	public String getCategorySlug() {
		return categorySlug;
	}

	public String getBrandSlug() {
		return brandSlug;
	}

	public String getSort() {
		return sort;
	}

	// Setters
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setCategorySlug(String categorySlug) {
		this.categorySlug = categorySlug;
	}

	public void setBrandSlug(String brandSlug) {
		this.brandSlug = brandSlug;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}
}
