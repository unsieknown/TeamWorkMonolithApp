package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenFamilyService {

    @Value("${security.app.refresh-token-family.max-session-days}")
    private int maxSessionDays;

    private final RefreshTokenFamilyRepository refreshTokenFamilyRepository;

    @Transactional
    public RefreshTokenFamily createNewFamily(UUID userId, Session session) {
        Instant now = Instant.now();
        RefreshTokenFamily newFamily = new RefreshTokenFamily(userId);
        newFamily.setCreatedAt(now);
        newFamily.setExpiresAt(now.plus(Duration.ofDays(maxSessionDays)));
        newFamily.setSession(session);

        return refreshTokenFamilyRepository.save(newFamily);
    }

    @Transactional
    public void deactivateUserAuthenticationFamily(Long familyId, Instant revokedAt) {
        refreshTokenFamilyRepository.deactivateAuthenticationsFamily(familyId, revokedAt);
    }

    @Transactional
    public void deactivateOldUserAuthentications(UUID userId, Long newFamilyId, Instant revokedAt) {
        refreshTokenFamilyRepository.deactivateOldUserAuthentications(userId, newFamilyId, revokedAt);
    }
}
