package com.example.demo.repository;

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

    @Override
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b WHERE c.slug = :categorySlug",
           countQuery = "SELECT count(p) FROM Product p WHERE p.category.slug = :categorySlug")
    Page<Product> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b WHERE b.slug = :brandSlug",
           countQuery = "SELECT count(p) FROM Product p WHERE p.brand.slug = :brandSlug")
    Page<Product> findByBrandSlug(@Param("brandSlug") String brandSlug, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b WHERE p.slug = :slug")
    Optional<Product> findBySlug(@Param("slug") String slug);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b WHERE lower(p.productName) LIKE lower(concat('%', :keyword, '%'))",
           countQuery = "SELECT count(p) FROM Product p WHERE lower(p.productName) LIKE lower(concat('%', :keyword, '%'))")
    Page<Product> findByProductNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.shop s WHERE s.id = :shopId",
           countQuery = "SELECT count(p) FROM Product p WHERE p.shop.id = :shopId")
    Page<Product> findByShopId(@Param("shopId") Integer shopId, Pageable pageable);

    List<Product> findTop8ByIsActiveTrueOrderBySoldCountDesc();

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b ORDER BY p.soldCount DESC",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findByOrderBySoldCountDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b ORDER BY p.averageRating DESC",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findByOrderByAverageRatingDesc(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c JOIN FETCH p.brand b LEFT JOIN p.wishlists w GROUP BY p.id ORDER BY COUNT(w) DESC",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findByOrderByWishlistCountDesc(Pageable pageable);

    List<Product> findTop8ByIsActiveTrueOrderByCreatedAtDesc();

}
