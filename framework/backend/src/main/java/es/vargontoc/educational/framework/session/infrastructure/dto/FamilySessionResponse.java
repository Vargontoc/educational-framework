package es.vargontoc.educational.framework.session.infrastructure.dto;

import java.time.LocalDateTime;

public record FamilySessionResponse(
    Long id,
    Long familyId,
    String status,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {
}
