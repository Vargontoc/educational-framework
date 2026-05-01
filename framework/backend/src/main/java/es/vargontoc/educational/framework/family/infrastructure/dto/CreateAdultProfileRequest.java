package es.vargontoc.educational.framework.family.infrastructure.dto;

import java.time.LocalDate;

public record CreateAdultProfileRequest(
    String name,
    LocalDate birthday,
    String avatar
) {
}
