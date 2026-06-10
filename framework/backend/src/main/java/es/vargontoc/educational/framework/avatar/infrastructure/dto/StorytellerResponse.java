package es.vargontoc.educational.framework.avatar.infrastructure.dto;

import es.vargontoc.educational.framework.avatar.model.StorytellerResult;

import java.util.Map;

public record StorytellerResponse(
    String text,
    boolean audioAvailable,
    Map<String, Object> audioMetadata
) {
    public static StorytellerResponse fromResult(StorytellerResult result) {
        return new StorytellerResponse(
            result.getText(),
            result.isAudioAvailable(),
            result.getAudioMetadata()
        );
    }
}