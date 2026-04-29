package com.bookshop.shared.dto;


import java.time.Instant;

public record ErrorDetails(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}
