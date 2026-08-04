package es.vargontoc.educational.framework.contact.infrastructure.dto;

import es.vargontoc.educational.framework.contact.model.ContactMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(

    ContactMessageType type,
    @NotBlank
    @Size(min = 1, max = 2_000)
    String message
) {}
