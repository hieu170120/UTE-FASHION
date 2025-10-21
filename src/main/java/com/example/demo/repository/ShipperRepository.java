package com.example.demo.repository;

import com.example.demo.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Integer> {
    List<Shipper> findByCarrierId(Integer carrierId);
    List<Shipper> findByCarrierIdAndIsActiveTrue(Integer carrierId);
    List<Shipper> findByIsActiveTrue();
    Optional<Shipper> findByUserUserId(Integer userId);
}
