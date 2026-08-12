package es.vargontoc.educational.framework.agents.infrastructure.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateConversationTitleDto(
    @NotNull
    UUID conversation,
    @NotBlank
    @Size(max = 40, message = "Titulo tiene que ser menor a 40 caracteres")
    String title) {

}
