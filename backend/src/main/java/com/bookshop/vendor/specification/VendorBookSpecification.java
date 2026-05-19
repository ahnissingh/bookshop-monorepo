package com.bookshop.vendor.specification;

import com.bookshop.vendor.dto.VendorBookFilterRequest;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class VendorBookSpecification {

    public static Specification<Book> withFilters(VendorBookFilterRequest request, User vendor) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Vendor can only see their OWN books
            predicates.add(criteriaBuilder.equal(root.get("user"), vendor));

            // Apply all other filters dynamically
            if (StringUtils.hasText(request.search())) {
                String searchPattern = "%" + request.search().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate authorMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), searchPattern);
                Predicate subtitleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("subtitle")), searchPattern);
                predicates.add(criteriaBuilder.or(titleMatch, authorMatch, subtitleMatch));
            }

            if (request.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.minPrice()));
            }

            if (request.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.maxPrice()));
            }

            if (request.grades() != null && !request.grades().isEmpty()) {
                predicates.add(root.get("grade").in(request.grades()));
            }

            // Vendor Specific Out-of-Stock check  so they know what to refill
            if (Boolean.TRUE.equals(request.outOfStockOnly())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), 0));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}