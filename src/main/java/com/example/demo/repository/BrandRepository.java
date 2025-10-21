package com.example.demo.repository;

import com.example.demo.entity.Brand;
import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findBySlug(String slug);
    List<Brand> findAllByIsActive(boolean isActive);

}
