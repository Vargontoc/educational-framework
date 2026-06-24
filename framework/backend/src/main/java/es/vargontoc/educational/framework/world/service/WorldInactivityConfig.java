package es.vargontoc.educational.framework.world.service;

public class WorldInactivityConfig {

    public static final int DEFAULT_INACTIVITY_THRESHOLD_SECONDS = 180;

    private int inactivityThresholdSeconds = DEFAULT_INACTIVITY_THRESHOLD_SECONDS;

    public int getInactivityThresholdSeconds() {
        return inactivityThresholdSeconds;
    }

    public void setInactivityThresholdSeconds(int inactivityThresholdSeconds) {
        this.inactivityThresholdSeconds = inactivityThresholdSeconds;
    }
}