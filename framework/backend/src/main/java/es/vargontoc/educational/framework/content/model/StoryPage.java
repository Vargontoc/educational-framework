package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class StoryPage {

    private Long id;
    private Long storyId;
    private Integer pageOrder;
    private String text;
    private String imageResourceRef;
    private String audioResourceRef;
    private ContentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public Integer getPageOrder() {
        return pageOrder;
    }

    public void setPageOrder(Integer pageOrder) {
        this.pageOrder = pageOrder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageResourceRef() {
        return imageResourceRef;
    }

    public void setImageResourceRef(String imageResourceRef) {
        this.imageResourceRef = imageResourceRef;
    }

    public String getAudioResourceRef() {
        return audioResourceRef;
    }

    public void setAudioResourceRef(String audioResourceRef) {
        this.audioResourceRef = audioResourceRef;
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
