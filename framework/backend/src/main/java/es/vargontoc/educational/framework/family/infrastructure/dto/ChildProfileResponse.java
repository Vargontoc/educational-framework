package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChildProfileResponse(
    Long id,
    Long familyId,
    String name,
    boolean active,
    LocalDate birthday,
    String avatar,
    boolean ttsEnabled,
    boolean agentEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
