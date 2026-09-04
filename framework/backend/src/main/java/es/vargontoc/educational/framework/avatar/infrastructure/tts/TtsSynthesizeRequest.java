package es.vargontoc.educational.framework.avatar.infrastructure.tts;



public record TtsSynthesizeRequest(
    String text,
    String locale,
    String tone,
    String context
) {
    public TtsSynthesizeRequest {
        if (locale == null || locale.isBlank()) {
            locale = "es";
        }
        if (tone == null || tone.isBlank()) {
            tone = "calm";
        }
        if (context == null || context.isBlank()) {
            context = "npc";
        }
    }
}