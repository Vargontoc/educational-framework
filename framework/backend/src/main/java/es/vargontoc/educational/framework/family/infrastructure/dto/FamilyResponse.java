package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDateTime;

public record FamilyResponse(
    Long id,
    String name,
    boolean audioGeneralEnabled,
    int audioGeneralVolume,
    boolean npcEnabled,
    boolean npcVoiceEnabled,
    int npcVoiceVolume,
    boolean narrativeVoiceEnabled,
    int narrativeVoiceVolume,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
