package es.vargontoc.educational.framework.family.infrastructure.dto;

public record UpdateFamilyRequest(
    String name,
    String pin,
    Boolean ttsEnabled,
    Boolean agentEnabled,
    Boolean audioGeneralEnabled,
    Integer audioGeneralVolume,
    Boolean npcEnabled,
    Boolean npcVoiceEnabled,
    Integer npcVoiceVolume,
    Boolean narrativeVoiceEnabled,
    Integer narrativeVoiceVolume
) {
}
