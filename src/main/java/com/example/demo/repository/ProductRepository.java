
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

	@Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b", countQuery = "SELECT count(p) FROM Product p")
	Page<Product> findAll(Pageable pageable);

	@Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE c.slug = :categorySlug", countQuery = "SELECT count(p) FROM Product p WHERE p.category.slug = :categorySlug")
	Page<Product> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

	@Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE b.slug = :brandSlug", countQuery = "SELECT count(p) FROM Product p WHERE p.brand.slug = :brandSlug")
	Page<Product> findByBrandSlug(@Param("brandSlug") String brandSlug, Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE p.slug = :slug")
	Optional<Product> findBySlug(@Param("slug") String slug);

	@Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE lower(p.productName) LIKE lower(concat('%', :keyword, '%'))", countQuery = "SELECT count(p) FROM Product p WHERE lower(p.productName) LIKE lower(concat('%', :keyword, '%'))")
	Page<Product> findByProductNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}
