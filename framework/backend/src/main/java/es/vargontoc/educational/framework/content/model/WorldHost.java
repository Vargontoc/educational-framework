package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class WorldHost {

    private Long id;
    private String code;
    private String displayName;
    private Biome biome;
    private String description;
    private Integer minAge;
    private Integer maxAge;
    private ContentStatus status;
    private Integer sortOrder;
    private String visualAssetKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorldHost() {
    }

    public WorldHost(String code, String displayName, Biome biome, String description,
                     Integer minAge, Integer maxAge, ContentStatus status,
                     Integer sortOrder, String visualAssetKey) {
        this.code = code;
        this.displayName = displayName;
        this.biome = biome;
        this.description = description;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.status = status;
        this.sortOrder = sortOrder;
        this.visualAssetKey = visualAssetKey;
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

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getVisualAssetKey() {
        return visualAssetKey;
    }

    public void setVisualAssetKey(String visualAssetKey) {
        this.visualAssetKey = visualAssetKey;
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
