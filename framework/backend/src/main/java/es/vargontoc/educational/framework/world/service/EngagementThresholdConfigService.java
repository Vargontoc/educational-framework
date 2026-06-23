package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;
import es.vargontoc.educational.framework.world.ports.in.EngagementThresholdConfigUseCase;

public class EngagementThresholdConfigService implements EngagementThresholdConfigUseCase {

    private static final int DEFAULT_WINDOW_SIZE = 3;
    private static final int DEFAULT_ABANDONMENT_THRESHOLD = 2;

    @Override
    public WorldEngagementThresholdConfig engagementThresholdConfig(Long childProfileId) {
        WorldEngagementThresholdConfig config = new WorldEngagementThresholdConfig();
        config.setChildProfileId(childProfileId);
        config.setWindowSize(DEFAULT_WINDOW_SIZE);
        config.setAbandonmentThreshold(DEFAULT_ABANDONMENT_THRESHOLD);
        return config;
    }
}