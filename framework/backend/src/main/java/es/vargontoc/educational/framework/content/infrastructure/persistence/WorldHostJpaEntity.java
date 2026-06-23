package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_host")
public class WorldHostJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false, length = 50)
    private String biome;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "visual_asset_key", length = 100)
    private String visualAssetKey;

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

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}
