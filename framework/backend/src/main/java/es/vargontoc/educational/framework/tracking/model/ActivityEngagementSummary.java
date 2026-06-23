package es.vargontoc.educational.framework.tracking.model;

public record ActivityEngagementSummary(
        String gameEngineType,
        long startedCount,
        long ignoredCount,
        long completedCount,
        long abandonedCount
) {
    public ActivityEngagementSummary {
        if (gameEngineType == null) {
            throw new IllegalArgumentException("gameEngineType cannot be null");
        }
        if (startedCount < 0) {
            throw new IllegalArgumentException("startedCount cannot be negative");
        }
        if (ignoredCount < 0) {
            throw new IllegalArgumentException("ignoredCount cannot be negative");
        }
        if (completedCount < 0) {
            throw new IllegalArgumentException("completedCount cannot be negative");
        }
        if (abandonedCount < 0) {
            throw new IllegalArgumentException("abandonedCount cannot be negative");
        }
    }
}