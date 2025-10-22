package com.example.demo.repository;

import com.example.demo.entity.CategorySales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CategorySalesRepository extends JpaRepository<CategorySales, Integer> {
    
    @Query("SELECT cs FROM CategorySales cs " +
           "WHERE cs.shop.id = :shopId " +
           "AND cs.periodType = 'MONTH' " +
           "AND cs.periodStart <= :date AND cs.periodEnd >= :date " +
           "ORDER BY cs.totalRevenue DESC")
    List<CategorySales> findTopCategoriesForCurrentMonth(@Param("shopId") Integer shopId,
                                                         @Param("date") LocalDate date);
}
