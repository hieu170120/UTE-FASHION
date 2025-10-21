package com.example.demo.repository;

import com.example.demo.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {

    boolean existsByShopName(String shopName);

    @Query("SELECT s FROM Shop s WHERE s.vendor.id = :userId")
    Optional<Shop> findByVendorUserId(Integer userId);

    @Query("SELECT s FROM Shop s JOIN FETCH s.vendor")
    List<Shop> findAllWithVendor();
}
