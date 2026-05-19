package com.bookshop.vendor.integration;

import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.auth.security.JwtUtil;
import com.bookshop.shared.AbstractIntegrationTest;
import com.bookshop.shared.entity.Book;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import com.bookshop.shared.repository.BookRepository;
import com.bookshop.vendor.dto.BookRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class VendorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User validVendor;
    private User hackerVendor;
    private User regularClient;

    @BeforeEach
    public void setup() {

        bookRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role vendorRole = roleRepository.save(Role.builder().name("ROLE_VENDOR").build());
        Role clientRole = roleRepository.save(Role.builder().name("ROLE_CLIENT").build());

        validVendor = userRepository.save(
                User.builder()
                        .username("real_vendor")
                        .email("vendor@test.com")
                        .password("pass")
                        .firstName("vendor_firstname")
                        .lastName("vendor_lastname")
                        .roles(Set.of(vendorRole))
                        .build()
        );

        hackerVendor = userRepository.save(User.builder()
                .username("hacker_vendor")
                .email("hacker@test.com")
                .password("pass")
                .firstName("vendor_hacker_firstname")
                .lastName("vendor_hacker_lastname")
                .roles(Set.of(vendorRole)).build());

        regularClient = userRepository.save(User.builder()
                .username("regular_client")
                .email("client@test.com")
                .password("pass")
                .firstName("client_firstname")
                .lastName("client_lastname")
                .roles(Set.of(clientRole)).build());
    }

    /**
     * Helper method to generate a REAL authentication cookie for a given user.
     */
    private Cookie getAuthCookie(User user) {
        String token = jwtUtil.generateAccessToken(user);
        return new Cookie("accessToken", token);
    }

    @Test
    @DisplayName("Should return paginated books for authorized vendor")
    public void givenVendor_whenGetMyBooks_thenReturnSuccess() throws Exception {
        // Save test books
        bookRepository.save(Book.builder().title("Vendor Book 1").author("A").quantity(10).price(BigDecimal.TEN).user(validVendor).build());
        Thread.sleep(10); // Guarantee distinct timestamps
        bookRepository.save(Book.builder().title("Vendor Book 2").author("B").quantity(10).price(BigDecimal.TEN).user(validVendor).build());

        mockMvc.perform(get("/api/v1/vendor/books")
                        .cookie(getAuthCookie(validVendor))) // FIX: Using real JWT cookie
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Books fetched successfully"))
                // Expect Vendor Book 2 because default sort is createdAt DESC
                .andExpect(jsonPath("$.data.content[0].title").value("Vendor Book 2"));
    }

    @Test
    @DisplayName("Should create a book for authorized vendor using Multipart")
    public void givenValidRequest_whenCreateBook_thenSaveToRealDatabase() throws Exception {

        BookRequest request = new BookRequest(
                "Integration Testing Mastery", "Ahnis Singh", "Subtitle", BigDecimal.valueOf(19.99), "New", "Desc", 50
        );

        // Create a MockMultipartFile containing the JSON payload
        MockMultipartFile bookPart = new MockMultipartFile(
                "book", "", "application/json", objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/v1/vendor/books")
                        .file(bookPart)
                        .cookie(getAuthCookie(validVendor))) // FIX: Using real JWT cookie instead of mock user()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book created successfully"))
                .andExpect(jsonPath("$.data.title").value("Integration Testing Mastery"));

        // Verify it actually saved to the DB
        assertThat(bookRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Should prevent a Vendor from deleting another Vendor's book")
    public void givenHackerVendor_whenDeleteOtherVendorBook_thenThrowErrorAndDoNotDelete() throws Exception {
        // Give validVendor a book
        Book targetBook = bookRepository.save(
                Book.builder().title("Precious Book").author("A").quantity(10).price(BigDecimal.TEN).user(validVendor).build()
        );

        // Hacker tries to delete it
        mockMvc.perform(delete("/api/v1/vendor/books/" + targetBook.getId())
                        .cookie(getAuthCookie(hackerVendor)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Book not found or you are not authorized to access it"));

        // Book must still exist!
        assertThat(bookRepository.findById(targetBook.getId())).isPresent();
    }

    @Test
    @DisplayName("Should return 403 Forbidden when a regular Client accesses vendor routes")
    public void givenClient_whenAccessVendorRoutes_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/vendor/books")
                        .cookie(getAuthCookie(regularClient)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }
}