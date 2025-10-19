package com.example.demo.repository;

import com.example.demo.entity.ShipperNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperNotificationRepository extends JpaRepository<ShipperNotification, Integer> {
    
    List<ShipperNotification> findByShipperIdOrderByCreatedAtDesc(Integer shipperId);
    
    Long countByShipperIdAndIsReadFalse(Integer shipperId);
    
    List<ShipperNotification> findByShipperIdAndIsReadFalseOrderByCreatedAtDesc(Integer shipperId);
    
    ShipperNotification findByShipper_IdAndOrder_IdAndIsReadFalse(Integer shipperId, Integer orderId);
}
