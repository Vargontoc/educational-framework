package es.vargontoc.educational.framework.world.model;

public class WorldEngagementThresholdConfig {

    private Long childProfileId;
    private int windowSize = 3;
    private int abandonmentThreshold = 2;

    public WorldEngagementThresholdConfig() {
    }

    public WorldEngagementThresholdConfig(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getAbandonmentThreshold() {
        return abandonmentThreshold;
    }

    public void setAbandonmentThreshold(int abandonmentThreshold) {
        this.abandonmentThreshold = abandonmentThreshold;
    }
}