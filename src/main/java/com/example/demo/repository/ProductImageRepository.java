package com.example.demo.repository;

import com.example.demo.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :productIds ORDER BY pi.product.id, pi.id")
    List<ProductImage> findImagesByProductIds(@Param("productIds") List<Integer> productIds);
}
