package es.vargontoc.educational.framework.family.infrastructure.dto;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChildProfileResponse(
    Long id,
    Long familyId,
    String name,
    boolean active,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
