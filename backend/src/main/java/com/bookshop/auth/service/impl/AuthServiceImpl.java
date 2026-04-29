package com.bookshop.auth.service.impl;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.auth.service.AuthService;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.exception.InvalidTokenException;
import com.bookshop.shared.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private static final String ROLE_VENDOR = "ROLE_VENDOR";
    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    @Override
    public AuthResponse registerVendor(UserRegistrationRequest request) {
        log.info("Initiating vendor registration for username: {}", request.username());
        return processRegistration(request, ROLE_VENDOR);
    }

    @Override
    public AuthResponse registerClient(UserRegistrationRequest request) {
        log.info("Initiating client registration for username: {}", request.username());
        return processRegistration(request, ROLE_CLIENT);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.usernameOrEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        var user = userRepository.findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User with Identifier :%s not found".formatted(request.usernameOrEmail())));

        return getAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        var user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new InvalidTokenException("User associated with token not found."));

        if (!jwtUtil.validateToken(refreshToken, user)) {
            throw new InvalidTokenException("Refresh token is invalid or expired.");
        }

        log.info("Successfully refreshed tokens for user: {}", username);
        return getAuthResponse(user);
    }

    private AuthResponse processRegistration(UserRegistrationRequest request, String roleName) {
        if (userRepository.existsByUsernameOrEmail(request.username(), request.email())) {
            log.warn("Registration failed: Username ({}) or Email ({}) already taken.", request.username(), request.email());
            throw new UserAlreadyExistsException("Username or Email is already taken.");
        }

        log.debug("Processing registration. Checking for existing user records.");
        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Critical System Error: Role '{}' not found in database.", roleName);
                    return new RuntimeException("Error: Role " + roleName + " not found in database.");
                });

        var user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(assignedRole))
                .build();

        userRepository.save(user);
        log.info("Successfully saved new user: {} with role: {}", user.getUsername(), roleName);

        return getAuthResponse(user);
    }

    @NonNull
    private AuthResponse getAuthResponse(User user) {
        log.debug("Generating access and refresh tokens for user: {}", user.getUsername());

        var newAccessToken = jwtUtil.generateAccessToken(user);
        var newRefreshToken = jwtUtil.generateRefreshToken(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(), roleNames);
    }
}