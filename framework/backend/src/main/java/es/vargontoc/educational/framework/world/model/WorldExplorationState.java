package es.vargontoc.educational.framework.world.model;

import java.time.LocalDateTime;

public class WorldExplorationState {

    private Long childProfileId;
    private String biome;
    private Double positionX;
    private Double positionY;
    private LocalDateTime updatedAt;

    public WorldExplorationState() {
    }

    public WorldExplorationState(Long childProfileId, String biome, Double positionX, Double positionY, LocalDateTime updatedAt) {
        this.childProfileId = childProfileId;
        this.biome = biome;
        this.positionX = positionX;
        this.positionY = positionY;
        this.updatedAt = updatedAt;
    }

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
