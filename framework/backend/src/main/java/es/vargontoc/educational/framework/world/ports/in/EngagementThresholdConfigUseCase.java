package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldEngagementThresholdConfig;

public interface EngagementThresholdConfigUseCase {

    WorldEngagementThresholdConfig engagementThresholdConfig(Long childProfileId);
}