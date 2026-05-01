package es.vargontoc.educational.framework.family.infrastructure.dto;

public record CreateFamilyRequest(
    String name,
    String pin,
    boolean ttsEnabled,
    boolean agentEnabled
) {
}
