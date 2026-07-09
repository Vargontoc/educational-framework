package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class AccessibleColor {

    private Long id;
    private String conceptualIdentity;
    private String labelKey;
    private String shapeIcon;
    private String symbol;
    private ContentStatus status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConceptualIdentity() {
        return conceptualIdentity;
    }

    public void setConceptualIdentity(String conceptualIdentity) {
        this.conceptualIdentity = conceptualIdentity;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getShapeIcon() {
        return shapeIcon;
    }

    public void setShapeIcon(String shapeIcon) {
        this.shapeIcon = shapeIcon;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
