package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDateTime;

public record FamilyResponse(
    Long id,
    String name,
    boolean ttsEnabled,
    boolean agentEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
