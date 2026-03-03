package com.mordiniaa.backend.events.authentication.events;

import java.util.UUID;

public record SessionMisuseEvent(UUID userId) {
}
