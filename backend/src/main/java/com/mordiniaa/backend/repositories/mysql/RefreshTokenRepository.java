package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update refresh_tokens old
            join refresh_tokens new on new.id = :newTokenId
            set old.replaced_by_id = new.id,
                old.revoked = true,
                old.revoked_at = :revokedAt
            where old.id = :oldTokenId
            """, nativeQuery = true)
    void rotateToken(Long newTokenId, Long oldTokenId, Instant revokedAt);
}
