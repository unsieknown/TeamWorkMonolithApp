package com.mordiniaa.backend.events.authentication.events;

import java.time.Instant;
import java.util.UUID;

public record DeactivateSessionAndTokensEvent(Long familyId, UUID sessionId, Instant revokedAt) {
}