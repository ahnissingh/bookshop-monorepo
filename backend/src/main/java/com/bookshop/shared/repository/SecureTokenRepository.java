package com.bookshop.shared.repository;

import com.bookshop.shared.entity.SecureToken;
import com.bookshop.shared.entity.TokenType;
import com.bookshop.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SecureTokenRepository extends JpaRepository<SecureToken, Long> {
    Optional<SecureToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM SecureToken st WHERE st.validatedAt <= :cutoffDate")
    int deleteValidatedTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);

    @Modifying
    @Query("DELETE FROM SecureToken st WHERE st.expiryAt <= :cutoffDate AND st.validatedAt IS NULL")
    int deleteExpiredUnusedTokensOlderThan(@Param("cutoffDate") Instant cutoffDate);

    @Modifying
    @Query("DELETE FROM SecureToken st WHERE st.user = :user AND st.type = :type AND st.validatedAt IS NULL")
    void deleteUnusedTokensByUserAndType(@Param("user") User user, @Param("type") TokenType type);
}
