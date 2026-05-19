package com.bookshop.client.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ClientBookResponse(
        Long id,
        String title,
        String author,
        String subtitle,
        BigDecimal price,
        String grade,
        String description,
        Integer quantity,
        String pictureUrl,
        Instant createdAt,
        Instant updatedAt,
        VendorSummary vendor
) {
}