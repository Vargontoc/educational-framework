package es.vargontoc.educational.framework.avatar.model;

import java.util.Map;

public class StorytellerResult {
    private String text;
    private boolean audioAvailable;
    private Map<String, Object> audioMetadata;
    private byte[] audioData;

    public StorytellerResult() {
    }

    public StorytellerResult(String text, boolean audioAvailable, Map<String, Object> audioMetadata, byte[] audioData) {
        this.text = text;
        this.audioAvailable = audioAvailable;
        this.audioMetadata = audioMetadata;
        this.audioData = audioData;
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

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }
}