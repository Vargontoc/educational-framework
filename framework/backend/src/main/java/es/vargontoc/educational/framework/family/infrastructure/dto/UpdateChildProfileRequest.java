package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDate;

public record UpdateChildProfileRequest(
    String name,
    LocalDate birthday,
    String avatar,
    Boolean ttsEnabled,
    Boolean agentEnabled
) {
}
