package com.bookshop.auth.integration;

import com.bookshop.auth.dto.LoginRequest;
import com.bookshop.auth.dto.UserRegistrationRequest;
import com.bookshop.auth.repository.RoleRepository;
import com.bookshop.auth.repository.UserRepository;
import com.bookshop.shared.AbstractIntegrationTest;
import com.bookshop.shared.entity.Role;
import com.bookshop.shared.entity.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vendor registered successfully"))
                .andExpect(jsonPath("$.data.username").value("ahnisaneja"));

        User savedUser = userRepository.findByUsernameOrEmail("ahnisaneja").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("ahnisaneja@gmail.com");
        assertThat(savedUser.getRoles()).extracting("name").contains("ROLE_VENDOR");
    }

    @Test
    public void givenDuplicateEmail_whenRegisterClient_thenReturnConflict() throws Exception {
        User existingUser = User.builder()
                .firstName("Test").lastName("User")
                .username("something_else").email("ahnisaneja@gmail.com")
                .password("password")
                .build();
        userRepository.save(existingUser);

        UserRegistrationRequest request = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );

        mockMvc.perform(post("/api/v1/auth/register/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username or Email is already taken."));
    }

    @Test
    public void givenValidCredentials_whenLogin_thenReturnRealTokens() throws Exception {
        UserRegistrationRequest registerRequest = new UserRegistrationRequest(
                "Ahnis", "Aneja", "ahnisaneja@gmail.com", "ahnisaneja", "password"
        );
        mockMvc.perform(post("/api/v1/auth/register/vendor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

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
}