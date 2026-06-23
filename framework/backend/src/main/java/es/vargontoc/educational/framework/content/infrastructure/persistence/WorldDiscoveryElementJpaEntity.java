package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_discovery_element")
public class WorldDiscoveryElementJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false, length = 50)
    private String elementType;

    @Column(nullable = false, length = 50)
    private String biome;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "visual_asset_key", length = 100)
    private String visualAssetKey;

    @Column(name = "interaction_cue_type", length = 50)
    private String interactionCueType;

    @Column(name = "sort_order")
    private Integer sortOrder;

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

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getInteractionCueType() {
        return interactionCueType;
    }

    public void setInteractionCueType(String interactionCueType) {
        this.interactionCueType = interactionCueType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
