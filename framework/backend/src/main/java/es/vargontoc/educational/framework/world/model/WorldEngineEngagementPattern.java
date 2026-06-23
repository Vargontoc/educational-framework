package es.vargontoc.educational.framework.world.model;

public class WorldEngineEngagementPattern {

    private String engineType;
    private int windowSize;
    private int abandonmentCount;
    private boolean patternDetected;

    public WorldEngineEngagementPattern() {
    }

    public WorldEngineEngagementPattern(String engineType, int windowSize, int abandonmentCount, boolean patternDetected) {
        this.engineType = engineType;
        this.windowSize = windowSize;
        this.abandonmentCount = abandonmentCount;
        this.patternDetected = patternDetected;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getAbandonmentCount() {
        return abandonmentCount;
    }

    public void setAbandonmentCount(int abandonmentCount) {
        this.abandonmentCount = abandonmentCount;
    }

    public boolean isPatternDetected() {
        return patternDetected;
    }

    public void setPatternDetected(boolean patternDetected) {
        this.patternDetected = patternDetected;
    }
}