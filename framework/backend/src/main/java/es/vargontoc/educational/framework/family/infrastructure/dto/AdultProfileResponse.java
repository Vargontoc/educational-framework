package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdultProfileResponse(
    Long id,
    Long familyId,
    String name,
    LocalDate birthday,
    String avatar,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
