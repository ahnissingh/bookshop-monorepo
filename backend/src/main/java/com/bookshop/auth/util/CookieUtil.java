package com.bookshop.auth.util;

import com.bookshop.auth.config.JwtProperties;
import com.bookshop.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CookieUtil {
    private final JwtProperties jwtProperties;

    public void attachAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        long accessMaxAgeSeconds = jwtProperties.getAccessExpiration() / 1000;
        long refreshMaxAgeSeconds = jwtProperties.getRefreshExpiration() / 1000;

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.accessToken())
                .httpOnly(true)
                .secure(false) // TODO: true in prod
                .path("/")
                .maxAge(accessMaxAgeSeconds)
                .sameSite("Lax")
                .domain("bookstacks.store")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.refreshToken())
                .httpOnly(true)
                .secure(false) // TODO: true in prod
                .path("/")
                .maxAge(refreshMaxAgeSeconds)
                .sameSite("Lax")
                .domain("bookstacks.store")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "").path("/").maxAge(0).httpOnly(true).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "").path("/").maxAge(0).httpOnly(true).build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }
}
