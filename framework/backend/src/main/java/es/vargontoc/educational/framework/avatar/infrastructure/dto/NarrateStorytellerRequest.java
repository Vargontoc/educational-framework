package es.vargontoc.educational.framework.avatar.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.AvatarTone;

public record NarrateStorytellerRequest(
    String text,
    String locale,
    AvatarTone tone
) {
    public String localeOrDefault() {
        return (locale != null && !locale.isBlank()) ? locale : "es";
    }

    public AvatarTone toneOrDefault() {
        return tone != null ? tone : AvatarTone.NEUTRAL;
    }
}