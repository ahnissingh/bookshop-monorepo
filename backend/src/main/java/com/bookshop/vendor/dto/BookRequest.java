package com.bookshop.vendor.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BookRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title cannot exceed 150 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 150, message = "Author cannot exceed 150 characters")
        String author,

        String subtitle,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price must have max 8 digits and 2 decimals")
        BigDecimal price,

        @Size(max = 50, message = "Grade cannot exceed 50 characters") //Altough we can migrate to Enum or table for grade later
        String grade,

        @Size(max = 5000, message = "Description is too long (max 5000 chars)")
        String description,
        
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        @Max(value = 10000, message = "Quantity cannot exceed 10,000")
        Integer quantity
) {}