package com.bookshop.auth.controller;

import com.bookshop.auth.dto.AuthResponse;
import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.security.CustomAccessDeniedHandler;
import com.bookshop.auth.security.CustomAuthenticationEntryPoint;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.auth.service.AuthService;
import com.bookshop.auth.service.CustomUserDetailsService;
import com.bookshop.auth.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    //Note it is very important to know that in boot 4 use  import tools.jackson.databind.ObjectMapper instead of fasterxml one
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;


    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;


    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    public void setup() {
        registrationRequest = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );

        loginRequest = new LoginRequest("ahnisaneja@gmail.com", "password");

        authResponse = new AuthResponse(
                "mock-access-token",
                "mock-refresh-token",
                "ahnisaneja@gmail.com",
                Set.of("ROLE_VENDOR")
        );
    }

    @Test
    public void givenValidRequest_whenRegisterVendor_thenReturnSuccess() throws Exception {
        given(authService.registerVendor(any(UserRegistrationRequest.class))).willReturn(authResponse);
        willDoNothing().given(cookieUtil).attachAuthCookies(any(HttpServletResponse.class), any(AuthResponse.class));

        mockMvc.perform(post("/api/v1/auth/register/vendor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vendor registered successfully"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja@gmail.com"));
    }

    @Test
    public void givenValidRequest_whenRegisterClient_thenReturnSuccess() throws Exception {
        given(authService.registerClient(any(UserRegistrationRequest.class))).willReturn(authResponse);
        willDoNothing().given(cookieUtil).attachAuthCookies(any(HttpServletResponse.class), any(AuthResponse.class));

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client registered successfully"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja@gmail.com"));
    }

    @Test
    public void givenValidRequest_whenLogin_thenReturnSuccess() throws Exception {
        given(authService.login(any(LoginRequest.class))).willReturn(authResponse);
        willDoNothing().given(cookieUtil).attachAuthCookies(any(HttpServletResponse.class), any(AuthResponse.class));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja@gmail.com"));
    }

    @Test
    public void givenLogoutRequest_whenLogout_thenReturnSuccess() throws Exception {
        willDoNothing().given(cookieUtil).clearAuthCookies(any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    public void givenRefreshTokenCookie_whenRefresh_thenReturnSuccess() throws Exception {
        given(authService.refreshToken("valid-refresh-token")).willReturn(authResponse);
        willDoNothing().given(cookieUtil).attachAuthCookies(any(HttpServletResponse.class), any(AuthResponse.class));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja@gmail.com"));
    }

    @Test
    public void givenMissingRefreshTokenCookie_whenRefresh_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().is4xxClientError());
    }
}