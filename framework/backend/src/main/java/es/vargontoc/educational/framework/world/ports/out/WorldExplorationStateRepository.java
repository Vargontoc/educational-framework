package es.vargontoc.educational.framework.world.ports.out;

import es.vargontoc.educational.framework.world.model.WorldExplorationState;

import java.util.Optional;

public interface WorldExplorationStateRepository {

    Optional<WorldExplorationState> findByChildProfileId(Long childProfileId);

    void save(WorldExplorationState state);

    int updatePosition(Long childProfileId, Double positionX, Double positionY);

    int updateBiomeAndPosition(Long childProfileId, String biome, Double positionX, Double positionY);
}
