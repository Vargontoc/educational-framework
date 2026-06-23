package es.vargontoc.educational.framework.world.ports.in;

import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldEngagementWindow;

public interface WorldOrchestrator {

    WorldDestinationSelectionResult selectDestination(Long childSessionId, Long childProfileId,
                                                    WorldEngagementWindow engagementWindow, Integer childAge);
}