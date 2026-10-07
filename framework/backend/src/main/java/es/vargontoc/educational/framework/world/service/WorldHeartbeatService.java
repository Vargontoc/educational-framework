package es.vargontoc.educational.framework.world.service;

import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.world.infrastructure.persistence.ExplorationStateTracker;
import es.vargontoc.educational.framework.world.model.WorldHeartbeatResult;
import es.vargontoc.educational.framework.world.model.WorldInactivityResult;
import es.vargontoc.educational.framework.world.model.WorldInactivityStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class WorldHeartbeatService implements WorldHeartbeatUseCase {

    private static final Logger log = LoggerFactory.getLogger(WorldHeartbeatService.class);

    private final WorldStateRegistry worldStateRegistry;
    private final WorldProposalService worldProposalService;
    private final ChildSessionUseCase childSessionUseCase;
    private final WorldInactivityConfig inactivityConfig;
    private final ExplorationStateTracker explorationStateTracker;

    public WorldHeartbeatService(WorldStateRegistry worldStateRegistry,
                                  WorldProposalService worldProposalService,
                                  ChildSessionUseCase childSessionUseCase,
                                  WorldInactivityConfig inactivityConfig,
                                  ExplorationStateTracker explorationStateTracker) {
        this.worldStateRegistry = worldStateRegistry;
        this.worldProposalService = worldProposalService;
        this.childSessionUseCase = childSessionUseCase;
        this.inactivityConfig = inactivityConfig;
        this.explorationStateTracker = explorationStateTracker;
    }

    @Override
    public WorldHeartbeatResult recordHeartbeat(Long childSessionId) {
        return recordHeartbeat(childSessionId, null, null, null);
    }

    @Override
    public WorldHeartbeatResult recordHeartbeat(Long childSessionId, Double positionX, Double positionY, String biome) {
        Optional<WorldState> worldStateOpt = worldStateRegistry.findByChildSessionId(childSessionId);

        if (worldStateOpt.isEmpty()) {
            log.debug("No WorldState found for childSessionId={}", childSessionId);
            childSessionUseCase.recordHeartbeat(childSessionId);
            return new WorldHeartbeatResult(childSessionId, false, null, true, WorldInactivityStatus.NO_WORLD_STATE);
        }

        WorldState worldState = worldStateOpt.get();

        WorldInactivityResult inactivityResult = checkAndHandleInactivity(worldState);

        if (inactivityResult.getStatus() == WorldInactivityStatus.INACTIVE_CLOSED) {
            childSessionUseCase.recordHeartbeat(childSessionId);
            return new WorldHeartbeatResult(
                childSessionId,
                true,
                worldState.getLastWorldActivityAt(),
                true,
                WorldInactivityStatus.INACTIVE_CLOSED
            );
        }

        worldState.setLastWorldActivityAt(LocalDateTime.now());
        worldState.setUpdatedAt(LocalDateTime.now());

        trackExplorationState(worldState.getChildProfileId(), positionX, positionY, biome);

        childSessionUseCase.recordHeartbeat(childSessionId);

        log.debug("World heartbeat recorded for childSessionId={}", childSessionId);

        return new WorldHeartbeatResult(
            childSessionId,
            true,
            worldState.getLastWorldActivityAt(),
            true,
            WorldInactivityStatus.ACTIVE
        );
    }

    private void trackExplorationState(Long childProfileId, Double positionX, Double positionY, String biome) {
        if (childProfileId == null) {
            return;
        }
        boolean hasPosition = positionX != null && positionY != null;
        boolean hasBiome = biome != null && !biome.isBlank();
        if (!hasPosition && !hasBiome) {
            return;
        }

        if (hasBiome) {
            String lastBiome = explorationStateTracker.getLastPersistedBiome(childProfileId);
            if (lastBiome == null || !lastBiome.equals(biome)) {
                Double clampedX = hasPosition ? clampNormalized(positionX) : null;
                Double clampedY = hasPosition ? clampNormalized(positionY) : null;
                explorationStateTracker.recordBiomeChange(childProfileId, biome, clampedX, clampedY);
            } else if (hasPosition) {
                explorationStateTracker.recordPosition(childProfileId, clampNormalized(positionX), clampNormalized(positionY));
            }
        } else if (hasPosition) {
            explorationStateTracker.recordPosition(childProfileId, clampNormalized(positionX), clampNormalized(positionY));
        }
    }

    private double clampNormalized(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private WorldInactivityResult checkAndHandleInactivity(WorldState worldState) {
        Long childSessionId = worldState.getChildSessionId();
        LocalDateTime lastActivity = worldState.getLastWorldActivityAt();

        if (lastActivity == null) {
            return new WorldInactivityResult(childSessionId, WorldInactivityStatus.ACTIVE, false, false, LocalDateTime.now());
        }

        long secondsSinceLastActivity = ChronoUnit.SECONDS.between(lastActivity, LocalDateTime.now());

        if (secondsSinceLastActivity >= inactivityConfig.getInactivityThresholdSeconds()) {
            log.info("World inactivity detected for childSessionId={}, secondsSinceLastActivity={}",
                childSessionId, secondsSinceLastActivity);

            boolean hadPendingProposal = worldState.getPendingProposalId() != null;

            worldProposalService.resolveAndCloseWorld(childSessionId);

            return new WorldInactivityResult(
                childSessionId,
                WorldInactivityStatus.INACTIVE_CLOSED,
                true,
                hadPendingProposal,
                LocalDateTime.now()
            );
        }

        return new WorldInactivityResult(childSessionId, WorldInactivityStatus.ACTIVE, false, false, LocalDateTime.now());
    }
}