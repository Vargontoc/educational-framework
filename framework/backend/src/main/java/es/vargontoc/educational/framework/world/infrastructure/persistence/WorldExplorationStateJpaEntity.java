package es.vargontoc.educational.framework.world.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "world_exploration_state")
public class WorldExplorationStateJpaEntity {

    @Id
    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "biome", length = 50)
    private String biome;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
