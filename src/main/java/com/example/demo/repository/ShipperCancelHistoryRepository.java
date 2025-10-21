package com.example.demo.repository;

import com.example.demo.entity.ShipperCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipperCancelHistoryRepository extends JpaRepository<ShipperCancelHistory, Integer> {
    
    // Đếm số lần shipper hủy đơn
    @Query("SELECT COUNT(h) FROM ShipperCancelHistory h WHERE h.shipper.id = :shipperId")
    Long countByShipperId(@Param("shipperId") Integer shipperId);
    
    // Lấy danh sách lịch sử hủy của shipper (sắp xếp theo thời gian mới nhất)
    @Query("SELECT h FROM ShipperCancelHistory h WHERE h.shipper.id = :shipperId ORDER BY h.cancelledAt DESC")
    List<ShipperCancelHistory> findByShipperIdOrderByCancelledAtDesc(@Param("shipperId") Integer shipperId);
    
    // Lấy lịch sử hủy của một đơn hàng cụ thể (để admin xem)
    @Query("SELECT h FROM ShipperCancelHistory h WHERE h.order.id = :orderId ORDER BY h.cancelledAt DESC")
    List<ShipperCancelHistory> findByOrderIdOrderByCancelledAtDesc(@Param("orderId") Integer orderId);
}
