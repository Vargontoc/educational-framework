package es.vargontoc.educational.framework.family.infrastructure.dto;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.time.LocalDate;

public record CreateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    boolean ttsEnabled,
    boolean agentEnabled,
    ColorVisionMode colorVisionMode
) {
}
