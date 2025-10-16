package com.example.demo.repository;

import com.example.demo.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :productIds ORDER BY pi.product.id, pi.displayOrder ASC, pi.id ASC")
    List<ProductImage> findImagesByProductIds(@Param("productIds") List<Integer> productIds);

    /**
     * Fetches the top 2 images for each product in a given list of product IDs.
     * This uses a native SQL query with a window function (ROW_NUMBER) for high efficiency,
     * avoiding the N+1 problem.
     * The images are ranked by 'display_order' first, then by 'image_id'.
     */
    @Query(
            value = "SELECT * FROM ( " +
                    "    SELECT pi.*, ROW_NUMBER() OVER(PARTITION BY pi.product_id ORDER BY pi.display_order ASC, pi.image_id ASC) as rn " +
                    "    FROM product_images pi WHERE pi.product_id IN (:productIds) " +
                    ") as ranked_images WHERE rn <= 2",
            nativeQuery = true
    )
    List<ProductImage> findTop2ImagesPerProduct(@Param("productIds") List<Integer> productIds);
}
