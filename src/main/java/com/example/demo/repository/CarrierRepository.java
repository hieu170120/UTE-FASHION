package com.example.demo.repository;

import com.example.demo.entity.Carrier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
    
	List<Carrier> findByIsActiveTrue();
	
	@Query("SELECT c FROM Carrier c WHERE " +
		   "(:search IS NULL OR :search = '' OR " +
		   "LOWER(c.carrierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		   "LOWER(c.contactPhone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
		   "(:active IS NULL OR c.isActive = :active)")
	Page<Carrier> findByFilters(@Param("search") String search,
								@Param("active") Boolean active,
								Pageable pageable);

}
