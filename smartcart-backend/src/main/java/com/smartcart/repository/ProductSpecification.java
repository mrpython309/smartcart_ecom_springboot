package com.smartcart.repository;

import com.smartcart.dto.SmartSearchCriteria;
import com.smartcart.entity.Category;
import com.smartcart.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withAiCriteria(SmartSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only search active products
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (criteria == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // Price filters
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            // Category filter (joining category table if necessary)
            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("name")), 
                        "%" + criteria.getCategory().toLowerCase() + "%"
                ));
            }

            // Keyword filters (match any keyword in name, description, or brand)
            if (criteria.getKeywords() != null && !criteria.getKeywords().isEmpty()) {
                List<Predicate> keywordPredicates = new ArrayList<>();
                for (String keyword : criteria.getKeywords()) {
                    String pattern = "%" + keyword.toLowerCase() + "%";
                    Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
                    Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                    Predicate brandMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), pattern);
                    
                    keywordPredicates.add(criteriaBuilder.or(nameMatch, descMatch, brandMatch));
                }
                // Group the keyword predicates with OR so any matching keyword is fine, or AND if they all must match.
                // Let's use AND so it narrows down based on the AI's keywords.
                predicates.add(criteriaBuilder.and(keywordPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
