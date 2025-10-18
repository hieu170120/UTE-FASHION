package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {

	Optional<Shop> findByVendorUserId(Integer userId);

	boolean existsByShopName(String shopName);

	Optional<Shop> findBySlug(String slug);

}
