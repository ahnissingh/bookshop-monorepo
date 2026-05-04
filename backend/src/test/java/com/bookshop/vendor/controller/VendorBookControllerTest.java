package com.bookshop.vendor.controller;

import com.bookshop.auth.config.CorsConfigProperties;
import com.bookshop.auth.config.SecurityConfig;
import com.bookshop.auth.security.CustomAccessDeniedHandler;
import com.bookshop.auth.security.CustomAuthenticationEntryPoint;
import com.bookshop.auth.security.JwtAuthenticationFilter;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.auth.service.CustomUserDetailsService;
import com.bookshop.auth.util.CookieUtil;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import com.bookshop.vendor.dto.BookRequest;
import com.bookshop.vendor.dto.BookResponse;
import com.bookshop.vendor.service.VendorBookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VendorBookController.class)
@Import({
        SecurityConfig.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        JwtAuthenticationFilter.class
})
@EnableMethodSecurity
@EnableConfigurationProperties(CorsConfigProperties.class)
public class VendorBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VendorBookService vendorBookService;

    @MockitoBean
    private CookieUtil cookieUtil;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;


    private User vendor;
    private User client;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    public void setup() {
        Role vendorRole = Role.builder().name("ROLE_VENDOR").build();
        Role clientRole = Role.builder().name("ROLE_CLIENT").build();

        vendor = User.builder()
                .id(1L)
                .username("vendor123")
                .password("password")
                .roles(Set.of(vendorRole))
                .build();

        client = User.builder()
                .id(2L)
                .username("client123")
                .password("password")
                .roles(Set.of(clientRole))
                .build();

        bookRequest = new BookRequest(
                "Spring Boot Mastery", "Ahnis Singh", "A deep dive", BigDecimal.valueOf(29.99), "New", "Great book"
        );

        bookResponse = new BookResponse(
                100L, "Spring Boot Mastery", "Ahnis Singh", "A deep dive", BigDecimal.valueOf(29.99), "New", "Great book", "/api/v1/vendor/books/100/picture", Instant.now()
        );
    }

    @Test
    @DisplayName("Should return paginated books for authorized vendor")
    public void givenVendor_whenGetMyBooks_thenReturnSuccess() throws Exception {
        given(vendorBookService.getMyBooks(any(User.class), any())).willReturn(new PageImpl<>(List.of(bookResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/vendor/books")
                        .with(user(vendor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Books fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot Mastery"));
    }

    @Test
    @DisplayName("Should create a book for authorized vendor")
    public void givenValidRequest_whenCreateBook_thenReturnSuccess() throws Exception {
        given(vendorBookService.createBook(any(User.class), any(BookRequest.class))).willReturn(bookResponse);

        mockMvc.perform(post("/api/v1/vendor/books")
                        .with(user(vendor))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Mastery"));
    }

    @Test
    @DisplayName("Should update book for authorized vendor")
    public void givenValidRequest_whenUpdateBook_thenReturnSuccess() throws Exception {
        given(vendorBookService.updateBook(any(User.class), eq(100L), any(BookRequest.class))).willReturn(bookResponse);

        mockMvc.perform(put("/api/v1/vendor/books/{id}", 100L)
                        .with(user(vendor))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book updated successfully"));
    }

    @Test
    @DisplayName("Should delete book for authorized vendor")
    public void givenValidId_whenDeleteBook_thenReturnSuccess() throws Exception {
        willDoNothing().given(vendorBookService).deleteBook(any(User.class), eq(100L));

        mockMvc.perform(delete("/api/v1/vendor/books/{id}", 100L)
                        .with(user(vendor))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book deleted successfully"));
    }

    @Test
    @DisplayName("Should successfully upload multipart picture")
    public void givenMultipartFile_whenUploadPicture_thenReturnSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "cover.jpg", "image/jpeg", "image data".getBytes());
        willDoNothing().given(vendorBookService).uploadBookPicture(any(User.class), eq(100L), any());

        mockMvc.perform(multipart("/api/v1/vendor/books/{id}/picture", 100L)
                        .file(mockFile)
                        .with(user(vendor))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Picture uploaded successfully"));
    }

    @Test
    @DisplayName("Should return raw byte array for picture GET endpoint")
    public void givenValidId_whenGetBookPicture_thenReturnImageBytes() throws Exception {
        byte[] mockBytes = {1, 2, 3};
        given(vendorBookService.getRawBookPicture(any(User.class), eq(100L))).willReturn(mockBytes);

        mockMvc.perform(get("/api/v1/vendor/books/{id}/picture", 100L)
                        .with(user(vendor)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(mockBytes));
    }

    //Some security test
    @Test
    @DisplayName("Should return 403 Forbidden when a Client tries to access Vendor endpoints")
    public void givenClientUser_whenGetMyBooks_thenReturnsForbidden() throws Exception {
        // Because the controller requires 'ROLE_VENDOR', sending a user with 'ROLE_CLIENT' should fail
        mockMvc.perform(get("/api/v1/vendor/books")
                        .with(user(client)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when accessing without authentication")
    public void givenUnauthenticatedRequest_whenGetMyBooks_thenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vendor/books"))
                .andExpect(status().isUnauthorized());
    }
}