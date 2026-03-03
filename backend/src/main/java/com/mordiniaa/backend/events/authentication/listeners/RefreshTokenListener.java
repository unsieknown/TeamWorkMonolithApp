package com.mordiniaa.backend.events.authentication.listeners;

import com.mordiniaa.backend.events.authentication.events.DeactivateSessionAndTokensEvent;
import com.mordiniaa.backend.events.authentication.events.DeactivateOldUserAuthenticationsEvent;
import com.mordiniaa.backend.security.service.SessionRedisService;
import com.mordiniaa.backend.security.service.token.RefreshTokenFamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RefreshTokenListener {

    private final RefreshTokenFamilyService refreshTokenFamilyService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DeactivateSessionAndTokensEvent event) {
        refreshTokenFamilyService.deactivateUserAuthenticationFamily(
                event.familyId(),
                event.revokedAt()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DeactivateOldUserAuthenticationsEvent event) {
        refreshTokenFamilyService.deactivateOldUserAuthentications(
                event.userId(),
                event.newFamilyId(),
                event.revokedAt()
        );
    }
}
