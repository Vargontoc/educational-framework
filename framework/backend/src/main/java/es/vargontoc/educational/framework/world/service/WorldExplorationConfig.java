package es.vargontoc.educational.framework.world.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.world.exploration")
public class WorldExplorationConfig {

    public static final int DEFAULT_MAX_VISIBLE_ELEMENTS = 3;
    public static final double DEFAULT_MIN_SEPARATION_NORMALIZED = 0.15;

    private int maxVisibleElements = DEFAULT_MAX_VISIBLE_ELEMENTS;
    private double minSeparationNormalized = DEFAULT_MIN_SEPARATION_NORMALIZED;

    public int getMaxVisibleElements() {
        return maxVisibleElements;
    }

    public void setMaxVisibleElements(int maxVisibleElements) {
        this.maxVisibleElements = maxVisibleElements;
    }

    public double getMinSeparationNormalized() {
        return minSeparationNormalized;
    }

    public void setMinSeparationNormalized(double minSeparationNormalized) {
        this.minSeparationNormalized = minSeparationNormalized;
    }
}
