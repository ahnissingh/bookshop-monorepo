package com.bookshop.auth.service;


import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
public interface AuthService {
    void registerVendor(UserRegistrationRequest request);
    void registerClient(UserRegistrationRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void verifyEmail(String token);

    void resendVerificationEmail(String email);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}