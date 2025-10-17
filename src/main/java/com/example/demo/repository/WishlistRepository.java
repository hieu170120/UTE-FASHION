package com.example.demo.repository;

import com.example.demo.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    long countByProductId(Integer productId);

}
