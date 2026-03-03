package com.mordiniaa.backend.events.authentication.listeners;

import com.mordiniaa.backend.events.authentication.events.DeactivateSessionAndTokensEvent;
import com.mordiniaa.backend.events.authentication.events.SessionMisuseEvent;
import com.mordiniaa.backend.security.service.SessionRedisService;
import com.mordiniaa.backend.services.auth.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionListener {

    private final SessionService sessionService;
    private final SessionRedisService sessionRedisService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handle(SessionMisuseEvent event) {
        sessionService.deactivateAllUserSessions(event.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DeactivateSessionAndTokensEvent event) {
        sessionRedisService.deleteSession(event.sessionId());
    }
}
