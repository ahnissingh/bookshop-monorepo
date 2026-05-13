package com.bookshop.client.specification;

import com.bookshop.client.dto.BookSearchFilterRequest;
import com.bookshop.shared.entity.Book;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> withFilters(BookSearchFilterRequest request) {
        return (root, query, criteriaBuilder) -> {

            // We must skip this for the Count query, otherwise Hibernate throws an exception
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();

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

            if (request.isInStockOnly()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}