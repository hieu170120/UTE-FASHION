package com.example.demo.dto;

public class ProductSearchCriteria {
    private String keyword;
    private String categorySlug;
    private String brandSlug;
    private String sort;
    private Integer shopId; // Add shopId field

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

    public Integer getShopId() {
        return shopId;
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

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }
}
