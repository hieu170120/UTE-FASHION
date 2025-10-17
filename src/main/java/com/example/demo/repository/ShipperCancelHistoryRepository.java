package com.example.demo.repository;

import com.example.demo.entity.ShipperCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipperCancelHistoryRepository extends JpaRepository<ShipperCancelHistory, Integer> {
    
    // Đếm số lần shipper hủy đơn
    @Query("SELECT COUNT(h) FROM ShipperCancelHistory h WHERE h.shipper.id = :shipperId")
    Long countByShipperId(@Param("shipperId") Integer shipperId);
}
