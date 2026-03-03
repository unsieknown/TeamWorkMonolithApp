package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("""
            select s.sessionId
            from Session s
            where s.refreshTokenFamily.userId = :userId and s.refreshTokenFamily.revoked = false
            """)
    Set<UUID> findAllActiveUserSessions(UUID userId);

    @Modifying
    @Query(value = """
            update sessions as s
            join refresh_token_families as rtf on rtf.session_id = s.session_id
            left join refresh_tokens as rt on rt.refresh_token_family = rtf.id
            set s.revoked = true,
                s.last_activity = NOW(),
                rtf.revoked = true,
                rtf.revoked_at = NOW(),
                rt.revoked = true,
                rt.revoked_at = NOW()
            where s.session_id in :sessionIds
            """, nativeQuery = true)
    void revokeSessionsAndTokens(Set<UUID> sessionIds);
}
