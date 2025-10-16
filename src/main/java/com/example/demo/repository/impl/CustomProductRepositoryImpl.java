package com.example.demo.repository.impl;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.entity.Product;
import com.example.demo.repository.CustomProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

public class CustomProductRepositoryImpl implements CustomProductRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ProductSummaryDTO> findSummaries(Specification<Product> spec, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 1. Query for the DTOs (the data page)
        CriteriaQuery<ProductSummaryDTO> dtoQuery = cb.createQuery(ProductSummaryDTO.class);
        Root<Product> productRoot = dtoQuery.from(Product.class);

        // Use the constructor of ProductSummaryDTO(id, productName, slug, price, salePrice)
        dtoQuery.select(cb.construct(
                ProductSummaryDTO.class,
                productRoot.get("id"),
                productRoot.get("productName"),
                productRoot.get("slug"),
                productRoot.get("price"),
                productRoot.get("salePrice")
        ));

        if (spec != null) {
            Predicate predicate = spec.toPredicate(productRoot, dtoQuery, cb);
            if (predicate != null) {
                dtoQuery.where(predicate);
            }
        }

        // Apply sorting from Pageable
        if (pageable.getSort().isSorted()) {
            dtoQuery.orderBy(org.springframework.data.jpa.repository.query.QueryUtils.toOrders(pageable.getSort(), productRoot, cb));
        }

        TypedQuery<ProductSummaryDTO> typedQuery = em.createQuery(dtoQuery);

        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<ProductSummaryDTO> summaries = typedQuery.getResultList();

        // 2. Query for the total count
        long total = executeCountQuery(spec);

        return new PageImpl<>(summaries, pageable, total);
    }

    private long executeCountQuery(Specification<Product> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);

        countQuery.select(cb.count(countRoot));

        if (spec != null) {
            Predicate predicate = spec.toPredicate(countRoot, countQuery, cb);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }

        return em.createQuery(countQuery).getSingleResult();
    }
}
