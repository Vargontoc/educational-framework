package es.vargontoc.educational.framework.session.infrastructure.dto;

import java.time.LocalDateTime;

public record ChildSessionResponse(
    Long id,
    Long childProfileId,
    Long familyId,
    String status,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Integer durationSeconds,
    LocalDateTime lastActivityAt
) {
}
