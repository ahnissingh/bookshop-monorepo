package com.bookshop.auth.service;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.auth.service.impl.AuthServiceImpl;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.exception.InvalidTokenException;
import com.bookshop.shared.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role vendorRole;
    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setup() {
        vendorRole = Role.builder().id(1L).name("ROLE_VENDOR").build();

        user = User.builder()
                .id(1L)
                .firstName("Ahnis")
                .lastName("Singh")
                .email("ahnisaneja@gmail.com@gmail.com")
                .username("vendor123")
                .password("encodedPassword")
                .roles(Set.of(vendorRole))
                .build();

        registrationRequest = new UserRegistrationRequest(
                "Ahnis", "Singh", "ahnisaneja@gmail.com@gmail.com", "vendor123", "rawPassword"
        );
        loginRequest = new LoginRequest("vendor123", "rawPassword");
    }

    @DisplayName("JUnit test for registerVendor method")
    @Test
    public void givenValidRequest_whenRegisterVendor_thenReturnAuthResponse() {
        given(userRepository.existsByUsernameOrEmail(registrationRequest.username(), registrationRequest.email()))
                .willReturn(false);
        given(roleRepository.findByName("ROLE_VENDOR"))
                .willReturn(Optional.of(vendorRole));
        given(passwordEncoder.encode(registrationRequest.password()))
                .willReturn("encodedPassword");
        given(userRepository.save(any(User.class)))
                .willReturn(user);
        given(jwtUtil.generateAccessToken(any(User.class)))
                .willReturn("mockAccessToken");
        given(jwtUtil.generateRefreshToken(any(User.class)))
                .willReturn("mockRefreshToken");

        AuthResponse response = authService.registerVendor(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mockAccessToken");
        assertThat(response.username()).isEqualTo("vendor123");
        assertThat(response.roles()).contains("ROLE_VENDOR");

        verify(userRepository).save(any(User.class));
    }

    @DisplayName("JUnit test for registerVendor method which throws exception")
    @Test
    public void givenExistingUser_whenRegisterVendor_thenThrowsException() {
        given(userRepository.existsByUsernameOrEmail(registrationRequest.username(), registrationRequest.email()))
                .willReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerVendor(registrationRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("JUnit test for login method")
    @Test
    public void givenLoginRequest_whenLogin_thenReturnAuthResponse() {
       
        given(userRepository.findByUsernameOrEmail(loginRequest.usernameOrEmail()))
                .willReturn(Optional.of(user));
        given(jwtUtil.generateAccessToken(user))
                .willReturn("mockAccessToken");
        given(jwtUtil.generateRefreshToken(user))
                .willReturn("mockRefreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mockAccessToken");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @DisplayName("JUnit test for refreshToken method")
    @Test
    public void givenValidRefreshToken_whenRefreshToken_thenReturnNewAuthResponse() {
        String oldToken = "oldRefreshToken";
        given(jwtUtil.extractUsername(oldToken)).willReturn("vendor123");
        given(userRepository.findByUsernameOrEmail("vendor123")).willReturn(Optional.of(user));
        given(jwtUtil.validateToken(oldToken, user)).willReturn(true);

        given(jwtUtil.generateAccessToken(user)).willReturn("newAccessToken");
        given(jwtUtil.generateRefreshToken(user)).willReturn("newRefreshToken");

        AuthResponse response = authService.refreshToken(oldToken);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("newAccessToken");
        assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
    }

    @DisplayName("JUnit test for refreshToken method when token is invalid")
    @Test
    public void givenInvalidRefreshToken_whenRefreshToken_thenThrowsInvalidTokenException() {
        String badToken = "badRefreshToken";
        given(jwtUtil.extractUsername(badToken)).willReturn("vendor123");
        given(userRepository.findByUsernameOrEmail("vendor123")).willReturn(Optional.of(user));

        // Simulate token validation failure
        given(jwtUtil.validateToken(badToken, user)).willReturn(false);

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(badToken));

        verify(jwtUtil, never()).generateAccessToken(any(User.class));
    }
}