package es.vargontoc.educational.framework.avatar.model;

import es.vargontoc.educational.framework.content.model.AvatarEventType;

import java.util.Map;

public class AvatarEventResult {

    private AvatarEventType eventType;
    private String text;
    private boolean audioAvailable;
    private Map<String, Object> audioMetadata;
    private boolean suppressed;

    public AvatarEventResult() {
    }

    public AvatarEventResult(AvatarEventType eventType, String text, boolean audioAvailable, Map<String, Object> audioMetadata, boolean suppressed) {
        this.eventType = eventType;
        this.text = text;
        this.audioAvailable = audioAvailable;
        this.audioMetadata = audioMetadata;
        this.suppressed = suppressed;
    }

    public AvatarEventType getEventType() {
        return eventType;
    }

    public void setEventType(AvatarEventType eventType) {
        this.eventType = eventType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isAudioAvailable() {
        return audioAvailable;
    }

    public void setAudioAvailable(boolean audioAvailable) {
        this.audioAvailable = audioAvailable;
    }

    public Map<String, Object> getAudioMetadata() {
        return audioMetadata;
    }

    public void setAudioMetadata(Map<String, Object> audioMetadata) {
        this.audioMetadata = audioMetadata;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }
}