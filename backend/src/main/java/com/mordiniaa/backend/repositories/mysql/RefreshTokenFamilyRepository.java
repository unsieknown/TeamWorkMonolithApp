package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RefreshTokenFamilyRepository extends JpaRepository<RefreshTokenFamily, Long> {

    @Modifying
    @Query(value = """
            update refresh_token_families family
            left join refresh_tokens token on family.id = token.refresh_token_family
            join sessions s on s.session_id = family.session_id
            set family.revoked = true,
                family.revoked_at = :revokedAt,
                token.revoked = true,
                token.revoked_at = :revokedAt,
                s.revoked = true,
                s.last_activity = :revokedAt
                where family.id = :familyId
            """, nativeQuery = true)
    void deactivateAuthenticationsFamily(Long familyId, Instant revokedAt);

    @Modifying
    @Query(value = """
            update refresh_token_families family
            left join refresh_tokens token on family.id = token.refresh_token_family
            join sessions s on s.session_id = family.session_id
            set family.revoked = true,
                family.revoked_at = :revokedAt,
                token.revoked = true,
                token.revoked_at = :revokedAt,
                s.revoked = true,
                s.last_activity = :revokedAt
                where family.user_id = :userId and family.id != :newFamilyId and (s.revoked = false or family.revoked = false or token.revoked = false)
            """, nativeQuery = true)
    void deactivateOldUserAuthentications(UUID userId, Long newFamilyId, Instant revokedAt);
}
