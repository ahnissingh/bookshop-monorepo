package com.bookshop.client.dto;


import java.math.BigDecimal;
import java.util.List;

public record BookSearchFilterRequest(
        String search,          // Universal search for title, author, subtitle
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> grades,    //Translates to checkboxes of grades (user can select say eight,seventh,ninth)
        Boolean inStockOnly
) {
    public boolean isInStockOnly() {
        return inStockOnly != null ? inStockOnly : false;
    }
}