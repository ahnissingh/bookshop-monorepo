package com.bookshop.vendor.dto;

import java.math.BigDecimal;
import java.util.List;

public record VendorBookFilterRequest(
        String search,          // Search own books by title, author
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> grades,
        Boolean outOfStockOnly  // Vendor might want to see what needs restocking
) {

}