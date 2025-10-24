package com.example.demo.repository;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.ViewedProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Integer> {

    Optional<ViewedProduct> findByUserAndProduct(User user, Product product);

    @Query("SELECT vp FROM ViewedProduct vp WHERE vp.user.id = :userId AND vp.product.id <> :currentProductId")
    List<ViewedProduct> findRecentlyViewedByUserId(@Param("userId") Integer userId, @Param("currentProductId") Integer currentProductId, Pageable pageable);
}
