package com.example.demo.repository;

import com.example.demo.entity.Color;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductVariant;
import com.example.demo.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    @Query("SELECT pv FROM ProductVariant pv " +
            "LEFT JOIN FETCH pv.color " +
            "LEFT JOIN FETCH pv.size " +
            "WHERE pv.product.id = :productId")
    List<ProductVariant> findWithDetailsByProductId(@Param("productId") Integer productId);

    Optional<ProductVariant> findByProductAndColorAndSize(Product product, Color color, Size size);

    Optional<ProductVariant> findBySkuIgnoreCase(String sku);
}
