package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser_UserId(Integer userId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.userId = :userId AND oi.product.id = :productId AND o.orderStatus = :orderStatus")
    boolean hasUserPurchasedProduct(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("orderStatus") String orderStatus);

    /**
     * Finds orders that are eligible for a review by a specific user for a specific product.
     * An order is eligible if:
     * 1. It belongs to the user.
     * 2. It contains the specified product.
     * 3. Its status is 'Delivered'.
     * 4. The user has not already written a review for that product in that specific order.
     *
     * @param userId    The ID of the user.
     * @param productId The ID of the product.
     * @return A list of eligible Order entities.
     */
    @Query("SELECT o FROM Order o JOIN o.orderItems oi " +
            "WHERE o.user.userId = :userId " +
            "AND oi.product.id = :productId " +
            "AND o.orderStatus = 'Delivered' " +
            "AND NOT EXISTS (SELECT r FROM Review r WHERE r.order.id = o.id AND r.product.id = :productId AND r.user.userId = :userId)")
    List<Order> findEligibleOrdersForReview(@Param("userId") Integer userId, @Param("productId") Integer productId);

    // Methods for order management
    List<Order> findByOrderStatus(String orderStatus);
    
    List<Order> findByShipperIdAndOrderStatus(Integer shipperId, String orderStatus);
    
    List<Order> findByShipperId(Integer shipperId);
    
    List<Order> findByUserUserId(Integer userId);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.shipper " +
           "LEFT JOIN FETCH o.carrier " +
           "WHERE o.user.userId = :userId " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByUserUserIdWithDetails(@Param("userId") Integer userId);
    
    @Query("SELECT o FROM Order o " +
           "WHERE o.user.userId = :userId " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByUserUserIdOrderByOrderDateDesc(@Param("userId") Integer userId, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.shipper " +
           "LEFT JOIN FETCH o.carrier " +
           "WHERE o.id = :orderId")
    java.util.Optional<Order> findByIdWithDetails(@Param("orderId") Integer orderId);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR :status = '' OR o.orderStatus = :status) AND " +
           "(:fromDate IS NULL OR o.orderDate >= :fromDate) AND " +
           "(:toDate IS NULL OR o.orderDate <= :toDate)")
    Page<Order> findByFilters(@Param("status") String status,
                              @Param("fromDate") java.time.LocalDateTime fromDate,
                              @Param("toDate") java.time.LocalDateTime toDate,
                              Pageable pageable);
}
