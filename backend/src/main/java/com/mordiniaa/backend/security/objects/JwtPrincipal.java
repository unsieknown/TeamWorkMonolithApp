package com.mordiniaa.backend.security.objects;

import java.util.List;
import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        UUID sessionId,
        List<String> roles
) {
}
