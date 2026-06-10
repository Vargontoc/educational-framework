package es.vargontoc.educational.framework.avatar.infrastructure.tts;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TtsSynthesizeRequest(
    String text,
    String locale,
    String tone,
    @JsonProperty("voice_profile")
    String voiceProfile
) {
    public TtsSynthesizeRequest {
        if (locale == null || locale.isBlank()) {
            locale = "es";
        }
        if (tone == null || tone.isBlank()) {
            tone = "calm";
        }
        if (voiceProfile == null || voiceProfile.isBlank()) {
            voiceProfile = "npc";
        }
    }
}