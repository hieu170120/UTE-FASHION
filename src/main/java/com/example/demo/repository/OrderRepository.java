package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser_UserId(Integer userId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.product.id = :productId AND o.orderStatus = :orderStatus")
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
           "WHERE o.user.id = :userId " +
           "AND oi.product.id = :productId " +
           "AND o.orderStatus = 'Delivered' " +
           "AND NOT EXISTS (SELECT r FROM Review r WHERE r.order.id = o.id AND r.product.id = :productId AND r.user.id = :userId)")
    List<Order> findEligibleOrdersForReview(@Param("userId") Integer userId, @Param("productId") Integer productId);


}
