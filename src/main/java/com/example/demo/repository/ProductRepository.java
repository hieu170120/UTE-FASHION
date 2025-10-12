package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByCategorySlug(String categorySlug, Pageable pageable);
    Page<Product> findByBrandSlug(String brandSlug, Pageable pageable);
    Optional<Product> findBySlug(String slug);
}
