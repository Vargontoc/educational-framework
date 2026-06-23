package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class WorldNarrativeSituation {

    private Long id;
    private String code;
    private String displayText;
    private SituationType situationType;
    private Tone tone;
    private Integer minAge;
    private Integer maxAge;
    private ContentStatus status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorldNarrativeSituation() {
    }

    public WorldNarrativeSituation(String code, String displayText, SituationType situationType,
                                   Tone tone, Integer minAge, Integer maxAge,
                                   ContentStatus status, Integer sortOrder) {
        this.code = code;
        this.displayText = displayText;
        this.situationType = situationType;
        this.tone = tone;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.status = status;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public SituationType getSituationType() {
        return situationType;
    }

    public void setSituationType(SituationType situationType) {
        this.situationType = situationType;
    }

    public Tone getTone() {
        return tone;
    }

    public void setTone(Tone tone) {
        this.tone = tone;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
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
