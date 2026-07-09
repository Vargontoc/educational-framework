package es.vargontoc.educational.framework.content.model;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.time.LocalDateTime;

public class AccessibleColorPalette {

    private Long id;
    private Long accessibleColorId;
    private ColorVisionMode colorVisionMode;
    private String accessibleColorValue;
    private String accessibleLabelKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccessibleColorId() {
        return accessibleColorId;
    }

    public void setAccessibleColorId(Long accessibleColorId) {
        this.accessibleColorId = accessibleColorId;
    }

    public ColorVisionMode getColorVisionMode() {
        return colorVisionMode;
    }

    public void setColorVisionMode(ColorVisionMode colorVisionMode) {
        this.colorVisionMode = colorVisionMode;
    }

    public String getAccessibleColorValue() {
        return accessibleColorValue;
    }

    public void setAccessibleColorValue(String accessibleColorValue) {
        this.accessibleColorValue = accessibleColorValue;
    }

    public String getAccessibleLabelKey() {
        return accessibleLabelKey;
    }

    public void setAccessibleLabelKey(String accessibleLabelKey) {
        this.accessibleLabelKey = accessibleLabelKey;
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
