package es.vargontoc.educational.framework.world.ports.out;

import es.vargontoc.educational.framework.world.model.WorldExplorationState;

import java.util.Optional;

public interface WorldExplorationStateRepository {

    Optional<WorldExplorationState> findByChildProfileId(Long childProfileId);

    void save(WorldExplorationState state);
}
