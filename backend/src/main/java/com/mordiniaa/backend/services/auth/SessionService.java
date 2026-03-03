package com.mordiniaa.backend.services.auth;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.repositories.mysql.SessionRepository;
import com.mordiniaa.backend.security.service.SessionRedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionRedisService sessionRedisService;

    @Transactional
    public Session createSession(HttpServletRequest request) {

        Session session = new Session();
        session.setLastActivity(Instant.now());
        session.setIpAddress(request.getRemoteAddr());
        session.setUserAgent(request.getHeader("user-agent"));

        return sessionRepository.save(session);
    }

    @Transactional
    public void deactivateAllUserSessions(UUID userId) {
        Set<UUID> sessionIds = sessionRepository.findAllActiveUserSessions(userId);
        sessionRepository.revokeSessionsAndTokens(sessionIds);
        sessionIds.forEach(sessionRedisService::deleteSession);
    }
}
