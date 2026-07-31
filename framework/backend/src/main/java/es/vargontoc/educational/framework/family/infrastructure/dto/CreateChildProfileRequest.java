package es.vargontoc.educational.framework.family.infrastructure.dto;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.time.LocalDate;

public record CreateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    boolean npcVoiceEnabled,
    boolean npcEnabled,
    int npcVoiceVolume,
    ColorVisionMode colorVisionMode
) {
}
