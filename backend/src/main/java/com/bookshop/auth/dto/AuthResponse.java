package com.bookshop.auth.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
public record AuthResponse(
        @JsonIgnore
        String accessToken,
        @JsonIgnore
        String refreshToken,
        String username,
        Set<String> roles
) {}