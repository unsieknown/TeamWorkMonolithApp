package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update refresh_tokens old
            join refresh_tokens new on new.id = :newTokenId
            join refresh_token_families rtf on old.refresh_token_family = rtf.id
            join sessions s on rtf.session_id = s.session_id
            set old.replaced_by_id = :newTokenId,
                old.revoked = true,
                old.revoked_at = :revokedAt,
                s.last_activity = NOW()
            where old.id = :oldTokenId
            """, nativeQuery = true)
    void rotateToken(Long newTokenId, Long oldTokenId, Instant revokedAt);

    @Query("""
            select rt.id
            from RefreshToken rt
            where rt.revoked = false
                and rt.refreshTokenFamily.revoked = false
                and rt.refreshTokenFamily.session.revoked = false
                and rt.refreshTokenFamily.session.sessionId = :sessionId
            """)
    Optional<Long> findTokenIdBySessionId(UUID sessionId);
}
