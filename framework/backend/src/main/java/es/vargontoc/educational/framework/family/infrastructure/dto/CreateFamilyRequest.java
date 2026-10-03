package es.vargontoc.educational.framework.family.infrastructure.dto;

public record CreateFamilyRequest(
    String name,
    String pin,
    Boolean ttsEnabled,
    Boolean agentEnabled
) {
    // Opcionales en el contrato (el frontend solo envia name y pin): ausentes equivalen a true,
    // igual que el valor por defecto de las columnas tts_enabled/agent_enabled.
    public boolean ttsEnabledOrDefault() {
        return ttsEnabled == null || ttsEnabled;
    }

    public boolean agentEnabledOrDefault() {
        return agentEnabled == null || agentEnabled;
    }
}
