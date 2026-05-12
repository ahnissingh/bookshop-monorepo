package com.bookshop.auth.controller;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.PasswordResetRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.service.AuthService;
import com.bookshop.auth.util.CookieUtil;
import com.bookshop.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<Void>> registerVendor(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received vendor registration request for email: {}", request.email());
        authService.registerVendor(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Vendor registered successfully. Please check your email for the verification link."));
    }

    @PostMapping("/register/client")
    public ResponseEntity<ApiResponse<Void>> registerClient(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received client registration request for email: {}", request.email());
        authService.registerClient(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Client registered successfully. Please check your email for the verification link."));
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
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        log.info("Received email verification request.");
        authService.verifyEmail(token);

        return ResponseEntity.ok(ApiResponse.success(null, "Email successfully verified. You can now log in."));
    }
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        log.info("API hit: Resend verification for {}", email);
        authService.resendVerificationEmail(email);

        return ResponseEntity.ok(ApiResponse.success(null, "If your account exists and is unverified, a new link has been sent."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam("email") String email) {
        log.info("API hit: Forgot password for {}", email);

        authService.forgotPassword(email);

        return ResponseEntity.ok(ApiResponse.success(null, "If an active account exists with that email, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody PasswordResetRequest request) {
        log.info("API hit: Reset password");
        authService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok(ApiResponse.success(null, "Password has been successfully reset. You can now log in."));
    }


}
