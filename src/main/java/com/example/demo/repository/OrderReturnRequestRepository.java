package com.example.demo.repository;

import com.example.demo.entity.OrderReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReturnRequestRepository extends JpaRepository<OrderReturnRequest, Integer> {
    
    List<OrderReturnRequest> findByStatus(String status);
    
    List<OrderReturnRequest> findByUserUserId(Integer userId);
    
    List<OrderReturnRequest> findByOrderId(Integer orderId);
}
