package com.bookshop.auth.service.impl;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.auth.service.AuthService;
import com.bookshop.notification.event.publisher.EventPublisher;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.SecureToken;
import com.bookshop.shared.entity.TokenType;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.event.NotificationEvent;
import com.bookshop.shared.exception.*;
import com.bookshop.shared.repository.SecureTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableMethodSecurity
public class AuthServiceImpl implements AuthService {
    private static final String ROLE_VENDOR = "ROLE_VENDOR";
    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SecureTokenRepository secureTokenRepository;
    private final EventPublisher eventPublisher;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void registerVendor(UserRegistrationRequest request) {
        log.info("Initiating vendor registration for username: {}", request.username());
        processRegistration(request, ROLE_VENDOR);
    }

    @Override
    @Transactional
    public void registerClient(UserRegistrationRequest request) {
        log.info("Initiating client registration for username: {}", request.username());
        processRegistration(request, ROLE_CLIENT);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.usernameOrEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        var user = userRepository.findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new UserNotFoundException("User with Identifier :%s not found".formatted(request.usernameOrEmail())));

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

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // Find the token
        SecureToken secureToken = secureTokenRepository.findByToken((token))
                .orElseThrow(() -> new InvalidTokenException("Invalid or missing verification token."));

        //  Check if already used
        if (secureToken.getValidatedAt() != null) {
            log.warn("Attempt to use an already validated token: {}", token);
            throw new InvalidTokenException("This verification link has already been used.");
        }

        //  Check if expired
        if (secureToken.getExpiryAt().isBefore(Instant.now())) {
            log.warn("Attempt to use an expired token: {}", token);
            throw new InvalidTokenException("This verification link has expired. Please request a new one.");
        }

        //  Enable the User
        User user = secureToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        //  Mark the Token as Consumed
        secureToken.setValidatedAt(Instant.now());
        secureTokenRepository.save(secureToken);

        log.info("Successfully verified email for user: {}", user.getEmail());
    }


    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Processing resend verification request for: {}", email);
        userRepository.findByEmailAndEnabled(email, false)
                // Pass the user AND the TokenType to our new master helper!
                .ifPresent(user -> generateTokenAndPublishEvent(user, TokenType.VERIFICATION));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Processing forgot password request for: {}", email);

        //  we check for enabled = TRUE. We don't let unverified users reset passwords!
        userRepository.findByEmailAndEnabled(email, true)
                .ifPresent(user -> generateTokenAndPublishEvent(user, TokenType.PASSWORD_RESET));

    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Processing password reset request.");


        SecureToken secureToken = secureTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or missing password reset token."));

        // Validate Token Type
        if (secureToken.getType() != TokenType.PASSWORD_RESET) {
            log.warn("Attempt to use a {} token for password reset.", secureToken.getType());
            throw new InvalidTokenException("Invalid token type.");
        }

        // Check if already used
        if (secureToken.getValidatedAt() != null) {
            throw new InvalidTokenException("This password reset link has already been used.");
        }

        // Check if expired
        if (secureToken.getExpiryAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("This password reset link has expired. Please request a new one.");
        }

        //  Update the User's Password
        User user = secureToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        //  Mark the Token as Consumed
        secureToken.setValidatedAt(Instant.now());
        secureTokenRepository.save(secureToken);

        log.info("Successfully reset password for user: {}", user.getEmail());
    }

    private void processRegistration(UserRegistrationRequest request, String roleName) {
        userRepository.findEnabledStatusByEmail(request.email())
                .ifPresent(isEnabled -> {
                    if (!isEnabled) {
                        throw new UnverifiedUserException("User is unverified, please retry verifying your email");
                    }
                    throw new UserAlreadyExistsException("This email is already registered.");
                });

        // Check the Username SECOND (Pure uniqueness check)
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("This username is already taken. Please choose another.");
        }

        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role " + roleName + " not found."));

        var user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(assignedRole))
                .enabled(false)
                .build();

        userRepository.save(user);

        generateTokenAndPublishEvent(user, TokenType.VERIFICATION);

        log.info("Successfully saved new user: {} with role: {}", user.getUsername(), roleName);
    }

    /**
     *  HELPER: Handles token creation, DB cleanup, URL mapping, and Event Publishing for ANY token type.
     */
    private void generateTokenAndPublishEvent(User user, TokenType tokenType) {
        //  Configure properties based on the Token Type
        int expiryMinutes;
        String frontendRoute;
        String eventType;

        switch (tokenType) {
            case VERIFICATION -> {
                expiryMinutes = 60;
                frontendRoute = "/verify-email";
                eventType = "ACCOUNT_VERIFICATION";
            }
            case PASSWORD_RESET -> {
                expiryMinutes = 15;
                frontendRoute = "/reset-password";
                eventType = "PASSWORD_RESET";
            }
            case null, default -> throw new IllegalArgumentException("Unsupported token type: " + tokenType);
        }

        //  Cleanup: Delete any old, unused tokens of this specific type so they don't have multiple active links
        secureTokenRepository.deleteUnusedTokensByUserAndType(user, tokenType);

        //  Generate and save the fresh token
        SecureToken secureToken = SecureToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .type(tokenType)
                .expiryAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES))
                .build();
        secureTokenRepository.save(secureToken);

        // Construct payload and publish event
        String actionUrl = frontendUrl + frontendRoute + "?token=" + secureToken.getToken();
        Map<String, Object> payload = Map.of(
                "firstName", user.getFirstName(),
                "actionUrl", actionUrl,
                "expiryMinutes", String.valueOf(expiryMinutes)
        );

        var event = new NotificationEvent(UUID.randomUUID().toString(), eventType, user.getEmail(), payload);
        eventPublisher.publish("email-topic", event);

        log.info("Successfully generated {} token and sent email event for: {}", tokenType, user.getEmail());
    }

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