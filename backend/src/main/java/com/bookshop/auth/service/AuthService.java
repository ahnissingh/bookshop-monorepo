package com.bookshop.auth.service;


import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
public interface AuthService {
    AuthResponse registerVendor(UserRegistrationRequest request);
    AuthResponse registerClient(UserRegistrationRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}