package es.vargontoc.educational.framework.family.infrastructure.dto;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.time.LocalDate;

public record UpdateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    Boolean npcVoiceEnabled,
    Boolean npcEnabled,
    Integer npcVoiceVolume,
    ColorVisionMode colorVisionMode
) {
}
