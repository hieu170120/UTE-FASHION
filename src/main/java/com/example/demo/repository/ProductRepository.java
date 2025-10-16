package com.example.demo.repository;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // --- Methods for Detailed Product View (Optimized with JOIN FETCH) ---

    /**
     * Optimized query to fetch a product and all its necessary associations for the detail page.
     * This includes brand, images, variants, and the color/size for each variant.
     * Using LEFT JOIN FETCH and DISTINCT helps to solve the N+1 query problem.
     */
    @Override
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.variants v " +
            "LEFT JOIN FETCH v.color " +
            "LEFT JOIN FETCH v.size " +
            "WHERE p.id = :id")
    Optional<Product> findById(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.variants v " +
            "LEFT JOIN FETCH v.color " +
            "LEFT JOIN FETCH v.size " +
            "WHERE p.slug = :slug")
    Optional<Product> findBySlug(@Param("slug") String slug);


    // --- Methods for Summary/List Views (Optimized with DTO Projections) ---

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p")
    Page<ProductSummaryDTO> findSummaryAll(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE p.category.slug = :categorySlug")
    Page<ProductSummaryDTO> findSummaryByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE p.brand.slug = :brandSlug")
    Page<ProductSummaryDTO> findSummaryByBrandSlug(@Param("brandSlug") String brandSlug, Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE lower(p.productName) LIKE lower(concat('%', :keyword, '%'))")
    Page<ProductSummaryDTO> findSummaryByProductNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE p.shop.id = :shopId")
    Page<ProductSummaryDTO> findSummaryByShopId(@Param("shopId") Integer shopId, Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    List<ProductSummaryDTO> findSummaryBestsellers(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p ORDER BY p.soldCount DESC")
    Page<ProductSummaryDTO> findSummaryByOrderBySoldCountDesc(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p ORDER BY p.averageRating DESC")
    Page<ProductSummaryDTO> findSummaryByOrderByAverageRatingDesc(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p ORDER BY p.wishlistCount DESC")
    Page<ProductSummaryDTO> findSummaryByOrderByWishlistCountDesc(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<ProductSummaryDTO> findSummaryNewest(Pageable pageable);

}
