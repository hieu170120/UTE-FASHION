package com.example.demo.repository;

import com.example.demo.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    /**
     * Fetches all variants for a given product ID, along with their associated Color and Size.
     * This is an efficient query to avoid N+1 problems when loading variants.
     * @param productId The ID of the product.
     * @return A list of ProductVariant entities with their details.
     */
    @Query("SELECT pv FROM ProductVariant pv " +
           "LEFT JOIN FETCH pv.color " +
           "LEFT JOIN FETCH pv.size " +
           "WHERE pv.product.id = :productId")
    List<ProductVariant> findWithDetailsByProductId(@Param("productId") Integer productId);
}
