package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class AvatarEventCatalog {

    private Long id;
    private AvatarEventType eventType;
    private AvatarTone tone;
    private String locale;
    private String messageText;
    private ContentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AvatarEventType getEventType() {
        return eventType;
    }

    public void setEventType(AvatarEventType eventType) {
        this.eventType = eventType;
    }

    public AvatarTone getTone() {
        return tone;
    }

    public void setTone(AvatarTone tone) {
        this.tone = tone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
