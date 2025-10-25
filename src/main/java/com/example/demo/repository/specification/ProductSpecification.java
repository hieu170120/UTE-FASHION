package com.example.demo.repository.specification;

import com.example.demo.dto.ProductSearchCriteria;
import com.example.demo.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> fromCriteria(ProductSearchCriteria criteria) {
        Specification<Product> spec = Specification.where(isActive());

        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            spec = spec.and(hasKeyword(criteria.getKeyword()));
        }

        if (criteria.getCategorySlug() != null && !criteria.getCategorySlug().isEmpty()) {
            spec = spec.and(hasCategory(criteria.getCategorySlug()));
        }

        if (criteria.getBrandSlug() != null && !criteria.getBrandSlug().isEmpty()) {
            spec = spec.and(hasBrand(criteria.getBrandSlug()));
        }

        if (criteria.getShopId() != null) {
            spec = spec.and(hasShopId(criteria.getShopId()));
        }

        return spec;
    }

    /**
     * Returns a Specification to filter for products where 'isActive' is true.
     */
    private static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

    private static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%");
    }

    private static Specification<Product> hasCategory(String categorySlug) {
        return (root, query, criteriaBuilder) -> {
            // Join to the category entity and check its slug
            return criteriaBuilder.equal(root.join("category").get("slug"), categorySlug);
        };
    }

    private static Specification<Product> hasBrand(String brandSlug) {
        return (root, query, criteriaBuilder) -> {
            // Join to the brand entity and check its slug
            return criteriaBuilder.equal(root.join("brand").get("slug"), brandSlug);
        };
    }

    private static Specification<Product> hasShopId(Integer shopId) {
        return (root, query, criteriaBuilder) -> {
            // Join to the shop entity and check its id
            return criteriaBuilder.equal(root.join("shop").get("id"), shopId);
        };
    }
}
