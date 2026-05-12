package com.bookshop.auth.integration;

import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.shared.AbstractIntegrationTest;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.repository.SecureTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SecureTokenRepository secureTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_VENDOR").build());
        roleRepository.save(Role.builder().name("ROLE_CLIENT").build());
    }

    @Test
    public void givenValidRequest_whenRegisterVendor_thenSaveToRealDatabase() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );

        mockMvc.perform(post("/api/v1/auth/register/vendor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vendor registered successfully. Please check your email for the verification link."));

        User savedUser = userRepository.findByUsernameOrEmail("ahnisaneja").orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo("ahnisaneja@gmail.com");
        assertThat(savedUser.getRoles()).extracting("name").contains("ROLE_VENDOR");
        assertThat(savedUser.isEnabled()).isFalse();
    }

    @Test
    public void givenDuplicateEmail_whenRegisterClient_thenReturnConflict() throws Exception {
        User existingUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .username("something_else")
                .email("ahnisaneja@gmail.com")
                .password("password")
                .enabled(true)
                .build();

        userRepository.save(existingUser);

        UserRegistrationRequest request = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This email is already registered."));
    }

    @Test
    public void givenUnverifiedEmail_whenRegisterClient_thenReturnConflict() throws Exception {
        User unverifiedUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .username("something_else")
                .email("ahnisaneja@gmail.com")
                .password("password")
                .enabled(false)
                .build();

        userRepository.save(unverifiedUser);

        UserRegistrationRequest request = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User is unverified, please retry verifying your email"));
    }

    @Test
    public void givenValidCredentials_whenLogin_thenReturnRealTokens() throws Exception {
        Role vendorRole = roleRepository.findByName("ROLE_VENDOR").orElseThrow();

        User enabledUser = User.builder()
                .firstName("Ahnis")
                .lastName("Aneja")
                .username("ahnisaneja")
                .email("ahnisaneja@gmail.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(vendorRole))
                .enabled(true)
                .build();

        userRepository.save(enabledUser);

        LoginRequest loginRequest = new LoginRequest("ahnisaneja@gmail.com", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }


    @Test
    public void givenValidToken_whenVerifyEmail_thenUserBecomesEnabled() throws Exception {
        User unverifiedUser = User.builder()
                .firstName("John").lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(roleRepository.findByName("ROLE_CLIENT").orElseThrow()))
                .enabled(false)
                .build();
        userRepository.save(unverifiedUser);

        com.bookshop.shared.entity.SecureToken token = com.bookshop.shared.entity.SecureToken.builder()
                .token(java.util.UUID.randomUUID().toString())
                .user(unverifiedUser)
                .type(com.bookshop.shared.entity.TokenType.VERIFICATION)
                .expiryAt(java.time.Instant.now().plusSeconds(3600))
                .build();
        secureTokenRepository.save(token);

        mockMvc.perform(post("/api/v1/auth/verify")
                        .param("token", token.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email successfully verified. You can now log in."));

        User verifiedUser = userRepository.findByUsernameOrEmail("johndoe").orElseThrow();
        assertThat(verifiedUser.isEnabled()).isTrue();
    }


    @Test
    public void givenValidEmail_whenForgotPassword_thenReturnOk() throws Exception {
        User enabledUser = User.builder()
                .firstName("Jane").lastName("Doe")
                .username("janedoe")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(roleRepository.findByName("ROLE_CLIENT").orElseThrow()))
                .enabled(true) // User must be verified to reset password
                .build();
        userRepository.save(enabledUser);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .param("email", "jane@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an active account exists with that email, a password reset link has been sent."));

        // Verify a token was actually generated in the DB
        assertThat(secureTokenRepository.findAll()).hasSize(1);
    }

    @Test
    public void givenValidToken_whenResetPassword_thenPasswordIsChanged() throws Exception {
        User enabledUser = User.builder()
                .firstName("Mark").lastName("Smith")
                .username("marksmith")
                .email("mark@example.com")
                .password(passwordEncoder.encode("oldPassword"))
                .roles(Set.of(roleRepository.findByName("ROLE_CLIENT").orElseThrow()))
                .enabled(true)
                .build();
        userRepository.save(enabledUser);

        com.bookshop.shared.entity.SecureToken token = com.bookshop.shared.entity.SecureToken.builder()
                .token(java.util.UUID.randomUUID().toString())
                .user(enabledUser)
                .type(com.bookshop.shared.entity.TokenType.PASSWORD_RESET)
                .expiryAt(java.time.Instant.now().plusSeconds(900))
                .build();
        secureTokenRepository.save(token);

        com.bookshop.auth.dto.PasswordResetRequest resetRequest =
                new com.bookshop.auth.dto.PasswordResetRequest(token.getToken(), "newSecurePassword");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been successfully reset. You can now log in."));

        User updatedUser = userRepository.findByUsernameOrEmail("marksmith").orElseThrow();
        assertThat(passwordEncoder.matches("newSecurePassword", updatedUser.getPassword())).isTrue();
    }


    @Test
    public void givenLogoutRequest_whenLogout_thenCookiesAreCleared() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(cookie().maxAge("accessToken", 0)) // Max age 0 means cookie deleted
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    public void givenValidRefreshToken_whenRefresh_thenReturnNewTokens() throws Exception {
        User user = User.builder()
                .firstName("Ref").lastName("Resh")
                .username("refresher")
                .email("refresh@example.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(roleRepository.findByName("ROLE_CLIENT").orElseThrow()))
                .enabled(true)
                .build();
        userRepository.save(user);

        // Generate a real JWT token for this user to test the refresh endpoint
        String validRefreshToken = jwtUtil.generateRefreshToken(user);
        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refreshToken", validRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(cookie().exists("accessToken"));
    }
}