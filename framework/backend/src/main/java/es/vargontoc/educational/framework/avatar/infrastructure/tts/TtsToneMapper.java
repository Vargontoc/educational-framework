package es.vargontoc.educational.framework.avatar.infrastructure.tts;

import es.vargontoc.educational.framework.content.model.AvatarTone;

public class TtsToneMapper {

    public String map(AvatarTone tone) {
        if (tone == null) {
            return "calm";
        }
        return switch (tone) {
            case CALM -> "calm";
            case JOYFUL -> "joyful";
            case ENTHUSIASTIC -> "enthusiastic";
            case SERIOUS -> "serious";
            case NEUTRAL -> "calm";
            case TENDER -> "tender";
            case MYSTERIOUS -> "mysterious";
        };
    }
}