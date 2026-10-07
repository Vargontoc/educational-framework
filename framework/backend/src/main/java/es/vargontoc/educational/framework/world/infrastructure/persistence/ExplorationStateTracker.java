package es.vargontoc.educational.framework.world.infrastructure.persistence;

import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExplorationStateTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExplorationStateTracker.class);

    private final Map<Long, TrackedExploration> trackedByProfile = new ConcurrentHashMap<>();
    private final WorldExplorationStateRepository worldExplorationStateRepository;

    public ExplorationStateTracker(WorldExplorationStateRepository worldExplorationStateRepository) {
        this.worldExplorationStateRepository = worldExplorationStateRepository;
    }

    public void recordPosition(Long childProfileId, Double positionX, Double positionY) {
        if (childProfileId == null || positionX == null || positionY == null) {
            return;
        }
        trackedByProfile.compute(childProfileId, (key, existing) -> {
            if (existing == null) {
                return new TrackedExploration(null, positionX, positionY, false);
            }
            return new TrackedExploration(existing.biome, positionX, positionY, existing.biomeDirty);
        });
    }

    public void recordBiomeChange(Long childProfileId, String biome, Double positionX, Double positionY) {
        if (childProfileId == null || biome == null) {
            return;
        }
        trackedByProfile.compute(childProfileId, (key, existing) -> {
            Double px = positionX != null ? positionX : (existing != null ? existing.positionX : null);
            Double py = positionY != null ? positionY : (existing != null ? existing.positionY : null);
            return new TrackedExploration(biome, px, py, true);
        });
    }

    public void setLastPersistedBiome(Long childProfileId, String biome) {
        if (childProfileId == null) {
            return;
        }
        trackedByProfile.compute(childProfileId, (key, existing) -> {
            Double px = existing != null ? existing.positionX : null;
            Double py = existing != null ? existing.positionY : null;
            return new TrackedExploration(null, px, py, false);
        });
    }

    public String getLastPersistedBiome(Long childProfileId) {
        TrackedExploration tracked = trackedByProfile.get(childProfileId);
        return tracked != null ? tracked.biome : null;
    }

    public void removeProfile(Long childProfileId) {
        trackedByProfile.remove(childProfileId);
    }

    @Scheduled(fixedDelayString = "${app.session.exploration-flush-interval-seconds:15}000")
    @Transactional
    public void flushAll() {
        if (trackedByProfile.isEmpty()) {
            return;
        }
        for (Map.Entry<Long, TrackedExploration> entry : trackedByProfile.entrySet()) {
            Long profileId = entry.getKey();
            TrackedExploration tracked = entry.getValue();
            if (!tracked.biomeDirty && tracked.positionX == null) {
                continue;
            }
            try {
                if (tracked.biomeDirty) {
                    worldExplorationStateRepository.updateBiomeAndPosition(
                        profileId, tracked.biome, tracked.positionX, tracked.positionY);
                    trackedByProfile.put(profileId, new TrackedExploration(tracked.biome, null, null, false));
                } else if (tracked.positionX != null) {
                    worldExplorationStateRepository.updatePosition(
                        profileId, tracked.positionX, tracked.positionY);
                    trackedByProfile.put(profileId, new TrackedExploration(null, null, null, false));
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to flush exploration state for childProfileId={}: {}", profileId, e.getMessage());
            }
        }
    }

    @Transactional
    public void flushAndRemove(Long childProfileId) {
        TrackedExploration tracked = trackedByProfile.remove(childProfileId);
        if (tracked == null) {
            return;
        }
        try {
            if (tracked.biomeDirty) {
                worldExplorationStateRepository.updateBiomeAndPosition(
                    childProfileId, tracked.biome, tracked.positionX, tracked.positionY);
            } else if (tracked.positionX != null) {
                worldExplorationStateRepository.updatePosition(
                    childProfileId, tracked.positionX, tracked.positionY);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to flush exploration state on close for childProfileId={}: {}", childProfileId, e.getMessage());
        }
    }

    private static class TrackedExploration {
        final String biome;
        final Double positionX;
        final Double positionY;
        final boolean biomeDirty;

        TrackedExploration(String biome, Double positionX, Double positionY, boolean biomeDirty) {
            this.biome = biome;
            this.positionX = positionX;
            this.positionY = positionY;
            this.biomeDirty = biomeDirty;
        }
    }
}
