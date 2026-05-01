package es.vargontoc.educational.framework.family.infrastructure.dto;

public record UpdateFamilyRequest(
    String name,
    String pin,
    Boolean ttsEnabled,
    Boolean agentEnabled
) {
}
