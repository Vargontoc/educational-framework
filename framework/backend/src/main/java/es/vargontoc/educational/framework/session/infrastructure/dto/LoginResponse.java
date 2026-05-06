package es.vargontoc.educational.framework.session.infrastructure.dto;

import java.time.LocalDateTime;

public record LoginResponse(
    String token,
    Long sessionId,
    Long familyId,
    LocalDateTime createdAt
) {
}
