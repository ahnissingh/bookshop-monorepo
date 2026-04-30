package com.bookshop.auth.repository;

import com.bookshop.shared.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryTests {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .firstName("Ahnis")
                .lastName("Aneja")
                .email("ahnisaneja@gmail.com")
                .username("ahnisaneja")
                .password("ahnisaneja1234")
                .build();
    }

    @DisplayName("JUnit test for custom query findByUsernameOrEmail using username")
    @Test
    public void givenUsername_whenFindByUsernameOrEmail_thenReturnUserObject() {
        // given - precondition or setup
        userRepository.save(user);

        // when - action or the behaviour that we are going test
        User savedUser = userRepository.findByUsernameOrEmail(user.getUsername()).get();

        // then - verify the output
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("ahnisaneja");
    }

    @DisplayName("JUnit test for custom query findByUsernameOrEmail using email")
    @Test
    public void givenEmail_whenFindByUsernameOrEmail_thenReturnUserObject() {
        userRepository.save(user);

        User savedUser = userRepository.findByUsernameOrEmail(user.getEmail()).get();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("ahnisaneja@gmail.com");
    }

    @DisplayName("JUnit test for derived query existsByUsername")
    @Test
    public void givenUsername_whenExistsByUsername_thenReturnTrue() {
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername(user.getUsername());

        assertThat(exists).isTrue();
    }

    @DisplayName("JUnit test for derived query existsByEmail")
    @Test
    public void givenEmail_whenExistsByEmail_thenReturnTrue() {
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail(user.getEmail());

        assertThat(exists).isTrue();
    }

    @DisplayName("JUnit test for derived query existsByUsernameOrEmail - Positive Scenario")
    @Test
    public void givenUsernameAndEmail_whenExistsByUsernameOrEmail_thenReturnTrue() {
        userRepository.save(user);

        boolean exists = userRepository.existsByUsernameOrEmail(user.getUsername(), "random@email.com");

        assertThat(exists).isTrue();
    }

    @DisplayName("JUnit test for derived query existsByUsernameOrEmail - Negative Scenario")
    @Test
    public void givenNonExistingUsernameAndEmail_whenExistsByUsernameOrEmail_thenReturnFalse() {
        userRepository.save(user);

        boolean exists = userRepository.existsByUsernameOrEmail("ghost", "ghost@gmail.com");

        assertThat(exists).isFalse();
    }
}