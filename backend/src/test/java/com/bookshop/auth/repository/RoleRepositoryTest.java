package com.bookshop.auth.repository;

import com.bookshop.shared.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RoleRepositoryTest extends AbstractBaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should find Role by name when it exists")
    void givenRoleName_whenFindByName_thenReturnRole() {
        // given - setup a role in the database
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        roleRepository.save(adminRole);

        // when - perform the search
        Optional<Role> foundRole = roleRepository.findByName("ROLE_ADMIN");

        // then - verify the results using AssertJ
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should return empty Optional when Role name does not exist")
    void givenNonExistentRoleName_whenFindByName_thenReturnEmpty() {

        Optional<Role> foundRole = roleRepository.findByName("ROLE_GHOST");

        assertThat(foundRole).isEmpty();
    }
}