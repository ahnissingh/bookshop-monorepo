package com.bookshop.auth.util;

import com.bookshop.auth.config.AppCookieProperties;
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
    private final AppCookieProperties cookieProps;

    public void attachAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        long accessMaxAge = jwtProperties.getAccessExpiration() / 1000;
        long refreshMaxAge = jwtProperties.getRefreshExpiration() / 1000;

        ResponseCookie accessCookie = buildCookie("accessToken", authResponse.accessToken(), accessMaxAge);
        ResponseCookie refreshCookie = buildCookie("refreshToken", authResponse.refreshToken(), refreshMaxAge);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = buildCookie("accessToken", "", 0);
        ResponseCookie clearRefresh = buildCookie("refreshToken", "", 0);

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProps.isSecure())
                .domain(cookieProps.getDomain())
                .sameSite(cookieProps.getSameSite())
                .path(cookieProps.getPath())
                .maxAge(maxAgeSeconds)
                .build();
    }
}