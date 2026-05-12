package com.bookshop.auth.repository;

import com.bookshop.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = :input OR u.email = :input")
    Optional<User> findByUsernameOrEmail(@Param("input") String input);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameOrEmail(String username, String email);
    @Modifying
    @Query("DELETE FROM User u WHERE u.enabled = false AND u.createdAt <= :cutoffDate")
    int deleteUnverifiedUsersOlderThan(@Param("cutoffDate") Instant cutoffDate);

    Optional<User> findByEmailAndEnabled(String email, boolean enabled);

    @Query("SELECT u.enabled FROM User u WHERE u.email = :email")
    Optional<Boolean> findEnabledStatusByEmail(@Param("email") String email);

    boolean existsByEnabled(boolean enabled);

    boolean existsByEmailAndEnabled(String email, boolean enabled);

}