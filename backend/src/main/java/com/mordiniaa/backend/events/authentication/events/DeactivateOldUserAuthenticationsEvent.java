package com.mordiniaa.backend.events.authentication.events;

import java.time.Instant;
import java.util.UUID;

public record DeactivateOldUserAuthenticationsEvent(UUID userId, Long newFamilyId, Instant revokedAt) {
}
