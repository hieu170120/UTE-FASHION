package com.example.demo.repository;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface CustomProductRepository {
    /**
     * Executes a query with the given Specification and projects the results directly into ProductSummaryDTOs.
     * This is highly efficient as it only selects the columns needed for the DTO.
     *
     * @param spec The Specification to filter products (can be null).
     * @param pageable The pagination and sorting information.
     * @return A Page of ProductSummaryDTOs.
     */
    Page<ProductSummaryDTO> findSummaries(Specification<Product> spec, Pageable pageable);
}
