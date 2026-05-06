package com.bookshop.auth.controller;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.service.AuthService;
import com.bookshop.auth.util.CookieUtil;
import com.bookshop.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register/vendor")
    public ResponseEntity<ApiResponse<AuthResponse>> registerVendor(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletResponse response) {
        log.info("Received vendor registration request for email: {}", request.email());
        AuthResponse authResponse = authService.registerVendor(request);
        cookieUtil.attachAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Vendor registered successfully"));
    }

    @PostMapping("/register/client")
    public ResponseEntity<ApiResponse<AuthResponse>> registerClient(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletResponse response) {
        log.info("Received client registration request for email: {}", request.email());
        AuthResponse authResponse = authService.registerClient(request);

        cookieUtil.attachAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Client registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        log.info("Received login request for: {}", request.usernameOrEmail());
        AuthResponse authResponse = authService.login(request);

        cookieUtil.attachAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        log.info("Processing logout request to clear cookies.");

        cookieUtil.clearAuthCookies(response);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response) {
        log.info("Received token refresh request.");
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        cookieUtil.attachAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }


}
