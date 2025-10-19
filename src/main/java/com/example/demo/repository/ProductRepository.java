package com.example.demo.repository;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product>, CustomProductRepository {

    Optional<Product> findBySlug(String slug);

    List<Product> findAllByShop_Id(Integer shopId);
    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) " +
           "FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<ProductSummaryDTO> findSummaryNewest(Pageable pageable);

    @Query("SELECT new com.example.demo.dto.ProductSummaryDTO(p.id, p.productName, p.slug, p.price, p.salePrice) " +
           "FROM Product p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    List<ProductSummaryDTO> findSummaryBestsellers(Pageable pageable);

}
