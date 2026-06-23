package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class WorldDiscoveryElement {

    private Long id;
    private String code;
    private String displayName;
    private ElementType elementType;
    private Biome biome;
    private Integer minAge;
    private Integer maxAge;
    private ContentStatus status;
    private Long activityId;
    private Long topicId;
    private String visualAssetKey;
    private InteractionCueType interactionCueType;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorldDiscoveryElement() {
    }

    public WorldDiscoveryElement(String code, String displayName, ElementType elementType,
                                 Biome biome, Integer minAge, Integer maxAge,
                                 ContentStatus status, Long activityId, Long topicId,
                                 String visualAssetKey, InteractionCueType interactionCueType,
                                 Integer sortOrder) {
        this.code = code;
        this.displayName = displayName;
        this.elementType = elementType;
        this.biome = biome;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.status = status;
        this.activityId = activityId;
        this.topicId = topicId;
        this.visualAssetKey = visualAssetKey;
        this.interactionCueType = interactionCueType;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
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

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getVisualAssetKey() {
        return visualAssetKey;
    }

    public void setVisualAssetKey(String visualAssetKey) {
        this.visualAssetKey = visualAssetKey;
    }

    public InteractionCueType getInteractionCueType() {
        return interactionCueType;
    }

    public void setInteractionCueType(InteractionCueType interactionCueType) {
        this.interactionCueType = interactionCueType;
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
